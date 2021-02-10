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
package org.bioimageanalysis.icy.icytomine.command.process.connected;

import java.util.List;

import org.bioimageanalysis.icy.icytomine.core.model.Image;

/**
 * @author Daniel Felipe Gonzalez Obando
 *
 */
public class ImagesCommandProcess extends ConnectedCommandProcess<String> {

	private static final String COMMAND = "images";
	private static final String NAME = "List images";
	private static final String[] ARGS_DESCRIPTION = new String[] { "projectID" };
	private static final String DESCRIPTION = "Lists all images associated to a project.";

	@Override
	public String getCommand() {
		return COMMAND;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String[] getArgumentsDescription() {
		return ARGS_DESCRIPTION;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public String call() throws Exception {
		if (getArguments().length < 1)
			throw new IllegalArgumentException("Expected at least 1 argument but got 0");
		StringBuffer imageList = new StringBuffer();

		for (int p = 0; p < getArguments().length; p++) {
			Long projectId = Long.parseLong(getArguments()[p]);

			List<Image> imgs = getClient().getProjectImages(projectId);
			imageList.append("Projects (ID, Name, # User annotations, # Algorithm annotations ):\n");
			for (Image img : imgs) {
				imageList.append(img.getId() + " " + img.getName().orElse("Not specified") + " "
						+ img.getAnnotationsOfUsersNumber().orElse(0L) + " " + img.getAnnotationsOfAlgorithmNumber().orElse(0L)
						+ "\n");
			}
		}
		return imageList.toString();
	}

}
