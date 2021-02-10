package org.bioimageanalysis.icy.icytomine.core.model.cache;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.model.Project;

public class ProjectCache extends EntityCache<Long, Project> {

	public static ProjectCache create(CytomineClient client) {
		return new ProjectCache(client);
	}

	private ProjectCache(CytomineClient client) {
		super(client);
	}

	@Override
	protected Class<Long> getKeyClass() {
		return Long.class;
	}

	@Override
	protected Class<Project> getValueClass() {
		return Project.class;
	}

}
