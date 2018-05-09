/*
 * Copyright 2010-2016 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bioimageanalysis.icy.icytomine.ui.core.explorer;

import javax.swing.JFrame;

import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.view.AnnotationView;
import org.bioimageanalysis.icy.icytomine.core.view.CachedView;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.ViewerFrame;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.provider.CachedViewProvider;

import be.cytomine.client.Cytomine;
import be.cytomine.client.CytomineException;
import icy.gui.frame.IcyFrame;

/**
 * @author Daniel Felipe Gonzalez Obando
 */
public class ExplorerFrame extends IcyFrame {

	private Cytomine cytomineClient;

	private ExplorerPanel explorerPanel;

	public ExplorerFrame(Cytomine cytomine) {
		super("Explorer - Icytomine", true, true, true, false);
		this.cytomineClient = cytomine;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		createExplorerPanel();
		addToDesktopPane();
		center();
	}

	private void createExplorerPanel() {
		explorerPanel = new ExplorerPanel(cytomineClient);
		explorerPanel.setOpenViewerListener(imageInformation -> {
			try {
				openViewer(imageInformation);
			} catch (CytomineException e) {
				throw new RuntimeException(e);
			}
		});
		setSize(explorerPanel.getPreferredSize());
		setMinimumSize(explorerPanel.getMinimumSize());
		setContentPane(explorerPanel);
	}

	private void openViewer(Image imageInformation) throws CytomineException {
		ViewerFrame viewer = getViewerFrame(imageInformation);
		viewer.setVisible(true);
	}

	private ViewerFrame getViewerFrame(Image imageInformation) throws CytomineException {
		return new ViewerFrame(new CachedViewProvider(new CachedView(imageInformation), new AnnotationView(imageInformation)));
	}

	/**
	 * @return The login panel
	 */
	public ExplorerPanel getExplorerPanel() {
		return explorerPanel;
	}
}
