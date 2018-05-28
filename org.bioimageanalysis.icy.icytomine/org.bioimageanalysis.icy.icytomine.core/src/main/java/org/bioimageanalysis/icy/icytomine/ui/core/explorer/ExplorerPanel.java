package org.bioimageanalysis.icy.icytomine.ui.core.explorer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;

import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.bioimageanalysis.icy.icytomine.core.model.Project;
import org.bioimageanalysis.icy.icytomine.ui.core.explorer.ImagePanel.ImageSelectionListener;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;

import be.cytomine.client.Cytomine;
import be.cytomine.client.CytomineException;
import icy.plugin.PluginLoader;

public class ExplorerPanel extends JPanel {
	private static final long serialVersionUID = 2595438652951822963L;

	private ProjectPanel				projectPanel;
	private ImagePanel					imagePanel;
	private ProjectDetailsPanel	projectDetailsPanel;
	private ImageDetailsPanel		imageDetailsPanel;
	private JSplitPane					splitPaneDetails;
	private JLabel							lblHostValue;
	private JPanel							panel;
	private Cytomine						cytomine;

	private Cache<Long, BufferedImage> previewCache;

	private ImageSelectionListener openViewerListener;

	/**
	 * Create the explorer panel with empty data. To use cytomine data, use
	 * {@link #ExplorerPanel(Cytomine)}.
	 */
	public ExplorerPanel() {
		setBorder(null);
		setBackground(SystemColor.control);
		setMinimumSize(new Dimension(500, 300));
		setPreferredSize(new Dimension(730, 410));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0 };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0 };
		setLayout(gridBagLayout);
		JMenuBar menuBar = new JMenuBar();
		menuBar.setMinimumSize(new Dimension(0, 20));
		GridBagConstraints gbc_menuBar = new GridBagConstraints();
		gbc_menuBar.anchor = GridBagConstraints.NORTH;
		gbc_menuBar.fill = GridBagConstraints.BOTH;
		gbc_menuBar.gridx = 0;
		gbc_menuBar.gridy = 0;
		add(menuBar, gbc_menuBar);

		panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, 1.0 };
		gbl_panel.rowWeights = new double[] { 0.0, 1.0 };
		panel.setLayout(gbl_panel);

		JLabel lblHostTitle = new JLabel("Cytomine host address");
		GridBagConstraints gbc_lblHostTitle = new GridBagConstraints();
		gbc_lblHostTitle.fill = GridBagConstraints.BOTH;
		gbc_lblHostTitle.anchor = GridBagConstraints.EAST;
		gbc_lblHostTitle.insets = new Insets(0, 0, 0, 5);
		gbc_lblHostTitle.gridx = 0;
		gbc_lblHostTitle.gridy = 0;
		panel.add(lblHostTitle, gbc_lblHostTitle);
		lblHostTitle.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblHostTitle.setHorizontalAlignment(SwingConstants.RIGHT);
		lblHostTitle.setAlignmentX(Component.RIGHT_ALIGNMENT);
		lblHostTitle.setAlignmentY(Component.CENTER_ALIGNMENT);

		lblHostValue = new JLabel("http://localhost-example/");
		GridBagConstraints gbc_lblHost = new GridBagConstraints();
		gbc_lblHost.fill = GridBagConstraints.BOTH;
		gbc_lblHost.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblHost.gridx = 1;
		gbc_lblHost.gridy = 0;
		panel.add(lblHostValue, gbc_lblHost);

		splitPaneDetails = new JSplitPane();
		splitPaneDetails.setDoubleBuffered(true);
		splitPaneDetails.setContinuousLayout(true);
		splitPaneDetails.setResizeWeight(0.66);
		splitPaneDetails.setMinimumSize(new Dimension(730, 390));
		GridBagConstraints gbc_splitPaneDetails = new GridBagConstraints();
		gbc_splitPaneDetails.anchor = GridBagConstraints.NORTH;
		gbc_splitPaneDetails.fill = GridBagConstraints.BOTH;
		gbc_splitPaneDetails.gridwidth = 2;
		gbc_splitPaneDetails.gridx = 0;
		gbc_splitPaneDetails.gridy = 1;
		panel.add(splitPaneDetails, gbc_splitPaneDetails);
		splitPaneDetails.setDividerSize(2);
		splitPaneDetails.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		splitPaneDetails.setPreferredSize(new Dimension(720, 300));
		JSplitPane splitPaneImages = new JSplitPane();
		splitPaneImages.setDoubleBuffered(true);
		splitPaneImages.setContinuousLayout(true);
		splitPaneImages.setResizeWeight(0.5);
		splitPaneImages.setDividerSize(2);
		splitPaneImages.setBorder(null);
		splitPaneDetails.setLeftComponent(splitPaneImages);
		projectPanel = new ProjectPanel();
		projectPanel.addProjectSelectionListener(p -> {
			selectProject(p);
		});
		splitPaneImages.setLeftComponent(projectPanel);

		imagePanel = new ImagePanel();
		imagePanel.addImageSelectionListener(i -> {
			imageDetailsPanel.setCurrentImage(i);
			splitPaneDetails.setRightComponent(imageDetailsPanel);
		});
		imagePanel.addImageDoubleClickListener(i -> {
			if (openViewerListener != null) openViewerListener.imageSelected(i);
		});
		splitPaneImages.setRightComponent(imagePanel);

		CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
				.withClassLoader(PluginLoader.getLoader()).build(true);
		
