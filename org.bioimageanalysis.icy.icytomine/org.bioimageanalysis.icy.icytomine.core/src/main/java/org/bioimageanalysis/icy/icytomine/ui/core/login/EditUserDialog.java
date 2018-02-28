package org.bioimageanalysis.icy.icytomine.ui.core.login;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.bioimageanalysis.icy.icytomine.core.connection.CytomineConnector;
import org.bioimageanalysis.icy.icytomine.core.connection.user.Preferences;
import org.bioimageanalysis.icy.icytomine.core.connection.user.UserKeys;

import icy.gui.dialog.MessageDialog;

public class EditUserDialog extends JDialog {
	private static final long serialVersionUID = -5384028048425032284L;

	private String	oldHost;
	private String	oldUserName;

	private JTextField	tFieldHost;
	private JTextField	tFieldUser;
	private JTextField	tFieldPublicKey;
	private JTextField	tFieldPrivateKey;

	/**
	 * Create the dialog.
	 * 
	 * @param host
	 * @param userName
	 * @param owner
	 */
	public EditUserDialog(String host, String userName, Frame owner) {
		super(owner, "Edit User - Icytomine", true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		oldHost = host;
		oldUserName = userName;

		getContentPane().setPreferredSize(new Dimension(300, 210));
		setModal(true);
		setResizable(false);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 70, 150 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0 };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0 };
		getContentPane().setLayout(gridBagLayout);

		JLabel lblHost = new JLabel("Host");
		GridBagConstraints gbc_lblHost = new GridBagConstraints();
		gbc_lblHost.insets = new Insets(0, 0, 5, 5);
		gbc_lblHost.anchor = GridBagConstraints.EAST;
		gbc_lblHost.gridx = 0;
		gbc_lblHost.gridy = 0;
		getContentPane().add(lblHost, gbc_lblHost);

		tFieldHost = new JTextField();
		GridBagConstraints gbc_tFieldHost = new GridBagConstraints();
		gbc_tFieldHost.insets = new Insets(0, 0, 5, 0);
		gbc_tFieldHost.fill = GridBagConstraints.HORIZONTAL;
		gbc_tFieldHost.gridx = 1;
		gbc_tFieldHost.gridy = 0;
		getContentPane().add(tFieldHost, gbc_tFieldHost);
		tFieldHost.setColumns(10);
		tFieldHost.setText(host);

		JLabel lblUser = new JLabel("User");
		GridBagConstraints gbc_lblUser = new GridBagConstraints();
		gbc_lblUser.anchor = GridBagConstraints.EAST;
		gbc_lblUser.insets = new Insets(0, 0, 5, 5);
		gbc_lblUser.gridx = 0;
		gbc_lblUser.gridy = 1;
		getContentPane().add(lblUser, gbc_lblUser);

		tFieldUser = new JTextField();
		GridBagConstraints gbc_tFieldUser = new GridBagConstraints();
		gbc_tFieldUser.insets = new Insets(0, 0, 5, 0);
		gbc_tFieldUser.fill = GridBagConstraints.HORIZONTAL;
		gbc_tFieldUser.gridx = 1;
		gbc_tFieldUser.gridy = 1;
		getContentPane().add(tFieldUser, gbc_tFieldUser);
		tFieldUser.setColumns(10);
		tFieldUser.setText(userName);

		UserKeys userData;
		try {
			userData = Preferences.getInstance().getAvailableCytomineCredentials().get(host).get(userName);
		} catch (Exception e) {
			userData = null;
		}
		if (userData == null) {
			userData = new UserKeys();
			userData.setPublicKey("");
			userData.setPrivateKey("");
		}

		JLabel lblPublicKey = new JLabel("Public Key");
		GridBagConstraints gbc_lblPublicKey = new GridBagConstraints();
		gbc_lblPublicKey.anchor = GridBagConstraints.EAST;
		gbc_lblPublicKey.insets = new Insets(0, 0, 5, 5);
		gbc_lblPublicKey.gridx = 0;
		gbc_lblPublicKey.gridy = 2;
		getContentPane().add(lblPublicKey, gbc_lblPublicKey);

		tFieldPublicKey = new JTextField();
		GridBagConstraints gbc_tFieldPublicKey = new GridBagConstraints();
		gbc_tFieldPublicKey.insets = new Insets(0, 0, 5, 0);
		gbc_tFieldPublicKey.fill = GridBagConstraints.HORIZONTAL;
		gbc_tFieldPublicKey.gridx = 1;
		gbc_tFieldPublicKey.gridy = 2;
		getContentPane().add(tFieldPublicKey, gbc_tFieldPublicKey);
		tFieldPublicKey.setColumns(10);
		tFieldPublicKey.setText(userData.getPublicKey());

		JLabel lblPrivateKey = new JLabel("Private Key");
		GridBagConstraints gbc_lblPrivateKey = new GridBagConstraints();
		gbc_lblPrivateKey.anchor = GridBagConstraints.EAST;
		gbc_lblPrivateKey.insets = new Insets(0, 0, 5, 5);
		gbc_lblPrivateKey.gridx = 0;
		gbc_lblPrivateKey.gridy = 3;
		getContentPane().add(lblPrivateKey, gbc_lblPrivateKey);

		tFieldPrivateKey = new JTextField();
		GridBagConstraints gbc_tFieldPrivateKey = new GridBagConstraints();
		gbc_tFieldPrivateKey.insets = new Insets(0, 0, 5, 0);
		gbc_tFieldPrivateKey.fill = GridBagConstraints.HORIZONTAL;
		gbc_tFieldPrivateKey.gridx = 1;
		gbc_tFieldPrivateKey.gridy = 3;
		getContentPane().add(tFieldPrivateKey, gbc_tFieldPrivateKey);
		tFieldPrivateKey.setColumns(10);
		tFieldPrivateKey.setText(userData.getPrivateKey());

		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(5, 0, 0, 0);
		gbc_panel.anchor = GridBagConstraints.EAST;
		gbc_panel.gridwidth = 2;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 4;
		getContentPane().add(panel, gbc_panel);
		panel.setLayout(new GridLayout(0, 2, 10, 0));

		JButton btnSave = new JButton("Save");
		btnSave.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				String host = tFieldHost.getText();
				String userName = tFieldUser.getText();
				String publicKey = tFieldPublicKey.getText();
				String privateKey = tFieldPrivateKey.getText();

				try {
					CytomineConnector.updateUser(new URL(oldHost), oldUserName, new URL(host), userName, publicKey, privateKey);
					Preferences.save();
					EditUserDialog.this.setVisible(false);
					EditUserDialog.this.dispose();
				} catch (RuntimeException | IOException e1) {
					e1.printStackTrace();
					MessageDialog.showDialog("Error", e1.getMessage(), MessageDialog.ERROR_MESSAGE);
				}

			}
		});
		panel.add(btnSave);

		JButton btnCancel = new JButton("Cancel");
		btnCancel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				setVisible(false);
				dispose();
			}
		});
		panel.add(btnCancel);

		pack();
		setLocationRelativeTo(owner);
		setVisible(true);
	}

}
