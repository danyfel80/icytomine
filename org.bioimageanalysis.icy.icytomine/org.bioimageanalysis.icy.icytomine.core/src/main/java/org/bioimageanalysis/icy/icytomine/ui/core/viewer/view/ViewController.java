package org.bioimageanalysis.icy.icytomine.ui.core.viewer.view;

import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.view.CachedViewController.PositionListener;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.view.CachedViewController.ResolutionListener;

public interface ViewController {

	void addResolutionListener(ResolutionListener listener);

	void addCursorPositionListener(PositionListener listener);

	void zoomIn();

	void zoomOut();

	void setResolution(double resolutionLevel);

	Image getImageInformation();

	void adjustImageZoomToView();

	void refreshView();

	void stopView();

}