//		CachingProvider provider = Caching.getCachingProvider();
//		CacheManager cacheManager = provider.getCacheManager();
		this.addAncestorListener(new AncestorListener() {

			@Override
			public void ancestorRemoved(AncestorEvent event) {
				cacheManager.removeCache("previewCache" + ExplorerPanel.this.hashCode());
				System.out.println("preview cache destroyed");
			}

			@Override
			public void ancestorMoved(AncestorEvent event) {}

			@Override
			public void ancestorAdded(AncestorEvent event) {
//				MutableConfiguration<Long, BufferedImage> cacheConfiguration = new MutableConfiguration<Long, BufferedImage>()
//						.setTypes(Long.class, BufferedImage.class).setStoreByValue(false)
//						.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.ONE_MINUTE));
//				
				CacheConfiguration<Long, BufferedImage> cacheConfiguration = CacheConfigurationBuilder
				.newCacheConfigurationBuilder(Long.class, BufferedImage.class, ResourcePoolsBuilder.heap(500)).withExpiry(Expirations.timeToLiveExpiration(Duration.of(1, TimeUnit.MINUTES))).build();
				
				previewCache = cacheManager.createCache("previewCache" + ExplorerPanel.this.hashCode(), cacheConfiguration);
				imageDetailsPanel.setPreviewCache(previewCache);
				System.out.println("preview cache created");
			}
		});

		projectDetailsPanel = new ProjectDetailsPanel();
		imageDetailsPanel = new ImageDetailsPanel();
		splitPaneDetails.setRightComponent(projectDetailsPanel);

		selectProject(null);
	}

	public ExplorerPanel(Cytomine cytomine) {
		this();
		setCytomine(cytomine);
	}

	public Cytomine getCytomine() {
		return cytomine;
	}

	public void setCytomine(Cytomine cytomine) {
		this.cytomine = cytomine;
		this.lblHostValue.setText(getCytomine().getHost());
		projectPanel.setCytomine(getCytomine());
		imagePanel.setCytomine(getCytomine());
		updateProjects();
		selectProject(null);
	}

	public void selectProject(Project project) {
		if (project == null) {
			splitPaneDetails.setRightComponent(null);
		} else {
			imagePanel.setCurrentProject(project);
			projectDetailsPanel.setCurrentProject(project);
			splitPaneDetails.setRightComponent(projectDetailsPanel);
		}
	}

	private void updateProjects() {
		try {
			projectPanel.updateProjectList();
		} catch (CytomineException e) {
			e.printStackTrace();
			selectProject(null);
		}
	}

	public void setOpenViewerListener(ImageSelectionListener listener) {
		this.openViewerListener = listener;
	}

}
