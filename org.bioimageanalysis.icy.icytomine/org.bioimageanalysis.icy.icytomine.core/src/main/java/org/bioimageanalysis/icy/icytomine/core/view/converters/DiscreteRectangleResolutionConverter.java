package org.bioimageanalysis.icy.icytomine.core.view.converters;

import java.awt.Rectangle;

public class DiscreteRectangleResolutionConverter {

	private DiscretePointResolutionConverter converterLocation;
	private DiscreteDimensionResolutionConverter converterDimension;

	public DiscreteRectangleResolutionConverter(Rectangle rectangle, int sourceResolution) {
		converterLocation = new DiscretePointResolutionConverter(rectangle.getLocation(), sourceResolution);
		converterDimension = new DiscreteDimensionResolutionConverter(rectangle.getSize(), sourceResolution);
	}

	public Rectangle getRectangleAtResolution(int targetResolution) {
		return new Rectangle(converterLocation.getPointAtResolution(targetResolution),
				converterDimension.getDimensionAtResolution(targetResolution));
	}
}
