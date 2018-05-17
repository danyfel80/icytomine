package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.icy2Cytomine.sequence;

import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;

import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.ViewController;

import icy.main.Icy;
import icy.sequence.Sequence;

public class IcySequenceToCytominePanelController {

	public interface RoiTransferCancellationListener {
		void cancelTransfer();
	}

	public interface RoiTransferStartListener {
		void startTransfer(Sequence fromSequence);
	}

	private IcySequenceToCytominePanel panel;
	private ViewController viewController;
	private Set<ActionListener> closeListeners;

	public IcySequenceToCytominePanelController(IcySequenceToCytominePanel panel, ViewController viewController) {
		this.panel = panel;
		this.viewController = viewController;
		setHandlers();
		setDisplayInformation();

		closeListeners = new HashSet<>();
	}

	private void setAvailableSequences() {
		List<Sequence> sequences = Icy.getMainInterface().getSequences();
		SequenceItem[] items = sequences.stream().map(seq -> new SequenceItem(seq)).toArray(SequenceItem[]::new);
		panel.getSequenceComboBox().setModel(new DefaultComboBoxModel<>(items));
		//panel.getSequenceComboBox().setSelectedItem();
	}

	private void setHandlers() {

	}

	private void setDisplayInformation() {
		setAvailableSequences();
	}

	public void addCloseListener(ActionListener listener) {
		closeListeners.add(listener);
	}

}
