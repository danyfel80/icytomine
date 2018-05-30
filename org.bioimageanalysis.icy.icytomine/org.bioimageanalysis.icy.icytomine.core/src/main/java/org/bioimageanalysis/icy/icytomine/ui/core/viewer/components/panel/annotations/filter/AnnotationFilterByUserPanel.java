package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.filter;

import java.awt.event.ActionEvent;
import java.util.Set;

import org.bioimageanalysis.icy.icytomine.core.model.User;
import org.bioimageanalysis.icy.icytomine.core.model.filters.AnnotationFilter;
import org.bioimageanalysis.icy.icytomine.core.model.filters.AnnotationFilter.ComputationMode;
import org.bioimageanalysis.icy.icytomine.core.model.filters.AnnotationFilterByUser;
import org.bioimageanalysis.icy.icytomine.ui.general.JCheckableItem;
import org.bioimageanalysis.icy.icytomine.ui.general.JCheckedComboBox;

@SuppressWarnings("serial")
public class AnnotationFilterByUserPanel extends AnnotationFilterPanel<User> {

	AnnotationFilterByUser userFilter;

	public AnnotationFilterByUserPanel() {
		setLabelText("Users:");
		userFilter = new AnnotationFilterByUser();
		setFilter(userFilter);
	}

	public void setPreviousFilter(AnnotationFilter previousFilter) {
		userFilter.setPreviousFilter(previousFilter);
	}

	public void setUsers(Set<User> users) {
		userFilter.setActiveUsers(users);
		setModel(users.toArray(new User[0]), User::getUserName);
	}

	@Override
	protected void choiceChanged(ActionEvent e) {
		@SuppressWarnings("unchecked")
		JCheckableItem<User> item = ((JCheckableItem<User>) (((JCheckedComboBox<User>) (e.getSource())).getSelectedItem()));
		User user = item.object;
		if (!item.isSelected()) {
			userFilter.getActiveUsers().add(user);
		} else {
			userFilter.getActiveUsers().remove(user);
		}
		userFilter.computeActiveAnnotations(ComputationMode.RECOMPUTE_JUST_THIS);
	}
}
