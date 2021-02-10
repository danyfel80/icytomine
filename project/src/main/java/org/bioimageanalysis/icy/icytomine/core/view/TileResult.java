/*
 * Copyright 2010-2018 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bioimageanalysis.icy.icytomine.core.view;

import java.awt.image.BufferedImage;

/**
 * Object of this class contain the result of a tile request to a Cytomine
 * server.
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class TileResult {
	private final Tile2DKey			key;
	private final BufferedImage	tile;

	public TileResult(Tile2DKey key, BufferedImage tile) {
		this.key = key;
		this.tile = tile;
	}

	/**
	 * @return The key.
	 */
	public Tile2DKey getKey() {
		return key;
	}

	/**
	 * @return The tile.
	 */
	public BufferedImage getTileImage() {
		return tile;
	}

}
