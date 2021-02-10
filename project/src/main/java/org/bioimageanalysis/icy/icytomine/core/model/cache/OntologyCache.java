package org.bioimageanalysis.icy.icytomine.core.model.cache;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.model.Ontology;

public class OntologyCache extends EntityCache<Long, Ontology> {

	public static OntologyCache create(CytomineClient client) {
		return new OntologyCache(client);
	}

	private OntologyCache(CytomineClient client) {
		super(client);
	}

	@Override
	protected Class<Long> getKeyClass() {
		return Long.class;
	}

	@Override
	protected Class<Ontology> getValueClass() {
		return Ontology.class;
	}

}
