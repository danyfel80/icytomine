package org.bioimageanalysis.icy.icytomine.ui.general;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;

public class JCheckBoxCellRenderer<E> implements ListCellRenderer<JCheckableItem<E>> {

	private final JLabel label = new JLabel(" ");
	private final JCheckBox check = new JCheckBox(" ");

	@Override
	public Component getListCellRendererComponent(JList<? extends JCheckableItem<E>> list, JCheckableItem<E> value,
			int index, boolean isSelected, boolean cellHasFocus) {
		if (index < 0) {
			label.setText(getCheckedItemString(list.getModel()));
			return label;
		} else {
			check.setText(Objects.toString(value, ""));
			check.setSelected(value.isSelected());
			if (isSelected) {
				check.setBackground(list.getSelectionBackground());
				check.setForeground(list.getSelectionForeground());
			} else {
				check.setBackground(list.getBackground());
				check.setForeground(list.getForeground());
			}
			return check;
		}
	}

	private static <T extends JCheckableItem<?>> String getCheckedItemString(ListModel<T> model) {
		List<String> selectedItems = new ArrayList<>();
		for (int i = 0; i < model.getSize(); i++) {
			T v = model.getElementAt(i);
			if (v.isSelected()) {
				selectedItems.add(v.toString());
			}
		}
		if (selectedItems.isEmpty()) {
			return " "; // When returning the empty string, the height of JComboBox
									// may become 0 in some cases.
		} else {
			return selectedItems.stream().filter(e -> e != null).sorted().collect(Collectors.joining(", "));
		}
	}

}
