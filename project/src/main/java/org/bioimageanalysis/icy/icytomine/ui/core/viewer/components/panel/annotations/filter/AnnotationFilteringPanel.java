package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.filter;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;

import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.model.filters.AnnotationFilter.AnnotationFilterUpdateListener;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.filter.FilterAdditionPanel.FilterAdditionListener;

@SuppressWarnings("serial")
public class AnnotationFilteringPanel extends JPanel {

	private GridBagLayout layout;
	private FilterAdditionPanel filterAdditionPanel;
	private List<AnnotationFilterPanel<?>> filterPanels;

	private AnnotationFilteringPanelController panelController;

	public AnnotationFilteringPanel(Image imageInformation) {
		this();
		setImageInformation(imageInformation);
	}

	public AnnotationFilteringPanel() {
		setView();
		setController();
	}

	private void setView() {
		layout = new GridBagLayout();
		layout.columnWidths = new int[] { 305 };
		setLayout(layout);

		this.filterPanels = new ArrayList<>();
		addFilterAdditionPanel();
	}

	private void addFilterAdditionPanel() {
		filterAdditionPanel = new FilterAdditionPanel();
		GridBagConstraints layoutConstraints = createLayoutConstraints(5, 5, 0);
		add(filterAdditionPanel, layoutConstraints);
	}

	private GridBagConstraints createLayoutConstraints(int topMargin, int bottomMargin, int row) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(topMargin, 5, bottomMargin, 5);
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx = 0;
		constraints.gridy = row;
		return constraints;
	}

	private void setController() {
		panelController = new AnnotationFilteringPanelController(this);
	}

	public void setImageInformation(Image imageInformation) {
		panelController.setImageInformation(imageInformation);
	}

	protected void addFilterAdditionListener(FilterAdditionListener listener) {
		filterAdditionPanel.addFilterAdditionListener(listener);
	}

	protected synchronized void addFilterPanel(AnnotationFilterPanel<?> filter) {
		int row = filterPanels.size();
		GridBagConstraints layoutConstraints = createLayoutConstraints(0, 5, row + 1);
		add(filter, layoutConstraints);
		revalidate();
		this.filterPanels.add(filter);
	}

	protected synchronized void removeFilterPanel(AnnotationFilterPanel<?> filter) {
		remove(filter);
		revalidate();
		this.filterPanels.remove(filter);
	}

	public void addAnnotationFilterUpdateListener(AnnotationFilterUpdateListener listener) {
		this.panelController.addAnnotationFilterUpdateListener(listener);
	}

	public void removeAnnotationFilterUpdateListener(AnnotationFilterUpdateListener listener) {
		this.panelController.removeAnnotationFilterUpdateListener(listener);
	}

	public Set<Annotation> getActiveAnnotations() {
		return panelController.getActiveAnnotations();
	}

}
