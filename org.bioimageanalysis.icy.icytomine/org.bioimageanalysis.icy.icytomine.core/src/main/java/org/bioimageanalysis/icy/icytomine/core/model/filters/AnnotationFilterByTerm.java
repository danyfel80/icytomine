package org.bioimageanalysis.icy.icytomine.core.model.filters;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;
import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.core.model.Term;

import com.google.common.base.Objects;

public class AnnotationFilterByTerm extends AnnotationFilter {

	public static class TermItem {
		public static final TermItem NO_TERM = new TermItem(null);

		private Term term;

		public TermItem(Term term) {
			this.term = term;
		}

		public Term getTerm() {
			return term;
		}

		@Override
		public String toString() {
			if (term != null) {
				return term.getName().orElse("Not specified");
			} else {
				return "No term";
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((term == null) ? 0 : term.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof TermItem)) {
				return false;
			}
			TermItem other = (TermItem) obj;
			if (term == null) {
				if (other.term != null) {
					return false;
				}
			} else if (!term.equals(other.term)) {
				return false;
			}
			return true;
		}
	}

	private Set<TermItem> activeTerms;

	public AnnotationFilterByTerm() {
		super();
		activeTerms = new HashSet<>(0);
	}

	public void setActiveTerms(Set<TermItem> activeTerms) {
		Set<TermItem> previousTerms = this.activeTerms;
		this.activeTerms = activeTerms;
		if (!Objects.equal(previousTerms, this.activeTerms)) {
			computeActiveAnnotations(ComputationMode.RECOMPUTE_JUST_THIS);
		}
	}

	public Set<TermItem> getActiveTerms() {
		return activeTerms;
	}

	@Override
	protected Set<Annotation> applyFilter(Set<Annotation> inputAnnotations) {
		return inputAnnotations.parallelStream().filter(a -> isActive(a)).collect(Collectors.toSet());
	}

	public Boolean isActive(Annotation annotation) {
		Set<Term> terms;
		try {
			terms = annotation.getAssociatedTerms();
		} catch (CytomineClientException e) {
			e.printStackTrace();
			terms = new HashSet<>();
		}
		Set<TermItem> termItems = terms.stream().map(t -> new TermItem(t)).collect(Collectors.toSet());
		if (termItems.isEmpty())
			termItems.add(TermItem.NO_TERM);

		termItems.retainAll(getActiveTerms());
		return !termItems.isEmpty();
	}
}
