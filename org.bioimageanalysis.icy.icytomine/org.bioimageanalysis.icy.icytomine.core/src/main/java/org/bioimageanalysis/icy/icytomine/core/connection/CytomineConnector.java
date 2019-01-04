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

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.connection.persistence.Preferences;
import org.bioimageanalysis.icy.icytomine.core.connection.persistence.UserCredential;

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
		Map<String, HashMap<String, UserCredential>> credentials = Preferences.getInstance().getAvailableCytomineCredentials();

		if (credentials.containsKey(urlString))
			throw new IllegalArgumentException("URL " + urlString + " is already registered");

		credentials.put(url.toString(), new HashMap<>());
	}

	public static boolean removeHost(URL url) {
		if (url != null && url.toString().equals(Preferences.getInstance().getDefaultHostURL())) {
			Preferences.getInstance().setDefaultHost(null);
			Preferences.getInstance().setDefaultUser(null);
		}

		return Preferences.getInstance().getAvailableCytomineCredentials().remove(url.toString()) != null;
	}

	public static void updateHost(URL currentUrl, URL newUrl) {
		HashMap<String, UserCredential> currentHostUsers = Preferences.getInstance().getAvailableCytomineCredentials().get(currentUrl.toString());
		if (currentHostUsers != null) {
			Preferences.getInstance().getAvailableCytomineCredentials().remove(currentUrl.toString());
			addHostIfAbsent(newUrl);
			HashMap<String, UserCredential> newHostUsers = Preferences.getInstance().getAvailableCytomineCredentials().get(newUrl.toString());
			newHostUsers.putAll(currentHostUsers);
		}
	}

	public static void addUser(URL url, String userName, String publicKey, String privateKey)
			throws IllegalArgumentException {
		Map<String, UserCredential> users = Preferences.getInstance().getAvailableCytomineCredentials().get(url.toString());

		if (users == null)
			throw new IllegalArgumentException("URL " + url + " is not registered");

		if (users.containsKey(userName))
			throw new IllegalArgumentException("User name " + userName + " is already registered");

		UserCredential userData = new UserCredential();
		userData.setPublicKey(publicKey);
		userData.setPrivateKey(privateKey);
		users.put(userName, userData);
	}

	public static void addUserIfAbsent(URL url, String userName, String publicKey, String privateKey) {
		addHostIfAbsent(url);
		UserCredential userData = new UserCredential();
		userData.setPublicKey(publicKey);
		userData.setPrivateKey(privateKey);
		Preferences.getInstance().getAvailableCytomineCredentials().get(url.toString()).putIfAbsent(userName, userData);
	}

	public static void addHostIfAbsent(URL url) {
		Preferences.getInstance().getAvailableCytomineCredentials().putIfAbsent(url.toString(), new HashMap<>());
	}

	public static boolean removeUser(URL url, String userName) throws IllegalArgumentException {
		Map<String, UserCredential> users = Preferences.getInstance().getAvailableCytomineCredentials().get(url.toString());

		if (users == null)
			throw new IllegalArgumentException("URL " + url + " is not registered");

		if (url.toString().equals(Preferences.getInstance().getDefaultHostURL()) && userName != null
				&& userName.equals(Preferences.getInstance().getDefaultUserName())) {
			Preferences.getInstance().setDefaultUser(null);
		}
		if (users.size() == 1)
			removeHost(url);

		return users.remove(userName) != null;
	}

	public static void updateUser(URL oldUrl, String oldUserName, URL url, String userName, String publicKey,
			String privateKey) throws IllegalArgumentException, RuntimeException {
		Map<String, UserCredential> users = Preferences.getInstance().getAvailableCytomineCredentials().get(oldUrl.toString());
		if (users == null)
			throw new IllegalArgumentException("Host URL " + oldUrl + " is not registered");
		UserCredential userData = users.get(oldUserName);
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

	public static Future<CytomineClient> login(URL url, String userName) throws IllegalArgumentException {
		HashMap<String, UserCredential> users = Preferences.getInstance().getAvailableCytomineCredentials().get(url.toString());
		if (users == null)
			throw new IllegalArgumentException("Host URL " + url + " is not registered");
		UserCredential userData = users.get(userName);
		if (userData == null)
			throw new IllegalArgumentException("User " + userName + " is not registered");

		return ThreadUtil.bgRun(() -> {
			System.out.print("Connecting as " + userName + "...");
			CytomineClient client = CytomineClient.create(url, userData.getPublicKey(), userData.getPrivateKey());
			return client;
		});
	}

	public static Future<CytomineClient> login(URL url, String userName, boolean remember) {
		return ThreadUtil.bgRun(() -> {
			UserCredential userData;
			try {
				userData = Preferences.getInstance().getAvailableCytomineCredentials().get(url.toString()).get(userName);
			} catch (NullPointerException e) {
				throw new RuntimeException("User credentials could not be retrieved.", e);
			}
			
			CytomineClient client = CytomineClient.create(url, userData.getPublicKey(), userData.getPrivateKey());
			if (remember) {
				Preferences.getInstance().setDefaultHost(url.toString());
				Preferences.getInstance().setDefaultUser(userName);
			}
			return client;
		});
	}

}
