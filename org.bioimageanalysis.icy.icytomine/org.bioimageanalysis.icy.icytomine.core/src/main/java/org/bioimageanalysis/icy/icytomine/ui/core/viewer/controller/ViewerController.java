package org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller;

import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;

import org.bioimageanalysis.icy.icytomine.ui.core.viewer.ViewerComponentContainer;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.view.ViewController;

public class ViewerController {

	private ViewerComponentContainer viewerContainer;
	private ViewController viewController;

	public ViewerController(ViewerComponentContainer viewerContainer) {
		this.viewerContainer = viewerContainer;
	}

	public void startViewer() {
		viewController = viewerContainer.getViewCanvasPanel().getViewController();
		setViewControllerListeners();
		setViewerContainerListeners();
		viewController.adjustImageZoomToView();
	}

	private void setViewControllerListeners() {
		viewController.addCursorPositionListener(
				(Point2D newPosition) -> viewerContainer.setCursorPosition(newPosition, getPositionInMicrons(newPosition)));
		viewController
				.addResolutionListener((double newResolution) -> viewerContainer.setZoomLevel(getZoomLevel(newResolution)));
	}

	private Point2D getPositionInMicrons(Point2D position) {
		Double pixelSize = viewController.getImageInformation().getResolution();
		if (pixelSize == null)
			pixelSize = 0d;
		return new Point2D.Double(position.getX() * pixelSize, position.getY() * pixelSize);
	}

	private double getZoomLevel(double resolutionLevel) {
		Integer intMagnification = viewController.getImageInformation().getMagnification();
		if (intMagnification == null)
			intMagnification = 1;
		double magnification = intMagnification.doubleValue();
		magnification /= Math.pow(2d, resolutionLevel);
		return magnification;
	}

	private double getResolutionLevel(double zoomLevel) {
		Integer intMagnification = viewController.getImageInformation().getMagnification();
		if (intMagnification == null)
			intMagnification = 1;
		double magnification = intMagnification.doubleValue();
		return Math.log(magnification / zoomLevel) / Math.log(2);
	}

	private void setViewerContainerListeners() {
		viewerContainer.addZoomInListener((ActionEvent e) -> {
			viewController.zoomIn();
		});
		viewerContainer.addZoomOutListener((ActionEvent e) -> {
			viewController.zoomOut();
		});
		viewerContainer
				.addZoomLevelSelectedListener(zoomLevel -> viewController.setResolution(getResolutionLevel(zoomLevel)));
	}

	public void stopViewer() {
		viewController.stopView();
	}
}
