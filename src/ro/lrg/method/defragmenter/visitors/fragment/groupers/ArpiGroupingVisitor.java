package ro.lrg.method.defragmenter.visitors.fragment.groupers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;

import ro.lrg.method.defragmenter.utils.AbstractInternalCodeFragment;
import ro.lrg.method.defragmenter.utils.EnvyComputer;
import ro.lrg.method.defragmenter.utils.InternalCodeFragment;
import ro.lrg.method.defragmenter.utils.InternalCodeFragmentLeaf;
import ro.lrg.method.defragmenter.utils.MetricsComputer;
import ro.lrg.method.defragmenter.visitors.fragment.FragmentVisitor;

public class ArpiGroupingVisitor extends GroupingVisitor implements FragmentVisitor{
	private final int ATFDTreshold = 1;
	private final int FDPTreshold = 2;
	private final double LAATreshold = 0.5;
	
	public ArpiGroupingVisitor(String analyzedClass, IFile iFile, IJavaProject iJavaProject) {
		super(analyzedClass, iFile, iJavaProject);
	}
	
	private boolean fragmentIsEnvious(MetricsComputer metricsComputer) {
		return EnvyComputer.computeEnvy(metricsComputer.getATFD(), metricsComputer.getFDP(), metricsComputer.getLAA(), 
				ATFDTreshold, FDPTreshold, LAATreshold);
	}

	@Override
	public void visit(InternalCodeFragment fragment) {
		List<AbstractInternalCodeFragment> previousNodes = new ArrayList<>();
		
		List<AbstractInternalCodeFragment> children = fragment.getChildren();
		for(AbstractInternalCodeFragment child : children) {
			child.accept(this);
			AbstractInternalCodeFragment node = popLastNode();
			
			if (node instanceof InternalCodeFragment) {
				previousNodes.add(node);
			} else {
				MetricsComputer metricsComputer = MetricsComputer.getComputedMetrics(node);
				boolean isEnvy = fragmentIsEnvious(metricsComputer);
				if (!isEnvy) {
					previousNodes.add(node);
				} else {
					List<AbstractInternalCodeFragment> nodesToRemove = new ArrayList<>();
					
					InternalCodeFragmentLeaf accumulator = newInternalCodeFragmentLeaf();
					accumulator.addInternalStatementsOfFragment(node);
					
					Collections.reverse(previousNodes);
					for (AbstractInternalCodeFragment previousNode : previousNodes) {
						if (previousNode instanceof InternalCodeFragment) break;
						
						MetricsComputer metricsComputerBefore = MetricsComputer.getComputedMetrics(accumulator);
						InternalCodeFragmentLeaf temp = mergeLeaves(accumulator, (InternalCodeFragmentLeaf) previousNode);
						MetricsComputer metricsComputerAfter = MetricsComputer.getComputedMetrics(temp);
						if (metricsComputerBefore.includesFDPMapOf(metricsComputerAfter)) {
							accumulator.addInternalStatementsOfFragment(previousNode);
							nodesToRemove.add(previousNode);
						} else {
							break;
						}
					}
					Collections.reverse(previousNodes);
					
					Collections.reverse(accumulator.getInternalStatements());
					previousNodes.add(accumulator);
					
					for(AbstractInternalCodeFragment nodeToRemove : nodesToRemove) {
						previousNodes.remove(nodeToRemove);
					}
				}
			}
		}
		
		AbstractInternalCodeFragment parent = null;
		if (previousNodes.size() == 1 && previousNodes.get(0) instanceof InternalCodeFragmentLeaf) {
			MetricsComputer metricsComputerBefore = MetricsComputer.getComputedMetrics(previousNodes.get(0));
			MetricsComputer metricsComputerAfter = MetricsComputer.getComputedMetrics(fragment);
			if(metricsComputerBefore.includesFDPMapOf(metricsComputerAfter)) {
				parent = newInternalCodeFragmentLeaf();
			}
		}
		if (parent == null) {
			parent = newInternalCodeFragment();
			for (AbstractInternalCodeFragment previousNode : previousNodes) {
				((InternalCodeFragment) parent).addChild(previousNode);
			}
		}
		parent.addInternalStatementsOfFragment(fragment);
		
		pushIntoLastNode(parent);
	}

	@Override
	public void visit(InternalCodeFragmentLeaf fragment) {
		InternalCodeFragmentLeaf leaf = newInternalCodeFragmentLeaf();
		leaf.addInternalStatementsOfFragment(fragment);
		pushIntoLastNode(leaf);
	}
}
