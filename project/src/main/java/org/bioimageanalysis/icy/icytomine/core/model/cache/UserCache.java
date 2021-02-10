package org.bioimageanalysis.icy.icytomine.core.model.cache;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.model.User;

public class UserCache extends EntityCache<Long, User> {

	public static UserCache create(CytomineClient client) {
		return new UserCache(client);
	}

	private UserCache(CytomineClient client) {
		super(client);
	}

	@Override
	protected Class<Long> getKeyClass() {
		return Long.class;
	}

	@Override
	protected Class<User> getValueClass() {
		return User.class;
	}

}
