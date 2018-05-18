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

import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.view.converters.MagnitudeResolutionConverter;
import org.bioimageanalysis.icy.icytomine.geom.GeometricHash;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import com.vividsolutions.jts.io.ParseException;

import be.cytomine.client.CytomineException;
import plugins.kernel.roi.roi2d.ROI2DPoint;
import plugins.kernel.roi.roi2d.ROI2DPolyLine;
import plugins.kernel.roi.roi2d.ROI2DPolygon;
import plugins.kernel.roi.roi2d.ROI2DShape;

public class AnnotationView {
	private static CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);

	private Image imageInformation;
	private List<Annotation> annotations; // All annotations in image

	private Rectangle2D.Double viewBoundsAtZeroResolution;
	private double targetResolution;
	private Dimension canvasSize;
	private GeometricHash<Annotation> annotationHash;
	private Set<Annotation> activeAnnotations; // annotations in the current field
																							// of view that might be visible
																							// or not
	private Set<Annotation> visibleAnnotations;
	private BufferedImage blankView;
	private BufferedImage currentView;

	private ThreadPoolExecutor annotationDrawingThreadPool;
	private Future<Void> currentDrawingTask;
	@SuppressWarnings("rawtypes")
	private Cache<Annotation, List> pointCache;

	private Set<ViewListener> listeners;

	private Point2D positionAtZeroResolution;

	public AnnotationView(Image imageInformation) throws CytomineException {
		this.imageInformation = imageInformation;
		this.activeAnnotations = new HashSet<>();
		this.visibleAnnotations = new HashSet<>();
		this.listeners = new HashSet<>();
		this.blankView = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
		startPointCache();
		retrieveAnnotations();
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

	private void retrieveAnnotations() throws CytomineException {
		annotations = imageInformation.getAnnotations();
		buildGeometricHash();
	}

	private void buildGeometricHash() {
		Rectangle imageBounds = new Rectangle(imageInformation.getSize());
		annotationHash = new GeometricHash<>(imageBounds, Math.max(1, annotations.size()));
		annotations.forEach(a -> {
			try {
				annotationHash.addObjectAt(a, a.getBounds());
			} catch (ParseException | CytomineException e) {
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
			} catch (InterruptedException e) {
				return null;
			} catch (Exception e) {
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
		activeAnnotations = annotationHash.cellObjectsAt(viewBoundsAtZeroResolution).parallelStream()
				.filter(a -> isActive(a)).collect(Collectors.toSet());
	}

	private boolean isActive(Annotation a) {
		return visibleAnnotations.contains(a);
	}

	private void drawAnnotations() throws InterruptedException {
		for (Annotation a : activeAnnotations) {
			if (Thread.interrupted())
				throw new InterruptedException();

			drawAnnotation(a);
		}
	}

	private void drawAnnotation(Annotation a) throws InterruptedException {
		try {
			List<Point2D> annotationPoints = getAnnotationPoints(a);
			ROI2DShape roi = a.getROI(0);
			if (roi instanceof ROI2DPoint) {
				drawPoint(annotationPoints, roi.getColor());
			} else if (roi instanceof ROI2DPolygon) {
				drawPolygon(annotationPoints, roi.getColor());
			} else if (roi instanceof ROI2DPolyLine) {
				drawPolyLine(annotationPoints, roi.getColor());
			}
		} catch (ParseException | CytomineException e) {
			e.printStackTrace();
			return;
		}
	}

	private List<Point2D> getAnnotationPoints(Annotation a) throws ParseException, CytomineException {
		@SuppressWarnings("unchecked")
		List<Point2D> points = (List<Point2D>) pointCache.get(a);
		if (points == null) {
			points = a.getROI(0).getPoints();
			pointCache.put(a, points);
		}
		return points;
	}

	private void drawPoint(List<Point2D> annotationPoints, Color color) {
		Graphics2D g2 = currentView.createGraphics();
		Point2D point = annotationPoints.get(0);
		int x = (int) MagnitudeResolutionConverter.convertMagnitude(point.getX() - viewBoundsAtZeroResolution.getMinX(), 0d,
				targetResolution);
		int y = (int) MagnitudeResolutionConverter.convertMagnitude(point.getY() - viewBoundsAtZeroResolution.getMinY(), 0d,
				targetResolution);
		g2.setColor(color);
		g2.setStroke(new BasicStroke(3));
		g2.drawOval(x - 2, y - 2, 4, 4);
		g2.dispose();
	}

	private void drawPolygon(List<Point2D> annotationPoints, Color color) throws InterruptedException {
		Graphics2D g2 = currentView.createGraphics();
		Point2D initPoint = annotationPoints.get(annotationPoints.size() - 1);
		int x1 = (int) MagnitudeResolutionConverter
				.convertMagnitude(initPoint.getX() - viewBoundsAtZeroResolution.getMinX(), 0d, targetResolution);
		int y1 = (int) MagnitudeResolutionConverter
				.convertMagnitude(initPoint.getY() - viewBoundsAtZeroResolution.getMinY(), 0d, targetResolution);

		g2.setColor(color);
		g2.setStroke(new BasicStroke(3));
		for (Point2D currentPoint : annotationPoints) {
			if (Thread.interrupted())
				throw new InterruptedException();
			int x2 = (int) MagnitudeResolutionConverter
					.convertMagnitude(currentPoint.getX() - viewBoundsAtZeroResolution.getMinX(), 0d, targetResolution);
			int y2 = (int) MagnitudeResolutionConverter
					.convertMagnitude(currentPoint.getY() - viewBoundsAtZeroResolution.getMinY(), 0d, targetResolution);
			g2.drawLine(x1, y1, x2, y2);
			x1 = x2;
			y1 = y2;
		}
		g2.dispose();
	}

	private void drawPolyLine(List<Point2D> annotationPoints, Color color) throws InterruptedException {
		Graphics2D g2 = currentView.createGraphics();
		int x1 = 0, y1 = 0;
		boolean first = true;

		g2.setColor(color);
		g2.setStroke(new BasicStroke(3));
		for (Point2D currentPoint : annotationPoints) {
			if (Thread.interrupted())
				throw new InterruptedException();
			int x2 = (int) MagnitudeResolutionConverter
					.convertMagnitude(currentPoint.getX() - viewBoundsAtZeroResolution.getMinX(), 0, targetResolution);
			int y2 = (int) MagnitudeResolutionConverter
					.convertMagnitude(currentPoint.getX() - viewBoundsAtZeroResolution.getMinY(), 0, targetResolution);
			if (!first) {
				g2.drawLine(x1, y1, x2, y2);
			} else {
				first = false;
			}
			x1 = x2;
			y1 = y2;
		}
		g2.dispose();
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

	public synchronized void forceViewRefresh() {
		cancelPreviousRequests();
		computeViewBoundsAtZeroResolution(positionAtZeroResolution, canvasSize);
		requestCurrentView(canvasSize);
	}

	public Set<Annotation> getVisibleAnnotations() {
		return new HashSet<>(visibleAnnotations);
	}

	public Set<Annotation> getActiveAnnotations() {
		return new HashSet<>(activeAnnotations);
	}

	public void updateModel() throws CytomineException {
		retrieveAnnotations();
		fillVisibleAnnotations();
	}

}
