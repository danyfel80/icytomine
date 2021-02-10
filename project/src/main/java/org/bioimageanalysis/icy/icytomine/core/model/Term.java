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
package org.bioimageanalysis.icy.icytomine.core.model;

import java.awt.Color;
import java.util.Optional;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;

public class Term extends Entity {

	public static final Color DEFAULT_TERM_COLOR = Color.GREEN;

	public static Term retrieve(CytomineClient client, long termId) throws CytomineClientException {
		return client.getTerm(termId);
	}

	public Term(CytomineClient client, be.cytomine.client.models.Term internalTerm) {
		super(client, internalTerm);
	}

	public be.cytomine.client.models.Term getInternalTerm() {
		return (be.cytomine.client.models.Term) getModel();
	}

	public Optional<String> getName() {
		return getStr("name");
	}

	public Optional<String> getComment() {
		return getStr("comment");
	}

	public Optional<String> getHexColor() {
		return getStr("color");
	}

	public Color getColor() {
		Optional<String> hexColor = getHexColor();
		if (!hexColor.isPresent()) {
			return DEFAULT_TERM_COLOR;
		} else {
			String hexColorString = hexColor.get();
			final int red = Integer.parseInt(hexColorString.substring(1, 3), 16);
			final int green = Integer.parseInt(hexColorString.substring(3, 5), 16);
			final int blue = Integer.parseInt(hexColorString.substring(5, 7), 16);
			final Color termColor = new Color(red, green, blue);
			return termColor;
		}
	}

	@Override
	public String toString() {
		return String.format("Term: id=%s, name=%s", String.valueOf(getId()), getName().orElse("Not specified"));
	}

}
