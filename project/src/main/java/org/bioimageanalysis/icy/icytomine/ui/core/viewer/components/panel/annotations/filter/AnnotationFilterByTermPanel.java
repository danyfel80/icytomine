package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.filter;

import java.awt.event.ActionEvent;
import java.util.Set;
import java.util.stream.Collectors;

import org.bioimageanalysis.icy.icytomine.core.model.Term;
import org.bioimageanalysis.icy.icytomine.core.model.filters.AnnotationFilter;
import org.bioimageanalysis.icy.icytomine.core.model.filters.AnnotationFilter.ComputationMode;
import org.bioimageanalysis.icy.icytomine.core.model.filters.AnnotationFilterByTerm;
import org.bioimageanalysis.icy.icytomine.core.model.filters.AnnotationFilterByTerm.TermItem;
import org.bioimageanalysis.icy.icytomine.ui.general.JCheckableItem;
import org.bioimageanalysis.icy.icytomine.ui.general.JCheckedComboBox;

@SuppressWarnings("serial")
public class AnnotationFilterByTermPanel extends AnnotationFilterPanel<TermItem> {

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
		Set<TermItem> termItems = terms.stream().map(t -> new TermItem(t)).collect(Collectors.toSet());
		termItems.add(TermItem.NO_TERM);

		termFilter.setActiveTerms(termItems);
		setModel(termItems.toArray(new TermItem[termItems.size()]), item -> item.toString());
	}

	@Override
	protected void choiceChanged(ActionEvent e) {
		@SuppressWarnings("unchecked")
		JCheckableItem<TermItem> checkableItem = ((JCheckableItem<TermItem>) (((JCheckedComboBox<TermItem>) (e.getSource())).getSelectedItem()));
		TermItem termItem = checkableItem.object;
		if (!checkableItem.isSelected()) {
			termFilter.getActiveTerms().add(termItem);
		} else {
			termFilter.getActiveTerms().remove(termItem);
		}
		termFilter.computeActiveAnnotations(ComputationMode.RECOMPUTE_JUST_THIS);
	}

}
