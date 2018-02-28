package vars;

import plugins.adufour.ezplug.EzVar;

public class EzVarCytomineHost extends EzVar<String> {

	public EzVarCytomineHost(String name, String[] defaultValues, int defaultValueIndex, boolean freeInput) {
		super(new VarCytomineHost(name), defaultValues, defaultValueIndex, freeInput);
	}

}
