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
package org.bioimageanalysis.icy.icytomine.core.model;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import be.cytomine.client.Cytomine;
import icy.roi.ROI2D;
import plugins.kernel.roi.roi2d.ROI2DPoint;
import plugins.kernel.roi.roi2d.ROI2DPolyLine;
import plugins.kernel.roi.roi2d.ROI2DPolygon;

/**
 * This class represents an annotation on an image.
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class Annotation {

	private be.cytomine.client.models.Annotation internalAnnotation;
	private Image image;
	private Cytomine cytomine;

	private int resolution;
	private List<Point2D> points;
	private ROI2D roi;

	public Annotation(be.cytomine.client.models.Annotation internalAnnotation, Image image, Cytomine cytomine) {
		this.internalAnnotation = internalAnnotation;
		this.image = image;
		this.cytomine = cytomine;
	}

	public Cytomine getClient() {
		return this.cytomine;
	}

	public Image getImage() {
		return this.image;
	}

	public be.cytomine.client.models.Annotation getInternalAnnotation() {
		return this.internalAnnotation;
	}

	public Long getId() {
		return getInternalAnnotation().getId();
	}

	public String getLocation() {
		return getInternalAnnotation().getStr("location");
	}

	/**
	 * Returns a Point, Polyline or Polygon according to the data obtained from
	 * the location attribute of this annotation. The ROI is scaled to the given
	 * resolution level.
	 * 
	 * @param resolution
	 *          Resolution level.
	 * @return ROI2D with the points presented in location attribute.
	 * @throws ParseException
	 *           If the location cannot be parsed to a geometry.
	 */
	public ROI2D getROI(int resolution) throws ParseException {

		// Create points just once
		if (this.points == null) {
			String location = getLocation();
			WKTReader reader = new WKTReader();
			Geometry geo = reader.read(location);
			Coordinate[] coords = geo.getCoordinates();
			this.points = Arrays.stream(coords).map(c -> new Point2D.Double(c.x, c.y)).collect(Collectors.toList());
		}

		// Create new roi only if the resolution changes or if it's the first
		// request
		if (this.resolution != resolution || this.roi == null) {
			this.resolution = resolution;
			double ratio = Stream.iterate(1d, r -> r / 2d).skip(resolution).findFirst().get();

			List<Point2D> scaledPoints = points.stream()
					.map(p -> new Point2D.Double(p.getX() * ratio, (getImage().getSizeY() - p.getY()) * ratio))
					.collect(Collectors.toList());

			if (scaledPoints.size() == 0) {
				this.roi = null;
			} else if (scaledPoints.size() == 1)
				this.roi = new ROI2DPoint(scaledPoints.get(0));
			else if (!(scaledPoints.get(0).equals(scaledPoints.get(scaledPoints.size() - 1)))) {
				this.roi = new ROI2DPolyLine(scaledPoints);
			} else {
				this.roi = new ROI2DPolygon(scaledPoints.subList(0, scaledPoints.size() - 1));
			}
		}
		return roi;
	}

}
