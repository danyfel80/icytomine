package vars.cytomine;

import org.bioimageanalysis.icy.icytomine.core.model.Project;

import plugins.adufour.vars.lang.Var;

public class VarCytomineProject extends Var<Project> {

	public static VarCytomineProject of(Project project) throws IllegalArgumentException {
		if (project == null)
			throw new IllegalArgumentException("Cannot create a project variable from null project");
		return new VarCytomineProject("Project", project);
	}

	public static VarCytomineProject ofNullable(Project project) {
		return new VarCytomineProject("Project", project);
	}

	private VarCytomineProject(String name, Project defaultValue) {
		super(name, Project.class, defaultValue, null);
	}

}
