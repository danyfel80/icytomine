package plugins.danyfel80.cytomine.batch;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.bioimageanalysis.icy.icytomine.core.image.annotation.AnnotationInserter;
import org.bioimageanalysis.icy.icytomine.core.image.importer.TiledImageImporter;
import org.bioimageanalysis.icy.icytomine.core.model.Annotation;
import org.bioimageanalysis.icy.icytomine.core.model.Image;
import org.bioimageanalysis.icy.icytomine.core.model.Term;
import org.bioimageanalysis.icy.icytomine.core.view.converters.MagnitudeResolutionConverter;

import icy.sequence.Sequence;
import plugins.adufour.blocks.lang.Loop;
import plugins.adufour.blocks.util.VarList;
import plugins.adufour.vars.lang.Var;
import plugins.adufour.vars.lang.VarInteger;
import plugins.adufour.vars.lang.VarSequence;
import plugins.adufour.vars.lang.VarString;
import vars.cytomine.VarCytomineImage;
import vars.geom.VarDimension;

public class CytomineAnnotationLoop extends Loop {

	// input variables
	private VarList inputMap;
	private VarCytomineImage imageVar;
	private VarInteger resolutionLevelVar;
	private VarDimension paddingSizeVar;
	private VarString termNameVar;
	private VarString includedTermsPerResultVar;

	private Image targetImageInstance;
	private List<Annotation> targetImageAnnotations;
	private Integer targetResolution;
	private Dimension targetPaddingSize;
	private String[] targetTermNames;
	private boolean searchNoTerm;
	private String[] targetIncludedTermNames;
	private boolean addAdditionalNoTerm;

	private ListIterator<Annotation> currentAnnotationIterator;
	private int currentAnnotationIndex;
	private Annotation currentAnnotation;
	private Rectangle2D currentAnnotationPaddedBounds;
	private Set<Term> additionalTerms;
	private Set<Annotation> additionalAnnotations;

	// iteration output variables
	private VarSequence currentAnnotationSequenceVar;

	private Sequence currentAnnotationSequence;

	@Override
	public void declareInput(VarList inputMap) {
		super.declareInput(inputMap);
		setInputMap(inputMap);
		initializeInputVariables();
		addInputVariables();
	}

	private void setInputMap(VarList inputMap) {
		this.inputMap = inputMap;
	}

	private void initializeInputVariables() {
		imageVar = VarCytomineImage.ofNullable(null);
		resolutionLevelVar = new VarInteger("Resolution level", 0);
		paddingSizeVar = new VarDimension("Padding size");
		termNameVar = new VarString("Term name", "No Term");
		includedTermsPerResultVar = new VarString("Addition annotations with terms", "");
	}

	private void addInputVariables() {
		inputMap.add(imageVar.getName(), imageVar);
		inputMap.add(resolutionLevelVar.getName(), resolutionLevelVar);
		inputMap.add(paddingSizeVar.getName(), paddingSizeVar);
		inputMap.add(termNameVar.getName(), termNameVar);
		inputMap.add(includedTermsPerResultVar.getName(), includedTermsPerResultVar);
	}

	@Override
	public void declareOutput(VarList outputMap) {
		super.declareOutput(outputMap);
		for (Var<?> var: inputMap) {
			outputMap.add(var.getName(), var);
		}
		currentAnnotationSequenceVar = new VarSequence("Current annotation sequence", null);
		currentAnnotationSequenceVar.setEnabled(false);
		outputMap.add(currentAnnotationSequenceVar.getName(), currentAnnotationSequenceVar);
	}

	@Override
	public void declareLoopVariables(List<Var<?>> loopVariables) {
		for (Var<?> var: inputMap) {
			loopVariables.add(var);
		}
		loopVariables.add(currentAnnotationSequenceVar);
	}

	@Override
	public void initializeLoop() {
		retrieveParameters();
		computeAnnotationsToLoad();
		initializeIterators();
	}

	private void retrieveParameters() {
		targetImageInstance = imageVar.getValue(true);
		targetResolution = resolutionLevelVar.getValue(true);
		targetPaddingSize = paddingSizeVar.getValue();
		targetTermNames = termNameVar.getValue(true).split(",+");
		targetIncludedTermNames = includedTermsPerResultVar.getValue(true).split(" *, *");
		System.out.println(Arrays.toString(targetTermNames));
		System.out.println(Arrays.toString(targetIncludedTermNames));

		if (targetPaddingSize == null)
			targetPaddingSize = new Dimension();
	}

	private void computeAnnotationsToLoad() {
		Set<Term> availableTerms = targetImageInstance.getProject().getOntology().getTerms(false);

		Set<Term> searchedTerms = availableTerms.stream()
				.filter(t -> Arrays.stream(targetTermNames).anyMatch(targetTermName -> Objects
						.equals(t.getName().orElse("Not available").toLowerCase(), targetTermName.toLowerCase())))
				.collect(Collectors.toSet());
		searchNoTerm = Arrays.stream(targetTermNames).anyMatch(name -> name.toLowerCase().equals("no term"));

		additionalTerms = availableTerms.stream()
				.filter(t -> Arrays.stream(targetIncludedTermNames).anyMatch(targetTermName -> Objects
						.equals(t.getName().orElse("No Term").toLowerCase(), targetTermName.toLowerCase())))
				.collect(Collectors.toSet());
		addAdditionalNoTerm = Arrays.stream(targetIncludedTermNames).anyMatch(name -> name.toLowerCase().equals("no term"));

		System.out.println("Target Terms=" + searchedTerms + ", Additional Terms=" + additionalTerms);

		targetImageAnnotations = new ArrayList<>(targetImageInstance.getAnnotationsWithGeometry(false));
		if (!searchedTerms.isEmpty() || searchNoTerm) {
			targetImageAnnotations = targetImageAnnotations.stream().filter(a -> {
				boolean isMatch = a.getAssociatedTerms().stream().anyMatch(aTerm -> searchedTerms.contains(aTerm));
				return isMatch || (a.getAssociatedTerms().isEmpty() && searchNoTerm);
			}).collect(Collectors.toList());
		}
	}

