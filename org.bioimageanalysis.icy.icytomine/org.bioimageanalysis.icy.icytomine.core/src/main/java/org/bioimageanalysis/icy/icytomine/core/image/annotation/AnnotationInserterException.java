package org.bioimageanalysis.icy.icytomine.core.image.annotation;

@SuppressWarnings("serial")
public class AnnotationInserterException extends RuntimeException {
	public AnnotationInserterException(String message) {
		super(message);
	}

	public AnnotationInserterException(Throwable cause) {
		super(cause);
	}

	public AnnotationInserterException(String message, Throwable cause) {
		super(message, cause);
	}
}
