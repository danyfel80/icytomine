package org.bioimageanalysis.icy.icytomine.ui.core.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.view.ViewCanvasPanel;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.provider.ViewProvider;

@SuppressWarnings("serial")
public class ViewerPanel extends JPanel {

	public interface ZoomEventListener {
		public void zoomChanged(double newZoomLevel);
	}

	public final class ViewPanelResizeToParentListener extends ComponentAdapter {
		@Override
		public void componentResized(ComponentEvent e) {
			viewCanvasPanel.setSize(viewCanvasPanel.getParent().getSize());
		}
	}

	private ViewProvider viewProvider;

	private JMenuBar menuBar;
	private JMenuItem cytomineToIcyMenuItem;
	private JMenuItem icySequenceToCytomineMenuItem;
	private JMenuItem icyFileToCytomineMenuItem;
	private JMenuItem icyFolderToCytomineMenuItem;
	private JMenuItem menuAnnotationsItem;

	private JLayeredPane layeredViewPane;

	private ViewCanvasPanel viewCanvasPanel;

	private JButton zoomInButton;
	private JButton zoomOutButton;
	private JButton zoomLevelButton;
	private List<JButton> specificZoomLevelButtons;

	private JPanel statusBar;
	private JLabel statusIcon;
	private JLabel statusMessage;
	private JLabel cursorPositionLabel;
	private JLabel cursorPixelColorLabel;

	private Set<ZoomEventListener> zoomLevelListeners;

	private double zoomLimit;

	public ViewerPanel(ViewProvider viewProvider) {
		this.viewProvider = viewProvider;
		setPreferredSize(new Dimension(600, 400));
		zoomLevelListeners = new HashSet<>();
		buildComponents();
	}

	private void buildComponents() {
		setLayout(new BorderLayout(0, 0));

		buildMenuBar();
		buildLayeredViewPane();
		buildStatusBar();

		add(menuBar, BorderLayout.NORTH);
		add(layeredViewPane, BorderLayout.CENTER);
		add(statusBar, BorderLayout.SOUTH);
	}

	private void buildMenuBar() {
		menuBar = new JMenuBar();
		JMenu transferMenu = createTransferMenu();
		menuBar.add(transferMenu);

		JMenu annotationsMenu = createAnnotationsMenu();
		menuBar.add(annotationsMenu);
	}

	private JMenu createTransferMenu() {
		JMenu menu = new JMenu("Transfer");

		cytomineToIcyMenuItem = new JMenuItem("Cytomine -> Icy...");
		menu.add(cytomineToIcyMenuItem);

		JMenu icyToCytomineMenu = new JMenu("Icy -> Cytomine");
		icySequenceToCytomineMenuItem = new JMenuItem("From image...");
		icyToCytomineMenu.add(icySequenceToCytomineMenuItem);
		icyFileToCytomineMenuItem = new JMenuItem("From File...");
		icyToCytomineMenu.add(icyFileToCytomineMenuItem);
		icyFolderToCytomineMenuItem = new JMenuItem("From Folder...");
		icyToCytomineMenu.add(icyFolderToCytomineMenuItem);
		menu.add(icyToCytomineMenu);

		return menu;
	}

	private JMenu createAnnotationsMenu() {
		JMenu menu = new JMenu("Annotations");
		menuAnnotationsItem = new JMenuItem("Filter...");
		menu.add(menuAnnotationsItem);
		return menu;
	}

	private void buildLayeredViewPane() {
		layeredViewPane = new JLayeredPane();

		buildViewCanvasPanel();
		buildZoomControls();

		addComponentsInLayers();

		setViewPanelAutoResize();
	}

	private void buildViewCanvasPanel() {
		viewCanvasPanel = new ViewCanvasPanel(viewProvider);
		viewCanvasPanel.setBounds(0, 0, 600, 400);
	}

	private void buildZoomControls() {
		setZoomInButton();
		setZoomOutButton();
		setZoomLevelButton();
	}

	private void setZoomInButton() {
		zoomInButton = createZoomButton("+");
		zoomInButton.setBounds(10, 10, 40, 40);
	}

