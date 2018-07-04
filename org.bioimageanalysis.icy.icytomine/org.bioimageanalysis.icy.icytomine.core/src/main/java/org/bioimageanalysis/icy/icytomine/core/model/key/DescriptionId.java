package org.bioimageanalysis.icy.icytomine.core.model.key;

public class DescriptionId {

	private String describedDomainName;
	private Long describedEntityId;

	public DescriptionId(String describedDomainName, Long describedEntityId) {
		this.describedDomainName = describedDomainName;
		this.describedEntityId = describedEntityId;
	}

	public String getDescribedDomainName() {
		return describedDomainName;
	}

	public Long getDescribedEntityId() {
		return describedEntityId;
	}

	@Override
	public String toString() {
		return String.format("Description for %s with id %s", describedDomainName, describedEntityId.toString());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((describedDomainName == null) ? 0 : describedDomainName.hashCode());
		result = prime * result + ((describedEntityId == null) ? 0 : describedEntityId.hashCode());
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
		if (!(obj instanceof DescriptionId)) {
			return false;
		}
		DescriptionId other = (DescriptionId) obj;
		if (describedDomainName == null) {
			if (other.describedDomainName != null) {
				return false;
			}
		} else if (!describedDomainName.equals(other.describedDomainName)) {
			return false;
		}
		if (describedEntityId == null) {
			if (other.describedEntityId != null) {
				return false;
			}
		} else if (!describedEntityId.equals(other.describedEntityId)) {
			return false;
		}
		return true;
	}

}
