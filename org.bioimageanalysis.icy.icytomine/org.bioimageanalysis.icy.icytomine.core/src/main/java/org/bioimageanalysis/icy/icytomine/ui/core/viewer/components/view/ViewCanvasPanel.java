package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.viewProvider.ViewProvider;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.view.ViewController;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.view.ViewControllerFactory;
import java.awt.BorderLayout;

@SuppressWarnings("serial")
public class ViewCanvasPanel extends JPanel {

	BufferedImage canvas;
	private ViewProvider viewProvider;
	private ViewController viewController;

	public ViewCanvasPanel(ViewProvider viewProvider) {
		setBackground(Color.GRAY);
		setLayout(new BorderLayout(0, 0));
		this.canvas = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
		this.viewProvider = viewProvider;
		this.viewProvider.addViewListener(newView -> updateCanvas(newView));
		this.viewController = ViewControllerFactory.create(this.viewProvider, this);
		
	}

	private void updateCanvas(BufferedImage viewImage) {
		canvas = viewImage;
		repaint();
	}

	public ViewProvider getViewProvider() {
		return viewProvider;
	}

	public ViewController getViewController() {
		return viewController;
	}

	public void refreshCanvas() {
		viewProvider.getView(getSize());
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawCanvas(g);
	}

	private void drawCanvas(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.drawImage(canvas, 0, 0, this);
		g2.dispose();
	}
}
