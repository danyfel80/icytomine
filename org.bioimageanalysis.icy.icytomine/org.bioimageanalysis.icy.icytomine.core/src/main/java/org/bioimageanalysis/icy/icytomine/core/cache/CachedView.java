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
package org.bioimageanalysis.icy.icytomine.core.cache;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import com.google.common.util.concurrent.AtomicDouble;

import be.cytomine.client.CytomineException;

/**
 * This class represents a multi-resolution view that caches the image it shows.
 * This view returns the most adapted view of the current target rectangle on an
 * image. The view notifies when a new version of the view is available to be
 * drawn. In order to do this, objects interested on this notifications must add
 * a listener to this view.
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class CachedView {

	@FunctionalInterface
	public interface ViewListener {
		public void onViewChanged();
	}

	private static CacheManager	cacheManager								= CacheManagerBuilder.newCacheManagerBuilder().build(true);
	private static final int		maxRetrievalRequestRetries	= 4;

	private BufferedImage										imageView;
	private BufferedImage										oldImageView;
	private BufferedImage										imageBackground;
	private Image														imageInformation;
	private List<ViewListener>							viewListeners;
	private String													cacheName;
	private Cache<Tile2DKey, BufferedImage>	cache;

	private Future<Long>										latestRequest;
	private AtomicLong											latestRequestTime;
	private AtomicReference<Point>					latestRequestPosition;
	private AtomicDouble										latestRequestResolution;
	private ThreadPoolExecutor							viewRequestThreadPool;
	private ExecutorCompletionService<Long>	viewRequestCompletionService;
	private Thread													resolutionRequestThread;
	private boolean													active;

	/**
	 * @param imageInformation
	 *          Information of the image to be cached.
	 */
	public CachedView(Image imageInformation) {
		setImageInformation(imageInformation);
		this.viewListeners = new LinkedList<>();
	}

	/**
	 * @return Image of the last requested view.
	 */
	public BufferedImage getImageView() {
		return imageView;
	}

	/**
	 * @return Information of the image used in this view.
	 */
	public Image getImageInformation() {
		return this.imageInformation;
	}

	/**
	 * Sets the image used for this view and starts the cache.
	 * 
	 * @param imageInformation
	 *          Information of the image to cache.
	 */
	private void setImageInformation(Image imageInformation) {
		this.imageInformation = imageInformation;
		try {
			this.imageBackground = getImageInformation().getThumbnail(512);
		} catch (CytomineException e2) {
			System.err.println("No thumbnail");
		}
		this.cacheName = "ViewCache" + this.hashCode();
		this.cache = cacheManager.getCache(this.cacheName, Tile2DKey.class, BufferedImage.class);
		if (this.cache == null) {
			this.cache = cacheManager.createCache(this.cacheName, CacheConfigurationBuilder
					.newCacheConfigurationBuilder(Tile2DKey.class, BufferedImage.class, ResourcePoolsBuilder.heap(500)).build());
		}

		this.latestRequestResolution = new AtomicDouble(0);
		this.latestRequestPosition = new AtomicReference<>(null);

		this.viewRequestThreadPool = (ThreadPoolExecutor) Executors
				.newFixedThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors() / 2));
		this.viewRequestCompletionService = new ExecutorCompletionService<>(viewRequestThreadPool);

		this.resolutionRequestThread = new Thread(() -> {
			while (active) {
				Future<Long> requestFuture = null;
				try {
					requestFuture = viewRequestCompletionService.take();
				} catch (InterruptedException e) {
					viewRequestThreadPool.shutdownNow();
					try {
						boolean finished = viewRequestThreadPool.awaitTermination(1, TimeUnit.SECONDS);
						if (!finished) throw new Exception("Did not finished view request thread pool.");
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					break;
				}

				try {
					@SuppressWarnings("unused")
					Long req = requestFuture.get();
					// System.out.println("Completed image at " + req);
				} catch (CancellationException e) {
					continue;
				} catch (ExecutionException e) {
					e.printStackTrace();
					continue;
				} catch (InterruptedException e) {
					viewRequestThreadPool.shutdownNow();
					try {
						boolean finished = viewRequestThreadPool.awaitTermination(1, TimeUnit.SECONDS);
						if (!finished) throw new Exception("Did not finished view request thread pool.");
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					break;
				}

			}
		}, "resolutionRequest");

		active = true;
		this.resolutionRequestThread.start();
	}

	private void cancelPreviousUnfinishedRequests() {
		if (latestRequest != null) {
			latestRequest.cancel(true);
			viewRequestThreadPool.purge();
		}
	}

	/**
	 * @param resolution
	 *          Resolution to clip
	 * @return Resolution clipped to the image resolution limits.
	 */
	public double clipResolution(double resolution) {
		return Math.max(Math.min(resolution, getImageInformation().getDepth()), 0d);
	}

	/**
	 * @param requestedResolution
	 *          Resolution level requested to the server.
	 * @param displayedResolution
	 *          Resolution at which the retrieved image is to be displayed.
	 * @return Scaling factor of the images retrieved from the server.
	 */
	private double getTileScaling(int requestedResolution, double displayedResolution) {
		double subSampling = requestedResolution - displayedResolution;
		return Math.pow(2, subSampling);
	}

	/**
	 * @param position
	 *          Position at base resolution (0) to be transformed to the given
	 *          {@code resolution}.
	 * @param resolution
	 *          Target resolution.
	 * @return Transformed {@code position} to the given {@code resolution}.
	 */
	private Point getPositionInResolution(Point position, double resolution) {
		Point positionInResolution = new Point(position);
		double denom = Math.pow(2, resolution);
		positionInResolution.x = (int) (positionInResolution.x / denom);
		positionInResolution.y = (int) (positionInResolution.y / denom);
		return positionInResolution;
	}

	/**
	 * @param position
	 *          Position at {@code baseResolution} to be transformed to the given
	 *          {@code resolution}.
	 * @param baseResolution
	 *          SourceResolution.
	 * @param resolution
	 *          Target resolution.
	 * @return Transformed {@code position} to the given {@code resolution}.
	 */
	private Point getPositionInResolution(Point position, double baseResolution, double resolution) {
		Point positionInResolution = new Point(position);
		double denom = Math.pow(2, resolution - baseResolution);
		positionInResolution.x = (int) (positionInResolution.x / denom);
		positionInResolution.y = (int) (positionInResolution.y / denom);
		return positionInResolution;
	}

	/**
	 * @param size
	 *          Size at base resolution (0) to be transformed to the given
	 *          {@code resolution}.
	 * @param resolution
	 *          Target resolution.
	 * @return Image size at target resolution
	 */
	private Dimension getDimensionInResolution(Dimension size, int resolution) {
		Dimension sizeInResolution = new Dimension(getImageInformation().getSize());
		IntStream.range(0, resolution).forEach(r -> {
			sizeInResolution.width /= 2;
			sizeInResolution.height /= 2;
		});
		return sizeInResolution;
	}

	/**
	 * @param size
	 *          Size at {@code baseResolution} to be transformed to the given
	 *          {@code resolution}.
	 * @param baseResolution
	 *          Source resolution.
	 * @param resolution
	 *          Target resolution.
	 * @return Image size at target resolution
	 */
	private Dimension getDimensionInResolution(Dimension size, double baseResolution, double resolution) {
		Dimension sizeInResolution = new Dimension(size);
		double denom = Math.pow(2, resolution - baseResolution);
		sizeInResolution.width = (int) (sizeInResolution.width / denom);
		sizeInResolution.height = (int) (sizeInResolution.height / denom);
		return sizeInResolution;
	}

	private Rectangle getImageTileRectangle(int resolution) {
		Dimension tileSize = getImageInformation().getTileSize();
		Dimension imageSize = getDimensionInResolution(getImageInformation().getSize(), resolution);
		Dimension tiles = new Dimension(imageSize.width / tileSize.width, imageSize.height / tileSize.height);
		if (imageSize.width % tileSize.width > 0) tiles.width++;
		if (imageSize.height % tileSize.height > 0) tiles.height++;
		return new Rectangle(new Point(), tiles);
	}

	/**
	 * @param rectangle
	 *          Image rectangle requested to the Cytomine server at target
	 *          resolution.
	 * @param tileSize
	 *          Size of the tiles at the target resolution.
	 * @param imageTileRectangle
	 *          Tile rectangle of the image at target resolution.
	 * @return Rectangle of the coordinates of the tiles.
	 */
	private Rectangle getTileRectangle(Rectangle rectangle, Dimension tileSize) {

		Point minCoord = new Point(rectangle.x / tileSize.width, rectangle.y / tileSize.height);
		if (rectangle.x < 0) minCoord.x--;
		if (rectangle.y < 0) minCoord.y--;
		Point maxCoord = new Point((rectangle.x + rectangle.width) / tileSize.width,
				(rectangle.y + rectangle.height) / tileSize.height);
		if ((rectangle.x + rectangle.width) % tileSize.width > 0) maxCoord.x++;
		if ((rectangle.y + rectangle.height) % tileSize.height > 0) maxCoord.y++;

		// Convert from coordinates to size
		Dimension tileRectangleSize = new Dimension(maxCoord.x - minCoord.x, maxCoord.y - minCoord.y);

		// Create tile rectangle
		return new Rectangle(minCoord, tileRectangleSize);
	}

	/**
	 * @param position
	 *          Position at which the image is requested to the Cytomine
	 *          server at target resolution.
	 * @param tileSize
	 *          Size of the tiles at the target resolution.
	 * @return Pixel offset in the view port, of the image retrieved from the
	 *         server.
	 */
	private synchronized Point getPixelOffset(Point position, Dimension tileSize) {
		int x = ((position.x % tileSize.width) + tileSize.width) % tileSize.width;
		int y = ((position.y % tileSize.height) + tileSize.height) % tileSize.height;
		return new Point(x, y);
	}

	/**
	 * Launches a request to the cytomine server of the image centered at
	 * {@code position} on the base image (resolution 0) at the given
	 * {@code resolution}. The tiles requested to the server are adjusted to the
	 * {@code viewPortSize}.
	 * 
	 * @param position
	 *          Position of the image to be requested at base resolution (0).
	 * @param resolution
	 *          Resolution at which the image is to be retrieved.
	 * @param viewPortSize
	 *          Size of the image to be displayed.
	 */
	public synchronized void requestView(Point position, double resolution, Dimension viewPortSize) {
		cancelPreviousUnfinishedRequests();
		// Set timestamp
		if (latestRequestTime == null) latestRequestTime = new AtomicLong();
		latestRequestTime.set(System.currentTimeMillis());
		double previousResolution = latestRequestResolution.getAndSet(resolution);
		Point previousPosition = latestRequestPosition.getAndSet(new Point(position));

		// Find out resolution and tiles needed
		int requestedResolution = (int) clipResolution(resolution);
		double tileScalingInResolution = getTileScaling(requestedResolution, resolution);
		Point requestedPositionInRealResolution = getPositionInResolution(position, resolution);
		Point requestedPositionInResolution = getPositionInResolution(position, requestedResolution);
		Dimension viewPortSizeInResolution = new Dimension((int) (viewPortSize.width / tileScalingInResolution),
				(int) (viewPortSize.height / tileScalingInResolution));
		Rectangle requestedRectangleInResolution = new Rectangle(requestedPositionInResolution, viewPortSizeInResolution);
		Dimension tileSizeInResolution = getImageInformation().getTileSize();
		Rectangle imageTileRectangle = getImageTileRectangle(requestedResolution);
		Rectangle requestedTileRectangle = getTileRectangle(requestedRectangleInResolution, tileSizeInResolution);
		Point pixelOffset = getPixelOffset(requestedPositionInResolution, tileSizeInResolution);

		// Check the image view exists
		if (imageView == null) {
			imageView = new BufferedImage(viewPortSize.width, viewPortSize.height, BufferedImage.TYPE_INT_RGB);
		}

		BufferedImage newImageView;
		if (oldImageView == null || oldImageView.getWidth() < viewPortSize.width
				|| oldImageView.getHeight() < viewPortSize.height) {
			newImageView = new BufferedImage(viewPortSize.width, viewPortSize.height, BufferedImage.TYPE_INT_RGB);
		} else {
			newImageView = oldImageView;
		}
		oldImageView = imageView;
		{
			Graphics2D g2 = newImageView.createGraphics();
			g2.setColor(Color.white);
			g2.fillRect(0, 0, viewPortSize.width, viewPortSize.height);
			g2.dispose();
		}

		// Use thumbnail to show rough version of image
		if (imageBackground != null) {
			Graphics2D g2 = newImageView.createGraphics();
			Dimension imageSizeInRes = getDimensionInResolution(getImageInformation().getSize(), 0, resolution);
			g2.drawImage(imageBackground, -requestedPositionInRealResolution.x, -requestedPositionInRealResolution.y,
					imageSizeInRes.width, imageSizeInRes.height, null);
			g2.dispose();
		}

		// Use previous image to show faster image
		if (previousPosition != null && previousResolution > resolution) {
			Graphics2D g2 = newImageView.createGraphics();
			Point oldImagePosition = getPositionInResolution(previousPosition, 0, resolution);
			oldImagePosition.x -= requestedPositionInRealResolution.x;
			oldImagePosition.y -= requestedPositionInRealResolution.y;

			Dimension oldImageDimension = getDimensionInResolution(new Dimension(imageView.getWidth(), imageView.getHeight()),
					previousResolution, resolution);
			g2.drawImage(imageView, oldImagePosition.x, oldImagePosition.y, oldImageDimension.width, oldImageDimension.height,
					null);
			g2.dispose();
		}

		// Find tile size for given resolution
		int tileWidth = (int) (imageInformation.getTileWidth() * tileScalingInResolution);
		int tileHeight = (int) (imageInformation.getTileHeight() * tileScalingInResolution);
		int tileOffsetX = (int) (pixelOffset.x * tileScalingInResolution);
		int tileOffsetY = (int) (pixelOffset.y * tileScalingInResolution);

		//int cachedTiles = 0;
		AtomicInteger downloadedTiles = new AtomicInteger(0);

		ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors
				.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		ExecutorCompletionService<TileResult> tileRequestCompletionService = new ExecutorCompletionService<>(threadPool);

		for (int j = 0; j < requestedTileRectangle.height; j++) {
			final int y = requestedTileRectangle.y + j;
			// Check inside tile y bounds
			if (y < imageTileRectangle.y || y >= imageTileRectangle.y + imageTileRectangle.height) continue;
			for (int i = 0; i < requestedTileRectangle.width; i++) {
				final int x = requestedTileRectangle.x + i;
				// Check inside tile x bounds
				if (x < imageTileRectangle.x || x >= imageTileRectangle.x + imageTileRectangle.width) continue;
				Tile2DKey key = new Tile2DKey(getImageInformation(), requestedResolution, x, y);
				BufferedImage tileImage;
				synchronized (cache) {
					tileImage = cache.get(key);
				}

				// Take cached version
				if (tileImage != null) {
					//cachedTiles++;

					int tilePositionOnViewX = (key.getX() - requestedTileRectangle.x) * tileWidth - tileOffsetX;
					int tilePositionOnViewY = (key.getY() - requestedTileRectangle.y) * tileHeight - tileOffsetY;
					int tileImageWidth = (int) (tileImage.getWidth() * tileScalingInResolution);
					int tileImageHeight = (int) (tileImage.getHeight() * tileScalingInResolution);

					Graphics2D g = newImageView.createGraphics();
					g.drawImage(tileImage, tilePositionOnViewX, tilePositionOnViewY, tileImageWidth, tileImageHeight, null);
					g.dispose();
				}
				// Download tile
				else {
					downloadedTiles.getAndIncrement();
					tileRequestCompletionService.submit(new Callable<TileResult>() {
						@Override
						public TileResult call() throws Exception {
							Tile2DKey key = new Tile2DKey(getImageInformation(), requestedResolution, x, y);
							BufferedImage tileImage = null;

							int retry = 0;
							while (tileImage == null && retry < maxRetrievalRequestRetries) {
								retry++;
								if (retry > 1) System.out.println("retry " + retry);
								try {
									tileImage = imageInformation.getClient().downloadPictureAsBufferedImage(
											getImageInformation().getUrl(key.getResolution(), key.getX(), key.getY()), "ndpi");
								} catch (CytomineException e) {
									throw new IOException("Could not retrieve tile " + key, e);
								}
							}
							if (tileImage == null) {
								throw new IOException("Could not retrieve tile " + key + " with 4 retries.");
							}

							synchronized (cache) {
								cache.put(key, tileImage);
							}

							return new TileResult(key, tileImage);
						}

					});
				}
			}
		}

		imageView = newImageView;
		for (ViewListener viewListener: viewListeners) {
			viewListener.onViewChanged();
		}

		threadPool.shutdown();

		// Receive results on a separate thread

		// Launch image construction task
		latestRequest = viewRequestCompletionService.submit(new Callable<Long>() {
			private long	requestTime			= latestRequestTime.get();
			private int		tilesToDownload	= downloadedTiles.get();

			@Override
			public Long call() throws Exception {

				if (latestRequestTime.get() > requestTime) return requestTime;
				TileResult tile;
				Graphics2D g = null;
				try {
					for (int i = 0; i < tilesToDownload; i++) {
						tile = tileRequestCompletionService.take().get();
						// System.out.println("Retrieved " + tile.getKey());
						if (latestRequestTime.get() > this.requestTime) break;

						int tilePositionOnViewX = (tile.getKey().getX() - requestedTileRectangle.x) * tileWidth - tileOffsetX;
						int tilePositionOnViewY = (tile.getKey().getY() - requestedTileRectangle.y) * tileHeight - tileOffsetY;
						int tileImageWidth = (int) (tile.getTileImage().getWidth() * tileScalingInResolution);
						int tileImageHeight = (int) (tile.getTileImage().getHeight() * tileScalingInResolution);

						g = imageView.createGraphics();
						g.drawImage(tile.getTileImage(), tilePositionOnViewX, tilePositionOnViewY, tileImageWidth, tileImageHeight,
								null);
						g.dispose();
						g = null;

						for (ViewListener viewListener: viewListeners) {
							viewListener.onViewChanged();
						}
					}
				} catch (InterruptedException e) {

				} catch (ExecutionException e) {
					if (!(e.getCause() instanceof IOException))
						throw e;
					else
						e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
					throw e;
				} finally {
					if (g != null) g.dispose();
				}
				return requestTime;
			}
		});

	}

	public void addViewListener(ViewListener listener) {
		this.viewListeners.add(listener);
	}

	public void close() {
		this.active = false;
		this.resolutionRequestThread.interrupt();

		cacheManager.removeCache(this.cacheName);
	}
}
