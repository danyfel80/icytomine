/*
 * Copyright 2010-2016 Institut Pasteur.
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
package org.bioimageanalysis.icy.icytomine.core;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;
import org.bioimageanalysis.icy.icytomine.core.model.Image;

import icy.common.exception.UnsupportedFormatException;
import icy.common.listener.ProgressListener;
import icy.image.IcyBufferedImage;
import icy.image.IcyBufferedImageUtil;
import icy.sequence.SequenceIdImporter;
import loci.formats.ome.OMEXMLMetadataImpl;
import ome.xml.meta.OMEXMLMetadata;

/**
 * @author Daniel Felipe Gonzalez Obando
 */
public class IcytomineImporter implements SequenceIdImporter {

	private static final int defaultTileSize = 256;
	private static final int defaultThumbnailSize = 200;
	private static final int maxRetrievalRetries = 4;

	private final CytomineClient client;

	private Image imageInformation;
	private Rectangle fullImageRectangle;
	private ProgressListener progressListener;

	public IcytomineImporter(CytomineClient cytomine) {
		this.client = cytomine;
	}

	public void setProgressListener(ProgressListener listener) {
		this.progressListener = listener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see icy.sequence.SequenceIdImporter#open(java.lang.String, int)
	 */
	@Override
	public boolean open(String idImage, int flags) throws UnsupportedFormatException, IOException {
		try {
			this.imageInformation = client.getImageInstance(Long.parseLong(idImage));
		} catch (NumberFormatException e) {
			throw new UnsupportedFormatException(String.format("Invalid id :%s", idImage), e);
		} catch (CytomineClientException e) {
			throw new IOException("Could not retrieve image information from host server.", e);
		}

		Optional<Dimension> imageSize = imageInformation.getSize();
		if (imageSize.isPresent()) {
			fullImageRectangle = new Rectangle(imageSize.get());
		} else {
			throw new UnsupportedFormatException(String.format("Invalid size for image instance %s", idImage));
		}
		return true;
	}

	public Image getImageInformation() {
		return this.imageInformation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see icy.sequence.SequenceIdImporter#close()
	 */
	@Override
	public void close() throws IOException {
		this.imageInformation = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see icy.image.ImageProvider#getTileWidth(int)
	 */
	@Override
	public int getTileWidth(int series) throws UnsupportedFormatException, IOException {
		validateImageInformation();
		return defaultTileSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see icy.image.ImageProvider#getTileHeight(int)
	 */
	@Override
	public int getTileHeight(int series) throws UnsupportedFormatException, IOException {
		validateImageInformation();
		return defaultTileSize;
	}

	private void validateImageInformation() throws IOException {
		if (getImageInformation() == null)
			throw new IOException("No opened image");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see icy.image.ImageProvider#getThumbnail(int)
	 */
	@Override
	public IcyBufferedImage getThumbnail(int series) throws UnsupportedFormatException, IOException {
		validateImageInformation();
		try {
			BufferedImage bImage = getImageInformation().getThumbnail(defaultThumbnailSize);
			return (bImage == null) ? null : IcyBufferedImage.createFrom(bImage);
		} catch (CytomineClientException e) {
			throw new IOException("Exception downloading image", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see icy.image.ImageProvider#getOMEXMLMetaData()
	 */
	@Override
	public OMEXMLMetadata getOMEXMLMetaData() throws UnsupportedFormatException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see icy.image.ImageProvider#getMetaData()
	 */
	@Override
	public OMEXMLMetadataImpl getMetaData() throws UnsupportedFormatException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see icy.image.ImageProvider#isResolutionAvailable(int, int)
	 */
	@Override
	public boolean isResolutionAvailable(int series, int resolution) throws UnsupportedFormatException, IOException {
		validateImageInformation();
		return 0 <= resolution && resolution < getImageInformation().getDepth().orElse(0L);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see icy.image.ImageProvider#getPixels(int, int, java.awt.Rectangle, int,
	 * int, int)
	 */
	@Override
	public Object getPixels(int series, int resolution, Rectangle rectangle, int z, int t, int c)
			throws UnsupportedFormatException, IOException {
		IcyBufferedImage image = getImage(series, resolution, rectangle, z, t, c);
		return image.getDataXY(c);
	}

	private void setProgress(int progress, int goal) {
		if (progressListener != null)
			progressListener.notifyProgress(progress, goal);
	}

	/**
	 * Finds the region of the given rectangle that intersects the full image
	 * region at full resolution.
	 * 
	 * @param rectangle
	 *          Region to intersect with full resolution image region.
	 * @return Intersection of the given region with that of the opened image at
	 *         resolution 0.
	 */
	private Rectangle getRectangleInImage(Rectangle rectangle) {
		if (rectangle == null) {
			rectangle = fullImageRectangle;
		}
		return rectangle.intersection(fullImageRectangle);
	}

	private void validateResolution(int resolution)
			throws IllegalArgumentException, IOException, UnsupportedFormatException {
		boolean resolutionAvailable = false;
		resolutionAvailable = isResolutionAvailable(0, resolution);
		if (!resolutionAvailable)
			throw new IllegalArgumentException(
					"resolution " + resolution + " out of bounds [0-" + (imageInformation.getDepth().orElse(0L)) + "]");
	}

	private Rectangle originalRectangleToResolutionRectangle(Rectangle rectangle, int resolution) {
		final Rectangle result = new Rectangle(rectangle);
		IntStream.range(0, resolution).forEach(r -> {
			result.x /= 2;
			result.y /= 2;
			result.width /= 2;
			result.height /= 2;
		});

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see icy.image.ImageProvider#getImage(int, int, java.awt.Rectangle, int,
	 * int)
	 */
	@Override
	public IcyBufferedImage getImage(int series, int resolution, Rectangle rectangle, int z, int t)
			throws UnsupportedFormatException, IOException, IllegalArgumentException {
		// Use the specified image model
		validateImageInformation();
		Image imageData = getImageInformation();
		validateResolution(resolution);
		rectangle = getRectangleInImage(rectangle);

		// Get rectangle in selected resolution
		Rectangle rectangleInResolution = originalRectangleToResolutionRectangle(rectangle, resolution);

		// Compute number of tiles to retrieve
		final Rectangle tileRectangle = new Rectangle(rectangleInResolution.x / getTileWidth(0),
				rectangleInResolution.y / getTileHeight(0), 1, 1);
		tileRectangle.width = ((int) rectangleInResolution.getMaxX()) / getTileWidth(0);
		if (((int) rectangleInResolution.getMaxX()) % getTileWidth(0) > 0)
			tileRectangle.width++;
		tileRectangle.width -= tileRectangle.x;
		tileRectangle.height = ((int) rectangleInResolution.getMaxY()) / getTileHeight(0);
		if (((int) rectangleInResolution.getMaxY()) % getTileHeight(0) > 0)
			tileRectangle.height++;
		tileRectangle.height -= tileRectangle.y;
		final Dimension pixelOffset = new Dimension(rectangleInResolution.x % getTileWidth(0),
				rectangleInResolution.y % getTileHeight(0));

		final int totalTiles = tileRectangle.width * tileRectangle.height;
		AtomicInteger processedTiles = new AtomicInteger(0);
		setProgress(processedTiles.get(), totalTiles);

		BufferedImage retrievedBImage = new BufferedImage(rectangleInResolution.width, rectangleInResolution.height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D retrievedGraphics = retrievedBImage.createGraphics();
		int threadNumber = Math.min(Runtime.getRuntime().availableProcessors(),
				(tileRectangle.width * tileRectangle.height));
		ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadNumber);
		ExecutorCompletionService<Void> poolCS = new ExecutorCompletionService<>(pool);
		// Start all tasks
		for (int i = 0, x = tileRectangle.x; i < tileRectangle.width; i++, x++) {
			for (int j = 0, y = tileRectangle.y; j < tileRectangle.height; j++, y++) {
				final Rectangle tile = new Rectangle(x, y, getTileWidth(0), getTileHeight(0));
				poolCS.submit(() -> {
					String url = imageData.getTileUrl(resolution, tile.x, tile.y).get();
					BufferedImage tileBI = null;
					// Retry to get hopefully a good image
					int retry = 0;
					while (tileBI == null && retry < maxRetrievalRetries) {
						retry++;
						tileBI = client.downloadPictureAsBufferedImage(url, "ndpi");
					}
					if (tileBI == null)
						throw new IOException("Image could not be retrieved " + tile);

					// Draw to full image
					retrievedGraphics.drawImage(tileBI, null, (tile.x - tileRectangle.x) * getTileWidth(0) - pixelOffset.width,
							(tile.y - tileRectangle.y) * getTileWidth(0) - pixelOffset.height);
					setProgress(processedTiles.addAndGet(1), totalTiles);
					return null;
				});
			}
		}
		pool.shutdown();

		// Check every thread completed ok
		try {
			for (int i = 0; i < tileRectangle.width; i++) {
				for (int j = 0; j < tileRectangle.height; j++) {
					poolCS.take().get();
				}
			}
		} catch (InterruptedException | ExecutionException e) {
			throw new IOException(e);
		} finally {
			retrievedGraphics.dispose();
		}
		IcyBufferedImage IBImage = IcyBufferedImage.createFrom(retrievedBImage);
		return IBImage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see icy.image.ImageProvider#getImage(int, int, int, int)
	 */
	@Override
	public IcyBufferedImage getImage(int series, int resolution, int z, int t)
			throws UnsupportedFormatException, IOException {
		return getImage(series, resolution, fullImageRectangle, z, t);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see icy.image.ImageProvider#getImage(int, int, java.awt.Rectangle, int,
	 * int, int)
	 */
	@Override
	public IcyBufferedImage getImage(int series, int resolution, Rectangle rectangle, int z, int t, int c)
			throws UnsupportedFormatException, IOException {
		IcyBufferedImage image = getImage(series, resolution, rectangle, z, t);
		return IcyBufferedImageUtil.extractChannel(image, c);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see icy.image.ImageProvider#getImage(int, int, int, int, int)
	 */
	@Override
	public IcyBufferedImage getImage(int series, int resolution, int z, int t, int c)
			throws UnsupportedFormatException, IOException {
		return getImage(series, resolution, fullImageRectangle, z, t, c);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see icy.image.ImageProvider#getImage(int, int, int)
	 */
	@Override
	public IcyBufferedImage getImage(int series, int z, int t) throws UnsupportedFormatException, IOException {
		return getImage(series, 0, fullImageRectangle, z, t);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see icy.image.ImageProvider#getImage(int, int)
	 */
	@Override
	public IcyBufferedImage getImage(int z, int t) throws UnsupportedFormatException, IOException {
		return getImage(0, 0, fullImageRectangle, z, t);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see icy.sequence.SequenceIdImporter#getOpened()
	 */
	@Override
	public String getOpened() {
		return imageInformation.getId().toString();
	}

}
