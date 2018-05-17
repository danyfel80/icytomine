package org.bioimageanalysis.icy.icytomine.core.image.annotation;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.core.view.converters.MagnitudeResolutionConverter;

import com.vividsolutions.jts.io.ParseException;

import be.cytomine.client.CytomineException;
import icy.sequence.Sequence;
import plugins.kernel.roi.roi2d.ROI2DPoint;
import plugins.kernel.roi.roi2d.ROI2DPolyLine;
import plugins.kernel.roi.roi2d.ROI2DPolygon;
import plugins.kernel.roi.roi2d.ROI2DShape;

public class AnnotationInserter {

	Sequence sequence;
	Rectangle2D viewBoundsAtZeroResolution;
	double sequenceResolution;
	Set<Annotation> activeAnnotations;

	public AnnotationInserter(Sequence sequence) {
		this.sequence = sequence;
	}

	public void insertAnnotations(Rectangle2D viewBoundsAtZeroResolution, double sequenceResolution,
			Set<Annotation> activeAnnotations) {

		this.viewBoundsAtZeroResolution = viewBoundsAtZeroResolution;
		this.sequenceResolution = sequenceResolution;
		this.activeAnnotations = activeAnnotations;

		addAnnotationsToSequence();
	}

	private void addAnnotationsToSequence() {
		activeAnnotations.forEach(a -> addAnnotationToSequence(a, sequence));
	}

	private void addAnnotationToSequence(Annotation annotation, Sequence sequence) {
		try {
			ROI2DShape roi = (ROI2DShape) annotation.getROI(0);
			ROI2DShape roiInView = createRoiInView(roi);
			roiInView.setName(Objects.toString(annotation.getId()));
			roiInView.setColor(roi.getColor());
			sequence.addROI(roiInView);
		} catch (ParseException | CytomineException e) {
			e.printStackTrace();
		}
	}

	private ROI2DShape createRoiInView(ROI2DShape roi) {
		List<Point2D> transformedPoints = roi.getControlPoints().stream().map(anchor -> anchor.getPositionInternal())
				.map(p -> new Point2D.Double(
						MagnitudeResolutionConverter.convertMagnitude(p.getX() - viewBoundsAtZeroResolution.getMinX(), 0,
								sequenceResolution),
						MagnitudeResolutionConverter.convertMagnitude(p.getY() - viewBoundsAtZeroResolution.getMinY(), 0,
								sequenceResolution)))
				.collect(Collectors.toList());

		ROI2DShape newRoi = null;
		if (roi instanceof ROI2DPoint) {
			newRoi = new ROI2DPoint(transformedPoints.get(0));
		} else if (roi instanceof ROI2DPolyLine) {
			newRoi = new ROI2DPolyLine(transformedPoints);
		} else if (roi instanceof ROI2DPolygon) {
			newRoi = new ROI2DPolygon(transformedPoints);
		}
		return newRoi;
	}
}
