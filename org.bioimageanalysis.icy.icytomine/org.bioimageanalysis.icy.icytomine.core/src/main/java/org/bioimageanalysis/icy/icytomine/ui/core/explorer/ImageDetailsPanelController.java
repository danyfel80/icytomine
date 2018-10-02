package org.bioimageanalysis.icy.icytomine.ui.core.explorer;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.ImageIcon;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.ehcache.Cache;

public class ImageDetailsPanelController {

	public interface ImageMagnificationChangeListener {
		void magnificationChanged(Image image, Integer newMagnification);
	}

	public interface ImageResolutionChangeListener {
		void resolutionChanged(Image image, Double newResolution);
	}

	private ImageDetailsPanel panel;
	private Image currentImage;
	private ExecutorService previewExecutor;
	private Cache<Long, BufferedImage> previewCache;

	private Set<ImageMagnificationChangeListener> magnificationChangeListeners;
	private Set<ImageResolutionChangeListener> resolutionChangeListeners;

	public ImageDetailsPanelController(ImageDetailsPanel panel) {
		this.panel = panel;
		magnificationChangeListeners = new HashSet<>();
		resolutionChangeListeners = new HashSet<>();
	}

	public void setPreviewCache(Cache<Long, BufferedImage> cache) {
		this.previewCache = cache;
	}

	public Image getCurrentImage() {
		return this.currentImage;
	}

	public void setCurrentImage(Image image) {
		this.currentImage = image;
		updateImageDetails();
	}

	public void updateImageDetails() {
		if (getCurrentImage() != null) {
			String id = "" + getCurrentImage().getId();

			String filename = getCurrentImage().getName().orElse("Not specified");

			String dimension;
			Optional<Double> dimensionX = getCurrentImage().getDimensionX(), dimensionY = getCurrentImage().getDimensionY();
			if (dimensionX.isPresent() && dimensionY.isPresent()) {
				dimension = String.format("%.2f x %.2f \u00B5m", dimensionX.get(), dimensionY.get());
			} else {
				dimension = "Not specified";
			}

			String magnification;
			Optional<Integer> magnificationValue = getCurrentImage().getMagnification();
			if (magnificationValue.isPresent()) {
				magnification = String.format("%dX", magnificationValue.get());
			} else {
				magnification = "Not specified";
			}

			String annotationsAlgo;
			Optional<Long> annotationsAlgoValue = getCurrentImage().getAnnotationsOfAlgorithmNumber();
			if (annotationsAlgoValue.isPresent()) {
				annotationsAlgo = annotationsAlgoValue.get().toString();
			} else {
				annotationsAlgo = "Not specified";
			}

			String annotationsUser;
			Optional<Long> annotationsUserValue = getCurrentImage().getAnnotationsOfUsersNumber();
			if (annotationsUserValue.isPresent()) {
				annotationsUser = annotationsUserValue.get().toString();
			} else {
				annotationsUser = "Not specified";
			}

			Optional<Dimension> sizeValue = getCurrentImage().getSize();
			String size;
			if (sizeValue.isPresent()) {
				size = String.format("%d x %d px", sizeValue.get().width, sizeValue.get().height);
			} else {
				size = "Not specified";
			}

			String resolution;
			Optional<Double> resolutionValue = getCurrentImage().getResolution();
			if (resolutionValue.isPresent()) {
				resolution = String.format("%f \u00B5m/px", resolutionValue.get());
			} else {
				resolution = "Not specified";
			}

			String depth;
			Optional<Long> depthValue = getCurrentImage().getDepth();
			if (depthValue.isPresent()) {
				depth = String.format("%d levels", depthValue.get());
			} else {
				depth = "Not specified";
			}

			String date;
			Optional<Calendar> dateValue = getCurrentImage().getCreationDate();
			if (dateValue.isPresent()) {
				DateFormat formatter = new SimpleDateFormat("d MMM yyyy HH:mm");
				date = formatter.format(dateValue.get().getTime());
			} else {
				date = "Not specified";
			}

			synchronized (panel.getPreviewLabel()) {
				panel.getPreviewLabel().setIcon(null);
				if (previewExecutor != null && !previewExecutor.isTerminated())
					previewExecutor.shutdownNow();
				previewExecutor = Executors.newSingleThreadExecutor();
				previewExecutor.submit(() -> {
					final ExecutorService thisExecutor = previewExecutor;

					BufferedImage preview = previewCache.get(getCurrentImage().getId());
					if (preview == null) {
						try {
							preview = getCurrentImage().getThumbnail(256);
						} catch (CytomineClientException e) {
							e.printStackTrace();
							preview = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
						}
					}
					ImageIcon icon;
					if (preview == null)
						icon = ImageDetailsPanel.defaultIcon;
					else
						icon = new ImageIcon(preview);

					if (previewExecutor == thisExecutor) {
						panel.getPreviewLabel().setIcon(icon);
						panel.getPreviewLabel().invalidate();
					}

				});
				previewExecutor.shutdown();
			}

			panel.getFileNameLabel().setText(filename);
			panel.getImageIdTextArea().setText(id);
			panel.getImageDimensionTextArea().setText(dimension);
			panel.getImageMagnificationTextArea().setText(magnification);
			panel.getNumberOfAlgoAnnotationsTextArea().setText(annotationsAlgo);
			panel.getNumberOfUserAnnotationsTextArea().setText(annotationsUser);
			panel.getImageSizeTextArea().setText(size);
			panel.getImageResolutionTextArea().setText(resolution);
			panel.getImageDepthTextArea().setText(depth);
			panel.getImageCreationDateTextArea().setText(date);

		} else {
			panel.getFileNameLabel().setText("Image file name");
			panel.getImageIdTextArea().setText("0");
			panel.getImageDimensionTextArea().setText("0 x 0 \u00B5m");
			panel.getImageMagnificationTextArea().setText("0X");
			panel.getNumberOfAlgoAnnotationsTextArea().setText("0");
			panel.getNumberOfUserAnnotationsTextArea().setText("0");
			panel.getImageSizeTextArea().setText("0 x 0 px");
			panel.getImageResolutionTextArea().setText("0 \u00B5m/px");
			panel.getImageDepthTextArea().setText("0 levels");
			panel.getImageCreationDateTextArea().setText("Not available");
			panel.getPreviewLabel().setIcon(ImageDetailsPanel.defaultIcon);
		}

		setEditionHandlers();
	}

