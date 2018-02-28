package vars.geom;

import java.awt.Dimension;

import plugins.adufour.vars.lang.Var;
import plugins.adufour.vars.util.VarListener;

public class VarDimension extends Var<Dimension> {
	public VarDimension(String name, Dimension defaultValue, VarListener<Dimension> defaultListener) {
		super(name, Dimension.class, defaultValue, defaultListener);
	}

	public VarDimension(String name, Dimension defaultValue) {
		super(name, defaultValue);
	}

	public VarDimension(String name) {
		super(name, Dimension.class, null, null);
	}
}
