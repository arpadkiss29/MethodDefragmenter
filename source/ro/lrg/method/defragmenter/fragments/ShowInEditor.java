package ro.lrg.method.defragmenter.fragments;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import methoddefragmenter.metamodel.entity.MFragment;
import ro.lrg.method.defragmenter.preferences.MethodDefragmenterPropertyStore;
import ro.lrg.method.defragmenter.utils.AbstractInternalCodeFragment;
import ro.lrg.method.defragmenter.utils.InternalCodeFragmentLeaf;
import ro.lrg.method.defragmenter.views.ColouredFragments;
import ro.lrg.xcore.metametamodel.ActionPerformer;
import ro.lrg.xcore.metametamodel.HListEmpty;
import ro.lrg.xcore.metametamodel.IActionPerformer;

@ActionPerformer
public class ShowInEditor implements IActionPerformer<Void, MFragment, HListEmpty> {
	@Override
	public Void performAction(MFragment arg0, HListEmpty arg1) {
		InternalCodeFragmentLeaf fragment = (InternalCodeFragmentLeaf) arg0.getUnderlyingObject();
		IFile iFile = fragment.getIFile();
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
			IDE.openEditor(page, iFile);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
			
		ITextEditor textEditor = (ITextEditor) page.getActiveEditor();

		MethodDefragmenterPropertyStore propertyStore = new MethodDefragmenterPropertyStore(fragment.getIJavaProject());
		
		if(!propertyStore.isApplyLongMethodIdentification()){
			try {
				fragment.colorEnvyLeafNodes(textEditor, iFile);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		} else {
			List<AbstractInternalCodeFragment> identifiedNodes = fragment.identifyFunctionalSegments();
			fragment.colorLongMethodFragments(textEditor, iFile, identifiedNodes);
		}
		ColouredFragments.addFragment(arg0);
		return null;
	}
}