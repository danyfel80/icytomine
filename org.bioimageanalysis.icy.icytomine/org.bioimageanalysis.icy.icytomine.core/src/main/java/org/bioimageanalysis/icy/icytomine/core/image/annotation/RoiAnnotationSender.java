package org.bioimageanalysis.icy.icytomine.core.image.annotation;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;
import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.model.Term;
import org.bioimageanalysis.icy.icytomine.geom.WKTUtils;

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

	private Map<String, Term> availableTerms;

	private Point2D sequenceLocationAtZeroResolution;
	private double sequencePixelScaleFactor;

	public RoiAnnotationSender(Image imageInformation, Sequence sequence, boolean selectedRois) {
		this.imageInformation = imageInformation;
		this.sequence = sequence;
		this.selectedRois = selectedRois;

		sequenceLocationAtZeroResolution = null;
		sequencePixelScaleFactor = Double.NaN;

		computeAvailableTerms();

		progressListeners = new HashSet<>();
	}

	private void computeAvailableTerms() {
		try {
			Set<Term> terms = imageInformation.getProject().getOntology().getTerms(false);
			availableTerms = terms.stream()
					.collect(Collectors.toMap(t -> t.getName().orElse("Not specified").toLowerCase(), t -> t));
		} catch (CytomineClientException e) {
			availableTerms = new HashMap<>(0);
		}
	}

	public void addProgressListener(ProgressListener listener) {
		this.progressListeners.add(listener);
	}

	public void removeProgressListener(ProgressListener listener) {
		this.progressListeners.remove(listener);
	}

	public List<Annotation> send() throws InterruptedException, CytomineClientException {
		List<? extends ROI2D> rois = getROIs();
		int numRois = rois.size();
		int processedRois = 0;
		List<Annotation> createdAnnotations = new ArrayList<>(rois.size());
		for (ROI2D roi: rois) {
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
			return imageInformation.getAnnotations(false).stream().map(a -> a.getId().toString()).collect(Collectors.toSet());
		} catch (CytomineClientException e) {
			e.printStackTrace();
			return new HashSet<>();
		}
	}

	private void checkThreadInterruption() throws InterruptedException {
		if (Thread.interrupted())
			throw new InterruptedException();
	}

	private Annotation sendROI(ROI2D roi) throws CytomineClientException, UnsupportedOperationException {
		String description = getRoiWTKDesciption(roi);
		Set<Term> terms = getRoiTermsBasedOnName(roi);

		Annotation annotation = createAnnotation(description, terms);

		return annotation;
	}

	private Annotation createAnnotation(String description, Set<Term> terms) throws CytomineClientException {
		Annotation annotation = imageInformation.getClient().addAnnotationWithTerms(imageInformation.getId(), description,
				terms.stream().map(term -> term.getId()).collect(Collectors.toList()));
		return annotation;
	}

	private String getRoiWTKDesciption(ROI2D roi) throws UnsupportedOperationException {
		if (roi instanceof ROI2DShape) {
			ROI2DShape shapeRoi = (ROI2DShape) roi;
			shapeRoi = adjustRoiToFullImage(shapeRoi);
			return WKTUtils.createFromROI2DShape(shapeRoi);
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
		computeSequenceLocationAtZeroResolution();
		computeSequencePixelScaleFactor();
		List<Anchor2D> controlPoints = roi.getControlPoints();

		List<Point2D> adjustedPoints = new ArrayList<>(controlPoints.size());
		for (Anchor2D anchor: controlPoints) {
			Point2D adjustedPoint = new Point2D.Double(
					(sequenceLocationAtZeroResolution.getX() + anchor.getX() * sequencePixelScaleFactor),
					imageInformation.getSizeY().orElse(1)
							- (sequenceLocationAtZeroResolution.getY() + anchor.getY() * sequencePixelScaleFactor));
			adjustedPoints.add(adjustedPoint);
		}
		return adjustedPoints;
	}

	private void computeSequenceLocationAtZeroResolution() {
		if (sequenceLocationAtZeroResolution == null) {
			sequenceLocationAtZeroResolution = new Point2D.Double(
					sequence.getPositionX() / imageInformation.getResolution().orElse(1d),
					sequence.getPositionY() / imageInformation.getResolution().orElse(1d));
		}
	}

	private void computeSequencePixelScaleFactor() {
		if (Double.isNaN(sequencePixelScaleFactor)) {
			Optional<Double> res = imageInformation.getResolution();
			sequencePixelScaleFactor = sequence.getPixelSizeX() / res.orElse(1d);
		}
	}

	private Set<Term> getRoiTermsBasedOnName(ROI2D roi) {
		Set<Term> terms = new HashSet<>();
		String termString = roi.getName();
		String[] termStrings = termString.split(",");
		for (String termName: termStrings) {
			termName = termName.trim().toLowerCase();
			if (isValidTerm(termName)) {
				terms.add(availableTerms.get(termName));
			}
		}
		return terms;
	}

	private boolean isValidTerm(String termName) {
		return availableTerms.containsKey(termName);
	}

	private void notifyProgress(int processedRois, int numRois) {
		progressListeners.forEach(l -> l.notifyProgress(processedRois, numRois));
	}

}
