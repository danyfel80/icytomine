package org.bioimageanalysis.icy.icytomine.ui.general;

public class JCheckableItem<T> {
	public final T object;
	public final String label;
	private boolean selected;

	public JCheckableItem(T object, String label, boolean selected) {
		this.object = object;
		this.label = label;
		this.selected = selected;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public T getObject() {
		return object;
	}

	@Override
	public String toString() {
		return label;
	}
}
