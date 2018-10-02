package org.bioimageanalysis.icy.icytomine.ui.core.explorer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.ui.core.explorer.ImageDetailsPanelController.ImageMagnificationChangeListener;
import org.bioimageanalysis.icy.icytomine.ui.core.explorer.ImageDetailsPanelController.ImageResolutionChangeListener;
import org.ehcache.Cache;

public class ImageDetailsPanel extends JPanel {
	private static final long serialVersionUID = 6055883183949045447L;
	protected static final ImageIcon defaultIcon = new ImageIcon(
			ImageDetailsPanel.class.getResource("/javax/swing/plaf/basic/icons/image-delayed.png"));

	private JScrollPane scrollPaneDetails;
	private JPanel imageDetailsPanel;

	private JTextArea fileNameLabel;
	private JLabel previewLabel;
	private JTextArea imageIdTextArea;
	private JTextArea imageDimensionTextArea;
	private JTextArea imageMagnificationTextArea;
	private JButton imageMagnificationEditButton;
	private JTextArea numberOfAlgoAnnotationsTextArea;
	private JTextArea numberOfUserAnnotationsTextArea;
	private JTextArea imageSizeTextArea;
	private JTextArea imageResolutionTextArea;
	private JButton imageResolutionEditButton;
	private JTextArea imageDepthTextArea;
	private JTextArea imageCreationDateTextArea;

	private ImageDetailsPanelController controller;

	/**
	 * Create the panel.
	 */
	public ImageDetailsPanel() {
		setView();
		setController();
	}

	private void setView() {
		setMinimumSize(new Dimension(150, 300));
		setPreferredSize(new Dimension(301, 400));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		addTitleLabel();
		addImageDetailsPanel();
	}

	private void addTitleLabel() {
		JLabel lblImageDetails = new JLabel("Image Details");
		lblImageDetails.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblImageDetails.setFont(new Font("Tahoma", Font.BOLD, 11));
		add(lblImageDetails);
	}

	private void addImageDetailsPanel() {
		scrollPaneDetails = new JScrollPane();
		add(scrollPaneDetails);

		imageDetailsPanel = new JPanel();
		imageDetailsPanel.setBackground(UIManager.getColor("Panel.background"));
		GridBagLayout imageDetailsPanelLayout = new GridBagLayout();
		imageDetailsPanelLayout.columnWidths = new int[] {40, 79, 45};
		imageDetailsPanelLayout.rowHeights = new int[] {28, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		imageDetailsPanelLayout.columnWeights = new double[] {0.0, 0.0, 0.0};
		imageDetailsPanelLayout.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
		imageDetailsPanel.setLayout(imageDetailsPanelLayout);
		scrollPaneDetails.setViewportView(imageDetailsPanel);

		addImageTitle();
		addImagePreview();
		addImageID();
		addImageDimension();
		addImageMagnification();
		addNumberOfAlgoAnnotations();
		addNumberOfUserAnnotations();
		addImageSize();
		addImageResolution();
		addImageDepth();
		addImageCreationDate();
	}

	private void addImageTitle() {
		fileNameLabel = new JTextArea("ImageFileName...longName");
		fileNameLabel.setMinimumSize(new Dimension(80, 22));
		fileNameLabel.setLineWrap(true);
		fileNameLabel.setOpaque(false);
		fileNameLabel.setFont(new Font("Tahoma", Font.BOLD, 15));

		GridBagConstraints fileNameLabelConstraints = new GridBagConstraints(0, 0, 3, 1, 0, 0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(10, 10, 10, 10), 0, 0);

		imageDetailsPanel.add(fileNameLabel, fileNameLabelConstraints);

	}

	private void addImagePreview() {
		previewLabel = new JLabel("");
		previewLabel.setDoubleBuffered(true);
		previewLabel.setMaximumSize(new Dimension(200, 200));
		previewLabel.setVerticalAlignment(SwingConstants.TOP);
		previewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		previewLabel.setIcon(defaultIcon);

		GridBagConstraints previewLabelConstraints = new GridBagConstraints(0, 1, 3, 1, 0, 0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0);

		imageDetailsPanel.add(previewLabel, previewLabelConstraints);
	}

	private void addImageID() {
		JLabel imageIdLabel = new JLabel("ID");
		imageIdLabel.setFocusable(false);
		imageIdLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		imageIdLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		GridBagConstraints gbc_lblId = new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.EAST,
				GridBagConstraints.BOTH, new Insets(0, 0, 5, 10), 0, 0);

		imageDetailsPanel.add(imageIdLabel, gbc_lblId);

		imageIdTextArea = new JTextArea("12345678");
		imageIdTextArea.setEditable(false);
		imageIdTextArea.setLineWrap(true);
		imageIdTextArea.setWrapStyleWord(true);
		imageIdTextArea.setOpaque(false);
		imageIdTextArea.setFont(new Font("Tahoma", Font.PLAIN, 11));

		GridBagConstraints gbc_lblIdValue = new GridBagConstraints(1, 2, 2, 1, 0, 0, GridBagConstraints.WEST,
				GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0);

		imageDetailsPanel.add(imageIdTextArea, gbc_lblIdValue);
	}

