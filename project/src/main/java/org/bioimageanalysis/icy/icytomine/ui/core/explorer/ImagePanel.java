package org.bioimageanalysis.icy.icytomine.ui.core.explorer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.model.Project;

public class ImagePanel extends JPanel
{
    private static final long serialVersionUID = 5990256964181871478L;

    public static class ImageItem
    {
        private Image image;

        public ImageItem(Image image)
        {
            this.image = image;
        }

        public Image getImage()
        {
            return image;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null)
                return false;
            if (!(obj instanceof ImageItem))
                return false;
            ImageItem other = (ImageItem) obj;
            return getImage().equals(other.getImage());
        }

        @Override
        public String toString()
        {
            return image.getName().orElse(String.format("Not specified (id=%d)", image.getId().longValue()));
        }
    }

    private static GridBagConstraints getConstraints(int x, int y, int width, int height, Insets insets, int fill)
    {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = fill;
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = width;
        constraints.gridheight = height;
        constraints.insets = insets;
        return constraints;
    }

    @FunctionalInterface
    public interface ImageSelectionListener
    {
        public void imageSelected(Image image);
    }

    private JLabel titleLabel;
    private JTextField searchBar;
    private JList<ImageItem> imageList;

    private ImagePanelController controller;

    /**
     * Creates an empty image panel. Use {@link ImagePanelController} to fill and control the panel.
     */
    public ImagePanel()
    {
        setView();
        setController();
    }

    private void setView()
    {
        setMinimumSize(new Dimension(50, 50));
        setPreferredSize(new Dimension(240, 400));
        setGridBagLayout();

        addTitleLabel();
        addSearchBar();
        addImageList();
    }

    private void setGridBagLayout()
    {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] {0, 0};
        gridBagLayout.rowHeights = new int[] {0, 0, 0};
        gridBagLayout.columnWeights = new double[] {0.0, 1.0};
        gridBagLayout.rowWeights = new double[] {0.0, 0.0, 1.0};
        setLayout(gridBagLayout);
    }

    private void addTitleLabel()
    {
        buildPanelTitle();
        add(titleLabel, getConstraints(0, 0, 2, 1, new Insets(0, 3, 3, 3), GridBagConstraints.BOTH));
    }

    private void buildPanelTitle()
    {
        titleLabel = new JLabel("Images");
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setToolTipText("Images available for the selected project");
        titleLabel.setBackground(SystemColor.control);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private void addSearchBar()
    {
        JLabel searchBarTitle = new JLabel("Search:");
        add(searchBarTitle, getConstraints(0, 1, 1, 1, new Insets(0, 3, 3, 0), GridBagConstraints.BOTH));
        buildSearchBar();
        add(searchBar, getConstraints(1, 1, 1, 1, new Insets(0, 3, 3, 3), GridBagConstraints.BOTH));
    }

    private void buildSearchBar()
    {
        searchBar = new JTextField();
        searchBar.setToolTipText("Search projects by their name");
    }

    private void addImageList()
    {
        JScrollPane scrollPane = createImageList();
        add(scrollPane, getConstraints(0, 2, 2, 1, new Insets(0, 3, 3, 3), GridBagConstraints.BOTH));
    }

    private JScrollPane createImageList()
    {
        JScrollPane scrollPane = new JScrollPane();
        imageList = new JList<>();
        imageList.setBackground(SystemColor.window);
        imageList.setModel(new DefaultListModel<>());
        imageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        titleLabel.setLabelFor(imageList);
        scrollPane.setViewportView(imageList);
        return scrollPane;
    }

    private void setController()
    {
        this.controller = new ImagePanelController(this);
    }

    public void setProject(Project project)
    {
        this.controller.setProject(project);
    }

    public void addImageSelectionListener(ImageSelectionListener listener)
    {
        this.controller.addImageSelectionListener(listener);
    }

    public void removeImageSelectionListener(ImageSelectionListener listener)
    {
        this.controller.removeImageSelectionListener(listener);
    }

    public void addImageDoubleClickListener(ImageSelectionListener listener)
    {
        this.controller.addImageDoubleClickListener(listener);
    }

    public void removeImageDoubleClickListener(ImageSelectionListener listener)
    {
        this.controller.removeImageDoubleClickListener(listener);
    }

    public JList<ImageItem> getImageList()
    {
        return imageList;
    }

    public JTextField getSearchBar()
    {
        return searchBar;
    }

    public Image getSelectedImage()
    {
        if (getImageList().isSelectionEmpty())
        {
            return null;
        }
        else
        {
            return getImageList().getSelectedValue().getImage();
        }
    }
}
