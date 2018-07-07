package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations;

import java.awt.BorderLayout;
import java.util.Set;

import javax.swing.JPanel;

import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.AnnotationManagerPanelController.AnnotationTermCommitListener;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.AnnotationTable.AnnotationDoubleClickListener;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.AnnotationTable.AnnotationSelectionListener;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.actions.AnnotationActionPanel;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.filter.AnnotationFilteringPanel;

@SuppressWarnings("serial")
public class AnnotationManagerPanel extends JPanel {

	public interface AnnotationsVisibilityListener {
		void annotationsVisibiliyChanged(Set<Annotation> newVisibleAnnotations);
	}

	private AnnotationFilteringPanel filterPanel;
	private AnnotationTable annotationTable;
	private AnnotationActionPanel actionPanel;

	private AnnotationManagerPanelController panelController;

	public AnnotationManagerPanel() {
		this(null);
	}

	public AnnotationManagerPanel(Image imageInformation) {
		setView();
		setController(imageInformation);
	}

	private void setView() {
		setLayout(new BorderLayout(0, 0));
		addFilterPanel();
		addAnnotationPanel();
		addActionPanel();
	}

	private void addFilterPanel() {
		filterPanel = new AnnotationFilteringPanel();
		add(filterPanel, BorderLayout.NORTH);
	}

	private void addAnnotationPanel() {
		annotationTable = new AnnotationTable();
		add(annotationTable, BorderLayout.CENTER);
	}

	private void addActionPanel() {
		actionPanel = new AnnotationActionPanel();
		add(actionPanel, BorderLayout.SOUTH);
	}

	private void setController(Image imageInformation) {
		panelController = new AnnotationManagerPanelController(this, imageInformation);
	}

	public void addAnnotationsVisibilityListener(AnnotationsVisibilityListener listener) {
		panelController.addAnnotationsVisibilityListener(listener);
	}

	public void removeAnnotationsVisibilityListener(AnnotationsVisibilityListener listener) {
		panelController.removeAnnotationsVisibilityListener(listener);
	}

	public void addAnnotationSelectionListener(AnnotationSelectionListener listener) {
		annotationTable.addAnnotationSelectionListener(listener);
	}

	public void removeAnnotationSelectionListener(AnnotationSelectionListener listener) {
		annotationTable.removeAnnotationSelectionListener(listener);
	}

	public void addAnnotationDoubleClickListener(AnnotationDoubleClickListener listener) {
		annotationTable.addAnnotationDoubleClickListener(listener);
	}

	public void removeAnnotationDoubleClickListener(AnnotationDoubleClickListener listener) {
		annotationTable.removeAnnotationDoubleClickListener(listener);
	}

	protected AnnotationFilteringPanel getFilteringPanel() {
		return filterPanel;
	}

	protected AnnotationTable getAnnotationTable() {
		return annotationTable;
	}

	public AnnotationActionPanel getActionPanel() {
		return actionPanel;
	}

	public void addAnnotationTermSelectionCommitListener(AnnotationTermCommitListener listener) {
		panelController.addAnnotationTermSelectionCommitListener(listener);
	}

	public void updateAnnotations() {
		panelController.updateAnnotations();
	}

}
