package org.bioimageanalysis.icy.icytomine.ui.core.login;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.bioimageanalysis.icy.icytomine.core.connection.CytomineConnector;
import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.connection.persistence.Preferences;
import org.bioimageanalysis.icy.icytomine.core.connection.persistence.UserCredential;

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
		panel.addAddHostButtonListener(getHostAdditionEventHandler());
		panel.addRemoveHostButtonListener(getHostRemovalEventHandler());
		panel.addEditHostButtonListener(getHostEditionEventHandler());
		panel.addAddUserButtonListener(getUserAdditionEventHandler());
		panel.addRemoveUserButtonListener(getRemoveUserEventHandler());
		panel.addEditUserButtonListener(getUserEditionEventHandler());
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

	private ActionListener getHostAdditionEventHandler() {
		return (ActionEvent event) -> {
			AddHostDialog dialog = new AddHostDialog(panel.getParentFrame());
			dialog.setHostAdditionListener((hostName) -> {
				if (hostName == null || hostName.isEmpty()) {
					throw new RuntimeException("No host was specified. Try again.");
				}
				try {
					CytomineConnector.addHostIfAbsent(new URL(hostName));
				} catch (MalformedURLException e) {
					throw new RuntimeException("Host URL (" + Objects.toString(hostName) + ") is not valid. Try again.", e);
				}
				try {
					Preferences.save();
				} catch (IOException e) {
					throw new RuntimeException("Could not save the host to preferences file.", e);
				}
			});
			dialog.setVisible(true);
			updateCredentials();
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

	private ActionListener getHostRemovalEventHandler() {
		return (ActionEvent event) -> {
			Optional<String> selectedHost = panel.getSelectedHost();
			if (!selectedHost.isPresent()) {
				MessageDialog.showDialog("Login error - Remove host", "Please select a host", MessageDialog.ERROR_MESSAGE);
				return;
			}

			if (!ConfirmDialog.confirm("Remove Host Locally - Icytomine",
					"Are you sure to locally remove host " + selectedHost.get(), ConfirmDialog.YES_NO_OPTION)) {
				return;
			}

			try {
				CytomineConnector.removeHost(new URL(selectedHost.get()));
				Preferences.save();
			} catch (IllegalArgumentException | IOException e) {
				MessageDialog.showDialog("Login error - Remove user", e.getMessage(), MessageDialog.ERROR_MESSAGE);
			}

			updateCredentials();
		};
	}

	private ActionListener getHostEditionEventHandler() {
		return (ActionEvent event) -> {
			Optional<String> selectedHost = panel.getSelectedHost();
			if (!selectedHost.isPresent()) {
				MessageDialog.showDialog("Login error - Edit user", "Please select a host", MessageDialog.ERROR_MESSAGE);
				return;
			}

			EditHostDialog dialog = new EditHostDialog(panel.getParentFrame(), selectedHost.get());
			dialog.setHostEditionListener((oldHostName, newHostName) -> {
				if (newHostName == null || newHostName.isEmpty()) {
					throw new RuntimeException("No host was specified. Try again.");
				}
				try {
					CytomineConnector.updateHost(new URL(oldHostName), new URL(newHostName));
				} catch (MalformedURLException e) {
					throw new RuntimeException("Host URL (" + Objects.toString(newHostName) + ") is not valid. Try again.", e);
				}
				try {
					Preferences.save();
				} catch (IOException e) {
					throw new RuntimeException("Could not save the host to preferences file.", e);
				}
			});
			dialog.setVisible(true);
			updateCredentials();
		};
	}

	private ActionListener getUserAdditionEventHandler() {
		return (ActionEvent event) -> {
			Optional<String> selectedHost = panel.getSelectedHost();
			if (!selectedHost.isPresent()) {
				MessageDialog.showDialog("Login error - Add user", "Please select a host", MessageDialog.ERROR_MESSAGE);
				return;
			}

			AddUserDialog dialog = new AddUserDialog(panel.getParentFrame(), selectedHost.get());
			dialog.setUserAdditionListener((String hostName, String userName, String publicKey, String privateKey) -> {
				if (hostName == null || hostName.isEmpty()) {
					throw new RuntimeException("The host is empty. Try again");
				}
				if (userName == null || userName.isEmpty()) {
					throw new RuntimeException("The user name is empty. Try again");
				}
				if (publicKey == null || publicKey.isEmpty()) {
					throw new RuntimeException("The public key is empty. Try again");
				}
				if (privateKey == null || privateKey.isEmpty()) {
					throw new RuntimeException("The private key is empty. Try again");
				}

				try {
					CytomineConnector.addUser(new URL(hostName), userName, publicKey, privateKey);
				} catch (MalformedURLException ex) {
					throw new RuntimeException("Host URL (" + Objects.toString(hostName) + ") is not valid. Try again.", ex);
				}

				try {
					Preferences.save();
				} catch (IOException ex) {
					throw new RuntimeException("Could not save the user to preferences file.", ex);
				}
			});
			dialog.setVisible(true);
			updateCredentials();
		};
	}

	private ActionListener getRemoveUserEventHandler() {
		return (ActionEvent event) -> {
			Optional<String> selectedHost = panel.getSelectedHost();
			Optional<String> selectedUser = panel.getSelectedUser();
			if (!selectedHost.isPresent() || !selectedUser.isPresent()) {
				MessageDialog.showDialog("Login error - Remove user", "Please select a host and a user",
						MessageDialog.ERROR_MESSAGE);
				return;
			}

			if (!ConfirmDialog.confirm("Remove User Locally - Icytomine",
					"Are you sure to locally remove user " + selectedUser.get(), ConfirmDialog.YES_NO_OPTION)) {
				return;
			}

			try {
				CytomineConnector.removeUser(new URL(selectedHost.get()), selectedUser.get());
				Preferences.save();
			} catch (IllegalArgumentException | IOException e) {
				MessageDialog.showDialog("Login error - Remove user", e.getMessage(), MessageDialog.ERROR_MESSAGE);
			}

			updateCredentials();
		};
	}

	private ActionListener getUserEditionEventHandler() {
		return (ActionEvent event) -> {
			Optional<String> selectedHost = panel.getSelectedHost();
			Optional<String> selectedUser = panel.getSelectedUser();
			if (!selectedHost.isPresent() || !selectedUser.isPresent()) {
				MessageDialog.showDialog("Login error - Edit user", "Please select a host and a user",
						MessageDialog.ERROR_MESSAGE);
				return;
			}

			UserCredential userCredentials = Preferences.getInstance().getAvailableCytomineCredentials()
					.get(selectedHost.get()).get(selectedUser.get());

			EditUserDialog dialog = new EditUserDialog(panel.getParentFrame(), selectedHost.get(), selectedUser.get(),
					userCredentials);
			dialog.setUserUpdateListener(
					(String hostName, String oldUserName, String userName, String publicKey, String privateKey) -> {
						if (userName == null || userName.isEmpty()) {
							throw new RuntimeException("The user name is empty. Try again");
						}
						if (publicKey == null || publicKey.isEmpty()) {
							throw new RuntimeException("The public key is empty. Try again");
						}
						if (privateKey == null || privateKey.isEmpty()) {
							throw new RuntimeException("The private key is empty. Try again");
						}

						try {
							CytomineConnector.updateUser(new URL(hostName), oldUserName, new URL(hostName), userName, publicKey,
									privateKey);
						} catch (MalformedURLException ex) {
							throw new RuntimeException("Host URL (" + Objects.toString(hostName) + ") is not valid. Try again.", ex);
						}

						try {
							Preferences.save();
						} catch (IOException ex) {
							throw new RuntimeException("Could not save the user to preferences file.", ex);
						}
					});
			dialog.setVisible(true);
			updateCredentials();
		};
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

	public void addLoginListener(LoginListener listener) {
		loginListeners.add(listener);
	}

	public void removeLoginListener(LoginListener listener) {
		loginListeners.remove(listener);
	}

}
