package org.bioimageanalysis.icy.icytomine.ui.core.login;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.bioimageanalysis.icy.icytomine.core.connection.persistence.UserCredential;

import icy.gui.dialog.MessageDialog;

@SuppressWarnings("serial")
public class EditUserDialog extends JDialog {

	public interface UserUpdateListener {
		public void requestUpdate(String hostName, String oldUserName, String userName, String publicKey, String privateKey)
				throws RuntimeException;
	}

	private String targetHostName;
	private String targetUserName;
	private UserCredential targetUserCredentials;

	private Container contentPane;
	private JTextField userTextField;
	private JTextField publicKeyTextField;
	private JTextField privateKeyTextField;
	private JButton updateButton;
	private JButton cancelButton;

	private ActionListener updateUserButtonListener;

	/**
	 * Create the dialog.
	 * 
     * @param owner
	 * @param targetHost
	 * @param targetUserName
	 * @param userCredentials
	 */
	public EditUserDialog(Frame owner, String targetHost, String targetUserName, UserCredential userCredentials) {
		super(owner, "Edit User - Icytomine", true);
		this.targetHostName = targetHost;
		this.targetUserName = targetUserName;
		this.targetUserCredentials = userCredentials;
		contentPane = getContentPane();

		setPreferredSize(new Dimension(300, 210));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setModal(true);
		setResizable(false);

		adjustLayout();
		setHostNameLabel();
		setUserNameLabel();
		setUserNameTextField();
		setPublicKeyLabel();
		setPublicKeyTextField();
		setPrivateKeyLabel();
		setPrivateKeyTextField();
		setActionButtonsPanel();

		setCancelButtonHandler();

		pack();
		setLocationRelativeTo(owner);
	}

	private void adjustLayout() {
		getContentPane().setPreferredSize(new Dimension(300, 210));

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {70, 150};
		gridBagLayout.rowHeights = new int[] {0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[] {0.0, 0.0};
		gridBagLayout.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0};

		contentPane.setLayout(gridBagLayout);
	}

	private void setHostNameLabel() {
		JLabel hostLabel = new JLabel("Target host: " + targetHostName);
		hostLabel.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints hostLabelConstraints = new GridBagConstraints();
		hostLabelConstraints.gridx = 0;
		hostLabelConstraints.gridy = 0;
		hostLabelConstraints.gridwidth = 2;
		hostLabelConstraints.insets = new Insets(0, 0, 7, 0);

		contentPane.add(hostLabel, hostLabelConstraints);
	}

	private void setUserNameLabel() {
		JLabel userLabel = new JLabel("User");

		GridBagConstraints userLabelConstraints = new GridBagConstraints();
		userLabelConstraints.anchor = GridBagConstraints.EAST;
		userLabelConstraints.insets = new Insets(0, 0, 5, 5);
		userLabelConstraints.gridx = 0;
		userLabelConstraints.gridy = 1;

		contentPane.add(userLabel, userLabelConstraints);
	}

	private void setUserNameTextField() {
		userTextField = new JTextField();
		userTextField.setColumns(10);
		userTextField.setText(targetUserName);

		GridBagConstraints userTextFieldConstraints = new GridBagConstraints();
		userTextFieldConstraints.insets = new Insets(0, 0, 5, 0);
		userTextFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
		userTextFieldConstraints.gridx = 1;
		userTextFieldConstraints.gridy = 1;

		contentPane.add(userTextField, userTextFieldConstraints);
	}

	private void setPublicKeyLabel() {
		JLabel publicKeyLabel = new JLabel("Public Key");

		GridBagConstraints publicKeyLabelConstraints = new GridBagConstraints();
		publicKeyLabelConstraints.anchor = GridBagConstraints.EAST;
		publicKeyLabelConstraints.insets = new Insets(0, 0, 5, 5);
		publicKeyLabelConstraints.gridx = 0;
		publicKeyLabelConstraints.gridy = 2;

		contentPane.add(publicKeyLabel, publicKeyLabelConstraints);
	}

	private void setPublicKeyTextField() {
		publicKeyTextField = new JTextField();
		publicKeyTextField.setColumns(10);
		publicKeyTextField.setText(targetUserCredentials.getPublicKey());

		GridBagConstraints publicKeyTextFieldConstraints = new GridBagConstraints();
		publicKeyTextFieldConstraints.insets = new Insets(0, 0, 5, 0);
		publicKeyTextFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
		publicKeyTextFieldConstraints.gridx = 1;
		publicKeyTextFieldConstraints.gridy = 2;

		contentPane.add(publicKeyTextField, publicKeyTextFieldConstraints);
	}

	private void setPrivateKeyLabel() {
		JLabel privateKeyLabel = new JLabel("Private Key");

		GridBagConstraints privateKeyLabelConstraints = new GridBagConstraints();
		privateKeyLabelConstraints.anchor = GridBagConstraints.EAST;
		privateKeyLabelConstraints.insets = new Insets(0, 0, 10, 5);
		privateKeyLabelConstraints.gridx = 0;
		privateKeyLabelConstraints.gridy = 3;

		contentPane.add(privateKeyLabel, privateKeyLabelConstraints);
	}

	private void setPrivateKeyTextField() {
		privateKeyTextField = new JTextField();
		privateKeyTextField.setColumns(10);
		privateKeyTextField.setText(targetUserCredentials.getPrivateKey());

		GridBagConstraints privateKeyTextFieldConstraints = new GridBagConstraints();
		privateKeyTextFieldConstraints.insets = new Insets(0, 0, 10, 0);
		privateKeyTextFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
		privateKeyTextFieldConstraints.gridx = 1;
		privateKeyTextFieldConstraints.gridy = 3;

		contentPane.add(privateKeyTextField, privateKeyTextFieldConstraints);
	}

	private void setActionButtonsPanel() {
		JPanel actionButtonsPanel = new JPanel();
		actionButtonsPanel.setLayout(new GridLayout(0, 2, 10, 0));

		GridBagConstraints actionButtonsPanelConstraints = new GridBagConstraints();
		actionButtonsPanelConstraints.insets = new Insets(5, 0, 0, 0);
		actionButtonsPanelConstraints.gridwidth = 2;
		actionButtonsPanelConstraints.gridx = 0;
		actionButtonsPanelConstraints.gridy = 4;

		getContentPane().add(actionButtonsPanel, actionButtonsPanelConstraints);

		updateButton = new JButton("Update");
		actionButtonsPanel.add(updateButton);

		cancelButton = new JButton("Cancel");
		actionButtonsPanel.add(cancelButton);
	}

	public void setUserUpdateListener(UserUpdateListener listener) {
		if (updateUserButtonListener == null) {
			updateUserButtonListener = (e) -> {
				String userName = userTextField.getText();
				String publicKey = publicKeyTextField.getText();
				String privateKey = privateKeyTextField.getText();

				try {
					listener.requestUpdate(targetHostName, targetUserName, userName, publicKey, privateKey);
					EditUserDialog.this.setVisible(false);
					EditUserDialog.this.dispose();
				} catch (RuntimeException ex) {
					MessageDialog.showDialog("Error updating user", ex.getMessage(), MessageDialog.ERROR_MESSAGE);
				}
			};
		}
		updateButton.addActionListener(updateUserButtonListener);
	}

	public void unsetHostUpdateListener() {
		if (updateUserButtonListener != null) {
			updateButton.removeActionListener(updateUserButtonListener);
		}
	}

	private void setCancelButtonHandler() {
		cancelButton.addActionListener((e) -> {
			EditUserDialog.this.setVisible(false);
			EditUserDialog.this.dispose();
		});
	}
}
