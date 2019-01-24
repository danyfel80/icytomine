package org.bioimageanalysis.icy.icytomine.core.model;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;

import be.cytomine.client.models.ImageInstance;

public class Image extends Entity {

	private static final int DEFAULT_TILE_SIZE = 256;

	public static Image retrieve(CytomineClient client, long imageInstanceId) throws CytomineClientException {
		return client.getImageInstance(imageInstanceId);
	}

	private List<String> imageServers;
	private List<Annotation> annotations;

	public Image(CytomineClient client, ImageInstance internalImage) {
		super(client, internalImage);
	}

	public ImageInstance getInternalImage() {
		return (ImageInstance) getModel();
	}

	/**
	 * @return Date of creation for this image.
	 */
	public Optional<Calendar> getCreationDate() {
		Optional<Long> numericDate = getLong("created");
		if (numericDate.isPresent()) {
			Calendar c = GregorianCalendar.getInstance();
			c.setTimeInMillis(numericDate.get());
			return Optional.of(c);
		} else {
			return Optional.ofNullable(null);
		}
	}

	public Optional<Long> getAbstractImageId() {
		return getLong("baseImage");
	}

	public Optional<Long> getProjectId() {
		return getLong("project");
	}

	public Optional<String> getName() {
		return getStr("originalFilename");
	}

	/**
	 * @return Format used to store this image.
	 */
	public Optional<String> getMimeType() {
		return getStr("mime");
	}

	/**
	 * @return Size of the image expressed in pixels in x direction.
	 */
	public Optional<Integer> getSizeX() {
		return getInt("width");
	}

	/**
	 * @return Size of the image expressed in pixels in y direction.
	 */
	public Optional<Integer> getSizeY() {
		return getInt("height");
	}

	/**
	 * @return Size of the image expressed in pixels in x and y directions.
	 */
	public Optional<Dimension> getSize() {
		if (getSizeX().isPresent() && getSizeY().isPresent()) {
			return Optional.of(new Dimension(getSizeX().get(), getSizeY().get()));
		} else {
			return Optional.ofNullable(null);
		}
	}

	/**
	 * @return Resolution of each pixel in x and y directions expressed in
	 *         microns.
	 */
	public Optional<Double> getResolution() {
		return super.getDbl("resolution");
	}

	/**
	 * @return Size of the image expressed in microns in x direction.
	 */
	public Optional<Double> getDimensionX() {
		Optional<Double> resolution = getResolution();
		Optional<Integer> size = getSizeX();
		if (size.isPresent() && resolution.isPresent()) {
			return Optional.of(resolution.get() * size.get());
		} else {
			return Optional.ofNullable(null);
		}
	}

	/**
	 * @return Size of the image expressed in microns in y direction.
	 */
	public Optional<Double> getDimensionY() {
		Optional<Double> resolution = getResolution();
		Optional<Integer> size = getSizeY();
		if (size.isPresent() && resolution.isPresent()) {
			return Optional.of(resolution.get() * size.get());
		} else {
			return Optional.ofNullable(null);
		}
	}

	/**
	 * @return Size of the image expressed in microns in x and y directions.
	 */
	public Optional<Dimension2D> getDimension() {
		Optional<Dimension> size = getSize();
		Optional<Double> resolution = getResolution();
		if (size.isPresent() && resolution.isPresent()) {
			return Optional.of(new icy.type.dimension.Dimension2D.Double(size.get().width * resolution.get(),
					size.get().height * resolution.get()));
		} else {
			return Optional.ofNullable(null);
		}
	}

	/**
	 * @return Magnification used when capturing this image.
	 */
	public Optional<Integer> getMagnification() {
		return getInt("magnification");
	}

	/**
	 * @return The maximum resolution that can be requested.
	 */
	public Optional<Long> getDepth() {
		return getLong("depth");
	}

	/**
	 * @return Size of the tile in x direction.
	 */
	public Optional<Integer> getTileWidth() {
		return Optional.of(DEFAULT_TILE_SIZE);
	}

	/**
	 * @return Size of the tile in y direction.
	 */
	public Optional<Integer> getTileHeight() {
		return Optional.of(DEFAULT_TILE_SIZE);
	}

	/**
	 * @return Size of the tile in x and y directions.
	 */
	public Optional<Dimension> getTileSize() {
		if (getTileWidth().isPresent() && getTileHeight().isPresent()) {
			return Optional.of(new Dimension(getTileWidth().get(), getTileHeight().get()));
		} else {
			return Optional.ofNullable(null);
		}

	}

