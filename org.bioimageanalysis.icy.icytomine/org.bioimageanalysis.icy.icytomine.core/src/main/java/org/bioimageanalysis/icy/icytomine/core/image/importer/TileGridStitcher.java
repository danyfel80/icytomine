package org.bioimageanalysis.icy.icytomine.core.image.importer;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.bioimageanalysis.icy.icytomine.core.view.TileResult;
import org.bioimageanalysis.icy.icytomine.core.view.converters.MagnitudeResolutionConverter;

import icy.common.listener.ProgressListener;

public class TileGridStitcher {

	public interface StitchingFinishListener {
		void stitchingFinished(Future<Void> endResult);
	}

	private double targetResolution;
	private Rectangle2D boundsAtTargetResolution;
	private TileGridImporter tileImporter;

	private Dimension2D tileDimensionAtTargetResolution;
	private Dimension targetImageDimension;

	private BufferedImage targetImage;
	private Set<StitchingFinishListener> stitchingFinishListeners;
	private Set<ProgressListener> progressListeners;

	private AtomicInteger stitchedTiles;
	private int totalTiles;

	public TileGridStitcher(double targetResolution, Rectangle2D boundsAtTargetResolution,
			TileGridImporter tileImporter) {
		this.targetResolution = targetResolution;
		this.boundsAtTargetResolution = boundsAtTargetResolution;
		this.tileImporter = tileImporter;

		stitchingFinishListeners = new HashSet<>(1);
		progressListeners = new HashSet<>(1);
		totalTiles = tileImporter.getNumberOfTiles();
		stitchedTiles = new AtomicInteger(0);
	}

	public void startStitchingTiles() {
		computeParameters();
		initializeImage();
		tileImporter.addTileImportationListener(futureTile -> placeTileInImage(futureTile));
		tileImporter.addTileImportationEndListener(endResult -> stichingFinished(endResult));
		tileImporter.requestTileGrid();
	}

	private void computeParameters() {
		computeTileDimensionAtTargetResolution();
		computeTargetImageDimension();
	}

	private void computeTileDimensionAtTargetResolution() {
		Dimension tileDimensionAtRequestResolution = tileImporter.getTileSize();
		double requestResolution = tileImporter.getResolution();
		tileDimensionAtTargetResolution = MagnitudeResolutionConverter.convertDimension2D(tileDimensionAtRequestResolution,
				requestResolution, targetResolution);
	}

	private void computeTargetImageDimension() {
		targetImageDimension = new Dimension((int) boundsAtTargetResolution.getWidth(),
				(int) boundsAtTargetResolution.getHeight());
	}

	private void initializeImage() {
		targetImage = new BufferedImage(targetImageDimension.width, targetImageDimension.height,
				BufferedImage.TYPE_INT_ARGB);
	}

	private void placeTileInImage(Future<TileResult> futureTile) {
		TileResult tileResult;
		try {
			tileResult = futureTile.get();
		} catch (ExecutionException e) {
			e.printStackTrace();
			return;
		} catch (InterruptedException e) {
			return;
		}

		Point tilePosition = tileResult.getKey().getPosition();
		Point tilePositionInImage = getTilePositionInImage(tilePosition);

		Graphics2D g = targetImage.createGraphics();
		g.drawImage(tileResult.getTileImage(), tilePositionInImage.x, tilePositionInImage.y,
				(int) Math.ceil(tileDimensionAtTargetResolution.getWidth()),
				(int) Math.ceil(tileDimensionAtTargetResolution.getHeight()), null);

		synchronized (stitchedTiles) {
			stitchedTiles.incrementAndGet();
			notifyProgress();
		}
	}

	private Point getTilePositionInImage(Point tilePosition) {
		int x = ((int) (tilePosition.x * tileDimensionAtTargetResolution.getWidth())
				- (int) boundsAtTargetResolution.getX());
		int y = ((int) (tilePosition.y * tileDimensionAtTargetResolution.getHeight())
				- (int) boundsAtTargetResolution.getY());
		return new Point(x, y);
	}

	private void notifyProgress() {
		progressListeners.forEach(listener -> listener.notifyProgress(stitchedTiles.get(), totalTiles));
	}

	private void stichingFinished(Future<Void> endResult) {
		stitchingFinishListeners.forEach(l -> l.stitchingFinished(endResult));
	}

	public void addStitchingFinishListener(StitchingFinishListener listener) {
		stitchingFinishListeners.add(listener);
	}

	public void removeStitchingFinishListener(StitchingFinishListener listener) {
		stitchingFinishListeners.remove(listener);
	}

	public BufferedImage getTargetImage() {
		return targetImage;
	}

	public void addProgressListener(ProgressListener listener) {
		progressListeners.add(listener);
	}
}
