package ro.lrg.method.defragmenter.visitors.fragment.collectors;

import java.util.ArrayList;
import java.util.List;

import ro.lrg.method.defragmenter.utils.AbstractInternalCodeFragment;
import ro.lrg.method.defragmenter.utils.EnvyComputer;
import ro.lrg.method.defragmenter.utils.InternalCodeFragment;
import ro.lrg.method.defragmenter.utils.InternalCodeFragmentLeaf;
import ro.lrg.method.defragmenter.utils.MetricsComputer;
import ro.lrg.method.defragmenter.visitors.fragment.FragmentVisitor;

public class AllEnviousLeavesVisitor implements FragmentVisitor {
	private final List<AbstractInternalCodeFragment> allEnviousFragments = new ArrayList<>();
	private final int ATFDTreshold;
	private final int FDPTreshold;
	private final double LAATreshold;
	
	public AllEnviousLeavesVisitor(int ATFDTreshold, int FDPTreshold, double LAATreshold) {
		this.ATFDTreshold = ATFDTreshold;
		this.FDPTreshold = FDPTreshold;
		this.LAATreshold = LAATreshold;
	}
	
	private boolean fragmentIsEnvy(AbstractInternalCodeFragment fragment) {
		MetricsComputer metricsComputer = MetricsComputer.getComputedMetrics(fragment);
		return EnvyComputer.computeEnvy(metricsComputer.getATFD(), metricsComputer.getFDPMap(), metricsComputer.getLAA(), 
				ATFDTreshold, FDPTreshold, LAATreshold);
	}
	
	@Override
	public void visit(InternalCodeFragment fragment) {
		List<AbstractInternalCodeFragment> children = fragment.getChildren();
		for (AbstractInternalCodeFragment child : children) {
			child.accept(this);
		}
	}

	@Override
	public void visit(InternalCodeFragmentLeaf fragment) {
		if(fragmentIsEnvy(fragment)) allEnviousFragments.add(fragment);
	}
	
	public List<AbstractInternalCodeFragment> getAllEnviousFragments() {
		return allEnviousFragments;
	}
}
