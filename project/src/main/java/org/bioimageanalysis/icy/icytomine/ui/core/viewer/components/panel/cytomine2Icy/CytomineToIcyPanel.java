package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.cytomine2Icy;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.geom.Dimension2D;
import java.util.Arrays;
import java.util.Optional;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.ViewController;

@SuppressWarnings("serial")
public class CytomineToIcyPanel extends JPanel {

	CytomineToIcyPanelController panelController;

	private JComboBox<Double> magnificationComboBox;
	private JTextField magnificationTextField;
	private JLabel outputSizeValueLabel;
	private JProgressBar progressBar;
	private JButton startButton;
	private JButton cancelButton;

	private JCheckBox magnificationSourceCheckBox;

	public CytomineToIcyPanel(ViewController viewController) {
		setupView();
		setupController(viewController);
	}

	private void setupController(ViewController viewController) {
		panelController = new CytomineToIcyPanelController(this, viewController);
	}

	private void setupView() {
		GridBagLayout layout = createPanelLayout();
		setLayout(layout);

		JPanel magnificationPanel = createMagnificationsPanel();
		GridBagConstraints magnificationPanelConstraints = createConstraints(0, 0, GridBagConstraints.BOTH, 0, 0, 5, 0);
		add(magnificationPanel, magnificationPanelConstraints);

		progressBar = createProgressBar();
		GridBagConstraints progressBarConstraints = createConstraints(0, 1, GridBagConstraints.HORIZONTAL, 5, 5, 5, 5);
		add(progressBar, progressBarConstraints);

		JPanel buttonsPanel = createButtonsPanel();
		GridBagConstraints buttonsPanelConstraints = createConstraints(0, 2, GridBagConstraints.BOTH, 5, 5, 5, 5);
		add(buttonsPanel, buttonsPanelConstraints);

		setPreferredSize(new Dimension(300, 180));
	}

	private GridBagLayout createPanelLayout() {
		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[] {0};
		layout.rowHeights = new int[] {0, 0, 0, 1};
		layout.columnWeights = new double[] {1.0};
		layout.rowWeights = new double[] {0.0, 0.0, 0.0, 1.0};
		return layout;
	}

	private JPanel createMagnificationsPanel() {
		JPanel panel = new JPanel();

		GridBagLayout panelLayout = new GridBagLayout();
		panelLayout.columnWidths = new int[] {0, 0, 0};
		panelLayout.rowHeights = new int[] {0, 0};
		panelLayout.columnWeights = new double[] {0.0, 1.0, 0.0};
		panelLayout.rowWeights = new double[] {0.0, 0.0};
		panel.setLayout(panelLayout);

		JLabel magnificationLabel = new JLabel("Magnification");
		GridBagConstraints magnificationLabelConstraints = createConstraints(0, 0, GridBagConstraints.BOTH, 5, 5, 5, 5);
		panel.add(magnificationLabel, magnificationLabelConstraints);

		magnificationComboBox = new JComboBox<>();
		magnificationTextField = new JTextField(String.valueOf(1d));
		GridBagConstraints magnificationComboBoxConstraints = createConstraints(1, 0, GridBagConstraints.BOTH, 5, 5, 5, 5);
		panel.add(magnificationComboBox, magnificationComboBoxConstraints);

		magnificationSourceCheckBox = new JCheckBox("Manual");
		magnificationSourceCheckBox.setSelected(false);
		magnificationSourceCheckBox.addItemListener((ItemEvent event) -> {
			if (event.getStateChange() == ItemEvent.SELECTED) {
				magnificationTextField.setText(magnificationComboBox.getSelectedItem().toString());
				panel.remove(magnificationComboBox);
				panel.add(magnificationTextField, magnificationComboBoxConstraints);
			} else {
				Object currentItem = magnificationComboBox.getSelectedItem();
				magnificationComboBox.setSelectedItem(null);
				magnificationComboBox.setSelectedItem(currentItem);
				panel.remove(magnificationTextField);
				panel.add(magnificationComboBox, magnificationComboBoxConstraints);
			}
			panel.updateUI();
		});

		GridBagConstraints magnificationSourceCheckBoxConstraints = createConstraints(2, 0, GridBagConstraints.BOTH, 5, 5,
				5, 5);
		panel.add(magnificationSourceCheckBox, magnificationSourceCheckBoxConstraints);

		JLabel outputSizeTitleLabel = new JLabel("Output size:");
		GridBagConstraints outputSizeTitleLabelConstraints = createConstraints(0, 1, GridBagConstraints.BOTH, 5, 5, 5, 5);
		panel.add(outputSizeTitleLabel, outputSizeTitleLabelConstraints);

		outputSizeValueLabel = new JLabel("1000 x 1000 pixels");
		GridBagConstraints outputSizeValueLabelConstraints = createConstraints(1, 1, GridBagConstraints.BOTH, 5, 5, 5, 5);
		panel.add(outputSizeValueLabel, outputSizeValueLabelConstraints);
		return panel;
	}

