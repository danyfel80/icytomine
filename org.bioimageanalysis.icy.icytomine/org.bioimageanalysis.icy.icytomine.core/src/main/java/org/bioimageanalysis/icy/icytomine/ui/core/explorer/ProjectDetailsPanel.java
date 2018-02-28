package org.bioimageanalysis.icy.icytomine.ui.core.explorer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.util.stream.Collectors;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import be.cytomine.client.CytomineException;
import javax.swing.BoxLayout;
import javax.swing.UIManager;

import org.bioimageanalysis.icy.icytomine.core.model.Project;

public class ProjectDetailsPanel extends JPanel {
	private static final long serialVersionUID = -8451854002703582368L;

	private Project currentProject;

	private JTextArea		lblProjectName;
	private JTextArea	txtrProjectDescription;
	private JTextArea		lblIdValue;
	private JTextArea		lblOntologyValue;
	private JTextArea		lblImagesValue;
	private JTextArea		lblAnnotationsValue;
	private JTextArea	txtrUsers;

	public ProjectDetailsPanel() {
		setMinimumSize(new Dimension(150, 300));
		setPreferredSize(new Dimension(240, 400));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JLabel lblProjectDetails = new JLabel("Project Details");
		lblProjectDetails.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblProjectDetails.setHorizontalAlignment(SwingConstants.CENTER);
		lblProjectDetails.setFont(new Font("Tahoma", Font.BOLD, 11));
		add(lblProjectDetails);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setAutoscrolls(true);

		add(scrollPane);

		JPanel panel = new JPanel();
		panel.setBackground(UIManager.getColor("Panel.background"));
		panel.setPreferredSize(new Dimension(200, 300));
		scrollPane.setViewportView(panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 40, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 0.0 };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		panel.setLayout(gbl_panel);

		lblProjectName = new JTextArea("Project Name");
		lblProjectName.setEditable(false);
		lblProjectName.setOpaque(false);
		lblProjectName.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblProjectName.setBackground(SystemColor.control);
		GridBagConstraints gbc_lblProjectName = new GridBagConstraints();
		gbc_lblProjectName.anchor = GridBagConstraints.NORTH;
		gbc_lblProjectName.gridwidth = 2;
		gbc_lblProjectName.insets = new Insets(10, 10, 10, 10);
		gbc_lblProjectName.gridx = 0;
		gbc_lblProjectName.gridy = 0;
		panel.add(lblProjectName, gbc_lblProjectName);

		txtrProjectDescription = new JTextArea();
		txtrProjectDescription.setOpaque(false);
		txtrProjectDescription.setSelectionColor(UIManager.getColor("TextArea.selectionBackground"));
		txtrProjectDescription.setSelectedTextColor(UIManager.getColor("TextArea.selectionForeground"));
		txtrProjectDescription.setEditable(false);
		txtrProjectDescription.setBackground(UIManager.getColor("Panel.background"));
		txtrProjectDescription.setFont(new Font("Tahoma", Font.PLAIN, 12));
		txtrProjectDescription.setMaximumSize(new Dimension(2147483647, 100));
		txtrProjectDescription.setPreferredSize(new Dimension(200, 50));
		txtrProjectDescription.setMinimumSize(new Dimension(60, 50));
		txtrProjectDescription.setLineWrap(true);
		txtrProjectDescription.setWrapStyleWord(true);
		txtrProjectDescription.setText("Some description of the project, maybe multiple lines involved here");
		GridBagConstraints gbc_txtrProjectDescription = new GridBagConstraints();
		gbc_txtrProjectDescription.gridwidth = 2;
		gbc_txtrProjectDescription.insets = new Insets(0, 0, 15, 0);
		gbc_txtrProjectDescription.fill = GridBagConstraints.BOTH;
		gbc_txtrProjectDescription.gridx = 0;
		gbc_txtrProjectDescription.gridy = 1;
		panel.add(txtrProjectDescription, gbc_txtrProjectDescription);

		JLabel lblId = new JLabel("ID");
		lblId.setFocusable(false);
		lblId.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblId.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblId = new GridBagConstraints();
		gbc_lblId.insets = new Insets(0, 0, 5, 10);
		gbc_lblId.anchor = GridBagConstraints.EAST;
		gbc_lblId.gridx = 0;
		gbc_lblId.gridy = 2;
		panel.add(lblId, gbc_lblId);

		lblIdValue = new JTextArea("12345678");
		lblIdValue.setEditable(false);
		lblIdValue.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblIdValue.setOpaque(false);
		GridBagConstraints gbc_lblIdValue = new GridBagConstraints();
		gbc_lblIdValue.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblIdValue.insets = new Insets(0, 0, 5, 0);
		gbc_lblIdValue.gridx = 1;
		gbc_lblIdValue.gridy = 2;
		panel.add(lblIdValue, gbc_lblIdValue);

		JLabel lblOntology = new JLabel("Ontology");
		lblOntology.setFocusable(false);
		lblOntology.setHorizontalAlignment(SwingConstants.RIGHT);
		lblOntology.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblOntology = new GridBagConstraints();
		gbc_lblOntology.anchor = GridBagConstraints.EAST;
		gbc_lblOntology.insets = new Insets(0, 0, 5, 10);
		gbc_lblOntology.gridx = 0;
		gbc_lblOntology.gridy = 3;
		panel.add(lblOntology, gbc_lblOntology);

		lblOntologyValue = new JTextArea("Some ontology name");
		lblOntologyValue.setEditable(false);
		lblOntologyValue.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblOntologyValue.setOpaque(false);
		GridBagConstraints gbc_lblOntologyValue = new GridBagConstraints();
		gbc_lblOntologyValue.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblOntologyValue.insets = new Insets(0, 0, 5, 0);
		gbc_lblOntologyValue.gridx = 1;
		gbc_lblOntologyValue.gridy = 3;
		panel.add(lblOntologyValue, gbc_lblOntologyValue);

		JLabel lblImages = new JLabel("Images");
		lblImages.setFocusable(false);
		lblImages.setHorizontalAlignment(SwingConstants.RIGHT);
		lblImages.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblImages = new GridBagConstraints();
		gbc_lblImages.anchor = GridBagConstraints.EAST;
		gbc_lblImages.insets = new Insets(0, 0, 5, 10);
		gbc_lblImages.gridx = 0;
		gbc_lblImages.gridy = 4;
		panel.add(lblImages, gbc_lblImages);

		lblImagesValue = new JTextArea("14");
		lblImagesValue.setEditable(false);
		lblImagesValue.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblImagesValue.setOpaque(false);
		GridBagConstraints gbc_lblImagesValue = new GridBagConstraints();
		gbc_lblImagesValue.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblImagesValue.insets = new Insets(0, 0, 5, 0);
		gbc_lblImagesValue.gridx = 1;
		gbc_lblImagesValue.gridy = 4;
		panel.add(lblImagesValue, gbc_lblImagesValue);

		JLabel lblAnnotations = new JLabel("Annotations");
		lblAnnotations.setFocusable(false);
		lblAnnotations.setHorizontalAlignment(SwingConstants.RIGHT);
		lblAnnotations.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblAnnotations = new GridBagConstraints();
		gbc_lblAnnotations.anchor = GridBagConstraints.EAST;
		gbc_lblAnnotations.insets = new Insets(0, 0, 5, 10);
		gbc_lblAnnotations.gridx = 0;
		gbc_lblAnnotations.gridy = 5;
		panel.add(lblAnnotations, gbc_lblAnnotations);

		lblAnnotationsValue = new JTextArea("14");
		lblAnnotationsValue.setEditable(false);
		lblAnnotationsValue.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblAnnotationsValue.setOpaque(false);
		GridBagConstraints gbc_lblAnnotationsValue = new GridBagConstraints();
		gbc_lblAnnotationsValue.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblAnnotationsValue.insets = new Insets(0, 0, 5, 0);
		gbc_lblAnnotationsValue.gridx = 1;
		gbc_lblAnnotationsValue.gridy = 5;
		panel.add(lblAnnotationsValue, gbc_lblAnnotationsValue);

		JLabel lblUsers = new JLabel("Users");
		lblUsers.setFocusable(false);
		lblUsers.setHorizontalAlignment(SwingConstants.RIGHT);
		lblUsers.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblUsers = new GridBagConstraints();
		gbc_lblUsers.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblUsers.insets = new Insets(0, 0, 5, 10);
		gbc_lblUsers.gridx = 0;
		gbc_lblUsers.gridy = 6;
		panel.add(lblUsers, gbc_lblUsers);

		txtrUsers = new JTextArea();
		txtrUsers.setOpaque(false);
		txtrUsers.setSelectionColor(UIManager.getColor("TextArea.selectionBackground"));
		txtrUsers.setSelectedTextColor(UIManager.getColor("TextArea.selectionForeground"));
		txtrUsers.setEditable(false);
		txtrUsers.setLineWrap(true);
		txtrUsers.setBackground(UIManager.getColor("Panel.background"));
		txtrUsers.setFont(new Font("Tahoma", Font.PLAIN, 12));
		txtrUsers.setWrapStyleWord(true);
		txtrUsers.setText("Username 1\nUsername 2\nUsername 3");
		GridBagConstraints gbc_txtrUsers = new GridBagConstraints();
		gbc_txtrUsers.gridwidth = 1;
		gbc_txtrUsers.insets = new Insets(0, 0, 5, 0);
		gbc_txtrUsers.fill = GridBagConstraints.BOTH;
		gbc_txtrUsers.gridx = 1;
		gbc_txtrUsers.gridy = 6;
		panel.add(txtrUsers, gbc_txtrUsers);
	}

