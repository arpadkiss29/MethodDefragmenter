package ro.lrg.method.defragmenter.metamodel.fragments;

import methoddefragmenter.metamodel.entity.MFragment;
import ro.lrg.method.defragmenter.utils.AbstractInternalCodeFragment;
import ro.lrg.method.defragmenter.utils.Selector;
import ro.lrg.method.defragmenter.visitors.fragment.colorers.ColorFragmentVisitor;
import ro.lrg.xcore.metametamodel.ActionPerformer;
import ro.lrg.xcore.metametamodel.HListEmpty;
import ro.lrg.xcore.metametamodel.IActionPerformer;

@ActionPerformer
public class Color implements IActionPerformer<Void, MFragment, HListEmpty> {
	@Override
	public Void performAction(MFragment arg0, HListEmpty arg1) {
		if (!Selector.isColorMultipleFragments()) Selector.uncolorAllColouredFragments();
		AbstractInternalCodeFragment fragment = arg0.getUnderlyingObject();
		ColorFragmentVisitor visitor = new ColorFragmentVisitor();
		fragment.accept(visitor);
		return null;
	}
}