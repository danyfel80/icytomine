package org.bioimageanalysis.icy.icytomine.core.view;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.view.ViewTileCache.ViewTileLoadListener;
import org.bioimageanalysis.icy.icytomine.core.view.converters.MagnitudeResolutionConverter;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.provider.ViewProvider.ViewProcessListener;

public class CachedImageView implements ViewTileLoadListener {

	private Image imageInformation;
	private BufferedImage currentView;
	private ViewTileCache tileCache;

	private Set<ViewListener> viewListeners;
	private Set<ViewProcessListener> viewProcessListeners;

	private double targetResolution;
	private long requestedResolution;

	private Rectangle2D imageBoundsAtZeroResolution;

	private Rectangle2D viewBoundsAtZeroResolution;
	private Rectangle2D viewBoundsAtTargetResolution;
	private Rectangle2D constrainedViewBoundsAtZeroResolution;
	private Rectangle2D constrainedViewBoundsAtRequestedResolution;

	private Rectangle tilesToRequest;
	private Dimension2D tileSizeAtRequestedResolution;
	private Dimension2D tileSizeAtTargetResolution;

	private BufferedImage lowResImage;
	@SuppressWarnings("unused")
	private BufferedImage previousView;
	@SuppressWarnings("unused")
	private double previousResolution;
	private Rectangle2D previousViewBoundsAtTargetResolution;

	public CachedImageView(Image imageInformation) {
		this.imageInformation = imageInformation;
		this.targetResolution = 0;
		this.viewBoundsAtZeroResolution = new Rectangle2D.Double(0, 0, 10, 10);
		this.viewBoundsAtTargetResolution = new Rectangle2D.Double(0, 0, 10, 10);
		this.previousViewBoundsAtTargetResolution = new Rectangle2D.Double(0, 0, 0, 0);
		this.constrainedViewBoundsAtZeroResolution = new Rectangle2D.Double(0, 0, 1, 1);
		this.constrainedViewBoundsAtRequestedResolution = new Rectangle2D.Double(0, 0, 1, 1);
		startCache();
		initializeListeners();
		computeImageBoundsAtZeroResolution();
		loadLowResImage();
	}

	private void startCache() {
		tileCache = new ViewTileCache(imageInformation);
		tileCache.addTileLoadedListener(this);
	}

	private void initializeListeners() {
		viewListeners = Collections.synchronizedSet(new HashSet<>());
		viewProcessListeners = Collections.synchronizedSet(new HashSet<>());
	}

	private void computeImageBoundsAtZeroResolution() {
		this.imageBoundsAtZeroResolution = new Rectangle(imageInformation.getSize().get());
	}

	private void loadLowResImage() {
		try {
			lowResImage = imageInformation.getThumbnail(512);
		} catch (CytomineClientException e) {
			lowResImage = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
			System.out.println("No thumbnail available...");
		}
	}

	public Image getImageInformation() {
		return imageInformation;
	}

	public void addViewListener(ViewListener listener) {
		this.viewListeners.add(listener);
	}

	public void addViewProcessListener(ViewProcessListener listener) {
		this.viewProcessListeners.add(listener);
	}

	public synchronized BufferedImage getView(Point2D targetPositionAt0Resolution, Dimension canvasSize,
			double targetResolution) {

		Rectangle2D newViewBoundsAtZeroResolution = createViewBoundsAtZeroResolution(targetPositionAt0Resolution,
				canvasSize, targetResolution);

		if (isNewRequest(newViewBoundsAtZeroResolution, targetResolution)) {
			tileCache.cancelPreviousRequest();
			notifyProcessListeners(true);
			setPreviousViewValues();
			setCurrentParameters(newViewBoundsAtZeroResolution, canvasSize, targetResolution);
			requestView();
		}
		return currentView;
	}

	private Rectangle2D createViewBoundsAtZeroResolution(Point2D viewPositionAt0Resolution, Dimension canvasSize,
			double targetResolution) {
		Dimension2D canvasSizeAtZeroResolution = MagnitudeResolutionConverter.convertDimension2D(canvasSize,
				targetResolution, 0d);
		return new Rectangle2D.Double(viewPositionAt0Resolution.getX(), viewPositionAt0Resolution.getY(),
				canvasSizeAtZeroResolution.getWidth(), canvasSizeAtZeroResolution.getHeight());
	}

	private boolean isNewRequest(Rectangle2D newViewBoundsAtZeroResolution, double newResolution) {
		return !newViewBoundsAtZeroResolution.equals(this.viewBoundsAtZeroResolution)
				|| newResolution != this.targetResolution;
	}

