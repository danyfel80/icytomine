package org.bioimageanalysis.icy.icytomine.core.model.cache;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.model.Term;

public class TermCache extends EntityCache<Long, Term> {

	public static TermCache create(CytomineClient client) {
		return new TermCache(client);
	}

	private TermCache(CytomineClient client) {
		super(client);
	}

	@Override
	protected Class<Long> getKeyClass() {
		return Long.class;
	}

	@Override
	protected Class<Term> getValueClass() {
		return Term.class;
	}

}
