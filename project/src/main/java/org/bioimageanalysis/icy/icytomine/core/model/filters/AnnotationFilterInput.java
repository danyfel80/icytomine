package org.bioimageanalysis.icy.icytomine.core.model.filters;

import java.util.HashSet;
import java.util.Set;

import org.bioimageanalysis.icy.icytomine.core.model.Annotation;

import com.google.common.base.Objects;

public class AnnotationFilterInput extends AnnotationFilter {

	public AnnotationFilterInput() {
		super();
		activeAnnotations = new HashSet<>(0);
	}

	public void setActiveAnnotations(Set<Annotation> activeAnnotations) {
		Set<Annotation> previousAnnotations = activeAnnotations;
		this.activeAnnotations = activeAnnotations;
		if (!Objects.equal(previousAnnotations, this.activeAnnotations)) {
			notifyUpdateListeners();
		}
	}

	@Override
	public void computeActiveAnnotations(ComputationMode mode) {
		notifyUpdateListeners();
	}

	@Override
	protected Set<Annotation> applyFilter(Set<Annotation> inputAnnotations) {
		return inputAnnotations;
	}
}