	private void setZoomOutButton() {
		zoomOutButton = createZoomButton("-");
		zoomOutButton.setBounds(10, 80, 40, 40);
	}

	private JButton createZoomButton(String label) {
		JButton zoomButton = new JButton(label);
		zoomButton.setMinimumSize(new Dimension(40, 40));
		zoomButton.setMaximumSize(new Dimension(40, 40));
		zoomButton.setPreferredSize(new Dimension(40, 40));
		zoomButton.setMargin(new Insets(0, 0, 0, 0));
		zoomButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		return zoomButton;
	}

	private void setZoomLevelButton() {
		zoomLevelButton = new JButton("xx X");
		zoomLevelButton.setFont(zoomLevelButton.getFont().deriveFont(Font.BOLD));
		zoomLevelButton.setOpaque(true);
		zoomLevelButton.setContentAreaFilled(false);
		zoomLevelButton.setBorderPainted(false);
		zoomLevelButton.setHorizontalTextPosition(SwingConstants.CENTER);
		zoomLevelButton.setHorizontalAlignment(SwingConstants.CENTER);
		zoomLevelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		zoomLevelButton.setBounds(0, 50, 60, 30);
		setZoomLevelButtons();
		zoomLevelButton.addActionListener(l -> showZoomLevels());
	}

	private void setZoomLevelButtons() {
		double[] levels = new double[] { 1.25, 2.5, 5, 10, 20, 40 };
		specificZoomLevelButtons = new ArrayList<>(levels.length);
		Point position = new Point(60, 50);
		for (double zoomLevel : levels) {
			JButton button = createSpecificZoomLevelButton(zoomLevel);
			button.setVisible(false);
			button.setLocation(position);
			specificZoomLevelButtons.add(button);
			position.setLocation(position.getX() + 65, position.getY());
			button.addActionListener(event -> selectSpecificZoomLevel(zoomLevel));
		}
	}

	private void selectSpecificZoomLevel(double newZoomLevel) {
		specificZoomLevelButtons.forEach(b -> b.setVisible(false));
		zoomLevelListeners.forEach(l -> l.zoomChanged(newZoomLevel));
	}

	private JButton createSpecificZoomLevelButton(double zoomLevel) {
		DecimalFormat df = new DecimalFormat("#.##");
		JButton button = new JButton(df.format(zoomLevel) + "x");
		button.setSize(60, 30);
		return button;
	}

	private void showZoomLevels() {
		specificZoomLevelButtons.forEach(btn -> btn.setVisible(!btn.isVisible()));
	}

	private void addComponentsInLayers() {
		addInLayer(viewCanvasPanel, 0);
		addInLayer(zoomInButton, 1);
		addInLayer(zoomLevelButton, 1);
		addInLayer(zoomOutButton, 1);
		addAllInLayer(specificZoomLevelButtons, 2);
	}

	private void addInLayer(JComponent component, int z) {
		layeredViewPane.setLayout(null);
		layeredViewPane.add(component);
		layeredViewPane.setLayer(component, z);
	}

	private void addAllInLayer(List<? extends JComponent> components, int layer) {
		components.forEach(cpnt -> addInLayer(cpnt, layer));
	}

	private void setViewPanelAutoResize() {
		this.addComponentListener(new ViewPanelResizeToParentListener());
	}

