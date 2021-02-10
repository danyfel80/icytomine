package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.icy2Cytomine.folder;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.ViewController;

@SuppressWarnings("serial")
public class IcyFolderToCytominePanel extends JPanel {

	private JLabel folderPathLabel;
	private JButton selectFolderButton;
	private JProgressBar transferProgressBar;
	private JButton sendButton;
	private JButton cancelButton;
	private IcyFolderToCytominePanelController panelController;

	/**
	 * Create the panel.
	 */
	public IcyFolderToCytominePanel(ViewController viewController) {
		setView();
		setController(viewController);
	}

	private void setView() {
		setPreferredSize(new Dimension(330, 150));
		setPanelLayout();
		addInstructions();
		addFolderSelection();
		addProgressBar();
		addActionPanel();
	}

	private void setPanelLayout() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);
	}

	private void addInstructions() {
		JLabel instructionsLabel = new JLabel("Select a folder to send the annotations on images inside that folder.");
		GridBagConstraints instructionsLabelConstraints = new GridBagConstraints();
		instructionsLabelConstraints.gridwidth = 3;
		instructionsLabelConstraints.insets = new Insets(5, 5, 5, 5);
		instructionsLabelConstraints.gridx = 0;
		instructionsLabelConstraints.gridy = 0;
		add(instructionsLabel, instructionsLabelConstraints);
	}

	private void addFolderSelection() {
		JLabel fileLabel = new JLabel("Folder:");
		GridBagConstraints fileLabelConstraints = new GridBagConstraints();
		fileLabelConstraints.anchor = GridBagConstraints.EAST;
		fileLabelConstraints.insets = new Insets(0, 5, 5, 5);
		fileLabelConstraints.gridx = 0;
		fileLabelConstraints.gridy = 1;
		add(fileLabel, fileLabelConstraints);

		folderPathLabel = new JLabel("No folder selected");
		GridBagConstraints filePathLabelConstraints = new GridBagConstraints();
		filePathLabelConstraints.insets = new Insets(0, 0, 5, 5);
		filePathLabelConstraints.gridx = 1;
		filePathLabelConstraints.gridy = 1;
		add(folderPathLabel, filePathLabelConstraints);

		selectFolderButton = new JButton("Select folder...");
		GridBagConstraints selectFileButtonConstraints = new GridBagConstraints();
		selectFileButtonConstraints.anchor = GridBagConstraints.WEST;
		selectFileButtonConstraints.insets = new Insets(0, 0, 5, 5);
		selectFileButtonConstraints.gridx = 2;
		selectFileButtonConstraints.gridy = 1;
		add(selectFolderButton, selectFileButtonConstraints);
	}

	private void addProgressBar() {
		transferProgressBar = new JProgressBar();
		GridBagConstraints transferProgressBarConstraints = new GridBagConstraints();
		transferProgressBarConstraints.fill = GridBagConstraints.HORIZONTAL;
		transferProgressBarConstraints.gridwidth = 3;
		transferProgressBarConstraints.insets = new Insets(0, 5, 5, 5);
		transferProgressBarConstraints.gridx = 0;
		transferProgressBarConstraints.gridy = 2;
		add(transferProgressBar, transferProgressBarConstraints);
	}

	private void addActionPanel() {
		JPanel actionPanel = new JPanel();
		GridBagConstraints actionPanelConstraints = new GridBagConstraints();
		actionPanelConstraints.gridwidth = 3;
		actionPanelConstraints.insets = new Insets(0, 5, 5, 5);
		actionPanelConstraints.fill = GridBagConstraints.HORIZONTAL;
		actionPanelConstraints.gridx = 0;
		actionPanelConstraints.gridy = 3;
		add(actionPanel, actionPanelConstraints);

		GridBagLayout actionPanelLayout = new GridBagLayout();
		actionPanelLayout.columnWidths = new int[] { 119, 89 };
		actionPanelLayout.rowHeights = new int[] { 23 };
		actionPanelLayout.columnWeights = new double[] { 0.0, 0.0 };
		actionPanelLayout.rowWeights = new double[] { 0.0 };
		actionPanel.setLayout(actionPanelLayout);

		sendButton = new JButton("Send");
		GridBagConstraints sendButtonConstraints = new GridBagConstraints();
		sendButtonConstraints.insets = new Insets(0, 0, 0, 5);
		sendButtonConstraints.gridx = 0;
		sendButtonConstraints.gridy = 0;
		actionPanel.add(sendButton, sendButtonConstraints);

		cancelButton = new JButton("Cancel");
		GridBagConstraints cancelButtonConstraints = new GridBagConstraints();
		cancelButtonConstraints.anchor = GridBagConstraints.NORTHWEST;
		cancelButtonConstraints.gridx = 1;
		cancelButtonConstraints.gridy = 0;
		actionPanel.add(cancelButton, cancelButtonConstraints);
	}

	private void setController(ViewController viewController) {
		if (panelController == null) {
			panelController = new IcyFolderToCytominePanelController(this, viewController);
		}
	}

	protected JLabel getFolderPathLabel() {
		return folderPathLabel;
	}

	protected JButton getSelectFolderButton() {
		return selectFolderButton;
	}

	public JProgressBar getTransferProgressBar() {
		return transferProgressBar;
	}

	protected JButton getSendButton() {
		return sendButton;
	}

	protected JButton getCancelButton() {
		return cancelButton;
	}

	public IcyFolderToCytominePanelController getController() {
		return panelController;
	}
}
