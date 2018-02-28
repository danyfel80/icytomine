package vars;

import plugins.adufour.ezplug.EzVar;

public class EzVarCytomineUser extends EzVar<String> {

	public EzVarCytomineUser(String name, String[] defaultValues, int defaultValueIndex, boolean freeInput) {
		super(new VarCytomineHost(name), defaultValues, defaultValueIndex, freeInput);
	}

}
