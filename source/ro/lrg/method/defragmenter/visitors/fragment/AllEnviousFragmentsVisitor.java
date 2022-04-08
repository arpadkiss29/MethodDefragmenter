package ro.lrg.method.defragmenter.visitors.fragment;

import java.util.ArrayList;
import java.util.List;

import ro.lrg.method.defragmenter.utils.AbstractInternalCodeFragment;
import ro.lrg.method.defragmenter.utils.EnvyComputer;
import ro.lrg.method.defragmenter.utils.InternalCodeFragment;
import ro.lrg.method.defragmenter.utils.InternalCodeFragmentLeaf;
import ro.lrg.method.defragmenter.utils.MetricsComputer;

public class AllEnviousFragmentsVisitor implements FragmentVisitor {
	private List<AbstractInternalCodeFragment> allEnviousFragments = new ArrayList<>();
	private final boolean considerStaticFieldAccess;
	private final boolean libraryCheck;
	private final int minBlockSize;
	private final int ATFDTreshold;
	private final int FDPTreshold;
	private final double LAATreshold;
	
	public AllEnviousFragmentsVisitor(int ATFDTreshold, int FDPTreshold, double LAATreshold, 
			boolean considerStaticFieldsAccess, boolean libraryCheck, int minBlockSize) {
		this.ATFDTreshold = ATFDTreshold;
		this.FDPTreshold = FDPTreshold;
		this.LAATreshold = LAATreshold;
		this.considerStaticFieldAccess = considerStaticFieldsAccess;
		this.libraryCheck = libraryCheck;
		this.minBlockSize = minBlockSize;
	}
	
	private boolean fragmentIsEnvy(AbstractInternalCodeFragment fragment) {
		MetricsComputer metricsComputer = new MetricsComputer();
		metricsComputer.computeDataAccesses(fragment, fragment.getAnalizedClass(), considerStaticFieldAccess, libraryCheck, minBlockSize);
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
