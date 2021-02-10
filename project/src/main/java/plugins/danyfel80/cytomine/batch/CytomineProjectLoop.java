package plugins.danyfel80.cytomine.batch;

import java.util.Iterator;
import java.util.List;

import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.model.Project;

import plugins.adufour.blocks.lang.Loop;
import plugins.adufour.blocks.util.VarList;
import plugins.adufour.vars.lang.Var;
import vars.cytomine.VarCytomineImage;
import vars.cytomine.VarCytomineProject;

public class CytomineProjectLoop extends Loop {
	// input variables
	private VarList inputMap;
	private VarCytomineProject projectVar;

	Project project;
	List<Image> images;
	Iterator<Image> imageIterator;

	// iteration output variables
	private VarCytomineImage currentImageVar;

	private Image currentImage;

	@Override
	public void declareInput(VarList inputMap) {
		super.declareInput(inputMap);
		setInputMap(inputMap);
		initializeInputVariables();
		addInputVariables();
	}

	private void setInputMap(VarList inputMap) {
		this.inputMap = inputMap;
	}

	private void initializeInputVariables() {
		projectVar = VarCytomineProject.ofNullable(null);
	}

	private void addInputVariables() {
		inputMap.add(projectVar.getName(), projectVar);
	}

	@Override
	public void declareOutput(VarList outputMap) {
		super.declareOutput(outputMap);
		for (Var<?> var : inputMap) {
			outputMap.add(var.getName(), var);
		}
		currentImageVar = VarCytomineImage.ofNullable(null);
		currentImageVar.setEnabled(false);
		outputMap.add(currentImageVar.getName(), currentImageVar);
	}

	@Override
	public void declareLoopVariables(List<Var<?>> loopVariables) {
		for (Var<?> var : inputMap) {
			loopVariables.add(var);
		}
		loopVariables.add(currentImageVar);
	}

	@Override
	public void initializeLoop() {
		computeNumberOfImages();
	}

	private void computeNumberOfImages() {
		project = projectVar.getValue(true);
		images = project.getImages(true);
		imageIterator = images.iterator();
	}

	@Override
	public void beforeIteration() {
		retrieveCurrentImage();
		currentImageVar.setValue(currentImage);
	}

	private void retrieveCurrentImage() {
		currentImage = imageIterator.next();
	}

	@Override
	public void afterIteration() {
		super.afterIteration();
	}

	@Override
	public boolean isStopConditionReached() {
		return !imageIterator.hasNext();
	}
}
