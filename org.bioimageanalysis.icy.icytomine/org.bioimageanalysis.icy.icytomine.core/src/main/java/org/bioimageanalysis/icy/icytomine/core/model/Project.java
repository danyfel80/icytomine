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

	/**
	 * @return The project identifier
	 */
	public Long getId() {
		return internalProject.getId();
	}

	/**
	 * @return The project name
	 */
	public String getName() {
		return internalProject.getStr("name");
	}

	/**
	 * @return The project description
	 * @throws CytomineException
	 */
	public String getDescription() throws CytomineException {
		try {
			return cytomine.getDescription(internalProject.getId(), internalProject.getDomainName()).getStr("data");
		} catch (CytomineException e) {
			if (e.getHttpCode() == 500)
				return "N/A";
			else
				throw e;
		}
	}

	/**
	 * @return The ontology name used for this project
	 */
	public String getOntologyName() {
		return internalProject.getStr("ontologyName");
	}

	/**
	 * @return The amount of images available for this project
	 */
	public Long getNumberOfImages() {
		return internalProject.getLong("numberOfImages");
	}

	/**
	 * @return The amount of human user made annotations
	 */
	public Long getNumberOfAnnotations() {
		return internalProject.getLong("numberOfAnnotations");
	}

	/**
	 * @return The amount of annotations created by software
	 */
	public Long getNumberOfJobAnnotations() {
		return internalProject.getLong("numberOfJobAnnotations");
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
		UserCollection userCollection = cytomine.getProjectUsers(getId());
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
		result = prime * result + ((cytomine == null) ? 0 : cytomine.getHost().hashCode());
		result = prime * result + ((internalProject == null) ? 0 : getId().hashCode());
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
		if (cytomine == null) {
			if (other.cytomine != null) {
				return false;
			}
		} else if (!cytomine.getHost().equals(other.cytomine.getHost())) {
			return false;
		}
		if (internalProject == null) {
			if (other.internalProject != null) {
				return false;
			}
		} else if (!getId().equals(other.getId())) {
			return false;
		}
		return true;
	}

}
