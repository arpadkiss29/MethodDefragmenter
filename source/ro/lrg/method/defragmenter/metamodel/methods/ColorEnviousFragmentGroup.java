package ro.lrg.method.defragmenter.metamodel.methods;

import methoddefragmenter.metamodel.entity.MFragment;
import methoddefragmenter.metamodel.entity.MMethod;
import ro.lrg.xcore.metametamodel.ActionPerformer;
import ro.lrg.xcore.metametamodel.Group;
import ro.lrg.xcore.metametamodel.HListEmpty;
import ro.lrg.xcore.metametamodel.IActionPerformer;

@ActionPerformer
public class ColorEnviousFragmentGroup implements IActionPerformer<Void, MMethod, HListEmpty> {
	@Override
	public Void performAction(MMethod arg0, HListEmpty arg1) {
		Group<MFragment> enviousFragments = arg0.enviousFragmentGroup();
		enviousFragments.getElements().forEach(MFragment::showInEditor);
		return null;
	}
}