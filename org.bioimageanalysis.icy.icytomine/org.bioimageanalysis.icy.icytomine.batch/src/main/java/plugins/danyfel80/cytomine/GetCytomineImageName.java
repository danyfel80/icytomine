package plugins.danyfel80.cytomine;

import icy.plugin.abstract_.Plugin;
import plugins.adufour.blocks.lang.Block;
import plugins.adufour.blocks.util.VarList;
import plugins.adufour.vars.lang.VarString;
import vars.cytomine.VarCytomineImage;

public class GetCytomineImageName extends Plugin implements Block {
	VarCytomineImage imageVar;

	VarString imageNameVar;

	@Override
	public void declareInput(VarList inputMap) {
		imageVar = VarCytomineImage.ofNullable(null);
		inputMap.add(imageVar.getName(), imageVar);
	}

	@Override
	public void declareOutput(VarList outputMap) {
		imageNameVar = new VarString("Image name", "");
		outputMap.add(imageNameVar.getName(), imageNameVar);
	}

	@Override
	public void run() {
		imageNameVar.setValue(imageVar.getValue().getName().orElse(""));
	}
}
