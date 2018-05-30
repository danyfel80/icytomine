package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.actions;

import java.util.Set;

import org.bioimageanalysis.icy.icytomine.core.model.Term;

public class AnnotationActionPanelController {

	private AnnotationActionPanel panel;

	public AnnotationActionPanelController(AnnotationActionPanel panel) {
		this.panel = panel;
	}

	public void setAvailableTerms(Set<Term> terms) {
		panel.getTermSelectionPanel().setAvailableTerms(terms);
	}
}
