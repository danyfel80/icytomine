package org.bioimageanalysis.icy.icytomine.core.view;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

public class ViewTileCache {
	private static CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);

	public interface ViewTileLoadListener {
		void tileLoaded(Tile2DKey tilePosition, BufferedImage tileImage);
	}

	private Image imageInformation;
	private Cache<Tile2DKey, BufferedImage> tileCache;

	private Set<ViewTileLoadListener> tileLoadListeners;

	private ThreadPoolExecutor tileRequestThreadPool;
	private ExecutorCompletionService<TileResult> tileRequestCompletionService;
	private List<Future<TileResult>> tileRequests;

	public ViewTileCache(Image imageInformation) {
		this.imageInformation = imageInformation;
		this.tileLoadListeners = new HashSet<>();
		startTileCache();
		this.tileRequestThreadPool = (ThreadPoolExecutor) Executors
				.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
		this.tileRequestCompletionService = new ExecutorCompletionService<>(tileRequestThreadPool);
		this.tileRequests = new LinkedList<>();
		startCompletedRequestHandlingService();
	}

	private void startTileCache() {
		String cacheName = "ViewCache" + this.hashCode();
		this.tileCache = cacheManager.getCache(cacheName, Tile2DKey.class, BufferedImage.class);
		if (tileCache == null) {
			tileCache = cacheManager.createCache(cacheName,
					CacheConfigurationBuilder
							.newCacheConfigurationBuilder(Tile2DKey.class, BufferedImage.class, ResourcePoolsBuilder.heap(500))
							.build());
		}
	}

	private void startCompletedRequestHandlingService() {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<Void> executionResult = executor.submit(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				while (!tileRequestThreadPool.isTerminated()) {
					try {
						Future<TileResult> futureResult = tileRequestCompletionService.take();
						TileResult result = futureResult.get();
						notifyTileLoaded(result);
					} catch (CancellationException e) {
						continue;
					} catch (InterruptedException e) {
						break;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				System.out.println("Stopped receiving cache events.");
				return null;
			}
		});
		executor.shutdown();

		ExecutorService executor2 = Executors.newSingleThreadExecutor();
		executor2.submit(() -> {
			try {
				executionResult.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		});
		executor2.shutdown();
	}

	protected void notifyTileLoaded(TileResult result) {
		tileLoadListeners.forEach(l -> l.tileLoaded(result.getKey(), result.getTileImage()));
	}

	public void addTileLoadedListener(ViewTileLoadListener listener) {
		tileLoadListeners.add(listener);
	}

	public void cancelPreviousRequest() {
		tileRequests.forEach(future -> future.cancel(true));
		tileRequestThreadPool.purge();
		tileRequests.clear();
	}

	public void requestTile(long resolutionLevel, int x, int y) {
		Tile2DKey key = new Tile2DKey(imageInformation, resolutionLevel, x, y);
		Future<TileResult> request = tileRequestCompletionService.submit(() -> {
			BufferedImage tileImage = tileCache.get(key);
			if (tileImage == null) {
				tileImage = imageInformation.getClient()
						.downloadPictureAsBufferedImage(imageInformation.getUrl(resolutionLevel, x, y), "ndpi");
			}
			if (tileImage != null) {
				tileCache.put(key, tileImage);
			} else {
				tileImage = createDefaultTile();
			}
			return new TileResult(key, tileImage);
		});

		tileRequests.add(request);
	}

	private BufferedImage createDefaultTile() {
		return new BufferedImage(imageInformation.getTileWidth(), imageInformation.getTileHeight(),
				BufferedImage.TYPE_INT_ARGB);
	}

	public void stop() {
		this.tileRequestThreadPool.shutdownNow();
		try {
			this.tileRequestThreadPool.awaitTermination(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public boolean isProcessing() {
		return !this.tileRequestThreadPool.getQueue().isEmpty();
	}
}
