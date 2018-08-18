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
package org.bioimageanalysis.icy.icytomine.geom;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * Geometric hash class represented by a grid. The grid contains 'cols' many
 * columns and 'lines' many lines that form 'cols'x'lines' many equi-sized cells
 * filling the rectangle 'area'. Every cell contains objects of type 'T'.
 * Objects that are outside of 'area' are fetched in infinitely big cells by
 * extending the grid-lines to infinity in both directions.
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class GeometricHash<T> {

	private Rectangle2D area;
	private int columns;
	private int rows;
	private List<Set<T>> cells;

	/**
	 * Creates a geometric hash on the rectangular 'area' with 'cols' columns and
	 * 'rows' rows.
	 * 
	 * @param area
	 * @param cols
	 * @param rows
	 */
	public GeometricHash(Rectangle2D area, int cols, int rows) throws IllegalArgumentException {
		if (cols < 1)
			throw new IllegalArgumentException("Column count less than 1.");
		if (rows < 1)
			throw new IllegalArgumentException("Row count less than 1.");
		this.area = area;
		this.columns = cols;
		this.rows = rows;

		this.cells = new ArrayList<>((columns + 2) * (rows + 2));
		IntStream.range(0, (columns + 2) * (rows + 2))
				.forEach(i -> this.cells.add(Collections.synchronizedSet(new HashSet<>())));
	}

	/**
	 * @param area
	 * @param cellCount
	 */
	public GeometricHash(Rectangle2D area, int cellCount) {
		if (cellCount < 1)
			throw new IllegalArgumentException("Cell count less than 1.");

		this.area = area;
		double rel = Math.abs(area.getWidth() / area.getHeight());
		columns = Math.max(1, (int) Math.sqrt(cellCount * rel));

		// We set columns twice because of big rels lead to big columns leading to 1
		// lines. When setting columns a second time in dependence to lines, columns
		// doesn't get larger than numCells.
		rows = Math.max(1, cellCount / columns);
		columns = Math.max(1, cellCount / rows);
		cells = new ArrayList<>((columns + 2) * (rows + 2));
		IntStream.range(0, (columns + 2) * (rows + 2)).forEach(i -> this.cells.add(new HashSet<>()));
	}

	/**
	 * @return Rectangle representing the area of this hash.
	 */
	public Rectangle2D area() {
		return area;
	}

	/**
	 * @return Number of columns.
	 */
	public int columns() {
		return columns;
	}

	/**
	 * @return Number of rows.
	 */
	public int rows() {
		return rows;
	}

	/**
	 * @param column
	 *          vertical gridline.
	 * @return Horizontal position of column-th vertical gridline. Valid values
	 *         for 'column' are between -1 and columns+1.
	 * @throws IllegalArgumentException
	 *           If column is not in [-1,columns+1]
	 */
	public double verticalLinePosition(int column) throws IllegalArgumentException {
		if (column < -1 || column > columns + 1)
			throw new IllegalArgumentException("Invalid column (<-1 or >columns+1");
		// Please, note that the most natural way would be something like setting
		// left = area.left() + column/columns*area.getWidth().
		// But this method is not stable since numerical errors of
		// area.getWidth()/columns accumulate with bigger column's. The following
		// approach distributes those errors on the whole grid.

		double rel = (double) column / columns;

		double pos = rel * area.getMaxX() + (1d - rel) * area.getMinX();

		if (column == -1)
			pos = Double.NEGATIVE_INFINITY;
		if (column == columns + 1)
			pos = Double.POSITIVE_INFINITY;

		return pos;
	}

	/**
	 * @param row
	 *          horizontal gridline.
	 * @return Vertical position of row-th horizontal gridline. Valid values for
	 *         'row' are between -1 and rows+1.
	 * @throws IllegalArgumentException
	 *           If row is not in [-1,row+1]
	 */
	public double horizontalLinePosition(int row) throws IllegalArgumentException {
		if (row < -1 || row > rows + 1)
			throw new IllegalArgumentException("Invalid row (<-1 or >rows+1");
		// Please, note that the most natural way would be something like setting
		// bottom = area.bottom() + row/rows*area.getHeight().
		// But this method is not stable since numerical errors of
		// area.getHeight()/rows accumulate with bigger row's. The following
		// approach distributes those errors on the whole grid.

		double rel = (double) row / rows;

		double pos = rel * area.getMaxY() + (1d - rel) * area.getMinY();

		if (row == -1)
			pos = Double.NEGATIVE_INFINITY;
		if (row == rows + 1)
			pos = Double.POSITIVE_INFINITY;

		return pos;
	}

	/**
	 * Get the cell position of the cell hit by vector pos.
	 * 
	 * @param pos
	 *          Vector used to find coordinates.
	 * @return coordinates which is hit by vector pos ([x,y]).
	 */
	public int[] cellContaining(Point2D pos) {
		int[] coord = new int[2];
		if (pos.getX() >= area.getMaxX())
			coord[0] = columns;
		else if (pos.getX() < area.getMinX())
			coord[0] = -1;
		else {
			double dc = ((pos.getX() - area.getMinX()) * columns) / area.getWidth();

			if (dc >= columns)
				coord[0] = columns;
			else if (dc <= -1)
				coord[0] = -1;
			else {
				int ic = (int) dc;
				coord[0] = Math.max(Math.min(ic, columns), -1);
			}
		}

		if (pos.getY() >= area.getMaxY())
			coord[1] = rows;
		else if (pos.getY() < area.getMinY())
			coord[1] = -1;
		else {
			double dl = (pos.getY() - area.getMinY()) * rows / area.getHeight();

			if (dl >= rows)
				coord[1] = rows;
			else if (dl <= -1)
				coord[1] = -1;
			else {
				int il = (int) dl;
				coord[1] = Math.max(Math.min(il, rows), -1);
			}
		}
		return coord;
	}

	/**
	 * Get range of cells intersecting rectangle.
	 * 
	 * @param rectangle
	 *          Rectangle used to find cells.
	 * @return cell range containing the rectangle ([bottom, top, left, right]).
	 */
	public int[] cellsContaining(Rectangle2D rectangle) {
		int[] range = new int[4];
		int[] lb = cellContaining(new Point2D.Double(rectangle.getMinX(), rectangle.getMinY()));
		int[] rt = cellContaining(new Point2D.Double(rectangle.getMaxX(), rectangle.getMaxY()));
		range[0] = lb[1];
		range[1] = rt[1];
		range[2] = lb[0];
		range[3] = rt[0];
		return range;
	}

	/**
	 * Get area of cell at column-th columns and row-th row.
	 * 
	 * @param column
	 *          Column of cell.
	 * @param row
	 *          Row of cell.
	 * @return area of cell at column and row.
	 * @throws IllegalArgumentException
	 *           If column is less than -1 or column is greater than columns or
	 *           row is less than -1 or row is greater than rows.
	 */
	public Rectangle2D cellArea(int column, int row) throws IllegalArgumentException {
		if (column < -1 || column > columns)
			throw new IllegalArgumentException("Invalid column < -1 or > columns");
		if (row < -1 || row > rows)
			throw new IllegalArgumentException("Invalid row < -1 or > rows");
		return new Rectangle2D.Double(horizontalLinePosition(row), horizontalLinePosition(row + 1),
				verticalLinePosition(column), verticalLinePosition(column + 1));
	}

	/**
	 * Add an object to cells containing area.
	 * 
	 * @param object
	 *          Object to insert in cells.
	 * @param area
	 *          Area to add object.
	 */
	public void addObjectAt(T object, Rectangle2D area) {
		int[] range = cellsContaining(area);

		for (int y = range[0]; y <= range[1]; y++) {
			for (int x = range[2]; x <= range[3]; x++) {
				cellObjectsAt(x, y).add(object);
			}
		}
	}

	/**
	 * Retrieves objects in cells containing a given rectangle.
	 * 
	 * @param rectangle
	 *          Rectangle to search for objects.
	 * @return Objects in cells.
	 * @throws InterruptedException
	 *           If the thread gets interrupted
	 */
	public Set<T> cellObjectsAt(Rectangle2D rectangle) throws InterruptedException {
		Set<T> objs = new HashSet<>();

		int[] range = cellsContaining(rectangle);

		for (int y = range[0]; y <= range[1]; y++) {
			for (int x = range[2]; x <= range[3]; x++) {
				if (Thread.interrupted())
					throw new InterruptedException();
				objs.addAll(cellObjectsAt(x, y));
			}
		}

		return objs;
	}

	/**
	 * Get the cell-set of a specific cell.
	 * 
	 * @param column
	 *          Column of cell.
	 * @param row
	 *          Row of cell.
	 * @return Set of elements in cell.
	 */
	protected Set<T> cellObjectsAt(int column, int row) {
		if (column < -1 || column > columns)
			throw new IllegalArgumentException("Invalid column < -1 or > columns");
		if (row < -1 || row > rows)
			throw new IllegalArgumentException("Invalid row < -1 or > rows");

		return cells.get((column + 1) * (rows + 2) + (row + 1));
	}

	/**
	 * Remove all objects in all cells.
	 */
	public void clear() {
		for (Set<T> cell: cells) {
			cell.clear();
		}
	}

}
