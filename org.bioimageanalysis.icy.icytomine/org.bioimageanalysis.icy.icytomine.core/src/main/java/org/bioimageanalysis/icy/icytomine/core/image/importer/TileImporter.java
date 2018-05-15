package org.bioimageanalysis.icy.icytomine.core.image.importer;

import java.awt.image.BufferedImage;

import org.bioimageanalysis.icy.icytomine.core.model.Image;

import be.cytomine.client.CytomineException;

public class TileImporter {

	private Image imageInformation;
	private int resolution;
	private int x;
	private int y;

	public TileImporter(Image imageInformation, int resolution, int x, int y) {
		this.imageInformation = imageInformation;
		this.resolution = resolution;
		this.x = x;
		this.y = y;
	}

	public BufferedImage getTile() throws CytomineException {
		String url = imageInformation.getUrl(resolution, x, y);
		return imageInformation.getClient().downloadPictureAsBufferedImage(url, "ndpi");
	}
}
