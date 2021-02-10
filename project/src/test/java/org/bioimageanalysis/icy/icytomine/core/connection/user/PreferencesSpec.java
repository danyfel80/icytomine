package org.bioimageanalysis.icy.icytomine.core.connection.user;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.bioimageanalysis.icy.icytomine.core.connection.persistence.Preferences;
import org.bioimageanalysis.icy.icytomine.core.connection.persistence.UserCredential;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

public class PreferencesSpec {

	@AfterClass
	public static void tearDown() throws Exception {
		Path path = Preferences.getDefaultPreferencesPath();
		try {
			Files.deleteIfExists(path);
		} catch (DirectoryNotEmptyException x) {
			System.err.format("%s not empty%n", path);
		} catch (IOException x) {
			System.err.println(x);
		}
	}

	Preferences preferences;

	@Before
	public void setup() throws MalformedURLException, URISyntaxException {
		UserCredential user1 = new UserCredential();
		user1.setPublicKey("public 1");
		user1.setPrivateKey("private 1");
		UserCredential user2 = new UserCredential();
		user2.setPublicKey("public 2");
		user2.setPrivateKey("private 2");

		HashMap<String, UserCredential> users = new HashMap<>();
		users.put("User1", user1);
		//users.put("User2", user2);

		Preferences.clear();
		preferences = Preferences.getInstance();
		preferences.getAvailableCytomineCredentials().put("http://cytomine-core/", users);
		preferences.getAvailableCytomineCredentials().put("http://second-cytomine/", users);
	}

	@Test
	public void testPreferenceSave() throws IOException {
		Preferences.save();
		assertThat(Files.exists(Preferences.getDefaultPreferencesPath()), is(true));
	}

	@Test
	public void testPreferenceLoad() throws IOException {
		Map<String, Map<String, UserCredential>> expected = new HashMap<>(preferences.getAvailableCytomineCredentials());
		Preferences.save();
		Preferences.load();
		preferences = Preferences.getInstance();
		assertThat(preferences.getAvailableCytomineCredentials(), is(equalTo(expected)));
	}

}
