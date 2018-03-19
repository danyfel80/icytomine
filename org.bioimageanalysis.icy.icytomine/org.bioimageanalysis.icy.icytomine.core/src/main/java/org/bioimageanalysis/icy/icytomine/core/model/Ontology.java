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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import be.cytomine.client.Cytomine;
import be.cytomine.client.CytomineException;
import be.cytomine.client.collections.TermCollection;

/**
 * @author Daniel Felipe Gonzalez Obando
 *
 */
public class Ontology {
	private Cytomine client;
	private be.cytomine.client.models.Ontology internalOntology;

	// Cached terms
	private TermCollection nativeTerms;

	public Ontology(be.cytomine.client.models.Ontology internalOntology, Cytomine client) {
		this.client = client;
		this.internalOntology = internalOntology;
	}

	public Cytomine getClient() {
		return client;
	}

	public be.cytomine.client.models.Ontology getInternalOntology() {
		return internalOntology;
	}

	public Long getId() {
		return getInternalOntology().getId();
	}

	public String getName() {
		return getInternalOntology().getStr("name");
	}

	public static List<Term> getTerms(Cytomine client, Long ontologyId) throws CytomineException {
		TermCollection nativeTerms = client.getTermsByOntology(ontologyId);

		return IntStream.range(0, nativeTerms.size()).mapToObj(i -> nativeTerms.get(i)).map(t -> new Term(client, t))
				.collect(Collectors.toList());
	}

	public List<Term> getTerms() throws CytomineException {
		if (this.nativeTerms == null) {
			this.nativeTerms = getClient().getTermsByOntology(getId());
		}
		return IntStream.range(0, this.nativeTerms.size()).mapToObj(i -> this.nativeTerms.get(i))
				.map(t -> new Term(getClient(), t)).collect(Collectors.toList());
	}
}
