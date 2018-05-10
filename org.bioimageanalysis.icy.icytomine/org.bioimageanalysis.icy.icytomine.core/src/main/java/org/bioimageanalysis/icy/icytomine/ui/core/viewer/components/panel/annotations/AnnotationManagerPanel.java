package org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.ui.core.viewer.components.panel.annotations.filter.AnnotationFilterPanel;

@SuppressWarnings("serial")
public class AnnotationManagerPanel extends JPanel {

	private Image imageInformation;
	private List<Annotation> annotations;

	public AnnotationManagerPanel() {
		this(Image.getNoImage(null));
	}

	public AnnotationManagerPanel(Image imageInformation) {
		this.imageInformation = imageInformation;
		try {
			annotations = imageInformation.getAnnotations();
		} catch (Exception e) {
			e.printStackTrace();
			annotations = new ArrayList<>();
		}

		setLayout(new BorderLayout(0, 0));

		AnnotationFilterPanel filterPanel = new AnnotationFilterPanel(this.imageInformation);
		add(filterPanel, BorderLayout.NORTH);

		AnnotationTable annotationTable = new AnnotationTable(this.annotations);
		add(annotationTable, BorderLayout.CENTER);
	}

}
