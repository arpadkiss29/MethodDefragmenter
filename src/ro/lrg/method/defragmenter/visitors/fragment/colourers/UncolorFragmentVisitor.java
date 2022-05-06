package ro.lrg.method.defragmenter.visitors.fragment.colourers;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.ITextEditor;

import ro.lrg.method.defragmenter.utils.InternalCodeFragmentLeaf;
import ro.lrg.method.defragmenter.utils.Selector;

public class UncolorFragmentVisitor extends ColourerFragmentVisitor{
	@Override
	public void visit(InternalCodeFragmentLeaf fragment) {
		IFile iFile = fragment.getIFile();
		Position position = fragment.getPosition();
		ITextEditor iTextEditor = createITextEditor(iFile);
		IMarker iMarker = Selector.createIMarker(iFile, position);
		Selector.removeAnnotation(iTextEditor, iMarker);
	}
}
