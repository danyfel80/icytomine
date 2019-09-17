package plugins.danyfel80.cytomine;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;
import org.bioimageanalysis.icy.icytomine.core.model.Annotation;

import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginLibrary;
import plugins.adufour.blocks.lang.Block;
import plugins.adufour.blocks.util.VarList;
import plugins.adufour.ezplug.EzStoppable;
import plugins.adufour.vars.lang.VarBoolean;
import plugins.adufour.vars.lang.VarLong;
import vars.cytomine.VarCytomineAnnotation;
import vars.cytomine.VarCytomineClient;

public class GetCytomineAnnotation extends Plugin implements PluginLibrary, Block, EzStoppable {

	VarCytomineClient clientVar;
	VarLong annotationIdVar;
	VarBoolean retrieveProperties;

	VarCytomineAnnotation annotationVar;

	@Override
	public void declareInput(VarList inputMap) {
		clientVar = VarCytomineClient.ofNullable(null);
		annotationIdVar = new VarLong("Annotation id", 0L);
		retrieveProperties = new VarBoolean("Include properties", false);
		inputMap.add(clientVar.getName(), clientVar);
		inputMap.add(annotationIdVar.getName(), annotationIdVar);
		inputMap.add(retrieveProperties.getName(), retrieveProperties);
	}

	@Override
	public void declareOutput(VarList outputMap) {
		annotationVar = VarCytomineAnnotation.ofNullable(null);
		outputMap.add(annotationVar.getName(), annotationVar);
	}

	@Override
	public void run() {
		CytomineClient client = clientVar.getValue(true);
		long annotationId = annotationIdVar.getValue(true);
		Annotation annotationInstance;
		try {
			annotationInstance = client.getAnnotation(annotationId);
			annotationInstance.getAnnotationProperties(retrieveProperties.getValue());
		} catch (CytomineClientException e) {
			throw new RuntimeException(e);
		}
		annotationVar.setValue(annotationInstance);
	}

	@Override
	public void stopExecution() {}

}
