package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.table.AbstractTableModel;

import org.bioimageanalysis.icy.icytomine.core.model.Annotation;

import be.cytomine.client.CytomineException;

@SuppressWarnings("serial")
public class AnnotationTableModel extends AbstractTableModel {
	private static final String[] columnNames = new String[] { "Visible", "Name", "Terms", "Author" };

	private List<Annotation> annotations;
	private List<Boolean> annotationVisibility;

	public AnnotationTableModel(List<Annotation> annotations) {
		this.annotations = new ArrayList<>(annotations);
		annotationVisibility = annotations.stream().map(a -> true).collect(Collectors.toList());
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

	private Boolean isAnnotationVisible(int annotationIndex) {
		return annotationVisibility.get(annotationIndex);
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
			annotationVisibility.set(rowIndex, (Boolean) aValue);
			fireTableCellUpdated(rowIndex, columnIndex);
		}
	}

}
