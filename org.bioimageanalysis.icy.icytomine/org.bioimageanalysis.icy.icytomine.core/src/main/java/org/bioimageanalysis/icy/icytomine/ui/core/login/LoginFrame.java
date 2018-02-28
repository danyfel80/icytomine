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
package org.bioimageanalysis.icy.icytomine.ui.core.login;

import javax.swing.JFrame;

import icy.gui.frame.IcyFrame;

public class LoginFrame extends IcyFrame {

	private LoginPanel loginPanel;

	public LoginFrame() {
		super("Login - Icytomine", false, true, false, false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		loginPanel = new LoginPanel(this.getExternalFrame());
		setSize(loginPanel.getPreferredSize());
		setMinimumSize(loginPanel.getPreferredSize());
		setContentPane(loginPanel);
		addToDesktopPane();
		center();
	}

	/**
	 * @return The login panel
	 */
	public LoginPanel getLoginPanel() {
		return loginPanel;
	}

}
