package org.bioimageanalysis.icy.icytomine.core.model;

import java.util.Optional;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;

public class Property extends Entity {

	public Property(CytomineClient client, be.cytomine.client.models.Property internalProperty) {
		super(client, internalProperty);
	}

	public be.cytomine.client.models.Property getInternalProperty() {
		return (be.cytomine.client.models.Property) getModel();
	}

	public Optional<String> getKey() {
		return getStr("key");
	}

	public Optional<String> getValue() {
		return getStr("value");
	}

	@Override
	public String toString() {
		return String.format("Property (%s). key: %s, value: %s", String.valueOf(getId()), getKey().orElse("Unknown"),
				getValue().orElse("Unknown"));
	}

}
