package org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.viewProvider;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.view.CachedView;
import org.bioimageanalysis.icy.icytomine.core.view.CachedView.ViewListener;

public class CachedViewProvider extends ViewProvider {

	private CachedView cachedView;

	public CachedViewProvider(CachedView cachedView) {
		super();
		this.cachedView = cachedView;
	}

	@Override
	public void addViewListener(ViewListener listener) {
		this.cachedView.addViewListener(listener);
	}

	@Override
	public void addViewProcessListener(ViewProcessListener listener) {
		this.cachedView.addViewProcessListener(listener);
	}

	@Override
	public synchronized BufferedImage getView(Dimension canvasSize) {
		if (canvasSize.width == 0 || canvasSize.height == 0) {
			return new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
		}
		return cachedView.getView(getPosition(), canvasSize, getResolutionLevel());
	}

	@Override
	public Image getImageInformation() {
		return cachedView.getImageInformation();
	}

	@Override
	public void stop() {
		this.cachedView.stop();
	}

}
