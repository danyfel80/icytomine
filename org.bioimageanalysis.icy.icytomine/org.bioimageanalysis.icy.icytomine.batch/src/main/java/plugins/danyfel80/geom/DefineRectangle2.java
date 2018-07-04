package plugins.danyfel80.geom;

import java.awt.Rectangle;

import icy.plugin.abstract_.Plugin;
import plugins.adufour.blocks.lang.Block;
import plugins.adufour.blocks.util.VarList;
import plugins.adufour.vars.lang.VarInteger;
import vars.geom.VarRectangle;

public class DefineRectangle2 extends Plugin implements Block {

	VarInteger varX;
	VarInteger varY;
	VarInteger varW;
	VarInteger varH;

	VarRectangle varRectangle;

	@Override
	public void declareInput(VarList inputMap) {
		varX = new VarInteger("X", 0);
		varY = new VarInteger("Y", 0);
		varW = new VarInteger("Width", 0);
		varH = new VarInteger("Height", 0);

		inputMap.add(varX.getName(), varX);
		inputMap.add(varY.getName(), varY);
		inputMap.add(varW.getName(), varH);
		inputMap.add(varH.getName(), varW);
	}

	@Override
	public void declareOutput(VarList outputMap) {
		varRectangle = new VarRectangle("Rectangle");
		outputMap.add(varRectangle.getName(), varRectangle);
	}

	@Override
	public void run() {
		varRectangle
				.setValue(new Rectangle(varX.getValue(true), varY.getValue(true), varW.getValue(true), varH.getValue(true)));
	}

}