	private void addImageDimension() {
		JLabel dimensionLabel = new JLabel("Dimension");
		dimensionLabel.setFocusable(false);
		dimensionLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		dimensionLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		GridBagConstraints dimensionLabelConstraints = new GridBagConstraints(0, 3, 1, 1, 0, 0, GridBagConstraints.EAST,
				GridBagConstraints.EAST, new Insets(0, 0, 5, 10), 0, 0);

		imageDetailsPanel.add(dimensionLabel, dimensionLabelConstraints);

		imageDimensionTextArea = new JTextArea("12.35 x 12.35 \u00B5m");
		imageDimensionTextArea.setEditable(false);
		imageDimensionTextArea.setLineWrap(true);
		imageDimensionTextArea.setWrapStyleWord(true);
		imageDimensionTextArea.setFont(new Font("Tahoma", Font.PLAIN, 11));
		imageDimensionTextArea.setOpaque(false);

		GridBagConstraints dimensionTextAreaConstraints = new GridBagConstraints(1, 3, 2, 1, 0, 0, GridBagConstraints.WEST,
				GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0);

		imageDetailsPanel.add(imageDimensionTextArea, dimensionTextAreaConstraints);
	}

	private void addImageMagnification() {
		JLabel magnificationLabel = new JLabel("Magnification");
		magnificationLabel.setFocusable(false);
		magnificationLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		magnificationLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		GridBagConstraints magnificationLabelConstraints = new GridBagConstraints(0, 4, 1, 1, 0, 0, GridBagConstraints.EAST,
				GridBagConstraints.BOTH, new Insets(0, 0, 5, 10), 0, 0);

		imageDetailsPanel.add(magnificationLabel, magnificationLabelConstraints);

		imageMagnificationTextArea = new JTextArea("20X");
		imageMagnificationTextArea.setEditable(false);
		imageMagnificationTextArea.setLineWrap(true);
		imageMagnificationTextArea.setWrapStyleWord(true);
		imageMagnificationTextArea.setFont(new Font("Tahoma", Font.PLAIN, 11));
		imageMagnificationTextArea.setOpaque(false);

		GridBagConstraints magnificationTextAreaConstraints = new GridBagConstraints(1, 4, 1, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0);

		imageDetailsPanel.add(imageMagnificationTextArea, magnificationTextAreaConstraints);

		imageMagnificationEditButton = new JButton("Edit");

		GridBagConstraints magnificationEditButtonConstraints = new GridBagConstraints(2, 4, 1, 1, 0, 0,
				GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0);

		imageDetailsPanel.add(imageMagnificationEditButton, magnificationEditButtonConstraints);
	}

	private void addNumberOfAlgoAnnotations() {
		JLabel numberOfAlgoAnnotationsLabel = new JLabel("<html>Annotations<br>(Algorithms)");
		numberOfAlgoAnnotationsLabel.setFocusable(false);
		numberOfAlgoAnnotationsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		numberOfAlgoAnnotationsLabel.setMaximumSize(new Dimension(80, 22));
		numberOfAlgoAnnotationsLabel.setFont(new Font("Tahoma", Font.BOLD, 12));

		GridBagConstraints numberOfAlgoAnnotationLabelConstraints = new GridBagConstraints(0, 5, 1, 1, 0, 0,
				GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(0, 0, 5, 10), 0, 0);

		imageDetailsPanel.add(numberOfAlgoAnnotationsLabel, numberOfAlgoAnnotationLabelConstraints);

		numberOfAlgoAnnotationsTextArea = new JTextArea("14");
		numberOfAlgoAnnotationsTextArea.setEditable(false);
		numberOfAlgoAnnotationsTextArea.setLineWrap(true);
		numberOfAlgoAnnotationsTextArea.setWrapStyleWord(true);
		numberOfAlgoAnnotationsTextArea.setFont(new Font("Tahoma", Font.PLAIN, 11));
		numberOfAlgoAnnotationsTextArea.setOpaque(false);

		GridBagConstraints numberOfAlgoAnnotationsTextAreaConstraints = new GridBagConstraints(1, 5, 2, 1, 0, 0,
				GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0);

		imageDetailsPanel.add(numberOfAlgoAnnotationsTextArea, numberOfAlgoAnnotationsTextAreaConstraints);
	}

