package org.bioimageanalysis.icy.icytomine.core.user;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.net.MalformedURLException;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import org.bioimageanalysis.icy.icytomine.core.connection.user.UserKeys;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class UserSpec {
	private static Jsonb jsonb;

	@BeforeClass
	public static void init() {
		jsonb = JsonbBuilder.create();
	}

	private UserKeys cytomineUser;

	@Before
	public void setup() {
		cytomineUser = new UserKeys();
	}

	@Test
	public void testSetAndGetPublicKey() {
		assertThat(cytomineUser.getPublicKey(), is(nullValue()));
		String publicKey = "APublicKey";
		cytomineUser.setPublicKey(publicKey);
		assertThat(cytomineUser.getPublicKey(), is(equalTo(publicKey)));
	}

	@Test
	public void testSetAndGetPrivateKey() {

		assertThat(cytomineUser.getPrivateKey(), is(nullValue()));
		String privateKey = "APrivateKey";
		cytomineUser.setPrivateKey(privateKey);
		assertThat(cytomineUser.getPrivateKey(), is(equalTo(privateKey)));
	}

	@Test
	public void testJsonBinding() throws MalformedURLException {
		String publicKey = "APublicKey";
		cytomineUser.setPublicKey(publicKey);
		String privateKey = "APrivateKey";
		cytomineUser.setPrivateKey(privateKey);

		String jsonText = jsonb.toJson(cytomineUser);
		System.out.println(jsonText);
		UserKeys reconstructedCredentials = jsonb.fromJson(jsonText, UserKeys.class);
		assertThat(reconstructedCredentials, is(equalTo(cytomineUser)));
	}
}
