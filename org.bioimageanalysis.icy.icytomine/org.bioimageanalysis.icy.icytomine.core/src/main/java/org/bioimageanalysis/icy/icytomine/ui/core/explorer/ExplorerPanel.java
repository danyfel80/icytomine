package org.bioimageanalysis.icy.icytomine.ui.core.explorer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;

import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.ui.core.explorer.ImagePanel.ImageSelectionListener;

public class ExplorerPanel extends JPanel {
	private static final long serialVersionUID = 2595438652951822963L;

	private ProjectPanel projectPanel;
	private ImagePanel imagePanel;
	private ProjectDetailsPanel projectDetailsPanel;
	private ImageDetailsPanel imageDetailsPanel;
	private JSplitPane navigationPane;
	private JLabel hostAddressLabel;
	private JPanel mainPanel;

	private ExplorerPanelController controller;

	/**
	 * Create the explorer panel with empty data. To use cytomine data, use
	 * {@link #setClient(CytomineClient)}.
	 */
	public ExplorerPanel() {
		setView();
		setController();
	}

	private void setView() {
		setBorder(null);
		setBackground(SystemColor.control);
		setMinimumSize(new Dimension(500, 300));
		setPreferredSize(new Dimension(730, 410));

		setGridBagLayout();
		setMenuBar();
		setMainPanel();
	}

	private void setGridBagLayout() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {0};
		gridBagLayout.rowHeights = new int[] {0, 0};
		gridBagLayout.columnWeights = new double[] {1.0};
		gridBagLayout.rowWeights = new double[] {0.0, 1.0};
		setLayout(gridBagLayout);
	}

	private void setMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		menuBar.setMinimumSize(new Dimension(0, 20));

		GridBagConstraints menuBarConstraints = new GridBagConstraints();
		menuBarConstraints.anchor = GridBagConstraints.NORTH;
		menuBarConstraints.fill = GridBagConstraints.BOTH;
		menuBarConstraints.gridx = 0;
		menuBarConstraints.gridy = 0;

		add(menuBar, menuBarConstraints);
	}

	private void setMainPanel() {
		createMainPanel();
		setHostComponents();
		setNavigationPane();

		GridBagConstraints mainPanelConstraints = new GridBagConstraints();
		mainPanelConstraints.fill = GridBagConstraints.BOTH;
		mainPanelConstraints.gridx = 0;
		mainPanelConstraints.gridy = 1;

		add(mainPanel, mainPanelConstraints);
	}

	private void createMainPanel() {
		mainPanel = new JPanel();

		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] {0, 0};
		gbl_panel.rowHeights = new int[] {0, 0};
		gbl_panel.columnWeights = new double[] {0.0, 1.0};
		gbl_panel.rowWeights = new double[] {0.0, 1.0};
		mainPanel.setLayout(gbl_panel);
	}

	private void setHostComponents() {
		JLabel hostTitleLabel = new JLabel("Cytomine host address");
		hostTitleLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		hostTitleLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		hostTitleLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		hostTitleLabel.setAlignmentY(Component.CENTER_ALIGNMENT);

		GridBagConstraints hostTitleLabelConstraints = new GridBagConstraints();
		hostTitleLabelConstraints.fill = GridBagConstraints.BOTH;
		hostTitleLabelConstraints.anchor = GridBagConstraints.EAST;
		hostTitleLabelConstraints.insets = new Insets(0, 0, 0, 5);
		hostTitleLabelConstraints.gridx = 0;
		hostTitleLabelConstraints.gridy = 0;
		mainPanel.add(hostTitleLabel, hostTitleLabelConstraints);

		hostAddressLabel = new JLabel("No server specified");

		GridBagConstraints hostAddressLabelConstraints = new GridBagConstraints();
		hostAddressLabelConstraints.fill = GridBagConstraints.BOTH;
		hostAddressLabelConstraints.anchor = GridBagConstraints.NORTHWEST;
		hostAddressLabelConstraints.gridx = 1;
		hostAddressLabelConstraints.gridy = 0;
		mainPanel.add(hostAddressLabel, hostAddressLabelConstraints);
	}

	private void setNavigationPane() {

		navigationPane = new JSplitPane();
		navigationPane.setDoubleBuffered(true);
		navigationPane.setContinuousLayout(true);
		navigationPane.setResizeWeight(0.66);
		navigationPane.setMinimumSize(new Dimension(730, 300));
		navigationPane.setPreferredSize(new Dimension(720, 390));
		navigationPane.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));

		setNavigationPaneDivisions();

		GridBagConstraints navigationPaneConstraints = new GridBagConstraints();
		navigationPaneConstraints.anchor = GridBagConstraints.NORTH;
		navigationPaneConstraints.fill = GridBagConstraints.BOTH;
		navigationPaneConstraints.gridwidth = 2;
		navigationPaneConstraints.gridx = 0;
		navigationPaneConstraints.gridy = 1;
		mainPanel.add(navigationPane, navigationPaneConstraints);
	}

	private void setNavigationPaneDivisions() {
		navigationPane.setDividerSize(2);

		JSplitPane projectImageSplitPane = new JSplitPane();
		projectImageSplitPane.setDoubleBuffered(true);
		projectImageSplitPane.setContinuousLayout(true);
		projectImageSplitPane.setResizeWeight(0.5);
		projectImageSplitPane.setDividerSize(2);
		projectImageSplitPane.setBorder(null);

		projectPanel = new ProjectPanel();
		projectImageSplitPane.setLeftComponent(projectPanel);

		imagePanel = new ImagePanel();
		projectImageSplitPane.setRightComponent(imagePanel);

		navigationPane.setLeftComponent(projectImageSplitPane);

		projectDetailsPanel = new ProjectDetailsPanel();
		imageDetailsPanel = new ImageDetailsPanel();

		navigationPane.setRightComponent(projectDetailsPanel);
	}

	private void setController() {
		this.controller = new ExplorerPanelController(this);
	}

	public JLabel getHostAddressLabel() {
		return hostAddressLabel;
	}

	public ProjectPanel getProjectPanel() {
		return projectPanel;
	}

	public ImagePanel getImagePanel() {
		return imagePanel;
	}

	public ProjectDetailsPanel getProjectDetailsPanel() {
		return projectDetailsPanel;
	}

	public ImageDetailsPanel getImageDetailsPanel() {
		return imageDetailsPanel;
	}

	public void showNoDetails() {
		navigationPane.setRightComponent(null);
	}

	public void showProjectDetails() {
		navigationPane.setRightComponent(projectDetailsPanel);
	}

	public void showImageDetails() {
		navigationPane.setRightComponent(imageDetailsPanel);
	}

	public void addOpenViewerListener(ImageSelectionListener listener) {
		controller.addImageViewerRequestListener(listener);
	}

	public void setClient(CytomineClient client) {
		controller.setClient(client);
	}

}