	private void addNumberOfUserAnnotations() {
		JLabel numberOfUserAnnotationsLabel = new JLabel("<html>Annotations<br>(User)");
		numberOfUserAnnotationsLabel.setFocusable(false);
		numberOfUserAnnotationsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		numberOfUserAnnotationsLabel.setMaximumSize(new Dimension(80, 22));
		numberOfUserAnnotationsLabel.setFont(new Font("Tahoma", Font.BOLD, 12));

		GridBagConstraints numberOfUserAnnotationLabelConstraints = new GridBagConstraints(0, 6, 1, 1, 0, 0,
				GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(0, 0, 5, 10), 0, 0);

		imageDetailsPanel.add(numberOfUserAnnotationsLabel, numberOfUserAnnotationLabelConstraints);

		numberOfUserAnnotationsTextArea = new JTextArea("14");
		numberOfUserAnnotationsTextArea.setEditable(false);
		numberOfUserAnnotationsTextArea.setLineWrap(true);
		numberOfUserAnnotationsTextArea.setWrapStyleWord(true);
		numberOfUserAnnotationsTextArea.setOpaque(false);
		numberOfUserAnnotationsTextArea.setFont(new Font("Tahoma", Font.PLAIN, 11));

		GridBagConstraints numberOfUserAnnotationsTextAreaConstraints = new GridBagConstraints(1, 6, 2, 1, 0, 0,
				GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0);

		imageDetailsPanel.add(numberOfUserAnnotationsTextArea, numberOfUserAnnotationsTextAreaConstraints);
	}

	private void addImageSize() {
		JLabel imageSizeLabel = new JLabel("Size");
		imageSizeLabel.setFocusable(false);
		imageSizeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		imageSizeLabel.setFont(new Font("Tahoma", Font.BOLD, 11));

		GridBagConstraints imageSizeLabelConstraints = new GridBagConstraints(0, 7, 1, 1, 0, 0, GridBagConstraints.EAST,
				GridBagConstraints.BOTH, new Insets(0, 0, 5, 10), 0, 0);

		imageDetailsPanel.add(imageSizeLabel, imageSizeLabelConstraints);

		imageSizeTextArea = new JTextArea("20000 x 20000 px");
		imageSizeTextArea.setEditable(false);
		imageSizeTextArea.setLineWrap(true);
		imageSizeTextArea.setWrapStyleWord(true);
		imageSizeTextArea.setOpaque(false);
		imageSizeTextArea.setFont(new Font("Tahoma", Font.PLAIN, 11));

		GridBagConstraints imageSizeTextAreaConstraints = new GridBagConstraints(1, 7, 2, 1, 0, 0, GridBagConstraints.WEST,
				GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0);

		imageDetailsPanel.add(imageSizeTextArea, imageSizeTextAreaConstraints);
	}

	private void addImageResolution() {
		JLabel imageResolutionLabel = new JLabel("Pixel Resolution");
		imageResolutionLabel.setFocusable(false);
		imageResolutionLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		imageResolutionLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		GridBagConstraints imageResolutionLabelConstraints = new GridBagConstraints(0, 8, 1, 1, 0, 0,
				GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(0, 0, 5, 10), 0, 0);

		imageDetailsPanel.add(imageResolutionLabel, imageResolutionLabelConstraints);

		imageResolutionTextArea = new JTextArea("2.8 \u00B5m/px");
		imageResolutionTextArea.setPreferredSize(new Dimension(60, 22));
		imageResolutionTextArea.setMinimumSize(new Dimension(60, 22));
		imageResolutionTextArea.setEditable(false);
		imageResolutionTextArea.setLineWrap(true);
		imageResolutionTextArea.setWrapStyleWord(true);
		imageResolutionTextArea.setOpaque(false);
		imageResolutionTextArea.setFont(new Font("Tahoma", Font.PLAIN, 11));

		GridBagConstraints imageResolutionTextAreaConstraints = new GridBagConstraints(1, 8, 1, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0);

		imageDetailsPanel.add(imageResolutionTextArea, imageResolutionTextAreaConstraints);

		imageResolutionEditButton = new JButton("Edit");
		GridBagConstraints imageResolutionEditButtonConstraints = new GridBagConstraints(2, 8, 1, 1, 0, 0,
				GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0);

		imageDetailsPanel.add(imageResolutionEditButton, imageResolutionEditButtonConstraints);
	}

