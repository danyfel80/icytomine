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
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

/**
 * This class represents an annotation on an image.
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class Annotation extends Entity {

	public static Entity retrieve(CytomineClient client, long annotationId) throws CytomineClientException {
		return client.getAnnotation(annotationId);
	}

	private String location;
	Geometry geometry;
	Geometry latestSimplifiedGeometry;
	int latestSimplifiedGeometryResolution;
	private List<AnnotationTerm> annotationTerms;
	private Map<Long, Set<Long>> termUsers;
	private Rectangle2D adjustedBounds;
	private Rectangle2D approximativeBounds;
	private Rectangle2D adjustedApproximativeBounds;

	public Annotation(CytomineClient client, be.cytomine.client.models.Annotation internalAnnotation) {
		super(client, internalAnnotation);
		latestSimplifiedGeometryResolution = 0;
	}

	public be.cytomine.client.models.Annotation getInternalAnnotation() {
		return (be.cytomine.client.models.Annotation) getModel();
	}

	public Optional<Long> getImageInstanceId() {
		return getLong("image");
	}

	public Optional<Long> getUserId() {
		return getLong("user");
	}

	/**
	 * @throws CytomineClientException
	 *           If the user cannot be retrieved from the host server.
	 * @throws NoSuchElementException
	 *           If the annotation does not have an associated user.
	 */
	public User getUser() throws CytomineClientException, NoSuchElementException {
		long userId = getUserId().get();
		return User.retrieve(getClient(), userId);
	}

	public Rectangle2D getYAdjustedBounds() {
		if (adjustedBounds == null) {
			Envelope envelope = getGeometryAtZeroResolution(false).getEnvelopeInternal();
			adjustedBounds = new Rectangle2D.Double(envelope.getMinX(), getImage().getSizeY().get() - envelope.getMaxY(),
					envelope.getWidth(), envelope.getHeight());
			if (adjustedBounds.isEmpty()) {
				adjustedBounds = new Rectangle2D.Double(adjustedBounds.getX() - Double.MIN_VALUE,
						adjustedBounds.getY() - Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE);
			}
		}
		return adjustedBounds;
	}

	public Geometry getSimplifiedGeometryForResolution(int resolution) throws CytomineClientException {
		double pixelTolerance = 1;
		if (resolution > 0)
			for (int i = 0; i < resolution; i++)
				pixelTolerance *= 2d;
		else if (resolution < 0)
			for (int i = 0; i < -resolution; i++)
				pixelTolerance /= 2d;

		if (geometry == null || latestSimplifiedGeometry == null || latestSimplifiedGeometryResolution != resolution) {
			latestSimplifiedGeometry = getSimplifiedGeometry(pixelTolerance);
			latestSimplifiedGeometryResolution = resolution;
		}
		return latestSimplifiedGeometry;
	}

	public Rectangle2D getApproximativeBounds() throws CytomineClientException {
		if (approximativeBounds == null) {
			Geometry simplifiedGeometry = getSimplifiedGeometry(10);
			Envelope envelope = simplifiedGeometry.getEnvelopeInternal();
			approximativeBounds = new Rectangle2D.Double(envelope.getMinX(), envelope.getMinY(), envelope.getWidth(),
					envelope.getHeight());
		}
		return approximativeBounds;
	}

	public Rectangle2D getYAdjustedApproximativeBounds() throws CytomineClientException {
		if (adjustedApproximativeBounds == null) {
			Geometry simplifiedGeometry = getSimplifiedGeometry(10);
			Envelope envelope = simplifiedGeometry.getEnvelopeInternal();
			adjustedApproximativeBounds = new Rectangle2D.Double(envelope.getMinX(),
					getImage().getSizeY().get() - envelope.getMaxY(), envelope.getWidth(), envelope.getHeight());
			if (adjustedApproximativeBounds.isEmpty()) {
				adjustedApproximativeBounds = new Rectangle2D.Double(adjustedApproximativeBounds.getX() - Double.MIN_VALUE,
						adjustedApproximativeBounds.getY() - Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE);
			}
		}
		return adjustedApproximativeBounds;
	}

	private Geometry getSimplifiedGeometry(double pixelTolerance) throws CytomineClientException {
		pixelTolerance = pixelTolerance > 0? pixelTolerance: 0;
		Geometry baseGeometry = getGeometryAtZeroResolution(false);
		if (baseGeometry == null)
			throw new CytomineClientException(String.format("Null base geometry (annotation id=%d)", getId()));
		TopologyPreservingSimplifier simplifier = new TopologyPreservingSimplifier(baseGeometry);
		simplifier.setDistanceTolerance(pixelTolerance);
		return simplifier.getResultGeometry();
	}

	/**
	 * @return Geometry of the annotation without any simplification.
	 * @throws CytomineClientException
	 *           If the geometry cannot be retrieved from host server.
	 */
	public Geometry getGeometryAtZeroResolution(boolean recompute) throws CytomineClientException {
		if (this.geometry == null || recompute) {
			retrieveGeometry();
		}
		return geometry;
	}

	/**
	 * @throws CytomineClientException
	 *           If the location cannot be retrieved from host server. Also, if
	 *           the geometry cannot be constructed from server response.
	 */
	private void retrieveGeometry() throws CytomineClientException {
		geometry = null;
		Optional<String> location = getLocation();
		if (location.isPresent()) {
			WKTReader reader = new WKTReader();
			try {
				geometry = reader.read(location.get());
			} catch (ParseException e) {
				throw new CytomineClientException(String.format("Couldn't create geometry for annotation %d", getId()), e);
			}
		}
	}

	/**
	 * @return WKT formated string with this annotation geometry.
	 * @throws CytomineClientException
	 *           If the geometry cannot be retrieved.
	 */
	public Optional<String> getLocation() throws CytomineClientException {
		if (this.location == null) {
			Optional<String> locationString = getStr("location");
			if (locationString.isPresent()) {
				this.location = locationString.get();
			} else {
				this.location = getClient().getAnnotationLocation(getId()).orElse(null);
			}
		}
		return Optional.ofNullable(location);
	}

	/**
	 * @throws CytomineClientException
	 *           If the image instance cannot be retrieved from host server.
	 */
	public Image getImage() throws CytomineClientException {
		return Image.retrieve(getClient(), getImageInstanceId().get());
	}

	public Color getColor() {
		// Use current user terms first.
		Set<Term> terms = getAssociatedTermsByCurrentUser();
		if (terms.isEmpty()) {
			terms = getAssociatedTerms();
		}

		Color color;
		if (!terms.isEmpty()) {
			color = terms.iterator().next().getColor();
		} else {
			color = Term.DEFAULT_TERM_COLOR;
		}
		return color;
	}

	/**
	 * @throws CytomineClientException
	 *           If the terms of any annotation cannot be retrieved.
	 */
	public Set<Term> getAssociatedTerms() throws CytomineClientException {
		return getTermUsers().keySet().stream().map(id -> getClient().getTerm(id)).collect(Collectors.toSet());
	}

	@SuppressWarnings("unchecked")
	private Map<Long, Set<Long>> getTermUsers() throws CytomineClientException {
		if (termUsers == null) {
			try {
				termUsers = new HashMap<>();
				JSONArray termUsersArray = (JSONArray) getInternalAnnotation().get("userByTerm");
				if (termUsersArray == null) {
					termUsersArray = getClient().getAnnotationUsersByTerm(this);
				}
				for (Object tObject: termUsersArray) {
					JSONObject termUser = (JSONObject) tObject;
					long termId = (Long) termUser.get("term");
					termUsers.putIfAbsent(termId, new HashSet<>());
					JSONArray userIds;
					try {
						userIds = (JSONArray) termUser.get("user");
					} catch (ClassCastException e) {
						userIds = new JSONArray();
						userIds.add(termUser.get("user"));
					}
					for (Object uObject: userIds) {
						long userId = (Long) uObject;
						termUsers.get(termId).add(userId);
					}
				}

			} catch (Exception e) {
				throw new CytomineClientException(String.format("Could not create term users map for annotation %d", getId()),
						e);
			}
		}
		return termUsers;
	}

	public Set<Term> getAssociatedTermsByCurrentUser() throws CytomineClientException {
		Long userId = getClient().getCurrentUser().getId();
		return getAssociatedTermsByUser(userId);
	}

	public Set<Term> getAssociatedTermsByUser(long userId) throws CytomineClientException {
		return getTermUsers().entrySet().stream().filter(entry -> entry.getValue().contains(userId)).map(e -> e.getKey())
				.distinct().map(id -> getClient().getTerm(id)).collect(Collectors.toSet());
	}

	public List<AnnotationTerm> getAnnotationTerms(boolean recompute) throws CytomineClientException {
		if (annotationTerms == null || recompute) {
			annotationTerms = getClient().downloadAnnotationTerms(getId());
		}
		return annotationTerms;
	}

	public void associateTerms(Map<Term, Boolean> termSelection) throws CytomineClientException {
		getClient().associateTerms(this, termSelection);
		updateModel();
	}

	private void updateModel() {
		Annotation newModel = getClient().downloadAnnotation(getId());
		getInternalAnnotation().setAttr(newModel.getInternalAnnotation().getAttr());
		this.termUsers = null;
		this.annotationTerms = null;
	}

	@Override
	public String toString() {
		return String.format("Annotation: id=%s", String.valueOf(getId()));
	}

}
