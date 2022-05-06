package ro.lrg.method.defragmenter.visitors.fragment.collectors;

import java.util.ArrayList;
import java.util.List;

import ro.lrg.method.defragmenter.utils.AbstractInternalCodeFragment;
import ro.lrg.method.defragmenter.utils.InternalCodeFragment;
import ro.lrg.method.defragmenter.utils.InternalCodeFragmentLeaf;
import ro.lrg.method.defragmenter.visitors.fragment.FragmentVisitor;

public class AllNodesVisitor implements FragmentVisitor {
	
	private final List<AbstractInternalCodeFragment> allNodes = new ArrayList<>();

	@Override
	public void visit(InternalCodeFragment fragment) {
		fragment.getChildren().forEach(f->f.accept(this));
		allNodes.add(fragment);
	}

	@Override
	public void visit(InternalCodeFragmentLeaf fragment) {
		allNodes.add(fragment);
	}
	
	public List<AbstractInternalCodeFragment> getAllNodes() {
		return allNodes;
	}
}
