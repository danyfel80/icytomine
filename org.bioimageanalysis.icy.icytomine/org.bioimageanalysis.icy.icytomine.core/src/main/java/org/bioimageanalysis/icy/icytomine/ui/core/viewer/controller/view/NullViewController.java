package org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

import org.bioimageanalysis.icy.icytomine.core.model.DummyImage;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.model.Term;
import org.bioimageanalysis.icy.icytomine.core.model.User;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.view.ViewCanvasPanel;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.CachedViewController.PositionListener;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.CachedViewController.ResolutionListener;

public class NullViewController implements ViewController {

	List<ResolutionListener> resolutionListeners;
	List<PositionListener> cursorPositionListeners;
	private ViewCanvasPanel viewCanvasPanel;

	//private Point2D viewPosition;
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
		return new DummyImage();
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
	public void setUserAnnotationVisibility(User user, boolean selected) {
		// Not used
		
	}

	@Override
	public void setTermAnnotationVisibility(Term term, boolean selected) {
		// Not used
		
	}

}