package org.bioimageanalysis.icy.icytomine.ui.core.explorer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.UIManager;

import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.ehcache.Cache;

import be.cytomine.client.CytomineException;

public class ImageDetailsPanel extends JPanel {
	private static final long serialVersionUID = 6055883183949045447L;
	private static final ImageIcon defaultIcon = new ImageIcon(
			ImageDetailsPanel.class.getResource("/javax/swing/plaf/basic/icons/image-delayed.png"));
	private Image currentImage;

	private JScrollPane scrollPaneDetails;
	private JPanel panelDetails;
	private JTextArea lblImageFileName;
	private JLabel lblPreview;
	private JTextArea lblIdValue;
	private JTextArea lblDimensionValue;
	private JTextArea lblMagnificationValue;
	private JTextArea lblAnnotationsAlgoValue;
	private JTextArea lblAnnotationsUserValue;
	private JTextArea lblSizeValue;
	private JTextArea lblResolutionValue;
	private JTextArea lblDepthValue;
	private JTextArea lblDateCreationValue;

	private ExecutorService previewExecutor;
	private Cache<Long, BufferedImage> previewCache;

	/**
	 * Create the panel.
	 */
	public ImageDetailsPanel() {
		setMinimumSize(new Dimension(150, 300));
		setPreferredSize(new Dimension(240, 400));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JLabel lblImageDetails = new JLabel("Image Details");
		lblImageDetails.setAlignmentY(Component.TOP_ALIGNMENT);
		lblImageDetails.setVerticalAlignment(SwingConstants.TOP);
		lblImageDetails.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblImageDetails.setHorizontalAlignment(SwingConstants.CENTER);
		lblImageDetails.setFont(new Font("Tahoma", Font.BOLD, 11));
		add(lblImageDetails);

		scrollPaneDetails = new JScrollPane();
		add(scrollPaneDetails);

		panelDetails = new JPanel();
		panelDetails.setBackground(UIManager.getColor("Panel.background"));
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 40, 0 };
		gbl_panel.rowHeights = new int[] { 28, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, 0.0 };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 };
		panelDetails.setLayout(gbl_panel);
		scrollPaneDetails.setViewportView(panelDetails);

		lblImageFileName = new JTextArea("ImageFileName...longName");
		lblImageFileName.setEditable(false);
		lblImageFileName.setOpaque(false);
		lblImageFileName.setLineWrap(true);
		lblImageFileName.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblImageFileName.setBackground(UIManager.getColor("Panel.background"));
		GridBagConstraints gbc_lblImageFileName = new GridBagConstraints();
		gbc_lblImageFileName.fill = GridBagConstraints.BOTH;
		gbc_lblImageFileName.gridwidth = 2;
		gbc_lblImageFileName.insets = new Insets(10, 10, 10, 10);
		gbc_lblImageFileName.gridx = 0;
		gbc_lblImageFileName.gridy = 0;
		panelDetails.add(lblImageFileName, gbc_lblImageFileName);

		lblPreview = new JLabel("");
		lblPreview.setDoubleBuffered(true);
		lblPreview.setHorizontalTextPosition(SwingConstants.CENTER);
		lblPreview.setMaximumSize(new Dimension(200, 200));
		lblPreview.setVerticalTextPosition(SwingConstants.TOP);
		lblPreview.setVerticalAlignment(SwingConstants.TOP);
		lblPreview.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblPreview.setHorizontalAlignment(SwingConstants.CENTER);
		lblPreview.setIcon(defaultIcon);
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.fill = GridBagConstraints.BOTH;
		gbc_label.gridwidth = 2;
		gbc_label.insets = new Insets(0, 0, 5, 0);
		gbc_label.gridx = 0;
		gbc_label.gridy = 1;
		panelDetails.add(lblPreview, gbc_label);

		JLabel lblId = new JLabel("ID");
		lblId.setFocusable(false);
		lblId.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblId.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblId = new GridBagConstraints();
		gbc_lblId.fill = GridBagConstraints.BOTH;
		gbc_lblId.insets = new Insets(0, 0, 5, 10);
		gbc_lblId.gridx = 0;
		gbc_lblId.gridy = 2;
		panelDetails.add(lblId, gbc_lblId);

		lblIdValue = new JTextArea("12345678");
		lblIdValue.setEditable(false);
		lblIdValue.setLineWrap(true);
		lblIdValue.setWrapStyleWord(true);
		lblIdValue.setOpaque(false);
		lblIdValue.setBackground(UIManager.getColor("Panel.background"));
		lblIdValue.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_lblIdValue = new GridBagConstraints();
		gbc_lblIdValue.fill = GridBagConstraints.BOTH;
		gbc_lblIdValue.insets = new Insets(0, 0, 5, 0);
		gbc_lblIdValue.gridx = 1;
		gbc_lblIdValue.gridy = 2;
		panelDetails.add(lblIdValue, gbc_lblIdValue);

		JLabel lblDimension = new JLabel("Dimension");
		lblDimension.setFocusable(false);
		lblDimension.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDimension.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblDimension = new GridBagConstraints();
		gbc_lblDimension.fill = GridBagConstraints.BOTH;
		gbc_lblDimension.insets = new Insets(0, 0, 5, 10);
		gbc_lblDimension.gridx = 0;
		gbc_lblDimension.gridy = 3;
		panelDetails.add(lblDimension, gbc_lblDimension);

		lblDimensionValue = new JTextArea("12.35 x 12.35 \u00B5m");
		lblDimensionValue.setEditable(false);
		lblDimensionValue.setLineWrap(true);
		lblDimensionValue.setWrapStyleWord(true);
		lblDimensionValue.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblDimensionValue.setBackground(UIManager.getColor("Panel.background"));
		lblDimensionValue.setOpaque(false);
		GridBagConstraints gbc_lblDimensionValue = new GridBagConstraints();
		gbc_lblDimensionValue.fill = GridBagConstraints.BOTH;
		gbc_lblDimensionValue.insets = new Insets(0, 0, 5, 0);
		gbc_lblDimensionValue.gridx = 1;
		gbc_lblDimensionValue.gridy = 3;
		panelDetails.add(lblDimensionValue, gbc_lblDimensionValue);

		JLabel lblMagnifiation = new JLabel("Magnification");
		lblMagnifiation.setFocusable(false);
		lblMagnifiation.setHorizontalAlignment(SwingConstants.RIGHT);
		lblMagnifiation.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblMagnification = new GridBagConstraints();
		gbc_lblMagnification.fill = GridBagConstraints.BOTH;
		gbc_lblMagnification.insets = new Insets(0, 0, 5, 10);
		gbc_lblMagnification.gridx = 0;
		gbc_lblMagnification.gridy = 4;
		panelDetails.add(lblMagnifiation, gbc_lblMagnification);

		lblMagnificationValue = new JTextArea("20X");
		lblMagnificationValue.setEditable(false);
		lblMagnificationValue.setLineWrap(true);
		lblMagnificationValue.setWrapStyleWord(true);
		lblMagnificationValue.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblMagnificationValue.setOpaque(false);
		GridBagConstraints gbc_lblMagnificationValue = new GridBagConstraints();
		gbc_lblMagnificationValue.fill = GridBagConstraints.BOTH;
		gbc_lblMagnificationValue.insets = new Insets(0, 0, 5, 0);
		gbc_lblMagnificationValue.gridx = 1;
		gbc_lblMagnificationValue.gridy = 4;
		panelDetails.add(lblMagnificationValue, gbc_lblMagnificationValue);

		JLabel lblAnnotationsAlgo = new JLabel("<html>Annotations<br>(Algorithms)");
		lblAnnotationsAlgo.setFocusable(false);
		lblAnnotationsAlgo.setHorizontalAlignment(SwingConstants.RIGHT);
		lblAnnotationsAlgo.setMaximumSize(new Dimension(80, 22));
		lblAnnotationsAlgo.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblUsers = new GridBagConstraints();
		gbc_lblUsers.anchor = GridBagConstraints.EAST;
		gbc_lblUsers.fill = GridBagConstraints.VERTICAL;
		gbc_lblUsers.insets = new Insets(0, 0, 5, 5);
		gbc_lblUsers.gridx = 0;
		gbc_lblUsers.gridy = 5;
		panelDetails.add(lblAnnotationsAlgo, gbc_lblUsers);

		lblAnnotationsAlgoValue = new JTextArea("14");
		lblAnnotationsAlgoValue.setEditable(false);
		lblAnnotationsAlgoValue.setLineWrap(true);
		lblAnnotationsAlgoValue.setWrapStyleWord(true);
		lblAnnotationsAlgoValue.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblAnnotationsAlgoValue.setOpaque(false);
		GridBagConstraints gbc_label_1 = new GridBagConstraints();
		gbc_label_1.fill = GridBagConstraints.BOTH;
		gbc_label_1.insets = new Insets(0, 0, 5, 0);
		gbc_label_1.gridx = 1;
		gbc_label_1.gridy = 5;
		panelDetails.add(lblAnnotationsAlgoValue, gbc_label_1);

		JLabel txtrAnnotationsUser = new JLabel("<html>Annotations<br>(User)");
		txtrAnnotationsUser.setFocusable(false);
		txtrAnnotationsUser.setHorizontalAlignment(SwingConstants.RIGHT);
		txtrAnnotationsUser.setMaximumSize(new Dimension(80, 22));
		txtrAnnotationsUser.setFont(new Font("Tahoma", Font.BOLD, 12));
		txtrAnnotationsUser.setBackground(UIManager.getColor("Panel.background"));
		GridBagConstraints gbc_txtrAnnotationsUser = new GridBagConstraints();
		gbc_txtrAnnotationsUser.insets = new Insets(0, 0, 5, 5);
		gbc_txtrAnnotationsUser.anchor = GridBagConstraints.EAST;
		gbc_txtrAnnotationsUser.fill = GridBagConstraints.VERTICAL;
		gbc_txtrAnnotationsUser.gridx = 0;
		gbc_txtrAnnotationsUser.gridy = 6;
		panelDetails.add(txtrAnnotationsUser, gbc_txtrAnnotationsUser);

		lblAnnotationsUserValue = new JTextArea("14");
		lblAnnotationsUserValue.setEditable(false);
		lblAnnotationsUserValue.setLineWrap(true);
		lblAnnotationsUserValue.setWrapStyleWord(true);
		lblAnnotationsUserValue.setOpaque(false);
		lblAnnotationsUserValue.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_lblAnnotationsUserValue = new GridBagConstraints();
		gbc_lblAnnotationsUserValue.insets = new Insets(0, 0, 5, 0);
		gbc_lblAnnotationsUserValue.fill = GridBagConstraints.BOTH;
		gbc_lblAnnotationsUserValue.gridx = 1;
		gbc_lblAnnotationsUserValue.gridy = 6;
		panelDetails.add(lblAnnotationsUserValue, gbc_lblAnnotationsUserValue);

		JLabel lblSize = new JLabel("Size");
		lblSize.setFocusable(false);
		lblSize.setHorizontalAlignment(SwingConstants.RIGHT);
		lblSize.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblSize = new GridBagConstraints();
		gbc_lblSize.fill = GridBagConstraints.VERTICAL;
		gbc_lblSize.anchor = GridBagConstraints.EAST;
		gbc_lblSize.insets = new Insets(0, 0, 5, 10);
		gbc_lblSize.gridx = 0;
		gbc_lblSize.gridy = 7;
		panelDetails.add(lblSize, gbc_lblSize);

		lblSizeValue = new JTextArea("20000 x 20000 px");
		lblSizeValue.setEditable(false);
		lblSizeValue.setLineWrap(true);
		lblSizeValue.setWrapStyleWord(true);
		lblSizeValue.setOpaque(false);
		lblSizeValue.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_lblSizeValue = new GridBagConstraints();
		gbc_lblSizeValue.fill = GridBagConstraints.VERTICAL;
		gbc_lblSizeValue.anchor = GridBagConstraints.WEST;
		gbc_lblSizeValue.insets = new Insets(0, 0, 5, 0);
		gbc_lblSizeValue.gridx = 1;
		gbc_lblSizeValue.gridy = 7;
		panelDetails.add(lblSizeValue, gbc_lblSizeValue);

		JLabel lblPixelResotuion = new JLabel("Pixel Resolution");
		lblPixelResotuion.setFocusable(false);
		lblPixelResotuion.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPixelResotuion.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblPixelResotuion = new GridBagConstraints();
		gbc_lblPixelResotuion.fill = GridBagConstraints.VERTICAL;
		gbc_lblPixelResotuion.anchor = GridBagConstraints.EAST;
		gbc_lblPixelResotuion.insets = new Insets(0, 0, 5, 10);
		gbc_lblPixelResotuion.gridx = 0;
		gbc_lblPixelResotuion.gridy = 8;
		panelDetails.add(lblPixelResotuion, gbc_lblPixelResotuion);

		lblResolutionValue = new JTextArea("2.8 \u00B5m/px");
		lblResolutionValue.setEditable(false);
		lblResolutionValue.setLineWrap(true);
		lblResolutionValue.setWrapStyleWord(true);
		lblResolutionValue.setOpaque(false);
		lblResolutionValue.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_lblResolutionValue = new GridBagConstraints();
		gbc_lblResolutionValue.fill = GridBagConstraints.VERTICAL;
		gbc_lblResolutionValue.insets = new Insets(0, 0, 5, 0);
		gbc_lblResolutionValue.anchor = GridBagConstraints.WEST;
		gbc_lblResolutionValue.gridx = 1;
		gbc_lblResolutionValue.gridy = 8;
		panelDetails.add(lblResolutionValue, gbc_lblResolutionValue);

		JLabel lblDepth = new JLabel("Depth");
		lblDepth.setFocusable(false);
		lblDepth.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDepth.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblDepth = new GridBagConstraints();
		gbc_lblDepth.fill = GridBagConstraints.BOTH;
		gbc_lblDepth.insets = new Insets(0, 0, 5, 10);
		gbc_lblDepth.gridx = 0;
		gbc_lblDepth.gridy = 9;
		panelDetails.add(lblDepth, gbc_lblDepth);

		lblDepthValue = new JTextArea("8 levels");
		lblDepthValue.setEditable(false);
		lblDepthValue.setLineWrap(true);
		lblDepthValue.setWrapStyleWord(true);
		lblDepthValue.setOpaque(false);
		lblDepthValue.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_lblDepthValue = new GridBagConstraints();
		gbc_lblDepthValue.fill = GridBagConstraints.BOTH;
		gbc_lblDepthValue.insets = new Insets(0, 0, 5, 0);
		gbc_lblDepthValue.gridx = 1;
		gbc_lblDepthValue.gridy = 9;
		panelDetails.add(lblDepthValue, gbc_lblDepthValue);

		JLabel lblDateCreation = new JLabel("Created on");
		lblDateCreation.setVerticalTextPosition(SwingConstants.TOP);
		lblDateCreation.setVerticalAlignment(SwingConstants.TOP);
		lblDateCreation.setFocusable(false);
		lblDateCreation.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDateCreation.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblDateCreation = new GridBagConstraints();
		gbc_lblDateCreation.weighty = 1.0;
		gbc_lblDateCreation.fill = GridBagConstraints.VERTICAL;
		gbc_lblDateCreation.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblDateCreation.insets = new Insets(0, 0, 0, 5);
		gbc_lblDateCreation.gridx = 0;
		gbc_lblDateCreation.gridy = 10;
		panelDetails.add(lblDateCreation, gbc_lblDateCreation);

		lblDateCreationValue = new JTextArea("June 8, 2017");
		lblDateCreationValue.setEditable(false);
		lblDateCreationValue.setLineWrap(true);
		lblDateCreationValue.setWrapStyleWord(true);
		lblDateCreationValue.setOpaque(false);
		lblDateCreationValue.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_lblDateCreationValue = new GridBagConstraints();
		gbc_lblDateCreationValue.weighty = 1.0;
		gbc_lblDateCreationValue.fill = GridBagConstraints.VERTICAL;
		gbc_lblDateCreationValue.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblDateCreationValue.gridx = 1;
		gbc_lblDateCreationValue.gridy = 10;
		panelDetails.add(lblDateCreationValue, gbc_lblDateCreationValue);
	}

	ImageDetailsPanel(Image image) {
		this();
		setCurrentImage(image);
	}

	public void setCurrentImage(Image image) {
		this.currentImage = image;
		updateImageDetails();
	}

	public Image getCurrentImage() {
		return this.currentImage;
	}

	public void setPreviewCache(Cache<Long, BufferedImage> cache) {
		this.previewCache = cache;
	}

	public void updateImageDetails() {
		if (getCurrentImage() != null) {
			String filename = getCurrentImage().getName();
			String id = "" + getCurrentImage().getId();
			Double dimensionX = getCurrentImage().getDimensionX(), dimensionY = getCurrentImage().getDimensionY();
			String dimension = String.format("%.2f x %.2f \u00B5m", dimensionX, dimensionY);
			if (dimensionX == null || dimensionY == null)
				dimension = "Unavailable";
			String magnification = String.format("%dX", getCurrentImage().getMagnification());
			if (magnification.equals("nullX"))
				magnification = "Unavailable";
			String annotationsAlgo = "" + getCurrentImage().getAnnotationsAlgo();
			if (annotationsAlgo.equals("null"))
				annotationsAlgo = "Unavailable";
			String annotationsUser = "" + getCurrentImage().getAnnotationsUser();
			if (annotationsUser.equals("null"))
				annotationsUser = "Unavailable";
			Integer sizeX = getCurrentImage().getSizeX(), sizeY = getCurrentImage().getSizeY();
			String size = String.format("%d x %d px", sizeX, sizeY);
			if (sizeX == null || sizeY == null)
				size = "Unavailable";
			Double res = getCurrentImage().getResolution();
			String resolution = String.format("%f \u00B5m/px", res);
			if (res == null)
				resolution = "Unavailable";
			Long dep = getCurrentImage().getDepth();
			String depth = String.format("%d levels", dep);
			if (dep == null)
				depth = "Unavailable";
			Date dt = getCurrentImage().getCreationDate().getTime();
			DateFormat formatter = new SimpleDateFormat("d MMM yyyy HH:mm");
			String date = dt == null ? "Unavailable" : formatter.format(dt);

			synchronized (this.lblPreview) {
				this.lblPreview.setIcon(null);
				if (previewExecutor != null && !previewExecutor.isTerminated())
					previewExecutor.shutdownNow();
				previewExecutor = Executors.newSingleThreadExecutor();
				previewExecutor.submit(() -> {
					final ExecutorService thisExecutor = previewExecutor;

					BufferedImage preview = previewCache.get(getCurrentImage().getId());
					if (preview == null) {
						try {
							preview = getCurrentImage().getThumbnail(256);
						} catch (CytomineException e) {
							e.printStackTrace();
							preview = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
						}
					}
					ImageIcon icon;
					if (preview == null)
						icon = defaultIcon;
					else
						icon = new ImageIcon(preview);

					if (previewExecutor == thisExecutor) {
						this.lblPreview.setIcon(icon);
						this.lblPreview.invalidate();
					}

				});
				previewExecutor.shutdown();
				Timer timer = new Timer(200, ev -> {
					lblPreview.revalidate();
					scrollPaneDetails.getVerticalScrollBar().setValue(0);
				});
				timer.setRepeats(false);
				timer.start();
			}

			this.lblImageFileName.setText(filename);
			this.lblIdValue.setText(id);
			this.lblDimensionValue.setText(dimension);
			this.lblMagnificationValue.setText(magnification);
			this.lblAnnotationsAlgoValue.setText(annotationsAlgo);
			this.lblAnnotationsUserValue.setText(annotationsUser);
			this.lblSizeValue.setText(size);
			this.lblResolutionValue.setText(resolution);
			this.lblDepthValue.setText(depth);
			this.lblDateCreationValue.setText(date);

		} else {
			this.lblImageFileName.setText("Image file name");
			this.lblIdValue.setText("0");
			this.lblDimensionValue.setText("0 x 0 \u00B5m");
			this.lblMagnificationValue.setText("0X");
			this.lblAnnotationsAlgoValue.setText("0");
			this.lblAnnotationsUserValue.setText("0");
			this.lblSizeValue.setText("0 x 0 px");
			this.lblResolutionValue.setText("0 \u00B5m/px");
			this.lblDepthValue.setText("0 levels");
			this.lblDateCreationValue.setText("Not available");
			this.lblPreview.setIcon(defaultIcon);
		}
	}
}