	private void setEditionHandlers() {
		setMagnificationEditionHandlers();
		setResolutionEditionHandlers();
	}

	private void setMagnificationEditionHandlers() {
		panel.getImageMagnificationTextArea().setEditable(false);
		panel.getImageMagnificationEditButton().setText("Edit");
		String magnification;
		Optional<Integer> magnificationValue = getCurrentImage().getMagnification();
		if (magnificationValue.isPresent()) {
			magnification = String.format("%dX", magnificationValue.get());
		} else {
			magnification = "Not specified";
		}
		panel.getImageMagnificationTextArea().setText(magnification);

		MouseAdapter magnificationEditListener = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				if (!event.isConsumed()) {
					if (panel.getImageMagnificationTextArea().isEditable()) {
						panel.getImageMagnificationTextArea().setEditable(false);
						panel.getImageMagnificationEditButton().setText("Edit");
						Integer newMagnification = null;
						try {
							newMagnification = Integer.parseInt(panel.getImageMagnificationTextArea().getText());
						} catch (NumberFormatException e) {
							// Nothing to change
						}
						final Integer finalMagnification = newMagnification;
						magnificationChangeListeners.forEach(l -> l.magnificationChanged(currentImage, finalMagnification));

						String magnification;
						Optional<Integer> magnificationValue = getCurrentImage().getMagnification();
						if (magnificationValue.isPresent()) {
							magnification = String.format("%dX", magnificationValue.get());
						} else {
							magnification = "Not specified";
						}
						panel.getImageMagnificationTextArea().setText(magnification);
					} else {
						panel.getImageMagnificationTextArea().setText(currentImage.getMagnification().orElse(1).toString());
						panel.getImageMagnificationEditButton().setText("Save");
						panel.getImageMagnificationTextArea().setEditable(true);
					}
				}
				event.consume();
			}
		};
		panel.getImageMagnificationEditButton().addMouseListener(magnificationEditListener);
	}

	private void setResolutionEditionHandlers() {
		panel.getImageResolutionTextArea().setEditable(false);
		panel.getImageResolutionEditButton().setText("Edit");
		String resolution;
		Optional<Double> resolutionValue = getCurrentImage().getResolution();
		if (resolutionValue.isPresent()) {
			resolution = String.format("%f \u00B5m/px", resolutionValue.get());
		} else {
			resolution = "Not specified";
		}
		panel.getImageResolutionTextArea().setText(resolution);

		MouseAdapter resolutionEditListener = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				if (panel.getImageResolutionTextArea().isEditable()) {
					panel.getImageResolutionTextArea().setEditable(false);
					panel.getImageResolutionEditButton().setText("Edit");
					Double newResolution = null;
					try {
						newResolution = Double.parseDouble(panel.getImageResolutionTextArea().getText());
					} catch (NumberFormatException e) {
						// Nothing to change
					}
					final Double finalResolution = newResolution;
					resolutionChangeListeners.forEach(l -> l.resolutionChanged(currentImage, finalResolution));

					String resolution;
					Optional<Double> resolutionValue = getCurrentImage().getResolution();
					if (resolutionValue.isPresent()) {
						resolution = String.format("%f \u00B5m/px", resolutionValue.get());
					} else {
						resolution = "Not specified";
					}
					panel.getImageResolutionTextArea().setText(resolution);
				} else {
					panel.getImageResolutionTextArea().setText(currentImage.getResolution().orElse(1d).toString());
					panel.getImageResolutionEditButton().setText("Save");
					panel.getImageResolutionTextArea().setEditable(true);
				}
			}
		};
		panel.getImageResolutionEditButton().addMouseListener(resolutionEditListener);
	}

	public void addImageMagnificationChangeListener(ImageMagnificationChangeListener listener) {
		this.magnificationChangeListeners.add(listener);
	}

	public void addImageResolutionChangeListener(ImageResolutionChangeListener listener) {
		this.resolutionChangeListeners.add(listener);
	}
}
