/*
 * Copyright 2010-2018 Institut Pasteur.
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
package org.bioimageanalysis.icy.icytomine.ui.core.viewer;

import javax.swing.JFrame;

import org.bioimageanalysis.icy.icytomine.core.model.Image;

import icy.gui.frame.IcyFrame;
import icy.gui.frame.IcyFrameEvent;
import icy.gui.frame.IcyFrameListener;

/**
 * @author Daniel Felipe Gonzalez Obando
 */
public class ViewerFrame extends IcyFrame {
	private ViewPanel viewerPanel;

	/**
	 * Creates a frame containing a {@link ViewPanel} that displays the given
	 * {@code image}.
	 * 
	 * @param imageInformation
	 *          Information of the image to be shown.
	 */
	public ViewerFrame(Image imageInformation) {
		super(imageInformation.getName() + " - Icytomine", true, true, true, false);

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		viewerPanel = new ViewPanel(imageInformation);
		setSize(viewerPanel.getPreferredSize());
		setMinimumSize(viewerPanel.getMinimumSize());
		setContentPane(viewerPanel);
		addFrameListener(new IcyFrameListener() {

			@Override
			public void icyFrameOpened(IcyFrameEvent e) {}

			@Override
			public void icyFrameInternalized(IcyFrameEvent e) {}

			@Override
			public void icyFrameIconified(IcyFrameEvent e) {}

			@Override
			public void icyFrameExternalized(IcyFrameEvent e) {}

			@Override
			public void icyFrameDeiconified(IcyFrameEvent e) {}

			@Override
			public void icyFrameDeactivated(IcyFrameEvent e) {}

			@Override
			public void icyFrameClosing(IcyFrameEvent e) {}

			@Override
			public void icyFrameClosed(IcyFrameEvent e) {
				viewerPanel.close();
			}

			@Override
			public void icyFrameActivated(IcyFrameEvent e) {
				getViewerPanel().gainFocus();
			}
		});
		addToDesktopPane();
		center();
	}

	/**
	 * @return The viewer panel.
	 */
	public ViewPanel getViewerPanel() {
		return viewerPanel;
	}
}
