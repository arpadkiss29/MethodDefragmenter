package ro.lrg.method.defragmenter.views;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;

public class SelectionView {
	private static final String MARKER = "ro.lrg.method.defragmenter.marker";

	public static IMarker createMarker(IResource res, Position position)
			throws CoreException {
		IMarker marker = null;
		marker = res.createMarker(MARKER);
		int start = position.getOffset();
		int end = position.getOffset() + position.getLength();
		marker.setAttribute(IMarker.CHAR_START, start);
		marker.setAttribute(IMarker.CHAR_END, end);
		return marker;
	}


	public static void addAnnotation(IMarker marker, ITextEditor editor, String annotationType, Position pos) {
		//The DocumentProvider enables to get the document currently loaded in the editor
		IDocumentProvider idp = editor.getDocumentProvider();

		//This is the document we want to connect to. This is taken from 
		//the current editor input.
		IDocument document = idp.getDocument(editor.getEditorInput());

		//The IannotationModel enables to add/remove/change annotation to a Document 
		//loaded in an Editor
		IAnnotationModel iamf = idp.getAnnotationModel(editor.getEditorInput());

		//Note: The annotation type id specify that you want to create one of your 
		//annotations
		SimpleMarkerAnnotation ma = new SimpleMarkerAnnotation(annotationType, marker);

		//Finally add the new annotation to the model
		iamf.connect(document);
		iamf.addAnnotation(ma, pos);
		iamf.disconnect(document);
	}
}