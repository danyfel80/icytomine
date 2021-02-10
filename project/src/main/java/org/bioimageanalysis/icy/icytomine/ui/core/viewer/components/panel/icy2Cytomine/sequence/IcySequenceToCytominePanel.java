package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.icy2Cytomine.sequence;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.ViewController;

@SuppressWarnings("serial")
public class IcySequenceToCytominePanel extends JPanel {
	private JComboBox<SequenceItem> sequenceComboBox;
	private JCheckBox roiSelectionCheckBox;
	private JProgressBar transferProgressBar;
	private JButton sendButton;
	private JButton cancelButton;

	private IcySequenceToCytominePanelController panelController;

	public IcySequenceToCytominePanel(ViewController viewController) {
		setView();
		setPanelController(viewController);
	}

	private void setView() {
		setPreferredSize(new Dimension(300, 220));
		setGridBagLayout();
		addInstructionsMessage();
		addSequenceSelection();
		addRoiSelection();
		addProgressBar();
		addActionButtons();
	}

	private void setGridBagLayout() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 };
		setLayout(gridBagLayout);
	}

	private void addInstructionsMessage() {
		JLabel messageLabel = new JLabel("Please select the sequence to send");
		addWithConstraints(messageLabel, this, 0, 0, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 5, 5, 5, 5);

	}

	private void addSequenceSelection() {
		JLabel sequenceLabel = new JLabel("Sequence");
		addWithConstraints(sequenceLabel, this, 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0, 5, 5,
				5);

		sequenceComboBox = new JComboBox<>();
		addWithConstraints(sequenceComboBox, this, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 0,
				0, 5, 5);
	}

	private void addRoiSelection() {
		roiSelectionCheckBox = new JCheckBox("Send only selected ROI's");
		addWithConstraints(roiSelectionCheckBox, this, 0, 2, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 0, 5,
				5, 5);
	}

	private void addProgressBar() {
		transferProgressBar = new JProgressBar();
		addWithConstraints(transferProgressBar, this, 0, 3, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 0, 5,
				5, 5);

	}

	private void addActionButtons() {
		JPanel panel = getActionButtonPanel();
		addWithConstraints(panel, this, 0, 4, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 0, 5, 5, 5);
	}

	private JPanel getActionButtonPanel() {
		JPanel panel = new JPanel();
		GridBagLayout panelLayout = new GridBagLayout();
		panelLayout.columnWidths = new int[] { 0, 0 };
		panelLayout.rowHeights = new int[] { 0 };
		panelLayout.columnWeights = new double[] { 0.0, 0.0 };
		panelLayout.rowWeights = new double[] { 0.0 };
		panel.setLayout(panelLayout);

		sendButton = new JButton("Send");
		addWithConstraints(sendButton, panel, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 0, 0, 0, 5);

		cancelButton = new JButton("Cancel");
		addWithConstraints(cancelButton, panel, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 0, 0, 0, 0);

		return panel;
	}

	private void addWithConstraints(Component component, Container container, int x, int y, int width, int height,
			int anchor, int fill, int topInset, int leftInset, int bottomInset, int rightInset) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = anchor;
		constraints.fill = fill;
		constraints.gridx = x;
		constraints.gridy = y;
		constraints.gridwidth = width;
		constraints.gridheight = height;
		constraints.insets = new Insets(10, 10, 10, 10);
		container.add(component, constraints);
	}

	private void setPanelController(ViewController viewController) {
		if (viewController != null) {
			panelController = new IcySequenceToCytominePanelController(this, viewController);
		}
	}

	public void addCloseListener(ActionListener listener) {
		panelController.addCloseListener(listener);
	}

	protected JComboBox<SequenceItem> getSequenceComboBox() {
		return sequenceComboBox;
	}

	protected JCheckBox getRoiSelectionCheckBox() {
		return roiSelectionCheckBox;
	}

	protected JProgressBar getTransferProgressBar() {
		return transferProgressBar;
	}

	protected JButton getSendButton() {
		return sendButton;
	}

	protected JButton getCancelButton() {
		return cancelButton;
	}

	public IcySequenceToCytominePanelController getController() {
		return panelController;
	}
}
