package org.bioimageanalysis.icy.icytomine.ui.core.login;

import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Dimension;
import java.awt.Component;

@SuppressWarnings("serial")
public class CredentialManagementTools extends JPanel {

	private static Image plusImage;
	private static Image minusImage;
	private static Image pencilImage;

	{
		plusImage = readImage("/res/icon/alpha/plus.png").getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH);
		minusImage = readImage("/res/icon/alpha/minus.png").getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH);
		pencilImage = readImage("/res/icon/alpha/pencil.png").getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH);
	}

	private static Image readImage(String resource) {
		try {
			return ImageIO.read(CredentialManagementTools.class.getResource(resource));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private GridBagLayout layout;
	private JButton addButton;
	private JButton removeButton;
	private JButton editButton;

	public CredentialManagementTools() {
		setPreferredSize(new Dimension(66, 22));
		setMinimumSize(new Dimension(66, 22));
		configureLayout();
		createAddButton();
		createRemoveButton();
		createEditButton();
	}

	private void configureLayout() {
		layout = new GridBagLayout();
		layout.columnWidths = new int[] {22, 22, 22, 0};
		layout.rowHeights = new int[] {22, 0};
		layout.columnWeights = new double[] {0.0, 0.0, 0.0, Double.MIN_VALUE};
		layout.rowWeights = new double[] {0, Double.MIN_VALUE};
		setLayout(layout);
	}

	private void createAddButton() {
		addButton = new JButton();
		addButton.setMaximumSize(new Dimension(22, 22));
		addButton.setMinimumSize(new Dimension(22, 22));
		addButton.setPreferredSize(new Dimension(22, 22));
		addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		addButton.setMargin(new Insets(0, 0, 0, 0));
		addButton.setIcon(new ImageIcon(plusImage));

		GridBagConstraints addButtonConstraints = new GridBagConstraints();
		addButtonConstraints.insets = new Insets(0, 0, 0, 0);
		addButtonConstraints.gridx = 0;
		addButtonConstraints.gridy = 0;

		add(addButton, addButtonConstraints);
	}

	private void createRemoveButton() {
		removeButton = new JButton();
		removeButton.setMaximumSize(new Dimension(22, 22));
		removeButton.setMinimumSize(new Dimension(22, 22));
		removeButton.setPreferredSize(new Dimension(22, 22));
		removeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		removeButton.setMargin(new Insets(0, 0, 0, 0));
		removeButton.setIcon(new ImageIcon(minusImage));

		GridBagConstraints removeButtonConstraints = new GridBagConstraints();
		removeButtonConstraints.insets = new Insets(0, 0, 0, 0);
		removeButtonConstraints.gridx = 1;
		removeButtonConstraints.gridy = 0;

		add(removeButton, removeButtonConstraints);
	}

	private void createEditButton() {
		editButton = new JButton();
		editButton.setMinimumSize(new Dimension(22, 22));
		editButton.setMaximumSize(new Dimension(22, 22));
		editButton.setPreferredSize(new Dimension(22, 22));
		editButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		editButton.setMargin(new Insets(0, 0, 0, 0));
		editButton.setIcon(new ImageIcon(pencilImage));

		GridBagConstraints editButtonConstraints = new GridBagConstraints();
		editButtonConstraints.insets = new Insets(0, 0, 0, 0);
		editButtonConstraints.gridx = 2;
		editButtonConstraints.gridy = 0;

		add(editButton, editButtonConstraints);
	}

	public void addAddButtonActionListener(ActionListener listener) {
		this.addButton.addActionListener(listener);
	}

	public void removeAddButtonActionListener(ActionListener listener) {
		this.addButton.removeActionListener(listener);
	}

	public void addRemoveButtonActionListener(ActionListener listener) {
		this.removeButton.addActionListener(listener);
	}

	public void removeRemoveButtonActionListener(ActionListener listener) {
		this.removeButton.removeActionListener(listener);
	}

	public void addEditButtonActionListener(ActionListener listener) {
		this.editButton.addActionListener(listener);
	}

	public void editAddButtonActionListener(ActionListener listener) {
		this.editButton.removeActionListener(listener);
	}

	public void setAddToolTip(String tip) {
		addButton.setToolTipText(tip);
	}

	public void setRemoveToolTip(String tip) {
		removeButton.setToolTipText(tip);
	}

	public void setEditToolTip(String tip) {
		editButton.setToolTipText(tip);
	}
}
