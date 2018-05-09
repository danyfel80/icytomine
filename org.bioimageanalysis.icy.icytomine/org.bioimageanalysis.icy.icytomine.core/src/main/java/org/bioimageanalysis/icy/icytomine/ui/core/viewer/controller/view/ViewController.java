package org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view;

import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.model.Term;
import org.bioimageanalysis.icy.icytomine.core.model.User;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.CachedViewController.PositionListener;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.CachedViewController.ResolutionListener;

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

	void setUserAnnotationVisibility(User user, boolean selected);

	void setTermAnnotationVisibility(Term term, boolean selected);

}