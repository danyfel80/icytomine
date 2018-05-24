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
	
	private static final ImageInstance INTERNAL_NO_IMAGE = new ImageInstance();
	{
		INTERNAL_NO_IMAGE.set("id", 0L);
	}
	
	public static Image getNoImage(Cytomine client) {
		return new Image(INTERNAL_NO_IMAGE, client);
	}

	private static int defaultTileSize = 256;

	private ImageInstance internalImage;
	private Cytomine cytomine;

	private String originalFilename;
	private List<String> servers;


	public Image(ImageInstance internalImage, Cytomine cytomine) {
		this.internalImage = internalImage;
		this.cytomine = cytomine;
	}

	/**
	 * @return Internal Cytomine image object.
	 */
	public ImageInstance getInternalImage() {
		return this.internalImage;
	}

	/**
	 * @return Cytomine client.
	 */
	public Cytomine getClient() {
		return this.cytomine;
	}

	/**
	 * @return Image ID.
	 */
	public Long getId() {
		return getInternalImage().getId();
	}

	/**
	 * @return ID of the abstract image this image is associated with.
	 */
	public Long getAbstractImageId() {
		return getInternalImage().getLong("baseImage");
	}

	/**
	 * @return Name of this image.
	 */
	public String getName() {
		if (originalFilename == null) {
			originalFilename = getInternalImage().getStr("originalFilename");
			originalFilename = CytomineUtils.convertFromSystenEncodingToUTF8(originalFilename);
		}
		return originalFilename;
	}

	/**
	 * @return Magnification used when capturing this image.
	 */
	public Integer getMagnification() {
		return getInternalImage().getInt("magnification");
	}

	/**
	 * @return Resolution of each pixel in x and y directions expressed in
	 *         microns.
	 */
	public Double getResolution() {
		return getInternalImage().getDbl("resolution");
	}

	/**
	 * @return The maximum resolution that can be requested.
	 */
	public Long getDepth() {
		return getInternalImage().getLong("depth");
	}

	/**
	 * @return Size of the image expressed in pixels in x direction.
	 */
	public Integer getSizeX() {
		return getInternalImage().getInt("width");
	}

	/**
	 * @return Size of the image expressed in pixels in y direction.
	 */
	public Integer getSizeY() {
		return getInternalImage().getInt("height");
	}

	/**
	 * @return Size of the image expressed in pixels in x and y directions.
	 */
	public Dimension getSize() {
		return new Dimension(getSizeX(), getSizeY());
	}

	/**
	 * @return Size of the image expressed in microns in x direction.
	 */
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

	/**
	 * @return Size of the image expressed in microns in y direction.
	 */
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

	/**
	 * @return Size of the tile in x direction.
	 */
	public int getTileWidth() {
		return defaultTileSize;
	}

	/**
	 * @return Size of the tile in y direction.
	 */
	public int getTileHeight() {
		return defaultTileSize;
	}

	/**
	 * @return Size of the tile in x and y directions.
	 */
	public Dimension getTileSize() {
		return new Dimension(getTileWidth(), getTileHeight());
	}

	/**
	 * @return Number of annotations users have associated to this image.
	 */
	public Long getAnnotationsUser() {
		return getInternalImage().getLong("numberOfAnnotations");
	}

	/**
	 * @return Number of annotations associated to this image produced by
	 *         algorithms.
	 */
	public Long getAnnotationsAlgo() {
		return getInternalImage().getLong("numberOfJobAnnotations");
	}

	/**
	 * @return Date of creation for this image.
	 */
	public Calendar getCreationDate() {
		Long date = getInternalImage().getLong("created");
		if (date == null)
			return null;
		Calendar c = GregorianCalendar.getInstance();
		c.setTimeInMillis(date);
		return c;
	}

	/**
	 * @return Format used to store this image.
	 */
	public String getMimeType() {
		return getInternalImage().getStr("mime");
	}

	/**
	 * Retrieves the URL used to retrieve a given tile at a given resolution.
	 * 
	 * @param resolution
	 *          Resolution of the image to retrieve.
	 * @param x
	 *          Tile index in x direction.
	 * @param y
	 *          Tile index in y direction.
	 * @return URL used to retrieve the tile from the server.
	 * @throws CytomineException
	 *           If no image server has been declared for this image.
	 */
	public String getUrl(long resolution, int x, int y) throws CytomineException {
		List<String> servers = getImageServers();
		if (servers.size() == 0)
			throw new CytomineException(404, "No image server declared");

		return String.format("%s&z=%d&x=%d&y=%d&mimeType=%s", servers.get(0), getDepth() - resolution, x, y, getMimeType());
	}

	/**
	 * Retrieves a thumbnail of this image.
	 * 
	 * @param maxSize
	 *          Maximum size of the retrieved thumbnail.
	 * @return Thumbnail.
	 * @throws CytomineException
	 *           If the thumbnail fails to be downloaded.
	 */
	public BufferedImage getThumbnail(int maxSize) throws CytomineException {
		return getClient().downloadAbstractImageAsBufferedImage(getAbstractImageId(), maxSize);
	}

	/**
	 * Annotations associated to this image.
	 * 
	 * @return Annotation collection for this image.
	 * @throws CytomineException
	 *           If annotations for this image cannot be retrieved from the
	 *           server.
	 */
	public List<Annotation> getAnnotations() throws CytomineException {
		AnnotationCollection annotationsNative = getClient().getAnnotationsByImage(getInternalImage().getId());
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

	/**
	 * @return Collection with image servers available for this image.
	 * @throws CytomineException
	 *           If servers cannot be retrieved from the main server.
	 */
	public List<String> getImageServers() throws CytomineException {
		if (servers == null) {
			servers = getClient().getImageInstanceServers(getInternalImage()).getServerList();
		}
		return servers;
	}

	/**
	 * @return Id of the project this image is associated with.
	 */
	public Long getProjectId() {
		return getInternalImage().getLong("project");
	}

	/**
	 * @return The project this image is associated with.
	 * @throws CytomineException
	 *           If the project cannot be retreived from the server.
	 */
	public Project getProject() throws CytomineException {
		return new Project(getClient().getProject(getProjectId()), getClient());
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;

		if (getClient() != null) {
			result = prime * result + (getClient().getHost() == null ? 0 : getClient().getHost().hashCode());
			result = prime * result + (getClient().getPublicKey() == null ? 0 : getClient().getPublicKey().hashCode());
			result = prime * result + (getClient().getPrivateKey() == null ? 0 : getClient().getPrivateKey().hashCode());
		} else {
			result = prime * result;
		}
		result = prime * result + ((getInternalImage() == null) ? 0 : getInternalImage().getId().hashCode());
		return result;
	}

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
		if (getClient() == null) {
			if (other.getClient() != null) {
				return false;
			}
		} else if (!getClient().getHost().equals(other.getClient().getHost())
				|| !getClient().getPublicKey().equals(other.getClient().getPublicKey())
				|| !getClient().getPrivateKey().equals(other.getClient().getPrivateKey())) {
			return false;
		}
		if (getInternalImage() == null) {
			if (other.getInternalImage() != null) {
				return false;
			}
		} else if (!getInternalImage().equals(other.getInternalImage())) {
			return false;
		}
		return true;
	}

	public List<User> getAnnotationUsers() throws CytomineException, InterruptedException, ExecutionException {
		List<Annotation> annotations = getAnnotations();

		ThreadPoolExecutor tp = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
		ExecutorCompletionService<User> cs = new ExecutorCompletionService<>(tp);

		int userCount = (int) (annotations.stream().map(annotation -> annotation.getUserId())
				.filter(userId -> userId != null).distinct()
				.map((Long userId) -> cs.submit(() -> new User(getClient().getUser(userId.longValue())))).count());

		tp.shutdown();

		List<User> users = new ArrayList<>(userCount);
		for (long i = 0; i < userCount; i++) {
			if (Thread.interrupted())
				throw new InterruptedException();

			users.add(cs.take().get());
		}

		return users;
	}

	public List<Term> getAvailableTerms() throws CytomineException {
		return getProject().getAvailableTerms();
	}

}
