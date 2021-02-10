/*
 * Copyright 2010-2016 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bioimageanalysis.icy.icytomine.core.connection.persistence;

/**
 * This class holds the credentials of a Cytomine user. It is used to connect to
 * the Cytomine server.
 * 
 * @author Daniel Felipe Gonzalez Obando
 */
public class UserCredential {

	private String privateKey;
	private String publicKey;

	/**
	 * @return The private key used to connect to the host server
	 */
	public String getPrivateKey() {
		return privateKey;
	}

	/**
	 * @param privateKey
	 *          The private key to set
	 */
	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	/**
	 * @return the public key used to connect to the host server
	 */
	public String getPublicKey() {
		return publicKey;
	}

	/**
	 * @param publicKey
	 *          the public key to set
	 */
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	@Override
	public String toString() {
		return String.format("CytomineUserCredentials [privateKey=%s, publicKey=%s]", privateKey, publicKey);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((privateKey == null) ? 0 : privateKey.hashCode());
		result = prime * result + ((publicKey == null) ? 0 : publicKey.hashCode());
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
		if (!(obj instanceof UserCredential)) {
			return false;
		}
		UserCredential other = (UserCredential) obj;
		if (privateKey == null) {
			if (other.privateKey != null) {
				return false;
			}
		} else if (!privateKey.equals(other.privateKey)) {
			return false;
		}
		if (publicKey == null) {
			if (other.publicKey != null) {
				return false;
			}
		} else if (!publicKey.equals(other.publicKey)) {
			return false;
		}
		return true;
	}

}
