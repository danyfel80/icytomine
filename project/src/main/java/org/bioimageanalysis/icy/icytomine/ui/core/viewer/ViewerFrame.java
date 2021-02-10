package org.bioimageanalysis.icy.icytomine.ui.core.viewer;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.view.CachedAnnotationView;
import org.bioimageanalysis.icy.icytomine.core.view.CachedImageView;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.provider.CachedViewProvider;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.controller.view.provider.ViewProvider;
import org.pushingpixels.substance.api.skin.SubstanceOfficeBlack2007LookAndFeel;

import icy.gui.frame.IcyFrame;
import icy.gui.frame.IcyFrameAdapter;
import icy.gui.frame.IcyFrameEvent;

public class ViewerFrame extends IcyFrame {

	private ViewerPanel viewerComponentContainer;
	private ViewerPanelController viewerController;
	private JPanel loadingPanel;

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
		super("Viewer - Icytomine", true, true, true, true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		addToDesktopPane();
		center();
		setLoadingPane();
	}

	private void setLoadingPane() {
		loadingPanel = new JPanel();

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {0};
		gridBagLayout.rowHeights = new int[] {0};
		gridBagLayout.columnWeights = new double[] {1.0};
		gridBagLayout.rowWeights = new double[] {1.0};
		loadingPanel.setLayout(gridBagLayout);

		JLabel loadingLabel = new JLabel("Loading viewer...");
		loadingLabel.setHorizontalAlignment(JLabel.CENTER);
		GridBagConstraints loadingLabelConstraints = new GridBagConstraints();
		loadingLabelConstraints.anchor = GridBagConstraints.NORTH;
		loadingLabelConstraints.fill = GridBagConstraints.BOTH;
		loadingLabelConstraints.gridx = 0;
		loadingLabelConstraints.gridy = 0;
		loadingPanel.add(loadingLabel, loadingLabelConstraints);

		setContentPane(loadingPanel);
	}

	public void setImageInstance(Image imageInstance) throws RuntimeException {
		try {
			ViewProvider viewProvider;
			viewProvider = new CachedViewProvider(new CachedImageView(imageInstance),
					new CachedAnnotationView(imageInstance));

			setTitle(imageInstance.getName().orElse(String.valueOf(imageInstance.getId())) + " - Icytomine");
			SwingUtilities.invokeLater(() -> {
				viewerComponentContainer = new ViewerPanel(viewProvider);
				viewerController = new ViewerPanelController(viewerComponentContainer);
				this.addFrameListener(new IcyFrameAdapter() {
					@Override
					public void icyFrameClosed(IcyFrameEvent e) {
						System.out.println("frame closed");
						viewerController.stopViewer();
					}
				});

				setContentPane(viewerComponentContainer);
				setSize(viewerComponentContainer.getPreferredSize());
				setMinimumSize(viewerComponentContainer.getMinimumSize());
				viewerController.startViewer();
			});
		} catch (Exception e) {
			throw new RuntimeException("Error loading image: " + e.getMessage(), e);
		}
	}

}
