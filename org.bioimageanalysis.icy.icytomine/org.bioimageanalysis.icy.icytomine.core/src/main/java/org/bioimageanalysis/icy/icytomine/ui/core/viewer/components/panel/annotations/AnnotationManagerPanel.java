package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations;

import java.awt.BorderLayout;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.JPanel;

import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.model.Term;
import org.bioimageanalysis.icy.icytomine.core.model.User;
import org.bioimageanalysis.icy.icytomine.core.model.filters.FilterAnnotationByTerm;
import org.bioimageanalysis.icy.icytomine.core.model.filters.FilterAnnotationByUser;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.AnnotationTable.AnnotationSelectionListener;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.filter.AnnotationFilterPanel;

import be.cytomine.client.CytomineException;
import java.awt.GridLayout;
import javax.swing.JLabel;
import org.bioimageanalysis.icy.icytomine.ui.general.JCheckedComboBox;
import javax.swing.JButton;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.border.TitledBorder;

@SuppressWarnings("serial")
public class AnnotationManagerPanel extends JPanel {

	public interface AnnotationsVisibilityListener {
		void annotationsVisibiliyChanged(Set<Annotation> newVisibleAnnotations);
	}

	private AnnotationFilterPanel filterPanel;
	private AnnotationTable annotationTable;

	private Image imageInformation;
	private Set<Annotation> annotations;
	private Set<User> activeUsers;
	private Set<Term> activeTerms;

	Set<AnnotationsVisibilityListener> annotationsVisibilitylisteners;

	public AnnotationManagerPanel() {
		this(Image.getNoImage(null));
	}

