package org.bioimageanalysis.icy.icytomine.core.model.cache;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.model.Description;
import org.bioimageanalysis.icy.icytomine.core.model.key.DescriptionId;

public class DescriptionCache extends EntityCache<DescriptionId, Description> {

	public static DescriptionCache create(CytomineClient client) {
		return new DescriptionCache(client);
	}

	private DescriptionCache(CytomineClient client) {
		super(client);
	}

	@Override
	protected Class<DescriptionId> getKeyClass() {
		return DescriptionId.class;
	}

	@Override
	protected Class<Description> getValueClass() {
		return Description.class;
	}

}
