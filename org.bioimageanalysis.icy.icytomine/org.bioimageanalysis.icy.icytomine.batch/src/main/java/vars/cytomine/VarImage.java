package vars.cytomine;

import org.bioimageanalysis.icy.icytomine.core.model.Image;

import plugins.adufour.vars.lang.Var;

public class VarImage extends Var<Image> {
	public static VarImage of(Image image) throws IllegalArgumentException {
		if (image == null)
			throw new IllegalArgumentException("Cannot create an image variable from null image");
		return new VarImage("Image", image);
	}

	public static VarImage ofNullable(Image image) {
		return new VarImage("Image", image);
	}

	private VarImage(String name, Image defaultValue) {
		super(name, Image.class, defaultValue, null);
	}
}
