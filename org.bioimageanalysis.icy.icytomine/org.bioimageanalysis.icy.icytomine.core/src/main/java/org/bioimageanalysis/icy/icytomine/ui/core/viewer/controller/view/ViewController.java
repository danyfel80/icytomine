package org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view;

import java.awt.geom.Rectangle2D;
import java.util.Set;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;
import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.CachedViewController.PositionListener;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.CachedViewController.ResolutionListener;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.provider.ViewProvider;

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

	void setVisibileAnnotations(Set<Annotation> newVisibleAnnotations);

	Set<Annotation> getVisibleAnnotations();

	Set<Annotation> getActiveAnnotations();

	void setSelectedAnnotations(Set<Annotation> selectedAnnotations);
	
	Rectangle2D getCurrentViewBoundsAtZeroResolution();

	double getCurrentResolution();

	ViewProvider getViewProvider();

	void focusOnAnnotation(Annotation a);

	void updateAnnotations() throws CytomineClientException;


}