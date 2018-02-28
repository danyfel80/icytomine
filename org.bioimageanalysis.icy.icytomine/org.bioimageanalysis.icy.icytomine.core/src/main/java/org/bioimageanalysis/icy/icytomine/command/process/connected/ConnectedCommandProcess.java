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

import org.bioimageanalysis.icy.icytomine.command.process.CommandProcess;

import be.cytomine.client.Cytomine;

/**
 * Instances of this class can use the cytomine client during their execution if
 * it has been set using the {@link #setCytomineClient(Cytomine)} method.
 * 
 * @author Daniel Felipe Gonzalez Obando
 * @param <T>
 *          Type of result for the execution
 */
public interface ConnectedCommandProcess<T> extends CommandProcess<T> {
	void setCytomineClient(Cytomine client);
}
