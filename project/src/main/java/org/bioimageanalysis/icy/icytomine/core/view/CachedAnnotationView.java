package org.bioimageanalysis.icy.icytomine.core.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;
import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.core.model.Entity;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.view.converters.MagnitudeResolutionConverter;
import org.bioimageanalysis.icy.icytomine.geom.GeometricHash;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import be.cytomine.client.CytomineException;
import icy.plugin.PluginLoader;

public class CachedAnnotationView {
	private static CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
			.withClassLoader(PluginLoader.getLoader()).build(true);

	private Image imageInformation;
	private List<Annotation> annotations; // All annotations in image

	private Rectangle2D.Double viewBoundsAtZeroResolution;
	private double targetResolution;
	private Dimension canvasSize;
	private GeometricHash<Annotation> annotationHash;

	/**
	 * Annotations that should be drawn in the view.
	 */
	private Set<Annotation> visibleAnnotations;
	/**
	 * Annotations in the current field of view that are visible.
	 */
	private Set<Annotation> activeAnnotations;
	/**
	 * Annotations that should be highlighted in the view when active.
	 */
	private Set<Annotation> selectedAnnotations;

	private BufferedImage blankView;

	private BufferedImage currentView;

	private ThreadPoolExecutor annotationDrawingThreadPool;
	private Future<Void> currentDrawingTask;
	@SuppressWarnings("rawtypes")
	private Cache<Annotation, List> pointCache;

	private Set<ViewListener> listeners;

	private Point2D positionAtZeroResolution;

	public CachedAnnotationView(Image imageInformation) throws CytomineException {
		this.imageInformation = imageInformation;
		this.visibleAnnotations = new HashSet<>();
		this.activeAnnotations = new HashSet<>();
		this.selectedAnnotations = new HashSet<>();
		this.listeners = new HashSet<>();
		this.blankView = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
		startPointCache();
		retrieveAnnotations(false);
		fillVisibleAnnotations();
		startDrawingThread();
	}

	private void startPointCache() {
		String cacheName = "PointCache" + this.hashCode();
		this.pointCache = cacheManager.getCache(cacheName, Annotation.class, List.class);
		if (pointCache == null) {
			pointCache = cacheManager.createCache(cacheName, CacheConfigurationBuilder
					.newCacheConfigurationBuilder(Annotation.class, List.class, ResourcePoolsBuilder.heap(100)).build());
		}
	}

	private void retrieveAnnotations(boolean downloadAgain) throws CytomineClientException {
		if (downloadAgain) {
			System.out.print("Downloading annotations...");
		} else {
			System.out.print("Refreshing annotations...");
		}
		long start = System.currentTimeMillis();
		annotations = imageInformation.getAnnotationsWithGeometry(downloadAgain);
		buildGeometricHash();
		long annotationsTime = System.currentTimeMillis() - start;
		System.out.format("%d milliseconds\n", annotationsTime);
	}

	private void buildGeometricHash() {
		Rectangle imageBounds = new Rectangle(imageInformation.getSize().get());
		annotationHash = new GeometricHash<>(imageBounds, Math.max(1, annotations.size()));
		annotations.parallelStream().forEach(a -> {
			try {
				Rectangle2D bounds = a.getYAdjustedApproximativeBounds();
				annotationHash.addObjectAt(a, bounds);
			} catch (CytomineClientException e) {
				e.printStackTrace();
				return;
			}
		});
	}

	private void fillVisibleAnnotations() {
		visibleAnnotations = new HashSet<>(annotations);
	}

	private void startDrawingThread() {
		annotationDrawingThreadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
		annotationDrawingThreadPool.prestartAllCoreThreads();
	}

	public synchronized BufferedImage getView(Point2D positionAtZeroResolution, Dimension canvasSize,
			double targetResolution) {
		if (this.canvasSize == null || !this.canvasSize.equals(canvasSize)) {
			blankView = new BufferedImage(canvasSize.width, canvasSize.height, BufferedImage.TYPE_INT_ARGB);
			this.canvasSize = canvasSize;
		}
		cancelPreviousRequests();
		this.targetResolution = targetResolution;
		this.positionAtZeroResolution = positionAtZeroResolution;
		computeViewBoundsAtZeroResolution(positionAtZeroResolution, canvasSize);
		requestCurrentView(canvasSize);

		return blankView;
	}

	private void cancelPreviousRequests() {
		if (currentDrawingTask != null) {
			currentDrawingTask.cancel(true);
			annotationDrawingThreadPool.purge();
		}
	}

