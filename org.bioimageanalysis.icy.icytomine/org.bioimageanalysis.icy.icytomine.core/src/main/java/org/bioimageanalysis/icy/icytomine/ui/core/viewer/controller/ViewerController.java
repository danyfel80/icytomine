package org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;

import javax.swing.JFrame;

import org.bioimageanalysis.icy.icytomine.core.model.Term;
import org.bioimageanalysis.icy.icytomine.core.model.User;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.ViewerComponentContainer;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.AnnotationManagerPanel;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.ViewController;

import icy.gui.frame.IcyFrame;

public class ViewerController {

	private ViewerComponentContainer viewerContainer;
	private ViewController viewController;
	private IcyFrame annotationsFrame;

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
		viewerContainer.addAnnotationMenuListener(e -> {
			System.out.println("clicked");
			annotationsFrame = new IcyFrame("Annotations - Icytomine", true, true, false, false);
			annotationsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			annotationsFrame.setSize(new Dimension(400, 400));
			annotationsFrame.setMinimumSize(new Dimension(10, 10));
			annotationsFrame.setContentPane(new AnnotationManagerPanel(viewController.getImageInformation()));
			annotationsFrame.addToDesktopPane();
			annotationsFrame.center();
			annotationsFrame.setVisible(true);
		});
		viewerContainer.addUserFilterListener(
				(User user, boolean selected) -> viewController.setUserAnnotationVisibility(user, selected));
		viewerContainer.addTermFilterListener(
				(Term term, boolean selected) -> viewController.setTermAnnotationVisibility(term, selected));
	}

	public void stopViewer() {
		if (annotationsFrame != null)
			annotationsFrame.close();

		viewController.stopView();
	}
}
