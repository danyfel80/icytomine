package org.bioimageanalysis.icy.icytomine.ui.core.login;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

import icy.gui.dialog.MessageDialog;

@SuppressWarnings("serial")
public class EditHostDialog extends JDialog {

	public interface HostEditionListener {
		public void requestEdition(String oldHostName, String newHostName) throws RuntimeException;
	}

	private String originalHostName;
	
	private Container contentPane;
	private JTextField hostTextField;
	private JButton editHostButton;
	private JButton cancelButton;
	
	private ActionListener editHostButtonListener;

	public EditHostDialog(Frame owner, String hostName) {
		super(owner, "Edit Host - Icytomine", true);
		originalHostName = hostName;
		contentPane = getContentPane();

		setPreferredSize(new Dimension(275, 140));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setModal(true);
		setResizable(false);

		adjustLayout();
		addHostLabel();
		addHostTextField();
		addEditHostButton();
		addCancelButton();

		setCancelButtonHandler();

		pack();
		setLocationRelativeTo(owner);
	}

	private void adjustLayout() {
		contentPane.setPreferredSize(new Dimension(300, 210));

		GridBagLayout contentPanelLayout = new GridBagLayout();
		contentPanelLayout.columnWidths = new int[] {1, 1};
		contentPanelLayout.rowHeights = new int[] {0, 0, 0};
		contentPanelLayout.columnWeights = new double[] {0.0, 0.0};
		contentPanelLayout.rowWeights = new double[] {0.0, 0.0, 0.0};
		contentPane.setLayout(contentPanelLayout);
	}

	private void addHostLabel() {
		JLabel hostLabel = new JLabel("Specify the host address");

		GridBagConstraints hostLabelConstraints = new GridBagConstraints();
		hostLabelConstraints.gridwidth = 2;
		hostLabelConstraints.insets = new Insets(10, 0, 5, 0);
		hostLabelConstraints.gridx = 0;
		hostLabelConstraints.gridy = 0;

		contentPane.add(hostLabel, hostLabelConstraints);
	}

	private void addHostTextField() {
		hostTextField = new JTextField();
		hostTextField.setColumns(10);
		hostTextField.setText(originalHostName);

		GridBagConstraints hostTextFieldConstraints = new GridBagConstraints();
		hostTextFieldConstraints.gridwidth = 2;
		hostTextFieldConstraints.insets = new Insets(0, 0, 5, 0);
		hostTextFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
		hostTextFieldConstraints.gridx = 0;
		hostTextFieldConstraints.gridy = 1;

		contentPane.add(hostTextField, hostTextFieldConstraints);
	}

	private void addEditHostButton() {
		editHostButton = new JButton("Update host");
		editHostButton.setMinimumSize(new Dimension(95, 23));
		editHostButton.setMaximumSize(new Dimension(95, 23));
		editHostButton.setPreferredSize(new Dimension(95, 23));
		editHostButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		GridBagConstraints editHostButtonConstraints = new GridBagConstraints();
		editHostButtonConstraints.insets = new Insets(0, 0, 0, 5);
		editHostButtonConstraints.gridx = 0;
		editHostButtonConstraints.gridy = 2;

		contentPane.add(editHostButton, editHostButtonConstraints);
	}

	private void addCancelButton() {
		cancelButton = new JButton("Cancel");
		cancelButton.setMinimumSize(new Dimension(95, 23));
		cancelButton.setMaximumSize(new Dimension(95, 23));
		cancelButton.setPreferredSize(new Dimension(95, 23));
		cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		GridBagConstraints cancelButtonConstraints = new GridBagConstraints();
		cancelButtonConstraints.gridx = 1;
		cancelButtonConstraints.gridy = 2;

		contentPane.add(cancelButton, cancelButtonConstraints);
	}

	public void setHostEditionListener(HostEditionListener listener) {
		if (editHostButtonListener == null) {
			editHostButtonListener = (e) -> {
				String hostName = hostTextField.getText();
				try {
					listener.requestEdition(originalHostName, hostName);
					EditHostDialog.this.setVisible(false);
					EditHostDialog.this.dispose();
				} catch (RuntimeException ex) {
					MessageDialog.showDialog("Error updating host", ex.getMessage(), MessageDialog.ERROR_MESSAGE);
				}
			};
		}
		editHostButton.addActionListener(editHostButtonListener);
	}

	public void unsetHostEditionListener() {
		if (editHostButtonListener != null) {
			editHostButton.removeActionListener(editHostButtonListener);
		}
	}

	private void setCancelButtonHandler() {
		cancelButton.addActionListener((e) -> {
			EditHostDialog.this.setVisible(false);
			EditHostDialog.this.dispose();
		});
	}
}
