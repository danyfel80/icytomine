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
import java.awt.Rectangle;
import java.io.IOException;
import java.util.List;

import org.bioimageanalysis.icy.icytomine.core.IcytomineImporter;
import org.bioimageanalysis.icy.icytomine.core.model.Image;

import icy.common.exception.UnsupportedFormatException;
import icy.sequence.Sequence;
import icy.system.IcyHandledException;
import plugins.adufour.blocks.lang.Loop;
import plugins.adufour.blocks.util.VarList;
import plugins.adufour.vars.lang.Var;
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

	// iteration variables
	private Image image;
	private int resolutionLevel;
	private ProcessingType processingType;
	private Rectangle loadedArea;
	private Dimension tileSize;

	private IcytomineImporter importer;
	private Rectangle loadedAreaInResolutionR;
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
		this.inputMap = inputMap;

		this.varImage = new VarCytomineImage("Image");
		this.varResolutionLevel = new VarInteger("Resolution level", 0);
		this.varProcessingType = new VarEnum<CytomineImageBatch.ProcessingType>("Processing type", ProcessingType.EntireArea);
		this.varLoadedArea = new VarRectangle("Loaded Area");
		this.varTileDimension = new VarDimension("Tile dimension", new Dimension(256, 256));

		inputMap.add(varImage.getName(), varImage);
		inputMap.add(varResolutionLevel.getName(), varResolutionLevel);
		inputMap.add(varProcessingType.getName(), varProcessingType);
		inputMap.add(varLoadedArea.getName(), varLoadedArea);
		inputMap.add(varTileDimension.getName(), varTileDimension);
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

		this.image = varImage.getValue(true);
		this.resolutionLevel = varResolutionLevel.getValue(true);
		this.processingType = varProcessingType.getValue(true);
		this.loadedArea = varLoadedArea.getValue(true);
		this.tileSize = varTileDimension.getValue(true);

		this.importer = new IcytomineImporter(image.getClient());
		try {
			this.importer.open(image.getId().toString(), 0);
		} catch (UnsupportedFormatException | IOException e) {
			throw new RuntimeException(e);
		}

		this.loadedArea = (!loadedArea.isEmpty()) ? loadedArea : new Rectangle(image.getSize());
		this.loadedArea = loadedArea.intersection(new Rectangle(image.getSize()));
		if (loadedArea.isEmpty()) {
			totalTiles = 0;
			return;
		}

		this.loadedAreaInResolutionR = new Rectangle(loadedArea);

		this.tileSize = (tileSize.width != 0 && tileSize.height != 0) ? new Dimension(tileSize) : image.getTileSize();
		this.tileSizeInResolution0 = new Dimension(tileSize);
		this.pixelResolutionInResolution = importer.getImageInformation().getResolution();

		for (int lvl = 0; lvl < resolutionLevel; lvl++) {
			this.loadedAreaInResolutionR.width /= 2;
			this.loadedAreaInResolutionR.height /= 2;
			this.loadedAreaInResolutionR.x /= 2;
			this.loadedAreaInResolutionR.y /= 2;
			this.tileSizeInResolution0.width *= 2;
			this.tileSizeInResolution0.height *= 2;
			if (pixelResolutionInResolution != null) {
				this.pixelResolutionInResolution *= 2d;
			}
		}

		if (processingType == ProcessingType.EntireArea) {
			this.tileSizeInResolution0.width = image.getSizeX();
			this.tileSizeInResolution0.height = image.getSizeY();
			this.tileSize.width = loadedAreaInResolutionR.width;
			this.tileSize.height = loadedAreaInResolutionR.height;
		}

		this.tileSize.width = Math.min(loadedAreaInResolutionR.width, tileSize.width);
		this.tileSize.height = Math.min(loadedAreaInResolutionR.height, tileSize.height);
		this.tileSizeInResolution0.width = Math.min(loadedArea.width, tileSizeInResolution0.width);
		this.tileSizeInResolution0.height = Math.min(loadedArea.height, tileSizeInResolution0.height);

		if (pixelResolutionInResolution == null) {
			this.pixelResolutionInResolution = 1d;
		}
		this.tileGridSize = new Dimension((loadedAreaInResolutionR.width + tileSize.width - 1) / tileSize.width,
				(loadedAreaInResolutionR.height + tileSize.height - 1) / tileSize.height);

		this.totalTiles = tileGridSize.width * tileGridSize.height;

	}

	@Override
	public void beforeIteration() {
		try {
			int posX = currentTile % tileGridSize.width;
			int posY = currentTile / tileGridSize.width;
			// import image
			Rectangle region = new Rectangle(loadedArea.x + posX * tileSizeInResolution0.width,
					loadedArea.y + posY * tileSizeInResolution0.height, tileSizeInResolution0.width,
					tileSizeInResolution0.height);
			region = region.intersection(loadedArea);
			Sequence seq = new Sequence(importer.getImage(0, varResolutionLevel.getValue(), region, 0, 0));

			// set some metadata
			seq.setName(image.getName() + String.format("(%f, %f; %f, %f)", region.x * pixelResolutionInResolution,
					region.y * pixelResolutionInResolution, region.width * pixelResolutionInResolution,
					region.height * pixelResolutionInResolution));
			seq.setPositionX(region.x * pixelResolutionInResolution);
			seq.setPositionY(region.y * pixelResolutionInResolution);
			seq.setPixelSizeX(pixelResolutionInResolution);
			seq.setPixelSizeY(pixelResolutionInResolution);

			// send to output var
			varSequence.setValue(seq);
		} catch (UnsupportedFormatException | IOException e) {
			throw new IcyHandledException(e);
		}
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
