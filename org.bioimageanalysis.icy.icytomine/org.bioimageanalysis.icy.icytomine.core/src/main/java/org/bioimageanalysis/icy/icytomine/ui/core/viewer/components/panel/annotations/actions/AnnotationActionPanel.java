package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.actions;

import java.awt.GridLayout;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.bioimageanalysis.icy.icytomine.core.model.Term;

@SuppressWarnings("serial")
public class AnnotationActionPanel extends JPanel {

	private AnnotationTermSelectionPanel termSelectionPanel;

	private AnnotationActionPanelController panelController;

	public AnnotationActionPanel() {
		setView();
		setController();
	}

	private void setView() {
		setBorder(new TitledBorder(null, "Actions", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new GridLayout(1, 0, 0, 0));

		addTermSelectionPanel();
	}

	private void addTermSelectionPanel() {
		termSelectionPanel = new AnnotationTermSelectionPanel();
		add(termSelectionPanel);
	}

	private void setController() {
		panelController = new AnnotationActionPanelController(this);
	}

	public void setAvailableTerms(Set<Term> terms) {
		panelController.setAvailableTerms(terms);
	}

	public AnnotationTermSelectionPanel getTermSelectionPanel() {
		return termSelectionPanel;
	}
}
