package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.cytomine2Icy;

import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.DoubleStream;

import org.bioimageanalysis.icy.icytomine.core.image.annotation.AnnotationInserter;
import org.bioimageanalysis.icy.icytomine.core.image.annotation.AnnotationInserterException;
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
	private Rectangle2D viewBoundsAtZeroResolution;
	private double viewResolution;
	private Set<Annotation> activeAnnotations;
	private double viewMagnification;

	private ActionListener magnificationListener;
	private ActionListener startListener;
	private ActionListener endListener;

	private TiledImageImporter importer;

	private Set<ActionListener> closeListeners;

	private ProgressListener progressHandler;

	private TiledImageImportationListener tiledImageImportationHandler;

	private Double outputResolution;
	private Dimension2D outputDimension;

	public CytomineToIcyPanelController(CytomineToIcyPanel panel, ViewController viewController) {
		this.panel = panel;
		imageInformation = viewController.getImageInformation();
		viewBoundsAtZeroResolution = viewController.getCurrentViewBoundsAtZeroResolution();
		viewBoundsAtZeroResolution = viewBoundsAtZeroResolution.createIntersection(new Rectangle(imageInformation.getSize().get()));
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

	private ActionListener getMagnificationHandler() {
		if (magnificationListener == null) {
			magnificationListener = e -> {
				Optional<Double> outputMagnification = panel.getSelectedMagnification();
				if (outputMagnification.isPresent()) {
					outputResolution = MagnificationConverter.convertToResolution(getBaseMagnification(), outputMagnification.get());
					outputDimension = MagnitudeResolutionConverter.convertDimension2D(
							new icy.type.dimension.Dimension2D.Double(viewBoundsAtZeroResolution.getWidth(), viewBoundsAtZeroResolution.getHeight()), 0,
							outputResolution);
				} else {
					outputResolution = null;
					outputDimension = new icy.type.dimension.Dimension2D.Double();
				}
				panel.setOutputImageSize(outputDimension);

			};
		}
		return magnificationListener;
	}

	private ActionListener getStartHandler() {
		if (startListener == null) {
			startListener = e -> {
				if (outputResolution != null && outputDimension.getWidth() > 0 && outputDimension.getHeight() > 0) {
					panel.setStartButtonEnabled(false);
					panel.setMagnificationEnabled(false);
					panel.setProgress(0d);
	
					importer = new TiledImageImporter(imageInformation);
					importer.addImportationProgressListener(getProgressHandler());
					importer.addTiledImageImportationListener(getTiledImageImportationHandler());
					importer.requestImage(outputResolution, viewBoundsAtZeroResolution);
				} else {
					MessageDialog.showDialog("Invalid magnification", MessageDialog.ERROR_MESSAGE);
				}
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
			Dimension2D pixelSizeAtZeroResolution = getPixelSizeAtZeroResolution();
			Dimension2D pixelSizeAtTargetResolution = getPixelSizeAtViewResolution();
			image.setName(imageInformation.getName().orElse("CytomineImage"));
			image.setPixelSizeX(pixelSizeAtTargetResolution.getWidth());
			image.setPixelSizeY(pixelSizeAtTargetResolution.getHeight());
			image.setPositionX(viewBoundsAtZeroResolution.getX() * pixelSizeAtZeroResolution.getWidth());
			image.setPositionY(viewBoundsAtZeroResolution.getY() * pixelSizeAtZeroResolution.getHeight());

			panel.setProgress(.99);

			AnnotationInserter annotationsInserter = new AnnotationInserter(image);
			annotationsInserter.insertAnnotations(viewBoundsAtZeroResolution, outputResolution, activeAnnotations);
			Icy.getMainInterface().addSequence(image);
		} catch (CancellationException e) {
		} catch (AnnotationInserterException | InterruptedException | ExecutionException e) {
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
		panel.setMagnificationEnabled(true);
	}

	private Dimension2D getPixelSizeAtZeroResolution() {
		double pixelLength = imageInformation.getResolution().orElse(1d);
		return new icy.type.dimension.Dimension2D.Double(pixelLength, pixelLength);
	}

	private Dimension2D getPixelSizeAtViewResolution() {
		Dimension2D pixelSize = getPixelSizeAtZeroResolution();
		return MagnitudeResolutionConverter.convertDimension2D(pixelSize, outputResolution, 0d);
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
			if (panel != null) {
				panel.setStartButtonEnabled(true);
				panel.setMagnificationEnabled(true);
			}
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
