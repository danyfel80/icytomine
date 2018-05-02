package org.bioimageanalysis.icy.icytomine.core.view.converters;

public class DiscreteMagnitudeResolutionConverter {

	int magnitude;
	int sourceResolution;

	public DiscreteMagnitudeResolutionConverter(int magnitude, int resolution) {
		this.magnitude = magnitude;
		this.sourceResolution = resolution;
	}

	public int getMagnitudeAtResolution(int targetResolution) {
		int resolutionDifference = sourceResolution - targetResolution;

		int targetMagnitude = magnitude;
		int numIterations = Math.abs(resolutionDifference);
		boolean isMultiplying = resolutionDifference > 0;

		for (int i = 0; i < numIterations; i++) {
			if (isMultiplying) {
				targetMagnitude *= 2;
			} else
				targetMagnitude /= 2;
		}

		return targetMagnitude;
	}

	public int getFloorMagnitudeAtResolution(double targetResolution) {
		double resolutionDifference = sourceResolution - targetResolution;
		return (int) (magnitude * Math.pow(2d, resolutionDifference));
	}

	public int getCeilingMagnitudeAtResolution(double targetResolution) {
		double resolutionDifference = sourceResolution - targetResolution;
		return (int) Math.ceil(magnitude * Math.pow(2d, resolutionDifference));
	}
}
