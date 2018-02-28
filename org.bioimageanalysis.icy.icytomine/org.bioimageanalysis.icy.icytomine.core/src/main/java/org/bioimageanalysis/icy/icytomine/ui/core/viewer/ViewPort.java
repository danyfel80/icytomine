package org.bioimageanalysis.icy.icytomine.ui.core.viewer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.bioimageanalysis.icy.icytomine.core.cache.CachedView;

import icy.util.GraphicsUtil;

public class ViewPort extends JPanel {
	private static final long serialVersionUID = 8675464751926457676L;

	private boolean			changingViewPort;
	private CachedView	viewProvider;

	public boolean isChangingViewPort() {
		return changingViewPort;
	}

	public void setChangingViewPort(boolean changingViewPort) {
		this.changingViewPort = changingViewPort;
	}

	private CachedView getViewProvider() {
		return viewProvider;
	}

	public void setViewProvider(CachedView viewProvider) {
		this.viewProvider = viewProvider;
	}

	/**
	 * Create the view port panel which will be the one showing a portion of the
	 * whole image.
	 */
	protected ViewPort() {
		setChangingViewPort(true);
	}

	public void drawTextCenter(Graphics2D g, String text, float alpha) {
		final Rectangle2D rect = GraphicsUtil.getStringBounds(g, text);
		final int w = (int) rect.getWidth();
		final int h = (int) rect.getHeight();
		final int x = (getWidth() - (w + 8 + 2)) / 2;
		final int y = (getHeight() - (h + 8 + 2)) / 2;

		g.setColor(Color.gray);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		g.fillRoundRect(x, y, w + 8, h + 8, 8, 8);

		g.setColor(Color.white);
		g.drawString(text, x + 4, y + 2 + h);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		CachedView viewProvider = getViewProvider();
		if (viewProvider != null) {
			BufferedImage displayedImage = viewProvider.getImageView();
			if (displayedImage != null) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.drawImage(displayedImage, 0, 0, this);
				g2.dispose();
			}
		}

		// display a message
		if (isChangingViewPort()) {
			Graphics2D g2 = (Graphics2D) g.create();

			g2.setFont(new Font("Arial", Font.BOLD, 16));
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			drawTextCenter(g2, "Loading canvas...", 0.8f);
			g2.dispose();
		}
	}

}
