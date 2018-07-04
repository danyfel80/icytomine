package org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view;

import java.util.NoSuchElementException;

import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.view.ViewCanvasPanel;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.provider.CachedViewProvider;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.provider.ViewProvider;

public class ViewControllerFactory {

	public static ViewController create(ViewProvider viewProvider, ViewCanvasPanel canvasPanel) throws NoSuchElementException {
		if (viewProvider instanceof CachedViewProvider) {
			return new CachedViewController(((CachedViewProvider) viewProvider).getImageInformation(), canvasPanel);
		} else {
			throw new NoSuchElementException("No view controller for provider " + viewProvider.getClass().getSimpleName());
		}
	}

}
