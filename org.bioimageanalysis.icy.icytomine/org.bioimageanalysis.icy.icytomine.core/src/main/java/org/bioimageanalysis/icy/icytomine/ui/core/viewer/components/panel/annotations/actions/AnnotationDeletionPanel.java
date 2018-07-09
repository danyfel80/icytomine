package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.actions;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JButton;
import java.awt.Insets;
import java.awt.event.ActionListener;

@SuppressWarnings("serial")
public class AnnotationDeletionPanel extends JPanel {

	private JButton deleteAnnotationsButton;

	public AnnotationDeletionPanel() {
		setView();
	}

	private void setView() {
		setGridBagLayout();
		setDeletionTitle();
		setDeletionButton();
	}

	private void setGridBagLayout() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 23, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, Double.MIN_VALUE };

		setLayout(gridBagLayout);
	}

	private void setDeletionTitle() {
		JLabel deleteAnnotationsLabel = new JLabel("Delete Annotation(s)");

		GridBagConstraints deleteAnnotationsLabelConstraints = new GridBagConstraints();
		deleteAnnotationsLabelConstraints.anchor = GridBagConstraints.EAST;
		deleteAnnotationsLabelConstraints.insets = new Insets(2, 5, 2, 5);
		deleteAnnotationsLabelConstraints.gridx = 0;
		deleteAnnotationsLabelConstraints.gridy = 0;

		add(deleteAnnotationsLabel, deleteAnnotationsLabelConstraints);
	}

	private void setDeletionButton() {
		deleteAnnotationsButton = new JButton("Delete");

		GridBagConstraints deleteAnnotationsButtonConstraints = new GridBagConstraints();
		deleteAnnotationsButtonConstraints.anchor = GridBagConstraints.WEST;
		deleteAnnotationsButtonConstraints.insets = new Insets(2, 0, 2, 5);
		deleteAnnotationsButtonConstraints.gridx = 1;
		deleteAnnotationsButtonConstraints.gridy = 0;

		add(deleteAnnotationsButton, deleteAnnotationsButtonConstraints);
	}

	public void addDeletionButtonActionListener(ActionListener listener) {
		getDeletionButton().addActionListener(listener);
	}

	private JButton getDeletionButton() {
		return deleteAnnotationsButton;
	}

	public void removeDeletionButtonActionListener(ActionListener listener) {
		getDeletionButton().removeActionListener(listener);
	}

}
