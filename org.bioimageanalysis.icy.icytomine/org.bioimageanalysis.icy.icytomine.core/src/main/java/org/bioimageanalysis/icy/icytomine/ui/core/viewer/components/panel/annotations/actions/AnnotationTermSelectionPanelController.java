package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.actions;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

import org.bioimageanalysis.icy.icytomine.core.model.Term;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.actions.AnnotationTermSelectionPanel.TermItem;
import org.bioimageanalysis.icy.icytomine.ui.general.JCheckableItem;

public class AnnotationTermSelectionPanelController {
	public interface AnnotationTermSelectionCommitListener {
		void termsChanged(Set<Term> selection);
	}

	private AnnotationTermSelectionPanel panel;
	private Set<TermItem> availableTerms;
	private Set<AnnotationTermSelectionCommitListener> termSelectionCommitListeners;

	public AnnotationTermSelectionPanelController(AnnotationTermSelectionPanel panel) {
		this.panel = panel;
		this.availableTerms = new HashSet<>();
		this.termSelectionCommitListeners = new HashSet<>();
		commitTermButtonHandler();
	}

	private void commitTermButtonHandler() {
		panel.getTermSetButton().addActionListener(event -> notifyAnnotationTermChange());
	}

	private void notifyAnnotationTermChange() {
		Set<Term> selection = getSelectedTerms();
		termSelectionCommitListeners.forEach(listener -> listener.termsChanged(selection));
	}

	private Set<Term> getSelectedTerms() {
		Set<Term> selectedTerms = new HashSet<>();
		ComboBoxModel<JCheckableItem<TermItem>> model = panel.getTermComboBox().getModel();
		for (int i = 0; i < model.getSize(); i++) {
			JCheckableItem<TermItem> item = model.getElementAt(i);
			if (item.isSelected() && item.getObject().getTerm() != null)
				selectedTerms.add(item.getObject().getTerm());
		}
		return selectedTerms;
	}

	public void setAvailableTerms(Set<Term> terms) {
		availableTerms = terms.stream().map(term -> new TermItem(term)).collect(Collectors.toSet());
		availableTerms.add(TermItem.NO_TERM);

		panel.getTermComboBox().setModel(getTermModel());
		panel.getTermComboBox().setSelectedItem(TermItem.NO_TERM);
	}

	private ComboBoxModel<JCheckableItem<TermItem>> getTermModel() {
		@SuppressWarnings("unchecked")
		ComboBoxModel<JCheckableItem<TermItem>> model = new DefaultComboBoxModel<>(
				availableTerms.stream().map(termItem -> new JCheckableItem<TermItem>(termItem, termItem.toString(), false))
						.toArray(JCheckableItem[]::new));
		return model;
	}

	public void addTermSelectionCommitListener(AnnotationTermSelectionCommitListener listener) {
		this.termSelectionCommitListeners.add(listener);
	}

	public void removeTermSelectionCommitListener(AnnotationTermSelectionCommitListener listener) {
		this.termSelectionCommitListeners.remove(listener);
	}

	public void setSelectedTerms(Set<Term> terms) {
		if (terms.isEmpty()) {
			terms = new HashSet<>(1);
			terms.add(null);
		}

		ComboBoxModel<JCheckableItem<TermItem>> model = panel.getTermComboBox().getModel();
		for (int i = 0; i < model.getSize(); i++) {
			JCheckableItem<TermItem> checkableItem = model.getElementAt(i);
			if (terms.contains(checkableItem.getObject().getTerm())) {
				checkableItem.setSelected(true);
			} else {
				checkableItem.setSelected(false);
			}
		}
		panel.getTermComboBox().updateUI();
	}
}
