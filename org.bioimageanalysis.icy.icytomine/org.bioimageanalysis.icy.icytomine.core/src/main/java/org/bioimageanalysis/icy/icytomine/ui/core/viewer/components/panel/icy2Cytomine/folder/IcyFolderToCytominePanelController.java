package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.icy2Cytomine.folder;

import java.awt.EventQueue;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.swing.JFileChooser;

import org.bioimageanalysis.icy.icytomine.core.image.annotation.RoiAnnotationSender;
import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.ViewController;

import be.cytomine.client.CytomineException;
import icy.common.listener.ProgressListener;
import icy.file.Loader;
import icy.gui.dialog.MessageDialog;
import icy.sequence.Sequence;

public class IcyFolderToCytominePanelController {

	private IcyFolderToCytominePanel panel;
	private ViewController viewController;

	private ExecutorService transferService;
	private Set<ActionListener> closeListeners;

	private Path selectedFolderPath;

	public IcyFolderToCytominePanelController(IcyFolderToCytominePanel panel, ViewController viewController) {
		this.panel = panel;
		this.viewController = viewController;

		closeListeners = new HashSet<>();
		setHandlers();
		setDisplayInformation();
	}

	private void setHandlers() {
		panel.getSelectFolderButton().addActionListener(getSelectFolderButtonHandler());
		panel.getSendButton().addActionListener(getSendButtonHandler());
		panel.getCancelButton().addActionListener(getCancelButtonHandler());
	}

	private ActionListener getSelectFolderButtonHandler() {
		return event -> {
			JFileChooser dialog = new JFileChooser();
			dialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			dialog.setMultiSelectionEnabled(false);
			dialog.showOpenDialog(panel);

			File selectedFolder = dialog.getSelectedFile();
			if (selectedFolder != null) {
				selectedFolderPath = selectedFolder.toPath();
				panel.getFolderPathLabel().setText(selectedFolderPath.toString());
			} else {
				selectedFolderPath = null;
				panel.getFolderPathLabel().setText("No folder selected");
			}
		};
	}

	private ActionListener getSendButtonHandler() {
		return event -> {
			if (selectedFolderPath == null)
				MessageDialog.showDialog("Send annotations to cytomine", "No folder selected", MessageDialog.ERROR_MESSAGE);
			else {
				List<Sequence> sequences = Loader.loadSequences(Arrays.stream(selectedFolderPath.toFile().listFiles())
						.map(file -> file.toString()).collect(Collectors.toList()), 0, true, true, false, true);
				sendAnnotationsFromSequeces(sequences);
			}
		};
	}

	private void sendAnnotationsFromSequeces(List<Sequence> sequences) {
		if (sequences == null || sequences.isEmpty()) {
			MessageDialog.showDialog("Send annotations to Cytomine", "No sequences selected", MessageDialog.ERROR_MESSAGE);
		} else {
			transferService = Executors.newSingleThreadExecutor();
			transferService.submit(getTransferHandler(sequences));
			transferService.shutdown();
		}
	}

	private Runnable getTransferHandler(List<Sequence> sequences) {
		return () -> {
			try {
				List<Annotation> createdAnnotations = new LinkedList<>();
				for (Sequence sequence : sequences) {
					RoiAnnotationSender sender = new RoiAnnotationSender(viewController.getImageInformation(), sequence, false);
					sender.addProgressListener(getProgressUpdateHandler());
					createdAnnotations.addAll(sender.send());
					viewController.getImageInformation().getAnnotations(false).addAll(createdAnnotations);
				}
				notifySuccess(createdAnnotations);
			} catch (Exception e) {
				notifyFailure(e);
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
