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
package org.bioimageanalysis.icy.icytomine.command.process;

/**
 * This command allows the command processor to stop the cytomine console.
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class ExitCommand implements CommandProcess<Void> {

	private static final String COMMAND = "exit";
	private static final String NAME = "Exit";

	@Override
	public Void call() throws Exception {
		return null; // Does nothing.
	}

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
		return new String[0];
	}

	@Override
	public String getDescription() {
		return "Exits from the cytomine console.";
	}

	@Override
	public CommandProcess<Void> setArguments(String[] args) {
		return this; // Does nothing with arguments
	}

	@Override
	public CommandProcess<Void> setPreviousResult(Object result) {
		return this; // Does nothing with previous result
	}

	@Override
	public String[] getArguments() {
		return null;
	}

	@Override
	public Object getPreviousResult() {
		return null;
	}

}
