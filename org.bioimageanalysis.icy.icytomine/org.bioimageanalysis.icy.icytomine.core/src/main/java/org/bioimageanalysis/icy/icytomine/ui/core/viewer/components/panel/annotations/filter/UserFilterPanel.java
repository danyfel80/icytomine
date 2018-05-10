package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.filter;

import java.util.List;

import org.bioimageanalysis.icy.icytomine.core.model.User;

@SuppressWarnings("serial")
public class UserFilterPanel extends FilterPanel<User> {

	public UserFilterPanel(List<User> users) {
		super("Users:");
		super.setModel(users.toArray(new User[0]), User::getUserName);
	}

}
