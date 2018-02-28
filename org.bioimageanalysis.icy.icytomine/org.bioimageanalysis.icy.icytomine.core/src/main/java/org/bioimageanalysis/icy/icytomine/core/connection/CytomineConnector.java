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
package org.bioimageanalysis.icy.icytomine.core.connection;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import org.bioimageanalysis.icy.icytomine.core.connection.user.Preferences;
import org.bioimageanalysis.icy.icytomine.core.connection.user.UserKeys;

import be.cytomine.client.Cytomine;
import be.cytomine.client.CytomineException;
import icy.system.thread.ThreadUtil;

/**
 * This class is a helper class to manage hosts, users and connections to
 * cytomine servers.
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class CytomineConnector {

	public static void addHost(final URL url) throws IllegalArgumentException {
		String urlString = url.toString();
		Map<String, HashMap<String, UserKeys>> credentials = Preferences.getInstance().getAvailableCytomineCredentials();

		if (credentials.containsKey(urlString))
			throw new IllegalArgumentException("URL " + urlString + " is already registered");

		credentials.put(url.toString(), new HashMap<>());
	}

	public static void addUser(URL url, String userName, String publicKey, String privateKey)
			throws IllegalArgumentException {
		Map<String, UserKeys> users = Preferences.getInstance().getAvailableCytomineCredentials().get(url.toString());

		if (users == null)
			throw new IllegalArgumentException("URL " + url + " is not registered");

		if (users.containsKey(userName))
			throw new IllegalArgumentException("User name " + userName + " is already registered");

		UserKeys userData = new UserKeys();
		userData.setPublicKey(publicKey);
		userData.setPrivateKey(privateKey);
		users.put(userName, userData);
	}

	public static void addUserIfAbsent(URL url, String userName, String publicKey, String privateKey) {
		addHostIfAbsent(url);
		UserKeys userData = new UserKeys();
		userData.setPublicKey(publicKey);
		userData.setPrivateKey(privateKey);
		Preferences.getInstance().getAvailableCytomineCredentials().get(url.toString()).putIfAbsent(userName, userData);
	}

	public static void addHostIfAbsent(URL url) {
		Preferences.getInstance().getAvailableCytomineCredentials().putIfAbsent(url.toString(), new HashMap<>());
	}

	public static boolean removeHost(URL url) {
		if (url != null && url.toString().equals(Preferences.getInstance().getDefaultHost())) {
			Preferences.getInstance().setDefaultHost(null);
			Preferences.getInstance().setDefaultUser(null);
		}

		return Preferences.getInstance().getAvailableCytomineCredentials().remove(url.toString()) != null;
	}

	public static boolean removeUser(URL url, String userName) throws IllegalArgumentException {
		Map<String, UserKeys> users = Preferences.getInstance().getAvailableCytomineCredentials().get(url.toString());

		if (users == null)
			throw new IllegalArgumentException("URL " + url + " is not registered");

		if (url.toString().equals(Preferences.getInstance().getDefaultHost()) && userName != null
				&& userName.equals(Preferences.getInstance().getDefaultUser())) {
			Preferences.getInstance().setDefaultUser(null);
		}
		if (users.size() == 1)
			removeHost(url);

		return users.remove(userName) != null;
	}

	public static void updateUser(URL oldUrl, String oldUserName, URL url, String userName, String publicKey,
			String privateKey) throws IllegalArgumentException, RuntimeException {
		Map<String, UserKeys> users = Preferences.getInstance().getAvailableCytomineCredentials().get(oldUrl.toString());
		if (users == null)
			throw new IllegalArgumentException("Host URL " + oldUrl + " is not registered");
		UserKeys userData = users.get(oldUserName);
		if (userData == null)
			throw new IllegalArgumentException("User " + oldUserName + " is not registered");

		removeUser(oldUrl, oldUserName);
		addHostIfAbsent(url);
		try {
			addUser(url, userName, publicKey, privateKey);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("User already exists with other credentials", e);
		}
	}

	public static Future<Cytomine> login(URL url, String userName) throws IllegalArgumentException, RuntimeException {
		HashMap<String, UserKeys> users = Preferences.getInstance().getAvailableCytomineCredentials().get(url.toString());
		if (users == null)
			throw new IllegalArgumentException("Host URL " + url + " is not registered");
		UserKeys userData = users.get(userName);
		if (userData == null)
			throw new IllegalArgumentException("User " + userName + " is not registered");

		return ThreadUtil.bgRun(() -> {
			try {
				Cytomine connection = new Cytomine(url.toString(), userData.getPublicKey(), userData.getPrivateKey());
				System.out.print("Connecting as " + userName + "...");
				if (!userName.equals(connection.getCurrentUser().get("username"))) {
					System.out.println("Unsuccessful");
					throw new IllegalArgumentException(
							"\nUser " + userName + " is not registered in host server. Check your credentials");
				}
				System.out.println("Successful");
				return connection;
			} catch (CytomineException e) {
				throw new RuntimeException("Error while connecting to the server. Check your credentials.", e);
			}
		});
	}

	public static Future<Cytomine> login(URL url, String userName, boolean remember) throws RuntimeException {
		return ThreadUtil.bgRun(() -> {
			try {
				UserKeys userData = Preferences.getInstance().getAvailableCytomineCredentials().get(url.toString())
						.get(userName);
				Cytomine connection = new Cytomine(url.toString(), userData.getPublicKey(), userData.getPrivateKey());
				if (connection.getCurrentUser().get("username").equals(userName))
					throw new RuntimeException("The user name does not match with that associated to the given keys");
				if (remember) {
					Preferences.getInstance().setDefaultHost(url.toString());
					Preferences.getInstance().setDefaultUser(userName);
				}
				return connection;
			} catch (CytomineException e) {
				throw new RuntimeException("Exception while connecting to the server", e);
			}
		});
	}

}
