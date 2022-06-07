package ro.lrg.method.defragmenter.visitors.fragment.groupers;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;
import ro.lrg.method.defragmenter.utils.AbstractInternalCodeFragment;
import ro.lrg.method.defragmenter.utils.InternalCodeFragmentLeaf;
import ro.lrg.method.defragmenter.utils.MetricsComputer;

public class Saleh1GroupingVisitor extends SalehGroupingVisitor {
	
	public Saleh1GroupingVisitor(String analyzedClass, IFile iFile, IJavaProject iJavaProject, int FDPTreshold) {
		super(analyzedClass, iFile, iJavaProject, FDPTreshold);
	}
	
	protected List<AbstractInternalCodeFragment> breadthGrouping(List<AbstractInternalCodeFragment> children) {
		List<AbstractInternalCodeFragment> groupedNodes = new ArrayList<>();
		
		InternalCodeFragmentLeaf accumulator = newInternalCodeFragmentLeaf();
		
		for(AbstractInternalCodeFragment child : children) {
			child.accept(this);
			AbstractInternalCodeFragment node = popLastNode();
			
			if(node instanceof InternalCodeFragmentLeaf) {
				InternalCodeFragmentLeaf temp = mergeLeaves(accumulator, (InternalCodeFragmentLeaf) node);
				int tempFDP = MetricsComputer.getComputedMetrics(temp).getFDP();
				if(tempFDP > FDPTreshold) {
					groupedNodes.add(accumulator);
					accumulator = newInternalCodeFragmentLeaf();
				}
				
				accumulator.addInternalStatementsOfFragment(node);			
			} else {
				if(accumulator.getInternalStatementsSize() > 0) {
					groupedNodes.add(accumulator);
					accumulator = newInternalCodeFragmentLeaf();
				}
				groupedNodes.add(node);
			}
		}
		
		if(accumulator.getInternalStatementsSize() > 0) {
			groupedNodes.add(accumulator);
		}
		
        return groupedNodes;
	}
}
