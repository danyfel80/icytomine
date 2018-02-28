package plugins.danyfel80.icytomine.protocol;

import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.model.Project;

import be.cytomine.client.Cytomine;
import be.cytomine.client.CytomineException;
import be.cytomine.client.collections.ImageInstanceCollection;
import be.cytomine.client.collections.ProjectCollection;
import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginLibrary;
import plugins.adufour.blocks.lang.Block;
import plugins.adufour.blocks.util.VarList;
import plugins.adufour.vars.lang.Var;
import plugins.adufour.vars.util.VarListener;
import vars.EzVarCytomineImage;
import vars.EzVarCytomineProject;

public class CytomineResourceSelector extends Plugin implements PluginLibrary, Block {

	Var<Cytomine> clientVar;
	EzVarCytomineProject projectVar;
	EzVarCytomineImage imageVar;

	@Override
	public void declareInput(VarList inputMap) {
		clientVar = new Var<>("Cytomine client", Cytomine.class);
		addClientVar(clientVar, inputMap);
		clientVar.valueChanged(clientVar, null, null);
	}

	private void addClientVar(Var<Cytomine> clientVar, VarList inputMap) {
		inputMap.add(clientVar.getName(), clientVar);
		clientVar.addListener(new VarListener<Cytomine>() {

			@Override
			public void valueChanged(Var<Cytomine> source, Cytomine oldClient, Cytomine newClient) {
				try {
					inputMap.remove(projectVar.getVariable());
				} catch (Exception e) { /* Nothing to do */
				}
				if (newClient != null) {
					try {
						ProjectCollection nativeProjects = newClient.getProjects();
						Project[] projects = new Project[nativeProjects.size()];
						for (int i = 0; i < nativeProjects.size(); i++) {
							projects[i] = new Project(nativeProjects.get(i), newClient);
						}
						projectVar = new EzVarCytomineProject("Project", newClient, projects, 0, false);
						addProjectVar(projectVar, inputMap);
						if (projects.length > 0) {
							projectVar.getVariable().valueChanged(projectVar.getVariable(), null, projects[0]);
						} else {
							projectVar.getVariable().valueChanged(projectVar.getVariable(), null, null);
						}
					} catch (CytomineException e) {
						throw new RuntimeException(e);
					}
				}
			}

			@Override
			public void referenceChanged(Var<Cytomine> source, Var<? extends Cytomine> oldReference,
					Var<? extends Cytomine> newReference) { /* Nothing to do here */
			}
		});
	}

	private void addProjectVar(EzVarCytomineProject projectVar, VarList inputMap) {
		inputMap.add(projectVar.name, projectVar.getVariable());
		projectVar.getVariable().addListener(new VarListener<Project>() {

			@Override
			public void valueChanged(Var<Project> source, Project oldProject, Project newProject) {
				try {
					inputMap.remove(imageVar.getVariable());
				} catch (Exception e) { /* Nothing to do */
				}
				if (newProject != null) {
					try {
						ImageInstanceCollection nativeImages = newProject.getClient().getImageInstances(newProject.getId());
						Image[] images = new Image[nativeImages.size()];
						for (int i = 0; i < nativeImages.size(); i++) {
							images[i] = new Image(nativeImages.get(i), newProject.getClient());
						}
						imageVar = new EzVarCytomineImage("Image", images, 0, false);
						imageVar.setClient(newProject.getClient());
						inputMap.add(imageVar.name, imageVar.getVariable());
					} catch (CytomineException e) {
						throw new RuntimeException(e);
					}
				}
			}

			@Override
			public void referenceChanged(Var<Project> source, Var<? extends Project> oldReference,
					Var<? extends Project> newReference) { /* Nothing to do here */
			}
		});
	}

	@Override
	public void declareOutput(VarList outputMap) {
		// Nothing to do here
	}

	@Override
	public void run() {
		// Nothing to do here
	}

}
