package plugins.danyfel80.cytomine;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.model.Image;

import icy.plugin.abstract_.Plugin;
import plugins.adufour.blocks.lang.Block;
import plugins.adufour.blocks.util.VarList;
import plugins.adufour.vars.lang.VarLong;
import vars.cytomine.VarCytomineClient;
import vars.cytomine.VarImage;

public class GetImage extends Plugin implements Block {

	VarCytomineClient clientVar;
	VarLong imageIdVar;
	
	VarImage imageVar;
	
	@Override
	public void declareInput(VarList inputMap) {
		clientVar = VarCytomineClient.ofNullable(null);
		imageIdVar = new VarLong("Image id", 0L);
		
		inputMap.add(clientVar.getName(), clientVar);
		inputMap.add(imageIdVar.getName(), imageIdVar);
	}

	@Override
	public void declareOutput(VarList outputMap) {
		imageVar = VarImage.ofNullable(null);
		
		outputMap.add(imageVar.getName(), imageVar);
	}

	@Override
	public void run() {
		CytomineClient client = clientVar.getValue(true);
		long imageId = imageIdVar.getValue(true);
		Image imageInstance = client.getImageInstance(imageId);
		imageVar.setValue(imageInstance);
	}

}
