package plugins.danyfel80.cytomine;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.model.Project;

import icy.plugin.abstract_.Plugin;
import plugins.adufour.blocks.lang.Block;
import plugins.adufour.blocks.util.VarList;
import plugins.adufour.vars.lang.VarLong;
import vars.cytomine.VarCytomineClient;
import vars.cytomine.VarCytomineProject;

public class GetCytomineProject extends Plugin implements Block {

	VarCytomineClient clientVar;
	VarLong projectIdVar;

	VarCytomineProject projectVar;
	
	@Override
	public void declareInput(VarList inputMap) {
		clientVar = VarCytomineClient.ofNullable(null);
		projectIdVar = new VarLong("Project id", 0L);
		inputMap.add(clientVar.getName(), clientVar);
		inputMap.add(projectIdVar.getName(), projectIdVar);
	}

	@Override
	public void declareOutput(VarList outputMap) {
		projectVar = VarCytomineProject.ofNullable(null);
		outputMap.add(projectVar.getName(), projectVar);
	}

	@Override
	public void run() {
		long projectId = projectIdVar.getValue(true);
		CytomineClient client = clientVar.getValue(true);
		Project project = client.getProject(projectId);
		projectVar.setValue(project);
	}

}
