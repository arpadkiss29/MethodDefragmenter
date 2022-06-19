package ro.lrg.method.defragmenter.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;

import ro.lrg.method.defragmenter.visitors.fragment.colorers.UncolorFragmentVisitor;

public class Selector {
	private static int colorCounter = 0;
	private static Map<String, ColoredFragment> coloredFragments = new HashMap<>();
	private static boolean colorMultipleFragments = false;
	private static final String MARKER = "ro.lrg.method.defragmenter.marker";
	private static final String FDP_DOESNT_EXIST = "FDP_DOESNT_EXIST";
	
	private static ITextEditor createITextEditor(IFile iFile) {
		IWorkbenchPage iWorkbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
			IDE.openEditor(iWorkbenchPage, iFile);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		ITextEditor iTextEditor = (ITextEditor) iWorkbenchPage.getActiveEditor();
		return iTextEditor;
	}
	
	private static IMarker createIMarker(IResource iResource, Position position) {
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
	
	private static String sameFDPMapAlreadyExists(AbstractInternalCodeFragment abstractInternalCodeFragment) {
		MetricsComputer metricsComputer1 = MetricsComputer.getComputedMetrics(abstractInternalCodeFragment);
		for (ColoredFragment colouredFragment : coloredFragments.values()) {
			MetricsComputer metricsComputer2 = MetricsComputer.getComputedMetrics(colouredFragment.getFragment());
			if (metricsComputer2.hasTheSameFDPMapWith(metricsComputer1)) return colouredFragment.getColor();
		}
		return FDP_DOESNT_EXIST;
	}
	
	public static void uncolorAllColouredFragments() {
		UncolorFragmentVisitor visitor = new UncolorFragmentVisitor();
		List<ColoredFragment> colouredFragmentsList = coloredFragments.values().stream().collect(Collectors.toList());
		for (ColoredFragment colouredFragment : colouredFragmentsList) {
			colouredFragment.getFragment().accept(visitor);
		}
		coloredFragments.clear();
		colorCounter = 0;
	}
	
	public static void addAnnotation(AbstractInternalCodeFragment fragment) {
		IFile iFile = fragment.getIFile();
		Position position = fragment.getPosition();
		ITextEditor iTextEditor = createITextEditor(iFile);
		IMarker iMarker = Selector.createIMarker(iFile, position);
		String identifier = getMarkerIdentifier(iMarker);
		
		if (coloredFragments.containsKey(identifier)) return;
		
		IDocumentProvider iDocumentProvider = iTextEditor.getDocumentProvider();
		IDocument iDocument = iDocumentProvider.getDocument(iTextEditor.getEditorInput());
		IAnnotationModel iAnnotationModel = iDocumentProvider.getAnnotationModel(iTextEditor.getEditorInput());
		
		String color = sameFDPMapAlreadyExists(fragment);
		if (color.equals(FDP_DOESNT_EXIST)) {
			color = "annotationColor_" + colorCounter;
			colorCounter++;
		}
		
		SimpleMarkerAnnotation simpleMarkerAnnotation = new SimpleMarkerAnnotation(color, iMarker);
		iAnnotationModel.connect(iDocument);
		iAnnotationModel.addAnnotation(simpleMarkerAnnotation, position);
		iAnnotationModel.disconnect(iDocument);
		
		coloredFragments.put(identifier, new ColoredFragment(fragment, simpleMarkerAnnotation, color));
	}
	
	public static void removeAnnotation(AbstractInternalCodeFragment fragment) {
		IFile iFile = fragment.getIFile();
		Position position = fragment.getPosition();
		ITextEditor iTextEditor = createITextEditor(iFile);
		IMarker iMarker = Selector.createIMarker(iFile, position);
		String identifier = getMarkerIdentifier(iMarker);
		
		if (!coloredFragments.containsKey(identifier)) return;
		
		SimpleMarkerAnnotation simpleMarkerAnnotation = coloredFragments.remove(identifier).getAnnotation();
		
		IDocumentProvider iDocumentProvider = iTextEditor.getDocumentProvider();
		IDocument iDocument = iDocumentProvider.getDocument(iTextEditor.getEditorInput());
		IAnnotationModel iAnnotationModel = iDocumentProvider.getAnnotationModel(iTextEditor.getEditorInput());
		
		iAnnotationModel.connect(iDocument);
		iAnnotationModel.removeAnnotation(simpleMarkerAnnotation);
		iAnnotationModel.disconnect(iDocument);
	}
	
	public static Map<String, ColoredFragment> getColoredFragments() {
		return coloredFragments;
	}
	
	public static boolean isColorMultipleFragments() {
		return colorMultipleFragments;
	}
	
	public static void setColorMultipleFragments(boolean colorMultipleFragments) {
		Selector.colorMultipleFragments = colorMultipleFragments;
	}
}