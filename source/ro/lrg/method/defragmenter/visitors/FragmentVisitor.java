package ro.lrg.method.defragmenter.visitors;

import ro.lrg.method.defragmenter.utils.InternalCodeFragment;
import ro.lrg.method.defragmenter.utils.InternalCodeFragmentLeaf;

public interface FragmentVisitor {
	
	void visit(InternalCodeFragment fragment);
	
	void visit(InternalCodeFragmentLeaf fragment);
}
