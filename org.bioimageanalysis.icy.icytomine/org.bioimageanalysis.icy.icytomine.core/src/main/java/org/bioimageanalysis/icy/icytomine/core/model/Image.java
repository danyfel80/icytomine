package org.bioimageanalysis.icy.icytomine.core.model;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import be.cytomine.client.Cytomine;
import be.cytomine.client.CytomineException;
import be.cytomine.client.collections.AnnotationCollection;
import be.cytomine.client.models.ImageInstance;

public class Image {

	private static int defaultTileSize = 256;

	private ImageInstance internalImage;
	private Cytomine cytomine;

	private List<String> servers;

	public Image(ImageInstance internalImage, Cytomine cytomine) {
		this.internalImage = internalImage;
		this.cytomine = cytomine;
	}

	public Cytomine getClient() {
		return this.cytomine;
	}

	public Long getId() {
		return internalImage.getId();
	}

	public Long getAbstractImageId() {
		return internalImage.getLong("baseImage");
	}

	public String getName() {
		return internalImage.getStr("originalFilename");
	}

	public Integer getMagnification() {
		return internalImage.getInt("magnification");
	}

	public Double getResolution() {
		return internalImage.getDbl("resolution");
	}

	/**
	 * @return The maximum resolution that can be requested.
	 */
	public Long getDepth() {
		return internalImage.getLong("depth");
	}

	public Integer getSizeX() {
		return internalImage.getInt("width");
	}

	public Integer getSizeY() {
		return internalImage.getInt("height");
	}

	public Dimension getSize() {
		return new Dimension(getSizeX(), getSizeY());
	}

	public Double getDimensionX() {
		Double resolution = getResolution();
		Integer size = getSizeX();
		if (resolution == null) {
			return null;
		}
		if (size == null)
			return null;
		return resolution * size;
	}

	public Double getDimensionY() {
		Double resolution = getResolution();
		Integer size = getSizeY();
		if (resolution == null) {
			resolution = 1d;
		}
		if (size == null)
			return null;
		return resolution * size;
	}

	public int getTileWidth() {
		return defaultTileSize;
	}

	public int getTileHeight() {
		return defaultTileSize;
	}

	public Dimension getTileSize() {
		return new Dimension(getTileWidth(), getTileHeight());
	}

	public Long getAnnotationsUser() {
		return internalImage.getLong("numberOfAnnotations");
	}

	public Long getAnnotationsAlgo() {
		return internalImage.getLong("numberOfJobAnnotations");
	}

	public Calendar getCreationDate() {
		Long date = internalImage.getLong("created");
		if (date == null)
			return null;
		Calendar c = GregorianCalendar.getInstance();
		c.setTimeInMillis(date);
		return c;
	}

	public String getMimeType() {
		return internalImage.getStr("mime");
	}

	public String getUrl(long resolution, int x, int y) throws CytomineException {
		List<String> servers = getImageServers();
		if (servers.size() == 0)
			throw new CytomineException(404, "No image server declared");

		return String.format("%s&z=%d&x=%d&y=%d&mimeType=%s", servers.get(0), getDepth() - resolution, x, y, getMimeType());
	}

	public BufferedImage getThumbnail(int maxSize) throws CytomineException {
		return getClient().downloadAbstractImageAsBufferedImage(getAbstractImageId(), maxSize);
	}

	public List<Annotation> getAnnotations() throws CytomineException {
		AnnotationCollection annotationsNative = getClient().getAnnotationsByImage(internalImage.getId());
		List<Annotation> annotations = new ArrayList<>();

		ThreadPoolExecutor tp = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
		ExecutorCompletionService<be.cytomine.client.models.Annotation> cs = new ExecutorCompletionService<>(tp);

		for (int i = 0; i < annotationsNative.size(); i++) {
			final int idx = i;
			cs.submit(() -> {
				return getClient().getAnnotation(annotationsNative.get(idx).getId());
			});
		}
		tp.shutdown();

		for (int i = 0; i < annotationsNative.size(); i++) {
			try {
				annotations.add(new Annotation(cs.take().get(), this, getClient()));
			} catch (InterruptedException | ExecutionException e) {
				throw new CytomineException(e);
			}
		}

		return annotations;
	}

	public String toString() {
		return getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;

		if (cytomine != null) {
			result = prime * result + (cytomine.getHost() == null ? 0 : cytomine.getHost().hashCode());
			result = prime * result + (cytomine.getPublicKey() == null ? 0 : cytomine.getPublicKey().hashCode());
			result = prime * result + (cytomine.getPrivateKey() == null ? 0 : cytomine.getPrivateKey().hashCode());
		} else {
			result = prime * result;
		}
		result = prime * result + ((internalImage == null) ? 0 : internalImage.getId().hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Image)) {
			return false;
		}
		Image other = (Image) obj;
		if (cytomine == null) {
			if (other.cytomine != null) {
				return false;
			}
		} else if (!cytomine.getHost().equals(other.cytomine.getHost())
				|| !cytomine.getPublicKey().equals(other.cytomine.getPublicKey())
				|| !cytomine.getPrivateKey().equals(other.cytomine.getPrivateKey())) {
			return false;
		}
		if (internalImage == null) {
			if (other.internalImage != null) {
				return false;
			}
		} else if (!internalImage.equals(other.internalImage)) {
			return false;
		}
		return true;
	}

	public List<String> getImageServers() throws CytomineException {
		if (servers == null) {
			servers = cytomine.getImageInstanceServers(internalImage).getServerList();
		}
		return servers;
	}

}
