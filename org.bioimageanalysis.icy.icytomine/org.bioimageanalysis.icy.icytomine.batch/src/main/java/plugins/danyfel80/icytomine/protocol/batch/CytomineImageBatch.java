/*
 * Copyright 2010-2018 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <http://www.gnu.org/licenses/>.
 */
package plugins.danyfel80.icytomine.protocol.batch;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bioimageanalysis.icy.icytomine.core.IcytomineImporter;
import org.bioimageanalysis.icy.icytomine.core.model.Image;

import com.vividsolutions.jts.io.ParseException;

import be.cytomine.client.CytomineException;
import icy.common.exception.UnsupportedFormatException;
import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.sequence.Sequence;
import icy.system.IcyHandledException;
import plugins.adufour.blocks.lang.Loop;
import plugins.adufour.blocks.util.VarList;
import plugins.adufour.vars.lang.Var;
import plugins.adufour.vars.lang.VarBoolean;
import plugins.adufour.vars.lang.VarEnum;
import plugins.adufour.vars.lang.VarInteger;
import plugins.adufour.vars.lang.VarSequence;
import vars.VarCytomineImage;
import vars.geom.VarDimension;
import vars.geom.VarRectangle;

/**
 * @author Daniel Felipe Gonzalez Obando
 *
 */
public class CytomineImageBatch extends Loop {

	public enum ProcessingType {
		EntireArea, ByTiles;
	}

	private VarList inputMap;

	// input
	private VarCytomineImage varImage;
	private VarInteger varResolutionLevel;
	private VarEnum<ProcessingType> varProcessingType;
	private VarRectangle varLoadedArea;
	private VarDimension varTileDimension;
	private VarBoolean varLoadROIs;

	// iteration variables
	private Image image;
	private int resolutionLevel;
	private ProcessingType processingType;
	private Rectangle loadedArea;
	private Dimension tileSize;
	private boolean loadROIs;

	private IcytomineImporter importer;
	private Rectangle loadedAreaInTargetResolution;
	private Double pixelResolutionInResolution;
	private Dimension tileSizeInResolution0;
	private Dimension tileGridSize;
	private int currentTile;
	private int totalTiles;

	// iteration output
	private VarSequence varSequence;

	@Override
	public void declareInput(VarList inputMap) {
		super.declareInput(inputMap);
		setInputVariablesMap(inputMap);
		initializeInputVariables();
		addInputVariables();
	}

	private void setInputVariablesMap(VarList inputMap) {
		this.inputMap = inputMap;
	}

	private void initializeInputVariables() {
		this.varImage = new VarCytomineImage("Image");
		this.varResolutionLevel = new VarInteger("Resolution level", 0);
		this.varProcessingType = new VarEnum<CytomineImageBatch.ProcessingType>("Processing type",
				ProcessingType.EntireArea);
		this.varLoadedArea = new VarRectangle("Loaded Area");
		this.varTileDimension = new VarDimension("Tile dimension", new Dimension(256, 256));
		this.varLoadROIs = new VarBoolean("Load ROIs", true);
	}

	private void addInputVariables() {
		inputMap.add(varImage.getName(), varImage);
		inputMap.add(varResolutionLevel.getName(), varResolutionLevel);
		inputMap.add(varProcessingType.getName(), varProcessingType);
		inputMap.add(varLoadedArea.getName(), varLoadedArea);
		inputMap.add(varTileDimension.getName(), varTileDimension);
		inputMap.add(varLoadROIs.getName(), varLoadROIs);
	}

	@Override
	public void declareLoopVariables(List<Var<?>> loopVariables) {
		super.declareLoopVariables(loopVariables);
		for (Var<?> var : inputMap) {
			loopVariables.add(var);
		}
		varSequence = new VarSequence("Sequence", null);
		varSequence.setEnabled(false);
		loopVariables.add(varSequence);
	}

	@Override
	public void declareOutput(VarList outputMap) {
		super.declareOutput(outputMap);
		for (Var<?> var : inputMap) {
			outputMap.add(var.getName(), var);
		}
	}

