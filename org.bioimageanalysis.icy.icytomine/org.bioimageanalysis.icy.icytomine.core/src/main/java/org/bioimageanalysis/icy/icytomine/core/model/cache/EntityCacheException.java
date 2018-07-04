package org.bioimageanalysis.icy.icytomine.core.model.cache;

@SuppressWarnings("serial")
public class EntityCacheException extends RuntimeException {
	public EntityCacheException(String message) {
		super(message);
	}

	public EntityCacheException(Throwable cause) {
		super(cause);
	}

	public EntityCacheException(String message, Throwable cause) {
		super(message, cause);
	}
}
