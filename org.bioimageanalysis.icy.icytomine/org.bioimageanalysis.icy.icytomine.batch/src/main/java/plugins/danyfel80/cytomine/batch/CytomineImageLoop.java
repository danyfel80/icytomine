package plugins.danyfel80.cytomine.batch;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.bioimageanalysis.icy.icytomine.core.image.annotation.AnnotationInserter;
import org.bioimageanalysis.icy.icytomine.core.image.importer.TiledImageImporter;
import org.bioimageanalysis.icy.icytomine.core.image.tile.FixedTileCalculator;
import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.view.converters.MagnitudeResolutionConverter;

import icy.sequence.Sequence;
import plugins.adufour.blocks.lang.Loop;
import plugins.adufour.blocks.util.VarList;
import plugins.adufour.vars.lang.Var;
import plugins.adufour.vars.lang.VarBoolean;
import plugins.adufour.vars.lang.VarInteger;
import plugins.adufour.vars.lang.VarSequence;
import vars.cytomine.VarCytomineImage;
import vars.geom.VarDimension;
import vars.geom.VarRectangle;

public class CytomineImageLoop extends Loop {

	// input variables
	private VarList inputMap;
	private VarCytomineImage imageVar;
	private VarInteger resolutionLevelVar;
	private VarRectangle loadedAreaVar;
	private VarDimension tileSizeVar;
	private VarDimension tileMarginVar;
	private VarBoolean loadAnnotationsVar;

	private Image targetImageInstance;
	private Rectangle targetArea;
	private int targetResolution;
	private Dimension tileDimensionAtZeroResolution;
	private Dimension tileMarginAtZeroResolution;
	private Rectangle tileGridBounds;
	private int numberOfTiles;
	private boolean loadAnnotations;

	// iteration output variables
	private VarSequence currentTileSequenceVar;

	private int currentTileIndex;
	private Rectangle2D currentTileArea;
	private List<Annotation> currentTileAnnotations;
	private Sequence currentTileSequence;

	// output variables

	@Override
	public void declareInput(VarList inputMap) {
		super.declareInput(inputMap);
		setInputMap(inputMap);
		initializeInputVariables();
		addInputVariables();
	}

	private void setInputMap(VarList list) {
		this.inputMap = list;
	}

	private void initializeInputVariables() {
		imageVar = VarCytomineImage.ofNullable(null);
		resolutionLevelVar = new VarInteger("resolutionLevel", 0);
		loadedAreaVar = new VarRectangle("Loaded area");
		tileSizeVar = new VarDimension("Tile size");
		tileMarginVar = new VarDimension("Tile margin");
		loadAnnotationsVar = new VarBoolean("Load annotations", false);
	}

	private void addInputVariables() {
		inputMap.add(imageVar.getName(), imageVar);
		inputMap.add(resolutionLevelVar.getName(), resolutionLevelVar);
		inputMap.add(loadedAreaVar.getName(), loadedAreaVar);
		inputMap.add(tileSizeVar.getName(), tileSizeVar);
		inputMap.add(tileMarginVar.getName(), tileMarginVar);
		inputMap.add(loadAnnotationsVar.getName(), loadAnnotationsVar);
	}

	@Override
	public void declareOutput(VarList outputMap) {
		super.declareOutput(outputMap);
		for (Var<?> var: inputMap) {
			outputMap.add(var.getName(), var);
		}
		currentTileSequenceVar = new VarSequence("Current sequence", null);
		currentTileSequenceVar.setEnabled(false);
		outputMap.add(currentTileSequenceVar.getName(), currentTileSequenceVar);
	}

	@Override
	public void declareLoopVariables(List<Var<?>> loopVariables) {
		for (Var<?> var: inputMap) {
			loopVariables.add(var);
		}
		loopVariables.add(currentTileSequenceVar);
	}

	@Override
	public void initializeLoop() {
		computeTilesToLoad();
		currentTileIndex = 0;
	}

