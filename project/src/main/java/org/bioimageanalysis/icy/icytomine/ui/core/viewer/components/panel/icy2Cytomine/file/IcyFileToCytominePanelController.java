package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.icy2Cytomine.file;

import java.awt.EventQueue;
import java.awt.event.ActionListener;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.bioimageanalysis.icy.icytomine.core.image.annotation.RoiAnnotationSender;
import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.ViewController;

import be.cytomine.client.CytomineException;
import icy.common.listener.ProgressListener;
import icy.file.Loader;
import icy.gui.dialog.MessageDialog;
import icy.gui.dialog.OpenDialog;
import icy.sequence.Sequence;

public class IcyFileToCytominePanelController {

	private IcyFileToCytominePanel panel;
	private ViewController viewController;
	private Path selectedFilePath;

	private ExecutorService transferService;
	private Set<ActionListener> closeListeners;

	public IcyFileToCytominePanelController(IcyFileToCytominePanel icyFileToCytominePanel,
			ViewController viewController) {
		this.panel = icyFileToCytominePanel;
		this.viewController = viewController;
		
		closeListeners = new HashSet<>();
		setHandlers();
		setDisplayInformation();
	}

	private void setHandlers() {
		panel.getSelectFileButton().addActionListener(getSelectFileButtonHandler());
		panel.getSendButton().addActionListener(getSendButtonHandler());
		panel.getCancelButton().addActionListener(getCancelButtonHandler());
	}

	private ActionListener getSelectFileButtonHandler() {
		return event -> {
			String selectedFilePathText = OpenDialog.chooseFile();
			if (selectedFilePathText != null) {
				selectedFilePath = Paths.get(selectedFilePathText);
				panel.getFilePathLabel().setText(selectedFilePath.toString());
			} else {
				selectedFilePath = null;
				panel.getFilePathLabel().setText("No file selected");
			}
		};
	}

	private ActionListener getSendButtonHandler() {
		return event -> {
			if (selectedFilePath == null)
				MessageDialog.showDialog("Send annotations to cytomine", "No file selected", MessageDialog.ERROR_MESSAGE);
			else {
				Sequence loadedSequence = Loader.loadSequence(selectedFilePath.toString(), 0, true);
				sendAnnotations(loadedSequence);
			}
		};
	}

	private synchronized void sendAnnotations(Sequence sequence) {
		if (sequence == null) {
			MessageDialog.showDialog("Send annotations to Cytomine", "No sequence selected", MessageDialog.ERROR_MESSAGE);
		} else {
			panel.getSendButton().setEnabled(false);
			transferService = Executors.newSingleThreadExecutor();
			transferService.submit(getTransferHandler(sequence));
			transferService.shutdown();
		}
	}

	private Runnable getTransferHandler(Sequence sequence) {
		return () -> {
			try {
				RoiAnnotationSender sender = new RoiAnnotationSender(viewController.getImageInformation(), sequence, false);
				sender.addProgressListener(getProgressUpdateHandler());
				List<Annotation> createdAnnotations = sender.send();
				viewController.getImageInformation().getAnnotations(false).addAll(createdAnnotations);
				notifySuccess(createdAnnotations);
				notifyCloseListener();
			} catch (Exception e) {
				notifyFailure(e);
			}
			finally {
				panel.getSendButton().setEnabled(true);
			}
		};
	}

	private ProgressListener getProgressUpdateHandler() {
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
			viewController.getViewProvider().updateAnnotations(false);
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

	private void cancelPreviousRequest() throws InterruptedException, RuntimeException {
		if (transferService != null) {
			ExecutorService t = transferService;
			transferService = null;
			t.shutdownNow();
			if (!t.awaitTermination(5, TimeUnit.SECONDS))
				throw new RuntimeException("Could not stop transfer service");
		}
	}

	private void notifyCloseListener() {
		closeListeners.forEach(l -> l.actionPerformed(null));
	}

	private void setDisplayInformation() {
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
