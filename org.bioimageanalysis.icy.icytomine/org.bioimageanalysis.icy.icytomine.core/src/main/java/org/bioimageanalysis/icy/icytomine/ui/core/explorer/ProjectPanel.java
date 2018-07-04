package org.bioimageanalysis.icy.icytomine.ui.core.explorer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.SystemColor;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;
import org.bioimageanalysis.icy.icytomine.core.model.Project;

public class ProjectPanel extends JPanel {

	public static class ProjectItem {
		private Project project;

		public ProjectItem(Project project) {
			this.project = project;
		}

		public Project getProject() {
			return this.project;
		}

		@Override
		public String toString() {
			return project.getName().orElse(String.format("Not specified (id=%d)", project.getId().longValue()));
		}
	}

	@FunctionalInterface
	public interface ProjectSelectionListener {
		public void projectSelected(Project p);
	}

	private static final long serialVersionUID = 5990256964181871478L;

	private CytomineClient client;

	private JList<ProjectItem> listProjects;

	/**
	 * Create empty panel. To fill with cytomine details use
	 * {@link #setClient(CytomineClient)}.
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

	public void updateProjectList() throws CytomineClientException {
		List<Project> projectCollection = client.getUserProjects(client.getCurrentUser().getId());
		ProjectItem[] projects = projectCollection.stream().map(p -> new ProjectItem(p)).toArray(ProjectItem[]::new);
		listProjects.setListData(projects);
		listProjects.clearSelection();
		listProjects.setSelectedIndex(-1);
	}

	public void setClient(CytomineClient client) {
		this.client = client;
		try {
			updateProjectList();
		} catch (CytomineClientException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void addProjectSelectionListener(ProjectSelectionListener listener) {
		listProjects.addListSelectionListener(event -> {
			if (!event.getValueIsAdjusting()) {
				listener.projectSelected(((JList<ProjectItem>) event.getSource()).getSelectedValue().getProject());
			}
		});
	}

}
