package org.bioimageanalysis.icy.icytomine.ui.core.explorer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionListener;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.model.Project;

public class ImagePanel extends JPanel {
	public static class ImageItem {
		private Image image;

		public ImageItem(Image image) {
			this.image = image;
		}

		public Image getImage() {
			return image;
		}

		@Override
		public String toString() {
			return image.getName().orElse(String.format("Not specified (id=%d)", image.getId().longValue()));
		}
	}

	@FunctionalInterface
	public interface ImageSelectionListener {
		public void imageSelected(Image image);
	}

	private static final long serialVersionUID = 5990256964181871478L;

	private CytomineClient client;
	private Project currentProject;

	private JList<ImageItem> listImages;

	private Map<ImageSelectionListener, ListSelectionListener> listSelectionListeners;

	/**
	 * Creates an empty image panel. To fill with cytomine data use
	 * {@link #setClient(CytomineClient)}.
	 */
	public ImagePanel() {
		setMinimumSize(new Dimension(50, 50));
		setPreferredSize(new Dimension(240, 400));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JLabel lblImages = new JLabel("Images");
		lblImages.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblImages.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(lblImages);
		lblImages.setToolTipText("Images available for the selected project");
		lblImages.setBackground(SystemColor.control);
		lblImages.setHorizontalAlignment(SwingConstants.CENTER);

		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane);
		listImages = new JList<>();
		listImages.setBackground(SystemColor.window);
		listImages.setModel(new DefaultListModel<>());
		listImages.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lblImages.setLabelFor(listImages);
		scrollPane.setViewportView(listImages);

		listSelectionListeners = new HashMap<>();
	}

	public void setClient(CytomineClient client) {
		this.client = client;
	}

	public CytomineClient getClient() {
		return client;
	}

	public Project getCurrentProject() {
		return currentProject;
	}

	public void setCurrentProject(Project project) {
		this.currentProject = project;
		if (getClient() != null) {
			try {
				updateImageList();
			} catch (CytomineClientException e) {
				e.printStackTrace();
				currentProject = null;
			}
		} else {
			System.err.println("No cytomine instance at image panel yet.");
		}
	}

	@SuppressWarnings("unchecked")
	public void addImageSelectionListener(ImageSelectionListener listener) {
		ListSelectionListener listSelectionListener = event -> {
			if (!event.getValueIsAdjusting()) {
				Optional<ImageItem> imageItem = Optional.ofNullable(((JList<ImageItem>) event.getSource()).getSelectedValue());
				if (imageItem.isPresent()) {
					listener.imageSelected(imageItem.get().getImage());
				} else {
					listener.imageSelected(null);
				}
			}
		};
		listSelectionListeners.put(listener, listSelectionListener);
		listImages.addListSelectionListener(listSelectionListener);
	}

	public void removeImageSelectionListener(ImageSelectionListener listener) {
		ListSelectionListener listSelectionListener = listSelectionListeners.get(listener);
		if (listSelectionListener != null) {
			listImages.removeListSelectionListener(listSelectionListener);
		}
	}

	public void addImageDoubleClickListener(ImageSelectionListener listener) {
		listImages.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int index = listImages.locationToIndex(e.getPoint());
					if (index > -1) {
						listener.imageSelected(listImages.getSelectedValue().getImage());
					}
				}
			}
		});
	}

	public void updateImageList() throws CytomineClientException {
		if (getCurrentProject() != null) {
			List<Image> imageCollection = client.getProjectImages(getCurrentProject().getId());
			ImageItem[] images = imageCollection.stream().map(i -> new ImageItem(i)).toArray(ImageItem[]::new);
			listImages.setListData(images);
			listImages.clearSelection();
			listImages.setSelectedIndex(-1);
		} else {
			listImages.removeAll();
		}
	}

}
