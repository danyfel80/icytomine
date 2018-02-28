package org.bioimageanalysis.icy.icytomine.ui.core.login;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.bioimageanalysis.icy.icytomine.core.connection.CytomineConnector;
import org.bioimageanalysis.icy.icytomine.core.connection.user.Preferences;

import be.cytomine.client.Cytomine;
import icy.gui.dialog.ConfirmDialog;
import icy.gui.dialog.MessageDialog;

public class LoginPanel extends JPanel {

	@FunctionalInterface
	public interface LoginListener {
		void logged(Cytomine cytomine);
	}

	private static final long serialVersionUID = 885665565700074663L;

	private JComboBox<String>	cBoxHost;
	private JComboBox<String>	cBoxUser;
	private JButton						btnAddUser;
	private JButton						btnRemoveUser;
	private JButton						btnLogin;

	private LoginListener	onLoggedIn;
	private final JPanel	panel	= new JPanel();
	private JButton				btnEditUser;

	/**
	 * Listener that handles login event.
	 * 
	 * @param onLoggedIn
	 *          Method called when connection to cytomine is succesfull.
	 */
	public void setLoginListener(LoginListener onLoggedIn) {
		this.onLoggedIn = onLoggedIn;
	}

	/**
	 * Create the panel.
	 * 
	 * @param parent
	 */
	public LoginPanel(Frame parent) {
		this.setPreferredSize(new Dimension(320, 150));
		this.setMinimumSize(new Dimension(320, 150));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 40, 100, 110 };
		gridBagLayout.rowHeights = new int[] { 30, 30, 30, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0 };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0 };
		setLayout(gridBagLayout);

		JLabel lblHost = new JLabel("Host");
		lblHost.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblHost = new GridBagConstraints();
		gbc_lblHost.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblHost.insets = new Insets(0, 0, 10, 5);
		gbc_lblHost.gridx = 0;
		gbc_lblHost.gridy = 0;
		add(lblHost, gbc_lblHost);

		cBoxHost = new JComboBox<>();
		cBoxHost.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				String host = (String) e.getItem();
				cBoxUser.removeAllItems();
				Preferences.getInstance().getAvailableCytomineCredentials().get(host).keySet()
						.forEach(user -> cBoxUser.addItem(user));
				cBoxUser.setSelectedIndex(0);
			}
		});
		lblHost.setLabelFor(cBoxHost);
		GridBagConstraints gbc_cBoxHost = new GridBagConstraints();
		gbc_cBoxHost.insets = new Insets(0, 0, 10, 0);
		gbc_cBoxHost.fill = GridBagConstraints.BOTH;
		gbc_cBoxHost.gridwidth = 2;
		gbc_cBoxHost.gridx = 1;
		gbc_cBoxHost.gridy = 0;
		add(cBoxHost, gbc_cBoxHost);

		JLabel lblUser = new JLabel("User");
		lblUser.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblUser = new GridBagConstraints();
		gbc_lblUser.anchor = GridBagConstraints.EAST;
		gbc_lblUser.insets = new Insets(0, 0, 10, 5);
		gbc_lblUser.gridx = 0;
		gbc_lblUser.gridy = 1;
		add(lblUser, gbc_lblUser);

		cBoxUser = new JComboBox<>();
		GridBagConstraints gbc_cBoxUser = new GridBagConstraints();
		gbc_cBoxUser.gridwidth = 2;
		gbc_cBoxUser.insets = new Insets(0, 0, 10, 0);
		gbc_cBoxUser.fill = GridBagConstraints.BOTH;
		gbc_cBoxUser.gridx = 1;
		gbc_cBoxUser.gridy = 1;
		add(cBoxUser, gbc_cBoxUser);

		btnLogin = new JButton("Login");
		btnLogin.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					if (cBoxHost.getSelectedItem() == null || cBoxUser.getSelectedItem() == null)
						throw new NullPointerException("Please select a host and a user");
					Future<Cytomine> cytomineFuture = CytomineConnector.login(new URL(cBoxHost.getSelectedItem().toString()),
							cBoxUser.getSelectedItem().toString());
					Cytomine c = cytomineFuture.get();
					Preferences.getInstance().setDefaultHost(cBoxHost.getSelectedItem().toString());
					Preferences.getInstance().setDefaultUser(cBoxUser.getSelectedItem().toString());
					Preferences.save();
					if (onLoggedIn != null) onLoggedIn.logged(c);

				} catch (IllegalArgumentException | InterruptedException | ExecutionException | NullPointerException
						| IOException e1) {
					MessageDialog.showDialog("Error", e1.getMessage(), MessageDialog.ERROR_MESSAGE);
				}
			}
		});
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel.anchor = GridBagConstraints.EAST;
		gbc_panel.gridwidth = 3;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 2;
		add(panel, gbc_panel);
		panel.setLayout(new GridLayout(1, 0, 0, 0));
		{
			btnAddUser = new JButton("Add user");
			panel.add(btnAddUser);
			btnAddUser.setForeground(SystemColor.textHighlight);
			btnAddUser.setContentAreaFilled(false);
			btnAddUser.setBorderPainted(false);
			btnAddUser.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					new AddUserDialog("", "", parent);
					updateCredentials();
				}
			});

			btnEditUser = new JButton("Edit user");
			panel.add(btnEditUser);
			btnEditUser.setForeground(SystemColor.textHighlight);
			btnEditUser.setContentAreaFilled(false);
			btnEditUser.setBorderPainted(false);
			btnEditUser.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					try {
						String host, userName;
						if (cBoxHost.getSelectedItem() == null || cBoxUser.getSelectedItem() == null)
							throw new IllegalArgumentException("Please select a host and a user");

						host = cBoxHost.getSelectedItem().toString();
						userName = cBoxUser.getSelectedItem().toString();

						new EditUserDialog(host, userName, parent);
						updateCredentials();
					} catch (IllegalArgumentException e1) {
						e1.printStackTrace();
						MessageDialog.showDialog("Error", e1.getMessage(), MessageDialog.ERROR_MESSAGE);
					}
				}
			});

			btnRemoveUser = new JButton("Remove user");
			panel.add(btnRemoveUser);
			btnRemoveUser.setForeground(SystemColor.textHighlight);
			btnRemoveUser.setContentAreaFilled(false);
			btnRemoveUser.setBorderPainted(false);
			btnRemoveUser.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					try {
						if (cBoxHost.getSelectedItem() == null || cBoxUser.getSelectedItem() == null)
							throw new IllegalArgumentException("Please select a host and a user");
						if (!ConfirmDialog.confirm("Remove User Locally - Icytomine",
								"Are you sure to locally remove user " + cBoxUser.getSelectedItem().toString(),
								ConfirmDialog.YES_NO_OPTION))
							return;

						CytomineConnector.removeUser(new URL(cBoxHost.getSelectedItem().toString()),
								cBoxUser.getSelectedItem().toString());
						Preferences.save();
						updateCredentials();
					} catch (IllegalArgumentException | IOException e1) {
						e1.printStackTrace();
						MessageDialog.showDialog("Error", e1.getMessage(), MessageDialog.ERROR_MESSAGE);
					}
				}
			});

		}
		GridBagConstraints gbc_btnLogin = new GridBagConstraints();
		gbc_btnLogin.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnLogin.gridx = 2;
		gbc_btnLogin.gridy = 3;
		add(btnLogin, gbc_btnLogin);
		updateCredentials();
	}

	public void updateCredentials() {
		cBoxHost.removeAllItems();
		cBoxUser.removeAllItems();
		Preferences.getInstance().getAvailableCytomineCredentials().keySet().stream()
				.forEach(host -> cBoxHost.addItem(host));
		String selection = Preferences.getInstance().getDefaultHost();
		if (selection != null) {
			cBoxHost.setSelectedItem(selection);
		} else {
			try {
				cBoxHost.setSelectedIndex(0);
			} catch (IllegalArgumentException e) {}
		}
	}

}
