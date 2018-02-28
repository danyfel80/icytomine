package vars;

import org.bioimageanalysis.icy.icytomine.core.model.Image;

import be.cytomine.client.Cytomine;
import be.cytomine.client.CytomineException;
import plugins.adufour.vars.lang.Var;
import plugins.adufour.vars.util.VarListener;

public class VarCytomineImage extends Var<Image> {

	private Cytomine client;

	public VarCytomineImage(String name, Image defaultValue, VarListener<Image> defaultListener) {
		super(name, Image.class, defaultValue, defaultListener);
	}

	public VarCytomineImage(String name, Image defaultValue) {
		super(name, defaultValue);
	}

	public VarCytomineImage(String name) {
		super(name, Image.class, null, null);
	}

	public void setClient(Cytomine client) {
		this.client = client;
	}

	@Override
	public Image parse(String text) {
		if (client == null)
			throw new IllegalArgumentException("Null client");
		try {
			return new Image(client.getImageInstance(Long.parseLong(text)), client);
		} catch (NumberFormatException | CytomineException e) {
			throw new RuntimeException(e);
		}
	}

}
