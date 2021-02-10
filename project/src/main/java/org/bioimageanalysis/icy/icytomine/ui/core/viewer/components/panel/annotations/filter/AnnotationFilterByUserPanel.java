package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.filter;

import java.awt.event.ActionEvent;
import java.util.Set;
import java.util.stream.Collectors;

import org.bioimageanalysis.icy.icytomine.core.model.User;
import org.bioimageanalysis.icy.icytomine.core.model.filters.AnnotationFilter;
import org.bioimageanalysis.icy.icytomine.core.model.filters.AnnotationFilter.ComputationMode;
import org.bioimageanalysis.icy.icytomine.core.model.filters.AnnotationFilterByUser;
import org.bioimageanalysis.icy.icytomine.core.model.filters.AnnotationFilterByUser.UserItem;
import org.bioimageanalysis.icy.icytomine.ui.general.JCheckableItem;
import org.bioimageanalysis.icy.icytomine.ui.general.JCheckedComboBox;

@SuppressWarnings("serial")
public class AnnotationFilterByUserPanel extends AnnotationFilterPanel<UserItem> {

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
		Set<UserItem> userItems = users.stream().map(u -> new UserItem(u)).collect(Collectors.toSet());
		userItems.add(UserItem.NO_USER);

		userFilter.setActiveUsers(userItems);
		setModel(userItems.toArray(new UserItem[userItems.size()]), u->u.toString());
	}

	@Override
	protected void choiceChanged(ActionEvent e) {
		@SuppressWarnings("unchecked")
		JCheckableItem<UserItem> checkableItem = ((JCheckableItem<UserItem>) (((JCheckedComboBox<UserItem>) (e.getSource())).getSelectedItem()));
		UserItem userItem = checkableItem.object;
		if (!checkableItem.isSelected()) {
			userFilter.getActiveUsers().add(userItem);
		} else {
			userFilter.getActiveUsers().remove(userItem);
		}
		userFilter.computeActiveAnnotations(ComputationMode.RECOMPUTE_JUST_THIS);
	}
}
