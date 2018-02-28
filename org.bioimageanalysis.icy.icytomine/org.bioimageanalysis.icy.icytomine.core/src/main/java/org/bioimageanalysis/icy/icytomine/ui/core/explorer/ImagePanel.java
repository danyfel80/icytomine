package org.bioimageanalysis.icy.icytomine.ui.core.explorer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.model.Project;

import be.cytomine.client.Cytomine;
import be.cytomine.client.CytomineException;
import be.cytomine.client.collections.ImageInstanceCollection;

public class ImagePanel extends JPanel {
	@FunctionalInterface
	public interface ImageSelectionListener {
		public void imageSelected(Image image);
	}

	private static final long serialVersionUID = 5990256964181871478L;

	private Cytomine	cytomine;
	private Project		currentProject;

	private JList<Image> listImages;

	/**
	 * Creates an empty image panel. To fill with cytomine data use
	 * {@link #ImagePanel(Cytomine)}.
	 */
	public ImagePanel() {
		setMinimumSize(new Dimension(50, 50));
		setPreferredSize(new Dimension(240, 400));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JLabel lblImages = new JLabel("Images");
		lblImages.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblImages.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(lblImages);
		lblImages.setToolTipText("Images available for the selected project");
		lblImages.setBackground(SystemColor.control);
		lblImages.setHorizontalAlignment(SwingConstants.CENTER);

		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane);
		listImages = new JList<>();
		listImages.setBackground(SystemColor.window);
		listImages.setModel(new DefaultListModel<>());
		listImages.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lblImages.setLabelFor(listImages);
		scrollPane.setViewportView(listImages);

		setCurrentProject(null);
	}

	/**
	 * Create the panel.
	 * 
	 * @param cytomine
	 *          Cytomine client.
	 */
	public ImagePanel(Cytomine cytomine, Project project) {
		this();
		setCytomine(cytomine);
		setCurrentProject(project);
	}

	public void setCytomine(Cytomine cytomine) {
		this.cytomine = cytomine;
	}

	public Cytomine getCytomine() {
		return this.cytomine;
	}

	public Project getCurrentProject() {
		return currentProject;
	}

	public void setCurrentProject(Project project) {
		this.currentProject = project;
		if (getCytomine() != null) {
			try {
				updateImageList();
			} catch (CytomineException e) {
				e.printStackTrace();
				setCurrentProject(null);
			}
		} else {
			System.err.println("No cytomine instance at image panel yet.");
		}
	}

	@SuppressWarnings("unchecked")
	public void addImageSelectionListener(ImageSelectionListener listener) {
		listImages.addListSelectionListener(e -> listener.imageSelected(((JList<Image>) e.getSource()).getSelectedValue()));
	}

	public void addImageDoubleClickListener(ImageSelectionListener listener) {
		listImages.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int index = listImages.locationToIndex(e.getPoint());
					if (index > -1) {
						listener.imageSelected(listImages.getSelectedValue());
					}
				}
			}
		});
	}

	public void updateImageList() throws CytomineException {
		if (getCurrentProject() != null) {
			ImageInstanceCollection imageCollection = cytomine.getImageInstances(getCurrentProject().getId());
			Image[] images = new Image[imageCollection.size()];
			for (int i = 0; i < imageCollection.size(); i++) {
				be.cytomine.client.models.ImageInstance image = imageCollection.get(i);
				images[i] = new Image(image, cytomine);
			}
			listImages.setListData(images);
			listImages.clearSelection();
			listImages.setSelectedIndex(-1);
		} else {
			listImages.removeAll();
		}
	}

}
