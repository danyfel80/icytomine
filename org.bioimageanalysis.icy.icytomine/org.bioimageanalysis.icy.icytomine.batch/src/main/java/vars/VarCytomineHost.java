package vars;

import plugins.adufour.vars.lang.Var;
import plugins.adufour.vars.util.VarListener;

public class VarCytomineHost extends Var<String> {

	public VarCytomineHost(String name, String defaultValue, VarListener<String> defaultListener) {
		super(name, String.class, defaultValue, defaultListener);
	}

	public VarCytomineHost(String name, String defaultValue) {
		super(name, defaultValue);
	}

	public VarCytomineHost(String name) {
		super(name, String.class, null, null);
	}

	@Override
	public String parse(String text) {
		return this.getValue();
	}

}
