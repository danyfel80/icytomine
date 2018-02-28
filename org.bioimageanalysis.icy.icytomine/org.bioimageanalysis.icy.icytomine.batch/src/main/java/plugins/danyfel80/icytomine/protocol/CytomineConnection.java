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
package plugins.danyfel80.icytomine.protocol;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import org.bioimageanalysis.icy.icytomine.core.connection.CytomineConnector;
import org.bioimageanalysis.icy.icytomine.core.connection.user.Preferences;
import org.bioimageanalysis.icy.icytomine.core.connection.user.UserKeys;

import be.cytomine.client.Cytomine;
import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginLibrary;
import icy.system.IcyHandledException;
import plugins.adufour.blocks.lang.Block;
import plugins.adufour.blocks.util.VarList;
import plugins.adufour.vars.lang.Var;
import plugins.adufour.vars.lang.VarString;
import plugins.adufour.vars.util.VarListener;
import vars.EzVarCytomineHost;
import vars.EzVarCytomineUser;

/**
 * @author Daniel Felipe Gonzalez Obando
 *
 */
public class CytomineConnection extends Plugin implements PluginLibrary, Block {

	private EzVarCytomineHost hostVar;
	private EzVarCytomineUser userNameVar;
	private VarString publicKeyVar;
	private VarString privateKeyVar;

	private Var<Cytomine> client;

	@Override
	public void declareInput(VarList inputMap) {
		try {
			Preferences.loadOrDefault();
		} catch (IOException e) {
			throw new IcyHandledException(e);
		}

		String[] hosts = Preferences.getInstance().getAvailableCytomineCredentials().keySet().stream()
				.toArray(String[]::new);
		hostVar = new EzVarCytomineHost("Host URL", hosts, 0, false);
		addHostInput(hostVar, inputMap);
		if (hosts.length > 0)
			hostVar.getVariable().valueChanged(hostVar.getVariable(), null, hosts[0]);
	}

	private void addHostInput(EzVarCytomineHost hostVar, VarList inputMap) {
		inputMap.add(hostVar.name, hostVar.getVariable());
		hostVar.getVariable().addListener(new VarListener<String>() {

			@Override
			public void valueChanged(Var<String> source, String oldValue, String newValue) {
				try {
					inputMap.remove(userNameVar.getVariable());
				} catch (Exception e) {
					/* Nothing to do here */}

				if (newValue != null) {
					String[] users = Preferences.getInstance().getAvailableCytomineCredentials().get(newValue).keySet().stream()
							.toArray(String[]::new);
					userNameVar = new EzVarCytomineUser("User", users, 0, false);
					addUserInput(userNameVar, inputMap);
					if (users.length > 0)
						userNameVar.getVariable().valueChanged(userNameVar.getVariable(), null, users[0]);
				}
			}

			@Override
			public void referenceChanged(Var<String> source, Var<? extends String> oldReference,
					Var<? extends String> newReference) {
				/* Nothing to do here */}
		});
	}

	private void addUserInput(EzVarCytomineUser userNameVar, VarList inputMap) {
		inputMap.add(userNameVar.name, userNameVar.getVariable());
		userNameVar.getVariable().addListener(new VarListener<String>() {

			@Override
			public void valueChanged(Var<String> source, String oldValue, String newValue) {
				try {
					inputMap.remove(publicKeyVar);
				} catch (Exception e) {
					/* Nothing to do here */}
				try {
					inputMap.remove(privateKeyVar);
				} catch (Exception e) {
					/* Nothing to do here */}

				if (newValue != null) {
					UserKeys keys = Preferences.getInstance().getAvailableCytomineCredentials().get(hostVar.getValue())
							.get(newValue);
					publicKeyVar = new VarString("Public key", keys.getPublicKey() != null ? keys.getPublicKey() : "");
					privateKeyVar = new VarString("Private key", keys.getPrivateKey() != null ? keys.getPrivateKey() : "");

					inputMap.add(publicKeyVar.getName(), publicKeyVar);
					inputMap.add(privateKeyVar.getName(), privateKeyVar);
				}
			}

			@Override
			public void referenceChanged(Var<String> source, Var<? extends String> oldReference,
					Var<? extends String> newReference) {
				/* Nothing to do here */
			}
		});
	}

	@Override
	public void declareOutput(VarList outputMap) {
		client = new Var<Cytomine>("Cytomine client", Cytomine.class, null, null);
		outputMap.add(client.getName(), client);
	}

	@Override
	public void run() {
		URL url;
		String user = userNameVar.getValue(true);
		String pblKey = publicKeyVar.getValue(true);
		String prvKey = privateKeyVar.getValue(true);

		try {
			url = new URL(hostVar.getValue(true));
		} catch (MalformedURLException e) {
			throw new IcyHandledException(e);
		}

		CytomineConnector.addUserIfAbsent(url, user, pblKey, prvKey);

		try {
			client.setValue(CytomineConnector.login(url, user).get());
		} catch (InterruptedException | ExecutionException | RuntimeException e) {
			throw new IcyHandledException(e);
		}
	}

}
