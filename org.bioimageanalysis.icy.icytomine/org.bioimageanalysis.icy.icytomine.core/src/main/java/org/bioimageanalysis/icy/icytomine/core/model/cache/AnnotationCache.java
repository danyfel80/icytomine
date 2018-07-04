package org.bioimageanalysis.icy.icytomine.core.model.cache;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.model.Annotation;

public class AnnotationCache extends EntityCache<Long, Annotation> {

	public static AnnotationCache create(CytomineClient client) {
		return new AnnotationCache(client);
	}

	private AnnotationCache(CytomineClient client) {
		super(client);
	}

	@Override
	protected Class<Long> getKeyClass() {
		return Long.class;
	}

	@Override
	protected Class<Annotation> getValueClass() {
		return Annotation.class;
	}

}