	/**
	 * Create the panel.
	 */
	public ProjectDetailsPanel(Project project) {
		this();
		setCurrentProject(project);
	}

	public void setCurrentProject(Project project) {
		this.currentProject = project;
		updateProjectDetails();
	}

	public Project getCurrentProject() {
		return currentProject;
	}

	public void updateProjectDetails() {
		if (getCurrentProject() != null) {
			String name = getCurrentProject().getName();
			if (name == null) name = "Unavailable";
			this.lblProjectName.setText(name);
			String description;
			try {
				description = getCurrentProject().getDescription();
			} catch (CytomineException e) {
				description = null;
			}
			if (description == null) description = "Unavailable";
			this.txtrProjectDescription.setText(description);
			String id = "" + getCurrentProject().getId();
			if (id.equals("null")) id = "Unavailable";
			this.lblIdValue.setText(id);
			String ontology = getCurrentProject().getOntologyName();
			if (ontology == null) ontology = "Unavailable";
			this.lblOntologyValue.setText(ontology);
			String numImages = "" + getCurrentProject().getNumberOfImages();
			if (numImages.equals("null")) numImages = "Unavailable";
			this.lblImagesValue.setText(numImages);
			String annotations = "" + getCurrentProject().getTotalNumberOfAnnotations();
			if (annotations.equals("null")) annotations = "Unavailable";
			this.lblAnnotationsValue.setText(annotations);
			String users;
			try {
				users = getCurrentProject().getUsers().stream().map(u -> u.getUserName())
						.collect(Collectors.joining(System.lineSeparator()));
			} catch (CytomineException e) {
				users = null;
			}
			if (users == null) users = "Unavailable";
			this.txtrUsers.setText(users);
		} else {
			this.lblProjectName.setText("Unavailable");
			this.txtrProjectDescription.setText("Unavailable");
			this.lblIdValue.setText("Unavailable");
			this.lblOntologyValue.setText("Unavailable");
			this.lblImagesValue.setText("Unavailable");
			this.lblAnnotationsValue.setText("Unavailable");
			this.txtrUsers.setText("Unavailable");
		}
	}

}
