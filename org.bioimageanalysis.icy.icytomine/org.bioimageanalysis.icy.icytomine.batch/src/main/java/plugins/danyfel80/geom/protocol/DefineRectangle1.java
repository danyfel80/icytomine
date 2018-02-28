package plugins.danyfel80.geom.protocol;

import java.awt.Rectangle;

import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginLibrary;
import plugins.adufour.blocks.lang.Block;
import plugins.adufour.blocks.util.VarList;
import vars.geom.VarDimension;
import vars.geom.VarPoint;
import vars.geom.VarRectangle;

public class DefineRectangle1 extends Plugin implements PluginLibrary, Block {

	VarPoint varPosition;
	VarDimension varDimension;

	VarRectangle varRectangle;

	@Override
	public void declareInput(VarList inputMap) {
		varPosition = new VarPoint("Position");
		varDimension = new VarDimension("Dimension");
		inputMap.add(varPosition.getName(), varPosition);
		inputMap.add(varDimension.getName(), varDimension);
	}

	@Override
	public void declareOutput(VarList outputMap) {
		varRectangle = new VarRectangle("Rectangle");
		outputMap.add(varRectangle.getName(), varRectangle);
	}

	@Override
	public void run() {
		varRectangle.setValue(new Rectangle(varPosition.getValue(true), varDimension.getValue(true)));
	}

}
