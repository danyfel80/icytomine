package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.actions;

import java.util.Set;

import org.bioimageanalysis.icy.icytomine.core.model.Term;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.actions.AnnotationTermSelectionPanelController.AnnotationTermSelectionCommitListener;

public class AnnotationActionPanelController {

	private AnnotationActionPanel panel;

	public AnnotationActionPanelController(AnnotationActionPanel panel) {
		this.panel = panel;
	}

	public void setAvailableTerms(Set<Term> terms) {
		panel.getTermSelectionPanel().setAvailableTerms(terms);
	}

	public void setSelectedTerms(Set<Term> terms) {
		panel.getTermSelectionPanel().setSelectedTerms(terms);
	}
	
	public void addTermSelectionCommitListener(AnnotationTermSelectionCommitListener listener) {
		panel.getTermSelectionPanel().addTermSelectionCommitListener(listener);
	}
}
