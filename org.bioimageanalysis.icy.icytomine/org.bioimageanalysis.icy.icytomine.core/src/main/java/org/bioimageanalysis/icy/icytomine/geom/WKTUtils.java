package org.bioimageanalysis.icy.icytomine.geom;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import icy.painter.Anchor2D;
import plugins.kernel.roi.roi2d.ROI2DLine;
import plugins.kernel.roi.roi2d.ROI2DPoint;
import plugins.kernel.roi.roi2d.ROI2DPolyLine;
import plugins.kernel.roi.roi2d.ROI2DRectShape;
import plugins.kernel.roi.roi2d.ROI2DShape;

public abstract class WKTUtils {

	/**
	 * Creates a WKT formatted string from a given ROI2DShape.
	 * 
	 * @param shape
	 *          ROI with a list of point-coordinates.
	 * @return Shape description in WKT format.
	 */
	public static String createFromROI2DShape(ROI2DShape shape) {
		List<Anchor2D> points = new ArrayList<>(shape.getControlPoints());

		StringBuffer buffer = new StringBuffer();
		if (shape instanceof ROI2DPoint) {
			buffer.append("POINT(");
			buffer.append(String.format(Locale.US, "%f %f", points.get(0).getPositionX(), points.get(0).getPositionY()));
			buffer.append(")");
		} else if (shape instanceof ROI2DLine || shape instanceof ROI2DPolyLine) {
			buffer.append("LINESTRING (");
			buffer.append(points.stream().map(p -> String.format(Locale.US, "%f %f", p.getPositionX(), p.getPositionY()))
					.collect(Collectors.joining(",")));
			buffer.append(")");
		} else {
			buffer.append("POLYGON((");
			if (shape instanceof ROI2DRectShape) {
				Rectangle2D rect = ((ROI2DRectShape) shape).getBounds2D();
				buffer.append(String.format(Locale.US, "%f %f, ", rect.getMinX(), rect.getMinY()));
				buffer.append(String.format(Locale.US, "%f %f, ", rect.getMinX(), rect.getMaxY()));
				buffer.append(String.format(Locale.US, "%f %f, ", rect.getMaxX(), rect.getMaxY()));
				buffer.append(String.format(Locale.US, "%f %f, ", rect.getMaxX(), rect.getMinY()));
				buffer.append(String.format(Locale.US, "%f %f", rect.getMinX(), rect.getMinY()));
			} else {
				buffer.append(points.stream().map(p -> String.format(Locale.US, "%f %f", p.getPositionX(), p.getPositionY()))
						.collect(Collectors.joining(",")));
				buffer.append(String.format(Locale.US, ",%f %f", points.get(0).getPositionX(), points.get(0).getPositionY()));
			}
			buffer.append("))");
		}
		return buffer.toString();
	}

	public static String createFromRectangle2D(Rectangle2D rect) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("POLYGON((");
		buffer.append(String.format(Locale.US, "%f %f, ", rect.getMinX(), rect.getMinY()));
		buffer.append(String.format(Locale.US, "%f %f, ", rect.getMinX(), rect.getMaxY()));
		buffer.append(String.format(Locale.US, "%f %f, ", rect.getMaxX(), rect.getMaxY()));
		buffer.append(String.format(Locale.US, "%f %f, ", rect.getMaxX(), rect.getMinY()));
		buffer.append(String.format(Locale.US, "%f %f", rect.getMinX(), rect.getMinY()));
		buffer.append("))");
		return buffer.toString();
	}
	
}
