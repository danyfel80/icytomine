package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.filter;

import java.awt.event.ActionEvent;
import java.util.Set;

import org.bioimageanalysis.icy.icytomine.core.model.Term;
import org.bioimageanalysis.icy.icytomine.core.model.filters.AnnotationFilter;
import org.bioimageanalysis.icy.icytomine.core.model.filters.AnnotationFilter.ComputationMode;
import org.bioimageanalysis.icy.icytomine.core.model.filters.AnnotationFilterByTerm;
import org.bioimageanalysis.icy.icytomine.ui.general.JCheckableItem;
import org.bioimageanalysis.icy.icytomine.ui.general.JCheckedComboBox;

@SuppressWarnings("serial")
public class AnnotationFilterByTermPanel extends AnnotationFilterPanel<Term> {

	AnnotationFilterByTerm termFilter;

	public AnnotationFilterByTermPanel() {
		setLabelText("Terms:");
		termFilter = new AnnotationFilterByTerm();
		setFilter(termFilter);
	}

	public void setPreviousFilter(AnnotationFilter previousFilter) {
		termFilter.setPreviousFilter(previousFilter);
	}

	public void setTerms(Set<Term> terms) {
		termFilter.setActiveTerms(terms);
		setModel(terms.toArray(new Term[0]), Term::getName);
	}

	@Override
	protected void choiceChanged(ActionEvent e) {
		@SuppressWarnings("unchecked")
		JCheckableItem<Term> item = ((JCheckableItem<Term>) (((JCheckedComboBox<Term>) (e.getSource())).getSelectedItem()));
		Term term = item.object;
		if (!item.isSelected()) {
			termFilter.getActiveTerms().add(term);
		} else {
			termFilter.getActiveTerms().remove(term);
		}
		termFilter.computeActiveAnnotations(ComputationMode.RECOMPUTE_JUST_THIS);
	}

}
