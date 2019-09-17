package org.bioimageanalysis.icy.icytomine.core.image.annotation;

import java.awt.geom.Path2D;
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
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import icy.sequence.Sequence;
import plugins.kernel.roi.roi2d.ROI2DPath;
import plugins.kernel.roi.roi2d.ROI2DPoint;
import plugins.kernel.roi.roi2d.ROI2DPolyLine;
import plugins.kernel.roi.roi2d.ROI2DPolygon;
import plugins.kernel.roi.roi2d.ROI2DShape;

public class AnnotationInserter {

	Sequence sequence;
	boolean retrieveProperties;
	Rectangle2D viewBoundsAtZeroResolution;
	double sequenceResolution;
	Set<Annotation> activeAnnotations;

	public AnnotationInserter(Sequence sequence) {
		this.sequence = sequence;
	}

	public void insertAnnotations(Rectangle2D viewBoundsAtZeroResolution, double sequenceResolution,
			Set<Annotation> activeAnnotations, boolean retrieveProperties) throws AnnotationInserterException {

		this.viewBoundsAtZeroResolution = viewBoundsAtZeroResolution;
		this.sequenceResolution = sequenceResolution;
		this.activeAnnotations = activeAnnotations;
		this.retrieveProperties = retrieveProperties;

		addAnnotationsToSequence();
	}

	private void addAnnotationsToSequence() throws AnnotationInserterException {
		activeAnnotations.forEach(a -> addAnnotationToSequence(a, sequence));
	}

	private void addAnnotationToSequence(Annotation annotation, Sequence sequence)
			throws CytomineClientException, AnnotationInserterException {
		ROI2DShape roiInView = createRoiInView(annotation);
		roiInView.setName(Objects.toString(annotation.getId()));
		roiInView.setProperty("cytomineId", Objects.toString(annotation.getId()));
		if (retrieveProperties)
			annotation.getAnnotationProperties(false).forEach(p -> {
				if (p.getKey().orElse("").equals("ANNOTATION_GROUP_ID")) {
					roiInView.setName(p.getValue().orElse(Objects.toString(annotation.getId())));
				}
				roiInView.setProperty(p.getKey().orElse("Unknown"), p.getValue().orElse("Unknown"));
			});

		roiInView.setColor(annotation.getColor());
		sequence.addROI(roiInView);
	}

	private ROI2DShape createRoiInView(Annotation annotation)
			throws CytomineClientException, AnnotationInserterException {
		Geometry geometry = annotation.getGeometryAtZeroResolution(false);
		if (geometry instanceof Point) {
			return createPoint((Point) geometry, annotation);
		} else if (geometry instanceof LineString) {
			return createLineString((LineString) geometry, annotation);
		} else if (geometry instanceof Polygon) {
			return createPolygon((Polygon) geometry, annotation);
		} else if (geometry instanceof MultiPolygon) {
			return createMultiPolygon((MultiPolygon) geometry, annotation);
		} else {
			throw new AnnotationInserterException(
					String.format("Unsupported annotation geometry (%s)", geometry.getGeometryType()));
		}
	}

	private ROI2DPoint createPoint(Point geometry, Annotation annotation) throws CytomineClientException {
		int maxY = annotation.getImage().getSizeY().get();
		double x = MagnitudeResolutionConverter
				.convertMagnitude(geometry.getCoordinate().x - viewBoundsAtZeroResolution.getMinX(), 0d, sequenceResolution);
		double y = MagnitudeResolutionConverter.convertMagnitude(
				(maxY - geometry.getCoordinate().y) - viewBoundsAtZeroResolution.getMinY(), 0d, sequenceResolution);
		return new ROI2DPoint(x, y);
	}

	private ROI2DPolyLine createLineString(LineString geometry, Annotation annotation) throws CytomineClientException {
		int maxY = annotation.getImage().getSizeY().get();

		CoordinateSequence coordinates = geometry.getCoordinateSequence();
		int size = coordinates.size();

		List<Point2D> points = IntStream.range(0, size).mapToObj(i -> {
			Coordinate coordinate = coordinates.getCoordinate(i);
			double x = MagnitudeResolutionConverter.convertMagnitude(coordinate.x - viewBoundsAtZeroResolution.getMinX(), 0d,
					sequenceResolution);
			double y = MagnitudeResolutionConverter
					.convertMagnitude((maxY - coordinate.y) - viewBoundsAtZeroResolution.getMinY(), 0d, sequenceResolution);
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
			double x = MagnitudeResolutionConverter.convertMagnitude(coordinate.x - viewBoundsAtZeroResolution.getMinX(), 0d,
					sequenceResolution);
			double y = MagnitudeResolutionConverter
					.convertMagnitude((maxY - coordinate.y) - viewBoundsAtZeroResolution.getMinY(), 0d, sequenceResolution);
			return new Point2D.Double(x, y);
		}).collect(Collectors.toList());

		return new ROI2DPolygon(points);
	}

	private ROI2DPath createMultiPolygon(MultiPolygon geometry, Annotation annotation) {
		int numPolygons = geometry.getNumGeometries();
		Path2D.Double path = new Path2D.Double();
		for (int i = 0; i < numPolygons; i++) {
			Geometry subGeometry = geometry.getGeometryN(i);
			ROI2DShape internalROI;
			if (subGeometry instanceof Point) {
				internalROI = createPoint((Point) subGeometry, annotation);
			} else if (subGeometry instanceof LineString) {
				internalROI = createLineString((LineString) subGeometry, annotation);
			} else if (subGeometry instanceof Polygon) {
				internalROI = createPolygon((Polygon) subGeometry, annotation);
			} else if (subGeometry instanceof Polygon) {
				internalROI = createMultiPolygon((MultiPolygon) subGeometry, annotation);
			} else {
				throw new AnnotationInserterException(
						String.format("Unsupported annotation geometry (%s)", subGeometry.getGeometryType()));
			}
			path.append(internalROI.getShape(), false);
		}

		return new ROI2DPath(path);
	}
}
