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
//	fdp <= 2;
//	laa <= 0.5;
//	atfd>=1
	private final int ATFDTreshold = 1;
	private final int FDPTreshold = 2;
	private final double LAATreshold = 0.5;
	
	public ArpiGroupingVisitor(String analyzedClass, IFile iFile, IJavaProject iJavaProject,
			boolean considerStaticFieldsAccess, boolean libraryCheck, int minBlockSize) {
		super(analyzedClass, iFile, iJavaProject, considerStaticFieldsAccess, libraryCheck, minBlockSize);
	}
	
	private boolean fragmentIsEnvy(MetricsComputer metricsComputer) {
		return EnvyComputer.computeEnvy(metricsComputer.getATFD(), metricsComputer.getFDPMap(), metricsComputer.getLAA(), 
				ATFDTreshold, FDPTreshold, LAATreshold);
	}
	
	private boolean canMergeParentWithLeaf(AbstractInternalCodeFragment baseFragment, AbstractInternalCodeFragment toBeMerged) {
		AbstractInternalCodeFragment temp = newInternalCodeFragmentLeaf();
		temp.addInternalStatements(baseFragment.getInternalStatements());
		MetricsComputer metricsComputerBefore = computeMetricsOfFragment(temp);
		temp.addInternalStatements(toBeMerged.getInternalStatements());
		MetricsComputer metricsComputerAfter = computeMetricsOfFragment(temp);
		
		return fragmentIsEnvy(metricsComputerAfter) && metricsComputerBefore.includesFDPMapOf(metricsComputerAfter);
	}

	@Override
	public void visit(InternalCodeFragment fragment) {
		InternalCodeFragment parent = newInternalCodeFragment();
		parent.addInternalStatements(fragment.getInternalStatements());
		
		List<AbstractInternalCodeFragment> previousNodes = new ArrayList<>();
		
		List<AbstractInternalCodeFragment> children = fragment.getChildren();
		
		for(AbstractInternalCodeFragment child : children) {
			child.accept(this);
			AbstractInternalCodeFragment node = popLastNode();
			
			if (children.size() == 1) {
				if (canMergeParentWithLeaf(node, fragment)) {
					AbstractInternalCodeFragment leaf = newInternalCodeFragmentLeaf();
					leaf.addInternalStatements(parent.getInternalStatements());
					leaf.addInternalStatements(node.getInternalStatements());
					pushIntoLastNode(leaf);
					return;
				}
			}
			
			MetricsComputer metricsComputer = computeMetricsOfFragment(node);
			boolean envy = fragmentIsEnvy(metricsComputer);
			
			if (!envy || node instanceof InternalCodeFragment) {
				previousNodes.add(node);
			} else {
				List<AbstractInternalCodeFragment> nodesToRemove = new ArrayList<>();
				
				AbstractInternalCodeFragment accumulator = newInternalCodeFragmentLeaf();
				accumulator.addInternalStatements(node.getInternalStatements());
				
				Collections.reverse(previousNodes);
				for (AbstractInternalCodeFragment previousNode : previousNodes) {
					if (previousNode instanceof InternalCodeFragment) break;
					if (canMergeParentWithLeaf(accumulator, previousNode)) {
						accumulator.addInternalStatements(previousNode.getAllInternalStatementsOfTree());
						nodesToRemove.add(previousNode);
					} else {
						break;
					}
				}
				Collections.reverse(previousNodes);
				previousNodes.add(accumulator);
				
				for(AbstractInternalCodeFragment nodeToRemove : nodesToRemove) {
					previousNodes.remove(nodeToRemove);
				}
			}
		}
		
		if (previousNodes.size() == 1) {
			if (canMergeParentWithLeaf(previousNodes.get(0), fragment)) {
				AbstractInternalCodeFragment leaf = newInternalCodeFragmentLeaf();
				leaf.addInternalStatements(parent.getInternalStatements());
				leaf.addInternalStatements(previousNodes.get(0).getInternalStatements());
				pushIntoLastNode(leaf);
				return;
			}
		}
		
		for (AbstractInternalCodeFragment previousNode : previousNodes) {
			parent.addChild(previousNode);
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
