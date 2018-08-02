package org.bioimageanalysis.icy.icytomine.ui.core.explorer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;
import org.bioimageanalysis.icy.icytomine.core.model.Project;
import org.bioimageanalysis.icy.icytomine.ui.core.explorer.ProjectPanel.ProjectItem;
import org.bioimageanalysis.icy.icytomine.ui.core.explorer.ProjectPanel.ProjectSelectionListener;

public class ProjectPanelController {

	private ProjectPanel panel;
	private Optional<CytomineClient> client;
	private Map<ProjectSelectionListener, ListSelectionListener> listSelectionListeners;

	private Set<Project> availableProjects;
	private Project currentProject;

	public ProjectPanelController(ProjectPanel projectPanel) {
		this.panel = projectPanel;
		this.client = Optional.ofNullable(null);
		this.listSelectionListeners = new HashMap<>();

		this.availableProjects = new HashSet<>(0);
		this.currentProject = null;

		panel.getSearchBar().getDocument().addDocumentListener(getSearchBarChangeHandler());
	}

	private DocumentListener getSearchBarChangeHandler() {
		return new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateProjectList();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateProjectList();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateProjectList();
			}
		};
	}

	public void updateProjectList() {
		applySearchCriterion();
		ProjectItem[] projects = getProjectItemArray(availableProjects);

		panel.getProjectList().setValueIsAdjusting(true);
		panel.getProjectList().setListData(projects);
		if (availableProjects.size() == 1) {
			panel.getProjectList().setSelectedValue(projects[0], true);
		} else if (currentProject != null && availableProjects.contains(currentProject)) {
			panel.getProjectList().setSelectedValue(new ProjectItem(currentProject), true);
		} else {
			panel.getProjectList().clearSelection();
		}
		panel.getProjectList().setValueIsAdjusting(false);
	}

	private void applySearchCriterion() {
		Pattern searchPattern = getSearchPattern();
		availableProjects = getClientProjects().stream()
				.filter(p -> searchPattern.matcher(p.getName().orElse("not available").toLowerCase()).matches())
				.collect(Collectors.toSet());
	}

	private Pattern getSearchPattern() {
		String searchCriterion = panel.getSearchBar().getText();
		if (searchCriterion.isEmpty()) {
			return Pattern.compile(".*");
		}
		return Pattern.compile(".*" + searchCriterion.toLowerCase() + ".*");
	}

	private List<Project> getClientProjects() {
		if (client.isPresent()) {
			return client.get().getCurrentUser().getProjects(false);
		}
		return new ArrayList<>(0);
	}

	private ProjectItem[] getProjectItemArray(Collection<Project> projectCollection) {
		return projectCollection.stream().map(p -> new ProjectItem(p)).toArray(ProjectItem[]::new);
	}

	public void addProjectSelectionListener(ProjectSelectionListener listener) {
		ListSelectionListener listListener = createListSelectionListener(listener);
		listSelectionListeners.put(listener, listListener);
		panel.getProjectList().addListSelectionListener(listListener);
	}

	private ListSelectionListener createListSelectionListener(ProjectSelectionListener listener) {
		return event -> {
			if (!event.getValueIsAdjusting()) {
				Project project = panel.getSelectedProject();
				if (!Objects.equals(currentProject, project)) {
					currentProject = project;
					listener.projectSelected(currentProject);
				}
			}
		};
	}

	public void removeProjectSelectionListener(ProjectSelectionListener listener) {
		ListSelectionListener listListener = listSelectionListeners.remove(listener);
		if (listListener != null) {
			panel.getProjectList().removeListSelectionListener(listListener);
		}
	}

	public void setClient(CytomineClient client) {
		this.client = Optional.ofNullable(client);
		try {
			updateProjectList();
		} catch (CytomineClientException e) {
			e.printStackTrace();
		}
	}

}
