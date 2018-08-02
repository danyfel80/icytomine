package org.bioimageanalysis.icy.icytomine.ui.core.explorer;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.model.Project;
import org.bioimageanalysis.icy.icytomine.ui.core.explorer.ImagePanel.ImageItem;
import org.bioimageanalysis.icy.icytomine.ui.core.explorer.ImagePanel.ImageSelectionListener;

public class ImagePanelController {

	private ImagePanel panel;
	private Optional<Project> project;

	private Map<ImageSelectionListener, ListSelectionListener> listSelectionListeners;
	private Map<ImageSelectionListener, MouseListener> listDoubleClickListeners;

	private Set<Image> availableImages;
	private Image currentImage;

	public ImagePanelController(ImagePanel imagePanel) {
		this.panel = imagePanel;
		this.project = Optional.ofNullable(null);
		this.listSelectionListeners = new HashMap<>();
		this.listDoubleClickListeners = new HashMap<>();
		this.availableImages = new HashSet<>(0);
		this.currentImage = null;

		panel.getSearchBar().getDocument().addDocumentListener(getSearchBarChangeHandler());
	}

	private DocumentListener getSearchBarChangeHandler() {
		return new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateImageList();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateImageList();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateImageList();
			}
		};
	}

	private void updateImageList() throws CytomineClientException {
		applySearchCriterion();
		ImageItem[] images = getImageItemArray(availableImages);

		panel.getImageList().setValueIsAdjusting(true);
		panel.getImageList().setListData(images);
		if (availableImages.size() == 1) {
			panel.getImageList().setSelectedValue(images[0], true);
		} else if (currentImage != null && availableImages.contains(currentImage)) {
			panel.getImageList().setSelectedValue(new ImageItem(currentImage), true);
		} else {
			panel.getImageList().clearSelection();
		}
		panel.getImageList().setValueIsAdjusting(false);
	}

	private ImageItem[] getImageItemArray(Collection<Image> imageCollection) {
		return imageCollection.stream().map(ima -> new ImageItem(ima)).toArray(ImageItem[]::new);
	}

	private void applySearchCriterion() {
		Pattern searchPattern = getSearchPattern();
		availableImages = getProjectImages().stream()
				.filter(p -> searchPattern.matcher(p.getName().orElse("not available").toLowerCase()).matches())
				.collect(Collectors.toSet());
	}

	private Pattern getSearchPattern() {
		String searchCriterion = panel.getSearchBar().getText();
		if (searchCriterion.isEmpty()) {
			return Pattern.compile(".*");
		}
		return Pattern.compile(".*" + searchCriterion.toLowerCase() + ".*");
	}

	private List<Image> getProjectImages() {
		if (project.isPresent()) {
			return project.get().getImages(false);
		}
		return new ArrayList<>(0);
	}

	public void setProject(Project project) {
		if (!Objects.equals(this.project.orElse(null), project)) {
			this.project = Optional.ofNullable(project);
			try {
				updateImageList();
			} catch (CytomineClientException e) {
				e.printStackTrace();
			}
		}
	}

	public void addImageSelectionListener(ImageSelectionListener listener) {
		ListSelectionListener listSelectionListener = createListSelectionListener(listener);
		listSelectionListeners.put(listener, listSelectionListener);
		panel.getImageList().addListSelectionListener(listSelectionListener);
	}

	private ListSelectionListener createListSelectionListener(ImageSelectionListener listener) {
		return event -> {
			if (!event.getValueIsAdjusting()) {
				Image image = panel.getSelectedImage();
				if (!Objects.equals(currentImage, image)) {
					currentImage = image;
					listener.imageSelected(currentImage);
				}
			}
		};
	}

	public void removeImageSelectionListener(ImageSelectionListener listener) {
		ListSelectionListener listSelectionListener = listSelectionListeners.remove(listener);
		if (listSelectionListener != null) {
			panel.getImageList().removeListSelectionListener(listSelectionListener);
		}
	}

	public void addImageDoubleClickListener(ImageSelectionListener listener) {
		MouseListener listDoubleClickListener = createListDoubleClickListener(listener);
		listDoubleClickListeners.put(listener, listDoubleClickListener);
		panel.getImageList().addMouseListener(listDoubleClickListener);
	}

	private MouseListener createListDoubleClickListener(ImageSelectionListener listener) {
		return new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int index = panel.getImageList().locationToIndex(e.getPoint());
					if (index > -1) {
						listener.imageSelected(panel.getImageList().getSelectedValue().getImage());
					}
				}
			}
		};
	}

	public void removeImageDoubleClickListener(ImageSelectionListener listener) {
		MouseListener listDoubleClickListener = listDoubleClickListeners.remove(listener);
		if (listDoubleClickListener != null) {
			panel.getImageList().removeMouseListener(listDoubleClickListener);
		}
	}

}
