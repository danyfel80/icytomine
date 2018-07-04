package org.bioimageanalysis.icy.icytomine.core.view;

@SuppressWarnings("serial")
public class AnnotationViewException extends RuntimeException {

	public AnnotationViewException(String message) {
		super(message);
	}

	public AnnotationViewException(Throwable cause) {
		super(cause);
	}

	public AnnotationViewException(String message, Throwable cause) {
		super(message, cause);
	}
}
