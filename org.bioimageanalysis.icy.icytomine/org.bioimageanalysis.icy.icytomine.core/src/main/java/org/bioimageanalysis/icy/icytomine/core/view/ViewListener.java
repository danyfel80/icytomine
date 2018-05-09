package org.bioimageanalysis.icy.icytomine.core.view;

import java.awt.image.BufferedImage;

@FunctionalInterface
public interface ViewListener {
	public void onViewChanged(BufferedImage... newView);
}