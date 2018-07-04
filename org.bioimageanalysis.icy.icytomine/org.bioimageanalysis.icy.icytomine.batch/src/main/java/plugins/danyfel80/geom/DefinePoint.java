package plugins.danyfel80.geom;

import java.awt.Point;

import icy.plugin.abstract_.Plugin;
import plugins.adufour.blocks.lang.Block;
import plugins.adufour.blocks.util.VarList;
import plugins.adufour.vars.lang.VarInteger;
import vars.geom.VarPoint;

public class DefinePoint extends Plugin implements Block {

	VarInteger varX;
	VarInteger varY;

	VarPoint varPoint;

	@Override
	public void declareInput(VarList inputMap) {
		varX = new VarInteger("X", 0);
		varY = new VarInteger("Y", 0);
		inputMap.add(varX.getName(), varX);
		inputMap.add(varY.getName(), varY);
	}

	@Override
	public void declareOutput(VarList outputMap) {
		varPoint = new VarPoint("Point");
		outputMap.add(varPoint.getName(), varPoint);
	}

	@Override
	public void run() {
		varPoint.setValue(new Point(varX.getValue(true), varY.getValue(true)));
	}

}
