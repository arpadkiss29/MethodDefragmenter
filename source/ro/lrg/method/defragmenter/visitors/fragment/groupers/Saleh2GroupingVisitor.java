package ro.lrg.method.defragmenter.visitors.fragment.groupers;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;
import ro.lrg.method.defragmenter.utils.AbstractInternalCodeFragment;
import ro.lrg.method.defragmenter.utils.InternalCodeFragment;
import ro.lrg.method.defragmenter.utils.InternalCodeFragmentLeaf;
import ro.lrg.method.defragmenter.utils.MetricsComputer;
import ro.lrg.method.defragmenter.visitors.fragment.FragmentVisitor;

public class Saleh2GroupingVisitor extends GroupingVisitor implements FragmentVisitor {
	private final int FDPTreshold;
	
	public Saleh2GroupingVisitor(String analyzedClass, IFile iFile, IJavaProject iJavaProject, int FDPTreshold) {
		super(analyzedClass, iFile, iJavaProject);
		this.FDPTreshold = FDPTreshold;
	}
	
	private boolean canMergeLeaves(InternalCodeFragmentLeaf fragment, InternalCodeFragmentLeaf leaf, boolean reduceLimits) {
		int fragmentFDP = MetricsComputer.getComputedMetrics(fragment).getFDP();
		int leafFDP = MetricsComputer.getComputedMetrics(leaf).getFDP();
		
		if (reduceLimits
				&& fragment.getInternalStatementsSize() > 0
				&& fragmentFDP == 0
				&& leafFDP > 0) {
			return false;
		}
		
		if (fragmentFDP == 0) return true;
		if (fragmentFDP > FDPTreshold || leafFDP > FDPTreshold) return false;
		
		InternalCodeFragmentLeaf temp = newInternalCodeFragmentLeaf();
		temp.addInternalStatements(fragment.getInternalStatements());
		temp.addInternalStatements(leaf.getInternalStatements());
		int tempFDP = MetricsComputer.getComputedMetrics(temp).getFDP();
		if (tempFDP > FDPTreshold) return false;
		
		return true;
	}
	
	private boolean parentCanBeLeaf(InternalCodeFragment parent, List<AbstractInternalCodeFragment> groupedNodes) {
		if (groupedNodes.size() != 1 || !(groupedNodes.get(0) instanceof InternalCodeFragmentLeaf)) return false;
		int parentFDP = MetricsComputer.getComputedMetrics(parent).getFDP();
		if (parentFDP > FDPTreshold) return false;
		return true;
	}
	
	private List<AbstractInternalCodeFragment> breadthGrouping(List<AbstractInternalCodeFragment> children) {
		List<AbstractInternalCodeFragment> groupedNodes = new ArrayList<>();
		
		InternalCodeFragmentLeaf accumulator = newInternalCodeFragmentLeaf();
		InternalCodeFragmentLeaf zeroFDPAccumulator = newInternalCodeFragmentLeaf();
		
		for (AbstractInternalCodeFragment child : children) {
			child.accept(this);
			AbstractInternalCodeFragment node = popLastNode();
			
			if (node instanceof InternalCodeFragment) {
				if (accumulator.getInternalStatementsSize() > 0) {
					groupedNodes.add(accumulator);
				}
				if (zeroFDPAccumulator.getInternalStatementsSize() > 0) {
					groupedNodes.add(zeroFDPAccumulator);
				}
				groupedNodes.add(node);
			} else {
				InternalCodeFragmentLeaf leaf = (InternalCodeFragmentLeaf) node;
				int accumulatorFDP = MetricsComputer.getComputedMetrics(accumulator).getFDP();
				int leafFDP = MetricsComputer.getComputedMetrics(leaf).getFDP();
				if (accumulatorFDP > 0 && leafFDP == 0) {
					zeroFDPAccumulator.addInternalStatements(leaf.getInternalStatements());
				} else {
					if (zeroFDPAccumulator.getInternalStatementsSize() > 0) {
						InternalCodeFragmentLeaf temp = newInternalCodeFragmentLeaf();
						temp.addInternalStatements(accumulator.getInternalStatements());
						temp.addInternalStatements(leaf.getInternalStatements());
						int tempFDP = MetricsComputer.getComputedMetrics(temp).getFDP();
						
						if (tempFDP <= FDPTreshold) {
							accumulator.addInternalStatements(zeroFDPAccumulator.getInternalStatements());
							zeroFDPAccumulator = newInternalCodeFragmentLeaf();
						} else {
							groupedNodes.add(accumulator);
							accumulator = newInternalCodeFragmentLeaf();
							groupedNodes.add(zeroFDPAccumulator);
							zeroFDPAccumulator = newInternalCodeFragmentLeaf();
						}
					} else {
						if (!canMergeLeaves(accumulator, leaf, true)) {
							groupedNodes.add(accumulator);
							accumulator = newInternalCodeFragmentLeaf();
						}
					}
					accumulator.addInternalStatements(leaf.getInternalStatements());
				}
			}
		}
		
        if (accumulator.getInternalStatementsSize() != 0){
        	groupedNodes.add(accumulator);
        }
        if (zeroFDPAccumulator.getInternalStatementsSize() != 0) {
        	groupedNodes.add(zeroFDPAccumulator);
        }
        
        return groupedNodes;
	}
	
	@Override
	public void visit(InternalCodeFragment fragment) {
		AbstractInternalCodeFragment parent = newInternalCodeFragment();
		parent.addInternalStatements(fragment.getInternalStatements());
		
		List<AbstractInternalCodeFragment> groupedNodes = breadthGrouping(fragment.getChildren());
		
		if (parentCanBeLeaf((InternalCodeFragment) parent, groupedNodes)) {
			parent = newInternalCodeFragmentLeaf();
			parent.addInternalStatements(fragment.getInternalStatements());
		} else {
			for (AbstractInternalCodeFragment node : groupedNodes) {
				((InternalCodeFragment) parent).addChild(node);
			}
		}
		pushIntoLastNode(parent);
	}

	@Override
	public void visit(InternalCodeFragmentLeaf fragment) {
		InternalCodeFragmentLeaf leaf = newInternalCodeFragmentLeaf();
		leaf.addInternalStatements(fragment.getInternalStatements());
		pushIntoLastNode(leaf);
	}
}
