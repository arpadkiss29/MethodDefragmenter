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
	
	public Saleh2GroupingVisitor(String analyzedClass, IFile iFile, IJavaProject iJavaProject,
			boolean considerStaticFieldsAccess, boolean libraryCheck, int minBlockSize, int FDPTreshold) {
		super(analyzedClass, iFile, iJavaProject, considerStaticFieldsAccess, libraryCheck, minBlockSize);
		this.FDPTreshold = FDPTreshold;
	}
	
	private boolean canMergeFragmentWithLeaf(AbstractInternalCodeFragment fragment, InternalCodeFragmentLeaf leaf, boolean reduceLimits) {
		int FDPOfFragment = computeMetricsOfFragment(fragment).getFDP();
		int FDPOfLeaf = computeMetricsOfFragment(leaf).getFDP();
		
		if (reduceLimits
				&& fragment.getInternalStatementsSize() > 0
				&& FDPOfFragment == 0
				&& FDPOfLeaf > 0) {
			return false;
		}
		
		if (FDPOfFragment == 0) return true;
		
		if (FDPOfFragment > FDPTreshold || FDPOfLeaf > FDPTreshold) return false;
		
		InternalCodeFragmentLeaf tempLeaf = newInternalCodeFragmentLeaf();
		tempLeaf.addInternalStatements(fragment.getInternalStatements());
		tempLeaf.addInternalStatements(leaf.getInternalStatements());
		MetricsComputer metricsComputerOfTempLeaf = computeMetricsOfFragment(tempLeaf);
		if (metricsComputerOfTempLeaf.getFDP() > FDPTreshold) return false;
		
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
				int accumulatorFDP = computeMetricsOfFragment(accumulator).getFDP();
				int leafFDP = computeMetricsOfFragment(leaf).getFDP();
				if (accumulatorFDP > 0 && leafFDP == 0) {
					zeroFDPAccumulator.addInternalStatements(leaf.getInternalStatements());
				} else {
					if (zeroFDPAccumulator.getInternalStatementsSize() > 0) {
						InternalCodeFragmentLeaf temp = newInternalCodeFragmentLeaf();
						temp.addInternalStatements(accumulator.getInternalStatements());
						temp.addInternalStatements(leaf.getInternalStatements());
						int tempFDP = computeMetricsOfFragment(temp).getFDP();
						
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
						if (!canMergeFragmentWithLeaf(accumulator, leaf, true)) {
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
		
		List<AbstractInternalCodeFragment> groupedNodes = breadthGrouping(fragment.getChildren());
		
		if (groupedNodes.size() == 1 && groupedNodes.get(0) instanceof InternalCodeFragmentLeaf
				&& canMergeFragmentWithLeaf(fragment, (InternalCodeFragmentLeaf) groupedNodes.get(0), false)) {
			parent = newInternalCodeFragmentLeaf();
			parent.addInternalStatements(fragment.getInternalStatements());
			parent.addInternalStatements(groupedNodes.get(0).getInternalStatements());
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
