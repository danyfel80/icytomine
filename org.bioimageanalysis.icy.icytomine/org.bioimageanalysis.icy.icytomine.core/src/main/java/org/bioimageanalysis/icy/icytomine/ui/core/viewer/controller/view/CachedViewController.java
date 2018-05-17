package org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.view.converters.MagnitudeResolutionConverter;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.view.ViewCanvasPanel;

public class CachedViewController implements ViewController {

	public class RepaintOnResizeListener extends ComponentAdapter {
		@Override
		public void componentResized(ComponentEvent e) {
			viewCanvasPanel.updateCanvas();
			viewCanvasPanel.invalidate();
		}

		@Override
		public void componentShown(ComponentEvent e) {
			viewCanvasPanel.invalidate();
		}
	}

	public interface ResolutionListener {
		void resolutionChanged(double newResolution);
	}

	public interface PositionListener {
		void positionChanged(Point2D newPosition);
	}

	private Image imageInformation;
	private ViewCanvasPanel viewCanvasPanel;

	private Point2D viewPositionAt0Resolution;
	private double resolutionLevel;

	private Point2D lastDragStartPositionInView;
	private Point2D lastDragStartPositionInImage;

	private Collection<ResolutionListener> resolutionListeners;
	private Collection<PositionListener> cursorPositionListeners;

	public CachedViewController(Image imageInformation, ViewCanvasPanel viewCanvasPanel) {
		this.imageInformation = imageInformation;
		this.resolutionListeners = new HashSet<>();
		this.cursorPositionListeners = new HashSet<>();
		this.viewCanvasPanel = viewCanvasPanel;
		this.viewPositionAt0Resolution = new Point2D.Double();
		this.resolutionLevel = 0;
		this.lastDragStartPositionInView = new Point2D.Double();
		this.lastDragStartPositionInImage = new Point2D.Double();
		setCanvasEventListeners();
	}

	private void setCanvasEventListeners() {
		setCanvasMouseEventListeners();
		setCanvasComponentEventListeners();
	}

