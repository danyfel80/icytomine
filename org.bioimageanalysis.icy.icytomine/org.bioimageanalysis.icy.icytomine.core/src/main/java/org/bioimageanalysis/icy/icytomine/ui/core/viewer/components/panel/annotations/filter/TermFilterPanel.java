package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.filter;

import java.util.List;

import org.bioimageanalysis.icy.icytomine.core.model.Term;

@SuppressWarnings("serial")
public class TermFilterPanel extends FilterPanel<Term> {

	public TermFilterPanel(List<Term> terms) {
		super("Terms:");
		setModel(terms.toArray(new Term[0]), Term::getName);
	}

}
