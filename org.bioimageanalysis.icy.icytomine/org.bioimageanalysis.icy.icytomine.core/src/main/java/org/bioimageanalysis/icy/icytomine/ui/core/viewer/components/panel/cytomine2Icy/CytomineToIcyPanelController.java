package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.cytomine2Icy;

import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.DoubleStream;

import javax.swing.JComboBox;

import org.bioimageanalysis.icy.icytomine.core.image.annotation.AnnotationInserter;
import org.bioimageanalysis.icy.icytomine.core.image.importer.TiledImageImporter;
import org.bioimageanalysis.icy.icytomine.core.image.importer.TiledImageImporter.TiledImageImportationListener;
import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.view.converters.MagnificationConverter;
import org.bioimageanalysis.icy.icytomine.core.view.converters.MagnitudeResolutionConverter;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.ViewController;

import icy.common.listener.ProgressListener;
import icy.gui.dialog.MessageDialog;
import icy.main.Icy;
import icy.sequence.Sequence;

public class CytomineToIcyPanelController {
	private static double[] fixedObjectives = new double[] { 1.25, 2.5, 5, 10, 20, 40 };

	private CytomineToIcyPanel panel;
	private Image imageInformation;
	private Rectangle2D viewBounds;
	private double viewResolution;
	private Set<Annotation> activeAnnotations;
	private double viewMagnification;

	private ActionListener magnificationListener;
	private ActionListener startListener;
	private ActionListener endListener;

	private double outputMagnification;
	private double outputResolution;
	private Dimension2D outputDimension;

	private TiledImageImporter importer;

	private Set<ActionListener> closeListeners;

	private ProgressListener progressHandler;

	private TiledImageImportationListener tiledImageImportationHandler;

	public CytomineToIcyPanelController(CytomineToIcyPanel panel, ViewController viewController) {
		this.panel = panel;
		imageInformation = viewController.getImageInformation();
		viewBounds = viewController.getCurrentViewBoundsAtZeroResolution();
		viewBounds = viewBounds.createIntersection(new Rectangle(imageInformation.getSize().get()));
		viewResolution = viewController.getCurrentResolution();
		activeAnnotations = viewController.getActiveAnnotations();
		viewMagnification = getMagnificationOf(viewResolution);

		closeListeners = new HashSet<>();

		setAvailableMagnifications();
		setListeners();
		setDefaultOutputMagnification();
	}

	private double getMagnificationOf(double resolution) {
		return MagnificationConverter.convertFromResolution(getBaseMagnification(), resolution);
	}

	private int getBaseMagnification() {
		Integer magnification = imageInformation.getMagnification().orElse(1);
		if (magnification == null)
			magnification = 1;
		return magnification;
	}

	private void setAvailableMagnifications() {
		panel.setAvailableMagnifications(
				DoubleStream.concat(DoubleStream.of(viewMagnification), DoubleStream.of(fixedObjectives)).toArray());
	}

	private void setListeners() {
		panel.addMagnificationListener(getMagnificationHandler());
		panel.addStartButtonActionListener(getStartHandler());
		panel.addCancelButtonActionListener(getCancelHandler());

	}

	@SuppressWarnings("unchecked")
	private ActionListener getMagnificationHandler() {
		if (magnificationListener == null) {
			magnificationListener = e -> {
				outputMagnification = (Double) ((JComboBox<Double>) e.getSource()).getSelectedItem();
				outputResolution = MagnificationConverter.convertToResolution(getBaseMagnification(), outputMagnification);
				outputDimension = MagnitudeResolutionConverter.convertDimension2D(
						new icy.type.dimension.Dimension2D.Double(viewBounds.getWidth(), viewBounds.getHeight()), 0,
						outputResolution);
				panel.setOutputImageSize(outputDimension);
			};
		}
		return magnificationListener;
	}

	private ActionListener getStartHandler() {
		if (startListener == null) {
			startListener = e -> {
				panel.setStartButtonEnabled(false);
				panel.setProgress(0d);

				importer = new TiledImageImporter(imageInformation);
				importer.addImportationProgressListener(getProgressHandler());
				importer.addTiledImageImportationListener(getTiledImageImportationHandler());
				importer.requestImage(outputResolution, viewBounds);
			};
		}
		return startListener;
	}

	private ProgressListener getProgressHandler() {
		if (progressHandler == null) {
			progressHandler = (double position, double length) -> {
				panel.setProgress(position / length);
				return true;
			};
		}
		return progressHandler;
	}

	private TiledImageImportationListener getTiledImageImportationHandler() {
		if (tiledImageImportationHandler == null) {
			tiledImageImportationHandler = (Future<BufferedImage> result) -> imageImported(result);
		}
		return tiledImageImportationHandler;
	}

	private void imageImported(Future<BufferedImage> result) {
		try {
			Sequence image = new Sequence(result.get());
			Dimension2D pixelSize = getPixelSizeAtViewResolution();
			image.setName(imageInformation.getName().orElse("CytomineImage"));
			image.setPixelSizeX(pixelSize.getWidth());
			image.setPixelSizeY(pixelSize.getHeight());
			image.setPositionX(viewBounds.getX() * pixelSize.getWidth());
			image.setPositionY(viewBounds.getY() * pixelSize.getHeight());

			panel.setProgress(.99);

			AnnotationInserter annotationsInserter = new AnnotationInserter(image);
			annotationsInserter.insertAnnotations(viewBounds, outputResolution, activeAnnotations);
			Icy.getMainInterface().addSequence(image);
		} catch (CancellationException e) {
		} catch (InterruptedException | ExecutionException e) {
			MessageDialog.showDialog("Transfer error", e.getMessage(), MessageDialog.ERROR_MESSAGE);
			e.printStackTrace();
		} finally {
			if (importer != null) {
				importer.removeImportationProgressListener(getProgressHandler());
				importer.removeTiledImageImportationListener(getTiledImageImportationHandler());
			}
			importer = null;
		}
		panel.setProgressIdle();
		panel.setStartButtonEnabled(true);
	}

	private Dimension2D getPixelSizeAtViewResolution() {
		double pixelLength = imageInformation.getResolution().orElse(1d);
		Dimension2D pixelSize = new icy.type.dimension.Dimension2D.Double(pixelLength, pixelLength);
		return MagnitudeResolutionConverter.convertDimension2D(pixelSize, 0d, outputResolution);
	}

	private ActionListener getCancelHandler() {
		if (endListener == null) {
			endListener = e -> {
				cancelTransfer();
				requestDialogClose();
			};
		}
		return endListener;
	}

	private void requestDialogClose() {
		closeListeners.forEach(l -> l.actionPerformed(null));
	}

	public void cancelTransfer() {
		TiledImageImporter imp = importer;
		if (imp != null)
			synchronized (imp) {
				if (imp != null) {
					try {
						imp.cancel();
						imp.removeImportationProgressListener(getProgressHandler());
						imp.removeTiledImageImportationListener(getTiledImageImportationHandler());
					} catch (InterruptedException e) {
					} catch (RuntimeException e) {
						MessageDialog.showDialog("Transfer error", e.getMessage(), MessageDialog.ERROR_MESSAGE);
					}
				}
			}
		importer = null;
		synchronized (panel) {
			if (panel != null)
				panel.setStartButtonEnabled(true);
		}

	}

	private void setDefaultOutputMagnification() {
		panel.setDefaultMagnification();
	}

	public void addCloseListener(ActionListener listener) {
		closeListeners.add(listener);
	}

	public void close() {
		cancelTransfer();
	}
}
