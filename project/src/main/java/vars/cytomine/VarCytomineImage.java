package vars.cytomine;

import org.bioimageanalysis.icy.icytomine.core.model.Image;

import plugins.adufour.vars.lang.Var;

public class VarCytomineImage extends Var<Image> {
	public static VarCytomineImage of(Image image) throws IllegalArgumentException {
		if (image == null)
			throw new IllegalArgumentException("Cannot create an image variable from null image");
		return new VarCytomineImage("Image", image);
	}

	public static VarCytomineImage ofNullable(Image image) {
		return new VarCytomineImage("Image", image);
	}

	private VarCytomineImage(String name, Image defaultValue) {
		super(name, Image.class, defaultValue, null);
	}
}
