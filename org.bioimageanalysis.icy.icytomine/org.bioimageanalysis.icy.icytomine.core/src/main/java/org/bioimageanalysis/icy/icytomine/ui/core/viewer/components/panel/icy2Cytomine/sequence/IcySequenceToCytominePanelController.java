package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.icy2Cytomine.sequence;

import java.awt.EventQueue;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.DefaultComboBoxModel;

import org.bioimageanalysis.icy.icytomine.core.image.annotation.RoiAnnotationSender;
import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.ViewController;

import be.cytomine.client.CytomineException;
import icy.common.listener.ProgressListener;
import icy.gui.dialog.MessageDialog;
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

	private ExecutorService transferService;

	public IcySequenceToCytominePanelController(IcySequenceToCytominePanel panel, ViewController viewController) {
		this.panel = panel;
		this.viewController = viewController;
		closeListeners = new HashSet<>();

		setHandlers();
		setDisplayInformation();
	}

	private void setAvailableSequences() {
		List<Sequence> sequences = Icy.getMainInterface().getSequences();
		sequences.add(0, null);
		SequenceItem[] items = sequences.stream().map(seq -> new SequenceItem(seq)).toArray(SequenceItem[]::new);
		panel.getSequenceComboBox().setModel(new DefaultComboBoxModel<>(items));
		panel.getSequenceComboBox().setSelectedItem(items[0]);
	}

	private void setHandlers() {
		panel.getSendButton().addActionListener(getSendButtonHandler());
		panel.getCancelButton().addActionListener(getCancelButtonHandler());
	}

	private ActionListener getSendButtonHandler() {
		return (event) -> {
			try {
				cancelPreviousRequest();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (RuntimeException e) {
				MessageDialog.showDialog("Send annotations to Cytomine", e.getMessage(), MessageDialog.ERROR_MESSAGE);
				e.printStackTrace();
				return;
			}
			Sequence sequence = ((SequenceItem) panel.getSequenceComboBox().getSelectedItem()).getSequence();
			boolean selectedRois = panel.getRoiSelectionCheckBox().isSelected();
			sendAnnotations(sequence, selectedRois);
		};
	}

	private synchronized void sendAnnotations(Sequence sequence, boolean selectedRois) {
		if (sequence == null) {
			MessageDialog.showDialog("Send annotations to Cytomine", "No sequence selected", MessageDialog.ERROR_MESSAGE);
		} else {
			transferService = Executors.newSingleThreadExecutor();
			transferService.submit(getTransferHandler(sequence, selectedRois));
			transferService.shutdown();
		}
	}

	private Runnable getTransferHandler(Sequence sequence, boolean selectedRois) {
		return () -> {
			try {
				RoiAnnotationSender sender = new RoiAnnotationSender(viewController.getImageInformation(), sequence,
						selectedRois);
				sender.addProgressListener(getProgressHandler());
				List<Annotation> createdAnnotations = sender.send();
				viewController.getImageInformation().getAnnotations(false).addAll(createdAnnotations);
				notifySuccess(createdAnnotations);
			} catch (Exception e) {
				notifyFailure(e);
			}
		};
	}

	private ProgressListener getProgressHandler() {
		return (pos, len) -> {
			setProgress(pos / len);
			return true;
		};
	}

	private void setProgress(double progress) {
		EventQueue.invokeLater(() -> {
			panel.getTransferProgressBar().setValue((int) (progress * 100));
		});
	}

	private void notifySuccess(List<Annotation> createdAnnotations) {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}
		setProgress(0);
		try {
			viewController.getViewProvider().updateAnnotations();
			viewController.refreshView();
		} catch (Exception e) {
			e.printStackTrace();
		}
		MessageDialog.showDialog("Send annotations to Cytomine",
				String.format("Transfer complete: %d annotations sent", createdAnnotations.size()),
				MessageDialog.INFORMATION_MESSAGE);

	}

	private void notifyFailure(Exception e) {
		if (e instanceof CytomineException) {
			MessageDialog.showDialog("Send annotations to Cytomine", ((CytomineException) e).getMsg(),
					MessageDialog.ERROR_MESSAGE);
		} else {
			MessageDialog.showDialog("Send annotations to Cytomine", e.getMessage(), MessageDialog.ERROR_MESSAGE);
		}
		e.printStackTrace();
	}

	private void cancelPreviousRequest() throws InterruptedException, RuntimeException {
		if (transferService != null) {
			ExecutorService t = transferService;
			transferService = null;
			t.shutdownNow();
			if (!t.awaitTermination(5, TimeUnit.SECONDS))
				throw new RuntimeException("Could not stop transfer service");
		}
	}

	private ActionListener getCancelButtonHandler() {
		return (event) -> {
			try {
				cancelPreviousRequest();
			} catch (InterruptedException | RuntimeException e) {
				e.printStackTrace();
			}
			notifyCloseListener();
		};
	}

	private void notifyCloseListener() {
		closeListeners.forEach(l -> l.actionPerformed(null));
	}

	private void setDisplayInformation() {
		setAvailableSequences();
	}

	public void addCloseListener(ActionListener listener) {
		closeListeners.add(listener);
	}

	public void close() {
		try {
			cancelPreviousRequest();
		} catch (Exception e) {
			e.printStackTrace();
			MessageDialog.showDialog("Error on closing dialog - Icytomine", e.getMessage(), MessageDialog.ERROR_MESSAGE);
		}
	}

}
