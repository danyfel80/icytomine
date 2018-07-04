package org.bioimageanalysis.icy.icytomine.ui.core.login;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.bioimageanalysis.icy.icytomine.core.connection.CytomineConnector;
import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.connection.persistence.Preferences;

import icy.gui.dialog.ConfirmDialog;
import icy.gui.dialog.MessageDialog;

public class LoginPanelController {

	@FunctionalInterface
	public interface LoginListener {
		void logged(CytomineClient cytomine);
	}

	private LoginPanel panel;
	private Set<LoginListener> loginListeners;

	private Preferences preferences;

	public LoginPanelController(LoginPanel panel) {
		this.panel = panel;
		this.loginListeners = new HashSet<>();
		this.preferences = Preferences.getInstance();
		setEventHandlers();
	}

	private void setEventHandlers() {
		panel.addHostServerSelectionListener(getHostServerSelectionEventHandler());
		panel.addAddUserButtonListener(getUserAdditionEventHandler());
		panel.addEditUserButtonListener(getUserEditionEventHandler());
		panel.addRemoveUserButtonListener(getRemoveUserEventHandler());
		panel.addLoginButtonListener(getLoginButtonEventHandler());
	}

	private ItemListener getHostServerSelectionEventHandler() {
		return e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				String host = (String) e.getItem();
				setUsersForHost(host);
			}
		};
	}

	private void setUsersForHost(String host) {
		Set<String> users = preferences.getAvailableCytomineCredentials().getOrDefault(host, new HashMap<>()).keySet();
		Optional<String> userSelection = preferences.getDefaultUserName();
		if (userSelection.isPresent()) {
			panel.setUsers(users, userSelection);
		} else {
			panel.setUsers(users, Optional.ofNullable(null));
		}
	}

	private MouseListener getLoginButtonEventHandler() {
		return new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				Optional<String> selectedHost = panel.getSelectedHost();
				Optional<String> selectedUser = panel.getSelectedUser();
				if (!selectedHost.isPresent() || !selectedUser.isPresent()) {
					MessageDialog.showDialog("Login error", "Please select a host and a user", MessageDialog.ERROR_MESSAGE);
					return;
				}

				CytomineClient client;
				try {
					client = login(selectedHost.get(), selectedUser.get());
				} catch (IllegalArgumentException | ExecutionException | InterruptedException e) {
					e.printStackTrace();
					MessageDialog.showDialog("Login error", e.getMessage(), MessageDialog.ERROR_MESSAGE);
					return;
				}
				
				try {
					setDefaultCredentials(selectedHost.get(), selectedUser.get());
				} catch (IOException e) {
					e.printStackTrace();
					MessageDialog.showDialog("Credentials update error", e.getMessage(), MessageDialog.ERROR_MESSAGE);
				}
				
				notifyLoginListeners(client);
			}
		};
	}

	private CytomineClient login(String host, String userName)
			throws IllegalArgumentException, InterruptedException, ExecutionException {
		Future<CytomineClient> connectionResult;
		try {
			connectionResult = CytomineConnector.login(new URL(host), userName);
		} catch (IllegalArgumentException | MalformedURLException e) {
			throw new IllegalArgumentException(String.format("Invalid host: %s", host), e);
		}
		return connectionResult.get();
	}

	private void setDefaultCredentials(String host, String userName) throws IOException {
		preferences.setDefaultHost(host);
		preferences.setDefaultUser(userName);
		Preferences.save();
	}

	private void notifyLoginListeners(CytomineClient client) {
		loginListeners.forEach(l -> l.logged(client));
	}

	private MouseListener getUserAdditionEventHandler() {
		return new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				try {
					new AddUserDialog(panel.getParentFrame());
				} catch (Exception e) {
					MessageDialog.showDialog("Login error - Add user", e.getMessage(), MessageDialog.ERROR_MESSAGE);
					return;
				}
				updateCredentials();
			}
		};
	}

	void updateCredentials() {

		Set<String> hosts = preferences.getAvailableCytomineCredentials().keySet();
		Optional<String> selection = preferences.getDefaultHostURL();
		if (selection.isPresent()) {
			panel.setHosts(hosts, selection);
		} else {
			panel.setHosts(hosts, Optional.ofNullable(null));
		}
	}

	private MouseListener getUserEditionEventHandler() {
		return new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {

				Optional<String> selectedHost = panel.getSelectedHost();
				Optional<String> selectedUser = panel.getSelectedUser();
				if (!selectedHost.isPresent() || !selectedUser.isPresent()) {
					MessageDialog.showDialog("Login error - Edit user", "Please select a host and a user",
							MessageDialog.ERROR_MESSAGE);
					return;
				}

				try {
					new EditUserDialog(selectedHost.get(), selectedUser.get(), panel.getParentFrame());
				} catch (Exception e) {
					MessageDialog.showDialog("Login error - Edit user", e.getMessage(), MessageDialog.ERROR_MESSAGE);
					return;
				}

				updateCredentials();

			}
		};
	}

	private MouseListener getRemoveUserEventHandler() {
		return new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				Optional<String> selectedHost = panel.getSelectedHost();
				Optional<String> selectedUser = panel.getSelectedUser();
				if (!selectedHost.isPresent() || !selectedUser.isPresent()) {
					MessageDialog.showDialog("Login error - Remove user", "Please select a host and a user",
							MessageDialog.ERROR_MESSAGE);
					return;
				}

				if (!ConfirmDialog.confirm("Remove User Locally - Icytomine",
						"Are you sure to locally remove user " + selectedUser.get(), ConfirmDialog.YES_NO_OPTION))
					return;

				try {
					CytomineConnector.removeUser(new URL(selectedHost.get()), selectedUser.get());
					Preferences.save();
				} catch (IllegalArgumentException | IOException e) {
					MessageDialog.showDialog("Login error - Remove user", e.getMessage(), MessageDialog.ERROR_MESSAGE);
				}

				updateCredentials();
			}
		};
	}

	public void addLoginListener(LoginListener listener) {
		loginListeners.add(listener);
	}

	public void removeLoginListener(LoginListener listener) {
		loginListeners.remove(listener);
	}

}