	/**
	 * @return Id of the user who uploaded the image.
	 */
	public Long getOriginalUserId() {
		return getLong("user").get();
	}

	/**
	 * @return Number of annotations users have associated to this image.
	 */
	public Optional<Long> getAnnotationsOfUsersNumber() {
		return getLong("numberOfAnnotations");
	}

	/**
	 * @return Number of annotations associated to this image produced by
	 *         algorithms.
	 */
	public Optional<Long> getAnnotationsOfAlgorithmNumber() {
		return getLong("numberOfJobAnnotations");
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
	 * @throws CytomineClientException
	 *           If image servers for this image cannot be retrieved.
	 */
	public Optional<String> getTileUrl(long resolution, int x, int y) throws CytomineClientException {
		List<String> servers = getImageServers(false);
		if (servers.isEmpty()) {
			return Optional.ofNullable(null);
		}
		return Optional.of(String.format("%s&z=%d&x=%d&y=%d&mimeType=%s", servers.get(0),
				getDepth().orElse(0L) - resolution, x, y, getMimeType().orElse("ndpi")));
	}

	/**
	 * @return Collection with image servers available for this image.
	 * @throws CytomineClientException
	 *           If the image servers cannot be retrieved from the server.
	 */
	public List<String> getImageServers(boolean recompute) throws CytomineClientException {
		if (imageServers == null || recompute) {
			imageServers = null;
			imageServers = getClient().getImageServers(this);
		}
		return imageServers;
	}

	/**
	 * Retrieves a thumbnail of this image.
	 * 
	 * @param maxSize
	 *          Maximum size of the retrieved thumbnail.
	 * @return Thumbnail.
	 * @throws CytomineClientException
	 *           If the thumbnail cannot be retrieved from the server.
	 */
	public BufferedImage getThumbnail(int maxSize) throws CytomineClientException {
		return getClient().downloadImageAsBufferedImage(getAbstractImageId().get(), maxSize);
	}

	/**
	 * Annotations associated to this image.
	 * 
	 * @return Annotation collection for this image.
	 * @throws CytomineClientException
	 *           If annotations for this image cannot be retrieved from the
	 *           server.
	 */
	public List<Annotation> getAnnotations(boolean recompute) throws CytomineClientException {
		if (annotations == null || recompute) {
			annotations = null;
			annotations = getClient().getImageAnnotations(getId());
		}
		return annotations;
	}

	/**
	 * Annotations associated to this image containing geometric information.
	 * 
	 * @return Annotation collection for this image.
	 * @throws CytomineClientException
	 *           If full annotations for this image cannot be retrieved from the
	 *           server.
	 */
	public List<Annotation> getAnnotationsWithGeometry(boolean recompute) throws CytomineClientException {
		long annotationsWithoutGeometry = 0;
		if (annotations != null) {
			annotationsWithoutGeometry = annotations.stream().filter(a -> !a.getLocation().isPresent()).count();
		}

		if (annotations == null || recompute || annotationsWithoutGeometry > 0) {
			annotations = null;
			annotations = getClient().getFullImageAnnotations(getId());
		}
		return annotations;
	}

	public List<Annotation> getAnnotationsWithGeometryOf(Rectangle2D currentTileArea) throws CytomineClientException {
		return getClient().getFullImageAnnotations(getId(), currentTileArea);
	}

	public Project getProject() throws CytomineClientException {
		return getClient().getProject(getProjectId().get());
	}

	public Set<User> getAnnotationUsers() {
		List<Annotation> annotations = getAnnotations(false);
		return annotations.stream().map(a -> a.getUser()).collect(Collectors.toSet());
	}

	public void removeAnnotations(Set<Annotation> selectedAnnotations) throws CytomineClientException {
		for (Annotation annotation: selectedAnnotations) {
			getClient().removeAnnotation(annotation.getId());
		}
		Set<Long> selectedAnnotationIds = selectedAnnotations.stream().map(a -> a.getId()).collect(Collectors.toSet());
		if (annotations != null) {
			@SuppressWarnings("unused")
			boolean removed = annotations.removeIf(annotation -> selectedAnnotationIds.contains(annotation.getId()));
		}
	}

	public void setMagnification(Integer newMagnification) throws CytomineClientException {
		getClient().updateImageMagnfication(this, newMagnification);
	}

	public void setResolution(Double newResolution) throws CytomineClientException {
		getClient().updateImageResolution(this, newResolution);
	}

	@Override
	public String toString() {
		return String.format("Image instance: id=%s, name=%s", String.valueOf(getId()), getName().orElse("Not specified"));
	}
}
