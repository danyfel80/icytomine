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
	private JCheckedComboBox<E> choicesComboBox;
	private static Image userRemoveImage = new ImageIcon(
			AnnotationManagerPanel.class.getResource("/com/sun/java/swing/plaf/windows/icons/Error.gif")).getImage();

	public FilterPanel(String label) {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		JLabel userLabel = new JLabel(label);
		add(userLabel);

		choicesComboBox = new JCheckedComboBox<>();
		choicesComboBox.setModel(new DefaultComboBoxModel<>());
		add(choicesComboBox);

		JButton userRemoveButton = new JButton("");
		userRemoveButton.setPreferredSize(new Dimension(15, 15));
		userRemoveButton.setMaximumSize(new Dimension(15, 15));
		Image resizedUserRemoveIcon = userRemoveImage.getScaledInstance(userRemoveButton.getPreferredSize().width,
				userRemoveButton.getPreferredSize().height, Image.SCALE_SMOOTH);
		userRemoveButton.setIcon(new ImageIcon(resizedUserRemoveIcon));
		add(userRemoveButton);
	}

	public void setModel(E[] items, Function<E, String> labelFunction) {
		@SuppressWarnings("unchecked")
		JCheckableItem<E>[] checkableItems = Arrays.stream(items)
				.map((E it) -> new JCheckableItem<E>(it, labelFunction.apply(it), true)).toArray(JCheckableItem[]::new);
		choicesComboBox.setModel(new DefaultComboBoxModel<JCheckableItem<E>>(checkableItems));
		choicesComboBox.addActionListener((ActionEvent e) -> System.out.println("Item changed"));
	}

}
