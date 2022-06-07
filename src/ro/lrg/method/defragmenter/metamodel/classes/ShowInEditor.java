package ro.lrg.method.defragmenter.metamodel.classes;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.PartInitException;

import methoddefragmenter.metamodel.entity.MClass;
import ro.lrg.xcore.metametamodel.ActionPerformer;
import ro.lrg.xcore.metametamodel.HListEmpty;
import ro.lrg.xcore.metametamodel.IActionPerformer;

@ActionPerformer
public class ShowInEditor implements IActionPerformer<Void, MClass, HListEmpty> {
	@Override
	public Void performAction(MClass arg0, HListEmpty arg1) {
		IType iType = arg0.getUnderlyingObject();
		try {
			JavaUI.openInEditor(iType, true, true);
		} catch (PartInitException | JavaModelException e) {
			e.printStackTrace();
		}
		return null;
	}
}


