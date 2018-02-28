package vars;

import plugins.adufour.vars.lang.Var;
import plugins.adufour.vars.util.VarListener;

public class VarCytomineUser extends Var<String> {

	public VarCytomineUser(String name, String defaultValue, VarListener<String> defaultListener) {
		super(name, String.class, defaultValue, defaultListener);
	}

	public VarCytomineUser(String name, String defaultValue) {
		super(name, defaultValue);
	}

	public VarCytomineUser(String name) {
		super(name, String.class, null, null);
	}

	@Override
	public String parse(String text) {
		return this.getValue();
	}

}
