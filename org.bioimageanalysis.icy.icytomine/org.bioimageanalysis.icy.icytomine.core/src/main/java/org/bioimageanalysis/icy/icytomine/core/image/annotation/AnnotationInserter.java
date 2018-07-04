package org.bioimageanalysis.icy.icytomine.core.image.annotation;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;
import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.core.view.converters.MagnitudeResolutionConverter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

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

	private void addAnnotationToSequence(Annotation annotation, Sequence sequence) throws CytomineClientException, AnnotationInserterException {
		ROI2DShape roiInView = createRoiInView(annotation);
		roiInView.setName(Objects.toString(annotation.getId()));
		roiInView.setProperty("cytomineId", Objects.toString(annotation.getId()));
		roiInView.setColor(annotation.getColor());
		sequence.addROI(roiInView);
	}

	private ROI2DShape createRoiInView(Annotation annotation) throws CytomineClientException, AnnotationInserterException {
		Geometry geometry = annotation.getGeometryAtZeroResolution(false);
		if (geometry instanceof Point) {
			return createPoint((Point) geometry, annotation);
		} else if (geometry instanceof LineString) {
			return createLineString((LineString) geometry, annotation);
		} else if (geometry instanceof Polygon) {
			return createPolygon((Polygon) geometry, annotation);
		} else {
			throw new AnnotationInserterException(
					String.format("Unsupported annotation geometry (%s)", geometry.getGeometryType()));
		}
	}

	private ROI2DPoint createPoint(Point geometry, Annotation annotation) throws CytomineClientException {
		int maxY = annotation.getImage().getSizeY().get();
		int x = (int) MagnitudeResolutionConverter.convertMagnitude(geometry.getX() - viewBoundsAtZeroResolution.getMinX(),
				0d, sequenceResolution);
		int y = (int) MagnitudeResolutionConverter
				.convertMagnitude((maxY - geometry.getY()) - viewBoundsAtZeroResolution.getMinY(), 0d, sequenceResolution);
		return new ROI2DPoint(x, y);
	}

	private ROI2DPolyLine createLineString(LineString geometry, Annotation annotation) throws CytomineClientException {
		int maxY = annotation.getImage().getSizeY().get();

		CoordinateSequence coordinates = geometry.getCoordinateSequence();
		int size = coordinates.size();

		List<Point2D> points = IntStream.range(0, size).mapToObj(i -> {
			Coordinate coordinate = coordinates.getCoordinate(i);
			double x = MagnitudeResolutionConverter.convertMagnitude(coordinate.x - viewBoundsAtZeroResolution.getMinX(), 0,
					sequenceResolution);
			double y = MagnitudeResolutionConverter
					.convertMagnitude((maxY - coordinate.y) - viewBoundsAtZeroResolution.getMinY(), 0, sequenceResolution);
			return new Point2D.Double(x, y);
		}).collect(Collectors.toList());

		return new ROI2DPolyLine(points);
	}

	private ROI2DPolygon createPolygon(Polygon geometry, Annotation annotation) throws CytomineClientException {
		int maxY = annotation.getImage().getSizeY().get();

		Coordinate[] coordinates = geometry.getCoordinates();
		int size = coordinates.length;

		List<Point2D> points = IntStream.range(0, size - 1).mapToObj(i -> {
			Coordinate coordinate = coordinates[i];
			double x = MagnitudeResolutionConverter.convertMagnitude(coordinate.x - viewBoundsAtZeroResolution.getMinX(), 0,
					sequenceResolution);
			double y = MagnitudeResolutionConverter
					.convertMagnitude((maxY - coordinate.y) - viewBoundsAtZeroResolution.getMinY(), 0, sequenceResolution);
			return new Point2D.Double(x, y);
		}).collect(Collectors.toList());

		return new ROI2DPolygon(points);
	}
}
