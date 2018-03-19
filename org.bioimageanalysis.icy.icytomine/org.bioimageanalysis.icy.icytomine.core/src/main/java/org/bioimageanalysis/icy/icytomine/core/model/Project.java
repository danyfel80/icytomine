package org.bioimageanalysis.icy.icytomine.core.model;

import java.util.ArrayList;
import java.util.List;

import be.cytomine.client.Cytomine;
import be.cytomine.client.CytomineException;
import be.cytomine.client.collections.UserCollection;

/**
 * This class represents a project in cytomine. It only contains data about its
 * identifier and its name.
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
/**
 * @author Daniel Felipe Gonzalez Obando
 *
 */
/**
 * @author Daniel Felipe Gonzalez Obando
 *
 */
public class Project {

	private Cytomine cytomine;
	private be.cytomine.client.models.Project internalProject;

	public Project(be.cytomine.client.models.Project internalProject, Cytomine cytomine) {
		this.internalProject = internalProject;
		this.cytomine = cytomine;
	}

	public Cytomine getClient() {
		return this.cytomine;
	}

	public be.cytomine.client.models.Project getInternalProject() {
		return internalProject;
	}

	/**
	 * @return The project identifier
	 */
	public Long getId() {
		return getInternalProject().getId();
	}

	/**
	 * @return The project name
	 */
	public String getName() {
		return getInternalProject().getStr("name");
	}

	/**
	 * @return The project description
	 * @throws CytomineException
	 */
	public String getDescription() throws CytomineException {
		try {
			return getClient().getDescription(getInternalProject().getId(), getInternalProject().getDomainName())
					.getStr("data");
		} catch (CytomineException e) {
			if (e.getHttpCode() == 500)
				return "N/A";
			else
				throw e;
		}
	}

	/**
	 * @return The id of the ontology used for this project.
	 */
	public Long getOntologyId() {
		return getInternalProject().getLong("ontology");
	}

	/**
	 * @return The ontology name used for this project
	 */
	public String getOntologyName() {
		return getInternalProject().getStr("ontologyName");
	}

	/**
	 * @return The amount of images available for this project
	 */
	public Long getNumberOfImages() {
		return getInternalProject().getLong("numberOfImages");
	}

	/**
	 * @return The amount of human user made annotations
	 */
	public Long getNumberOfAnnotations() {
		return getInternalProject().getLong("numberOfAnnotations");
	}

	/**
	 * @return The amount of annotations created by software
	 */
	public Long getNumberOfJobAnnotations() {
		return getInternalProject().getLong("numberOfJobAnnotations");
	}

	/**
	 * @return The sum of user made annotations and job annotations.
	 */
	public Long getTotalNumberOfAnnotations() {
		return getNumberOfAnnotations() + getNumberOfJobAnnotations();
	}

	/**
	 * @return The users associated to this project.
	 * @throws CytomineException
	 *           if the users cannot be retrieved from the server.
	 */
	public List<User> getUsers() throws CytomineException {
		UserCollection userCollection = getClient().getProjectUsers(getId());
		List<User> users = new ArrayList<>(userCollection.size());
		for (int i = 0; i < userCollection.size(); i++) {
			users.add(new User(userCollection.get(i)));
		}

		return users;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName();
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
		result = prime * result + ((getClient() == null) ? 0 : getClient().getHost().hashCode());
		result = prime * result + ((getInternalProject() == null) ? 0 : getId().hashCode());
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
		if (!(obj instanceof Project)) {
			return false;
		}
		Project other = (Project) obj;
		if (getClient() == null) {
			if (other.getClient() != null) {
				return false;
			}
		} else if (!getClient().getHost().equals(other.getClient().getHost())) {
			return false;
		}
		if (getInternalProject() == null) {
			if (other.getInternalProject() != null) {
				return false;
			}
		} else if (!getId().equals(other.getId())) {
			return false;
		}
		return true;
	}

}
