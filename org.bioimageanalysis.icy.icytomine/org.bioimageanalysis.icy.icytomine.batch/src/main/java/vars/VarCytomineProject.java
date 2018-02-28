package vars;

import org.bioimageanalysis.icy.icytomine.core.model.Project;

import be.cytomine.client.Cytomine;
import be.cytomine.client.CytomineException;
import plugins.adufour.vars.lang.Var;
import plugins.adufour.vars.util.VarListener;

public class VarCytomineProject extends Var<Project> {

	private Cytomine client;

	public VarCytomineProject(String name, Cytomine client, Project defaultValue, VarListener<Project> defaultListener) {
		super(name, Project.class, defaultValue, defaultListener);
		this.client = client;
	}

	public VarCytomineProject(String name, Cytomine client, Project defaultValue) {
		super(name, defaultValue);
		this.client = client;
	}

	public VarCytomineProject(String name, Cytomine client) {
		super(name, Project.class, null, null);
		this.client = client;
	}

	/**
	 * Uses the id of the project to retrieve the instance on the server.
	 * 
	 * @param text
	 *          The id of the project.
	 * @see plugins.adufour.vars.lang.Var#parse(java.lang.String)
	 */
	@Override
	public Project parse(String text) {
		try {
			return new Project(client.getProject(Long.parseLong(text)), client);
		} catch (NumberFormatException | CytomineException e) {
			throw new RuntimeException(e);
		}
	}

}