	private void computeViewBoundsAtZeroResolution(Point2D positionAtZeroResolution, Dimension canvasSize) {
		Dimension2D canvasSizeAtZeroResolution = MagnitudeResolutionConverter.convertDimension2D(canvasSize,
				targetResolution, 0d);
		viewBoundsAtZeroResolution = new Rectangle2D.Double(positionAtZeroResolution.getX(),
				positionAtZeroResolution.getY(), canvasSizeAtZeroResolution.getWidth(), canvasSizeAtZeroResolution.getHeight());
	}

	private void requestCurrentView(Dimension canvasSize) {
		currentDrawingTask = annotationDrawingThreadPool.submit(() -> {
			try {
				initializeCurrentView(canvasSize);
				computeActiveAnnotations();
				drawAnnotations();
			} catch (InterruptedException e) {} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
			notifyViewReady();
			return null;
		});
	}

	private void initializeCurrentView(Dimension canvasSize) {
		currentView = new BufferedImage(canvasSize.width, canvasSize.height, BufferedImage.TYPE_INT_ARGB);
	}

	private void computeActiveAnnotations() throws InterruptedException {
		activeAnnotations = annotationHash.cellObjectsAt(viewBoundsAtZeroResolution).stream()
				.filter(a -> viewBoundsAtZeroResolution.intersects(a.getYAdjustedApproximativeBounds()))
				.filter(a -> isVisible(a)).collect(Collectors.toSet());
	}

	private boolean isVisible(Entity a) {
		return visibleAnnotations.contains(a);
	}

	private void drawAnnotations() throws CytomineClientException, AnnotationViewException, InterruptedException {
		annotations.parallelStream().forEach(a -> a.getSimplifiedGeometryForResolution((int) targetResolution));
		for (Annotation a: activeAnnotations) {
			if (Thread.interrupted())
				throw new InterruptedException();
			boolean selected = selectedAnnotations.contains(a);
			drawAnnotation(a, selected);
		}
	}

	private void drawAnnotation(Annotation a, boolean selected) throws CytomineClientException, AnnotationViewException {
		Geometry geometry;
		if (targetResolution > 0) {
			geometry = a.getSimplifiedGeometryForResolution((int) targetResolution);
		} else {
			geometry = a.getGeometryAtZeroResolution(false);
		}

		Color color = a.getColor();
		try {
			drawGeometry(geometry, color, selected);
		} catch (AnnotationViewException e) {
			throw new AnnotationViewException(String.format("Could not draw annotation %d", a.getId()), e);
		}
	}

	private void drawGeometry(Geometry geometry, Color color, boolean selected) {
		if (geometry instanceof Point) {
			drawPoint((Point) geometry, color, selected);
		} else if (geometry instanceof LineString) {
			drawLineString((LineString) geometry, color, selected);
		} else if (geometry instanceof Polygon) {
			drawPolygon((Polygon) geometry, color, selected);
		}
		// TODO implement multi point
		// TODO implement multi line string
		else if (geometry instanceof MultiPolygon) {
			drawMultiPolygon((MultiPolygon) geometry, color, selected);
		} else if (geometry != null) {
			throw new AnnotationViewException(
					String.format("Unsupported annotation geometry (%s)", geometry.getGeometryType()));
		} else {
			throw new AnnotationViewException("Null geometry");
		}
	}

	private void drawPoint(Point point, Color color, boolean selected) {
		int maxY = imageInformation.getSizeY().get();

		int x = (int) MagnitudeResolutionConverter.convertMagnitude(point.getX() - viewBoundsAtZeroResolution.getMinX(), 0d,
				targetResolution);
		int y = (int) MagnitudeResolutionConverter
				.convertMagnitude((maxY - point.getY()) - viewBoundsAtZeroResolution.getMinY(), 0d, targetResolution);
		int radius = 4;
		int diameter = 2 * radius;

		Graphics2D g2 = currentView.createGraphics();
		if (selected) {
			g2.setColor(Color.BLACK);
			g2.setStroke(new BasicStroke(getStrokeThickness(selected) + 2));
			g2.drawLine(x - radius, y - radius, x + radius, y + radius);
			g2.drawLine(x + radius, y - radius, x - radius, y + radius);
		}
		g2.setColor(color);
		g2.setStroke(new BasicStroke(getStrokeThickness(selected)));
		g2.drawLine(x - radius, y - radius, x + radius, y + radius);
		g2.drawLine(x + radius, y - radius, x - radius, y + radius);
		if (selected) {
			g2.setColor(getSelectedFillColor(color));
			g2.fillOval(x - radius, y - radius, diameter, diameter);
		}
		g2.dispose();
	}

	private int getStrokeThickness(boolean selected) {
		return (selected? 3: 2);
	}

