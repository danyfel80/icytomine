package org.bioimageanalysis.icy.icytomine.core.model.filters;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.core.model.User;

import com.google.common.base.Objects;

public class AnnotationFilterByUser extends AnnotationFilter {
	public static class UserItem {
		public static final UserItem NO_USER = new UserItem(null);

		private User user;

		public UserItem(User user) {
			this.user = user;
		}

		public User getUser() {
			return user;
		}

		@Override
		public String toString() {
			if (user != null) {
				return user.getName().orElse("Not specified");
			} else {
				return "No user";
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((user == null) ? 0 : user.hashCode());
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
			if (!(obj instanceof UserItem)) {
				return false;
			}
			UserItem other = (UserItem) obj;
			if (user == null) {
				if (other.user != null) {
					return false;
				}
			} else if (!user.equals(other.user)) {
				return false;
			}
			return true;
		}
	}

	private Set<UserItem> activeUsers;

	public AnnotationFilterByUser() {
		super();
		activeUsers = new HashSet<>(0);
	}

	public void setActiveUsers(Set<UserItem> activeUsers) {
		Set<UserItem> previousUsers = this.activeUsers;
		this.activeUsers = activeUsers;
		if (!Objects.equal(previousUsers, this.activeUsers)) {
			computeActiveAnnotations(ComputationMode.RECOMPUTE_JUST_THIS);
		}
	}

	public Set<UserItem> getActiveUsers() {
		return activeUsers;
	}

	@Override
	protected Set<Annotation> applyFilter(Set<Annotation> inputAnnotations) {
		return inputAnnotations.parallelStream().filter(a -> isActive(a)).collect(Collectors.toSet());
	}

	public Boolean isActive(Annotation annotation) {
		User user = annotation.getUser();
		UserItem userItem = new UserItem(user);
		return activeUsers.contains(userItem);
	}

}
