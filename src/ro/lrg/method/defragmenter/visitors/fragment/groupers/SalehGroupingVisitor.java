package ro.lrg.method.defragmenter.visitors.fragment.groupers;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;

import ro.lrg.method.defragmenter.utils.AbstractInternalCodeFragment;
import ro.lrg.method.defragmenter.utils.InternalCodeFragment;
import ro.lrg.method.defragmenter.utils.InternalCodeFragmentLeaf;
import ro.lrg.method.defragmenter.utils.MetricsComputer;

public abstract class SalehGroupingVisitor extends GroupingVisitor{
	protected final int FDPTreshold;
	
	protected SalehGroupingVisitor(String analyzedClass, IFile iFile, IJavaProject iJavaProject, int FDPTreshold) {
		super(analyzedClass, iFile, iJavaProject);
		this.FDPTreshold = FDPTreshold;
	}
	
	protected boolean parentCanFormALeaf(InternalCodeFragment parent, List<AbstractInternalCodeFragment> groupedNodes) {
		if (groupedNodes.size() != 1 || !(groupedNodes.get(0) instanceof InternalCodeFragmentLeaf)) return false;
		int parentFDP = MetricsComputer.getComputedMetrics(parent).getFDP();
		if (parentFDP > FDPTreshold) return false;
		return true;
	}
	
	protected abstract List<AbstractInternalCodeFragment> breadthGrouping(List<AbstractInternalCodeFragment> children);
	
	@Override
	public void visit(InternalCodeFragment fragment) {
		List<AbstractInternalCodeFragment> groupedNodes = breadthGrouping(fragment.getChildren());
		
		AbstractInternalCodeFragment parent;
		if (parentCanFormALeaf((InternalCodeFragment) fragment, groupedNodes)) {
			parent = newInternalCodeFragmentLeaf();		
		} else {
			parent = newInternalCodeFragment();
			for (AbstractInternalCodeFragment node : groupedNodes) {
				((InternalCodeFragment) parent).addChild(node);
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
