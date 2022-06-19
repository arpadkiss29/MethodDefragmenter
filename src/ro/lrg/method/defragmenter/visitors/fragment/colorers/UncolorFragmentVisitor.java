package ro.lrg.method.defragmenter.visitors.fragment.colorers;

import ro.lrg.method.defragmenter.utils.InternalCodeFragmentLeaf;
import ro.lrg.method.defragmenter.utils.Selector;

public class UncolorFragmentVisitor extends ColourerFragmentVisitor{
	@Override
	public void visit(InternalCodeFragmentLeaf fragment) {
		Selector.removeAnnotation(fragment);
	}
}
