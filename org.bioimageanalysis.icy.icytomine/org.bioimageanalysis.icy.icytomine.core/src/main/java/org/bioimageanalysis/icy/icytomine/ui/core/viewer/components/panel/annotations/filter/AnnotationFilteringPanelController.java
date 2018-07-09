package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.filter;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;
import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.model.Term;
import org.bioimageanalysis.icy.icytomine.core.model.User;
import org.bioimageanalysis.icy.icytomine.core.model.filters.AnnotationFilter;
import org.bioimageanalysis.icy.icytomine.core.model.filters.AnnotationFilter.AnnotationFilterUpdateListener;
import org.bioimageanalysis.icy.icytomine.core.model.filters.AnnotationFilter.ComputationMode;
import org.bioimageanalysis.icy.icytomine.core.model.filters.AnnotationFilterInput;

public class AnnotationFilteringPanelController {

	private AnnotationFilteringPanel panel;

	private Set<AnnotationFilterUpdateListener> filteringListeners;

	private AnnotationFilterInput filterInput;
	private LinkedList<AnnotationFilter> filters;
	private Image imageInformation;

	public AnnotationFilteringPanelController(AnnotationFilteringPanel panel) {
		this.panel = panel;
		filteringListeners = new HashSet<>();
		filterInput = new AnnotationFilterInput();
		filters = new LinkedList<>();
		filters.add(filterInput);
		panel.addFilterAdditionListener((String filterName) -> addFilterPanel(filterName));
	}

	public void setImageInformation(Image imageInformation) {
		this.imageInformation = imageInformation;
		Set<Annotation> annotations;
		try {
			annotations = new HashSet<>(imageInformation.getAnnotations(false));
		} catch (CytomineClientException e) {
			e.printStackTrace();
			annotations = new HashSet<>();
		}
		filterInput.setActiveAnnotations(annotations);
		filterInput.computeActiveAnnotations(ComputationMode.USE_LAST_RESULT);
	}

	private synchronized void addFilterPanel(String filterName) {
		AnnotationFilterPanel<?> filterPanel = configureFilterPanel(filterName);
		panel.addFilterPanel(filterPanel);
	}

	private AnnotationFilterPanel<?> configureFilterPanel(String filterName) {
		AnnotationFilterPanel<?> filterPanel = createFilterPanel(filterName);
		filterPanel.addRemoveButtonListener(event -> removeFilter(filterPanel));

		AnnotationFilter lastFilter = getLastFilter();
		AnnotationFilter currentFilter = filterPanel.getFilter();
		for (AnnotationFilterUpdateListener listener : filteringListeners) {
			lastFilter.removeAnnotationFilterUpdateListener(listener);
			currentFilter.addAnnotationFilterUpdateListener(listener);
		}
		filters.add(currentFilter);

		return filterPanel;
	}

	private AnnotationFilterPanel<?> createFilterPanel(String filterName) {
		AnnotationFilterPanel<?> filterPanel = null;
		AnnotationFilter lastFilter = getLastFilter();
		if (Objects.equals(filterName, "User")) {
			AnnotationFilterByUserPanel userFilterPanel = new AnnotationFilterByUserPanel();
			userFilterPanel.setPreviousFilter(lastFilter);
			userFilterPanel.setUsers(getImageUsers());
			filterPanel = userFilterPanel;
		} else if (Objects.equals(filterName, "Term")) {
			AnnotationFilterByTermPanel termFilterPanel = new AnnotationFilterByTermPanel();
			termFilterPanel.setPreviousFilter(lastFilter);
			termFilterPanel.setTerms(getImageTerms());
			filterPanel = termFilterPanel;
		}
		return filterPanel;
	}

	private void removeFilter(AnnotationFilterPanel<?> filterPanel) {
		panel.removeFilterPanel(filterPanel);
		AnnotationFilter filter = filterPanel.getFilter();
		filter.disconnect();
		filters.remove(filter);
	}

	private Set<User> getImageUsers() {
		try {
			return new HashSet<>(imageInformation.getAnnotationUsers());
		} catch (Exception e) {
			e.printStackTrace();
			return new HashSet<>(0);
		}
	}

	private Set<Term> getImageTerms() {
		try {
			return imageInformation.getProject().getOntology().getTerms(false);
		} catch (CytomineClientException e) {
			e.printStackTrace();
			return new HashSet<>(0);
		}
	}

	public AnnotationFilter getLastFilter() {
		return filters.getLast();
	}

	public void addAnnotationFilterUpdateListener(AnnotationFilterUpdateListener listener) {
		this.filteringListeners.add(listener);
	}

	public void removeAnnotationFilterUpdateListener(AnnotationFilterUpdateListener listener) {
		this.filteringListeners.remove(listener);
	}

	public Set<Annotation> getActiveAnnotations() {
		return getLastFilter().getActiveAnnotations(ComputationMode.USE_LAST_RESULT);
	}
}
