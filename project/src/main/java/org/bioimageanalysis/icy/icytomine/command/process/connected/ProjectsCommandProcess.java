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

import org.bioimageanalysis.icy.icytomine.core.model.Project;

/**
 * @author Daniel Felipe Gonzalez Obando
 *
 */
public class ProjectsCommandProcess extends ConnectedCommandProcess<String> {

	private static final String COMMAND = "projects";
	private static final String NAME = "List projects";
	private static final String[] ARGS_DESCRIPTION = new String[0];
	private static final String DESCRIPTION = "Lists all projects associated to the connected user.";

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
		StringBuffer projectList = new StringBuffer();

		List<Project> projects = getClient().getUserProjects(getClient().getCurrentUser().getId());
		projectList.append("Projects (ID, Name, Description, # Images, # Annotations):\n");
		for (Project project : projects) {
			projectList.append(project.getId() + " " + project.getName().orElse("Not specified") + " "
					+ project.getDescription().getData().orElse("Not specified") + " " + project.getNumberOfImages().orElse(0L)
					+ " " + project.getNumberOfAnnotations().orElse(0L) + "\n");
		}

		return projectList.toString();
	}

}
