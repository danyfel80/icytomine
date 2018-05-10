package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.filter;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.bioimageanalysis.icy.icytomine.core.model.Term;
import org.bioimageanalysis.icy.icytomine.core.model.User;

@SuppressWarnings("serial")
public class AnnotationFilterPanel extends JPanel {

	private FilterAdditionPanel filterAdditionPanel;
	private List<FilterPanel<?>> filterPanels;

	public AnnotationFilterPanel(org.bioimageanalysis.icy.icytomine.core.model.Image imageInformation) {
		GridBagLayout gbl_filterPanel = new GridBagLayout();
		gbl_filterPanel.columnWidths = new int[]{305};
		setLayout(gbl_filterPanel);

		filterAdditionPanel = new FilterAdditionPanel();
		GridBagConstraints filterAdditionPanelConstraints = new GridBagConstraints();
		filterAdditionPanelConstraints.insets = new Insets(5, 0, 5, 0);
		filterAdditionPanelConstraints.fill = GridBagConstraints.BOTH;
		filterAdditionPanelConstraints.gridx = 0;
		filterAdditionPanelConstraints.gridy = 0;
		add(filterAdditionPanel, filterAdditionPanelConstraints);

		filterPanels = new ArrayList<>();
		UserFilterPanel userFilterPanel = new UserFilterPanel(getImageUsers(imageInformation));
		filterPanels.add(userFilterPanel);
		userFilterPanel.setToolTipText("Select the users to be filtered on the annotation list");
		GridBagConstraints userFilterPanelConstraints = new GridBagConstraints();
		userFilterPanelConstraints.insets = new Insets(0, 0, 5, 0);
		userFilterPanelConstraints.fill = GridBagConstraints.BOTH;
		userFilterPanelConstraints.gridx = 0;
		userFilterPanelConstraints.gridy = 1;
		add(userFilterPanel, userFilterPanelConstraints);

		TermFilterPanel termFilterPanel = new TermFilterPanel(getImageTerms(imageInformation));
		filterPanels.add(termFilterPanel);
		GridBagConstraints termFilterPanelConstraints = new GridBagConstraints();
		termFilterPanelConstraints.insets = new Insets(0, 0, 5, 0);
		termFilterPanelConstraints.fill = GridBagConstraints.BOTH;
		termFilterPanelConstraints.gridx = 0;
		termFilterPanelConstraints.gridy = 2;
		add(termFilterPanel, termFilterPanelConstraints);
	}

	private List<User> getImageUsers(org.bioimageanalysis.icy.icytomine.core.model.Image imageInformation) {
		try {
			return imageInformation.getAnnotationUsers();
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>(0);
		}
	}

	private List<Term> getImageTerms(org.bioimageanalysis.icy.icytomine.core.model.Image imageInformation) {
		try {
			return imageInformation.getAvailableTerms();
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>(0);
		}
	}

}
