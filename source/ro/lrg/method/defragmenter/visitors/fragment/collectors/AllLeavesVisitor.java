package ro.lrg.method.defragmenter.visitors.fragment.collectors;

import java.util.ArrayList;
import java.util.List;

import ro.lrg.method.defragmenter.utils.AbstractInternalCodeFragment;
import ro.lrg.method.defragmenter.utils.InternalCodeFragment;
import ro.lrg.method.defragmenter.utils.InternalCodeFragmentLeaf;
import ro.lrg.method.defragmenter.visitors.fragment.FragmentVisitor;

public class AllLeavesVisitor implements FragmentVisitor{
	
	private List<AbstractInternalCodeFragment> allLeaves = new ArrayList<>();

	@Override
	public void visit(InternalCodeFragment fragment) {
		fragment.getChildren().forEach(f->f.accept(this));
	}

	@Override
	public void visit(InternalCodeFragmentLeaf fragment) {
		allLeaves.add(fragment);
	}
	
	public List<AbstractInternalCodeFragment> getAllLeaves() {
		return allLeaves;
	}
}