	@Override
	public void initializeLoop() {
		this.currentTile = 0;

		recoverVariableValues();
		setImageImporter();
		setAndLimitLoadedArea();

		if (loadedArea.isEmpty()) {
			totalTiles = 0;
			return;
		}

		setupTileSize();
		setupResolutionSizes();
		setupTileGridSize();
	}

	private void recoverVariableValues() {
		this.image = varImage.getValue(true);
		this.resolutionLevel = varResolutionLevel.getValue(true);
		this.processingType = varProcessingType.getValue(true);
		this.loadedArea = varLoadedArea.getValue(true);
		this.tileSize = varTileDimension.getValue(true);
		this.loadROIs = varLoadROIs.getValue(true);
	}

	private void setImageImporter() {
		this.importer = new IcytomineImporter(image.getClient());
		try {
			this.importer.open(image.getId().toString(), 0);
		} catch (UnsupportedFormatException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void setAndLimitLoadedArea() {
		setupLoadedArea();
		limitLoadedArea();
	}

	private void setupLoadedArea() {
		this.loadedArea = (!loadedArea.isEmpty()) ? loadedArea : new Rectangle(image.getSize());
	}

	private void limitLoadedArea() {
		this.loadedArea = loadedArea.intersection(new Rectangle(image.getSize()));
	}

	private void setupTileSize() {
		this.tileSize = (tileSize.width != 0 && tileSize.height != 0) ? new Dimension(tileSize) : image.getTileSize();
	}

	private void setupResolutionSizes() {
		this.loadedAreaInTargetResolution = new Rectangle(loadedArea);
		this.tileSizeInResolution0 = new Dimension(tileSize);
		this.pixelResolutionInResolution = getPixelResolutionOrDefaultTo1();

		for (int lvl = 0; lvl < resolutionLevel; lvl++) {
			this.loadedAreaInTargetResolution.width /= 2;
			this.loadedAreaInTargetResolution.height /= 2;
			this.loadedAreaInTargetResolution.x /= 2;
			this.loadedAreaInTargetResolution.y /= 2;
			this.tileSizeInResolution0.width *= 2;
			this.tileSizeInResolution0.height *= 2;
			if (pixelResolutionInResolution != null) {
				this.pixelResolutionInResolution *= 2d;
			}
		}

		if (processingType == ProcessingType.EntireArea) {
			adjustTileSizeToEntireArea();
		}
		limitTileSizeToLoadedArea();
	}

	private Double getPixelResolutionOrDefaultTo1() {
		Double resolution = importer.getImageInformation().getResolution();
		if (pixelResolutionInResolution == null) {
			this.pixelResolutionInResolution = 1d;
		}
		return resolution;
	}

	private void adjustTileSizeToEntireArea() {
		this.tileSizeInResolution0.width = image.getSizeX();
		this.tileSizeInResolution0.height = image.getSizeY();
		this.tileSize.width = loadedAreaInTargetResolution.width;
		this.tileSize.height = loadedAreaInTargetResolution.height;
	}

	private void limitTileSizeToLoadedArea() {
		this.tileSize.width = Math.min(loadedAreaInTargetResolution.width, tileSize.width);
		this.tileSize.height = Math.min(loadedAreaInTargetResolution.height, tileSize.height);
		this.tileSizeInResolution0.width = Math.min(loadedArea.width, tileSizeInResolution0.width);
		this.tileSizeInResolution0.height = Math.min(loadedArea.height, tileSizeInResolution0.height);
	}

	private void setupTileGridSize() {
		this.tileGridSize = new Dimension((loadedAreaInTargetResolution.width + tileSize.width - 1) / tileSize.width,
				(loadedAreaInTargetResolution.height + tileSize.height - 1) / tileSize.height);

		this.totalTiles = tileGridSize.width * tileGridSize.height;
	}

	@Override
	public void beforeIteration() {
		try {
			Point tilePosition = getCurrentTilePosition();
			Rectangle tileRegion = getTileRegionInResolution0(tilePosition);
			Sequence seq = importTile(tileRegion);
			varSequence.setValue(seq);
		} catch (UnsupportedFormatException | IOException e) {
			throw new IcyHandledException(e);
		}
	}

	private Sequence importTile(Rectangle tileRegion)
			throws IllegalArgumentException, UnsupportedFormatException, IOException {
		Sequence seq = new Sequence(importer.getImage(0, resolutionLevel, tileRegion, 0, 0));
		setSequenceMetadata(seq, tileRegion);

		if (loadROIs) {
			addROIsToSeq(seq, tileRegion);
		}
		return seq;
	}

	private void setSequenceMetadata(Sequence seq, Rectangle tileRegion) {
		seq.setName(image.getName() + String.format("(%f, %f; %f, %f)", tileRegion.x * pixelResolutionInResolution,
				tileRegion.y * pixelResolutionInResolution, tileRegion.width * pixelResolutionInResolution,
				tileRegion.height * pixelResolutionInResolution));
		seq.setPositionX(tileRegion.x * pixelResolutionInResolution);
		seq.setPositionY(tileRegion.y * pixelResolutionInResolution);
		seq.setPixelSizeX(pixelResolutionInResolution);
		seq.setPixelSizeY(pixelResolutionInResolution);
	}

	private void addROIsToSeq(Sequence seq, Rectangle tileRegion) throws IOException {
		Rectangle regionInResolution = getRegionInTargetResolution(tileRegion);
		List<ROI2D> annotations = getAnnotationsAsROIs(regionInResolution);
		// Add rois to sequence
		seq.addROIs(annotations.stream().map(r -> (ROI) r).collect(Collectors.toList()), true);
	}

	private List<ROI2D> getAnnotationsAsROIs(Rectangle regionInResolution) throws IOException {
		List<ROI2D> annotations = new ArrayList<>(0);
		try {
			// ROIs inside the loaded area with global coordinates
			annotations = this.image.getAnnotations().stream().filter(a -> {
				// Check annotation intersects loaded image
				try {
					return a.getROI(resolutionLevel).intersects(regionInResolution);
				} catch (ParseException | CytomineException e) {
					throw new RuntimeException(e);
				}
			}).map(a -> {
				// convert to roi
				try {
					return a.getROI(resolutionLevel);
				} catch (ParseException | CytomineException e) {
					throw new RuntimeException(e);
				}
			}).collect(Collectors.toList());
		} catch (CytomineException | RuntimeException e) {
			throw new IOException(e);
		}

		convertAnnotationCoordinatesToTargetRegion(annotations, regionInResolution);

		return annotations;
	}

	private void convertAnnotationCoordinatesToTargetRegion(List<ROI2D> annotations, Rectangle regionInResolution) {
		annotations.stream().forEach(r -> r.translate(-regionInResolution.x, -regionInResolution.y));
	}

	private Rectangle getRegionInTargetResolution(Rectangle tileRegion) {
		Rectangle region = new Rectangle(tileRegion);
		IntStream.range(0, resolutionLevel).forEach(i -> {
			region.x /= 2;
			region.y /= 2;
			region.width /= 2;
			region.height /= 2;
		});
		return region;
	}

	private Rectangle getTileRegionInResolution0(Point tilePosition) {
		Rectangle region = new Rectangle(loadedArea.x + tilePosition.x * tileSizeInResolution0.width,
				loadedArea.y + tilePosition.y * tileSizeInResolution0.height, tileSizeInResolution0.width,
				tileSizeInResolution0.height);
		region = region.intersection(loadedArea);
		return region;
	}

	private Point getCurrentTilePosition() {
		return new Point(currentTile % tileGridSize.width, currentTile / tileGridSize.width);
	}

	@Override
	public void afterIteration() {
		currentTile++;
		super.afterIteration();
	}

	@Override
	public boolean isStopConditionReached() {
		return !(currentTile < totalTiles);
	}

}
