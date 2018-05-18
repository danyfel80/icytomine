package org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.view.ViewCanvasPanel;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.CachedViewController.PositionListener;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.CachedViewController.ResolutionListener;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.provider.ViewProvider;

public class NullViewController implements ViewController {

	List<ResolutionListener> resolutionListeners;
	List<PositionListener> cursorPositionListeners;
	private ViewCanvasPanel viewCanvasPanel;

	// private Point2D viewPosition;
	private double resolutionLevel;

	private Point2D lastDragStartPosition;

	public NullViewController(ViewCanvasPanel viewCanvasPanel) {
		this.viewCanvasPanel = viewCanvasPanel;
		resolutionListeners = new LinkedList<>();
		cursorPositionListeners = new LinkedList<>();
		setCanvasMouseEventListeners();
		resolutionLevel = 0d;
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
			public void mouseMoved(MouseEvent e) {
				notifyMouseMovedTo(e.getPoint());
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				mouseDragTo(e.getPoint());
			}
		});
	}

	private void startMouseDragAt(Point point) {
		System.out.println("Drag started at " + point);
		lastDragStartPosition = point;
	}

	protected void mouseDragTo(Point position) {
		moveViewBy(position.getX() - lastDragStartPosition.getX(), position.getY() - lastDragStartPosition.getY());
	}

	private void moveViewBy(double deltaX, double deltaY) {
		System.out.format("moving by (%f, %f)\n", deltaX, deltaY);
	}

	protected void notifyMouseMovedTo(Point2D point) {
		cursorPositionListeners.forEach(l -> l.positionChanged(point));
	}

	@Override
	public Image getImageInformation() {
		return Image.getNoImage(null);
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
		System.out.println("Zoom in");
		// TODO Auto-generated method stub

	}

	@Override
	public void zoomOut() {
		System.out.println("Zoom out");
		// TODO Auto-generated method stub

	}

	@Override
	public void adjustImageZoomToView() {
		refreshView();
	}

	@Override
	public void refreshView() {
		resolutionListeners.stream().forEach(l -> l.resolutionChanged(resolutionLevel));
	}

	@Override
	public void stopView() {
		viewCanvasPanel.getViewProvider().stop();
	}

	@Override
	public void setResolution(double resolutionLevel) {
		System.out.println("resolution set to " + resolutionLevel);
	}

	@Override
	public void setVisibileAnnotations(Set<Annotation> newVisibleAnnotations) {
		// Not used

	}

	@Override
	public Rectangle2D getCurrentViewBoundsAtZeroResolution() {
		// Not used
		return null;
	}

	@Override
	public double getCurrentResolution() {
		// Not used
		return 0;
	}

	@Override
	public Set<Annotation> getVisibleAnnotations() {
		// Not used
		return null;
	}

	@Override
	public Set<Annotation> getActiveAnnotations() {
		// Not used
		return null;
	}

	@Override
	public ViewProvider getViewProvider() {
		return viewCanvasPanel.getViewProvider();
	}

}
