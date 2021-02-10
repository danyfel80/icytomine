package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.icy2Cytomine.sequence;

import icy.main.Icy;
import icy.sequence.Sequence;

public class SequenceItem {

	private Sequence sequence;

	public SequenceItem(Sequence sequence) {
		super();
		this.sequence = sequence;
	}

	public Sequence getSequence() {
		if (sequence == null)
			return Icy.getMainInterface().getActiveSequence();
		return sequence;
	}

	@Override
	public String toString() {
		if (sequence == null)
			return "Active sequence";
		return sequence.getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sequence == null) ? 0 : sequence.hashCode());
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
		if (!(obj instanceof SequenceItem)) {
			return false;
		}
		SequenceItem other = (SequenceItem) obj;
		if (sequence == null) {
			if (other.sequence != null) {
				return false;
			}
		} else {
			if (other.sequence == null)
				return false;
			else if (sequence.hashCode() != (other.sequence.hashCode())) {
				return false;
			}
		}
		return true;
	}

}
