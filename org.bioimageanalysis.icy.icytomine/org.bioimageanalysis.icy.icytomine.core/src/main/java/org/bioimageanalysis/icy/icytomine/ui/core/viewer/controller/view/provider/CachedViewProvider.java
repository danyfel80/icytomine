package org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.provider;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.model.Term;
import org.bioimageanalysis.icy.icytomine.core.model.User;
import org.bioimageanalysis.icy.icytomine.core.view.AnnotationView;
import org.bioimageanalysis.icy.icytomine.core.view.CachedView;
import org.bioimageanalysis.icy.icytomine.core.view.ViewListener;

public class CachedViewProvider extends ViewProvider {
	private CachedView cachedView;
	private AnnotationView annotationView;
	private Set<ViewListener> viewListeners;

	private BufferedImage currentImageView;
	private BufferedImage currentAnnotationView;

	public CachedViewProvider(CachedView cachedView, AnnotationView annotationView) {
		super();
		this.cachedView = cachedView;
		this.annotationView = annotationView;
		this.viewListeners = new HashSet<>();
		this.cachedView.addViewListener((BufferedImage... newViews) -> imageLayerUpdated(newViews));
		this.annotationView.addViewListener((BufferedImage... newViews) -> annotationLayerUpdated(newViews));
	}

	@Override
	public void addViewListener(ViewListener listener) {
		viewListeners.add(listener);
	}

	@Override
	public void addViewProcessListener(ViewProcessListener listener) {
		this.cachedView.addViewProcessListener(listener);
	}

	@Override
	public void setUserAnnotationVisibility(User user, boolean visible) {
		this.annotationView.setUserAnnotationVisibility(user, visible);
	}

	@Override
	public void setTermAnnotationVisibility(Term term, boolean visible) {
		this.annotationView.setTermAnnotationVisibility(term, visible);
	}

	@Override
	public synchronized BufferedImage getView(Dimension canvasSize) {
		if (canvasSize.width == 0 || canvasSize.height == 0) {
			return new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
		}
		return buildView(canvasSize);
	}

	private BufferedImage buildView(Dimension canvasSize) {
		currentImageView = cachedView.getView(getPosition(), canvasSize, getResolutionLevel());
		currentAnnotationView = annotationView.getView(getPosition(), canvasSize, getResolutionLevel());
		return currentImageView;
	}

	@Override
	public Image getImageInformation() {
		return cachedView.getImageInformation();
	}

	@Override
	public void stop() {
		this.cachedView.stop();
		this.annotationView.stop();
	}

	private void imageLayerUpdated(BufferedImage[] newViews) {
		currentImageView = newViews[0];
		notifyViewListeners();
	}

	private void annotationLayerUpdated(BufferedImage[] newViews) {
		currentAnnotationView = newViews[0];
		notifyViewListeners();
	}

	private void notifyViewListeners() {
		viewListeners.forEach(l -> l.onViewChanged(currentImageView, currentAnnotationView));
	}

}
