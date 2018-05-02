package org.bioimageanalysis.icy.icytomine.ui.core.viewer.view;

import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.view.ViewCanvasPanel;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.viewProvider.CachedViewProvider;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.viewProvider.ViewProvider;

public class ViewControllerFactory {

	public static ViewController create(ViewProvider viewProvider, ViewCanvasPanel canvasPanel) {
		if (viewProvider instanceof CachedViewProvider) {
			return new CachedViewController(((CachedViewProvider)viewProvider).getImageInformation(), canvasPanel);
		} else {
			return new NullViewController(canvasPanel);
		}
	}

}