	private static GridBagConstraints createConstraints(int gridX, int gridY, int fill, int topInset, int leftInset,
			int bottomInset, int rightInset) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(topInset, leftInset, bottomInset, rightInset);
		constraints.fill = fill;
		constraints.gridx = gridX;
		constraints.gridy = gridY;
		return constraints;
	}

	private JProgressBar createProgressBar() {
		progressBar = new JProgressBar();
		setProgressIdle();
		return progressBar;
	}

	private JPanel createButtonsPanel() {
		JPanel panel = new JPanel();

		GridBagLayout panelLayout = new GridBagLayout();
		panelLayout.columnWidths = new int[] {0, 0};
		panelLayout.rowHeights = new int[] {1};
		panelLayout.columnWeights = new double[] {0.0, 0.0};
		panelLayout.rowWeights = new double[] {0.0};
		panel.setLayout(panelLayout);

		startButton = new JButton("Start");
		GridBagConstraints startButtonConstraints = createConstraints(0, 0, GridBagConstraints.NONE, 0, 0, 0, 5);
		panel.add(startButton, startButtonConstraints);

		cancelButton = new JButton("Cancel");
		GridBagConstraints cancelButtonConstraints = createConstraints(1, 0, GridBagConstraints.NONE, 0, 0, 0, 0);
		panel.add(cancelButton, cancelButtonConstraints);

		return panel;
	}

	public void setAvailableMagnifications(double[] magnifications) {
		ComboBoxModel<Double> comboBoxModel = new DefaultComboBoxModel<>(
				Arrays.stream(magnifications).boxed().toArray(Double[]::new));
		magnificationComboBox.setModel(comboBoxModel);
	}

	public void addMagnificationListener(ActionListener listener) {
		magnificationComboBox.addActionListener(listener);
		magnificationTextField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				warn();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				warn();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				warn();
			}

			private void warn() {
				listener.actionPerformed(new ActionEvent(magnificationTextField, ActionEvent.ACTION_PERFORMED, "textChanged"));
			}
		});
	}

	public void removeMagnificationListener(ActionListener listener) {
		magnificationComboBox.removeActionListener(listener);
	}

	public void addStartButtonActionListener(ActionListener listener) {
		startButton.addActionListener(listener);
	}

	public void removeStartButtonActionListener(ActionListener listener) {
		startButton.addActionListener(listener);
	}

	public void addCancelButtonActionListener(ActionListener listener) {
		cancelButton.addActionListener(listener);
	}

	public void setStartButtonEnabled(boolean enabled) {
		startButton.setEnabled(enabled);
	}

	public void setProgress(String message, double percent) {
		if (percent == 0d) {
			EventQueue.invokeLater(() -> {
				progressBar.setIndeterminate(true);
				progressBar.setStringPainted(true);
				String fullMessage = (message.isEmpty())? ("Initializing..."): (message + ": Initializing...");
				progressBar.setString(fullMessage);
			});
		} else {
			EventQueue.invokeLater(() -> {
				int percentage = (int) (percent * 100);
				progressBar.setIndeterminate(false);
				progressBar.setValue(percentage);
				progressBar.setStringPainted(true);
				String fullMessage = (message.isEmpty())? (percentage + "%"): (message + ": " + percentage + "%");
				progressBar.setString(fullMessage);
			});
		}

	}

	public void setProgress(double percent) {
		setProgress("", percent);
	}

	public void addCloseListener(ActionListener listener) {
		panelController.addCloseListener(listener);
	}

	public void setOutputImageSize(Dimension2D outputDimension) {
		outputSizeValueLabel
				.setText(String.format("%d x %d pixels", (int) outputDimension.getWidth(), (int) outputDimension.getHeight()));
	}

	public void setDefaultMagnification() {
		magnificationComboBox.setSelectedIndex(0);
	}

	public void setProgressIdle() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {}
		EventQueue.invokeLater(() -> {
			progressBar.setIndeterminate(false);
			progressBar.setStringPainted(false);
			progressBar.setValue(0);
		});
	}

	public CytomineToIcyPanelController getController() {
		return panelController;
	}

	public Optional<Double> getSelectedMagnification() {
		if (magnificationSourceCheckBox.isSelected()) {
			try {
				return Optional.of(Math.abs(Double.parseDouble(magnificationTextField.getText())));
			} catch (Exception e) {
				return Optional.ofNullable(null);
			}
		} else {
			return Optional.ofNullable((Double) magnificationComboBox.getSelectedItem());
		}
	}

	public void setMagnificationEnabled(boolean enabled) {
		EventQueue.invokeLater(() -> {
			magnificationSourceCheckBox.setEnabled(enabled);
			magnificationComboBox.setEnabled(enabled);
			magnificationTextField.setEnabled(enabled);
		});
	}
}
