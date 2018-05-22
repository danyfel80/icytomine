package org.bioimageanalysis.icy.icytomine.core.image.annotation;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.core.model.Image;

import be.cytomine.client.CytomineException;
import icy.common.listener.ProgressListener;
import icy.painter.Anchor2D;
import icy.roi.ROI2D;
import icy.sequence.Sequence;
import plugins.kernel.roi.roi2d.ROI2DEllipse;
import plugins.kernel.roi.roi2d.ROI2DLine;
import plugins.kernel.roi.roi2d.ROI2DPoint;
import plugins.kernel.roi.roi2d.ROI2DPolyLine;
import plugins.kernel.roi.roi2d.ROI2DPolygon;
import plugins.kernel.roi.roi2d.ROI2DRectangle;
import plugins.kernel.roi.roi2d.ROI2DShape;

public class RoiAnnotationSender {

	private Image imageInformation;
	private Sequence sequence;
	private boolean selectedRois;

	private Set<ProgressListener> progressListeners;

	private Point2D sequenceLocation;
	private double sequenceScaleFactor;

	public RoiAnnotationSender(Image imageInformation, Sequence sequence, boolean selectedRois) {
		this.imageInformation = imageInformation;
		this.sequence = sequence;
		this.selectedRois = selectedRois;

		progressListeners = new HashSet<>();

		sequenceLocation = null;
		sequenceScaleFactor = Double.NaN;
	}

	public void addProgressListener(ProgressListener listener) {
		this.progressListeners.add(listener);
	}

	public void removeProgressListener(ProgressListener listener) {
		this.progressListeners.remove(listener);
	}

	public List<Annotation> send() throws InterruptedException, CytomineException {
		List<? extends ROI2D> rois = getROIs();
		int numRois = rois.size();
		int processedRois = 0;
		List<Annotation> createdAnnotations = new ArrayList<>(rois.size());
		for (ROI2D roi : rois) {
			checkThreadInterruption();
			Annotation createdAnnotation;
			try {
				createdAnnotation = sendROI(roi);
			} catch (UnsupportedOperationException e) {
				continue;
			} finally {
				processedRois++;
				notifyProgress(processedRois, numRois);
			}
			createdAnnotations.add(createdAnnotation);
		}
		return createdAnnotations;
	}

	private List<? extends ROI2D> getROIs() {
		Set<String> ids = getExistingAnnotationIds();
		if (selectedRois) {
			return sequence.getSelectedROI2Ds().stream().filter(roi -> !ids.contains(getAnnotationId(roi)))
					.collect(Collectors.toList());
		} else {
			return sequence.getROI2Ds().stream().filter(roi -> !ids.contains(getAnnotationId(roi)))
					.collect(Collectors.toList());
		}
	}

	private String getAnnotationId(ROI2D roi) {
		String idString = roi.getProperty("cytomineId");
		Long id;
		try {
			id = Long.parseUnsignedLong(idString);
		} catch (NumberFormatException e) {
			id = Long.MIN_VALUE;
		}
		return id.toString();
	}

	private Set<String> getExistingAnnotationIds() {
		try {
			return imageInformation.getAnnotations().stream().map(a -> a.getId().toString()).collect(Collectors.toSet());
		} catch (CytomineException e) {
			e.printStackTrace();
			return new HashSet<>();
		}
	}

	private void checkThreadInterruption() throws InterruptedException {
		if (Thread.interrupted())
			throw new InterruptedException();
	}

	private Annotation sendROI(ROI2D roi) throws CytomineException, UnsupportedOperationException {
		String description = getRoiWTKDesciption(roi);

		be.cytomine.client.models.Annotation internalAnnotation = imageInformation.getClient().addAnnotation(description,
				imageInformation.getId());
		Annotation annotation = new Annotation(internalAnnotation, imageInformation, imageInformation.getClient());
		return annotation;
	}

	private String getRoiWTKDesciption(ROI2D roi) throws UnsupportedOperationException {
		if (roi instanceof ROI2DShape) {
			ROI2DShape shapeRoi = (ROI2DShape) roi;
			shapeRoi = adjustRoiToFullImage(shapeRoi);
			return Annotation.convertToWKT(shapeRoi);
		} else {
			throw new UnsupportedOperationException("Unsupported roi type: " + roi.getClassName());
		}
	}

	private ROI2DShape adjustRoiToFullImage(ROI2DShape roi) {
		List<Point2D> adjustedPoints = getAdjustedPoints(roi);
		if (roi instanceof ROI2DPoint) {
			return new ROI2DPoint(adjustedPoints.get(0));
		} else if (roi instanceof ROI2DLine) {
			return new ROI2DLine(adjustedPoints.get(0), adjustedPoints.get(1));
		} else if (roi instanceof ROI2DPolyLine) {
			return new ROI2DPolyLine(adjustedPoints);
		} else if (roi instanceof ROI2DRectangle) {
			return new ROI2DRectangle(adjustedPoints.get(0), adjustedPoints.get(2));
		} else if (roi instanceof ROI2DEllipse) {
			return new ROI2DEllipse(adjustedPoints.get(0), adjustedPoints.get(2));
		} else if (roi instanceof ROI2DPolygon) {
			return new ROI2DPolygon(adjustedPoints);
		} else {
			throw new RuntimeException("unsupported shape roi: " + roi.getClassName());
		}
	}

	private List<Point2D> getAdjustedPoints(ROI2DShape roi) {
		Point2D sequenceLocation = getSequenceLocation();
		double scaleFactor = getSequenceScaleFactor();
		List<Anchor2D> controlPoints = roi.getControlPoints();

		List<Point2D> adjustedPoints = new ArrayList<>(controlPoints.size());
		for (Anchor2D anchor : controlPoints) {
			Point2D adjustedPoint = new Point2D.Double((sequenceLocation.getX() + anchor.getX() * scaleFactor),
					imageInformation.getSizeY() - (sequenceLocation.getY() + anchor.getY() * scaleFactor));
			adjustedPoints.add(adjustedPoint);
		}
		return adjustedPoints;
	}

	private double getSequenceScaleFactor() {
		if (Double.isNaN(sequenceScaleFactor)) {
			Optional<Double> res = Optional.ofNullable(imageInformation.getResolution());
			sequenceScaleFactor = res.orElse(1d) / sequence.getPixelSizeX();
		}
		return sequenceScaleFactor;

	}

	private Point2D getSequenceLocation() {
		if (sequenceLocation == null) {
			sequenceLocation = new Point2D.Double(sequence.getPositionX() / sequence.getPixelSizeX(),
					sequence.getPositionY() / sequence.getPixelSizeY());
		}
		return sequenceLocation;
	}

	private void notifyProgress(int processedRois, int numRois) {
		progressListeners.forEach(l -> l.notifyProgress(processedRois, numRois));
	}

}
