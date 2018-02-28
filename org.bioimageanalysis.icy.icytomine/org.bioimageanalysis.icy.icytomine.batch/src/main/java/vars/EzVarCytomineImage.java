package vars;

import org.bioimageanalysis.icy.icytomine.core.model.Image;

import be.cytomine.client.Cytomine;
import plugins.adufour.ezplug.EzVar;

public class EzVarCytomineImage extends EzVar<Image> {

	public EzVarCytomineImage(String name, Image[] defaultValues, int defaultValueIndex, boolean freeInput) {
		super(new VarCytomineImage(name), defaultValues, defaultValueIndex, freeInput);
	}

	public void setClient(Cytomine client) {
		((VarCytomineImage) getVariable()).setClient(client);
	}

}