	private void buildStatusBar() {
		statusBar = new JPanel();
		GridBagLayout statusBarLayout = new GridBagLayout();
		statusBarLayout.columnWidths = new int[] { 16, 0, 0, 0 };
		statusBarLayout.rowHeights = new int[] { 1 };
		statusBarLayout.columnWeights = new double[] { 0d, 1.0, 0.0, 0d };
		statusBarLayout.rowWeights = new double[] { 0.0 };
		statusBar.setLayout(statusBarLayout);

		statusIcon = new JLabel("");
		statusIcon.setBorder(null);
		statusIcon.setIcon(new ImageIcon(ViewerPanel.class
				.getResource("/org/apache/log4j/lf5/viewer/images/channelexplorer_satellite.gif")));

		GridBagConstraints statusIconConstraints = new GridBagConstraints();
		statusIconConstraints.anchor = GridBagConstraints.WEST;
		statusIconConstraints.insets = new Insets(0, 0, 0, 5);
		statusIconConstraints.gridx = 0;
		statusIconConstraints.gridy = 0;
		statusBar.add(statusIcon, statusIconConstraints);

		statusMessage = new JLabel("status...");
		GridBagConstraints statusLabelConstraints = new GridBagConstraints();
		statusLabelConstraints.anchor = GridBagConstraints.WEST;
		statusLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
		statusLabelConstraints.insets = new Insets(0, 0, 0, 5);
		statusLabelConstraints.gridx = 1;
		statusLabelConstraints.gridy = 0;
		statusBar.add(statusMessage, statusLabelConstraints);

		cursorPositionLabel = new JLabel("Position: x=13 px (54.4 \u00B5m), y=434 px (549 \u00B5m)");
		GridBagConstraints positionLabelConstraints = new GridBagConstraints();
		positionLabelConstraints.anchor = GridBagConstraints.WEST;
		positionLabelConstraints.insets = new Insets(0, 0, 0, 5);
		positionLabelConstraints.gridx = 2;
		positionLabelConstraints.gridy = 0;
		statusBar.add(cursorPositionLabel, positionLabelConstraints);

		cursorPixelColorLabel = new JLabel("");
		cursorPixelColorLabel.setBackground(Color.GREEN);
		cursorPixelColorLabel.setOpaque(true);
		cursorPixelColorLabel.setPreferredSize(new Dimension(14, 14));
		GridBagConstraints pixelColorLabelConstraints = new GridBagConstraints();
		pixelColorLabelConstraints.gridx = 3;
		pixelColorLabelConstraints.gridy = 0;
		statusBar.add(cursorPixelColorLabel, pixelColorLabelConstraints);

		viewProvider.addViewProcessListener(isProcessing -> {
			statusIcon.setVisible(isProcessing);
			if (isProcessing)
				statusMessage.setText("Retreiving image from server...");
			else
				statusMessage.setText("Ready...");
		});
	}

	public ViewCanvasPanel getViewCanvasPanel() {
		return viewCanvasPanel;
	}

	public void setCursorPosition(Point2D position, Point2D positionInMicrons) {
		cursorPositionLabel.setText(String.format("Position: x=%.2f px (%.2f \u00B5m), y=%.2f px (%.2f \u00B5m)",
				position.getX(), positionInMicrons.getX(), position.getY(), positionInMicrons.getY()));
	}

	public void addZoomInListener(ActionListener listener) {
		zoomInButton.addActionListener(listener);
	}

	public void addZoomOutListener(ActionListener listener) {
		zoomOutButton.addActionListener(listener);
	}

	public void addZoomLevelSelectedListener(ZoomEventListener listener) {
		zoomLevelListeners.add(listener);
	}

	public void setZoomLevel(double zoomLevel) {
		zoomLevelButton.setText(String.format("%.1f X", zoomLevel));
		if (zoomLevel <= zoomLimit)
			zoomLevelButton.setForeground(Color.BLACK);
		else
			zoomLevelButton.setForeground(Color.RED);
	}
	
	public void setZoomLimit(double limit) {
		this.zoomLimit = limit;
	}

	public void addCytomineToIcyMenuListener(ActionListener actionListener) {
		cytomineToIcyMenuItem.addActionListener(actionListener);
	}

	public void addIcySequenceToCytomineMenuListener(ActionListener actionListener) {
		icySequenceToCytomineMenuItem.addActionListener(actionListener);
	}

	public void addIcyFileToCytomineMenuListener(ActionListener actionListener) {
		icyFileToCytomineMenuItem.addActionListener(actionListener);
	}

	public void addIcyFolderToCytomineMenuListener(ActionListener actionListener) {
		icyFolderToCytomineMenuItem.addActionListener(actionListener);
	}

	public void addAnnotationMenuListener(ActionListener listener) {
		menuAnnotationsItem.addActionListener(listener);
	}
}
