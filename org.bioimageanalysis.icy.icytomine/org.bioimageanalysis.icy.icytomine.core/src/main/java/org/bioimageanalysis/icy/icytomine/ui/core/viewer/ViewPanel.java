package org.bioimageanalysis.icy.icytomine.ui.core.viewer;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.JPanel;

import org.bioimageanalysis.icy.icytomine.core.cache.CachedView;
import org.bioimageanalysis.icy.icytomine.core.model.Image;

import be.cytomine.client.CytomineException;
import icy.canvas.IcyCanvas2D;
import icy.canvas.IcyCanvasEvent;
import icy.canvas.IcyCanvasListener;
import icy.gui.viewer.Viewer;
import icy.main.Icy;
import icy.sequence.Sequence;

/**
 * This panel contains the components present on the image viewer. This
 * components are:
 * - The displayed view: A view port of the image stored in the server
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class ViewPanel extends JPanel {
	private static final long serialVersionUID = 8794683795558989863L;

	/**
	 * Where the image is actually displayed
	 */
	private ViewPort displayedView;

	/**
	 * Information of the image to be shown.
	 */
	private Image				imageInformation;
	/**
	 * The image provider for the displayed view.
	 */
	private CachedView	viewProvider;
	/**
	 * Resolution currently displayed for this viewer
	 */
	private double			displayedResolution;
	/**
	 * Position in the base-resolution image used to display this image.
	 */
	private Point				displayedPosition;

	/**
	 * Last position of the mouse when dragging in the viewer. Used to compute the
	 * image translation.
	 */
	private Point dragPosition;

	private IcyCanvas2D canvas;

	/**
	 * Constructs a viewer panel containing a {@link ViewPort} that presents the
	 * image using the image information.
	 */
	protected ViewPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0 };
		gridBagLayout.rowHeights = new int[] { 0 };
		gridBagLayout.columnWeights = new double[] { 1.0 };
		gridBagLayout.rowWeights = new double[] { 1.0 };
		setLayout(gridBagLayout);
		setPreferredSize(new Dimension(512, 512));

		displayedView = new ViewPort();
		// Mouse button pressed event
		displayedView.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				// System.out.println(" StartDrag...");
				dragPosition = e.getPoint();
			}
		});

		// Mouse dragged event
		displayedView.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				dragImage(e.getPoint());
			}
		});

		// Mouse wheel event
		displayedView.addMouseWheelListener((MouseWheelEvent e) -> {
			Point mousePositionInScreen = e.getPoint();
			Point mousePositionInScreenAtRes0 = scalePositionToBaseResolution(mousePositionInScreen);
			Point mousePositionInImage = new Point(displayedPosition.x + mousePositionInScreenAtRes0.x,
					displayedPosition.y + mousePositionInScreenAtRes0.y);

			double zoom = e.getPreciseWheelRotation() * .25;
			displayedResolution += zoom;
			displayedResolution = Math.max(0, Math.min(getImageInformation().getDepth(), displayedResolution));

			Point newMousePositionInScreenAtRes0 = scalePositionToBaseResolution(mousePositionInScreen);
			displayedPosition.x = mousePositionInImage.x - newMousePositionInScreenAtRes0.x;
			displayedPosition.y = mousePositionInImage.y - newMousePositionInScreenAtRes0.y;

			showImage(displayedPosition, displayedResolution);
		});

		// Component size events
		displayedView.addComponentListener(new ComponentListener() {
			@Override
			public void componentShown(ComponentEvent e) {
				displayedPosition = new Point(0, 0);
				displayedResolution = getViewAdjustedResolution(displayedView.getSize());
				showImage(displayedPosition, displayedResolution);
			}

			@Override
			public void componentResized(ComponentEvent e) {
				if (displayedPosition == null) {
					displayedPosition = new Point(0, 0);
					displayedResolution = getViewAdjustedResolution(displayedView.getSize());
				}
				showImage(displayedPosition, displayedResolution);
			}

			@Override
			public void componentMoved(ComponentEvent e) {}

			@Override
			public void componentHidden(ComponentEvent e) {}
		});
		GridBagConstraints gbc_viewPort = new GridBagConstraints();
		gbc_viewPort.weighty = 1.0;
		gbc_viewPort.weightx = 1.0;
		gbc_viewPort.fill = GridBagConstraints.BOTH;
		gbc_viewPort.gridx = 0;
		gbc_viewPort.gridy = 0;
		add(displayedView, gbc_viewPort);

	}

	private Point scalePositionToBaseResolution(Point mousePosition) {
		double denom = Math.pow(2, displayedResolution);
		Point scaledPosition = new Point((int) (mousePosition.x * denom), (int) (mousePosition.y * denom));
		return scaledPosition;
	}

	/**
	 * Constructs a viewer panel containing a {@link ViewPort} that presents the
	 * image using the image information. The view port notifies events occurring
	 * on the image such as mouse events on the view port area, key events, etc.
	 * 
	 * @param imageInformation
	 *          information of the image to show on the view port.
	 */
	public ViewPanel(Image imageInformation) {
		this();
		setImageInformation(imageInformation);
		setCanvas();
	}

	public Image getImageInformation() {
		return imageInformation;
	}

	private void setImageInformation(Image imageInformation) {
		this.imageInformation = imageInformation;
		this.viewProvider = new CachedView(imageInformation);
		this.viewProvider.addViewListener(() -> {
			displayedView.setChangingViewPort(false);
			displayedView.repaint();
		});
		this.displayedView.setViewProvider(viewProvider);
	}

	private double getViewAdjustedResolution(Dimension viewSize) {
		Dimension imageSize = this.getImageInformation().getSize();
		double w = imageSize.width / viewSize.width;
		double h = imageSize.height / viewSize.height;

		double rw = Math.log(w) / Math.log(2);
		double rh = Math.log(h) / Math.log(2);

		double r = Math.min(rw, rh);

		return Math.max(0, Math.min(getImageInformation().getDepth(), r));
	}

	private void showImage(Point displayedPosition, double resolutionLevel) {
		Dimension viewSize = this.displayedView.getSize();
		if (viewSize.width == 0 || viewSize.height == 0) {
			viewSize = getPreferredSize();
		}

		this.viewProvider.requestView(displayedPosition, resolutionLevel, viewSize);
	}

	private synchronized void dragImage(Point point) {
		double movementMultiplier = Math.pow(2, displayedResolution);
		Point movement = new Point((int) ((dragPosition.x - point.x) * movementMultiplier),
				(int) ((dragPosition.y - point.y) * movementMultiplier));
		dragPosition = point;
		displayedPosition = new Point(displayedPosition);
		displayedPosition.translate(movement.x, movement.y);
		// System.out.println("move to " + displayedPosition);
		showImage(displayedPosition, displayedResolution);
	}

	private void setCanvas() {
		Sequence canvasImage;
		try {
			canvasImage = new Sequence("Cytomine Image " + getImageInformation().getName(),
					getImageInformation().getThumbnail(512));
		} catch (CytomineException e) {
			System.err.println("Could not retrieve canvas image");
			canvasImage = new Sequence("Cytomine Image " + getImageInformation().getName());
		}
		
		this.canvas = (IcyCanvas2D) (new Viewer(canvasImage, false).getCanvas());
		
	}

	public void close() {
		viewProvider.close();
		this.canvas.getViewer().close();
	}

	public void gainFocus() {
		Icy.getMainInterface().setActiveViewer(this.canvas.getViewer());
	}

}
