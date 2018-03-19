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

import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.core.model.Image;

import be.cytomine.client.collections.AnnotationCollection;

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
		if (args.length < 1)
			throw new IllegalArgumentException("Expected at least 1 argument but got 0");
		StringBuffer annotationList = new StringBuffer();
		
		for (int p = 0; p < args.length; p++) {
			Long imgId = Long.parseLong(args[p]);
			Image image = new Image(client.getImageInstance(imgId), client);
			
			AnnotationCollection annots = client.getAnnotationsByImage(imgId);
			annotationList.append("Annotations (ID, terms, Polygon):\n");
			for (int i = 0; i < annots.size(); i++) {
				Annotation annot = new Annotation(annots.get(i), image, client);
				annotationList.append(
						annot.getId() + " " + annot.getTerms() + " " + annot.getLocation() + "\n");
			}
		}
		return annotationList.toString();
	}

}
