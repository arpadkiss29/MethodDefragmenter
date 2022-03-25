package ro.lrg.method.defragmenter.visitors;

import java.util.ArrayList;
import java.util.List;

import ro.lrg.method.defragmenter.utils.InternalCodeFragment;
import ro.lrg.method.defragmenter.utils.InternalCodeFragmentLeaf;

public class AllLeavesVisitor implements FragmentVisitor{
	
	List<InternalCodeFragmentLeaf> leaves = new ArrayList<>();

	@Override
	public void visit(InternalCodeFragment fragment) {
		fragment.getChildren().forEach(f->f.accept(this));
	}

	@Override
	public void visit(InternalCodeFragmentLeaf fragment) {
		leaves.add(fragment);
	}
	
	public List<InternalCodeFragmentLeaf> getLeaves() {
		return leaves;
	}
}
