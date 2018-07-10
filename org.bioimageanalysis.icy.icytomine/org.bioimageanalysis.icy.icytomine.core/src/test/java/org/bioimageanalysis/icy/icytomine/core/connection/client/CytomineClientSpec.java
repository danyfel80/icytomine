package org.bioimageanalysis.icy.icytomine.core.connection.client;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CytomineClientSpec {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private URL host;
	private String username;
	private String publicKey;
	private String privateKey;

	private CytomineClient client;

	@Before
	public void setUpBefore() throws Exception {
		host = new URL("http://localhost-core/");
		username = "danyfel80";
		publicKey = "8d5272e3-a97a-4b50-a365-70fa7aaa0d5a";
		privateKey = "3a3dbe30-788f-46bd-ab4d-5a71ce4bf9b4";
	}

	@Test
	public void givenWrongHostWhenConstructionThenCytomineClientException() throws MalformedURLException {
		URL fakeURL = new URL("http://notARealHost/");
		thrown.expect(CytomineClientException.class);
		thrown.expectMessage("Could not connect to server: ");
		client = CytomineClient.create(fakeURL, "something-wrong", "invalid-keys");
	}

	@Test
	public void givenWrongHostAndKeysWhenConstructionThenCytomineClientException() {
		thrown.expect(CytomineClientException.class);
		thrown.expectMessage("User credentials not recognized for public key: ");
		client = CytomineClient.create(host, "something-wrong", "invalid-keys");
	}
	
	@Test
	public void givenCorrectHostAndKeysWhenConstructionThenCytomineClient() {
		client = CytomineClient.create(host, publicKey, privateKey);
		assertThat(client.getCurrentUser().getName().get(), is(username));
	}
}
