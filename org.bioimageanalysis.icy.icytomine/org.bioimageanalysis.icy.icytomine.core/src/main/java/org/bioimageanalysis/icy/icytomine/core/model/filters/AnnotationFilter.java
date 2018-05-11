package org.bioimageanalysis.icy.icytomine.core.model.filters;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.core.model.Term;
import org.bioimageanalysis.icy.icytomine.core.model.User;

import be.cytomine.client.CytomineException;

public abstract class AnnotationFilter {

	public static List<Annotation> byUsers(List<Annotation> annotations, Set<User> users) {
		return annotations.stream().filter(a -> users.contains(a.getUser())).collect(Collectors.toList());
	}

	public static List<Annotation> byTerms(List<Annotation> annotations, Set<Term> terms) {
		return annotations.stream().filter(a -> existTermIntersection(a, terms)).collect(Collectors.toList());
	}

	private static boolean existTermIntersection(Annotation a, Set<Term> terms) {
		try {
			return !Collections.disjoint(a.getTerms(), terms);
		} catch (CytomineException e) {
			e.printStackTrace();
			return false;
		}
	}

	public Set<Annotation> apply(Set<Annotation> annotations) {
		return annotations;
	}

}
