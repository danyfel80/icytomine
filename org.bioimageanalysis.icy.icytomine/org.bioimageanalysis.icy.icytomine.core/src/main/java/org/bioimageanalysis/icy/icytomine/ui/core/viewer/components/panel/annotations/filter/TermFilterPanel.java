package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.filter;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bioimageanalysis.icy.icytomine.core.model.Term;
import org.bioimageanalysis.icy.icytomine.ui.general.JCheckableItem;
import org.bioimageanalysis.icy.icytomine.ui.general.JCheckedComboBox;

@SuppressWarnings("serial")
public class TermFilterPanel extends FilterPanel<Term> {

	public interface TermSelectionListener {
		void termSelectionChange(Term term, boolean selected);
	}

	private Set<TermSelectionListener> termSelectionListeners;

	public TermFilterPanel(List<Term> terms) {
		super("Terms:");
		setModel(terms.toArray(new Term[0]), Term::getName);
		termSelectionListeners = new HashSet<>();
	}

	@Override
	protected void choiceChanged(ActionEvent e) {
		@SuppressWarnings("unchecked")
		JCheckableItem<Term> item = ((JCheckableItem<Term>) (((JCheckedComboBox<Term>) (e.getSource())).getSelectedItem()));
		notifyTermSelectionChange(item.getObject(), !item.isSelected());
	}

	private void notifyTermSelectionChange(Term term, boolean selected) {
		termSelectionListeners.forEach(l -> l.termSelectionChange(term, selected));
	}

	public void addTermSelectionListener(TermSelectionListener listener) {
		this.termSelectionListeners.add(listener);
	}

	public void removeTermSelectionListener(TermSelectionListener listener) {
		this.termSelectionListeners.remove(listener);
	}

}
