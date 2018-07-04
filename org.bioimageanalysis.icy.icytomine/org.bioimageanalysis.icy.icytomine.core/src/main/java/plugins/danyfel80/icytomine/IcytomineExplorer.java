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
package plugins.danyfel80.icytomine;

import java.io.IOException;

import org.bioimageanalysis.icy.icytomine.command.CommandProcessor;
import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;
import org.bioimageanalysis.icy.icytomine.core.connection.persistence.Preferences;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.ui.core.explorer.ExplorerFrame;
import org.bioimageanalysis.icy.icytomine.ui.core.login.LoginFrame;
import org.bioimageanalysis.icy.icytomine.ui.core.login.LoginPanelController.LoginListener;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.ViewerFrame;

import be.cytomine.client.CytomineException;
import icy.gui.dialog.MessageDialog;
import icy.main.Icy;
import icy.plugin.abstract_.PluginActionable;

/**
 * @author Daniel Felipe Gonzalez Obando
 */
public class IcytomineExplorer extends PluginActionable {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		System.out.println("Reading Preferences...");
		// Prepare settings
		try {
			Preferences.loadOrDefault();
		} catch (IOException e) {
			if (!Icy.getMainInterface().isHeadLess()) {
				MessageDialog.showDialog("Error - Icytomine", e.getMessage(), MessageDialog.ERROR_MESSAGE);
			}
			e.printStackTrace();
		}

		// Headless mode
		if (Icy.getMainInterface().isHeadLess()) {
			try {
				CommandProcessor processor = new CommandProcessor();
				processor.call();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		// GUI mode
		else {
			System.out.println("Showing Login...");
			// Login to Cytomine
			LoginFrame loginFrame = new LoginFrame();
			loginFrame.getLoginPanel().addLoginListener(getLoginHandler(loginFrame));
			loginFrame.setVisible(true);
		}
	}

	private LoginListener getLoginHandler(LoginFrame loginFrame) {
		return client -> {
			try {
				String username = client.getCurrentUser().getName().orElse("not specified");
				System.out.println("Logged in as " + username);
				ExplorerFrame explorerFrame = new ExplorerFrame();
				explorerFrame.setClient(client);
				explorerFrame.addOpenViewerListener(imageInformation -> {
					try {
						openViewer(imageInformation);
					} catch (CytomineException e) {
						throw new RuntimeException(e);
					}
				});
				explorerFrame.setVisible(true);
				loginFrame.setVisible(false);
				loginFrame.dispose();
				System.out.println("Login done");
			} catch (CytomineClientException e) {
				MessageDialog.showDialog("Icytomine: Error", e.getMessage(), MessageDialog.ERROR_MESSAGE);
			}
		};
	}

	private void openViewer(Image imageInformation) throws CytomineException {
		ViewerFrame viewer = createViewerFrame(imageInformation);
		viewer.setVisible(true);
	}

	private ViewerFrame createViewerFrame(Image imageInstance) throws CytomineException {
		ViewerFrame viewer = new ViewerFrame();
		viewer.setImageInstance(imageInstance);
		return viewer;
	}

}
