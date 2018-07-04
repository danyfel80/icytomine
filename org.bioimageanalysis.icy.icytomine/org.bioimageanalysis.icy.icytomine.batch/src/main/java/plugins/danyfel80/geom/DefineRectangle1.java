package plugins.danyfel80.geom;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Optional;

import icy.plugin.abstract_.Plugin;
import plugins.adufour.blocks.lang.Block;
import plugins.adufour.blocks.util.VarList;
import vars.geom.VarDimension;
import vars.geom.VarPoint;
import vars.geom.VarRectangle;

public class DefineRectangle1 extends Plugin implements Block {

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
		Point position = Optional.ofNullable(varPosition.getValue()).orElse(new Point());
		Dimension dimension = varDimension.getValue(true);
		varRectangle.setValue(new Rectangle(position, dimension));
	}

}
