package ro.lrg.method.defragmenter.visitors.fragment;

import ro.lrg.method.defragmenter.utils.InternalCodeFragment;
import ro.lrg.method.defragmenter.utils.InternalCodeFragmentLeaf;

public interface FragmentVisitor {
	
	void visit(InternalCodeFragment fragment);
	
	void visit(InternalCodeFragmentLeaf fragment);
}
