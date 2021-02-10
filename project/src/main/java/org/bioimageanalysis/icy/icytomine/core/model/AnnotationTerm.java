package org.bioimageanalysis.icy.icytomine.core.model;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;

public class AnnotationTerm extends Entity {
	
	public AnnotationTerm(CytomineClient client, be.cytomine.client.models.AnnotationTerm internalAnnotationTerm) {
		super(client, internalAnnotationTerm);
	}

	public be.cytomine.client.models.AnnotationTerm getInternalAnnotationTerm() {
		return (be.cytomine.client.models.AnnotationTerm) getModel();
	}

	public Optional<Long> getTermId() {
		return getLong("term");
	}

	public Optional<Long> getUserId() {
		return getLong("user");
	}

	public Term getTerm() throws CytomineClientException, NoSuchElementException {
		Optional<Long> termId = getTermId();
		return getClient().getTerm(termId.get());
	}

	public User getUser() throws CytomineClientException {
		Optional<Long> userId = getUserId();
		return getClient().getUser(userId.get());
	}

}
