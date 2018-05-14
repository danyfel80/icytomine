package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.filter;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.swing.JPanel;

import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.model.Term;
import org.bioimageanalysis.icy.icytomine.core.model.User;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.filter.TermFilterPanel.TermSelectionListener;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.filter.UserFilterPanel.UserSelectionListener;

@SuppressWarnings("serial")
public class AnnotationFilterPanel extends JPanel {

	private FilterAdditionPanel filterAdditionPanel;
	private List<FilterPanel<?>> filterPanels;

	private Set<UserSelectionListener> userSelectionListeners;
	private Set<TermSelectionListener> termSelectionListeners;

	private Image imageInformation;

	public AnnotationFilterPanel(org.bioimageanalysis.icy.icytomine.core.model.Image imageInformation) {
		this.filterPanels = new ArrayList<>();

		userSelectionListeners = new HashSet<>();
		termSelectionListeners = new HashSet<>();

		this.imageInformation = imageInformation;

		GridBagLayout gbl_filterPanel = new GridBagLayout();
		gbl_filterPanel.columnWidths = new int[] { 305 };
		setLayout(gbl_filterPanel);

		addFilterAdditionPanel();
	}

	private void addFilterAdditionPanel() {
		filterAdditionPanel = new FilterAdditionPanel();
		GridBagConstraints layoutConstraints = createLayoutConstraints(5, 5, 0);
		add(filterAdditionPanel, layoutConstraints);
		filterAdditionPanel.addFilterAdditionListener((String filterName) -> addFilter(filterName));
	}

	private GridBagConstraints createLayoutConstraints(int topMargin, int bottomMargin, int row) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(topMargin, 0, bottomMargin, 0);
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx = 0;
		constraints.gridy = row;
		return constraints;
	}

	private synchronized void addFilter(String filterName) {
		System.out.println(filterName);
		int row = filterPanels.size();
		FilterPanel<?> filter = createFilter(filterName);
		GridBagConstraints layoutConstraints = createLayoutConstraints(0, 5, row+1);
		add(filter, layoutConstraints);
		revalidate();
		this.filterPanels.add(filter);
	}

	private FilterPanel<?> createFilter(String filterName) {
		FilterPanel<?> filterPanel = null;
		if (Objects.equals(filterName, "User")) {
			UserFilterPanel userFilterPanel = new UserFilterPanel(getImageUsers(imageInformation));
			userSelectionListeners.forEach(l -> userFilterPanel.addUserSelectionListener(l));
			filterPanel = userFilterPanel;
		} else if (Objects.equals(filterName, "Term")) {
			TermFilterPanel termFilterPanel = new TermFilterPanel(getImageTerms(imageInformation));
			termSelectionListeners.forEach(l -> termFilterPanel.addTermSelectionListener(l));

			filterPanel = termFilterPanel;
		}
		return filterPanel;
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

	public void addUserSelectionListener(UserSelectionListener listener) {
		this.userSelectionListeners.add(listener);
	}

	public void removeUserSelectionListener(UserSelectionListener listener) {
		this.userSelectionListeners.remove(listener);
	}

	public void addTermSelectionListener(TermSelectionListener listener) {
		this.termSelectionListeners.add(listener);
	}

	public void removeTermSelectionListener(TermSelectionListener listener) {
		this.termSelectionListeners.remove(listener);
	}
}
