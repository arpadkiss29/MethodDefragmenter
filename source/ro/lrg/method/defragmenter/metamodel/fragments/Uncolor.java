package ro.lrg.method.defragmenter.metamodel.fragments;

import methoddefragmenter.metamodel.entity.MFragment;
import ro.lrg.method.defragmenter.utils.AbstractInternalCodeFragment;
import ro.lrg.method.defragmenter.visitors.fragment.colourers.UncolorFragmentVisitor;
import ro.lrg.xcore.metametamodel.ActionPerformer;
import ro.lrg.xcore.metametamodel.HListEmpty;
import ro.lrg.xcore.metametamodel.IActionPerformer;

@ActionPerformer
public class Uncolor implements IActionPerformer<Void, MFragment, HListEmpty> {
	@Override
	public Void performAction(MFragment arg0, HListEmpty arg1) {
		AbstractInternalCodeFragment fragment = arg0.getUnderlyingObject();
		UncolorFragmentVisitor visitor = new UncolorFragmentVisitor();
		fragment.accept(visitor);
		return null;
	}
}
