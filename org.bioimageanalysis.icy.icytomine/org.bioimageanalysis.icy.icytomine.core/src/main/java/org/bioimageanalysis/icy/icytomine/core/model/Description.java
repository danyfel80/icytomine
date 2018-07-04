package org.bioimageanalysis.icy.icytomine.core.model;

import java.util.Optional;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;
import org.bioimageanalysis.icy.icytomine.core.model.key.DescriptionId;

public class Description extends Entity {

	public static Description retrieve(CytomineClient client, long describedEntityId, String describedDomainName)
			throws CytomineClientException {
		return client.getDescription(describedEntityId, describedDomainName);
	}

	private DescriptionId descriptionId;

	public Description(CytomineClient client, be.cytomine.client.models.Description model) {
		super(client, model);
		this.descriptionId = new DescriptionId(model.getStr("domainClassName"), model.getLong("domainIdent"));
	}

	public be.cytomine.client.models.Description getInternalUser() {
		return (be.cytomine.client.models.Description) getModel();
	}

	public DescriptionId getDescriptionId() {
		return descriptionId;
	}

	public Optional<String> getData() {
		return getStr("data");
	}

	@Override
	public String getDomainName() {
		return new be.cytomine.client.models.Description().getDomainName();
	}

	@Override
	public String toString() {
		return String.format("Description: Id=%s, Data=%s", String.valueOf(getId()), String.valueOf(getData()));
	}
}
