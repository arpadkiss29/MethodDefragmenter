package ro.lrg.method.defragmenter.visitors.fragment.colourers;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import ro.lrg.method.defragmenter.utils.AbstractInternalCodeFragment;
import ro.lrg.method.defragmenter.utils.InternalCodeFragment;
import ro.lrg.method.defragmenter.visitors.fragment.FragmentVisitor;

public abstract class ColourerFragmentVisitor implements FragmentVisitor{
	@Override
	public void visit(InternalCodeFragment fragment) {
		List<AbstractInternalCodeFragment> children = fragment.getChildren();
		for (AbstractInternalCodeFragment child : children) {
			child.accept(this);
		}
	}
	
	protected ITextEditor createITextEditor(IFile iFile) {
		IWorkbenchPage iWorkbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
			IDE.openEditor(iWorkbenchPage, iFile);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		ITextEditor iTextEditor = (ITextEditor) iWorkbenchPage.getActiveEditor();
		return iTextEditor;
	}
}
