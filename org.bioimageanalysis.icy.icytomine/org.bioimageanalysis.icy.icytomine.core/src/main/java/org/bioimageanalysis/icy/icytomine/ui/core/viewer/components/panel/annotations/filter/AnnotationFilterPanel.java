package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.filter;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.core.model.filters.AnnotationFilter;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.AnnotationManagerPanel;
import org.bioimageanalysis.icy.icytomine.ui.general.JCheckableItem;
import org.bioimageanalysis.icy.icytomine.ui.general.JCheckedComboBox;

@SuppressWarnings("serial")
public abstract class AnnotationFilterPanel<E> extends JPanel {
	public interface AnnotationFilterListener {
		void filterUpdated(Set<Annotation> activeAnnotations);
	}

	private static Image panelRemoveImage = new ImageIcon(
			AnnotationManagerPanel.class.getResource("/com/sun/java/swing/plaf/windows/icons/Error.gif")).getImage();
	private static final Dimension CHOICESBOX_MIN_SIZE = new Dimension(28, 20);

	private JLabel filterLabel;
	private JCheckedComboBox<E> choicesComboBox;
	private JButton filterRemoveButton;
	private AnnotationFilter filter;

	public AnnotationFilterPanel() {
		setGridBagLayout();
		addFilterLabel();
		addChoiceComboBox();
		addFilterRemoveButton();
	}

	private void setGridBagLayout() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);
	}

	private void addFilterLabel() {
		filterLabel = new JLabel();
		filterLabel.setMinimumSize(new Dimension(55, 14));

		GridBagConstraints gbc_filterLabel = new GridBagConstraints();
		gbc_filterLabel.anchor = GridBagConstraints.WEST;
		gbc_filterLabel.insets = new Insets(0, 0, 0, 5);
		gbc_filterLabel.gridx = 0;
		gbc_filterLabel.gridy = 0;

		add(filterLabel, gbc_filterLabel);
	}

	private void addChoiceComboBox() {
		choicesComboBox = new JCheckedComboBox<>();
		choicesComboBox.setModel(new DefaultComboBoxModel<>());

		GridBagConstraints gbc_choicesComboBox = new GridBagConstraints();
		gbc_choicesComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_choicesComboBox.insets = new Insets(0, 0, 0, 5);
		gbc_choicesComboBox.gridx = 1;
		gbc_choicesComboBox.gridy = 0;

		add(choicesComboBox, gbc_choicesComboBox);
	}

	private void addFilterRemoveButton() {
		filterRemoveButton = new JButton("");
		Dimension filterRemoveButtonSize = new Dimension(15, 15);
		filterRemoveButton.setPreferredSize(filterRemoveButtonSize);
		filterRemoveButton.setMinimumSize(filterRemoveButtonSize);
		filterRemoveButton.setIcon(getRemoveIcon(filterRemoveButtonSize));

		GridBagConstraints gbc_filterRemoveButton = new GridBagConstraints();
		gbc_filterRemoveButton.anchor = GridBagConstraints.WEST;
		gbc_filterRemoveButton.gridx = 2;
		gbc_filterRemoveButton.gridy = 0;

		add(filterRemoveButton, gbc_filterRemoveButton);
	}

	private Icon getRemoveIcon(Dimension size) {
		Image resizedFilterRemoveIcon = panelRemoveImage.getScaledInstance(size.width, size.height, Image.SCALE_SMOOTH);
		return new ImageIcon(resizedFilterRemoveIcon);
	}

	public void setLabelText(String labelText) {
		filterLabel.setText(labelText);
	}

	protected void setFilter(AnnotationFilter filter) {
		this.filter = filter;
	}

	public AnnotationFilter getFilter() {
		return filter;
	}

	public void setModel(E[] items, Function<E, String> labelFunction) {
		@SuppressWarnings("unchecked")
		JCheckableItem<E>[] checkableItems = Arrays.stream(items)
				.map((E it) -> new JCheckableItem<E>(it, labelFunction.apply(it), true)).toArray(JCheckableItem[]::new);
		choicesComboBox.setModel(new DefaultComboBoxModel<JCheckableItem<E>>(checkableItems));
		choicesComboBox.addActionListener((ActionEvent e) -> choiceChanged(e));
		choicesComboBox.setMinimumSize(CHOICESBOX_MIN_SIZE);
	}

	protected abstract void choiceChanged(ActionEvent e);

	public void addRemoveButtonListener(ActionListener listener) {
		filterRemoveButton.addActionListener(listener);
	}

	public void removeRemoveButtonListener(ActionListener listener) {
		filterRemoveButton.removeActionListener(listener);
	}
}
