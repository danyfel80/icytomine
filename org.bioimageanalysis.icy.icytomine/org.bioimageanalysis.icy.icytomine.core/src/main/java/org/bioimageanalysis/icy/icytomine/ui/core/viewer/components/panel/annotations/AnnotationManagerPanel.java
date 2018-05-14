package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations;

import java.awt.BorderLayout;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.JPanel;

import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.model.Term;
import org.bioimageanalysis.icy.icytomine.core.model.User;
import org.bioimageanalysis.icy.icytomine.core.model.filters.FilterAnnotationByTerm;
import org.bioimageanalysis.icy.icytomine.core.model.filters.FilterAnnotationByUser;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.filter.AnnotationFilterPanel;

import be.cytomine.client.CytomineException;

@SuppressWarnings("serial")
public class AnnotationManagerPanel extends JPanel {

	public interface AnnotationsVisibilityListener {
		void annotationsVisibiliyChanged(Set<Annotation> newVisibleAnnotations);
	}

	private AnnotationFilterPanel filterPanel;
	private AnnotationTable annotationTable;

	private Image imageInformation;
	private Set<Annotation> annotations;
	private Set<User> activeUsers;
	private Set<Term> activeTerms;

	Set<AnnotationsVisibilityListener> annotationsVisibilitylisteners;

	public AnnotationManagerPanel() {
		this(Image.getNoImage(null));
	}

	public AnnotationManagerPanel(Image imageInformation) {
		this.imageInformation = imageInformation;
		retrieveImageAnnotations();
		fillActiveUsers();
		fillActiveTerms();
		Map<Annotation, Boolean> annotationVisibility = createAnnotationVisibility(annotations);

		setLayout(new BorderLayout(0, 0));

		filterPanel = new AnnotationFilterPanel(this.imageInformation);
		add(filterPanel, BorderLayout.NORTH);
		filterPanel.addUserSelectionListener((User user, boolean selected) -> userSelectionChange(user, selected));
		filterPanel.addTermSelectionListener((Term term, boolean selected) -> termSelectionChange(term, selected));

		annotationTable = new AnnotationTable(annotationVisibility);
		add(annotationTable, BorderLayout.CENTER);
		annotationTable.addAnnotationVisibilityListener((Annotation annotation, boolean visible) -> annotationVisibilityChanged(annotation, visible));

		annotationsVisibilitylisteners = new HashSet<>();
	}

	private Set<Annotation> retrieveImageAnnotations() {
		if (annotations == null) {
			try {
				annotations = imageInformation.getAnnotations().stream().collect(Collectors.toSet());
			} catch (CytomineException e) {
				e.printStackTrace();
				annotations = new HashSet<>();
			}
		}
		return annotations;
	}

	private void fillActiveUsers() {
		activeUsers = new HashSet<>();
		annotations.forEach(annotation -> activeUsers.add(annotation.getUser()));
	}

	private void fillActiveTerms() {
		activeTerms = new HashSet<>();
		annotations.forEach(annotation -> activeTerms.addAll(getAnnotationTerms(annotation)));
	}

	private Set<Term> getAnnotationTerms(Annotation a) {
		Set<Term> terms;
		try {
			terms = new HashSet<>(a.getTerms());
		} catch (CytomineException e) {
			e.printStackTrace();
			terms = new HashSet<>();
		}
		return terms;
	}

	private Map<Annotation, Boolean> createAnnotationVisibility(Set<Annotation> annotations) {
		Map<Annotation, Boolean> annotationVisibility;
		annotationVisibility = annotations.stream().collect(Collectors.toMap(Function.identity(), a -> true));
		return annotationVisibility;
	}

	private void userSelectionChange(User user, boolean selected) {
		if (selected) {
			activeUsers.add(user);
		} else {
			activeUsers.remove(user);
		}

		FilterAnnotationByUser userFilter = new FilterAnnotationByUser(activeUsers);
		FilterAnnotationByTerm termFilter = new FilterAnnotationByTerm(activeTerms);

		Set<Annotation> newActiveAnnotations = annotations.stream().filter(a -> userFilter.apply(a))
				.filter(a -> termFilter.apply(a)).collect(Collectors.toSet());

		Map<Annotation, Boolean> annotationVisibility = createAnnotationVisibility(newActiveAnnotations);
		annotationTable.setTableModel(annotationVisibility);

		notifyAnnotationsVisibilityListeners(newActiveAnnotations);
	}

	private void termSelectionChange(Term term, boolean selected) {
		if (selected) {
			activeTerms.add(term);
		} else {
			activeTerms.remove(term);
		}

		FilterAnnotationByUser userFilter = new FilterAnnotationByUser(activeUsers);
		FilterAnnotationByTerm termFilter = new FilterAnnotationByTerm(activeTerms);

		Set<Annotation> newActiveAnnotations = annotations.stream().filter(a -> userFilter.apply(a))
				.filter(a -> termFilter.apply(a)).collect(Collectors.toSet());

		Map<Annotation, Boolean> annotationVisibility = createAnnotationVisibility(newActiveAnnotations);
		annotationTable.setTableModel(annotationVisibility);

		notifyAnnotationsVisibilityListeners(newActiveAnnotations);
	}

	private void notifyAnnotationsVisibilityListeners(Set<Annotation> newVisibleAnnotations) {
		annotationsVisibilitylisteners.forEach(l -> l.annotationsVisibiliyChanged(newVisibleAnnotations));
	}

	public void addAnnotationsVisibilityListener(AnnotationsVisibilityListener listener) {
		this.annotationsVisibilitylisteners.add(listener);
	}

	public void removeAnnotationsVisibilityListener(AnnotationsVisibilityListener listener) {
		this.annotationsVisibilitylisteners.remove(listener);
	}

	private void annotationVisibilityChanged(Annotation annotation, boolean visible) {
		notifyAnnotationsVisibilityListeners(annotationTable.getTableModel().getVisibleAnnotations());
	}

}
