package plugins.danyfel80.geom.protocol;

import java.awt.Dimension;

import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginLibrary;
import plugins.adufour.blocks.lang.Block;
import plugins.adufour.blocks.util.VarList;
import plugins.adufour.vars.lang.VarInteger;
import vars.geom.VarDimension;

public class DefineDimension extends Plugin implements PluginLibrary, Block {

	VarInteger varW;
	VarInteger varH;

	VarDimension varDimension;

	@Override
	public void declareInput(VarList inputMap) {
		varW = new VarInteger("Widht", 0);
		varH = new VarInteger("Height", 0);

		inputMap.add(varW.getName(), varW);
		inputMap.add(varH.getName(), varH);
	}

	@Override
	public void declareOutput(VarList outputMap) {
		varDimension = new VarDimension("Dimension");
		outputMap.add(varDimension.getName(), varDimension);
	}

	@Override
	public void run() {
		varDimension.setValue(new Dimension(varW.getValue(true), varH.getValue(true)));
	}

}
