package ro.lrg.method.defragmenter.visitors.fragment.colorers;

import java.util.List;

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
}
