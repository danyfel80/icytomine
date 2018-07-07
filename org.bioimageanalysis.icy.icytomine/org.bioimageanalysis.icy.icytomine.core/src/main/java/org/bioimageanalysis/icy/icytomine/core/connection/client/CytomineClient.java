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
package org.bioimageanalysis.icy.icytomine.core.connection.client;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.core.model.AnnotationTerm;
import org.bioimageanalysis.icy.icytomine.core.model.Description;
import org.bioimageanalysis.icy.icytomine.core.model.Entity;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.model.Ontology;
import org.bioimageanalysis.icy.icytomine.core.model.Project;
import org.bioimageanalysis.icy.icytomine.core.model.Term;
import org.bioimageanalysis.icy.icytomine.core.model.User;
import org.bioimageanalysis.icy.icytomine.core.model.cache.AnnotationCache;
import org.bioimageanalysis.icy.icytomine.core.model.cache.DescriptionCache;
import org.bioimageanalysis.icy.icytomine.core.model.cache.EntityCacheException;
import org.bioimageanalysis.icy.icytomine.core.model.cache.ImageInstanceCache;
import org.bioimageanalysis.icy.icytomine.core.model.cache.OntologyCache;
import org.bioimageanalysis.icy.icytomine.core.model.cache.ProjectCache;
import org.bioimageanalysis.icy.icytomine.core.model.cache.TermCache;
import org.bioimageanalysis.icy.icytomine.core.model.cache.UserCache;
import org.bioimageanalysis.icy.icytomine.core.model.key.DescriptionId;
import org.bioimageanalysis.icy.icytomine.geom.WKTUtils;
import org.json.simple.JSONObject;

import be.cytomine.client.Cytomine;
import be.cytomine.client.CytomineException;
import be.cytomine.client.collections.AnnotationCollection;
import be.cytomine.client.collections.ImageInstanceCollection;
import be.cytomine.client.collections.ProjectCollection;
import be.cytomine.client.collections.TermCollection;
import be.cytomine.client.collections.UserCollection;
import be.cytomine.client.models.ImageServers;