	private void notifyProcessListeners(boolean isProcessing) {
		viewProcessListeners.forEach(l -> l.onViewProcessEvent(isProcessing));
	}

	private void setPreviousViewValues() {
		this.previousResolution = this.targetResolution;
		this.previousViewBoundsAtTargetResolution.setFrame(this.viewBoundsAtTargetResolution);
		this.previousView = this.currentView;
	}

	private void setCurrentParameters(Rectangle2D newViewBoundsAtZeroResolution, Dimension canvasSize,
			double targetResolution) {
		this.targetResolution = targetResolution;
		this.viewBoundsAtZeroResolution.setRect(newViewBoundsAtZeroResolution);
		Point2D viewPositionAtTargetResolution = MagnitudeResolutionConverter.convertPoint2D(
				new Point2D.Double(newViewBoundsAtZeroResolution.getX(), newViewBoundsAtZeroResolution.getY()), 0d,
				targetResolution);
		this.viewBoundsAtTargetResolution.setFrame(viewPositionAtTargetResolution, canvasSize);
	}

	private void requestView() {
		computeViewRequestParameters();
		initializeCurrentView();
		requestTiles();
	}

	private void computeViewRequestParameters() {
		computeRequestedResolution();
		computeRequestedParameters();
	}

	private void computeRequestedResolution() {
		requestedResolution = Math.min(Math.max(0, (long) targetResolution), imageInformation.getDepth().get());
	}

	private void computeRequestedParameters() {
		computeConstrainedParameters();
		computeTileRequestParameters();
	}

	private void computeConstrainedParameters() {
		this.constrainedViewBoundsAtZeroResolution
				.setRect(intersectRectangles(imageBoundsAtZeroResolution, viewBoundsAtZeroResolution));
		this.constrainedViewBoundsAtRequestedResolution = MagnitudeResolutionConverter
				.convertRectangle2D(constrainedViewBoundsAtZeroResolution, 0d, requestedResolution);
	}

	private Rectangle2D intersectRectangles(Rectangle2D rect1, Rectangle2D rect2) {
		double maxStartX = Math.max(rect1.getX(), rect2.getX());
		double maxStartY = Math.max(rect1.getY(), rect2.getY());
		double minEndX = Math.min(rect1.getMaxX(), rect2.getMaxX());
		double minEndY = Math.min(rect1.getMaxY(), rect2.getMaxY());

		double width = 0;
		double height = 0;
		if (minEndX > maxStartX) {
			width = minEndX - maxStartX;
		}
		if (minEndY > maxStartY) {
			height = minEndY - maxStartY;
		}
		return new Rectangle2D.Double(maxStartX, maxStartY, width, height);
	}

	private void computeTileRequestParameters() {
		computeTileSize();
		int x = Math.max(0,
				(int) (constrainedViewBoundsAtRequestedResolution.getX() / tileSizeAtRequestedResolution.getWidth()));
		int y = Math.max(0,
				(int) (constrainedViewBoundsAtRequestedResolution.getY() / tileSizeAtRequestedResolution.getHeight()));
		int maxX = (constrainedViewBoundsAtRequestedResolution.getWidth() > 0d) ? ceilDiv(
				(int) constrainedViewBoundsAtRequestedResolution.getMaxX(), (int) tileSizeAtRequestedResolution.getWidth()) : 0;
		int maxY = (constrainedViewBoundsAtRequestedResolution.getHeight() > 0d)
				? ceilDiv((int) constrainedViewBoundsAtRequestedResolution.getMaxY(),
						(int) tileSizeAtRequestedResolution.getHeight())
				: 0;
		this.tilesToRequest = new Rectangle(x, y, Math.max(0, maxX - x), Math.max(0, maxY - y));
	}

	private void computeTileSize() {
		tileSizeAtRequestedResolution = imageInformation.getTileSize().get();
		tileSizeAtTargetResolution = MagnitudeResolutionConverter.convertDimension2D(tileSizeAtRequestedResolution,
				requestedResolution, targetResolution);
	}

	private int ceilDiv(int a, int b) {
		return a / b + (a % b == 0 ? 0 : 1);
	}

	private void initializeCurrentView() {
		currentView = new BufferedImage((int) viewBoundsAtTargetResolution.getWidth(),
				(int) viewBoundsAtTargetResolution.getHeight(), BufferedImage.TYPE_INT_ARGB);
		paintLowResolutionViewInCurrentView();
		paintPreviousViewInCurrentView();
		notifyTileLoaded();
	}

