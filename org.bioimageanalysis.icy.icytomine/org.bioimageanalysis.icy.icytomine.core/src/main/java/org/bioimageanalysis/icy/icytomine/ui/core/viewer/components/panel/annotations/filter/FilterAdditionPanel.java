package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.filter;

import java.util.LinkedList;
import java.util.List;

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

	private List<FilterAdditionListener> filterListeners;

	public FilterAdditionPanel() {
		this(new String[] { "Test filter 1", "Test filter 2", "Test filter 3" });
	}

	public FilterAdditionPanel(String[] availableFilters) {
		filterListeners = new LinkedList<>();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		JComboBox<String> filterComboBox = new JComboBox<>();
		filterComboBox.setModel(new DefaultComboBoxModel<String>(availableFilters));
		add(filterComboBox);

		JButton addFilterButton = new JButton("Add Filter");
		addFilterButton.addActionListener((e) -> addFilterButtonClicked((String) filterComboBox.getSelectedItem()));
		add(addFilterButton);
	}

	public void addFilterAdditionListener(FilterAdditionListener listener) {
		filterListeners.add(listener);
	}

	private void addFilterButtonClicked(String selectedItem) {
		filterListeners.forEach(l -> l.filterAdditionRequested(selectedItem));
	}
}
