package org.bioimageanalysis.icy.icytomine.core.view.converters;

import java.awt.Dimension;

public class DiscreteDimensionResolutionConverter {
	private DiscreteMagnitudeResolutionConverter converterX;
	private DiscreteMagnitudeResolutionConverter converterY;

	public DiscreteDimensionResolutionConverter(Dimension dimension, int sourceResolution) {
		converterX = new DiscreteMagnitudeResolutionConverter(dimension.width, sourceResolution);
		converterY = new DiscreteMagnitudeResolutionConverter(dimension.height, sourceResolution);
	}

	public Dimension getDimensionAtResolution(int targetResolution) {
		return new Dimension(converterX.getMagnitudeAtResolution(targetResolution),
				converterY.getMagnitudeAtResolution(targetResolution));
	}

	public Dimension getFloorDimensionAtResolution(double targetResolution) {
		return new Dimension(converterX.getFloorMagnitudeAtResolution(targetResolution),
				converterY.getFloorMagnitudeAtResolution(targetResolution));
	}

	public Dimension getCeilingDimensionAtResolution(double targetResolution) {
		return new Dimension(converterX.getCeilingMagnitudeAtResolution(targetResolution),
				converterY.getCeilingMagnitudeAtResolution(targetResolution));
	}
}
