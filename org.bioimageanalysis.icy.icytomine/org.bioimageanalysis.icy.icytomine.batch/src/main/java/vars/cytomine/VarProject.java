package vars.cytomine;

import org.bioimageanalysis.icy.icytomine.core.model.Project;

import plugins.adufour.vars.lang.Var;

public class VarProject extends Var<Project> {

	public static VarProject of(Project project) throws IllegalArgumentException {
		if (project == null)
			throw new IllegalArgumentException("Cannot create a project variable from null project");
		return new VarProject("Project", project);
	}

	public static VarProject ofNullable(Project project) {
		return new VarProject("Project", project);
	}

	private VarProject(String name, Project defaultValue) {
		super(name, Project.class, defaultValue, null);
	}

}
