package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.actions;

import java.util.Set;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

import org.bioimageanalysis.icy.icytomine.core.model.Term;
import org.bioimageanalysis.icy.icytomine.ui.general.JCheckableItem;

public class AnnotationTermSelectionPanelController {
	private AnnotationTermSelectionPanel panel;

	public AnnotationTermSelectionPanelController(AnnotationTermSelectionPanel panel) {
		this.panel = panel;
		setTermSetButtonHandler();
	}

	private void setTermSetButtonHandler() {
		panel.getTermSetButton().addActionListener(event -> setTerms());
	}

	private void setTerms() {
		// TODO implement
	}

	public void setAvailableTerms(Set<Term> terms) {
		panel.getTermComboBox().setModel(getTermModel(terms));
	}

	private ComboBoxModel<JCheckableItem<Term>> getTermModel(Set<Term> terms) {
		@SuppressWarnings("unchecked")
		ComboBoxModel<JCheckableItem<Term>> model = new DefaultComboBoxModel<JCheckableItem<Term>>(
				terms.stream().map(t -> new JCheckableItem<Term>(t, t.getName(), false)).toArray(JCheckableItem[]::new));
		return model;
	}
}
