package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;

import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.AnnotationTableModel.AnnotationVisibilityListener;

@SuppressWarnings("serial")
public class AnnotationTable extends JScrollPane {

	public interface AnnotationSelectionListener {
		void selectionChanged(Set<Annotation> selectedAnnotations);
	}

	public interface AnnotationDoubleClickListener {
		void annotationDoubleClicked(Annotation annotation);
	}

	private JTable annotationTable;
	private AnnotationTableModel annotationTableModel;

	private Set<AnnotationVisibilityListener> annotationVisibilityListeners;
	private Set<AnnotationSelectionListener> annotationSelectionListeners;
	private Set<AnnotationDoubleClickListener> annotationDoubleClickListeners;

	public AnnotationTable() {
		this(new HashMap<>());
	}

	public AnnotationTable(Map<? extends Annotation, Boolean> annotationVisibility) {
		annotationTable = new JTable();
		annotationVisibilityListeners = new HashSet<>();
		annotationSelectionListeners = new HashSet<>();
		annotationDoubleClickListeners = new HashSet<>();
		setTableModel(annotationVisibility);
		setColumnWidths();
		setViewportView(annotationTable);
		addSelectionListener();
	}

	public void setTableModel(Map<? extends Annotation, Boolean> annotationVisibility) {
		AnnotationTableModel newTableModel = new AnnotationTableModel(annotationVisibility);
		synchronized (annotationTable) {
			removeAnnotationVisibilityListenersFromTableModel();
			annotationTable.setModel(newTableModel);
			annotationTableModel = newTableModel;
			newTableModel.fireTableDataChanged();
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

	private void setColumnWidths() {
		annotationTable.getColumnModel().getColumn(0).setPreferredWidth(50);
		annotationTable.getColumnModel().getColumn(0).setMinWidth(30);
		annotationTable.getColumnModel().getColumn(1).setMinWidth(50);
		annotationTable.getColumnModel().getColumn(2).setMinWidth(60);
		annotationTable.getColumnModel().getColumn(3).setMinWidth(30);
	}

	private void addSelectionListener() {
		annotationTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
			if (!e.getValueIsAdjusting()) {
				notifySelectionChange();
			}
		});
		annotationTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getClickCount() == 2 && annotationTable.getSelectedRow() != -1) {
					Point point = e.getPoint();
					int row = annotationTable.rowAtPoint(point);
					if (row != -1) {
						notifyDoubleClickSelection();
					}
				}
			}
		});
	}

	private void notifySelectionChange() {
		Set<Annotation> selectedAnnotations = getSelectedAnnotations();
		this.annotationSelectionListeners.forEach(l -> l.selectionChanged(selectedAnnotations));
	}

	public Set<Annotation> getSelectedAnnotations() {
		ListSelectionModel selectionModel = annotationTable.getSelectionModel();
		Set<Annotation> selectedAnnotations = new HashSet<>();
		if (!selectionModel.isSelectionEmpty()) {
			int minIndex = selectionModel.getMinSelectionIndex();
			int maxIndex = selectionModel.getMaxSelectionIndex();
			for (int i = minIndex; i <= maxIndex; i++) {
				if (selectionModel.isSelectedIndex(i)) {
					selectedAnnotations.add(((AnnotationTableModel) annotationTable.getModel()).getAnnotationAt(i));
				}
			}
		}

		return selectedAnnotations;
	}

	private void notifyDoubleClickSelection() {
		this.annotationDoubleClickListeners.forEach(l -> l.annotationDoubleClicked(getSelectedAnnotation()));
	}

	private Annotation getSelectedAnnotation() {
		ListSelectionModel selectionModel = annotationTable.getSelectionModel();
		int rowIndex = selectionModel.getMinSelectionIndex();
		if (rowIndex != -1)
			return ((AnnotationTableModel) annotationTable.getModel()).getAnnotationAt(rowIndex);
		else
			return null;
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

	public void addAnnotationSelectionListener(AnnotationSelectionListener listener) {
		this.annotationSelectionListeners.add(listener);
	}

	public void removeAnnotationSelectionListener(AnnotationSelectionListener listener) {
		this.annotationSelectionListeners.remove(listener);
	}

	public void addAnnotationDoubleClickListener(AnnotationDoubleClickListener listener) {
		this.annotationDoubleClickListeners.add(listener);
	}

	public void removeAnnotationDoubleClickListener(AnnotationDoubleClickListener listener) {
		this.annotationDoubleClickListeners.remove(listener);
	}

	public void setSelectedAnnotations(Set<Annotation> selectedAnnotations) {
		int numAnnotations = annotationTableModel.getRowCount();
		annotationTable.getSelectionModel().clearSelection();
		for (int row = 0; row < numAnnotations; row++) {
			Annotation annotationAtRow = annotationTableModel.getAnnotationAt(row);
			if (selectedAnnotations.contains(annotationAtRow)) {
				annotationTable.getSelectionModel().addSelectionInterval(row, row);
			}
		}
	}
}
