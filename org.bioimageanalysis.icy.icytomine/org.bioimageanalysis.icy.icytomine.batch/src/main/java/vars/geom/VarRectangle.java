package vars.geom;

import java.awt.Rectangle;

import plugins.adufour.vars.lang.Var;
import plugins.adufour.vars.util.VarListener;

public class VarRectangle extends Var<Rectangle> {
	public VarRectangle(String name, Rectangle defaultValue, VarListener<Rectangle> defaultListener) {
		super(name, Rectangle.class, defaultValue, defaultListener);
	}

	public VarRectangle(String name, Rectangle defaultValue) {
		super(name, defaultValue);
	}

	public VarRectangle(String name) {
		super(name, Rectangle.class, null, null);
	}
}
