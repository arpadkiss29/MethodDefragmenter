package ro.lrg.method.defragmenter.utils;

import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;

public class ColoredFragment {
	private final AbstractInternalCodeFragment fragment;
	private final SimpleMarkerAnnotation annotation;
	private final String color;
	
	public ColoredFragment(AbstractInternalCodeFragment fragment, SimpleMarkerAnnotation annotation, String color) {
		this.fragment = fragment;
		this.annotation = annotation;
		this.color = color;
	}

	public AbstractInternalCodeFragment getFragment() {
		return fragment;
	}

	public SimpleMarkerAnnotation getAnnotation() {
		return annotation;
	}

	public String getColor() {
		return color;
	}
}
