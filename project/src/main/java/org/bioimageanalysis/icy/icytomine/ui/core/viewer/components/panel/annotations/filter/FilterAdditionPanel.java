package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.filter;

import java.util.HashSet;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class FilterAdditionPanel extends JPanel {

	public interface FilterAdditionListener {
		void filterAdditionRequested(String filterName);
	}

	private Set<FilterAdditionListener> filterListeners;

	public FilterAdditionPanel() {
		this(new String[] { "User", "Term" });
	}

	public FilterAdditionPanel(String[] availableFilters) {
		filterListeners = new HashSet<>();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		JComboBox<String> filterComboBox = new JComboBox<>();
		filterComboBox.setModel(new DefaultComboBoxModel<String>(availableFilters));
		add(filterComboBox);

		JButton addFilterButton = new JButton("Add Filter");
		add(addFilterButton);
		addFilterButton.addActionListener((e) -> addFilterButtonClicked((String) filterComboBox.getSelectedItem()));
	}

	public void addFilterAdditionListener(FilterAdditionListener listener) {
		filterListeners.add(listener);
	}

	public void removeFilterAdditionListener(FilterAdditionListener listener) {
		filterListeners.remove(listener);
	}

	private void addFilterButtonClicked(String selectedItem) {
		System.out.println(selectedItem);
		filterListeners.forEach(l -> l.filterAdditionRequested(selectedItem));
	}
}
