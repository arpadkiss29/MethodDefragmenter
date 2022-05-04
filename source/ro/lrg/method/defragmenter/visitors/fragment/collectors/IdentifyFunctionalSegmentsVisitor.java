package ro.lrg.method.defragmenter.visitors.fragment.collectors;

import java.util.ArrayList;
import java.util.List;

import ro.lrg.method.defragmenter.utils.AbstractInternalCodeFragment;
import ro.lrg.method.defragmenter.utils.InternalCodeFragment;
import ro.lrg.method.defragmenter.utils.InternalCodeFragmentLeaf;
import ro.lrg.method.defragmenter.visitors.fragment.FragmentVisitor;

public class IdentifyFunctionalSegmentsVisitor implements FragmentVisitor {
	
	private final double NCOCP2Treshold;
	private final List<AbstractInternalCodeFragment> functionalSegments = new ArrayList<>();
	
	public IdentifyFunctionalSegmentsVisitor(double NCOCP2Treshold) {
		this.NCOCP2Treshold = NCOCP2Treshold;
	}
	
	@Override
	public void visit(InternalCodeFragment fragment) {
		List<AbstractInternalCodeFragment> receivedNodes = new ArrayList<>();
		if (fragment.calculateNCOCP2() >= NCOCP2Treshold) {
			receivedNodes.add(fragment);
			return;
		}
		List<AbstractInternalCodeFragment> children = fragment.getChildren();
		for (AbstractInternalCodeFragment child : children) {
			receivedNodes.addAll(child.identifyFunctionalSegments(NCOCP2Treshold));
		}
	}
	
	@Override
	public void visit(InternalCodeFragmentLeaf fragment) {
		functionalSegments.add(fragment);
	}
	
	public List<AbstractInternalCodeFragment> getFunctionalSegments() {
		return functionalSegments;
	}
}
