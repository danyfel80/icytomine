/*
 * Copyright 2010-2016 Institut Pasteur.
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
package org.bioimageanalysis.icy.icytomine.core.connection.user;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.annotation.JsonbProperty;

/**
 * This class holds the core Icytomine preferences
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class Preferences {

	private static Path DEFAULT_PREFERENCES_PATH = Paths.get(Paths.get("").toAbsolutePath().toString(),
			"preferences/icytomine.prefs");

	/**
	 * @return The default location for the preferences file
	 */
	public static Path getDefaultPreferencesPath() {
		return DEFAULT_PREFERENCES_PATH;
	}

	private static Preferences instance;

	private Map<String, HashMap<String, UserKeys>>	availableCytomineCredentials;
	@JsonbProperty(nillable = true)
	private String															defaultHost;
	@JsonbProperty(nillable = true)
	private String															defaultUser;

	/**
	 * Default constructor
	 */
	protected Preferences() {
		this.availableCytomineCredentials = new HashMap<>();
		this.defaultHost = null;
		this.defaultUser = null;
	}

	/**
	 * @return Singleton instance.
	 */
	public static Preferences getInstance() {
		if (instance == null) instance = new Preferences();
		return instance;
	}

	/**
	 * @return Map associating host addresses to user credentials.
	 */
	public Map<String, HashMap<String, UserKeys>> getAvailableCytomineCredentials() {
		return this.availableCytomineCredentials;
	}

	/**
	 * @param cytomineCredentials
	 *          Map of host/users to set
	 */
	public void setAvailableCytomineCredentials(Map<String, HashMap<String, UserKeys>> cytomineCredentials) {
		this.availableCytomineCredentials = cytomineCredentials;
	}

	/**
	 * @return The default host server. null if no default is set.
	 */
	public String getDefaultHost() {
		return defaultHost;
	}

	/**
	 * @param defaultHost
	 *          The default host server to set. Must exist in cytomine
	 *          credentials.
	 * @throws IllegalArgumentException
	 *           If {@code defaultHost} does not make part of the available
	 *           cytomine credentials.
	 */
	public void setDefaultHost(String defaultHost) throws IllegalArgumentException {
		if (defaultHost != null && !getAvailableCytomineCredentials().containsKey(defaultHost))
			throw new IllegalArgumentException("Invalid host " + defaultHost);
		this.defaultHost = defaultHost;
	}

	/**
	 * @return The default user. null if no default is set.
	 */
	public String getDefaultUser() {
		return defaultUser;
	}

	/**
	 * @param defaultUser
	 *          The default user to set. The user must exist for the current
	 *          default host.
	 * @throws IllegalArgumentException
	 *           If no default host is set. Also if {@code defaultUser} does not
	 *           make part of the available users for the default host.
	 */
	public void setDefaultUser(String defaultUser) throws IllegalArgumentException {
		HashMap<String, UserKeys> users = getAvailableCytomineCredentials().get(getDefaultHost());
		if (defaultUser != null && users == null)
			throw new IllegalArgumentException("default host " + getDefaultHost() + " does not exist in the hosts");
		if (defaultUser != null && !users.containsKey(defaultUser))
			throw new IllegalArgumentException("Invalid user " + defaultUser);
		this.defaultUser = defaultUser;
	}

	/**
	 * Saves this instance to the file specified by {@code path}.
	 * 
	 * @param path
	 *          Path to save this instance.
	 * @throws IOException
	 *           If The preferences cannot be saved to the provided path.
	 */
	public static void save(Path path) throws IOException {
		Preferences instance = getInstance();
		try {
			if (Files.notExists(DEFAULT_PREFERENCES_PATH.getParent())) {
				Files.createDirectories(DEFAULT_PREFERENCES_PATH.getParent());
			}
		} catch (IOException e) {
			throw new IOException("Exception preparing preferences path.", e);
		}
		try (BufferedOutputStream output = new BufferedOutputStream(
				Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
			try (Jsonb builder = JsonbBuilder.create(new JsonbConfig().setProperty(JsonbConfig.FORMATTING, true))) {
				builder.toJson(instance, output);
			} catch (Exception jsonE) {
				throw new IOException("Exception converting preferences.", jsonE);
			}
		} catch (FileNotFoundException e) {
			throw new IOException("Exception exporting preferences.", e);
		}
	}

	/**
	 * Saves this instance to the default preferences file provided by
	 * {@link #getDefaultPreferencesPath()}.
	 * 
	 * @throws IOException
	 *           If The preferences cannot be saved to the provided path.
	 */
	public static void save() throws IOException {
		save(Preferences.getDefaultPreferencesPath());
	}

	/**
	 * Loads the preferences from the file specified by {@code path}.
	 * 
	 * @param path
	 *          Path of the file containing the preferences to load.
	 * @return Preferences loaded from {@code path}.
	 * @throws IOException
	 *           If The preferences cannot be loaded from the provided path.
	 */
	public static void load(Path path) throws IOException {
		try (BufferedInputStream input = new BufferedInputStream(Files.newInputStream(path))) {
			try (Jsonb builder = JsonbBuilder.create(new JsonbConfig().setProperty(JsonbConfig.FORMATTING, true))) {
				instance = builder.fromJson(input, Preferences.class);
			} catch (Exception jsonE) {
				throw new IOException("Exception converting preferences.", jsonE);
			}
		} catch (IOException e) {
			throw new IOException("Exception loading preferences.", e);
		}
	}

	/**
	 * Loads the preferences from the default preferences file specified by
	 * {@link #getDefaultPreferencesPath()}.
	 * 
	 * @return Preferences loaded from {@link #getDefaultPreferencesPath()}.
	 * @throws IOException
	 *           If The preferences cannot be loaded from the provided path.
	 */
	public static void load() throws IOException {
		load(Preferences.getDefaultPreferencesPath());
	}

	/**
	 * Loads the preferences from the default preferences file specified by
	 * {@link #getDefaultPreferencesPath()}. If the file does not exist, an clean
	 * instance is created.
	 * 
	 * @throws IOException
	 *           If The preferences cannot be loaded from the provided existing
	 *           path.
	 */
	public static void loadOrDefault() throws IOException {
		if (Files.exists(getDefaultPreferencesPath())) {
			load();
		} else {
			Preferences.clear();
		}
	}

	public static void clear() {
		instance = new Preferences();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((availableCytomineCredentials == null)? 0: availableCytomineCredentials.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
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
		if (!(obj instanceof Preferences)) {
			return false;
		}
		Preferences other = (Preferences) obj;
		if (availableCytomineCredentials == null) {
			if (other.availableCytomineCredentials != null) {
				return false;
			}
		} else if (!availableCytomineCredentials.equals(other.availableCytomineCredentials)) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("Preferences [availableCytomineCredentials=%s]", availableCytomineCredentials);
	}

}
