package org.bioimageanalysis.icy.icytomine.core.model.filters;

import java.util.HashSet;
import java.util.Set;

import org.bioimageanalysis.icy.icytomine.core.model.Annotation;

import com.google.common.base.Objects;

public abstract class AnnotationFilter {
	public enum ComputationMode {
		RECOMPUTE_ALL,
		RECOMPUTE_JUST_THIS,
		USE_LAST_RESULT
	}

	public interface AnnotationFilterUpdateListener {
		void filterUpdated(Set<Annotation> activeAnnotations);
	}

	private AnnotationFilter previousFilter;
	private AnnotationFilterUpdateListener previousFilterUpdateHandler;
	protected Set<Annotation> activeAnnotations;
	private Set<AnnotationFilterUpdateListener> updateListeners;

	public AnnotationFilter() {
		updateListeners = new HashSet<>();
	}

	public void setPreviousFilter(AnnotationFilter newPreviousFilter) {
		if (!Objects.equal(this.previousFilter, newPreviousFilter)) {
			if (this.previousFilter != null) {
				this.previousFilter.removeAnnotationFilterUpdateListener(getPreviousFilterUpdateHandler());
			}

			this.previousFilter = newPreviousFilter;

			if (this.previousFilter != null) {
				this.previousFilter.addAnnotationFilterUpdateListener(getPreviousFilterUpdateHandler());
				computeActiveAnnotations(ComputationMode.RECOMPUTE_JUST_THIS);
			}
		}
	}

	private AnnotationFilterUpdateListener getPreviousFilterUpdateHandler() {
		if (previousFilterUpdateHandler == null) {
			previousFilterUpdateHandler = activeAnnotations -> {
				computeActiveAnnotations(ComputationMode.RECOMPUTE_JUST_THIS);
			};
		}
		return previousFilterUpdateHandler;
	}

	public AnnotationFilter getPreviousFilter() {
		return previousFilter;
	}

	public Set<Annotation> getActiveAnnotations(ComputationMode mode) {
		if (!isComputed() || mode != ComputationMode.USE_LAST_RESULT) {
			computeActiveAnnotations(mode);
		}
		return activeAnnotations;
	}

	private boolean isComputed() {
		return activeAnnotations != null;
	}

	public void computeActiveAnnotations(ComputationMode mode) {
		Set<Annotation> inputAnnotations = null;
		if (mode == ComputationMode.USE_LAST_RESULT) {
			if (!isComputed()) {
				inputAnnotations = previousFilter.getActiveAnnotations(mode);
				activeAnnotations = applyFilter(inputAnnotations);
				notifyUpdateListeners();
			}
		} else {
			if (mode == ComputationMode.RECOMPUTE_ALL) {
				inputAnnotations = previousFilter.getActiveAnnotations(mode);
			} else {
				inputAnnotations = previousFilter.getActiveAnnotations(ComputationMode.USE_LAST_RESULT);
			}
			activeAnnotations = applyFilter(inputAnnotations);
			notifyUpdateListeners();
		}
	}

	protected abstract Set<Annotation> applyFilter(Set<Annotation> inputAnnotations);

	protected void notifyUpdateListeners() {
		updateListeners.forEach(l -> l.filterUpdated(activeAnnotations));
	}

	public void addAnnotationFilterUpdateListener(AnnotationFilterUpdateListener listener) {
		this.updateListeners.add(listener);
	}

	public void removeAnnotationFilterUpdateListener(AnnotationFilterUpdateListener listener) {
		this.updateListeners.remove(listener);
	}

	public void disconnect() {
		AnnotationFilter previousFilter = getPreviousFilter();
		Set<AnnotationFilterUpdateListener> updateListenersCopy = new HashSet<>(updateListeners);
		for (AnnotationFilterUpdateListener listener : updateListenersCopy) {
			removeAnnotationFilterUpdateListener(listener);
			previousFilter.addAnnotationFilterUpdateListener(listener);
		}
		setPreviousFilter(null);
		previousFilter.computeActiveAnnotations(ComputationMode.RECOMPUTE_JUST_THIS);
	}

}
