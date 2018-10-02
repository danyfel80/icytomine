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

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.ui.core.explorer.ImageDetailsPanelController.ImageMagnificationChangeListener;
import org.bioimageanalysis.icy.icytomine.ui.core.explorer.ImageDetailsPanelController.ImageResolutionChangeListener;
import org.bioimageanalysis.icy.icytomine.ui.core.explorer.ImagePanel.ImageSelectionListener;

import icy.gui.frame.IcyFrame;

/**
 * @author Daniel Felipe Gonzalez Obando
 */
public class ExplorerFrame extends IcyFrame {

	private ExplorerPanel explorerPanel;

	public ExplorerFrame() {
		super("Explorer - Icytomine", true, true, true, true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		createExplorerPanel();
		addToDesktopPane();
		center();
	}

	private void createExplorerPanel() {
		explorerPanel = new ExplorerPanel();
		setSize(explorerPanel.getPreferredSize());
		setMinimumSize(explorerPanel.getMinimumSize());
		setContentPane(explorerPanel);
	}

	public void setClient(CytomineClient client) {
		explorerPanel.setClient(client);
	}

	public ExplorerPanel getExplorerPanel() {
		return explorerPanel;
	}

	public void addOpenViewerListener(ImageSelectionListener listener) {
		explorerPanel.addOpenViewerListener(listener);
	}
}
