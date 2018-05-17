package org.bioimageanalysis.icy.icytomine.core.image.importer;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.view.Tile2DKey;
import org.bioimageanalysis.icy.icytomine.core.view.TileResult;

public class TileGridImporter {

	public interface TileImportationListener {
		void tileImported(Future<TileResult> resultFuture);
	}

	public interface TileImportationEndListener {
		void importationFinished(Future<Void> endFuture);
	}

	private Image imageInformation;
	private Rectangle grid;
	private int resolution;
	private Dimension tileSize;

	private Set<TileImportationListener> tileImportationListeners;
	private Set<TileImportationEndListener> tileImportationEndListeners;
	private ExecutorService threadPool;
	private ExecutorService futureResultsHandlingService;

	public TileGridImporter(Image imageInformation, int resolution, Rectangle grid) {
		this.imageInformation = imageInformation;
		this.resolution = resolution;
		this.grid = grid;
		this.tileSize = imageInformation.getTileSize();

		tileImportationListeners = new HashSet<>();
		tileImportationEndListeners = new HashSet<>();
	}

	public void requestTileGrid() {
		int numberOfTiles = getNumberOfTiles();
		int numProcessors = Math.min(numberOfTiles, Runtime.getRuntime().availableProcessors());
		threadPool = Executors.newFixedThreadPool(numProcessors);
		CompletionService<TileResult> completionService = new ExecutorCompletionService<>(threadPool);
		List<Future<TileResult>> futureResults = submitTileTasks(completionService);
		startHandlingFutureResults(completionService, futureResults);
	}

	private List<Future<TileResult>> submitTileTasks(CompletionService<TileResult> completionService) {
		List<Future<TileResult>> futureResults = new LinkedList<>();

		for (int x = 0; !Thread.interrupted() && x < grid.width; x++) {
			for (int y = 0; !Thread.interrupted() && y < grid.height; y++) {
				Tile2DKey key = new Tile2DKey(imageInformation, resolution, grid.x + x, grid.y + y);
				futureResults.add(completionService.submit(getTileTask(key)));
			}
		}

		return futureResults;
	}

	private void startHandlingFutureResults(CompletionService<TileResult> completionService,
			List<Future<TileResult>> futureResults) {
		futureResultsHandlingService = Executors.newSingleThreadExecutor();
		futureResultsHandlingService.submit(() -> {
			CompletableFuture<Void> endFuture = new CompletableFuture<>();
			int numberOfTiles = futureResults.size();
			int tile;
			for (tile = 0; !Thread.interrupted() && tile < numberOfTiles; tile++) {
				try {
					Future<TileResult> futureResult = completionService.take();
					TileResult result = futureResult.get();
					CompletableFuture<TileResult> returnFuture = new CompletableFuture<>();
					returnFuture.complete(result);
					notifyTileImportationListeners(returnFuture);
				} catch (InterruptedException e) {
					futureResults.forEach(f -> f.cancel(true));
					break;
				} catch (ExecutionException e) {
					futureResults.forEach(f -> f.cancel(true));
					endFuture.completeExceptionally(e);
					break;
				}
			}

			if (Thread.interrupted() || tile < numberOfTiles)
				endFuture.cancel(true);

			if (!endFuture.isDone())
				endFuture.complete(null);

			notifyTileImportationEndListeners(endFuture);
			threadPool.shutdownNow();
			futureResultsHandlingService.shutdown();
		});
	}

	public int getNumberOfTiles() {
		return grid.width * grid.height;
	}

	private Callable<TileResult> getTileTask(Tile2DKey key) {
		return () -> {
			TileImporter importer = new TileImporter(key.getImage(), (int) key.getResolution(), key.getX(), key.getY());
			BufferedImage tileImage = importer.getTile();
			return new TileResult(key, tileImage);
		};
	}

	private void notifyTileImportationListeners(CompletableFuture<TileResult> returnFuture) {
		tileImportationListeners.forEach(l -> l.tileImported(returnFuture));
	}

	private void notifyTileImportationEndListeners(CompletableFuture<Void> endFuture) {
		tileImportationEndListeners.forEach(l -> l.importationFinished(endFuture));
	}

	public void addTileImportationListener(TileImportationListener listener) {
		tileImportationListeners.add(listener);
	}

	public void removeTileImportationListener(TileImportationListener listener) {
		tileImportationListeners.remove(listener);
	}

	public void addTileImportationEndListener(TileImportationEndListener listener) {
		tileImportationEndListeners.add(listener);
	}

	public void removeTileImportationEndListener(TileImportationEndListener listener) {
		tileImportationEndListeners.remove(listener);
	}

	public void cancelImportation() throws RuntimeException, InterruptedException {
		futureResultsHandlingService.shutdownNow();

		if (!futureResultsHandlingService.awaitTermination(5, TimeUnit.SECONDS))
			throw new RuntimeException("Could not interrupt all threads");
	}

	public int getResolution() {
		return resolution;
	}

	public Dimension getTileSize() {
		return tileSize;
	}
}
