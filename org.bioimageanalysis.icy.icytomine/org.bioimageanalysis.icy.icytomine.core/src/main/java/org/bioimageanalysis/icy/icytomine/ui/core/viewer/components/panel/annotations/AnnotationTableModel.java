package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import org.bioimageanalysis.icy.icytomine.core.model.Annotation;

import be.cytomine.client.CytomineException;

@SuppressWarnings("serial")
public class AnnotationTableModel extends AbstractTableModel {
	public interface AnnotationVisibilityListener {
		void annotationVisibilityChanged(Annotation annotation, boolean visible);
	}

	private static final String[] columnNames = new String[] { "Visible", "Name", "Terms", "Author" };

	private List<Annotation> annotations;
	private Map<Annotation, Boolean> annotationVisibility;

	private Set<AnnotationVisibilityListener> annotationVisibilityListeners;

	public AnnotationTableModel(Map<? extends Annotation, Boolean> annotationVisibility) {
		this.annotationVisibility = new ConcurrentHashMap<>(annotationVisibility);
		this.annotations = new ArrayList<>(annotationVisibility.keySet());
		this.annotationVisibilityListeners = new HashSet<>();
		this.addTableModelListener(event -> onTableValuesChanged(event));
	}

	@Override
	public String getColumnName(int column) {
		return (column < columnNames.length) ? columnNames[column] : null;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 0) {
			return Boolean.class;
		} else if (columnIndex == 1) {
			return String.class;
		} else if (columnIndex == 2) {
			return List.class;
		} else if (columnIndex == 3) {
			return String.class;
		}
		return null;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 0 && rowIndex < annotations.size();
	}

	@Override
	public int getRowCount() {
		return annotations.size();
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex < annotations.size()) {
			if (columnIndex == 0) {
				return isAnnotationVisible(rowIndex);
			} else if (columnIndex == 1) {
				return getAnnotationId(rowIndex);
			} else if (columnIndex == 2) {
				return getAnnotationTerms(rowIndex);
			} else if (columnIndex == 3) {
				return getAnnotationAuthor(rowIndex);
			}
		}
		return null;
	}

	private boolean isAnnotationVisible(int annotationIndex) {
		return annotationVisibility.get(annotations.get(annotationIndex));
	}

	private Long getAnnotationId(int annotationIndex) {
		return annotations.get(annotationIndex).getId();
	}

	private List<String> getAnnotationTerms(int annotationIndex) {
		try {
			return annotations.get(annotationIndex).getTerms().stream().map(t -> t.getName()).collect(Collectors.toList());
		} catch (CytomineException e) {
			e.printStackTrace();
			return new ArrayList<>(0);
		}
	}

	private String getAnnotationAuthor(int annotationIndex) {
		return annotations.get(annotationIndex).getUser().getUserName();
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			annotationVisibility.put(annotations.get(rowIndex), (Boolean) aValue);
			fireTableCellUpdated(rowIndex, columnIndex);
		}
	}

	public boolean isAnnotationVisible(Annotation annotation) {
		return annotationVisibility.get(annotation);
	}

	public void addAnnotationVisibilityListener(AnnotationVisibilityListener listener) {
		synchronized (annotationVisibilityListeners) {
			this.annotationVisibilityListeners.add(listener);
		}
	}

	public void removeAnnotationVisibilityListener(AnnotationVisibilityListener listener) {
		synchronized (annotationVisibilityListeners) {
			this.annotationVisibilityListeners.remove(listener);
		}
	}

	private void onTableValuesChanged(TableModelEvent event) {
		if (event.getType() == TableModelEvent.UPDATE) {
			notifyAnnotationVisibilityChanged(event.getFirstRow());
		}
	}

	private void notifyAnnotationVisibilityChanged(int annotationIndex) {
		Annotation annotation = annotations.get(annotationIndex);
		this.annotationVisibilityListeners
				.forEach(listener -> listener.annotationVisibilityChanged(annotation, annotationVisibility.get(annotation)));
	}

	public Set<Annotation> getAnnotations() {
		return new HashSet<>(annotations);
	}

	public Map<Annotation, Boolean> getAnnotationVisibility() {
		return annotationVisibility;
	}

	public Set<Annotation> getVisibleAnnotations() {
		return this.annotationVisibility.keySet().stream().filter(a -> annotationVisibility.get(a))
				.collect(Collectors.toSet());
	}

}
