package ro.lrg.method.defragmenter.visitors.fragment.groupers;

import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;
import ro.lrg.method.defragmenter.utils.AbstractInternalCodeFragment;
import ro.lrg.method.defragmenter.utils.InternalCodeFragment;
import ro.lrg.method.defragmenter.utils.InternalCodeFragmentLeaf;
import ro.lrg.method.defragmenter.visitors.fragment.FragmentVisitor;

public class Saleh1GroupingVisitor extends GroupingVisitor implements FragmentVisitor {
	private final int FDPTreshold;

	public Saleh1GroupingVisitor(String analyzedClass, IFile iFile, IJavaProject iJavaProject,
			boolean considerStaticFieldsAccess, boolean libraryCheck, int minBlockSize, int FDPTreshold) {
		super(analyzedClass, iFile, iJavaProject, considerStaticFieldsAccess, libraryCheck, minBlockSize);
		this.FDPTreshold = FDPTreshold;
	}
	
	@Override
	public void visit(InternalCodeFragment fragment) {
		InternalCodeFragment parent = newInternalCodeFragment();
		parent.addInternalStatements(fragment.getInternalStatements());
		
		List<AbstractInternalCodeFragment> children = fragment.getChildren();
		InternalCodeFragmentLeaf accumulator = newInternalCodeFragmentLeaf();
		
		for(AbstractInternalCodeFragment child : children) {
			child.accept(this);
			AbstractInternalCodeFragment node = popLastNode();
			
			if(node instanceof InternalCodeFragmentLeaf) {
				InternalCodeFragmentLeaf temp = newInternalCodeFragmentLeaf();
				temp.addInternalStatements(accumulator.getInternalStatements());
				temp.addInternalStatements(node.getInternalStatements());
				int tempFDP = computeMetricsOfFragment(temp).getFDP();
				
				if(tempFDP > FDPTreshold) {
					parent.addChild(accumulator);
					accumulator = newInternalCodeFragmentLeaf();
				}
				accumulator.addInternalStatements(node.getInternalStatements());				
			} else {
				if(accumulator.getInternalStatementsSize() != 0) {
					parent.addChild(accumulator);
					accumulator = newInternalCodeFragmentLeaf();
				}
				parent.addChild(node);
			}
		}
		
		if(accumulator.getInternalStatementsSize() != 0) {
			parent.addChild(accumulator);
		}
		
		pushIntoLastNode(parent);
	}
	
//	@Override
//	public void visit(InternalCodeFragment fragment) {
//		InternalCodeFragment parent = newInternalCodeFragment();
//		parent.addInternalStatements(fragment.getInternalStatements());
//		
//		List<AbstractInternalCodeFragment> children = fragment.getChildren();
//		InternalCodeFragmentLeaf accumulator = newInternalCodeFragmentLeaf();
//		
//		for(AbstractInternalCodeFragment child : children) {
//			
//			child.accept(this);
//			AbstractInternalCodeFragment node = popLastNode();
//			
//			if(node instanceof InternalCodeFragmentLeaf) {
//				MetricsComputer metricsComputerOfAccumulator = computeMetricsOfFragment(accumulator);
//				MetricsComputer metricsComputerOfNode = computeMetricsOfFragment(node);
//				int accumulatorFDP = metricsComputerOfAccumulator.getFDP();
//				int nodeFDP = metricsComputerOfAccumulator.getFDP();
//				
//				if(accumulatorFDP == 0 && nodeFDP == 0) {
//					accumulator.addInternalStatements(node.getInternalStatements());
//					
//				} else if(accumulatorFDP == 0 && nodeFDP != 0) {
//					if(accumulator.getInternalStatementsSize() == 0) {
//						accumulator.addInternalStatements(node.getInternalStatements());
//					} else {
//						parent.addChild(accumulator);
//						accumulator = newInternalCodeFragmentLeaf();
//						accumulator.addInternalStatements(node.getInternalStatements());
//					}
//					
//				} else if(accumulatorFDP != 0 && nodeFDP == 0) {
//					parent.addChild(accumulator);
//					accumulator = newInternalCodeFragmentLeaf();
//					accumulator.addInternalStatements(node.getInternalStatements());
//					
//				} else if(accumulatorFDP != 0 && nodeFDP != 0) {
//					if(metricsComputerOfAccumulator.includesFDPMapOf(metricsComputerOfNode)) {
//						accumulator.addInternalStatements(node.getInternalStatements());
//					} else {
//						parent.addChild(accumulator);
//						accumulator = newInternalCodeFragmentLeaf();
//						accumulator.addInternalStatements(node.getInternalStatements());
//					}
//				}
//				
//			} else {
//				if(accumulator.getInternalStatementsSize() == 0) {
//					parent.addChild(node);
//				} else {
//					parent.addChild(accumulator);
//					accumulator = newInternalCodeFragmentLeaf();
//					parent.addChild(node);
//				}
//			}
//		}
//		
//		if(accumulator.getInternalStatementsSize() != 0) {
//			parent.addChild(accumulator);
//		}
//		
//		pushIntoLastNode(parent);
//	}

	@Override
	public void visit(InternalCodeFragmentLeaf fragment) {
		InternalCodeFragmentLeaf leaf = newInternalCodeFragmentLeaf();
		leaf.addInternalStatements(fragment.getInternalStatements());
		computeMetricsOfFragment(leaf);
		pushIntoLastNode(leaf);
	}
}
