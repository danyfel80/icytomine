package vars.cytomine;

import java.net.URL;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;

import plugins.adufour.vars.lang.Var;

public class VarCytomineClient extends Var<CytomineClient> {

	public static VarCytomineClient create(URL host, String publicKey, String privateKey) throws CytomineClientException {
		CytomineClient cytomineClient = CytomineClient.create(host, publicKey, privateKey);
		return of(cytomineClient);
	}

	public static VarCytomineClient of(CytomineClient client) throws IllegalArgumentException {
		if (client == null) {
			throw new IllegalArgumentException("Cannot create variable with a null client");
		}

		return new VarCytomineClient("CytomineClient", client);
	}

	public static VarCytomineClient ofNullable(CytomineClient client) {
		return new VarCytomineClient("CytomineClient", client);
	}

	private VarCytomineClient(String name, CytomineClient defaultValue) {
		super(name, CytomineClient.class, defaultValue, null);
	}
}
