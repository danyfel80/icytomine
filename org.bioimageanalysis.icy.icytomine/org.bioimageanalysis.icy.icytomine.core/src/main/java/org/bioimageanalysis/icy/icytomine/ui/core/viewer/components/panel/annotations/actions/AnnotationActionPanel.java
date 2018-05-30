package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.actions;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.bioimageanalysis.icy.icytomine.core.model.Term;
import org.bioimageanalysis.icy.icytomine.ui.general.JCheckedComboBox;

@SuppressWarnings("serial")
public class AnnotationActionPanel extends JPanel {
	public AnnotationActionPanel() {
		setView();
	}

	private void setView() {
		setBorder(new TitledBorder(null, "Actions", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new GridLayout(1, 0, 0, 0));
		
		addTermSelectionPanel();
	}

	private void addTermSelectionPanel() {
		JPanel termSelectionPanel = new JPanel();
		add(termSelectionPanel);
		
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
		
		JCheckedComboBox<Term> checkedComboBox = new JCheckedComboBox<>();
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
	}
}
