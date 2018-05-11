package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.JPanel;

import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.model.Term;
import org.bioimageanalysis.icy.icytomine.core.model.User;
import org.bioimageanalysis.icy.icytomine.core.model.filters.AnnotationFilter;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.filter.AnnotationFilterPanel;

import com.google.common.base.Objects;

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

	Set<AnnotationsVisibilityListener> annotationsVisibilitylisteners;

	public AnnotationManagerPanel() {
		this(Image.getNoImage(null));
	}

	public AnnotationManagerPanel(Image imageInformation) {
		this.imageInformation = imageInformation;
		Map<Annotation, Boolean> annotationVisibility = createAnnotationVisibility(getImageAnnotations());

		setLayout(new BorderLayout(0, 0));

		filterPanel = new AnnotationFilterPanel(this.imageInformation);
		add(filterPanel, BorderLayout.NORTH);
		filterPanel.addUserSelectionListener((User user, boolean selected) -> userSelectionChange(user, selected));
		filterPanel.addTermSelectionListener((Term term, boolean selected) -> termSelectionChange(term, selected));

		annotationTable = new AnnotationTable(annotationVisibility);
		add(annotationTable, BorderLayout.CENTER);

		annotationsVisibilitylisteners = new HashSet<>();
	}

	private Map<Annotation, Boolean> createAnnotationVisibility(Set<Annotation> annotations) {
		Map<Annotation, Boolean> annotationVisibility;
		annotationVisibility = annotations.stream().collect(Collectors.toMap(Function.identity(), a -> true));
		return annotationVisibility;
	}

	private Set<Annotation> getImageAnnotations() {
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

	private void userSelectionChange(User user, boolean selected) {
		Set<Annotation> newVisibleAnnotations;
		if (selected) {
			newVisibleAnnotations = includeAnnotations(userAnnotations);
		} else {
			Set<Annotation> userAnnotations = annotationTable.getTableModel().getAnnotations().stream()
					.filter(a -> Objects.equal(a.getUser(), user)).collect(Collectors.toSet());
			newVisibleAnnotations = excludeAnnotations(userAnnotations);
		}

		Map<Annotation, Boolean> annotationVisibility = createAnnotationVisibility(newVisibleAnnotations);
		annotationTable.setTableModel(annotationVisibility);

		notifyAnnotationsVisibilityListeners(newVisibleAnnotations);
	}

	private void termSelectionChange(Term term, boolean selected) {
		Set<Annotation> termAnnotations = annotationTable.getTableModel().getAnnotations().stream()
				.filter(a -> getAnnotationTerms(a).contains(term)).collect(Collectors.toSet());
		Set<Annotation> newVisibleAnnotations;
		if (selected) {
			newVisibleAnnotations = includeAnnotations(termAnnotations);
		} else {
			newVisibleAnnotations = excludeAnnotations(termAnnotations);
		}

		Map<Annotation, Boolean> annotationVisibility = createAnnotationVisibility(newVisibleAnnotations);
		annotationTable.setTableModel(annotationVisibility);

		notifyAnnotationsVisibilityListeners(newVisibleAnnotations);
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

	private Set<Annotation> includeAnnotations(Set<Annotation> annotations) {
		Set<Annotation> newVisibleAnnotations = new HashSet<>(annotationTable.getTableModel().getAnnotations());
		newVisibleAnnotations.addAll(annotations);
		return newVisibleAnnotations;
	}

	private Set<Annotation> excludeAnnotations(Set<Annotation> userAnnotations) {
		Set<Annotation> newVisibleAnnotations = new HashSet<>(annotationTable.getTableModel().getAnnotations());
		newVisibleAnnotations.removeAll(annotations);
		return newVisibleAnnotations;
	}

	private void notifyAnnotationsVisibilityListeners(Set<Annotation> newVisibleAnnotations) {
		annotationsVisibilitylisteners.forEach(l -> l.annotationsVisibiliyChanged(newVisibleAnnotations));
	}

}
