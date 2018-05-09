package org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view;

import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.view.ViewCanvasPanel;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.provider.CachedViewProvider;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.provider.ViewProvider;

public class ViewControllerFactory {

	public static ViewController create(ViewProvider viewProvider, ViewCanvasPanel canvasPanel) {
		if (viewProvider instanceof CachedViewProvider) {
			return new CachedViewController(((CachedViewProvider)viewProvider).getImageInformation(), canvasPanel);
		} else {
			return new NullViewController(canvasPanel);
		}
	}

}
