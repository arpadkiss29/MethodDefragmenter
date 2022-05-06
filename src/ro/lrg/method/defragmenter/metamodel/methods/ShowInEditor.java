package ro.lrg.method.defragmenter.metamodel.methods;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.PartInitException;

import methoddefragmenter.metamodel.entity.MMethod;
import ro.lrg.xcore.metametamodel.ActionPerformer;
import ro.lrg.xcore.metametamodel.HListEmpty;
import ro.lrg.xcore.metametamodel.IActionPerformer;

@ActionPerformer
public class ShowInEditor implements IActionPerformer<Void, MMethod, HListEmpty> {
	@Override
	public Void performAction(MMethod arg0, HListEmpty arg1) {
		try {
			JavaUI.openInEditor(arg0.getUnderlyingObject(), true, true);
		} catch (PartInitException | JavaModelException e) {
			e.printStackTrace();
		}
		return null;
	}
}