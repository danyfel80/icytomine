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

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import be.cytomine.client.Cytomine;
import be.cytomine.client.CytomineException;
import be.cytomine.client.collections.TermCollection;
import icy.painter.Anchor2D;
import icy.roi.ROI2D;
import plugins.kernel.roi.roi2d.ROI2DLine;
import plugins.kernel.roi.roi2d.ROI2DPoint;
import plugins.kernel.roi.roi2d.ROI2DPolyLine;
import plugins.kernel.roi.roi2d.ROI2DPolygon;
import plugins.kernel.roi.roi2d.ROI2DRectShape;
import plugins.kernel.roi.roi2d.ROI2DShape;

/**
 * This class represents an annotation on an image.
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class Annotation {

	private Cytomine cytomine;
	private Image image;
	private be.cytomine.client.models.Annotation internalAnnotation;
	private User user;

	private String location;

	private int resolution;
	private List<Point2D> points;
	private ROI2DShape roi;
	private List<Term> terms;

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

	public String getLocation() throws CytomineException {
		if (this.location == null) {
			this.location = getInternalAnnotation().getStr("location");
			if (this.location == null) {
				this.internalAnnotation = getClient().getAnnotation(getId());
				this.location = getInternalAnnotation().getStr("location");
			}
		}
		return location;
	}

	/**
	 * Converts a ROI2DShape to a WKT formatted string.
	 * 
	 * @param shape
	 *          ROI with point-coordinates relative to full image on server at
	 *          resolution 0.
	 * @return Shape description in WKT format.
	 */
	public static String convertToWKT(ROI2DShape shape) {
		List<Anchor2D> points = new ArrayList<>(shape.getControlPoints());

		StringBuffer buffer = new StringBuffer();
		if (shape instanceof ROI2DPoint) {
			buffer.append("POINT(");
			buffer.append(String.format("%f %f", points.get(0).getPositionX(), points.get(0).getPositionY()));
			buffer.append(")");
		} else if (shape instanceof ROI2DLine || shape instanceof ROI2DPolyLine) {
			buffer.append("LINESTRING (");
			buffer.append(points.stream().map(p -> String.format("%f %f", p.getPositionX(), p.getPositionY()))
					.collect(Collectors.joining(",")));
			buffer.append(")");
		} else {
			buffer.append("POLYGON((");
			if (shape instanceof ROI2DRectShape) {
				Rectangle2D rect = ((ROI2DRectShape) shape).getBounds2D();
				buffer.append(String.format("%f %f, ", rect.getMinX(), rect.getMinY()));
				buffer.append(String.format("%f %f, ", rect.getMinX(), rect.getMaxY()));
				buffer.append(String.format("%f %f, ", rect.getMaxX(), rect.getMaxY()));
				buffer.append(String.format("%f %f, ", rect.getMaxX(), rect.getMinY()));
				buffer.append(String.format("%f %f", rect.getMinX(), rect.getMinY()));
			} else {
				buffer.append(points.stream().map(p -> String.format("%f %f", p.getPositionX(), p.getPositionY()))
						.collect(Collectors.joining(",")));
				buffer.append(String.format(",%f %f", points.get(0).getPositionX(), points.get(0).getPositionY()));
			}
			buffer.append("))");
		}
		return buffer.toString();
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
	 * @throws CytomineException
	 *           If terms for this annotation cannot be retrieved.
	 */
	public ROI2DShape getROI(int resolution) throws ParseException, CytomineException {

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

			List<Term> terms = getTerms();
			if (!terms.isEmpty()) {
				Color color = terms.get(0).getColor();
				this.roi.setColor(color);
			}

			this.roi.setProperty("cytomineId", Objects.toString(this.getId()));
		}

		return roi;
	}

	public List<Term> getTerms() throws CytomineException {
		if (terms == null) {
			TermCollection nativeAnnotationTerms = Optional.ofNullable(getClient().getTermsByAnnotation(getId()))
					.orElse(new TermCollection(0, 0));
			ThreadPoolExecutor tp = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
			CompletionService<Term> cs = new ExecutorCompletionService<>(tp);
			IntStream.range(0, nativeAnnotationTerms.size()).mapToObj(i -> nativeAnnotationTerms.get(i))
					.forEach(aTerm -> cs.submit(() -> new Term(getClient(), getClient().getTerm(aTerm.getLong("term")))));
			tp.shutdown();
			terms = new ArrayList<>(nativeAnnotationTerms.size());
			for (int i = 0; i < nativeAnnotationTerms.size(); i++) {
				try {
					terms.add(cs.take().get());
				} catch (ExecutionException | InterruptedException e) {
					e.printStackTrace();
					return new ArrayList<>(0);
				}
			}
			if (terms.size() == 0)
				terms.add(Term.getNoTerm(getClient()));
		}
		return terms;
	}

	public Long getUserId() {
		return internalAnnotation.getLong("user");
	}

	public Rectangle2D getBounds() throws ParseException, CytomineException {
		ROI2D roi = getROI(0);
		Rectangle2D bounds = roi.getBounds2D();
		return bounds;
	}

	public User getUser() {
		if (user == null) {
			try {
				user = new User(getClient().getUser(getUserId()));
			} catch (CytomineException e) {
				e.printStackTrace();
				be.cytomine.client.models.User dummyUser = new be.cytomine.client.models.User();
				dummyUser.set("id", 0L);
				user = new User(dummyUser);
			}
		}
		return user;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cytomine == null) ? 0 : cytomine.hashCode());
		result = prime * result + ((internalAnnotation == null) ? 0 : internalAnnotation.getId().hashCode());
		return result;
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
		if (!(obj instanceof Annotation)) {
			return false;
		}
		Annotation other = (Annotation) obj;
		if (cytomine == null) {
			if (other.cytomine != null) {
				return false;
			}
		} else if (!cytomine.equals(other.cytomine)) {
			return false;
		}
		if (internalAnnotation == null) {
			if (other.internalAnnotation != null) {
				return false;
			}
		} else if (!internalAnnotation.getId().equals(other.internalAnnotation.getId())) {
			return false;
		}
		return true;
	}

}
