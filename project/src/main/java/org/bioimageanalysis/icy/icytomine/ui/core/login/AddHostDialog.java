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
public class AddHostDialog extends JDialog {

	public interface HostAdditionListener {
		public void requestAddition(String hostName) throws RuntimeException;
	}

	private Container contentPane;
	private JTextField hostTextField;
	private JButton addHostButton;
	private JButton cancelButton;
	private ActionListener addHostButtonListener;

	public AddHostDialog(Frame owner) {
		super(owner, "Add Host - Icytomine", true);
		contentPane = getContentPane();

		setPreferredSize(new Dimension(275, 140));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setModal(true);
		setResizable(false);

		adjustLayout();
		addHostLabel();
		addHostTextField();
		addAddHostButton();
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

		GridBagConstraints hostTextFieldConstraints = new GridBagConstraints();
		hostTextFieldConstraints.gridwidth = 2;
		hostTextFieldConstraints.insets = new Insets(0, 0, 5, 0);
		hostTextFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
		hostTextFieldConstraints.gridx = 0;
		hostTextFieldConstraints.gridy = 1;

		contentPane.add(hostTextField, hostTextFieldConstraints);
	}

	private void addAddHostButton() {
		addHostButton = new JButton("Add host");
		addHostButton.setMinimumSize(new Dimension(75, 23));
		addHostButton.setMaximumSize(new Dimension(75, 23));
		addHostButton.setPreferredSize(new Dimension(75, 23));
		addHostButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		GridBagConstraints addHostButtonConstraints = new GridBagConstraints();
		addHostButtonConstraints.insets = new Insets(0, 0, 0, 5);
		addHostButtonConstraints.gridx = 0;
		addHostButtonConstraints.gridy = 2;

		contentPane.add(addHostButton, addHostButtonConstraints);
	}

	private void addCancelButton() {
		cancelButton = new JButton("Cancel");
		cancelButton.setMinimumSize(new Dimension(75, 23));
		cancelButton.setMaximumSize(new Dimension(75, 23));
		cancelButton.setPreferredSize(new Dimension(75, 23));
		cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		GridBagConstraints cancelButtonConstraints = new GridBagConstraints();
		cancelButtonConstraints.gridx = 1;
		cancelButtonConstraints.gridy = 2;

		contentPane.add(cancelButton, cancelButtonConstraints);
	}

	public void setHostAdditionListener(HostAdditionListener listener) {
		if (addHostButtonListener == null) {
			addHostButtonListener = (e) -> {
				String hostName = hostTextField.getText();
				try {
					listener.requestAddition(hostName);
					AddHostDialog.this.setVisible(false);
					AddHostDialog.this.dispose();
				} catch (RuntimeException ex) {
					MessageDialog.showDialog("Error adding host", ex.getMessage(), MessageDialog.ERROR_MESSAGE);
				}
			};
		}
		addHostButton.addActionListener(addHostButtonListener);
	}

	public void unsetHostAdditionListener() {
		if (addHostButtonListener != null) {
			addHostButton.removeActionListener(addHostButtonListener);
		}
	}

	private void setCancelButtonHandler() {
		cancelButton.addActionListener((e) -> {
			AddHostDialog.this.setVisible(false);
			AddHostDialog.this.dispose();
		});
	}
}
