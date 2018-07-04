package org.bioimageanalysis.icy.icytomine.geom;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import plugins.kernel.roi.roi2d.ROI2DPoint;
import plugins.kernel.roi.roi2d.ROI2DPolyLine;
import plugins.kernel.roi.roi2d.ROI2DPolygon;
import plugins.kernel.roi.roi2d.ROI2DShape;

public class ROIUtils {
	public static ROI2DShape createROIFromGeometry(Geometry geometry, double maxY, double scalingFactor) {
		Coordinate[] coords = geometry.getCoordinates();
		List<Point2D> scaledPoints = getScaledPoints(coords, maxY, scalingFactor);
		if (scaledPoints.size() == 0) {
			return null;
		} else if (scaledPoints.size() == 1)
			return new ROI2DPoint(scaledPoints.get(0));
		else if (!(scaledPoints.get(0).equals(scaledPoints.get(scaledPoints.size() - 1)))) {
			return new ROI2DPolyLine(scaledPoints);
		} else {
			return new ROI2DPolygon(scaledPoints.subList(0, scaledPoints.size() - 1));
		}
	}

	public static List<Point2D> getScaledPoints(Coordinate[] coords, double maxY, double scalingFactor) {
		return Arrays.stream(coords).map(c -> getScaledPoint(scalingFactor, maxY, c.x, c.y)).collect(Collectors.toList());
	}

	public static Point2D getScaledPoint(double scalingFactor, double maxY, double x, double y) {
		return new Point2D.Double(x * scalingFactor, maxY - y * scalingFactor);
	}
}
