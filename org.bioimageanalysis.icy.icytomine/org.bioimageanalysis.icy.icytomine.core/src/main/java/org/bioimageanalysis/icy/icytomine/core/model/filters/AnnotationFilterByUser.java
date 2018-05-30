package org.bioimageanalysis.icy.icytomine.core.model.filters;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.core.model.User;

import com.google.common.base.Objects;

public class AnnotationFilterByUser extends AnnotationFilter {

	private Set<User> activeUsers;

	public AnnotationFilterByUser() {
		super();
		activeUsers = new HashSet<>(0);
	}

	public void setActiveUsers(Set<User> activeUsers) {
		Set<User> previousUsers = this.activeUsers;
		this.activeUsers = activeUsers;
		if (!Objects.equal(previousUsers, this.activeUsers)) {
			computeActiveAnnotations(ComputationMode.RECOMPUTE_JUST_THIS);
		}
	}

	public Set<User> getActiveUsers() {
		return activeUsers;
	}

	@Override
	protected Set<Annotation> applyFilter(Set<Annotation> inputAnnotations) {
		return inputAnnotations.parallelStream().filter(a -> isActive(a)).collect(Collectors.toSet());
	}

	public Boolean isActive(Annotation annotation) {
		User user = annotation.getUser();
		return activeUsers.contains(user);
	}

}