	private void initializeIterators() {
		currentAnnotationIterator = targetImageAnnotations.listIterator();
		currentAnnotationIndex = 0;
	}

	@Override
	public void beforeIteration() {
		importCurrentAnnotationTile();
		currentAnnotationSequenceVar.setValue(currentAnnotationSequence);
	}

	private void importCurrentAnnotationTile() {
		currentAnnotation = currentAnnotationIterator.next();
		currentAnnotationPaddedBounds = getCurrentAnnotationPaddedBounds();
		computeAdditionalAnnotations();
		computeCurrentAnnotationSequence();
	}

	private Rectangle2D getCurrentAnnotationPaddedBounds() {
		Rectangle2D annotationBounds = getCurrentAnnotationBounds();
		Dimension2D targetPaddinSizeAtResolutionZero = MagnitudeResolutionConverter.convertDimension2D(targetPaddingSize,
				targetResolution, 0);
		Rectangle2D annotationPaddedBounds = new Rectangle2D.Double(
				annotationBounds.getX() - targetPaddinSizeAtResolutionZero.getWidth(),
				annotationBounds.getY() - targetPaddinSizeAtResolutionZero.getHeight(),
				annotationBounds.getWidth() + 2 * targetPaddinSizeAtResolutionZero.getWidth(),
				annotationBounds.getHeight() + 2 * targetPaddinSizeAtResolutionZero.getHeight());

		return annotationPaddedBounds;
	}

	private Rectangle2D getCurrentAnnotationBounds() {
		return currentAnnotation.getYAdjustedBounds();
	}

	private void computeAdditionalAnnotations() {
		List<Annotation> availableAnnotations = targetImageInstance.getAnnotationsWithGeometry(false);
		additionalAnnotations = availableAnnotations.stream()
				.filter(a -> a.getYAdjustedBounds().intersects(currentAnnotationPaddedBounds)).filter(a -> {
					boolean isMatch = a.getAssociatedTerms().stream()
							.anyMatch(annotationTerm -> additionalTerms.contains(annotationTerm));
					return isMatch || (a.getAssociatedTerms().isEmpty() && addAdditionalNoTerm);
				}).collect(Collectors.toSet());
	}

	private void computeCurrentAnnotationSequence() {
		BufferedImage tileImage = importTileArea(currentAnnotationPaddedBounds);
		buildCurrentAnnotationSequence(tileImage);
	}

	private BufferedImage importTileArea(Rectangle2D bounds) {
		TiledImageImporter importer = new TiledImageImporter(targetImageInstance);
		Future<BufferedImage> futureTileImage = importer.requestImage(targetResolution, bounds);
		try {
			return futureTileImage.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	private void buildCurrentAnnotationSequence(BufferedImage tileImage) {
		currentAnnotationSequence = new Sequence(tileImage);
		addCurrentAnnotationROIsToSequence();
		addCurrentSequenceMetadata();
	}

	private void addCurrentAnnotationROIsToSequence() {
		AnnotationInserter inserter = new AnnotationInserter(currentAnnotationSequence);
		Set<Annotation> annotationSet = new HashSet<>();
		annotationSet.add(currentAnnotation);
		annotationSet.addAll(additionalAnnotations);
		inserter.insertAnnotations(currentAnnotationPaddedBounds, targetResolution, annotationSet, false);
	}

	private void addCurrentSequenceMetadata() {
		double pixelSize = targetImageInstance.getResolution().orElse(1d);
		double pixelSizeAtTargetResolution = getPixelSizeAtTargetResolution();
		currentAnnotationSequence.setPositionX(currentAnnotationPaddedBounds.getX() * pixelSize);
		currentAnnotationSequence.setPositionY(currentAnnotationPaddedBounds.getY() * pixelSize);
		currentAnnotationSequence.setPixelSizeX(pixelSizeAtTargetResolution);
		currentAnnotationSequence.setPixelSizeY(pixelSizeAtTargetResolution);
		currentAnnotationSequence
				.setName(targetImageInstance.getName().orElse("Imported image") + " Annotation " + currentAnnotation.getId());
	}

	private double getPixelSizeAtTargetResolution() {
		double pixelSize = targetImageInstance.getResolution().orElse(1d);
		double scaleFactor = Math.pow(2, targetResolution);
		return pixelSize * scaleFactor;
	}

	@Override
	public void afterIteration() {
		currentAnnotationIndex++;
		super.afterIteration();
	}

	@Override
	public boolean isStopConditionReached() {
		return !(currentAnnotationIndex < targetImageAnnotations.size());
	}
}