	private void setCanvasMouseEventListeners() {
		viewCanvasPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				startMouseDragAt(e.getPoint());
			}
		});

		viewCanvasPanel.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				mouseDragTo(e.getPoint());
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				notifyMouseMovedTo(e.getPoint());
			}
		});

		viewCanvasPanel.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				notifyMouseWheelMovedTo(e.getWheelRotation(), e.getPoint());
			}
		});
	}

	private void setCanvasComponentEventListeners() {
		viewCanvasPanel.addComponentListener(new RepaintOnResizeListener());
	}

	private void adjustZoomToFitInView() {
		adjustResolutionToFitInView();
		viewPositionAt0Resolution = new Point2D.Double();
		viewCanvasPanel.getViewProvider().setPosition(viewPositionAt0Resolution);
		viewCanvasPanel.getViewProvider().setResolutionLevel(resolutionLevel);
		notifyResolutionChanged();
	}

	private void adjustResolutionToFitInView() {
		Dimension presentedImageDimension = imageInformation.getSize();
		Dimension viewDimension = viewCanvasPanel.getBounds().getSize();

		double widthAdjustedResolution = Math.log(presentedImageDimension.getWidth() / viewDimension.getWidth())
				/ Math.log(2d);
		double heightAdjustedResolution = Math.log(presentedImageDimension.getHeight() / viewDimension.getHeight())
				/ Math.log(2d);
		resolutionLevel = Math.min(Math.max(widthAdjustedResolution, heightAdjustedResolution),
				imageInformation.getDepth() + 1);
	}

	private void notifyResolutionChanged() {
		resolutionListeners.forEach(l -> l.resolutionChanged(resolutionLevel));
	}

	private void startMouseDragAt(Point position) {
		lastDragStartPositionInView = position;
		lastDragStartPositionInImage.setLocation(viewPositionAt0Resolution);
	}

	private void mouseDragTo(Point position) {
		moveViewBy(lastDragStartPositionInView.getX() - position.getX(),
				lastDragStartPositionInView.getY() - position.getY());
		refreshView();
	}

	private void moveViewBy(double x, double y) {
		Point2D deltaPosition = MagnitudeResolutionConverter.convertPoint2D(new Point2D.Double(x, y), resolutionLevel, 0d);

		viewPositionAt0Resolution.setLocation(lastDragStartPositionInImage.getX() + deltaPosition.getX(),
				lastDragStartPositionInImage.getY() + deltaPosition.getY());
	}

	protected void notifyMouseMovedTo(Point2D cursorPosition) {
		Point2D cursorPositionOnImage = getImagePositionInCurrentView(cursorPosition);
		notifyCursorPositionChanged(cursorPositionOnImage);
	}

	private Point2D getImagePositionInCurrentView(Point2D positionInView) {
		Point2D positionInViewAtZeroResolution = MagnitudeResolutionConverter.convertPoint2D(positionInView,
				resolutionLevel, 0d);
		return getImagePositionFromViewPosition(positionInViewAtZeroResolution);
	}

	private Point2D getImagePositionFromViewPosition(Point2D positionInViewAtZeroResolution) {
		return new Point2D.Double(viewPositionAt0Resolution.getX() + positionInViewAtZeroResolution.getX(),
				viewPositionAt0Resolution.getY() + positionInViewAtZeroResolution.getY());
	}

	private void notifyCursorPositionChanged(Point2D cursorPositionOnImage) {
		cursorPositionListeners.forEach(l -> l.positionChanged(cursorPositionOnImage));
	}

	protected void notifyMouseWheelMovedTo(int wheelRotation, Point position) {
		Point2D cursorPositionInImageAtZeroResolution = getCursorPositionOnImageAtZeroResolution(position);
		resolutionLevel = resolutionLevel + (0.25d * wheelRotation);
		viewPositionAt0Resolution = getViewPositionAtZeroResolutionFromCursorPosition(cursorPositionInImageAtZeroResolution,
				position);
		refreshView();
	}

	private Point2D getCursorPositionOnImageAtZeroResolution(Point position) {
		Point2D positionInViewAtZeroResolution = MagnitudeResolutionConverter.convertPoint2D(position, resolutionLevel, 0d);
		return new Point2D.Double(viewPositionAt0Resolution.getX() + positionInViewAtZeroResolution.getX(),
				viewPositionAt0Resolution.getY() + positionInViewAtZeroResolution.getY());
	}

	private Point2D getViewPositionAtZeroResolutionFromCursorPosition(Point2D cursorPositionAtZeroResolution,
			Point cursorPosititionInView) {
		Point2D positionInViewAtZeroResolution = MagnitudeResolutionConverter.convertPoint2D(cursorPosititionInView,
				resolutionLevel, 0d);
		return new Point2D.Double(cursorPositionAtZeroResolution.getX() - positionInViewAtZeroResolution.getX(),
				cursorPositionAtZeroResolution.getY() - positionInViewAtZeroResolution.getY());
	}

	@Override
	public Image getImageInformation() {
		return imageInformation;
	}

	@Override
	public void addResolutionListener(ResolutionListener listener) {
		resolutionListeners.add(listener);
	}

	@Override
	public void addCursorPositionListener(PositionListener listener) {
		cursorPositionListeners.add(listener);
	}

	@Override
	public void zoomIn() {
		Point2D centerPositionAtZeroResolution = getViewCenterPositionAtZeroResolution();
		resolutionLevel -= 1d;
		viewPositionAt0Resolution = getViewPositionAtZeroResolutionFromCenterPosition(centerPositionAtZeroResolution);
		refreshView();
	}

	@Override
	public void zoomOut() {
		Point2D centerPositionAtZeroResolution = getViewCenterPositionAtZeroResolution();
		resolutionLevel += 1d;
		viewPositionAt0Resolution = getViewPositionAtZeroResolutionFromCenterPosition(centerPositionAtZeroResolution);
		refreshView();
	}

	private Point2D getViewCenterPositionAtZeroResolution() {
		Dimension2D canvasSizeAtZeroResolution = MagnitudeResolutionConverter.convertDimension2D(viewCanvasPanel.getSize(),
				resolutionLevel, 0d);
		return new Point2D.Double(viewPositionAt0Resolution.getX() + (canvasSizeAtZeroResolution.getWidth() / 2d),
				viewPositionAt0Resolution.getY() + (canvasSizeAtZeroResolution.getHeight() / 2d));
	}

	private Point2D getViewPositionAtZeroResolutionFromCenterPosition(Point2D centerPositionAtZeroResolution) {
		Dimension2D canvasSizeAtZeroResolution = MagnitudeResolutionConverter.convertDimension2D(viewCanvasPanel.getSize(),
				resolutionLevel, 0d);
		return new Point2D.Double(centerPositionAtZeroResolution.getX() - (canvasSizeAtZeroResolution.getWidth() / 2d),
				centerPositionAtZeroResolution.getY() - (canvasSizeAtZeroResolution.getHeight() / 2d));
	}

	@Override
	public void setResolution(double resolutionLevel) {
		Point2D centerPositionAtZeroResolution = getViewCenterPositionAtZeroResolution();
		this.resolutionLevel = resolutionLevel;
		viewPositionAt0Resolution = getViewPositionAtZeroResolutionFromCenterPosition(centerPositionAtZeroResolution);
		refreshView();
	}

	@Override
	public void adjustImageZoomToView() {
		adjustZoomToFitInView();
		refreshView();
	}

	@Override
	public void refreshView() {
		viewCanvasPanel.getViewProvider().setPosition(viewPositionAt0Resolution);
		viewCanvasPanel.getViewProvider().setResolutionLevel(resolutionLevel);
		viewCanvasPanel.updateCanvas();
		resolutionListeners.stream().forEach(l -> l.resolutionChanged(resolutionLevel));
	}

	@Override
	public void stopView() {
		viewCanvasPanel.getViewProvider().stop();
	}

	@Override
	public void setVisibileAnnotations(Set<Annotation> newVisibleAnnotations) {
		viewCanvasPanel.getViewProvider().setVisibleAnnotations(newVisibleAnnotations);
		viewCanvasPanel.updateCanvas();
	}

	@Override
	public Rectangle2D getCurrentViewBoundsAtZeroResolution() {
		double viewResolution = viewCanvasPanel.getViewProvider().getResolution();
		Point2D positionAtZeroResolution = viewCanvasPanel.getViewProvider().getPosition();
		Dimension2D dimensionAtViewResolution = viewCanvasPanel.getSize();
		Dimension2D dimensionAtZeroResolution = MagnitudeResolutionConverter.convertDimension2D(dimensionAtViewResolution,
				viewResolution, 0d);
		return new Rectangle2D.Double(positionAtZeroResolution.getX(), positionAtZeroResolution.getY(),
				dimensionAtZeroResolution.getWidth(), dimensionAtZeroResolution.getHeight());
	}

	@Override
	public double getCurrentResolution() {
		return viewCanvasPanel.getViewProvider().getResolution();
	}

	@Override
	public Set<Annotation> getVisibleAnnotations() {
		return viewCanvasPanel.getViewProvider().getVisibleAnnotations();
	}

	@Override
	public Set<Annotation> getActiveAnnotations() {
		return viewCanvasPanel.getViewProvider().getActiveAnnotations();
	}
}
