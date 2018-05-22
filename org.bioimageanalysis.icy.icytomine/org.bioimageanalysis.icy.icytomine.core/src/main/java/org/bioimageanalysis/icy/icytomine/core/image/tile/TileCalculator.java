package org.bioimageanalysis.icy.icytomine.core.image.tile;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;

public class TileCalculator {
	Rectangle2D imageBounds;
	int resolution;
	Dimension tileDimension;

	public TileCalculator(Rectangle2D imageBounds, int resolution, Dimension tileDimension) {
		this.imageBounds = imageBounds;
		this.resolution = resolution;
		this.tileDimension = tileDimension;
	}

	public Rectangle getTileBounds() {
		int xStart = (int) this.imageBounds.getMinX();
		int xEnd = (int) Math.ceil(this.imageBounds.getMaxX());
		int xStartTile = xStart / tileDimension.width;
		int xEndTile = (xEnd + tileDimension.width - 1) / tileDimension.width;

		int yStart = (int) this.imageBounds.getMinY();
		int yEnd = (int) Math.ceil(this.imageBounds.getMaxY());
		int yStartTile = yStart / tileDimension.height;
		int yEndTile = (yEnd + tileDimension.height - 1) / tileDimension.height;

		return new Rectangle(xStartTile, yStartTile, xEndTile - xStartTile, yEndTile - yStartTile);
	}
	
	public Rectangle getLimitedTileBounds(Dimension2D imageSize) {
		int maxXTile = (int) Math.ceil(imageSize.getWidth() / tileDimension.getWidth());
		int maxYTile = (int) Math.ceil(imageSize.getHeight() / tileDimension.getHeight());
		Rectangle imageTileGrid = new Rectangle(0, 0, maxXTile, maxYTile);
		Rectangle tileGrid = getTileBounds();
		return tileGrid.intersection(imageTileGrid);
	}
}
