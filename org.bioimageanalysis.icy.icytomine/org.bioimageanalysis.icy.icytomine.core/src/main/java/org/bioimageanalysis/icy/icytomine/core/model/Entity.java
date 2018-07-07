package org.bioimageanalysis.icy.icytomine.core.model;

import java.util.Optional;

import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClient;
import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientException;
import org.bioimageanalysis.icy.icytomine.core.connection.client.CytomineClientUtils;

import be.cytomine.client.models.Model;

public abstract class Entity {
	private CytomineClient client;
	private Model model;

	public Entity(CytomineClient client, Model model) {
		this.client = client;
		this.model = model;
	}

	public CytomineClient getClient() {
		return client;
	}

	public Model getModel() {
		return model;
	}

	protected void setModel(Model model) {
		this.model = model;
	}

	public Long getId() {
		return model.getId();
	}

	public Optional<String> getStr(String field) {
		Optional<String> value = Optional.ofNullable(getModel().getStr(field));
		if (value.isPresent()) {
			value = Optional.of(CytomineClientUtils.convertFromSystenEncodingToUTF8(value.get()));
		}
		return value;
	}

	public Optional<Long> getLong(String field) {
		return Optional.ofNullable(getModel().getLong(field));
	}

	public Optional<Integer> getInt(String field) {
		return Optional.ofNullable(getModel().getInt(field));
	}

	public Optional<Double> getDbl(String field) {
		return Optional.ofNullable(getModel().getDbl(field));
	}

	public String getDomainName() {
		return getModel().getDomainName();
	}

	public Description getDescription() throws CytomineClientException {
		return getClient().getDescription(getId(), getDomainName());
	}

	@Override
	public String toString() {
		return String.format("Entity (%s). Id: %s", getClass().getTypeName(), String.valueOf(getId()));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result;
		if (client != null) {
			result += client.hashCode();
		}
		result = prime * result;
		if (model != null && getId() != null) {
			result += getId().hashCode();
		}
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
		if (!(obj instanceof Entity)) {
			return false;
		}
		Entity other = (Entity) obj;
		return hashCode() == other.hashCode();
	}

}
