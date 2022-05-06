package ro.lrg.method.defragmenter.metamodel.methods;

import methoddefragmenter.metamodel.entity.MMethod;
import ro.lrg.method.defragmenter.utils.Selector;
import ro.lrg.xcore.metametamodel.ActionPerformer;
import ro.lrg.xcore.metametamodel.HListEmpty;
import ro.lrg.xcore.metametamodel.IActionPerformer;

@ActionPerformer
public class UncolorEnviousFragments  implements IActionPerformer<Void, MMethod, HListEmpty> {
	@Override
	public Void performAction(MMethod arg0, HListEmpty arg1) {
		Selector.uncolorAllColouredFragments();
		return null;
	}
}