	private Color getSelectedFillColor(Color color) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), 77);
	}

	private void drawLineString(LineString geometry, Color color, boolean selected) {
		int maxY = imageInformation.getSizeY().get();
		CoordinateSequence coordinates = geometry.getCoordinateSequence();
		int size = coordinates.size();
		int[] xPoints = new int[size];
		int[] yPoints = new int[size];
		IntStream.range(0, size).forEach(i -> {
			Coordinate coordinate = coordinates.getCoordinate(i);
			xPoints[i] = (int) MagnitudeResolutionConverter
					.convertMagnitude(coordinate.x - viewBoundsAtZeroResolution.getMinX(), 0, targetResolution);
			yPoints[i] = (int) MagnitudeResolutionConverter
					.convertMagnitude((maxY - coordinate.y) - viewBoundsAtZeroResolution.getMinY(), 0, targetResolution);
		});

		// In the case of all coordinates are the same, move a point to make the
		// polygon visible
		if (xPoints[0] == xPoints[1] && yPoints[0] == yPoints[1]) {
			xPoints[1]++;
			yPoints[1]++;
		}

		Graphics2D g2 = currentView.createGraphics();
		if (selected) {
			g2.setColor(Color.BLACK);
			g2.setStroke(new BasicStroke(getStrokeThickness(selected) + 2));
			g2.drawPolyline(xPoints, yPoints, size);
		}
		g2.setColor(color);
		g2.setStroke(new BasicStroke(getStrokeThickness(selected)));
		g2.drawPolyline(xPoints, yPoints, size);
		g2.dispose();
	}

	private void drawPolygon(Polygon geometry, Color color, boolean selected) {
		int maxY = imageInformation.getSizeY().get();
		Coordinate[] coordinates = geometry.getCoordinates();
		int size = coordinates.length;
		int[] xPoints = new int[size];
		int[] yPoints = new int[size];
		IntStream.range(0, size).forEach(i -> {
			Coordinate coordinate = coordinates[i];
			xPoints[i] = (int) MagnitudeResolutionConverter
					.convertMagnitude(coordinate.x - viewBoundsAtZeroResolution.getMinX(), 0, targetResolution);
			yPoints[i] = (int) MagnitudeResolutionConverter
					.convertMagnitude((maxY - coordinate.y) - viewBoundsAtZeroResolution.getMinY(), 0, targetResolution);
		});

		// In the case of all coordinates are the same, move a point to make the
		// polygon visible
		if (xPoints[0] == xPoints[1] && yPoints[0] == yPoints[1]) {
			xPoints[1]++;
			yPoints[1]++;
		}

		Graphics2D g2 = currentView.createGraphics();
		if (selected) {
			g2.setColor(Color.BLACK);
			g2.setStroke(new BasicStroke(getStrokeThickness(selected) + 2));
			g2.drawPolygon(xPoints, yPoints, size);
		}

		g2.setColor(color);
		g2.setStroke(new BasicStroke(getStrokeThickness(selected)));
		g2.drawPolygon(xPoints, yPoints, size);

		if (selected) {
			g2.setColor(getSelectedFillColor(color));
			g2.fillPolygon(xPoints, yPoints, size);
		}

		g2.dispose();
	}

	private void drawMultiPolygon(MultiPolygon geometry, Color color, boolean selected) {
		int numGeometries = geometry.getNumGeometries();
		for (int i = 0; i < numGeometries; i++) {
			Geometry subGeometry = geometry.getGeometryN(i);
			drawGeometry(subGeometry, color, selected);
		}
	}

	private void notifyViewReady() {
		this.listeners.forEach(l -> l.onViewChanged(currentView));
	}

	public void stop() {
		cacheManager.removeCache("PointCache" + this.hashCode());
		this.annotationDrawingThreadPool.shutdownNow();
		try {
			this.annotationDrawingThreadPool.awaitTermination(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public void addViewListener(ViewListener listener) {
		this.listeners.add(listener);
	}

	public void setVisibleAnnotations(Set<Annotation> newVisibleAnnotations) {
		this.visibleAnnotations = newVisibleAnnotations;
	}

	public Set<Annotation> getVisibleAnnotations() {
		return new HashSet<>(visibleAnnotations);
	}

	public Set<Annotation> getActiveAnnotations() {
		return new HashSet<>(activeAnnotations);
	}

	public void setSelectedAnnotations(Set<Annotation> selectedAnnotations) {
		this.selectedAnnotations = selectedAnnotations;
	}

	public Set<Annotation> getSelectedAnnotations() {
		return new HashSet<>(selectedAnnotations);
	}

	public synchronized void forceViewRefresh() {
		cancelPreviousRequests();
		computeViewBoundsAtZeroResolution(positionAtZeroResolution, canvasSize);
		requestCurrentView(canvasSize);
	}

	public void updateModel(boolean downloadAgain) throws CytomineClientException {
		retrieveAnnotations(downloadAgain);
		fillVisibleAnnotations();
	}

}
