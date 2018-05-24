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

	private static final be.cytomine.client.models.Term internalNoTerm;
	static {
		internalNoTerm = new be.cytomine.client.models.Term();
		internalNoTerm.set("id", 0L);
		internalNoTerm.set("name", "No term");
	};

	public static Term getNoTerm(Cytomine client) {
		return new Term(client, internalNoTerm);
	}

	private Cytomine client;
	private be.cytomine.client.models.Term internalTerm;

	private String name;
	private String comment;

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
		if (name == null) {
			name = getInternalTerm().getStr("name");
			name = CytomineUtils.convertFromSystenEncodingToUTF8(name);
		}
		return name;
	}

	public String getComment() {
		if (comment == null) {
			comment = getInternalTerm().getStr("comment");
			comment = CytomineUtils.convertFromSystenEncodingToUTF8(comment);
		}
		return comment;
	}

	public String getHexColor() {
		return getInternalTerm().getStr("color");
	}

	public Color getColor() {
		String hexColor = getHexColor();
		if (hexColor == null) {
			return Color.DARK_GRAY;
		} else {
			final int red = Integer.parseInt(hexColor.substring(1, 3), 16);
			final int green = Integer.parseInt(hexColor.substring(3, 5), 16);
			final int blue = Integer.parseInt(hexColor.substring(5, 7), 16);
			final Color termColor = new Color(red, green, blue);
			return termColor;
		}
	}

	@Override
	public String toString() {
		return getId() + " " + getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((client == null) ? 0 : client.getHost().hashCode());
		result = prime * result + ((internalTerm == null) ? 0 : internalTerm.getId().hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Term)) {
			return false;
		}
		Term other = (Term) obj;
		if (client == null) {
			if (other.client != null) {
				return false;
			}
		} else if (!client.getHost().equals(other.client.getHost())) {
			return false;
		}
		if (internalTerm == null) {
			if (other.internalTerm != null) {
				return false;
			}
		} else if (!internalTerm.getId().equals(other.internalTerm.getId())) {
			return false;
		}
		return true;
	}

}
