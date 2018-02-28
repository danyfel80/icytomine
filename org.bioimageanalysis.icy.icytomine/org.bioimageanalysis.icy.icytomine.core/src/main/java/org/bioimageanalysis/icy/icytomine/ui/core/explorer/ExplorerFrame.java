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

import org.bioimageanalysis.icy.icytomine.ui.core.viewer.ViewerFrame;

import be.cytomine.client.Cytomine;
import icy.gui.frame.IcyFrame;

/**
 * @author Daniel Felipe Gonzalez Obando
 */
public class ExplorerFrame extends IcyFrame {
	private ExplorerPanel explorerPanel;

	public ExplorerFrame(Cytomine cytomine) {
		super("Explorer - Icytomine", true, true, true, false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		explorerPanel = new ExplorerPanel(cytomine);
		explorerPanel.setOpenViewerListener(i->{
			ViewerFrame viewer = new ViewerFrame(i);
			viewer.setVisible(true);
		});
		setSize(explorerPanel.getPreferredSize());
		setMinimumSize(explorerPanel.getMinimumSize());
		setContentPane(explorerPanel);
		addToDesktopPane();
		center();
	}

	/**
	 * @return The login panel
	 */
	public ExplorerPanel getExplorerPanel() {
		return explorerPanel;
	}
}
