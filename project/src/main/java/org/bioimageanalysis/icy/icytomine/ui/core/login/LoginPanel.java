package org.bioimageanalysis.icy.icytomine.ui.core.login;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
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
	private CredentialManagementTools hostCredentialsManagementTools;
	private JComboBox<String> userComboBox;
	private CredentialManagementTools userCredentialsManagementTools;
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
		this.setPreferredSize(new Dimension(340, 150));
		this.setMinimumSize(new Dimension(340, 150));
		setGridBagLayout();
		setHostServerField();
		setUserField();
		setLoginButton();
	}

	private void setGridBagLayout() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {40, 100, 110, 66};
		gridBagLayout.rowHeights = new int[] {30, 30, 25};
		gridBagLayout.columnWeights = new double[] {0.0, 0.0, 0.0, 0.0};
		gridBagLayout.rowWeights = new double[] {0.0, 0.0, 0.0};
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
		hostComboBoxConstraints.insets = new Insets(0, 0, 10, 5);
		hostComboBoxConstraints.fill = GridBagConstraints.BOTH;
		hostComboBoxConstraints.gridwidth = 2;
		hostComboBoxConstraints.gridx = 1;
		hostComboBoxConstraints.gridy = 0;

		add(hostComboBox, hostComboBoxConstraints);

		hostCredentialsManagementTools = new CredentialManagementTools();
		hostCredentialsManagementTools.setAddToolTip("Add a new host");
		hostCredentialsManagementTools.setRemoveToolTip("Remove current host");
		hostCredentialsManagementTools.setEditToolTip("Edit current host");

		GridBagConstraints hostCredentialsManagementToolsConstraints = new GridBagConstraints();
		hostCredentialsManagementToolsConstraints.anchor = GridBagConstraints.WEST;
		hostCredentialsManagementToolsConstraints.insets = new Insets(0, 0, 10, 0);
		hostLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
		hostLabelConstraints.insets = new Insets(0, 0, 10, 0);
		hostLabelConstraints.gridx = 3;
		hostLabelConstraints.gridy = 0;

		add(hostCredentialsManagementTools, hostCredentialsManagementToolsConstraints);
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
		userComboBoxConstraints.insets = new Insets(0, 0, 10, 5);
		userComboBoxConstraints.fill = GridBagConstraints.BOTH;
		userComboBoxConstraints.gridx = 1;
		userComboBoxConstraints.gridy = 1;

		add(userComboBox, userComboBoxConstraints);

		userCredentialsManagementTools = new CredentialManagementTools();
		userCredentialsManagementTools.setAddToolTip("Add a new user");
		userCredentialsManagementTools.setRemoveToolTip("Remove current user");
		userCredentialsManagementTools.setEditToolTip("Edit current user");

		GridBagConstraints userCredentialsManagementToolsConstraints = new GridBagConstraints();
		userCredentialsManagementToolsConstraints.anchor = GridBagConstraints.WEST;
		userCredentialsManagementToolsConstraints.insets = new Insets(0, 0, 10, 0);
		userLabelConstraints.anchor = GridBagConstraints.WEST;
		userCredentialsManagementToolsConstraints.gridx = 3;
		userCredentialsManagementToolsConstraints.gridy = 1;

		add(userCredentialsManagementTools, userCredentialsManagementToolsConstraints);
	}

	private void setLoginButton() {
		loginButton = new JButton("Login");

		GridBagConstraints loginButtonConstraints = new GridBagConstraints();
		loginButtonConstraints.gridwidth = 4;
		loginButtonConstraints.insets = new Insets(0, 0, 0, 0);
		loginButtonConstraints.gridx = 0;
		loginButtonConstraints.gridy = 2;

		add(loginButton, loginButtonConstraints);
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

	public void addAddHostButtonListener(ActionListener listener) {
		hostCredentialsManagementTools.addAddButtonActionListener(listener);
	}

	public void addRemoveHostButtonListener(ActionListener listener) {
		hostCredentialsManagementTools.addRemoveButtonActionListener(listener);
	}

	public void addEditHostButtonListener(ActionListener listener) {
		hostCredentialsManagementTools.addEditButtonActionListener(listener);
	}

	public void addAddUserButtonListener(ActionListener listener) {
		userCredentialsManagementTools.addAddButtonActionListener(listener);
	}

	public void addRemoveUserButtonListener(ActionListener listener) {
		userCredentialsManagementTools.addRemoveButtonActionListener(listener);
	}

	public void addEditUserButtonListener(ActionListener listener) {
		userCredentialsManagementTools.addEditButtonActionListener(listener);
	}

	protected void setHosts(Set<String> hosts, Optional<String> selection) {
		hostComboBox.removeAllItems();
		userComboBox.removeAllItems();
		for (String host: hosts) {
			hostComboBox.addItem(host);
		}
		if (selection.isPresent())
			hostComboBox.setSelectedItem(selection.get());

		hostComboBox.updateUI();
	}

	protected void setUsers(Set<String> users, Optional<String> selection) {
		userComboBox.removeAllItems();
		for (String user: users) {
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
