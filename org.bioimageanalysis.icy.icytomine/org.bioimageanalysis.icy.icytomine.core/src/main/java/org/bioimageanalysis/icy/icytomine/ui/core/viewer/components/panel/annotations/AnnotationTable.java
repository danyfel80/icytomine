package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.core.model.Term;
import org.bioimageanalysis.icy.icytomine.core.model.User;
import org.bioimageanalysis.icy.icytomine.core.model.filters.AnnotationFilter;

@SuppressWarnings("serial")
public class AnnotationTable extends JScrollPane {

	private List<Annotation> annotations;
	private List<Annotation> filteredAnnotations;

	private JTable annotationTable;
	private AnnotationTableModel annotationTableModel;

	public AnnotationTable() {
		this(new ArrayList<>(0));
	}

	public AnnotationTable(List<Annotation> annotations) {
		this.annotations = new ArrayList<>(annotations);
		this.filteredAnnotations = new ArrayList<>(annotations);
		annotationTableModel = new AnnotationTableModel(annotations);

		annotationTable = new JTable();
		annotationTable.setModel(annotationTableModel);
		annotationTable.getColumnModel().getColumn(0).setPreferredWidth(50);
		annotationTable.getColumnModel().getColumn(0).setMinWidth(30);
		annotationTable.getColumnModel().getColumn(1).setMinWidth(50);
		annotationTable.getColumnModel().getColumn(2).setMinWidth(60);
		annotationTable.getColumnModel().getColumn(3).setMinWidth(30);
		setViewportView(annotationTable);
	}

	public void applyUserFilter(Set<User> users) {
		if (users.isEmpty())
			filteredAnnotations = filteredAnnotations.stream().collect(Collectors.toList());
		else
			filteredAnnotations = AnnotationFilter.byUsers(filteredAnnotations, users);

		annotationTableModel = new AnnotationTableModel(filteredAnnotations);
		annotationTable.setModel(annotationTableModel);
	}

	public void applyTermFilter(Set<Term> terms) {
		if (terms.isEmpty())
			filteredAnnotations = filteredAnnotations.stream().collect(Collectors.toList());
		else
			filteredAnnotations = AnnotationFilter.byTerms(filteredAnnotations, terms);

		annotationTableModel = new AnnotationTableModel(filteredAnnotations);
		annotationTable.setModel(annotationTableModel);
	}

	public void resetFilters() {
		filteredAnnotations = new ArrayList<>(annotations);
	}

	public List<Annotation> getFilteredAnnotations() {
		return filteredAnnotations;
	}

	public List<Annotation> getVisibleAnotations() {
		List<Annotation> visibleAnnotations = new ArrayList<>();
		for (int i = 0; i < filteredAnnotations.size(); i++) {
			if ((boolean) annotationTableModel.getValueAt(i, 0))
				visibleAnnotations.add(filteredAnnotations.get(i));
		}
		return visibleAnnotations;
	}
}