	private void computeTilesToLoad() {
		targetImageInstance = imageVar.getValue(true);
		Dimension imageSize = targetImageInstance.getSize().get();
		targetResolution = resolutionLevelVar.getValue(true);
		targetArea = Optional.ofNullable(loadedAreaVar.getValue()).orElse(new Rectangle(imageSize))
				.intersection(new Rectangle(imageSize));
		Dimension tileDimension = tileSizeVar.getValue();
		if (tileDimension != null) {
			Dimension2D tileDimension2DAtZeroResolution = MagnitudeResolutionConverter.convertDimension2D(tileDimension,
					targetResolution, 0d);
			tileDimensionAtZeroResolution = new Dimension((int) Math.ceil(tileDimension2DAtZeroResolution.getWidth()),
					(int) Math.ceil(tileDimension2DAtZeroResolution.getHeight()));
		} else {
			tileDimensionAtZeroResolution = new Dimension(targetImageInstance.getSizeX().get(),
					targetImageInstance.getSizeY().get());
		}

		Dimension tileMargin = tileMarginVar.getValue();
		if (tileMargin != null) {
			Dimension2D tileMargin2DAtZeroResolution = MagnitudeResolutionConverter.convertDimension2D(tileMargin,
					targetResolution, 0d);
			tileMarginAtZeroResolution = new Dimension((int) Math.ceil(tileMargin2DAtZeroResolution.getWidth()),
					(int) Math.ceil(tileMargin2DAtZeroResolution.getHeight()));
		} else {
			tileMarginAtZeroResolution = new Dimension(0, 0);
		}

		FixedTileCalculator calculator = new FixedTileCalculator(targetArea, new Point(targetArea.x, targetArea.y),
				tileDimensionAtZeroResolution);
		tileGridBounds = calculator.getTileGridBounds();
		numberOfTiles = tileGridBounds.width * tileGridBounds.height;
		loadAnnotations = loadAnnotationsVar.getValue(true);
	}

	@Override
	public void beforeIteration() {
		importCurrentTile();
		currentTileSequenceVar.setValue(currentTileSequence);
	}

	private void importCurrentTile() {
		computeCurrentTileArea();
		BufferedImage tileImage = importTileArea(currentTileArea);
		setCurrentTileSequence(tileImage);
		setCurrentTileSequenceLocation(currentTileArea);
		if (loadAnnotations) {
			Double adjustedCurrentTileArea = new Rectangle2D.Double(currentTileArea.getX(),
					targetImageInstance.getSizeY().get() - currentTileArea.getMaxY(), currentTileArea.getWidth(),
					currentTileArea.getHeight());
			currentTileAnnotations = targetImageInstance.getAnnotationsWithGeometryOf(adjustedCurrentTileArea);
			insertCurrentTileAnnotationsToCurrentTileSequence();
		}
	}

	private void computeCurrentTileArea() {
		double xPosition = targetArea.getMinX()
				+ (currentTileIndex % tileGridBounds.width) * tileDimensionAtZeroResolution.getWidth()
				- tileMarginAtZeroResolution.getWidth();
		double yPosition = targetArea.getMinY()
				+ (currentTileIndex / tileGridBounds.width) * tileDimensionAtZeroResolution.getHeight()
				- tileMarginAtZeroResolution.getHeight();
		currentTileArea = new Rectangle2D.Double(xPosition, yPosition,
				tileDimensionAtZeroResolution.getWidth() + 2d * tileMarginAtZeroResolution.getWidth(),
				tileDimensionAtZeroResolution.getHeight() + 2d * tileMarginAtZeroResolution.getHeight())
						.createIntersection(targetArea).createIntersection(new Rectangle(targetImageInstance.getSize().get()));
	}

	private BufferedImage importTileArea(Rectangle2D tileArea) {
		TiledImageImporter importer = new TiledImageImporter(targetImageInstance);
		Future<BufferedImage> futureTileImage = importer.requestImage(targetResolution, tileArea);
		try {
			return futureTileImage.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	private void setCurrentTileSequence(BufferedImage tileImage) {
		currentTileSequence = new Sequence(tileImage);
		currentTileSequence.setName(targetImageInstance.getName().orElse("Imported image") + " Tile " + currentTileIndex);
	}

	private void setCurrentTileSequenceLocation(Rectangle2D tileArea) {
		double pixelSize = targetImageInstance.getResolution().orElse(1d);
		double pixelSizeAtTargetResolution = getPixelSizeAtTargetResolution();
		currentTileSequence.setPositionX(tileArea.getX() * pixelSize);
		currentTileSequence.setPositionY(tileArea.getY() * pixelSize);
		currentTileSequence.setPixelSizeX(pixelSizeAtTargetResolution);
		currentTileSequence.setPixelSizeY(pixelSizeAtTargetResolution);
	}

	private double getPixelSizeAtTargetResolution() {
		double pixelSize = targetImageInstance.getResolution().orElse(1d);
		double scaleFactor = Math.pow(2, targetResolution);
		return pixelSize * scaleFactor;
	}

	private void insertCurrentTileAnnotationsToCurrentTileSequence() {
		AnnotationInserter inserter = new AnnotationInserter(currentTileSequence);
		inserter.insertAnnotations(currentTileArea, targetResolution, new HashSet<>(currentTileAnnotations));
	}

	@Override
	public void afterIteration() {
		currentTileIndex++;
		super.afterIteration();
	}

	@Override
	public boolean isStopConditionReached() {
		return !(currentTileIndex < numberOfTiles);
	}
}
