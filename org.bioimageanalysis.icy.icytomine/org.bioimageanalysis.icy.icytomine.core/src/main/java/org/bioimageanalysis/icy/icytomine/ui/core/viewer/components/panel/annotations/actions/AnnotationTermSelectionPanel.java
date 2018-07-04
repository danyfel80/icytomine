package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.actions;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.bioimageanalysis.icy.icytomine.core.model.Term;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.actions.AnnotationTermSelectionPanelController.AnnotationTermSelectionCommitListener;
import org.bioimageanalysis.icy.icytomine.ui.general.JCheckedComboBox;

@SuppressWarnings("serial")
public class AnnotationTermSelectionPanel extends JPanel {

	static class TermItem {
		public static final TermItem NO_TERM = new TermItem(null);

		private Term term;

		public TermItem(Term term) {
			this.term = term;
		}

		public Term getTerm() {
			return term;
		}

		@Override
		public String toString() {
			if (term != null)
				return term.getName().orElse("Not specified");
			else
				return "No term";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((term == null) ? 0 : term.hashCode());
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
			if (!(obj instanceof TermItem)) {
				return false;
			}
			TermItem other = (TermItem) obj;
			if (term == null) {
				if (other.term != null) {
					return false;
				}
			} else if (!term.equals(other.term)) {
				return false;
			}
			return true;
		}

	}

	private JCheckedComboBox<TermItem> termComboBox;
	private JButton termSetButton;
	private AnnotationTermSelectionPanelController panelController;

	public AnnotationTermSelectionPanel() {
		setView();
		setController();
	}

	private void setView() {
		setGridBagLayout();
		addLabel();
		addTermComboBox();
		addTermSetButton();
	}

	private void setGridBagLayout() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 23, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, Double.MIN_VALUE };

		setLayout(gridBagLayout);
	}

	private void addLabel() {
		JLabel titleLabel = new JLabel("Associated terms");

		GridBagConstraints titleLabelConstraints = new GridBagConstraints();
		titleLabelConstraints.insets = new Insets(2, 5, 2, 5);
		titleLabelConstraints.gridx = 0;
		titleLabelConstraints.gridy = 0;

		add(titleLabel, titleLabelConstraints);
	}

	private void addTermComboBox() {
		termComboBox = new JCheckedComboBox<>();

		GridBagConstraints termComboBoxConstraints = new GridBagConstraints();
		termComboBoxConstraints.weightx = 1.0;
		termComboBoxConstraints.fill = GridBagConstraints.HORIZONTAL;
		termComboBoxConstraints.insets = new Insets(2, 0, 2, 5);
		termComboBoxConstraints.gridx = 1;
		termComboBoxConstraints.gridy = 0;

		add(termComboBox, termComboBoxConstraints);
	}

	private void addTermSetButton() {
		termSetButton = new JButton("Set");

		GridBagConstraints termSetConstraints = new GridBagConstraints();
		termSetConstraints.insets = new Insets(2, 0, 2, 5);
		termSetConstraints.gridx = 2;
		termSetConstraints.gridy = 0;

		add(termSetButton, termSetConstraints);
	}

	private void setController() {
		panelController = new AnnotationTermSelectionPanelController(this);
	}

	public void setAvailableTerms(Set<Term> terms) {
		panelController.setAvailableTerms(terms);

	}

	public JCheckedComboBox<TermItem> getTermComboBox() {
		return termComboBox;
	}

	public JButton getTermSetButton() {
		return termSetButton;
	}

	public void setSelectedTerms(Set<Term> terms) {
		panelController.setSelectedTerms(terms);
	}

	public void addTermSelectionCommitListener(AnnotationTermSelectionCommitListener listener) {
		panelController.addTermSelectionCommitListener(listener);
	}
}
