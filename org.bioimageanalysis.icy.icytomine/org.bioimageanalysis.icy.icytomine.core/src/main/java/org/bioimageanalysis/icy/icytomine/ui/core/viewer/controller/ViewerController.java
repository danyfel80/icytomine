package org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.ViewerComponentContainer;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.AnnotationManagerPanel;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.cytomine2Icy.CytomineToIcyPanel;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.icy2Cytomine.sequence.IcySequenceToCytominePanel;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.ViewController;

import icy.gui.frame.IcyFrame;

public class ViewerController {

	private ViewerComponentContainer viewerContainer;
	private ViewController viewController;
	private IcyFrame annotationsFrame;
	private IcyFrame cytomineToIcyFrame;
	private IcyFrame icySequenceToCytomineFrame;

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
		viewerContainer.addAnnotationMenuListener(getAnnotationMenuHandler());
		viewerContainer.addCytomineToIcyMenuListener(getCytomineToIcyMenuHandler());
		viewerContainer.addIcySequenceToCytomineMenuListener(getIcySequenceToCytomineMenuHandler());
		// TODO addIcyToCytomineFileMenuListener
		// TODO addIcyToCytomineFolderMenuListener
	}

	private ActionListener getAnnotationMenuHandler() {
		return e -> {
			System.out.println("Opening annotations menu...");
			if (annotationsFrame != null) {
				annotationsFrame.close();
			}

			AnnotationManagerPanel annotationsPanel = new AnnotationManagerPanel(viewController.getImageInformation());
			annotationsPanel.addAnnotationsVisibilityListener(
					(Set<Annotation> newVisibleAnnotations) -> viewController.setVisibileAnnotations(newVisibleAnnotations));
			annotationsFrame = createIcyDialog("Annotations - Icytomine", annotationsPanel, true);
			annotationsFrame.setSize(new Dimension(400, 400));
			annotationsFrame.setVisible(true);
		};
	}

	private static IcyFrame createIcyDialog(String title, JPanel contentPane, boolean resizable) {
		IcyFrame frame = new IcyFrame(title, resizable, true, false, false);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setMinimumSize(new Dimension(10, 10));
		frame.setContentPane(contentPane);
		frame.setSize(contentPane.getPreferredSize());
		frame.addToDesktopPane();
		frame.center();
		return frame;
	}

	private ActionListener getCytomineToIcyMenuHandler() {
		return e -> {
			System.out.println("Opening cytomine -> icy dialog...");
			if (cytomineToIcyFrame != null) {
				cytomineToIcyFrame.close();
			}

			CytomineToIcyPanel contentPane = new CytomineToIcyPanel(viewController);
			cytomineToIcyFrame = createIcyDialog("Download view from Cytomine - Icytomine", contentPane, false);
			contentPane.addCloseListener(a -> cytomineToIcyFrame.close());
			cytomineToIcyFrame.setVisible(true);
		};
	}

	private ActionListener getIcySequenceToCytomineMenuHandler() {
		return e -> {
			System.out.println("Opening icy sequence -> cytomine dialog...");
			if (icySequenceToCytomineFrame != null) {
				icySequenceToCytomineFrame.close();
			}

			IcySequenceToCytominePanel contentPane = new IcySequenceToCytominePanel(viewController);
			icySequenceToCytomineFrame = createIcyDialog("Send Sequence ROIs to Cytomine - Icytomine", contentPane, false);
			contentPane.addCloseListener(a -> icySequenceToCytomineFrame.close());
			icySequenceToCytomineFrame.setVisible(true);
		};
	}

	public void stopViewer() {
		closeFrame(annotationsFrame);
		closeFrame(cytomineToIcyFrame);
		closeFrame(icySequenceToCytomineFrame);

		viewController.stopView();
	}

	private void closeFrame(IcyFrame frame) {
		if (frame != null)
			frame.close();
	}
}
