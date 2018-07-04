/*
 * Copyright 2010-2018 Institut Pasteur.
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

import java.util.Optional;
import java.util.Set;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;

public class Ontology extends Entity {

	public static Ontology retrieve(CytomineClient client, long ontologyId) throws CytomineClientException {
		return client.getOntology(ontologyId);
	}

	private Set<Term> terms;

	public Ontology(CytomineClient client, be.cytomine.client.models.Ontology internalOntology) {
		super(client, internalOntology);
	}

	public be.cytomine.client.models.Ontology getInternalOntology() {
		return (be.cytomine.client.models.Ontology) getModel();
	}

	public Optional<String> getName() {
		return getStr("name");
	}

	public Set<Term> getTerms(boolean recompute) throws CytomineClientException {
		if (terms == null || recompute) {
			terms = null;
			terms = getClient().getOntologyTerms(getId());
		}
		return terms;
	}

	@Override
	public String toString() {
		return String.format("Ontology: id=%s, name=%s", String.valueOf(getId()), getName().orElse("Not specified"));
	}
}
