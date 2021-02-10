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
import java.util.stream.Collectors;

import org.bioimageanalysis.icy.icytomine.core.model.Annotation;

/**
 * @author Daniel Felipe Gonzalez Obando
 *
 */
public class AnnotationsCommandProcess extends ConnectedCommandProcess<String> {

	private static final String COMMAND = "annotations";
	private static final String NAME = "List image annotations made by users";
	private static final String[] ARGS_DESCRIPTION = new String[] { "imageID" };
	private static final String DESCRIPTION = "Lists all user annotation associated to a given image.";

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
		StringBuffer annotationList = new StringBuffer();

		for (int p = 0; p < getArguments().length; p++) {
			long imgId = Long.parseLong(getArguments()[p]);
			List<Annotation> annots = getClient().getImageAnnotations(imgId);
			annotationList.append("Annotations (ID, terms, Polygon):\n");
			for (Annotation annot : annots) {
				annotationList.append(
						annot.getId() + " " + annot.getAssociatedTerms().stream().map(t -> t.getName().orElse("Not specified"))
								.collect(Collectors.toSet()) + " " + annot.getLocation().orElse("Not specified") + "\n");
			}
		}
		return annotationList.toString();
	}

}
