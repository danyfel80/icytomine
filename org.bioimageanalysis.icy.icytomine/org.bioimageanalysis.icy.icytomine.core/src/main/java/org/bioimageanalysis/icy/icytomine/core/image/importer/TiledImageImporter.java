package org.bioimageanalysis.icy.icytomine.core.image.importer;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.bioimageanalysis.icy.icytomine.core.image.tile.TileCalculator;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.view.converters.MagnitudeResolutionConverter;

import icy.common.listener.ProgressListener;

public class TiledImageImporter {
	public interface TiledImageImportationListener {
		void imageImported(Future<BufferedImage> result);
	}

	private Image imageInformation;

	private double targetResolution;
	private Rectangle2D boundsAtTargetResolution;

	private int zeroResolution = 0;
	private Rectangle2D boundsAtZeroResolution;

	private int requestResolution;
	private Rectangle2D boundsAtRequestResolution;
	private Dimension2D tileDimensionAtRequestResolution;
	private Rectangle tileGrigToRequest;

	private Set<TiledImageImportationListener> tiledImageImportationListeners;
	private Set<ProgressListener> importationProgressListeners;

	private TileGridImporter tileGridImporter;

	private CompletableFuture<BufferedImage> requestResult;

	public TiledImageImporter(Image imageInformation) {
		this.imageInformation = imageInformation;
		tiledImageImportationListeners = new HashSet<>();
		importationProgressListeners = new HashSet<>();
	}

	public Future<BufferedImage> requestImage(double resolution, Rectangle2D boundsAtZeroResolution) {
		this.targetResolution = resolution;
		this.boundsAtZeroResolution = boundsAtZeroResolution;

		computeRequestParameters();
		return requestImage();
	}

	private void computeRequestParameters() {
		computeBoundsAtTargetResolution();
		computeRequestResolution();
		computeBoundsAtRequestResolution();
		computeTileDimensionAtRequestResolution();
		computeTileGridToRequest();
	}

	private void computeBoundsAtTargetResolution() {
		boundsAtTargetResolution = MagnitudeResolutionConverter.convertRectangle2D(boundsAtZeroResolution, zeroResolution,
				targetResolution);
	}

	private void computeRequestResolution() {
		int minResolution = zeroResolution;
		int maxResolution = imageInformation.getDepth().get().intValue();
		requestResolution = Math.max(minResolution, Math.min((int) targetResolution, maxResolution));
	}

	private void computeBoundsAtRequestResolution() {
		boundsAtRequestResolution = MagnitudeResolutionConverter.convertRectangle2D(boundsAtZeroResolution, zeroResolution,
				requestResolution);
	}

	private void computeTileDimensionAtRequestResolution() {
		tileDimensionAtRequestResolution = imageInformation.getTileSize().get();
	}

	private void computeTileGridToRequest() {
		Dimension tileDim = new Dimension((int) tileDimensionAtRequestResolution.getWidth(),
				(int) tileDimensionAtRequestResolution.getHeight());
		TileCalculator calculator = new TileCalculator(boundsAtRequestResolution, tileDim);
		Dimension2D imageDim = getImageSizeAtRequestResolution();
		tileGrigToRequest = calculator.getLimitedTileGridBounds(imageDim);
	}

	private Dimension2D getImageSizeAtRequestResolution() {
		Dimension dimAtZeroRes = imageInformation.getSize().get();
		return MagnitudeResolutionConverter.convertDimension2D(dimAtZeroRes, 0, requestResolution);
	}

	private Future<BufferedImage> requestImage() {
		tileGridImporter = new TileGridImporter(imageInformation, requestResolution, tileGrigToRequest);
		TileGridStitcher tileStitcher = new TileGridStitcher(targetResolution, boundsAtTargetResolution, tileGridImporter);
		tileStitcher.addStitchingFinishListener((Future<Void> endResult) -> stitchingFinished(tileStitcher, endResult));
		importationProgressListeners.forEach(listener -> tileStitcher.addProgressListener(listener));
		tileStitcher.startStitchingTiles();
		requestResult = new CompletableFuture<>();
		return requestResult;
	}

	private void stitchingFinished(TileGridStitcher tileStitcher, Future<Void> endResult) {
		try {
			if (!endResult.isCancelled()) {
				endResult.get();
				requestResult.complete(tileStitcher.getTargetImage());
			} else {
				requestResult.cancel(true);
			}
		} catch (InterruptedException | ExecutionException e) {
			requestResult.completeExceptionally(e);
		}
		tiledImageImportationListeners.forEach(l -> l.imageImported(requestResult));
	}

	public void addTiledImageImportationListener(TiledImageImportationListener listener) {
		tiledImageImportationListeners.add(listener);
	}

	public void removeTiledImageImportationListener(TiledImageImportationListener listener) {
		tiledImageImportationListeners.remove(listener);
	}

	public void addImportationProgressListener(ProgressListener listener) {
		importationProgressListeners.add(listener);
	}

	public void removeImportationProgressListener(ProgressListener listener) {
		importationProgressListeners.remove(listener);
	}

	public void cancel() throws RuntimeException, InterruptedException {
		if (tileGridImporter != null) {
			tileGridImporter.cancelImportation();
		}
	}

}
