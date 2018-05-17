package org.bioimageanalysis.icy.icytomine.core.view.converters;

public abstract class MagnificationConverter {

	public static double convertFromResolution(double baseMagnification, double resolution) {
		return baseMagnification / Math.pow(2, resolution);
	}

	public static double convertToResolution(double baseMagnification, double outputMagnification) {
		return Math.log(baseMagnification / outputMagnification) / Math.log(2);
	}

}
