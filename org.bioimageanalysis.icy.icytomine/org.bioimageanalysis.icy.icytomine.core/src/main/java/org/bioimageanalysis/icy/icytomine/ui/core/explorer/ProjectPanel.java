package org.bioimageanalysis.icy.icytomine.ui.core.explorer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.SystemColor;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import org.bioimageanalysis.icy.icytomine.core.model.Project;

import be.cytomine.client.Cytomine;
import be.cytomine.client.CytomineException;
import be.cytomine.client.collections.ProjectCollection;

public class ProjectPanel extends JPanel {
	@FunctionalInterface
	public interface ProjectSelectionListener {
		public void projectSelected(Project p);
	}

	private static final long serialVersionUID = 5990256964181871478L;

	private Cytomine cytomine;

	private JList<Project> listProjects;

	/**
	 * Create empty panel. To fill with cytomine details use
	 * {@link #ProjectPanel(Cytomine)}.
	 */
	public ProjectPanel() {
		setMinimumSize(new Dimension(50, 50));
		setPreferredSize(new Dimension(240, 400));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JLabel lblProjects = new JLabel("Projects");
		lblProjects.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblProjects.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(lblProjects);
		lblProjects.setToolTipText("Projects available in this host");
		lblProjects.setBackground(SystemColor.control);
		lblProjects.setHorizontalAlignment(SwingConstants.CENTER);

		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane);
		listProjects = new JList<>();
		listProjects.setBackground(SystemColor.window);
		listProjects.setModel(new DefaultListModel<>());
		listProjects.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lblProjects.setLabelFor(listProjects);
		scrollPane.setViewportView(listProjects);
	}

	/**
	 * Create the panel.
	 */
	public ProjectPanel(Cytomine cytomine) {
		this();
		setCytomine(cytomine);
	}

	public void updateProjectList() throws CytomineException {
		ProjectCollection projectCollection = cytomine.getProjectsByUser(cytomine.getCurrentUser().getId());
		Project[] projects = new Project[projectCollection.size()];
		for (int i = 0; i < projectCollection.size(); i++) {
			be.cytomine.client.models.Project project = projectCollection.get(i);
			projects[i] = new Project(project, cytomine);
		}
		listProjects.setListData(projects);
		listProjects.clearSelection();
		listProjects.setSelectedIndex(-1);
	}

	public void setCytomine(Cytomine cytomine) {
		this.cytomine = cytomine;
		try {
			updateProjectList();
		} catch (CytomineException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void addProjectSelectionListener(ProjectSelectionListener listener) {
		listProjects.addListSelectionListener(e -> listener.projectSelected(((JList<Project>)e.getSource()).getSelectedValue()));
	}

}
