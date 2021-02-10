package plugins.danyfel80.cytomine.batch;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.core.view.converters.MagnitudeResolutionConverter;

import plugins.adufour.blocks.lang.Loop;
import plugins.adufour.blocks.util.VarList;
import plugins.adufour.vars.lang.Var;
import plugins.adufour.vars.lang.VarInteger;
import vars.cytomine.VarCytomineAnnotation;
import vars.geom.VarDimension;
import vars.geom.VarPoint;

public class CytomineAnnotationTiler extends Loop {

	// Input variables
	private VarList inputMap;
	private VarCytomineAnnotation annotationVar;
	private VarDimension tileSizeVar;
	private VarInteger resolutionLevelVar;

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
		annotationVar = VarCytomineAnnotation.ofNullable(null);
		tileSizeVar = new VarDimension("Tile size");
		resolutionLevelVar = new VarInteger("Resolution level", 0);
	}

	private void addInputVariables() {
		inputMap.add(annotationVar.getName(), annotationVar);
		inputMap.add(tileSizeVar.getName(), tileSizeVar);
		inputMap.add(resolutionLevelVar.getName(), resolutionLevelVar);
	}

	//Iteration output variables
	private VarPoint currentTilePositionVar;

	@Override
	public void declareOutput(VarList outputMap) {
		super.declareOutput(outputMap);
		for (Var<?> var: inputMap) {
			outputMap.add(var.getName(), var);
		}
		currentTilePositionVar = new VarPoint("Current tile position");
		currentTilePositionVar.setEnabled(false);
		outputMap.add(currentTilePositionVar.getName(), currentTilePositionVar);
	}

	@Override
	public void declareLoopVariables(List<Var<?>> loopVariables) {
		for (Var<?> var: inputMap) {
			loopVariables.add(var);
		}
		loopVariables.add(currentTilePositionVar);
	}

	@Override
	public void initializeLoop() {
		retrieveParameters();
		computePositionsToLoad();
		initializeIterators();
	}

	private Annotation annotation;
	private Dimension tileSize;
	private Integer resolutionLevel;

	private void retrieveParameters() {
		annotation = annotationVar.getValue(true);
		tileSize = tileSizeVar.getValue(true);
		resolutionLevel = resolutionLevelVar.getValue(true);
	}

	private Dimension tileGridSize;
	private int numGridTiles;
	private Dimension2D tileSizeAtZeroResolution;
	private Rectangle2D annotationBounds;

	private void computePositionsToLoad() {
		annotationBounds = annotation.getYAdjustedBounds();
		tileSizeAtZeroResolution = MagnitudeResolutionConverter.convertDimension2D(tileSize, resolutionLevel, 0d);
		int tileGridWidth = (int) Math.ceil(annotationBounds.getWidth() / tileSizeAtZeroResolution.getWidth());
		int tileGridHeight = (int) Math.ceil(annotationBounds.getHeight() / tileSizeAtZeroResolution.getHeight());
		tileGridSize = new Dimension(tileGridWidth, tileGridHeight);
		numGridTiles = tileGridHeight * tileGridWidth;
	}

	private int currentTileIndex;
	Shape annotationShape;

	private void initializeIterators() {
		currentTileIndex = -1;
		annotationShape = annotation.getShapeAtZeroResolution();
		do {
			currentTileIndex++;
		} while (currentTileIndex < numGridTiles && !isCurrentTileInAnnotation());
	}

	private Rectangle2D currentTileAtZeroResolution;

	private boolean isCurrentTileInAnnotation() {
		computeCurrentInternalTileAtZeroResolution();
		return annotationShape.intersects(currentTileAtZeroResolution);
	}

	private void computeCurrentInternalTileAtZeroResolution() {
		int currentTileIndexY = currentTileIndex / tileGridSize.width;
		int currentTileIndexX = currentTileIndex % tileGridSize.width;
		Point2D.Double currentTilePositionAtZeroResolution = new Point2D.Double(
				annotationBounds.getMinX() + currentTileIndexX * tileSizeAtZeroResolution.getWidth(),
				annotationBounds.getMinY() + currentTileIndexY * tileSizeAtZeroResolution.getHeight());
		currentTileAtZeroResolution = new Rectangle2D.Double(currentTilePositionAtZeroResolution.x,
				currentTilePositionAtZeroResolution.y, tileSizeAtZeroResolution.getWidth(),
				tileSizeAtZeroResolution.getHeight());
	}

	@Override
	public void beforeIteration() {
		computeCurrentTilePosition();
	}

	private void computeCurrentTilePosition() {
		int currentTileIndexY = currentTileIndex / tileGridSize.width;
		int currentTileIndexX = currentTileIndex % tileGridSize.width;
		Point currentTilePositionAtZeroResolution = new Point(
				(int) (annotationBounds.getMinX() + currentTileIndexX * tileSizeAtZeroResolution.getWidth()),
				(int) (annotationBounds.getMinY() + currentTileIndexY * tileSizeAtZeroResolution.getHeight()));
		currentTilePositionVar.setValue(currentTilePositionAtZeroResolution);
	}

	@Override
	public void afterIteration() {
		do {
			currentTileIndex++;
		} while (currentTileIndex < numGridTiles && !isCurrentTileInAnnotation());
		super.afterIteration();
	}

	@Override
	public boolean isStopConditionReached() {
		return !(currentTileIndex < numGridTiles);
	}
}
