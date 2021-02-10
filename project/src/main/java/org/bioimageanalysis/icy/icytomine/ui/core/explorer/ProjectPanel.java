package org.bioimageanalysis.icy.icytomine.ui.core.explorer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;
import org.bioimageanalysis.icy.icytomine.core.model.Project;

public class ProjectPanel extends JPanel {
	private static final long serialVersionUID = 5990256964181871478L;

	public static class ProjectItem {
		private Project project;

		public ProjectItem(Project project) {
			this.project = project;
		}

		public Project getProject() {
			return this.project;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (!(obj instanceof ProjectItem))
				return false;
			ProjectItem other = (ProjectItem) obj;
			return getProject().equals(other.getProject());
		}

		@Override
		public String toString() {
			return project.getName().orElse(String.format("Not specified (id=%d)", project.getId().longValue()));
		}
	}

	private static GridBagConstraints getConstraints(int x, int y, int width, int height, Insets insets, int fill) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = fill;
		constraints.gridx = x;
		constraints.gridy = y;
		constraints.gridwidth = width;
		constraints.gridheight = height;
		constraints.insets = insets;
		return constraints;
	}

	@FunctionalInterface
	public interface ProjectSelectionListener {
		public void projectSelected(Project p);
	}

	private JLabel titleLabel;
	private JTextField searchBar;
	private JList<ProjectItem> projectList;

	private ProjectPanelController controller;

	/**
	 * Create empty panel. To fill with cytomine details use
	 * {@link #setClient(CytomineClient)}.
	 */
	public ProjectPanel() {
		setView();
		setController();
	}

	private void setView() {
		setMinimumSize(new Dimension(50, 50));
		setPreferredSize(new Dimension(240, 400));
		setGridBagLayout();

		addPanelTitle();
		addSearchBar();
		addProjectList();
	}

	private void setGridBagLayout() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {0, 0};
		gridBagLayout.rowHeights = new int[] {0, 0, 0};
		gridBagLayout.columnWeights = new double[] {0.0, 1.0};
		gridBagLayout.rowWeights = new double[] {0.0, 0.0, 1.0};
		setLayout(gridBagLayout);
	}

	private void addPanelTitle() {
		buildPanelTitle();
		add(titleLabel, getConstraints(0, 0, 2, 1, new Insets(0, 3, 3, 3), GridBagConstraints.BOTH));
	}

	private void buildPanelTitle() {
		titleLabel = new JLabel("Projects");
		titleLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		titleLabel.setToolTipText("Projects available in this host");
		titleLabel.setBackground(SystemColor.control);
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
	}

	private void addSearchBar() {
		JLabel searchBarTitle = new JLabel("Search:");
		add(searchBarTitle, getConstraints(0, 1, 1, 1, new Insets(0, 3, 3, 0), GridBagConstraints.BOTH));
		buildSearchBar();
		add(searchBar, getConstraints(1, 1, 1, 1, new Insets(0, 3, 3, 3), GridBagConstraints.BOTH));
	}

	private void buildSearchBar() {
		searchBar = new JTextField();
		searchBar.setToolTipText("Search projects by their name");
	}

	private void addProjectList() {
		JScrollPane scrollPane = createProjectScrollPane();
		add(scrollPane, getConstraints(0, 2, 2, 1, new Insets(0, 3, 3, 3), GridBagConstraints.BOTH));
	}

	private JScrollPane createProjectScrollPane() {
		JScrollPane scrollPane = new JScrollPane();
		buildProjectList();
		scrollPane.setViewportView(projectList);
		return scrollPane;
	}

	private void buildProjectList() {
		projectList = new JList<>();
		projectList.setBackground(SystemColor.window);
		projectList.setModel(new DefaultListModel<>());
		projectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		titleLabel.setLabelFor(projectList);
	}

	private void setController() {
		this.controller = new ProjectPanelController(this);
	}

	public void addProjectSelectionListener(ProjectSelectionListener listener) {
		controller.addProjectSelectionListener(listener);
	}

	public void removeProjectSelectionListener(ProjectSelectionListener listener) {
		controller.removeProjectSelectionListener(listener);
	}

	public void setClient(CytomineClient client) {
		controller.setClient(client);
	}

	public JTextField getSearchBar() {
		return searchBar;
	}

	public JList<ProjectItem> getProjectList() {
		return projectList;
	}

	/**
	 * @return Selected project or null if none is selected.
	 */
	public Project getSelectedProject() {
		if (getProjectList().isSelectionEmpty()) {
			return null;
		} else {
			return getProjectList().getSelectedValue().getProject();
		}
	}

	public void updateProjectList() throws CytomineClientException {
		controller.updateProjectList();
	}
}