	private void addImageDepth() {
		JLabel imageDepthLabel = new JLabel("Depth");
		imageDepthLabel.setFocusable(false);
		imageDepthLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		imageDepthLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		GridBagConstraints imageDepthLabelConstraints = new GridBagConstraints(0, 9, 1, 1, 0, 0, GridBagConstraints.EAST,
				GridBagConstraints.BOTH, new Insets(0, 0, 5, 10), 0, 0);

		imageDetailsPanel.add(imageDepthLabel, imageDepthLabelConstraints);

		imageDepthTextArea = new JTextArea("8 levels");
		imageDepthTextArea.setEditable(false);
		imageDepthTextArea.setLineWrap(true);
		imageDepthTextArea.setWrapStyleWord(true);
		imageDepthTextArea.setOpaque(false);
		imageDepthTextArea.setFont(new Font("Tahoma", Font.PLAIN, 11));

		GridBagConstraints imageDepthTextAreaConstraints = new GridBagConstraints(1, 9, 2, 1, 0, 0, GridBagConstraints.WEST,
				GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0);

		imageDetailsPanel.add(imageDepthTextArea, imageDepthTextAreaConstraints);
	}

	private void addImageCreationDate() {
		JLabel imageCreationDateLabel = new JLabel("Created on");
		imageCreationDateLabel.setFocusable(false);
		imageCreationDateLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		imageCreationDateLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		GridBagConstraints gbc_lblDateCreation = new GridBagConstraints(0, 10, 1, 1, 0, 1.0, GridBagConstraints.NORTHEAST,
				GridBagConstraints.BOTH, new Insets(0, 0, 5, 10), 0, 0);

		imageDetailsPanel.add(imageCreationDateLabel, gbc_lblDateCreation);

		imageCreationDateTextArea = new JTextArea("June 8, 2017");
		imageCreationDateTextArea.setEditable(false);
		imageCreationDateTextArea.setLineWrap(true);
		imageCreationDateTextArea.setWrapStyleWord(true);
		imageCreationDateTextArea.setOpaque(false);
		imageCreationDateTextArea.setFont(new Font("Tahoma", Font.PLAIN, 11));

		GridBagConstraints gbc_lblDateCreationValue = new GridBagConstraints(1, 10, 2, 1, 0, 1.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);

		imageDetailsPanel.add(imageCreationDateTextArea, gbc_lblDateCreationValue);
	}

	private void setController() {
		this.controller = new ImageDetailsPanelController(this);
	}

	public void setCurrentImage(Image image) {
		controller.setCurrentImage(image);
	}

	public Image getCurrentImage() {
		return controller.getCurrentImage();
	}

	public void setPreviewCache(Cache<Long, BufferedImage> cache) {
		controller.setPreviewCache(cache);
	}

	public void addImageMagnificationChangeListener(ImageMagnificationChangeListener listener) {
		controller.addImageMagnificationChangeListener(listener);
	}

	public void addImageResolutionChangeListener(ImageResolutionChangeListener listener) {
		controller.addImageResolutionChangeListener(listener);
	}

	protected static ImageIcon getDefaulticon() {
		return defaultIcon;
	}

	protected JTextArea getFileNameLabel() {
		return fileNameLabel;
	}

	protected JLabel getPreviewLabel() {
		return previewLabel;
	}

	protected JTextArea getImageIdTextArea() {
		return imageIdTextArea;
	}

	protected JTextArea getImageDimensionTextArea() {
		return imageDimensionTextArea;
	}

	protected JTextArea getImageMagnificationTextArea() {
		return imageMagnificationTextArea;
	}

	protected JButton getImageMagnificationEditButton() {
		return imageMagnificationEditButton;
	}

	protected JTextArea getNumberOfAlgoAnnotationsTextArea() {
		return numberOfAlgoAnnotationsTextArea;
	}

	protected JTextArea getNumberOfUserAnnotationsTextArea() {
		return numberOfUserAnnotationsTextArea;
	}

	protected JTextArea getImageSizeTextArea() {
		return imageSizeTextArea;
	}

	protected JTextArea getImageResolutionTextArea() {
		return imageResolutionTextArea;
	}

	protected JButton getImageResolutionEditButton() {
		return imageResolutionEditButton;
	}

	protected JTextArea getImageDepthTextArea() {
		return imageDepthTextArea;
	}

	protected JTextArea getImageCreationDateTextArea() {
		return imageCreationDateTextArea;
	}

}
