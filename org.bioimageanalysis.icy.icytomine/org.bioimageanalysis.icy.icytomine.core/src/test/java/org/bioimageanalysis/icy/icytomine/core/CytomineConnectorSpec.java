package org.bioimageanalysis.icy.icytomine.core;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.bioimageanalysis.icy.icytomine.core.connection.CytomineConnector;
import org.bioimageanalysis.icy.icytomine.core.connection.user.Preferences;
import org.bioimageanalysis.icy.icytomine.core.connection.user.UserKeys;
import org.junit.Before;
import org.junit.Test;

import be.cytomine.client.Cytomine;
import be.cytomine.client.CytomineException;

public class CytomineConnectorSpec {

	private String	urlString;
	private URL			url;
	private String	userName;
	private String	publicKey;
	private String	privateKey;

	@Before
	public void setup() throws MalformedURLException {
		Preferences.clear();

		urlString = "http://localhost-core/";
		url = new URL(urlString);
		userName = "danyfel80";
		publicKey = "8d5272e3-a97a-4b50-a365-70fa7aaa0d5a";
		privateKey = "3a3dbe30-788f-46bd-ab4d-5a71ce4bf9b4";
	}

	@Test
	public void givenHostURLWhenAddHostThenURLInAvailableCredentials() throws MalformedURLException {
		CytomineConnector.addHost(url);
		assertThat(Preferences.getInstance().getAvailableCytomineCredentials().containsKey(urlString), is(true));
	}

	private void addHostAndUser() {
		CytomineConnector.addHost(url);
		CytomineConnector.addUser(url, userName, publicKey, privateKey);
	}

	@Test
	public void givenHostURLAndUserInfoWhenAddUserThenUserInAvailableCredentials() throws MalformedURLException {
		addHostAndUser();
		Preferences prefs = Preferences.getInstance();
		assertThat(prefs.getAvailableCytomineCredentials().containsKey(urlString.toString()), is(true));
		assertThat(prefs.getAvailableCytomineCredentials().get(urlString).containsKey(userName), is(true));
		UserKeys userData = prefs.getAvailableCytomineCredentials().get(urlString).get(userName);
		assertThat(userData.getPublicKey(), is(equalTo(publicKey)));
		assertThat(userData.getPrivateKey(), is(equalTo(privateKey)));
	}

	@Test
	public void givenHostURLWhenRemoveHostThenHostRemovedFromCredentials() {
		addHostAndUser();
		CytomineConnector.removeHost(url);
		Preferences prefs = Preferences.getInstance();
		assertThat(prefs.getAvailableCytomineCredentials().containsKey(urlString), is(false));
	}

	@Test
	public void givenHostURLAndUserNameWhenRemoveUserThenUserRemovedFromHostCredentials() {
		addHostAndUser();
		CytomineConnector.removeUser(url, userName);
		Preferences prefs = Preferences.getInstance();
		assertThat(prefs.getAvailableCytomineCredentials().get(urlString), is(nullValue()));
	}

	@Test
	public void givenHostAndUserDataWhenUpdateUserThenUserUpdatedInCredentials()
			throws IllegalArgumentException, MalformedURLException, RuntimeException {
		addHostAndUser();
		URL newUrl = new URL("http://otherserver/");
		String newUserName = "newUserName";
		CytomineConnector.updateUser(url, userName, newUrl, newUserName, "otherPublic1", "otherPrivate1");
		Preferences prefs = Preferences.getInstance();
		UserKeys userData = new UserKeys();
		userData.setPublicKey("otherPublic1");
		userData.setPrivateKey("otherPrivate1");
		assertThat(prefs.getAvailableCytomineCredentials().get(urlString), is(nullValue()));
		assertThat(prefs.getAvailableCytomineCredentials().get(newUrl.toString()).get(newUserName), is(equalTo(userData)));
	}

	@Test
	public void givenGoodHostAndUserNameWhenLoginThenFutureInTrue()
			throws InterruptedException, ExecutionException, CytomineException {
		addHostAndUser();
		Future<Cytomine> connectionFuture = CytomineConnector.login(url, userName);
		Cytomine connection = connectionFuture.get();
		assertThat(connection, is(notNullValue()));
		assertThat(connection.getCurrentUser().get("username"), is(equalTo(userName)));
	}
}
