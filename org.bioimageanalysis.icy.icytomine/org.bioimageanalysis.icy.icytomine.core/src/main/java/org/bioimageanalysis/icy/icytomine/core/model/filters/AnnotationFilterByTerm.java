package org.bioimageanalysis.icy.icytomine.core.model.filters;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.core.model.Term;

import com.google.common.base.Objects;

import be.cytomine.client.CytomineException;

public class AnnotationFilterByTerm extends AnnotationFilter {

	private Set<Term> activeTerms;

	public AnnotationFilterByTerm() {
		super();
		activeTerms = new HashSet<>(0);
	}

	public void setActiveTerms(Set<Term> activeTerms) {
		Set<Term> previousTerms = this.activeTerms;
		this.activeTerms = activeTerms;
		if (!Objects.equal(previousTerms, this.activeTerms)) {
			computeActiveAnnotations(ComputationMode.RECOMPUTE_JUST_THIS);
		}
	}

	public Set<Term> getActiveTerms() {
		return activeTerms;
	}

	@Override
	protected Set<Annotation> applyFilter(Set<Annotation> inputAnnotations) {
		return inputAnnotations.parallelStream().filter(a -> isActive(a)).collect(Collectors.toSet());
	}

	public Boolean isActive(Annotation annotation) {
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
