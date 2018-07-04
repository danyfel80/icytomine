package plugins.danyfel80.cytomine;

import java.net.MalformedURLException;
import java.net.URL;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;

import icy.plugin.abstract_.Plugin;
import plugins.adufour.blocks.lang.Block;
import plugins.adufour.blocks.util.VarList;
import plugins.adufour.vars.lang.VarString;
import vars.cytomine.VarCytomineClient;

public class CreateCytomineClient extends Plugin implements Block {

	VarString hostVar;
	VarString publicKeyVar;
	VarString privateKeyVar;

	VarCytomineClient clientVar;

	@Override
	public void declareInput(VarList inputMap) {
		hostVar = new VarString("Host URL", "");
		publicKeyVar = new VarString("Public key", "");
		privateKeyVar = new VarString("Private key", "");

		inputMap.add(hostVar.getName(), hostVar);
		inputMap.add(publicKeyVar.getName(), publicKeyVar);
		inputMap.add(privateKeyVar.getName(), privateKeyVar);
	}

	@Override
	public void declareOutput(VarList outputMap) {
		clientVar = VarCytomineClient.ofNullable(null);
		outputMap.add(clientVar.getName(), clientVar);
	}

	@Override
	public void run() {
		URL host;
		try {
			host = new URL(hostVar.getValue());
		} catch (MalformedURLException e) {
			throw new RuntimeException("Invalid URL", e);
		}
		String publicKey = publicKeyVar.getValue();
		String privateKey = privateKeyVar.getValue();
		
		CytomineClient client = CytomineClient.create(host, publicKey, privateKey);
		clientVar.setValue(client);
	}

}
