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

import be.cytomine.client.Cytomine;

/**
 * @author Daniel Felipe Gonzalez Obando
 *
 */
public class Term {
	private Cytomine client;
	private be.cytomine.client.models.Term internalTerm;

	public Term(Cytomine client, be.cytomine.client.models.Term internalTerm) {
		this.client = client;
		this.internalTerm = internalTerm;
	}

	public Cytomine getClient() {
		return this.client;
	}

	public be.cytomine.client.models.Term getInternalTerm() {
		return this.internalTerm;
	}

	public Long getId() {
		return getInternalTerm().getId();
	}

	public String getName() {
		return getInternalTerm().getStr("name");
	}

	public String getComment() {
		return getInternalTerm().getStr("comment");
	}

	public String getHexColor() {
		return getInternalTerm().getStr("color");
	}

	public Color getColor() {
		String hexColor = getHexColor();
		final int red = Integer.parseInt(hexColor.substring(1, 3), 16);
		final int green = Integer.parseInt(hexColor.substring(3, 5), 16);
		final int blue = Integer.parseInt(hexColor.substring(5, 7), 16);
		final Color termColor = new Color(red, green, blue);
		return termColor;
	}
	
	@Override
	public String toString() {
		return getId() + " " + getName();
	}
}
