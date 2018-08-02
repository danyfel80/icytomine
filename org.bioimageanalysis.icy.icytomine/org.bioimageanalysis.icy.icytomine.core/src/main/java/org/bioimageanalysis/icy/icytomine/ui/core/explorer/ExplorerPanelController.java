package org.bioimageanalysis.icy.icytomine.ui.core.explorer;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
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

import icy.plugin.PluginLoader;

public class ExplorerPanelController {

	private ExplorerPanel panel;

	private Set<ImageSelectionListener> viewerRequestListeners;

	public ExplorerPanelController(ExplorerPanel explorerPanel) {
		this.panel = explorerPanel;
		this.viewerRequestListeners = new HashSet<>();
		setEventHandlers();
		setCacheManagement();
	}

	private void setEventHandlers() {
		setProjectSelectionHandler();
		setImageSelectionHandler();
		setImageViewerRequestHandler();
	}

	private void setProjectSelectionHandler() {
		panel.getProjectPanel().addProjectSelectionListener(project -> onProjectSelected(project));
	}

	private void onProjectSelected(Project project) {
		if (project == null) {
			panel.showNoDetails();
		} else {
			panel.getProjectDetailsPanel().setCurrentProject(project);
			panel.showProjectDetails();
		}
		panel.getImagePanel().setProject(project);
	}

	private void setImageSelectionHandler() {
		panel.getImagePanel().addImageSelectionListener(image -> onImageSelected(image));
	}

	private void onImageSelected(Image image) {
		if (image == null) {
			if (panel.getProjectPanel().getSelectedProject() == null) {
				panel.showNoDetails();
			} else {
				panel.showProjectDetails();
			}
		} else {
			panel.getImageDetailsPanel().setCurrentImage(image);
			panel.showImageDetails();
		}
	}

	private void setImageViewerRequestHandler() {
		panel.getImagePanel().addImageDoubleClickListener(image -> {
			viewerRequestListeners.forEach(listener -> listener.imageSelected(image));
		});
	}

	private void setCacheManagement() {
		CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().withClassLoader(PluginLoader.getLoader())
				.build(true);

		panel.addAncestorListener(new AncestorListener() {

			@Override
			public void ancestorRemoved(AncestorEvent event) {
				cacheManager.removeCache(getPreviewCacheAlias());
				System.out.println("preview cache destroyed");
			}

			@Override
			public void ancestorMoved(AncestorEvent event) {}

			@Override
			public void ancestorAdded(AncestorEvent event) {
				CacheConfiguration<Long, BufferedImage> cacheConfiguration = CacheConfigurationBuilder
						.newCacheConfigurationBuilder(Long.class, BufferedImage.class, ResourcePoolsBuilder.heap(500))
						.withExpiry(Expirations.timeToLiveExpiration(Duration.of(1, TimeUnit.MINUTES))).build();

				Cache<Long, BufferedImage> previewCache = cacheManager.createCache(getPreviewCacheAlias(), cacheConfiguration);
				panel.getImageDetailsPanel().setPreviewCache(previewCache);
				System.out.println("preview cache created");
			}
		});
	}

	private String getPreviewCacheAlias() {
		return "previewCache" + panel.hashCode();
	}

	public void setClient(CytomineClient client) {
		panel.getHostAddressLabel().setText(client.getHost());
		panel.getProjectPanel().setClient(client);

		updateProjects();
	}

	private void updateProjects() {
		try {
			panel.getProjectPanel().updateProjectList();
		} catch (CytomineClientException e) {
			e.printStackTrace();
			onProjectSelected(null);
		}
	}

	public void addImageViewerRequestListener(ImageSelectionListener listener) {
		this.viewerRequestListeners.add(listener);
	}

	public void removeImageViewerRequestListener(ImageSelectionListener listener) {
		this.viewerRequestListeners.remove(listener);
	}

}
