package org.bioimageanalysis.icy.icytomine.core.model.cache;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.model.Image;

public class ImageInstanceCache extends EntityCache<Long, Image> {

	public static ImageInstanceCache create(CytomineClient client) {
		return new ImageInstanceCache(client);
	}

	private ImageInstanceCache(CytomineClient client) {
		super(client);
	}

	@Override
	protected Class<Long> getKeyClass() {
		return Long.class;
	}

	@Override
	protected Class<Image> getValueClass() {
		return Image.class;
	}

}