/**
 * This class represents the connection point between local and remote cytomine
 * model.
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class CytomineClient implements AutoCloseable {

	/**
	 * @throws CytomineClientException
	 *           If the host server is not well specified. Also if the user
	 *           credentials are not correct.
	 */
	public static CytomineClient create(URL host, String publicKey, String privateKey) throws CytomineClientException {
		Cytomine client = new Cytomine(host.toString(), publicKey, privateKey);
		CytomineClient cytomineClient = new CytomineClient(client);
		return cytomineClient;
	}

	private Cytomine internalClient;
	private User currentUser;

	private UserCache userCache;
	private ProjectCache projectCache;
	private DescriptionCache descriptionCache;
	private OntologyCache ontologyCache;
	private TermCache termCache;
	private ImageInstanceCache imageInstanceCache;
	private AnnotationCache annotationCache;

	/**
	 * @throws CytomineClientException
	 *           see {@link #create(URL, String, String)}
	 */
	private CytomineClient(Cytomine client) throws CytomineClientException {
		this.internalClient = client;
		checkCurrentUser();
	}

	/**
	 * @throws CytomineClientException
	 *           If credentials are not recognized by host server. Also, if a
	 *           connection to the host server cannot be established.
	 */
	private void checkCurrentUser() throws CytomineClientException {
		try {
			be.cytomine.client.models.User internalUser = getInternalClient().getCurrentUser();
			if (internalUser.getAttr() == null)
				throw new CytomineClientException(
						String.format("User credentials not recognized for public key: %s", getInternalClient().getPublicKey()));
		} catch (CytomineException e) {
			throw new CytomineClientException("Could not connect to server: " + e.getMessage(), e);
		}
	}

	protected Cytomine getInternalClient() {
		return internalClient;
	}

	public String getHost() {
		return getInternalClient().getHost();
	}

	public String getPublicKey() {
		return getInternalClient().getPublicKey();
	}

	/**
	 * @throws CytomineClientException
	 *           If the user data cannot be retrieved from the host server.
	 */
	public User getCurrentUser() throws CytomineClientException {
		if (currentUser == null) {
			currentUser = downloadCurrentUser();
			getUserCache().store(currentUser.getId(), currentUser);
		}
		return currentUser;
	}

	private User downloadCurrentUser() throws CytomineClientException {
		try {
			be.cytomine.client.models.User user = getInternalClient().getCurrentUser();
			if (user.getAttr() == null) {
				throw new CytomineClientException("No user data downloaded");
			}
			return new User(this, user);
		} catch (CytomineException e) {
			throw new CytomineClientException("Could not download user data", e);
		}
	}

	private UserCache getUserCache() {
		if (userCache == null) {
			userCache = UserCache.create(this);
		}
		return userCache;
	}

	/**
	 * @throws CytomineClientException
	 *           If the user data cannot be retrieved from the host server.
	 */
	public User getUser(long userId) throws CytomineClientException {
		User user;
		try {
			user = getUserCache().retrieve(userId);
		} catch (EntityCacheException e) {
			user = downloadUser(userId);
			getUserCache().store(userId, user);
		}
		return user;
	}

	private User downloadUser(long userId) throws CytomineClientException {
		try {
			be.cytomine.client.models.User user = getInternalClient().getUser(userId);
			if (user.getAttr() == null) {
				throw new CytomineClientException("No user data downloaded");
			}
			return new User(this, user);
		} catch (CytomineException e) {
			throw new CytomineClientException(String.format("Could not download user data (user id=%d)", userId), e);
		}
	}

	/**
	 * @throws CytomineClientException
	 *           If the user projects cannot be retrieved from the host server.
	 */
	public List<Project> getUserProjects(long userId) throws CytomineClientException {
		ProjectCollection projectCollection;
		try {
			projectCollection = getInternalClient().getProjectsByUser(userId);
		} catch (CytomineException e) {
			throw new CytomineClientException(String.format("Could not download user projects (user id=%d)", userId), e);
		}

		List<Project> projects = new ArrayList<>(projectCollection.size());
		for (int i = 0; i < projectCollection.size(); i++) {
			Project project = new Project(this, projectCollection.get(i));
			getProjectCache().store(project.getId(), project);
			projects.add(project);
		}

		return projects;
	}

	/**
	 * @throws CytomineClientException
	 *           If the project data cannot be retrieved from the host server.
	 */
	public Project getProject(long projectId) throws CytomineClientException {
		Project project;
		try {
			project = getProjectCache().retrieve(projectId);
		} catch (EntityCacheException e) {
			project = downloadProject(projectId);
			getProjectCache().store(projectId, project);
		}
		return project;
	}

	private ProjectCache getProjectCache() {
		if (projectCache == null) {
			projectCache = ProjectCache.create(this);
		}
		return projectCache;
	}

	private Project downloadProject(long projectId) throws CytomineClientException {
		try {
			be.cytomine.client.models.Project project = getInternalClient().getProject(projectId);
			if (project.getAttr() == null) {
				throw new CytomineClientException("No project data downloaded");
			}
			return new Project(this, project);
		} catch (CytomineException e) {
			throw new CytomineClientException(String.format("Could not download project data (project id=%d)", projectId), e);
		}
	}

	/**
	 * @throws CytomineClientException
	 *           If the description data cannot be retrieved from the host server.
	 */
	public Description getDescription(long describedEntityId, String describedDomainName) throws CytomineClientException {
		Description description;
		DescriptionId descriptionId = new DescriptionId(describedDomainName, describedEntityId);
		try {
			description = getDescriptionCache().retrieve(descriptionId);
		} catch (EntityCacheException e) {
			description = downloadDescription(descriptionId);
			getDescriptionCache().store(descriptionId, description);
		}
		return description;
	}

	private DescriptionCache getDescriptionCache() {
		if (descriptionCache == null) {
			descriptionCache = DescriptionCache.create(this);
		}
		return descriptionCache;
	}

	private Description downloadDescription(DescriptionId descriptionId) throws CytomineClientException {
		try {
			be.cytomine.client.models.Description description = getInternalClient()
					.getDescription(descriptionId.getDescribedEntityId(), descriptionId.getDescribedDomainName());
			if (description.getAttr() == null) {
				throw new CytomineClientException("No description data downloaded");
			}
			return new Description(this, description);
		} catch (CytomineException e) {
			throw new CytomineClientException(
					String.format("Could not download description data (description key=%s)", descriptionId), e);
		}
	}

	/**
	 * @throws CytomineClientException
	 *           If the project users cannot be retrieved from the host server.
	 */
	public List<User> getProjectUsers(long projectId) throws CytomineClientException {
		UserCollection userCollection;
		try {
			userCollection = getInternalClient().getProjectUsers(projectId);
		} catch (CytomineException e) {
			throw new CytomineClientException(String.format("Could not download project users (project id=%d)", projectId),
					e);
		}

		List<User> users = new ArrayList<>(userCollection.size());
		for (int i = 0; i < userCollection.size(); i++) {
			User user = new User(this, userCollection.get(i));
			getUserCache().store(user.getId(), user);
			users.add(user);
		}

		return users;
	}

	/**
	 * @throws CytomineClientException
	 *           If the ontology data cannot be retrieved from the host server.
	 */
	public Ontology getOntology(long ontologyId) throws CytomineClientException {
		Ontology ontology;
		try {
			ontology = getOntologyCache().retrieve(ontologyId);
		} catch (EntityCacheException e) {
			ontology = downloadOntology(ontologyId);
			getOntologyCache().store(ontologyId, ontology);
		}
		return ontology;
	}

	private OntologyCache getOntologyCache() {
		if (ontologyCache == null) {
			ontologyCache = OntologyCache.create(this);
		}
		return ontologyCache;
	}

	private Ontology downloadOntology(long ontologyId) throws CytomineClientException {
		be.cytomine.client.models.Ontology ontology;
		try {
			ontology = getInternalClient().getOntology(ontologyId);
			if (ontology.getAttr() == null) {
				throw new CytomineClientException("No ontology data downloaded");
			}
			return new Ontology(this, ontology);
		} catch (CytomineException e) {
			throw new CytomineClientException(String.format("Could not download ontology data (ontology id=%d)", ontologyId),
					e);
		}
	}

	/**
	 * @throws CytomineClientException
	 *           If the ontology terms cannot be retrieved from the host server.
	 */
	public Set<Term> getOntologyTerms(long ontologyId) throws CytomineClientException {
		TermCollection termCollection;
		try {
			termCollection = getInternalClient().getTermsByOntology(ontologyId);
		} catch (CytomineException e) {
			throw new CytomineClientException(String.format("Could not download ontology terms (ontology id=%d)", ontologyId),
					e);
		}

		Set<Term> terms = new HashSet<>(termCollection.size());
		for (int i = 0; i < termCollection.size(); i++) {
			Term term = new Term(this, termCollection.get(i));
			getTermCache().store(term.getId(), term);
			terms.add(term);
		}

		return terms;
	}

	private TermCache getTermCache() {
		if (termCache == null) {
			termCache = TermCache.create(this);
		}
		return termCache;
	}

	/**
	 * @throws CytomineClientException
	 *           If the term cannot be retrieved from the host server.
	 */
	public Term getTerm(long termId) throws CytomineClientException {
		Term term;
		try {
			term = getTermCache().retrieve(termId);
		} catch (EntityCacheException e) {
			term = downloadTerm(termId);
			getTermCache().store(termId, term);
		}
		return term;
	}

	private Term downloadTerm(long termId) throws CytomineClientException {
		be.cytomine.client.models.Term term;
		try {
			term = getInternalClient().getTerm(termId);
			if (term.getAttr() == null) {
				throw new CytomineClientException("No term data downloaded");
			}
			return new Term(this, term);
		} catch (CytomineException e) {
			throw new CytomineClientException(String.format("Could not download term data (term id=%d)", termId), e);
		}
	}

	/**
	 * @throws CytomineClientException
	 *           If the project images cannot be retrieved from the host server.
	 */
	public List<Image> getProjectImages(long projectId) throws CytomineClientException {
		ImageInstanceCollection imageInstanceCollection;
		try {
			imageInstanceCollection = getInternalClient().getImageInstances(projectId);
		} catch (CytomineException e) {
			throw new CytomineClientException(String.format("Could not download project images (project id=%d)", projectId),
					e);
		}

		List<Image> images = new ArrayList<>(imageInstanceCollection.size());
		for (int i = 0; i < imageInstanceCollection.size(); i++) {
			Image image = new Image(this, imageInstanceCollection.get(i));
			getImageCache().store(image.getId(), image);
			images.add(image);
		}

		return images;
	}

	private ImageInstanceCache getImageCache() {
		if (imageInstanceCache == null) {
			imageInstanceCache = ImageInstanceCache.create(this);
		}
		return imageInstanceCache;
	}

	/**
	 * @throws CytomineClientException
	 *           If the image instance cannot be retrieved from the host server.
	 */
	public Image getImageInstance(long imageInstanceId) throws CytomineClientException {
		Image image;
		try {
			image = getImageCache().retrieve(imageInstanceId);
		} catch (EntityCacheException e) {
			image = downloadImage(imageInstanceId);
			getImageCache().store(imageInstanceId, image);
		}
		return image;
	}

	private Image downloadImage(long imageInstanceId) throws CytomineClientException {
		be.cytomine.client.models.ImageInstance imageInstance;
		try {
			imageInstance = getInternalClient().getImageInstance(imageInstanceId);
			if (imageInstance.getAttr() == null) {
				throw new CytomineClientException("No image instance data downloaded");
			}
			return new Image(this, imageInstance);
		} catch (CytomineException e) {
			throw new CytomineClientException(
					String.format("Could not download image instance data (image instance id=%d)", imageInstanceId), e);
		}
	}

	/**
	 * @throws CytomineClientException
	 *           If the image servers cannot be retrieved from the host server.
	 */
	public List<String> getImageServers(Image image) throws CytomineClientException {
		ImageServers servers;
		try {
			servers = getInternalClient().getImageInstanceServers(image.getInternalImage());
		} catch (CytomineException e) {
			throw new CytomineClientException(String.format("Could not download image servers (image=%s)", image.toString()),
					e);
		}
		return servers.getServerList();
	}

	/**
	 * @throws CytomineClientException
	 *           If the image preview cannot be retrieved from the host server.
	 */
	public BufferedImage downloadImageAsBufferedImage(long abstractImageId, int maxSize) throws CytomineClientException {
		try {
			return getInternalClient().downloadAbstractImageAsBufferedImage(abstractImageId, maxSize);
		} catch (CytomineException e) {
			throw new CytomineClientException(
					String.format("Could not download image as buffered image (abstract image id=%d)", abstractImageId), e);
		}
	}

	/**
	 * @return Annotations of the specified image in simple format (i.e. No
	 *         geometry data). To retrieve geometry data use
	 *         {@link #getFullImageAnnotations(long)}.
	 * 
	 * @throws CytomineClientException
	 *           If the image annotations cannot be retrieved from the host
	 *           server.
	 */
	public List<Annotation> getImageAnnotations(long imageInstanceId) throws CytomineClientException {
		AnnotationCollection annotationCollection;
		try {
			annotationCollection = getInternalClient().getAnnotationsByImage(imageInstanceId);
		} catch (CytomineException e) {
			throw new CytomineClientException(
					String.format("Could not download image annotations (image instance id=%d)", imageInstanceId), e);
		}

		List<Annotation> annotations = new ArrayList<>(annotationCollection.size());
		for (int i = 0; i < annotationCollection.size(); i++) {
			Annotation annotation = new Annotation(this, annotationCollection.get(i));
			getAnnotationCache().store(annotation.getId(), annotation);
			annotations.add(annotation);
		}

		return annotations;
	}

	private AnnotationCache getAnnotationCache() {
		if (annotationCache == null) {
			annotationCache = AnnotationCache.create(this);
		}
		return annotationCache;
	}

	/**
	 * @return Annotations of the specified image in full format (i.e. With
	 *         geometry data). To retrieve simple data use
	 *         {@link #getImageAnnotations(long)}.
	 * @throws CytomineClientException
	 *           If the image annotations cannot be retrieved from the host
	 *           server.
	 */
	public List<Annotation> getFullImageAnnotations(long imageInstanceId) throws CytomineClientException {
		AnnotationCollection annotationCollection;
		try {
			Map<String, String> filters = getFullImageAnnotationsFilters(imageInstanceId);
			annotationCollection = getInternalClient().getAnnotations(filters);
		} catch (CytomineException e) {
			throw new CytomineClientException(
					String.format("Could not download full image annotations (image instance id=%d)", imageInstanceId), e);
		}

		List<Annotation> annotations = new ArrayList<>(annotationCollection.size());
		for (int i = 0; i < annotationCollection.size(); i++) {
			Annotation annotation = new Annotation(this, annotationCollection.get(i));
			getAnnotationCache().store(annotation.getId(), annotation);
			annotations.add(annotation);
		}

		return annotations;
	}

	private Map<String, String> getFullImageAnnotationsFilters(long imageInstanceId) {
		Map<String, String> filters = new HashMap<>();
		filters.put("image", String.valueOf(imageInstanceId));
		filters.put("showMeta", "true");
		filters.put("showWKT", "true");
		filters.put("showGIS", "true");
		filters.put("showTerm", "true");
		return filters;
	}

	public List<Annotation> getFullImageAnnotations(Long imageInstanceId, Rectangle2D currentTileArea)
			throws CytomineClientException {
		AnnotationCollection annotationCollection;
		try {
			Map<String, String> filters = getFullImageAnnotationsFilters(imageInstanceId);
			filters.put("bbox", WKTUtils.createFromRectangle2D(currentTileArea).replace(" ", "%20"));
			annotationCollection = getInternalClient().getAnnotations(filters);
		} catch (CytomineException e) {
			throw new CytomineClientException(
					String.format("Could not download full image annotations (image instance id=%d) for area (%s)",
							imageInstanceId, String.valueOf(currentTileArea)),
					e);
		}

		List<Annotation> annotations = new ArrayList<>(annotationCollection.size());
		for (int i = 0; i < annotationCollection.size(); i++) {
			Annotation annotation = new Annotation(this, annotationCollection.get(i));
			getAnnotationCache().store(annotation.getId(), annotation);
			annotations.add(annotation);
		}

		return annotations;
	}

	/**
	 * @throws CytomineClientException
	 *           If the annotation cannot be retrieved from the host server.
	 */
	public Annotation getAnnotation(long annotationId) throws CytomineClientException {
		Annotation annotation;
		try {
			annotation = getAnnotationCache().retrieve(annotationId);
		} catch (EntityCacheException e) {
			annotation = downloadAnnotation(annotationId);
			getAnnotationCache().store(annotationId, annotation);
		}
		return annotation;
	}

	private Annotation downloadAnnotation(long annotationId) throws CytomineClientException {
		be.cytomine.client.models.Annotation annotation;
		try {
			annotation = getInternalClient().getAnnotation(annotationId);
			if (annotation.getAttr() == null) {
				throw new CytomineClientException("No annotation data downloaded");
			}
			return new Annotation(this, annotation);
		} catch (CytomineException e) {
			throw new CytomineClientException(
					String.format("Could not download annotation data (annotation id=%d)", annotationId), e);
		}
	}

	/**
	 * @throws CytomineClientException
	 *           If the annotation location cannot be retrieved from the host
	 *           server.
	 */
	public Optional<String> getAnnotationLocation(long annotationId) throws CytomineClientException {
		Annotation annotation = downloadAnnotation(annotationId);
		getAnnotationCache().store(annotationId, annotation);
		return annotation.getLocation();
	}

	public List<AnnotationTerm> downloadAnnotationTerms(long annotationId) throws CytomineClientException {
		TermCollection annotationTermCollection;
		try {
			annotationTermCollection = getInternalClient().getTermsByAnnotation(annotationId);
		} catch (CytomineException e) {
			throw new CytomineClientException(
					String.format("Could not download annotation terms (annotation id=%d)", annotationId), e);
		}

		List<AnnotationTerm> annotationTerms = new ArrayList<>(annotationTermCollection.size());
		for (int i = 0; i < annotationTermCollection.size(); i++) {
			be.cytomine.client.models.AnnotationTerm internalAnnotationTerm = new be.cytomine.client.models.AnnotationTerm();
			internalAnnotationTerm.setAttr((JSONObject) annotationTermCollection.getList().get(i));
			AnnotationTerm annotation = new AnnotationTerm(this, internalAnnotationTerm);
			annotationTerms.add(annotation);
		}

		return annotationTerms;
	}

	public BufferedImage downloadPictureAsBufferedImage(String url, String format) throws CytomineClientException {
		try {
			return getInternalClient().downloadPictureAsBufferedImage(url, format);
		} catch (CytomineException e) {
			throw new CytomineClientException(e.getMsg(), e);
		}
	}

	public Annotation addAnnotationWithTerms(long imageInstanceId, String geometry, List<Long> termIds)
			throws CytomineClientException {
		be.cytomine.client.models.Annotation internalAnnotation;
		try {
			internalAnnotation = getInternalClient().addAnnotationWithTerms(geometry, imageInstanceId, termIds);
		} catch (CytomineException e) {
			throw new CytomineClientException("Could not create annotation.", e);
		}
		Annotation annotation = new Annotation(this, internalAnnotation);
		getAnnotationCache().store(annotation.getId(), annotation);
		return annotation;
	}

	public void associateTerms(Entity annotation, Map<Term, Boolean> termSelection) throws CytomineClientException {
		try {
			for (Entry<Term, Boolean> entry : termSelection.entrySet()) {
				if (entry.getValue()) {
					getInternalClient().addAnnotationTerm(annotation.getId(), entry.getKey().getId());
				} else {
					getInternalClient().deleteAnnotationTerm(annotation.getId(), entry.getKey().getId());
				}
			}
		} catch (CytomineException e) {
			throw new CytomineClientException(
					String.format("Could not associate terms to annotation %d", annotation.getId().longValue()), e);
		}

	}

	@Override
	public String toString() {
		return String.format("Cytomine client: host=%s, public key=%s", String.valueOf(getHost()),
				String.valueOf(getPublicKey()));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result;
		if (internalClient != null) {
			result += getHost().hashCode();
			result = prime * result + getPublicKey().hashCode();
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CytomineClient)) {
			return false;
		}

		CytomineClient other = (CytomineClient) obj;
		return hashCode() == other.hashCode();
	}

	@Override
	public void close() throws Exception {
		closeCaches();
	}

	private void closeCaches() {
		UserCache.getCacheManager().removeCache(userCache.getCacheAlias());
		ProjectCache.getCacheManager().removeCache(projectCache.getCacheAlias());
		DescriptionCache.getCacheManager().removeCache(descriptionCache.getCacheAlias());
		OntologyCache.getCacheManager().removeCache(ontologyCache.getCacheAlias());
		TermCache.getCacheManager().removeCache(termCache.getCacheAlias());
		ImageInstanceCache.getCacheManager().removeCache(imageInstanceCache.getCacheAlias());
		AnnotationCache.getCacheManager().removeCache(annotationCache.getCacheAlias());
	}

}
