/*
 * Copyright 2010-2016 Institut Pasteur.
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

import java.awt.Point;

import org.bioimageanalysis.icy.icytomine.core.model.Image;

/**
 * @author Daniel Felipe Gonzalez Obando
 */
public class Tile2DKey {

	private final Image image;
	private final long resolution;
	private final int x;
	private final int y;

	private final int hashCode;

	public Tile2DKey(Image image, long resolution, int x, int y) {
		this.image = image;
		this.resolution = resolution;
		this.x = x;
		this.y = y;

		this.hashCode = computeHashCode();
	}

	public Tile2DKey(Tile2DKey k) {
		this.image = k.image;
		this.resolution = k.resolution;
		this.x = k.x;
		this.y = k.y;
		this.hashCode = k.hashCode;
	}

	private int computeHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((image == null) ? 0 : image.hashCode());
		result = prime * result + (int) (resolution ^ (resolution >>> 32));
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	/**
	 * @return The id of the abstract image.
	 */
	public Image getImage() {
		return image;
	}

	/**
	 * @return The resolution level.
	 */
	public long getResolution() {
		return resolution;
	}

	/**
	 * @return The tile x coordinate.
	 */
	public int getX() {
		return x;
	}

	/**
	 * @return The tile y coordinate.
	 */
	public int getY() {
		return y;
	}

	/**
	 * @return The tile position.
	 */
	public Point getPosition() {
		return new Point(x, y);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return hashCode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Tile2DKey)) {
			return false;
		}
		Tile2DKey other = (Tile2DKey) obj;
		if (image == null) {
			if (other.image != null) {
				return false;
			}
		} else if (!image.equals(other.image)) {
			return false;
		}
		if (resolution != other.resolution) {
			return false;
		}
		if (x != other.x) {
			return false;
		}
		if (y != other.y) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("Tile2DKey [hashCode=%s, host=%s, image=%s(id=%s), resolution=%s, x=%s, y=%s]", hashCode,
				image.getClient().getHost(), image, image.getId(), resolution, x, y);
	}

}
