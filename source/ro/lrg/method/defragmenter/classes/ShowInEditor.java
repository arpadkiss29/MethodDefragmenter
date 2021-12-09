package ro.lrg.method.defragmenter.classes;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.PartInitException;

import methoddefragmenter.metamodel.entity.MClass;
import ro.lrg.xcore.metametamodel.ActionPerformer;
import ro.lrg.xcore.metametamodel.HListEmpty;
import ro.lrg.xcore.metametamodel.IActionPerformer;

@ActionPerformer
public class ShowInEditor implements IActionPerformer<Void, MClass, HListEmpty>{
	@Override
	public Void performAction(MClass arg0, HListEmpty arg1) {
		try {
			JavaUI.openInEditor(arg0.getUnderlyingObject(), true, true);
		} catch(PartInitException | JavaModelException e) {
			e.printStackTrace();
		}
		return null;
	}
}
