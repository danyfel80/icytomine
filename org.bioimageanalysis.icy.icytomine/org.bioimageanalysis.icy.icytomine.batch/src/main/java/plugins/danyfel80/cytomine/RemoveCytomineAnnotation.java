package plugins.danyfel80.cytomine;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;

import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginLibrary;
import plugins.adufour.blocks.lang.Block;
import plugins.adufour.blocks.util.VarList;
import plugins.adufour.vars.lang.VarLong;
import vars.cytomine.VarCytomineClient;

public class RemoveCytomineAnnotation extends Plugin implements PluginLibrary, Block {

	VarCytomineClient clientVar;
	VarLong annotationIdVar;

	@Override
	public void declareInput(VarList inputMap) {
		clientVar = VarCytomineClient.ofNullable(null);
		annotationIdVar = new VarLong("Annotation id", 0L);
		inputMap.add(clientVar.getName(), clientVar);
		inputMap.add(annotationIdVar.getName(), annotationIdVar);
	}

	@Override
	public void declareOutput(VarList outputMap) {}

	@Override
	public void run() {
		CytomineClient client = clientVar.getValue(true);
		long annotationId = annotationIdVar.getValue(true);
		try {
			client.removeAnnotation(annotationId);
		} catch (CytomineClientException e) {
			throw new RuntimeException(e);
		}
	}

}
