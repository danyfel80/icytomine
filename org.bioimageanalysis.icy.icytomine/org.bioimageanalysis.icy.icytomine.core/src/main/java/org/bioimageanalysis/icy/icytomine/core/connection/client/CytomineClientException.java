package org.bioimageanalysis.icy.icytomine.core.connection.client;

@SuppressWarnings("serial")
public class CytomineClientException extends RuntimeException {
	public CytomineClientException(String message) {
		super(message);
	}

	public CytomineClientException(Throwable cause) {
		super(cause);
	}

	public CytomineClientException(String message, Throwable cause) {
		super(message, cause);
	}
}
