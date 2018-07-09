package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.actions;

import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.bioimageanalysis.icy.icytomine.core.model.Term;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.actions.AnnotationTermSelectionPanelController.AnnotationTermSelectionCommitListener;

@SuppressWarnings("serial")
public class AnnotationActionPanel extends JPanel {

	private AnnotationTermSelectionPanel termSelectionPanel;
	private AnnotationDeletionPanel annotationDeletionPanel;
	private AnnotationActionPanelController panelController;

	public AnnotationActionPanel() {
		setView();
		setController();
	}

	private void setView() {
		setBorder(new TitledBorder(null, "Actions", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new GridLayout(2, 0, 0, 0));

		addTermSelectionPanel();
		addAnnotationDeletionPanel();
	}

	private void addTermSelectionPanel() {
		termSelectionPanel = new AnnotationTermSelectionPanel();
		add(termSelectionPanel);
	}

	private void addAnnotationDeletionPanel() {
		annotationDeletionPanel = new AnnotationDeletionPanel();
		add(annotationDeletionPanel);
	}

	private void setController() {
		panelController = new AnnotationActionPanelController(this);
	}

	public void setAvailableTerms(Collection<Term> terms) {
		panelController.setAvailableTerms(new HashSet<Term>(terms));
	}

	public AnnotationTermSelectionPanel getTermSelectionPanel() {
		return termSelectionPanel;
	}

	public void setSelectedTerms(Set<Term> selectedTerms) {
		panelController.setSelectedTerms(selectedTerms);
	}

	public void addTermSelectionCommitListener(AnnotationTermSelectionCommitListener listener) {
		panelController.addTermSelectionCommitListener(listener);
	}

	public void removeTermSelectionCommitListener(AnnotationTermSelectionCommitListener listener) {
		panelController.removeTermSelectionCommitListener(listener);
	}

	public void addAnnotationDeletionListener(ActionListener listener) {
		panelController.addAnnotationDeletionListener(listener);
	}

	public AnnotationDeletionPanel getAnnotationDeletionPanel() {
		return annotationDeletionPanel;
	}

	public void removeAnnotationDeletionListener(ActionListener listener) {
		panelController.removeAnnotationDeletionListener(listener);
	}
}
