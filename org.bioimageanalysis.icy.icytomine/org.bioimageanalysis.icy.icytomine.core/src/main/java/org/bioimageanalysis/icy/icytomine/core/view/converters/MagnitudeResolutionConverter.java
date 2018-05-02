package org.bioimageanalysis.icy.icytomine.core.view.converters;

import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public abstract class MagnitudeResolutionConverter {

	public static double convertMagnitude(double magnitude, double sourceResolution, double targetResolution) {
		double resolutionDifference = sourceResolution - targetResolution;
		return magnitude * Math.pow(2d, resolutionDifference);
	}

	public static Point2D convertPoint2D(Point2D point, double sourceResolution, double targetResolution) {
		return new Point2D.Double(convertMagnitude(point.getX(), sourceResolution, targetResolution),
				convertMagnitude(point.getY(), sourceResolution, targetResolution));
	}

	public static Dimension2D convertDimension2D(Dimension2D dimension, double sourceResolution,
			double targetResolution) {
		return new icy.type.dimension.Dimension2D.Double(
				convertMagnitude(dimension.getWidth(), sourceResolution, targetResolution),
				convertMagnitude(dimension.getHeight(), sourceResolution, targetResolution));
	}

	public static Rectangle2D convertRectangle2D(Rectangle2D rectangle, double sourceResolution,
			double targetResolution) {
		Point2D convertedPosition = convertPoint2D(new Point2D.Double(rectangle.getX(), rectangle.getY()), sourceResolution,
				targetResolution);
		Dimension2D convertedDimension = convertDimension2D(
				new icy.type.dimension.Dimension2D.Double(rectangle.getWidth(), rectangle.getHeight()), sourceResolution,
				targetResolution);
		return new Rectangle2D.Double(convertedPosition.getX(), convertedPosition.getY(), convertedDimension.getWidth(),
				convertedDimension.getHeight());
	}
}
