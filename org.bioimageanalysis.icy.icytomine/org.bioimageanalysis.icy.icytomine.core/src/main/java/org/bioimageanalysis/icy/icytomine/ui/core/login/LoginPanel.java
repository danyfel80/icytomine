package org.bioimageanalysis.icy.icytomine.ui.core.login;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ItemListener;
import java.awt.event.MouseListener;
import java.util.Optional;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.bioimageanalysis.icy.icytomine.ui.core.login.LoginPanelController.LoginListener;

public class LoginPanel extends JPanel {

	private static final long serialVersionUID = 885665565700074663L;

	private JComboBox<String> hostComboBox;
	private JComboBox<String> userComboBox;
	private JPanel credentialsManagementPanel;
	private JButton addUserButton;
	private JButton editUserButton;
	private JButton removeUserButton;
	private JButton loginButton;

	private LoginPanelController controller;

	private Frame parentFrame;

	/**
	 * Create the panel.
	 * 
	 * @param parent
	 */
	public LoginPanel(Frame parent) {
		parentFrame = parent;
		setView();
		setController();
	}

	public Frame getParentFrame() {
		return this.parentFrame;
	}

	private void setView() {
		this.setPreferredSize(new Dimension(320, 150));
		this.setMinimumSize(new Dimension(320, 150));
		setGridBagLayout();
		setHostServerField();
		setUserField();
		setLoginButton();
		setCredentialsManagementPanel();
	}

	private void setGridBagLayout() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 40, 100, 110 };
		gridBagLayout.rowHeights = new int[] { 30, 30, 30, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0 };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0 };
		setLayout(gridBagLayout);
	}

	private void setHostServerField() {
		JLabel hostLabel = new JLabel("Host");
		hostLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		GridBagConstraints hostLabelConstraints = new GridBagConstraints();
		hostLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
		hostLabelConstraints.insets = new Insets(0, 0, 10, 5);
		hostLabelConstraints.gridx = 0;
		hostLabelConstraints.gridy = 0;

		add(hostLabel, hostLabelConstraints);

		hostComboBox = new JComboBox<>();
		hostLabel.setLabelFor(hostComboBox);

		GridBagConstraints hostComboBoxConstraints = new GridBagConstraints();
		hostComboBoxConstraints.insets = new Insets(0, 0, 10, 0);
		hostComboBoxConstraints.fill = GridBagConstraints.BOTH;
		hostComboBoxConstraints.gridwidth = 2;
		hostComboBoxConstraints.gridx = 1;
		hostComboBoxConstraints.gridy = 0;

		add(hostComboBox, hostComboBoxConstraints);
	}

	private void setUserField() {
		JLabel userLabel = new JLabel("User");
		userLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		GridBagConstraints userLabelConstraints = new GridBagConstraints();
		userLabelConstraints.anchor = GridBagConstraints.EAST;
		userLabelConstraints.insets = new Insets(0, 0, 10, 5);
		userLabelConstraints.gridx = 0;
		userLabelConstraints.gridy = 1;

		add(userLabel, userLabelConstraints);

		userComboBox = new JComboBox<>();

		GridBagConstraints userComboBoxConstraints = new GridBagConstraints();
		userComboBoxConstraints.gridwidth = 2;
		userComboBoxConstraints.insets = new Insets(0, 0, 10, 0);
		userComboBoxConstraints.fill = GridBagConstraints.BOTH;
		userComboBoxConstraints.gridx = 1;
		userComboBoxConstraints.gridy = 1;

		add(userComboBox, userComboBoxConstraints);
	}

	private void setLoginButton() {
		loginButton = new JButton("Login");

		GridBagConstraints loginButtonConstraints = new GridBagConstraints();
		loginButtonConstraints.fill = GridBagConstraints.HORIZONTAL;
		loginButtonConstraints.gridx = 2;
		loginButtonConstraints.gridy = 3;

		add(loginButton, loginButtonConstraints);
	}

	private void setCredentialsManagementPanel() {
		credentialsManagementPanel = new JPanel();
		credentialsManagementPanel.setLayout(new GridLayout(1, 0, 0, 0));
		setAddUserButton();
		setEditUserButton();
		setRemoveUserButton();

		GridBagConstraints credentialManagementPanelConstraints = new GridBagConstraints();
		credentialManagementPanelConstraints.fill = GridBagConstraints.HORIZONTAL;
		credentialManagementPanelConstraints.anchor = GridBagConstraints.EAST;
		credentialManagementPanelConstraints.gridwidth = 3;
		credentialManagementPanelConstraints.gridx = 0;
		credentialManagementPanelConstraints.gridy = 2;

		add(credentialsManagementPanel, credentialManagementPanelConstraints);
	}

	private void setAddUserButton() {
		addUserButton = new JButton("Add user");

		addUserButton.setForeground(SystemColor.textHighlight);
		addUserButton.setContentAreaFilled(false);
		addUserButton.setBorderPainted(false);

		credentialsManagementPanel.add(addUserButton);
	}

	private void setEditUserButton() {
		editUserButton = new JButton("Edit user");

		editUserButton.setForeground(SystemColor.textHighlight);
		editUserButton.setContentAreaFilled(false);
		editUserButton.setBorderPainted(false);

		credentialsManagementPanel.add(editUserButton);
	}

	private void setRemoveUserButton() {
		removeUserButton = new JButton("Remove user");

		removeUserButton.setForeground(SystemColor.textHighlight);
		removeUserButton.setContentAreaFilled(false);
		removeUserButton.setBorderPainted(false);

		credentialsManagementPanel.add(removeUserButton);
	}

	private void setController() {
		controller = new LoginPanelController(this);
	}

	protected void addHostServerSelectionListener(ItemListener listener) {
		hostComboBox.addItemListener(listener);
	}

	protected void addLoginButtonListener(MouseListener listener) {
		loginButton.addMouseListener(listener);
	}

	public void addAddUserButtonListener(MouseListener listener) {
		addUserButton.addMouseListener(listener);
	}

	public void addEditUserButtonListener(MouseListener listener) {
		editUserButton.addMouseListener(listener);
	}

	public void addRemoveUserButtonListener(MouseListener listener) {
		removeUserButton.addMouseListener(listener);
	}

	protected void setHosts(Set<String> hosts, Optional<String> selection) {
		hostComboBox.removeAllItems();
		userComboBox.removeAllItems();
		for (String host : hosts) {
			hostComboBox.addItem(host);
		}
		if (selection.isPresent())
			hostComboBox.setSelectedItem(selection.get());
		
		hostComboBox.updateUI();
	}

	protected void setUsers(Set<String> users, Optional<String> selection) {
		userComboBox.removeAllItems();
		for (String user : users) {
			userComboBox.addItem(user);
		}
		if (selection.isPresent())
			userComboBox.setSelectedItem(selection.get());
		else
			userComboBox.setSelectedIndex(0);
	}

	public Optional<String> getSelectedHost() {
		return Optional.ofNullable((String) hostComboBox.getSelectedItem());
	}

	public Optional<String> getSelectedUser() {
		return Optional.ofNullable((String) userComboBox.getSelectedItem());
	}

	public void addLoginListener(LoginListener listener) {
		controller.addLoginListener(listener);
	}

	public void removeLoginListener(LoginListener listener) {
		controller.removeLoginListener(listener);
	}

	public void start() {
		controller.updateCredentials();
	}

}
