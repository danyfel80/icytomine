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

import java.net.URL;

import org.bioimageanalysis.icy.icytomine.core.connection.CytomineConnector;
import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.connection.persistence.Preferences;

/**
 * @author Daniel Felipe Gonzalez Obando
 *
 */
public class ConnectionCommand implements CommandProcess<CytomineClient> {

	private static final String COMMAND = "connect";
	private static final String[] ARGUMENTS = new String[] { "host", "username", "(Optional) publicKey", "(Optional) privateKey" };
	private static final String NAME = "Connection";
	private static final String DESCRIPTION = "Allows to connect to a cytomine server by providing the corresponding credentials";

	private String[] args;

	@Override
	public CytomineClient call() throws Exception {
		Preferences.loadOrDefault();
		Preferences prefs = Preferences.getInstance();
		checkArgs();

		URL hostURL = null;
		String username = null;
		String publicKey = null;
		String privateKey = null;

		switch (args.length) {
		case 0:
			if (!prefs.getDefaultHostURL().isPresent())
				throw new IllegalArgumentException("No registered default host.");
			hostURL = new URL(prefs.getDefaultHostURL().get());
			if (!prefs.getDefaultUserName().isPresent())
				throw new IllegalArgumentException("No registered default user.");
			username = prefs.getDefaultUserName().get();
			break;

		case 2:
			hostURL = new URL(args[0]);
			username = args[1];
			break;

		case 4:
			hostURL = new URL(args[0]);
			username = args[1];
			publicKey = args[2];
			privateKey = args[3];
			break;
		}

		if (args.length == 4) {
			CytomineConnector.addUserIfAbsent(hostURL, username, publicKey, privateKey);
			Preferences.save();
		}
		
		return CytomineConnector.login(hostURL, username).get();
	}

	private void checkArgs() {
		if (args.length != 0 && args.length != 2 && args.length != 4)
			throw new IllegalArgumentException(String.format("2 or 4 arguments are expected, but received %d.", args.length));
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
		return ARGUMENTS;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public ConnectionCommand setArguments(String[] args) {
		this.args = args;
		return this;
	}

	@Override
	public CommandProcess<CytomineClient> setPreviousResult(Object result) {
		return this; // Take nothing from before
	}

	@Override
	public String[] getArguments() {
		return args;
	}

	@Override
	public Object getPreviousResult() {
		return null;
	}

}
