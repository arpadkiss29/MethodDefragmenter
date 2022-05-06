package ro.lrg.method.defragmenter.metamodel.methods;

import methoddefragmenter.metamodel.entity.MFragment;
import methoddefragmenter.metamodel.entity.MMethod;
import ro.lrg.method.defragmenter.utils.Selector;
import ro.lrg.xcore.metametamodel.ActionPerformer;
import ro.lrg.xcore.metametamodel.Group;
import ro.lrg.xcore.metametamodel.HListEmpty;
import ro.lrg.xcore.metametamodel.IActionPerformer;

@ActionPerformer
public class ColorEnviousFragments implements IActionPerformer<Void, MMethod, HListEmpty> {
	@Override
	public Void performAction(MMethod arg0, HListEmpty arg1) {
		Selector.uncolorAllColouredFragments();
		Group<MFragment> enviousFragments = arg0.enviousFragmentGroup();
		Selector.setColorMultipleFragments(true);
		enviousFragments.getElements().forEach(MFragment::color);
		Selector.setColorMultipleFragments(false);
		return null;
	}
}