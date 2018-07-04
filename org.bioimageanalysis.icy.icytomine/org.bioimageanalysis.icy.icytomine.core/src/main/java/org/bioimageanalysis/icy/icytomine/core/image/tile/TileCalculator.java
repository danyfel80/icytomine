package org.bioimageanalysis.icy.icytomine.core.image.tile;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;

public class TileCalculator {
	private Rectangle2D targetArea;
	private Dimension tileDimension;

	public TileCalculator(Rectangle2D targetArea, Dimension tileDimension) {
		this.targetArea = targetArea;
		this.tileDimension = tileDimension;
	}

	public Rectangle getTileGridBounds() {
		int xStart = (int) this.targetArea.getMinX();
		int xEnd = (int) Math.ceil(this.targetArea.getMaxX());
		int xStartTile = xStart / tileDimension.width;
		int xEndTile = (xEnd + tileDimension.width - 1) / tileDimension.width;

		int yStart = (int) this.targetArea.getMinY();
		int yEnd = (int) Math.ceil(this.targetArea.getMaxY());
		int yStartTile = yStart / tileDimension.height;
		int yEndTile = (yEnd + tileDimension.height - 1) / tileDimension.height;

		return new Rectangle(xStartTile, yStartTile, xEndTile - xStartTile, yEndTile - yStartTile);
	}
	
	public Rectangle getLimitedTileGridBounds(Dimension2D imageSize) {
		int maxXTile = (int) Math.ceil(imageSize.getWidth() / tileDimension.getWidth());
		int maxYTile = (int) Math.ceil(imageSize.getHeight() / tileDimension.getHeight());
		Rectangle imageTileGrid = new Rectangle(0, 0, maxXTile, maxYTile);
		Rectangle targetTileGrid = getTileGridBounds();
		return targetTileGrid.intersection(imageTileGrid);
	}
}
