package org.bioimageanalysis.icy.icytomine.core.model.filters;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.core.model.Term;

import be.cytomine.client.CytomineException;

public class FilterAnnotationByTerm implements Function<Annotation, Boolean> {
	private Set<Term> activeTerms;

	public FilterAnnotationByTerm(Set<Term> activeTerms) {
		this.activeTerms = activeTerms;
	}

	@Override
	public Boolean apply(Annotation annotation) {
		Set<Term> terms;
		try {
			terms = new HashSet<>(annotation.getTerms());
		} catch (CytomineException e) {
			e.printStackTrace();
			terms = new HashSet<>();
		}
		terms.retainAll(activeTerms);
		return !terms.isEmpty();
	}
}
