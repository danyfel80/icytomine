package vars.cytomine;

import org.bioimageanalysis.icy.icytomine.core.model.Annotation;

import plugins.adufour.vars.lang.Var;

public class VarCytomineAnnotation extends Var<Annotation> {

	public static VarCytomineAnnotation of(Annotation annotation) throws IllegalArgumentException {
		if (annotation == null)
			throw new IllegalArgumentException("Cannot create an annotation variable from null annotation");
		return new VarCytomineAnnotation("Annotation", annotation);
	}

	public static VarCytomineAnnotation ofNullable(Annotation annotation) {
		return new VarCytomineAnnotation("Annotation", annotation);
	}

	private VarCytomineAnnotation(String name, Annotation defaultValue) {
		super(name, Annotation.class, defaultValue, null);
	}

}
