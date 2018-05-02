package org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.viewProvider;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.bioimageanalysis.icy.icytomine.core.view.CachedView.ViewListener;

public class NullViewProvider extends ViewProvider {

	Dimension viewSize;
	BufferedImage view;

	public NullViewProvider() {
		setDefaultViewSize();
	}

	private void setDefaultViewSize() {
		viewSize = new Dimension(800, 600);
	}

	@Override
	public BufferedImage getView(Dimension viewSize) {
		if (!viewSize.equals(this.viewSize)) {
			setViewSize(viewSize);
			createView();
		}
		return view;
	}

	private void setViewSize(Dimension newSize) {
		this.viewSize = newSize;
	}

	@Override
	public void addViewListener(ViewListener listener) {
		// nothing happens for null viewer
	}

	private void createView() {
		view = new BufferedImage(viewSize.width, viewSize.height, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g2 = view.createGraphics();
		g2.setColor(Color.WHITE);
		g2.drawLine(0, 0, viewSize.width, viewSize.height);
		g2.drawLine(viewSize.width, 0, 0, viewSize.height);
	}

	@Override
	public void stop() {
		// nothing to stop
	}

	@Override
	public void addViewProcessListener(ViewProcessListener listener) {
		// doing nothing
		
	}

}
