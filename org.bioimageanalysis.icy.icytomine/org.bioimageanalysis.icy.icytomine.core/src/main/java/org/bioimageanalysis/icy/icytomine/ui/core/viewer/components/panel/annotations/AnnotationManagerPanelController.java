package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;
import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.model.Term;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.AnnotationManagerPanel.AnnotationsVisibilityListener;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.AnnotationTable.AnnotationSelectionListener;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.actions.AnnotationActionPanel;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.actions.AnnotationTermSelectionPanelController.AnnotationTermSelectionCommitListener;

import icy.gui.dialog.MessageDialog;

public class AnnotationManagerPanelController {

	public interface AnnotationTermCommitListener {
		void annotationTermCommited(Set<Annotation> annotations, Map<Term, Boolean> terms);
	}

	private AnnotationManagerPanel panel;
	private Image imageInformation;

	private Set<AnnotationsVisibilityListener> annotationsVisibilitylisteners;
	private Set<AnnotationTermCommitListener> annotationTermSelectionCommitListeners;

	public AnnotationManagerPanelController(AnnotationManagerPanel panel, Image imageInformation) {
		this.panel = panel;
		this.imageInformation = imageInformation;

		setupListeners();
		setupAnnotationFilterPanel();
		setupAnnotationTable();
		setupAnnotationActionPanel();
	}

	private void setupListeners() {
		annotationsVisibilitylisteners = new HashSet<>();
		annotationTermSelectionCommitListeners = new HashSet<>();

		panel.getAnnotationTable().addAnnotationVisibilityListener(
				(Annotation annotation, boolean visible) -> annotationVisibilityChanged(annotation, visible));

		panel.getFilteringPanel()
				.addAnnotationFilterUpdateListener(activeAnnotations -> activeAnnotationsUpdated(activeAnnotations));
	}

	private void annotationVisibilityChanged(Annotation annotation, boolean visible) {
		notifyAnnotationsVisibilityListeners(panel.getAnnotationTable().getTableModel().getVisibleAnnotations());
	}

	private void notifyAnnotationsVisibilityListeners(Set<Annotation> newVisibleAnnotations) {
		annotationsVisibilitylisteners.forEach(l -> l.annotationsVisibiliyChanged(newVisibleAnnotations));
	}

	private void activeAnnotationsUpdated(Set<Annotation> activeAnnotations) {
		Map<Annotation, Boolean> currentVisibility = panel.getAnnotationTable().getTableModel().getAnnotationVisibility();
		Map<Annotation, Boolean> annotationVisibility = createAnnotationVisibility(activeAnnotations, currentVisibility);
		panel.getAnnotationTable().setTableModel(annotationVisibility);
		Set<Annotation> visibleAnnotations = annotationVisibility.keySet().stream().filter(a -> annotationVisibility.get(a))
				.collect(Collectors.toSet());
		notifyAnnotationsVisibilityListeners(visibleAnnotations);
	}

	private Map<Annotation, Boolean> createAnnotationVisibility(Set<Annotation> annotations,
			Map<Annotation, Boolean> currentVisibility) {
		Map<Annotation, Boolean> annotationVisibility = annotations.parallelStream()
				.collect(Collectors.toMap(Function.identity(), a -> {
					if (currentVisibility.containsKey(a)) {
						return currentVisibility.get(a);
					} else {
						return true;
					}
				}));
		return annotationVisibility;
	}

	private void setupAnnotationFilterPanel() {
		panel.getFilteringPanel().setImageInformation(imageInformation);
	}

	private void setupAnnotationTable() {
		AnnotationTable annotationTable = panel.getAnnotationTable();
		Map<Annotation, Boolean> annotationVisibility = createAnnotationVisibility(
				panel.getFilteringPanel().getActiveAnnotations(), new HashMap<>(0));
		annotationTable.setTableModel(annotationVisibility);
		annotationTable.addAnnotationSelectionListener(getAnnotationSelectionChangeHandler());
	}

	private AnnotationSelectionListener getAnnotationSelectionChangeHandler() {
		return (Set<Annotation> selectedAnnotations) -> {
			Set<Term> selectedTerms = getUserTermsFromAnnotations(selectedAnnotations);
			panel.getActionPanel().setSelectedTerms(selectedTerms);
		};
	}

	private Set<Term> getUserTermsFromAnnotations(Set<Annotation> selectedAnnotations) {
		Set<Term> userTerms = selectedAnnotations.stream()
				.map(a -> a.getAssociatedTermsByUser(a.getClient().getCurrentUser().getId())).flatMap(Set::stream)
				.collect(Collectors.toSet());
		return userTerms;
	}

	private void setupAnnotationActionPanel() {
		AnnotationActionPanel actionPanel = panel.getActionPanel();
		actionPanel.setAvailableTerms(getAvailableTerms());
		actionPanel.addTermSelectionCommitListener(getAnnotationTermSelectionCommitHandler());
	}

	private AnnotationTermSelectionCommitListener getAnnotationTermSelectionCommitHandler() {
		return (Set<Term> selectedTerms) -> {
			Set<Annotation> selectedAnnotations = getSelectedAnnotations();
			if (selectedAnnotations.isEmpty()) {
				MessageDialog.showDialog("Associating terms to annotations - Icytomine", "No annotation selected",
						MessageDialog.WARNING_MESSAGE);
				return;
			}

			Map<Term, Boolean> termSelection = getAvailableTerms().stream()
					.collect(Collectors.toMap(term -> term, term -> selectedTerms.contains(term)));

			notifyAnnotationTermSelectionCommitted(selectedAnnotations, termSelection);
		};
	}

	private void notifyAnnotationTermSelectionCommitted(Set<Annotation> selectedAnnotations,
			Map<Term, Boolean> termSelection) {
		annotationTermSelectionCommitListeners
				.forEach(listener -> listener.annotationTermCommited(selectedAnnotations, termSelection));
	}

	private Set<Annotation> getSelectedAnnotations() {
		return panel.getAnnotationTable().getSelectedAnnotations();
	}

	private Collection<Term> getAvailableTerms() {
		Collection<Term> terms;
		try {
			terms = imageInformation.getProject().getOntology().getTerms(false);
		} catch (CytomineClientException e) {
			e.printStackTrace();
			terms = new ArrayList<Term>(0);
		}
		return terms;
	}

	public void addAnnotationsVisibilityListener(AnnotationsVisibilityListener listener) {
		this.annotationsVisibilitylisteners.add(listener);
	}

	public void removeAnnotationsVisibilityListener(AnnotationsVisibilityListener listener) {
		this.annotationsVisibilitylisteners.remove(listener);
	}

	public void addAnnotationTermSelectionCommitListener(AnnotationTermCommitListener listener) {
		this.annotationTermSelectionCommitListeners.add(listener);
	}

	public void removeAnnotationTermSelectionCommitListener(AnnotationTermCommitListener listener) {
		this.annotationTermSelectionCommitListeners.remove(listener);
	}
}
