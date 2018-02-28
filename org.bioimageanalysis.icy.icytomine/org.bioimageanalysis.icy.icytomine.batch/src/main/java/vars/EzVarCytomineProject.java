package vars;

import org.bioimageanalysis.icy.icytomine.core.model.Project;

import be.cytomine.client.Cytomine;
import plugins.adufour.ezplug.EzVar;

public class EzVarCytomineProject extends EzVar<Project> {

	public EzVarCytomineProject(String name, Cytomine client, Project[] defaultValues, int defaultValueIndex,
			boolean freeInput) {
		super(new VarCytomineProject(name, client), defaultValues, defaultValueIndex, freeInput);
	}

}
