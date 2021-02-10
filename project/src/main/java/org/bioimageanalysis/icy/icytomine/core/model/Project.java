package org.bioimageanalysis.icy.icytomine.core.model;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;

public class Project extends Entity {

	/**
	 * @throws CytomineClientException
	 *           If the project cannot be retrieved from the host server.
	 */
	public static Project retrieve(CytomineClient client, long projectId) throws CytomineClientException {
		return client.getProject(projectId);
	}

	private List<User> users;
	private List<Image> images;

	public Project(CytomineClient client, be.cytomine.client.models.Project internalProject) {
		super(client, internalProject);
	}

	public be.cytomine.client.models.Project getInternalProject() {
		return (be.cytomine.client.models.Project) getModel();
	}

	public Optional<String> getName() {
		return getStr("name");
	}

	public Optional<Long> getOntologyId() {
		return super.getLong("ontology");
	}

	public Optional<String> getOntologyName() {
		return getStr("ontologyName");
	}

	public Optional<Long> getNumberOfImages() {
		return getLong("numberOfImages");
	}

	public Optional<Long> getNumberOfAnnotations() {
		return getLong("numberOfAnnotations");
	}

	public Optional<Long> getNumberOfJobAnnotations() {
		return getLong("numberOfJobAnnotations");
	}

	public Long getTotalNumberOfAnnotations() {
		return getNumberOfAnnotations().orElse(0L) + getNumberOfJobAnnotations().orElse(0L);
	}

	/**
	 * @throws CytomineClientException
	 *           If the project users cannot be retrieved from the host server.
	 */
	public List<User> getUsers(boolean recompute) throws CytomineClientException {
		if (users == null || recompute) {
			users = null;
			users = getClient().getProjectUsers(getId());
		}
		return users;
	}

	/**
	 * @throws NoSuchElementException
	 *           If no ontology is specified for this project.
	 * @throws CytomineClientException
	 *           If the project ontology cannot be retrieved from the host server.
	 */
	public Ontology getOntology() throws NoSuchElementException, CytomineClientException {
		Long ontologyId = getOntologyId().get();
		return Ontology.retrieve(getClient(), ontologyId);
	}

	/**
	 * @throws CytomineClientException
	 *           If the project images cannot be retrieved from the host server.
	 */
	public List<Image> getImages(boolean recompute) throws CytomineClientException {
		if (images == null || recompute) {
			images = null;
			images = getClient().getProjectImages(getId());
		}
		return images;
	}

	/**
	 * @throws CytomineClientException
	 *           If the project image cannot be retrieved from the host server.
	 */
	public Image getImageInstance(long imageInstanceId) throws CytomineClientException {
		return getClient().getImageInstance(imageInstanceId);
	}

	@Override
	public String toString() {
		return String.format("Project: id=%s, name=%s", String.valueOf(getId()), getName().orElse("Not specified"));
	}

}
