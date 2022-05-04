package ro.lrg.method.defragmenter.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;

import ro.lrg.method.defragmenter.visitors.fragment.colourers.UncolorFragmentVisitor;

public class Selector {
	private static int colorCounter = 0;
	private static Map<String, ColouredFragment> colouredFragments = new HashMap<>();
	private static boolean colorMultipleFragments = false;
	private static final String MARKER = "ro.lrg.method.defragmenter.marker";
	private static final String FDP_DOESNT_EXIST = "FDP_DOESNT_EXIST";
	
	public static IMarker createIMarker(IResource iResource, Position position) {
		IMarker iMarker = null;
		try {
			iMarker = iResource.createMarker(MARKER);
			int start = position.getOffset();
			int end = position.getOffset() + position.getLength();
			iMarker.setAttribute(IMarker.SOURCE_ID, iResource.getName());
			iMarker.setAttribute(IMarker.CHAR_START, start);
			iMarker.setAttribute(IMarker.CHAR_END, end);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return iMarker;
	}
	
	private static String getMarkerIdentifier(IMarker iMarker) {
		String identifier = "";
		try {
			identifier += iMarker.getAttribute(IMarker.SOURCE_ID) + ";" 
						+ iMarker.getAttribute(IMarker.CHAR_START) + ";" 
						+ iMarker.getAttribute(IMarker.CHAR_END);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return identifier;
	}
	
	public static void uncolorAllColouredFragments() {
		UncolorFragmentVisitor visitor = new UncolorFragmentVisitor();
		List<ColouredFragment> colouredFragmentsList = colouredFragments.values().stream().collect(Collectors.toList());
		for (ColouredFragment colouredFragment : colouredFragmentsList) {
			colouredFragment.getFragment().accept(visitor);
		}
		colouredFragments.clear();
		colorCounter = 0;
	}
	
	public static String sameFDPAlreadyExists(AbstractInternalCodeFragment abstractInternalCodeFragment) {
		MetricsComputer metricsComputer1 = MetricsComputer.getComputedMetrics(abstractInternalCodeFragment);
		for (ColouredFragment colouredFragment : colouredFragments.values()) {
			MetricsComputer metricsComputer2 = MetricsComputer.getComputedMetrics(colouredFragment.getFragment());
			if (metricsComputer2.hasTheSameFDPMapWith(metricsComputer1)) return colouredFragment.getColor();
		}
		return FDP_DOESNT_EXIST;
	}
	
	public static void addAnnotation(ITextEditor iTextEditor, IMarker iMarker, Position position, AbstractInternalCodeFragment fragment) {
		String identifier = getMarkerIdentifier(iMarker);
		if (colouredFragments.containsKey(identifier)) return;
		
		//The DocumentProvider enables to get the document currently loaded in the editor
		IDocumentProvider iDocumentProvider = iTextEditor.getDocumentProvider();
		//This is the document we want to connect to. This is taken from 
		//the current editor input.
		IDocument iDocument = iDocumentProvider.getDocument(iTextEditor.getEditorInput());
		//The IannotationModel enables to add/remove/change annotation to a Document 
		//loaded in an Editor
		IAnnotationModel iAnnotationModel = iDocumentProvider.getAnnotationModel(iTextEditor.getEditorInput());
		//Note: The annotation type id specify that you want to create one of your 
		//annotations
		
		String color = sameFDPAlreadyExists(fragment);
		if (color.equals(FDP_DOESNT_EXIST)) {
			color = "annotationColor_" + colorCounter;
			colorCounter++;
		}
		SimpleMarkerAnnotation simpleMarkerAnnotation = new SimpleMarkerAnnotation(color, iMarker);
		//Finally add the new annotation to the model
		iAnnotationModel.connect(iDocument);
		iAnnotationModel.addAnnotation(simpleMarkerAnnotation, position);
		iAnnotationModel.disconnect(iDocument);
		
		colouredFragments.put(identifier, new ColouredFragment(fragment, simpleMarkerAnnotation, color));
	}
	
	public static void removeAnnotation(ITextEditor iTextEditor, IMarker iMarker) {
		String identifier = getMarkerIdentifier(iMarker);
		if (!colouredFragments.containsKey(identifier)) return;
		SimpleMarkerAnnotation simpleMarkerAnnotation = colouredFragments.remove(identifier).getAnnotation();
		
		//The DocumentProvider enables to get the document currently loaded in the editor
		IDocumentProvider iDocumentProvider = iTextEditor.getDocumentProvider();
		//This is the document we want to connect to. This is taken from 
		//the current editor input.
		IDocument iDocument = iDocumentProvider.getDocument(iTextEditor.getEditorInput());
		//The IannotationModel enables to add/remove/change annotation to a Document 
		//loaded in an Editor
		IAnnotationModel iAnnotationModel = iDocumentProvider.getAnnotationModel(iTextEditor.getEditorInput());
		//Finally add the new annotation to the model
		iAnnotationModel.connect(iDocument);
		iAnnotationModel.removeAnnotation(simpleMarkerAnnotation);
		iAnnotationModel.disconnect(iDocument);
	}
	
	public static void incrementColorCounter() {
		colorCounter++;
	}

	public static int getColorCounter() {
		return colorCounter;
	}
	
	public static boolean isColorMultipleFragments() {
		return colorMultipleFragments;
	}
	
	public static void setColorMultipleFragments(boolean colorMultipleFragments) {
		Selector.colorMultipleFragments = colorMultipleFragments;
	}
}