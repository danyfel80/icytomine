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
package org.bioimageanalysis.icy.icytomine.core.model;

import java.util.List;
import java.util.Optional;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;

public class User extends Entity {

	public static User retrieve(CytomineClient client, long userId) throws CytomineClientException {
		return client.getUser(userId);
	}

	private List<Project> projects;

	public User(CytomineClient client, be.cytomine.client.models.User internalUser) {
		super(client, internalUser);
	}

	public be.cytomine.client.models.User getInternalUser() {
		return (be.cytomine.client.models.User) getModel();
	}

	public Optional<String> getName() {
		return getStr("username");
	}

	public List<Project> getProjects(boolean recompute) {
		if (projects == null | recompute) {
			projects = null;
			projects = getClient().getUserProjects(getId());
		}
		return projects;
	}

	@Override
	public String toString() {
		return String.format("User: id=%s, name=%s", String.valueOf(getId()), getName().orElse("Not specified"));
	}

}
