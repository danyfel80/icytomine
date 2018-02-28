package vars.geom;

import java.awt.Point;

import plugins.adufour.vars.lang.Var;
import plugins.adufour.vars.util.VarListener;

public class VarPoint extends Var<Point> {
	public VarPoint(String name, Point defaultValue, VarListener<Point> defaultListener) {
		super(name, Point.class, defaultValue, defaultListener);
	}

	public VarPoint(String name, Point defaultValue) {
		super(name, defaultValue);
	}

	public VarPoint(String name) {
		super(name, Point.class, null, null);
	}
	
}