	private void paintLowResolutionViewInCurrentView() {
		Graphics2D painter = currentView.createGraphics();
		Dimension2D imageSizeAtTargetResolution = getImageSizeAtTargetResolution();
		painter.drawImage(lowResImage, (int) -viewBoundsAtTargetResolution.getX(),
				(int) -viewBoundsAtTargetResolution.getY(), (int) imageSizeAtTargetResolution.getWidth(),
				(int) imageSizeAtTargetResolution.getHeight(), null);
		painter.dispose();
	}

	private void paintPreviousViewInCurrentView() {
		// TODO NOT WORKING BECAUSE MULTIPLE REQUESTS CANCELLED ARE STILL PRINTED
		// if (previousView != null &&
		// !previousViewBoundsAtTargetResolution.isEmpty()) {
		// Rectangle2D previousViewBoundsAtNewResolution =
		// MagnitudeResolutionConverter
		// .convertRectangle2D(previousViewBoundsAtTargetResolution,
		// previousResolution, targetResolution);
		// Point2D previousViewPositionAtNewView = new Point2D.Double(
		// previousViewBoundsAtNewResolution.getX() -
		// viewBoundsAtTargetResolution.getX(),
		// previousViewBoundsAtNewResolution.getY() -
		// viewBoundsAtTargetResolution.getY());
		//
		// Graphics2D painter = currentView.createGraphics();
		// painter.drawImage(previousView, (int)
		// previousViewPositionAtNewView.getX(),
		// (int) previousViewPositionAtNewView.getY(), (int)
		// previousViewBoundsAtNewResolution.getWidth(),
		// (int) previousViewBoundsAtNewResolution.getHeight(), null);
		// painter.dispose();
		// }
	}

	private Dimension2D getImageSizeAtTargetResolution() {
		return MagnitudeResolutionConverter.convertDimension2D(
				new Dimension((int) imageBoundsAtZeroResolution.getWidth(), (int) imageBoundsAtZeroResolution.getHeight()), 0d,
				targetResolution);
	}

	private void requestTiles() {
		for (int i = tilesToRequest.x; i < tilesToRequest.getMaxX(); i++) {
			for (int j = tilesToRequest.y; j < tilesToRequest.getMaxY(); j++) {
				tileCache.requestTile(requestedResolution, i, j);
			}
		}
	}

	@Override
	public void tileLoaded(Tile2DKey tileKey, BufferedImage tileImage) {
		Point position = getTilePositionInView(tileKey);
		drawTileInCurrentView(position, tileImage);
		notifyTileLoaded();
	}

	private Point getTilePositionInView(Tile2DKey tileKey) {
		Point tilePositionAtTargetResolution = getTilePositonAtTargetResolution(tileKey);
		return new Point((int) (tilePositionAtTargetResolution.x - viewBoundsAtTargetResolution.getX()),
				(int) (tilePositionAtTargetResolution.y - viewBoundsAtTargetResolution.getY()));
	}

	private Point getTilePositonAtTargetResolution(Tile2DKey tileKey) {
		int x = (int) (tileKey.getX() * tileSizeAtTargetResolution.getWidth());
		int y = (int) (tileKey.getY() * tileSizeAtTargetResolution.getHeight());
		return new Point(x, y);
	}

	private void drawTileInCurrentView(Point position, BufferedImage tileImage) {
		Graphics2D g = currentView.createGraphics();
		Dimension2D tileSize = getTileSize(new Dimension(tileImage.getWidth(), tileImage.getHeight()));
		g.drawImage(tileImage, position.x, position.y, (int) Math.ceil(tileSize.getWidth()),
				(int) Math.ceil(tileSize.getHeight()), null);
		g.dispose();
	}

	private Dimension2D getTileSize(Dimension tileImageSize) {
		if (tileSizeAtRequestedResolution.equals(tileImageSize))
			return tileSizeAtTargetResolution;
		else
			return MagnitudeResolutionConverter.convertDimension2D(tileImageSize, requestedResolution, targetResolution);
	}

	private void notifyTileLoaded() {
		viewListeners.stream().forEach(l -> l.onViewChanged(currentView));
		tryNotifyProcess();
	}

	private void tryNotifyProcess() {
		if (!isTileCacheProcessing()) {
			notifyProcessListeners(false);
		}
	}

	private boolean isTileCacheProcessing() {
		return tileCache.isProcessing();
	}

	public void stop() {
		tileCache.stop();
	}

}
