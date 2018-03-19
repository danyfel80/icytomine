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
package plugins.danyfel80.icytomine.protocol;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.model.Ontology;
import org.bioimageanalysis.icy.icytomine.core.model.Project;
import org.bioimageanalysis.icy.icytomine.core.model.Term;

import be.cytomine.client.CytomineException;
import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginLibrary;
import icy.roi.ROI;
import icy.sequence.Sequence;
import icy.system.IcyHandledException;
import plugins.adufour.blocks.lang.Block;
import plugins.adufour.blocks.util.VarList;
import plugins.adufour.vars.lang.VarBoolean;
import plugins.adufour.vars.lang.VarInteger;
import plugins.adufour.vars.lang.VarSequence;
import plugins.adufour.vars.lang.VarString;
import plugins.kernel.roi.roi2d.ROI2DPolygon;
import plugins.kernel.roi.roi2d.ROI2DRectangle;
import plugins.kernel.roi.roi2d.ROI2DShape;
import vars.VarCytomineImage;

/**
 * This block can send ROIs included in a given sequence to a cytomine server.
 * The ROIs are associated to a given term.
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class CytomineExportROI extends Plugin implements PluginLibrary, Block {

	VarSequence inVarSeq;
	VarCytomineImage inVarImage;
	VarInteger inVarResolutionLevel;
	VarString inVarTermName;

	VarBoolean outVarSuccess;

	@Override
	public void declareInput(VarList inputMap) {
		inVarSeq = new VarSequence("Sequence", null);
		inVarImage = new VarCytomineImage("Image");
		inVarResolutionLevel = new VarInteger("Resolution level", 0);
		inVarTermName = new VarString("Associated term", "");
		inputMap.add(inVarSeq.getName(), inVarSeq);
		inputMap.add(inVarImage.getName(), inVarImage);
		inputMap.add(inVarResolutionLevel.getName(), inVarResolutionLevel);
		inputMap.add(inVarTermName.getName(), inVarTermName);
	}

	@Override
	public void declareOutput(VarList outputMap) {
		outVarSuccess = new VarBoolean("Success", false);
		outputMap.add(outVarSuccess.getName(), outVarSuccess);
	}

	private boolean isValidResolution(Image image, Integer resolutionLevel) {
		return resolutionLevel >= 0 && resolutionLevel <= image.getDepth();
	}

	private void exportAnnotation(String annotationDescription, Image image, Term associatedTerm)
			throws CytomineException {
		Annotation annotation = new Annotation(image.getClient().addAnnotation(annotationDescription, image.getId()), image,
				image.getClient());
		if (associatedTerm == null)
			System.out.println("No term associated to annotation");
		else
			image.getClient().addAnnotationTerm(annotation.getId(), associatedTerm.getId());
	}

	private Boolean exportROIs(Sequence seq, Image image, Integer resolutionLevel, String termName) {

		// Retrieve ROIs from sequence
		ArrayList<ROI> roisToExport = seq.getROIs();
		if (roisToExport.isEmpty())
			return Boolean.TRUE;

		// Find term associated to annotation
		Term term = null;
		try {
			Project project = image.getProject();
			List<Term> terms = Ontology.getTerms(image.getClient(), project.getOntologyId());
			term = terms.stream().filter(t -> t.getName().equals(termName)).findFirst().orElse(null);
		} catch (CytomineException e) {
			throw new IcyHandledException(e);
		}

		Term usedTerm = term;
		Point2D seqPositionInPixels = new Point2D.Double(seq.getPositionX() / seq.getPixelSizeX(),
				seq.getPositionY() / seq.getPixelSizeY());
		int ROIscale = IntStream.iterate(1, i -> i * 2).skip(resolutionLevel).findFirst().getAsInt();

		// convert ROIs to resolution 0 ROI2DShapes
		roisToExport.stream().map(roi -> {
			ROI2DShape roi2D = null;
			if (roi instanceof ROI2DPolygon || roi instanceof ROI2DRectangle) {
				List<Point2D> convertedPoints = ((ROI2DShape) roi).getControlPoints().stream()
						.map(p -> new Point2D.Double(seqPositionInPixels.getX() + p.getPositionX() * ROIscale,
								image.getSizeY() - (seqPositionInPixels.getY() + p.getPositionY() * ROIscale)))
						.collect(Collectors.toList());
				roi2D = new ROI2DPolygon(convertedPoints);
			}
			return roi2D;
		})
				// export annotation to server
				.forEach(shape -> {
					String wktPolygon = Annotation.convertToWKT(shape);
					try {
						exportAnnotation(wktPolygon, image, usedTerm);
					} catch (CytomineException e) {
						throw new IcyHandledException(e);
					}
				});
		return Boolean.TRUE;
	}

	@Override
	public void run() {
		Sequence seq = inVarSeq.getValue(true);
		Image image = inVarImage.getValue(true);
		Integer resolutionLevel = inVarResolutionLevel.getValue(true);
		String termName = inVarTermName.getValue(true);

		if (!isValidResolution(image, resolutionLevel))
			throw new IcyHandledException(
					String.format("Invalid resolution %d, [0-%d] was expected.", resolutionLevel, image.getDepth()));

		outVarSuccess.setValue(exportROIs(seq, image, resolutionLevel, termName));
	}
}
