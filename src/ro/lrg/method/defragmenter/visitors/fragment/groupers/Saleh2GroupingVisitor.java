package ro.lrg.method.defragmenter.visitors.fragment.groupers;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;
import ro.lrg.method.defragmenter.utils.AbstractInternalCodeFragment;
import ro.lrg.method.defragmenter.utils.InternalCodeFragment;
import ro.lrg.method.defragmenter.utils.InternalCodeFragmentLeaf;
import ro.lrg.method.defragmenter.utils.MetricsComputer;

public class Saleh2GroupingVisitor extends SalehGroupingVisitor {
	
	public Saleh2GroupingVisitor(String analyzedClass, IFile iFile, IJavaProject iJavaProject, int FDPTreshold) {
		super(analyzedClass, iFile, iJavaProject, FDPTreshold);
	}
	
	protected List<AbstractInternalCodeFragment> breadthGrouping(List<AbstractInternalCodeFragment> children) {
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
					zeroFDPAccumulator.addInternalStatementsOfFragment(leaf);
				} else {
					InternalCodeFragmentLeaf temp = mergeLeaves(accumulator, leaf);
					int tempFDP = MetricsComputer.getComputedMetrics(temp).getFDP();
					if (zeroFDPAccumulator.getInternalStatementsSize() > 0) {
						if (tempFDP <= FDPTreshold) {
							accumulator.addInternalStatementsOfFragment(zeroFDPAccumulator);
							zeroFDPAccumulator = newInternalCodeFragmentLeaf();
						} else {
							groupedNodes.add(accumulator);
							accumulator = newInternalCodeFragmentLeaf();
							groupedNodes.add(zeroFDPAccumulator);
							zeroFDPAccumulator = newInternalCodeFragmentLeaf();
						}
					} else {
						if (tempFDP > FDPTreshold) {
							groupedNodes.add(accumulator);
							accumulator = newInternalCodeFragmentLeaf();
						}
					}
					accumulator.addInternalStatementsOfFragment(leaf);
				}
			}
		}
		
        if (accumulator.getInternalStatementsSize() > 0){
        	groupedNodes.add(accumulator);
        }
        if (zeroFDPAccumulator.getInternalStatementsSize() > 0) {
        	groupedNodes.add(zeroFDPAccumulator);
        }
        
        return groupedNodes;
	}
}
