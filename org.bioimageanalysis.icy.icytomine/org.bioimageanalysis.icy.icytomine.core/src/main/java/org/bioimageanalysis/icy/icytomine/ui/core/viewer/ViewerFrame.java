package org.bioimageanalysis.icy.icytomine.ui.core.viewer;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.UIManager;

import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.view.CachedAnnotationView;
import org.bioimageanalysis.icy.icytomine.core.view.CachedImageView;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.provider.CachedViewProvider;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.provider.ViewProvider;
import org.pushingpixels.substance.api.skin.SubstanceOfficeBlack2007LookAndFeel;

import be.cytomine.client.CytomineException;
import icy.gui.dialog.MessageDialog;
import icy.gui.frame.IcyFrame;
import icy.gui.frame.IcyFrameAdapter;
import icy.gui.frame.IcyFrameEvent;

public class ViewerFrame extends IcyFrame {

	private ViewerPanel viewerComponentContainer;
	private ViewerPanelController viewerController;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(new SubstanceOfficeBlack2007LookAndFeel());
					ViewerFrame frame = new ViewerFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public ViewerFrame() {
		super("Viewer - Icytomine", true, true, true, false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		addToDesktopPane();
		center();

	}

	public void setImageInstance(Image imageInstance) {
		ViewProvider viewProvider;
		try {
			viewProvider = new CachedViewProvider(new CachedImageView(imageInstance), new CachedAnnotationView(imageInstance));
		} catch (CytomineException e) {
			MessageDialog.showDialog("Error loading image - Icytomine", e.getMessage(), MessageDialog.ERROR_MESSAGE);
			e.printStackTrace();
			return;
		}

		setTitle(imageInstance.getName().orElse(String.valueOf(imageInstance.getId())) + " - Icytomine");
		viewerComponentContainer = new ViewerPanel(viewProvider);
		viewerController = new ViewerPanelController(viewerComponentContainer);
		this.addFrameListener(new IcyFrameAdapter() {
			@Override
			public void icyFrameOpened(IcyFrameEvent e) {
				viewerController.startViewer();
			}

			@Override
			public void icyFrameClosed(IcyFrameEvent e) {
				System.out.println("frame closed");
				viewerController.stopViewer();
			}
		});

		setContentPane(viewerComponentContainer);
		setSize(viewerComponentContainer.getPreferredSize());
		setMinimumSize(viewerComponentContainer.getMinimumSize());
	}

}
