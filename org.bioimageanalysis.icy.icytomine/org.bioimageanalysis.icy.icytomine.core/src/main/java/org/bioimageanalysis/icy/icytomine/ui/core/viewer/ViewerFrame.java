package org.bioimageanalysis.icy.icytomine.ui.core.viewer;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.UIManager;

import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.ViewerController;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.provider.NullViewProvider;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.provider.ViewProvider;
import org.pushingpixels.substance.api.skin.SubstanceOfficeBlack2007LookAndFeel;

import icy.gui.frame.IcyFrame;
import icy.gui.frame.IcyFrameAdapter;
import icy.gui.frame.IcyFrameEvent;

public class ViewerFrame extends IcyFrame {

	private ViewerComponentContainer viewerComponentContainer;
	private ViewerController viewerController;

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
		this(new NullViewProvider());
	}

	public ViewerFrame(ViewProvider viewProvider) {
		super("Viewer - Icytomine", true, true, true, false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		viewerComponentContainer = new ViewerComponentContainer(viewProvider);
		viewerController = new ViewerController(viewerComponentContainer);
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
		setSize(viewerComponentContainer.getPreferredSize());
		setMinimumSize(viewerComponentContainer.getMinimumSize());
		setContentPane(viewerComponentContainer);
		addToDesktopPane();
		center();

	}

}