	public AnnotationManagerPanel(Image imageInformation) {
		this.imageInformation = imageInformation;
		retrieveImageAnnotations();
		fillActiveUsers();
		fillActiveTerms();
		Map<Annotation, Boolean> annotationVisibility = createAnnotationVisibility(annotations);

		setLayout(new BorderLayout(0, 0));

		filterPanel = new AnnotationFilterPanel(this.imageInformation);
		add(filterPanel, BorderLayout.NORTH);
		filterPanel.addUserSelectionListener((User user, boolean selected) -> userSelectionChange(user, selected));
		filterPanel.addTermSelectionListener((Term term, boolean selected) -> termSelectionChange(term, selected));

		annotationTable = new AnnotationTable(annotationVisibility);
		add(annotationTable, BorderLayout.CENTER);
		
		JPanel actionPanel = new JPanel();
		actionPanel.setBorder(new TitledBorder(null, "Actions", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(actionPanel, BorderLayout.SOUTH);
		actionPanel.setLayout(new GridLayout(1, 0, 0, 0));
		
		JPanel termSelectionPanel = new JPanel();
		actionPanel.add(termSelectionPanel);
		GridBagLayout gbl_termSelectionPanel = new GridBagLayout();
		gbl_termSelectionPanel.columnWidths = new int[] {0, 0, 0, 0};
		gbl_termSelectionPanel.rowHeights = new int[]{23, 0};
		gbl_termSelectionPanel.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_termSelectionPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		termSelectionPanel.setLayout(gbl_termSelectionPanel);
		
		JLabel lblTerms = new JLabel("Associated terms");
		GridBagConstraints gbc_lblTerms = new GridBagConstraints();
		gbc_lblTerms.insets = new Insets(2, 5, 2, 5);
		gbc_lblTerms.gridx = 0;
		gbc_lblTerms.gridy = 0;
		termSelectionPanel.add(lblTerms, gbc_lblTerms);
		
		JCheckedComboBox checkedComboBox = new JCheckedComboBox();
		GridBagConstraints gbc_checkedComboBox = new GridBagConstraints();
		gbc_checkedComboBox.weightx = 1.0;
		gbc_checkedComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_checkedComboBox.insets = new Insets(2, 0, 2, 5);
		gbc_checkedComboBox.gridx = 1;
		gbc_checkedComboBox.gridy = 0;
		termSelectionPanel.add(checkedComboBox, gbc_checkedComboBox);
		
		JButton btnSet = new JButton("Set");
		GridBagConstraints gbc_btnSet = new GridBagConstraints();
		gbc_btnSet.insets = new Insets(2, 0, 2, 5);
		gbc_btnSet.gridx = 2;
		gbc_btnSet.gridy = 0;
		termSelectionPanel.add(btnSet, gbc_btnSet);
		annotationTable.addAnnotationVisibilityListener((Annotation annotation, boolean visible) -> annotationVisibilityChanged(annotation, visible));

		annotationsVisibilitylisteners = new HashSet<>();
	}

	private Set<Annotation> retrieveImageAnnotations() {
		if (annotations == null) {
			try {
				annotations = imageInformation.getAnnotations().stream().collect(Collectors.toSet());
			} catch (CytomineException e) {
				e.printStackTrace();
				annotations = new HashSet<>();
			}
		}
		return annotations;
	}

	private void fillActiveUsers() {
		activeUsers = new HashSet<>();
		annotations.forEach(annotation -> activeUsers.add(annotation.getUser()));
	}

	private void fillActiveTerms() {
		activeTerms = new HashSet<>();
		annotations.forEach(annotation -> activeTerms.addAll(getAnnotationTerms(annotation)));
	}

	private Set<Term> getAnnotationTerms(Annotation a) {
		Set<Term> terms;
		try {
			terms = new HashSet<>(a.getTerms());
		} catch (CytomineException e) {
			e.printStackTrace();
			terms = new HashSet<>();
		}
		return terms;
	}

	private Map<Annotation, Boolean> createAnnotationVisibility(Set<Annotation> annotations) {
		Map<Annotation, Boolean> annotationVisibility;
		annotationVisibility = annotations.stream().collect(Collectors.toMap(Function.identity(), a -> true));
		return annotationVisibility;
	}

	private void userSelectionChange(User user, boolean selected) {
		if (selected) {
			activeUsers.add(user);
		} else {
			activeUsers.remove(user);
		}

		FilterAnnotationByUser userFilter = new FilterAnnotationByUser(activeUsers);
		FilterAnnotationByTerm termFilter = new FilterAnnotationByTerm(activeTerms);

		Set<Annotation> newActiveAnnotations = annotations.stream().filter(a -> userFilter.apply(a))
				.filter(a -> termFilter.apply(a)).collect(Collectors.toSet());

		Map<Annotation, Boolean> annotationVisibility = createAnnotationVisibility(newActiveAnnotations);
		annotationTable.setTableModel(annotationVisibility);

		notifyAnnotationsVisibilityListeners(newActiveAnnotations);
	}

	private void termSelectionChange(Term term, boolean selected) {
		if (selected) {
			activeTerms.add(term);
		} else {
			activeTerms.remove(term);
		}

		FilterAnnotationByUser userFilter = new FilterAnnotationByUser(activeUsers);
		FilterAnnotationByTerm termFilter = new FilterAnnotationByTerm(activeTerms);

		Set<Annotation> newActiveAnnotations = annotations.stream().filter(a -> userFilter.apply(a))
				.filter(a -> termFilter.apply(a)).collect(Collectors.toSet());

		Map<Annotation, Boolean> annotationVisibility = createAnnotationVisibility(newActiveAnnotations);
		annotationTable.setTableModel(annotationVisibility);

		notifyAnnotationsVisibilityListeners(newActiveAnnotations);
	}

	private void notifyAnnotationsVisibilityListeners(Set<Annotation> newVisibleAnnotations) {
		annotationsVisibilitylisteners.forEach(l -> l.annotationsVisibiliyChanged(newVisibleAnnotations));
	}

	public void addAnnotationsVisibilityListener(AnnotationsVisibilityListener listener) {
		this.annotationsVisibilitylisteners.add(listener);
	}

	public void removeAnnotationsVisibilityListener(AnnotationsVisibilityListener listener) {
		this.annotationsVisibilitylisteners.remove(listener);
	}

	private void annotationVisibilityChanged(Annotation annotation, boolean visible) {
		notifyAnnotationsVisibilityListeners(annotationTable.getTableModel().getVisibleAnnotations());
	}

	public void addAnnotationSelectionListener(AnnotationSelectionListener listener) {
		this.annotationTable.addAnnotationSelectionListener(listener);
	}

}
