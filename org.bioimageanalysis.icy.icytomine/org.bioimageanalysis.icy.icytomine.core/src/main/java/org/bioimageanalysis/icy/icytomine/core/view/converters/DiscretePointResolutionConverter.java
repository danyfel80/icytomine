package org.bioimageanalysis.icy.icytomine.core.view.converters;

import java.awt.Point;

public class DiscretePointResolutionConverter {
	
	private DiscreteMagnitudeResolutionConverter converterX;
	private DiscreteMagnitudeResolutionConverter converterY;

	public DiscretePointResolutionConverter(Point point, int sourceResolution) {
		converterX = new DiscreteMagnitudeResolutionConverter(point.x, sourceResolution);
		converterY = new DiscreteMagnitudeResolutionConverter(point.y, sourceResolution);
	}

	public Point getPointAtResolution(int targetResolution) {
		return new Point(converterX.getMagnitudeAtResolution(targetResolution),
				converterY.getMagnitudeAtResolution(targetResolution));
	}

	public Point getFloorPointAtResolution(double targetResolution) {
		return new Point(converterX.getFloorMagnitudeAtResolution(targetResolution),
				converterY.getFloorMagnitudeAtResolution(targetResolution));
	}

	public Point getCeilingPointAtResolution(double targetResolution) {
		return new Point(converterX.getCeilingMagnitudeAtResolution(targetResolution),
				converterY.getCeilingMagnitudeAtResolution(targetResolution));
	}
}
