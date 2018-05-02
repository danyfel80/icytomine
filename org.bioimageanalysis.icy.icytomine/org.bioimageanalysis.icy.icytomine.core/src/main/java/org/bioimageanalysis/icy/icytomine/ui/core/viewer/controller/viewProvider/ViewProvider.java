package org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.viewProvider;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.view.CachedView.ViewListener;

public abstract class ViewProvider {

	public interface ViewProcessListener {
		void onViewProcessEvent(boolean isProcessing);
	}

	private Point2D position;
	private double resolutionLevel;

	public ViewProvider() {
		position = new Point2D.Double();
		resolutionLevel = 0d;
	}

	public Point2D getPosition() {
		return position;
	}

	public void setPosition(Point2D position) {
		this.position.setLocation(position);
	}

	public double getResolutionLevel() {
		return resolutionLevel;
	}

	public void setResolutionLevel(double resolutionLevel) {
		this.resolutionLevel = resolutionLevel;
	}

	public abstract void addViewListener(ViewListener listener);

	public abstract BufferedImage getView(Dimension size);

	public abstract void stop();

	public abstract void addViewProcessListener(ViewProcessListener listener);

	public Image getImageInformation() {
		return null;
	}

}
