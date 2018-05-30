package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.AnnotationManagerPanel.AnnotationsVisibilityListener;

public class AnnotationManagerPanelController {

	private AnnotationManagerPanel panel;
	private Image imageInformation;

	private Set<AnnotationsVisibilityListener> annotationsVisibilitylisteners;

	public AnnotationManagerPanelController(AnnotationManagerPanel panel, Image imageInformation) {
		this.panel = panel;
		this.imageInformation = imageInformation;

		setupListeners();
		setupAnnotationFilterPanel();
		setupAnnotationTable();
	}

	private void setupListeners() {
		annotationsVisibilitylisteners = new HashSet<>();

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
		Map<Annotation, Boolean> annotationVisibility = createAnnotationVisibility(
				panel.getFilteringPanel().getActiveAnnotations(), new HashMap<>(0));
		panel.getAnnotationTable().setTableModel(annotationVisibility);
	}

	public void addAnnotationsVisibilityListener(AnnotationsVisibilityListener listener) {
		this.annotationsVisibilitylisteners.add(listener);
	}

	public void removeAnnotationsVisibilityListener(AnnotationsVisibilityListener listener) {
		this.annotationsVisibilitylisteners.remove(listener);
	}
}
