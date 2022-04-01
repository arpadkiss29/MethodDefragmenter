package ro.lrg.method.defragmenter.visitors.fragment.grouping;

import java.util.Stack;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;

import ro.lrg.method.defragmenter.utils.AbstractInternalCodeFragment;
import ro.lrg.method.defragmenter.utils.InternalCodeFragment;
import ro.lrg.method.defragmenter.utils.InternalCodeFragmentLeaf;
import ro.lrg.method.defragmenter.utils.MetricsComputer;
import ro.lrg.method.defragmenter.visitors.fragment.FragmentVisitor;

public class ArpiGroupingVisitor implements FragmentVisitor{
//	fdp >= 1;
//	laa <= 0.5;
//	atfd<2
	private final Stack<AbstractInternalCodeFragment> lastNode;
	private final String analyzedClass;
	private final IFile iFile;
	private final IJavaProject iJavaProject;
	private final boolean considerStaticFieldAccess;
	private final boolean libraryCheck;
	private final int minBlockSize;
	
	public ArpiGroupingVisitor(String analyzedClass, IFile iFile, IJavaProject iJavaProject,
			boolean considerStaticFieldsAccess, boolean libraryCheck, int minBlockSize, int FDPTreshold) {
		lastNode = new Stack<>();
		this.analyzedClass = analyzedClass;
		this.iFile = iFile;
		this.iJavaProject = iJavaProject;
		this.considerStaticFieldAccess = considerStaticFieldsAccess;
		this.libraryCheck = libraryCheck;
		this.minBlockSize = minBlockSize;
	}
	
	public AbstractInternalCodeFragment popLastNode() {
		return lastNode.pop();
	}
	
	private InternalCodeFragment newInternalCodeFragment() {
		return new InternalCodeFragment(analyzedClass, iFile, iJavaProject);
	}
	
	private InternalCodeFragmentLeaf newInternalCodeFragmentLeaf() {
		return new InternalCodeFragmentLeaf(analyzedClass, iFile, iJavaProject);
	}
	
	private MetricsComputer computeDataAccessesOfFragment(AbstractInternalCodeFragment fragment) {
		MetricsComputer metricsComputer = new MetricsComputer();
		metricsComputer.computeDataAccesses(fragment, analyzedClass, considerStaticFieldAccess, libraryCheck, minBlockSize);
		return metricsComputer;
	}

	@Override
	public void visit(InternalCodeFragment fragment) {
//		InternalCodeFragment parent = newInternalCodeFragment();
//		parent.addInternalStatements(fragment.getInternalStatements());
//		
//		List<AbstractInternalCodeFragment> children = fragment.getChildren();
//		InternalCodeFragmentLeaf accumulatorLeaf = newInternalCodeFragmentLeaf();
//		
//		for(AbstractInternalCodeFragment child : children) {
//			
//			child.accept(this);
//			AbstractInternalCodeFragment node = lastNode.pop(); 
//			
//			if(node instanceof InternalCodeFragmentLeaf) {
//				MetricsComputer metricsComputerOfAccumulatorLeaf = computeDataAccessesOfFragment(accumulatorLeaf);
//				MetricsComputer metricsComputerOfAccumulatorNode = computeDataAccessesOfFragment(node);
//				if(metricsComputerOfAccumulatorLeaf.getFDP() == 0 && metricsComputerOfAccumulatorNode.getFDP() == 0) {
//					accumulatorLeaf.addInternalStatements(node.getInternalStatements());
//					
//				} else if(metricsComputerOfAccumulatorLeaf.getFDP() == 0 && metricsComputerOfAccumulatorNode.getFDP() != 0) {
//					if(accumulatorLeaf.getInternalStatementsSize() == 0) {
//						accumulatorLeaf.addInternalStatements(node.getInternalStatements());
//					} else {
//						parent.addChild(accumulatorLeaf);
//						accumulatorLeaf = newInternalCodeFragmentLeaf();
//						accumulatorLeaf.addInternalStatements(node.getInternalStatements());
//					}
//					
//				} else if(metricsComputerOfAccumulatorLeaf.getFDP() != 0 && metricsComputerOfAccumulatorNode.getFDP() == 0) {
//					parent.addChild(accumulatorLeaf);
//					accumulatorLeaf = newInternalCodeFragmentLeaf();
//					accumulatorLeaf.addInternalStatements(node.getInternalStatements());
//					
//				} else if(metricsComputerOfAccumulatorLeaf.getFDP() != 0 && metricsComputerOfAccumulatorNode.getFDP() != 0) {
//					if(metricsComputerOfAccumulatorLeaf.includesFDPMapOf(metricsComputerOfAccumulatorNode)) {
//						accumulatorLeaf.addInternalStatements(node.getInternalStatements());
//					} else {
//						parent.addChild(accumulatorLeaf);
//						accumulatorLeaf = newInternalCodeFragmentLeaf();
//						accumulatorLeaf.addInternalStatements(node.getInternalStatements());
//					}
//				}
//				
//			} else {
//				if(accumulatorLeaf.getInternalStatementsSize() == 0) {
//					parent.addChild(node);
//				} else {
//					parent.addChild(accumulatorLeaf);
//					accumulatorLeaf = newInternalCodeFragmentLeaf();
//					parent.addChild(node);
//				}
//			}
//		}
//		
//		if(accumulatorLeaf.getInternalStatementsSize() != 0) {
//			parent.addChild(accumulatorLeaf);
//		}
//		
//		lastNode.push(parent);
	}

	@Override
	public void visit(InternalCodeFragmentLeaf fragment) {
//		InternalCodeFragmentLeaf leaf = newInternalCodeFragmentLeaf();
//		leaf.addInternalStatements(fragment.getInternalStatements());
//		computeDataAccessesOfFragment(leaf);
//		lastNode.push(leaf);
	}
}
