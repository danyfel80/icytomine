package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.view;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.ViewController;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.ViewControllerFactory;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.provider.ViewProvider;

@SuppressWarnings("serial")
public class ViewCanvasPanel extends JPanel {
	static final AlphaComposite comp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);

	BufferedImage[] layers;
	private ViewProvider viewProvider;
	private ViewController viewController;
	private Rectangle selectionBox;

	public ViewCanvasPanel(ViewProvider viewProvider) {
		setBackground(Color.GRAY);
		setLayout(new BorderLayout(0, 0));
		this.layers = new BufferedImage[] {new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB)};
		this.viewProvider = viewProvider;
		this.viewProvider.addViewListener(newViews -> refreshCanvas(newViews));
		this.viewController = ViewControllerFactory.create(this.viewProvider, this);

	}

	private void refreshCanvas(BufferedImage... newViews) {
		layers = newViews;
		repaint();
	}

	public ViewProvider getViewProvider() {
		return viewProvider;
	}

	public ViewController getViewController() {
		return viewController;
	}

	public void updateCanvas() {
		refreshCanvas(viewProvider.getView(getSize()));
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawLayers(g);
	}

	private void drawLayers(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.drawImage(layers[0], 0, 0, this);
		g2.setComposite(comp);
		for (int i = 1; i < layers.length; i++) {
			g2.drawImage(layers[1], 0, 0, null);
		}
		if (selectionBox != null) {
			drawSelectionBox(g2);
		}
		g2.dispose();
	}

	private void drawSelectionBox(Graphics2D g2) {
		synchronized (selectionBox) {
			g2.setColor(new Color(255, 255, 255, 128));
			g2.fillRect(selectionBox.x, selectionBox.y, selectionBox.width, selectionBox.height);
			g2.setColor(Color.WHITE);
			g2.setStroke(new BasicStroke(3));
			g2.drawRect(selectionBox.x, selectionBox.y, selectionBox.width, selectionBox.height);
			g2.setColor(Color.BLACK);
			g2.setStroke(new BasicStroke(1));
			g2.drawRect(selectionBox.x, selectionBox.y, selectionBox.width, selectionBox.height);
		}
	}

	public void setSelectionBox(Point2D start, Point2D end) {
		int x = (int) Math.min(start.getX(), end.getX());
		int y = (int) Math.min(start.getY(), end.getY());
		int width = (int) Math.max(start.getX(), end.getX()) - x;
		int height = (int) Math.max(start.getY(), end.getY()) - y;
		selectionBox = new Rectangle(x, y, width, height);
	}

	public void unsetSelectionBox() {
		selectionBox = null;
	}

}
