package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.filter;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bioimageanalysis.icy.icytomine.core.model.User;
import org.bioimageanalysis.icy.icytomine.ui.general.JCheckableItem;
import org.bioimageanalysis.icy.icytomine.ui.general.JCheckedComboBox;

@SuppressWarnings("serial")
public class UserFilterPanel extends FilterPanel<User> {

	public interface UserSelectionListener {
		void userSelectionChange(User user, boolean selected);
	}

	private Set<UserSelectionListener> userSelectionListeners;

	public UserFilterPanel(List<User> users) {
		super("Users:");
		super.setModel(users.toArray(new User[0]), User::getUserName);
		userSelectionListeners = new HashSet<>();
	}

	@Override
	protected void choiceChanged(ActionEvent e) {
		@SuppressWarnings("unchecked")
		JCheckableItem<User> item = ((JCheckableItem<User>) (((JCheckedComboBox<User>) (e.getSource())).getSelectedItem()));
		notifyUserSelectionChange(item.getObject(), item.isSelected());
	}

	private void notifyUserSelectionChange(User user, boolean selected) {
		userSelectionListeners.forEach(l -> l.userSelectionChange(user, selected));
	}

	public void addUserSelectionListener(UserSelectionListener listener) {
		this.userSelectionListeners.add(listener);
	}

	public void removeUserSelectionListener(UserSelectionListener listener) {
		this.userSelectionListeners.remove(listener);
	}
}
