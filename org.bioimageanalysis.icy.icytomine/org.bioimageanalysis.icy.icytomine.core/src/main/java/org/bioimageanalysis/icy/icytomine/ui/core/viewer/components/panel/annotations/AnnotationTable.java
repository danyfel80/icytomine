package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.AnnotationTableModel.AnnotationVisibilityListener;

@SuppressWarnings("serial")
public class AnnotationTable extends JScrollPane {

	private JTable annotationTable;
	private AnnotationTableModel annotationTableModel;

	private Set<AnnotationVisibilityListener> annotationVisibilityListeners;

	public AnnotationTable() {
		this(new HashMap<>());
	}

	public AnnotationTable(Map<? extends Annotation, Boolean> annotationVisibility) {
		annotationTable = new JTable();
		annotationVisibilityListeners = new HashSet<>();
		setTableModel(annotationVisibility);
		annotationTable.getColumnModel().getColumn(0).setPreferredWidth(50);
		annotationTable.getColumnModel().getColumn(0).setMinWidth(30);
		annotationTable.getColumnModel().getColumn(1).setMinWidth(50);
		annotationTable.getColumnModel().getColumn(2).setMinWidth(60);
		annotationTable.getColumnModel().getColumn(3).setMinWidth(30);
		setViewportView(annotationTable);
	}

	public void setTableModel(Map<? extends Annotation, Boolean> annotationVisibility) {
		AnnotationTableModel newTableModel = new AnnotationTableModel(annotationVisibility);
		synchronized (annotationTable) {
			removeAnnotationVisibilityListenersFromTableModel();
			annotationTable.setModel(newTableModel);
			annotationTableModel = newTableModel;
			addAnnotationVisibilityListenersToTableModel();
		}
	}

	public AnnotationTableModel getTableModel() {
		return annotationTableModel;
	}

	private void removeAnnotationVisibilityListenersFromTableModel() {
		if (annotationTableModel != null) {
			annotationVisibilityListeners
					.forEach(listener -> annotationTableModel.removeAnnotationVisibilityListener(listener));
		}
	}

	private void addAnnotationVisibilityListenersToTableModel() {
		annotationVisibilityListeners.forEach(listener -> annotationTableModel.addAnnotationVisibilityListener(listener));
	}

	public void addAnnotationVisibilityListener(AnnotationVisibilityListener listener) {
		synchronized (annotationTable) {
			annotationVisibilityListeners.add(listener);
			annotationTableModel.addAnnotationVisibilityListener(listener);
		}
	}

	public void removeAnnotationVisibilityListener(AnnotationVisibilityListener listener) {
		synchronized (annotationTable) {
			annotationVisibilityListeners.remove(listener);
			if (annotationTableModel != null) {
				annotationTableModel.removeAnnotationVisibilityListener(listener);
			}
		}
	}

}
