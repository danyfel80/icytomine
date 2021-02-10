package org.bioimageanalysis.icy.icytomine.ui.general;

import java.awt.AWTEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.accessibility.Accessible;
import javax.swing.AbstractAction;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.plaf.basic.ComboPopup;

@SuppressWarnings("serial")
public class JCheckedComboBox<E> extends JComboBox<JCheckableItem<E>> {
	private boolean keepOpen;
	private transient ActionListener listener;

	public JCheckedComboBox() {
		super();
	}

	public JCheckedComboBox(ComboBoxModel<JCheckableItem<E>> model) {
		super(model);
	}

	@Override
	public void updateUI() {
		setRenderer(null);
		removeActionListener(listener);
		super.updateUI();
		listener = e -> {
			if ((e.getModifiers() & AWTEvent.MOUSE_EVENT_MASK) != 0) {
				updateItem(getSelectedIndex());
				keepOpen = true;
			}
		};
		setRenderer(new JCheckBoxCellRenderer<E>());
		addActionListener(listener);
		getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "checkbox-select");
		getActionMap().put("checkbox-select", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Accessible a = getAccessibleContext().getAccessibleChild(0);
				if (a instanceof ComboPopup) {
					updateItem(((ComboPopup) a).getList().getSelectedIndex());
				}
			}
		});
	}

	private void updateItem(int selectedIndex) {
		if (isPopupVisible()) {
			JCheckableItem<E> item = getItemAt(selectedIndex);
			item.setSelected(!item.isSelected());
			// item.selected ^= true;
			// ComboBoxModel m = getModel();
			// if (m instanceof CheckableComboBoxModel) {
			// ((CheckableComboBoxModel) m).fireContentsChanged(index);
			// }
			// removeItemAt(index);
			// insertItemAt(item, index);
			setSelectedIndex(-1);
			setSelectedItem(item);
		}
	}

	@Override
	public void setPopupVisible(boolean v) {
		if (keepOpen) {
			keepOpen = false;
		} else {
			super.setPopupVisible(v);
		}
	}
}
