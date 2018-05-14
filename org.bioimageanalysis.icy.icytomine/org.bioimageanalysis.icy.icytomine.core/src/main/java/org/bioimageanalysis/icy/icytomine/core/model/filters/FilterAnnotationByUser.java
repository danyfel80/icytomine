package org.bioimageanalysis.icy.icytomine.core.model.filters;

import java.util.Set;
import java.util.function.Function;

import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.core.model.User;

public class FilterAnnotationByUser implements Function<Annotation, Boolean> {

	private Set<User> activeUsers;

	public FilterAnnotationByUser(Set<User> activeUsers) {
		this.activeUsers = activeUsers;
	}

	@Override
	public Boolean apply(Annotation annotation) {
		User user = annotation.getUser();
		return activeUsers.contains(user);
	}

}
