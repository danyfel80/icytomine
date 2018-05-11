package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.filter;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.function.Function;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.AnnotationManagerPanel;
import org.bioimageanalysis.icy.icytomine.ui.general.JCheckableItem;
import org.bioimageanalysis.icy.icytomine.ui.general.JCheckedComboBox;

@SuppressWarnings("serial")
public abstract class FilterPanel<E> extends JPanel {
	protected JCheckedComboBox<E> choicesComboBox;
	private static Image panelRemoveImage = new ImageIcon(
			AnnotationManagerPanel.class.getResource("/com/sun/java/swing/plaf/windows/icons/Error.gif")).getImage();

	public FilterPanel(String label) {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		JLabel filterLabel = new JLabel(label);
		add(filterLabel);

		choicesComboBox = new JCheckedComboBox<>();
		choicesComboBox.setModel(new DefaultComboBoxModel<>());
		add(choicesComboBox);

		JButton filterRemoveButton = new JButton("");
		filterRemoveButton.setPreferredSize(new Dimension(15, 15));
		filterRemoveButton.setMaximumSize(new Dimension(15, 15));
		Image resizedFilterRemoveIcon = panelRemoveImage.getScaledInstance(filterRemoveButton.getPreferredSize().width,
				filterRemoveButton.getPreferredSize().height, Image.SCALE_SMOOTH);
		filterRemoveButton.setIcon(new ImageIcon(resizedFilterRemoveIcon));
		add(filterRemoveButton);
	}

	public void setModel(E[] items, Function<E, String> labelFunction) {
		@SuppressWarnings("unchecked")
		JCheckableItem<E>[] checkableItems = Arrays.stream(items)
				.map((E it) -> new JCheckableItem<E>(it, labelFunction.apply(it), true)).toArray(JCheckableItem[]::new);
		choicesComboBox.setModel(new DefaultComboBoxModel<JCheckableItem<E>>(checkableItems));
		choicesComboBox.addActionListener((ActionEvent e) -> choiceChanged(e));
	}

	protected abstract void choiceChanged(ActionEvent e);
}
