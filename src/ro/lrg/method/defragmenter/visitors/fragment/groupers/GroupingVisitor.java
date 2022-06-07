package ro.lrg.method.defragmenter.visitors.fragment.groupers;

import java.util.Stack;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;

import ro.lrg.method.defragmenter.utils.AbstractInternalCodeFragment;
import ro.lrg.method.defragmenter.utils.InternalCodeFragment;
import ro.lrg.method.defragmenter.utils.InternalCodeFragmentLeaf;
import ro.lrg.method.defragmenter.visitors.fragment.FragmentVisitor;

public abstract class GroupingVisitor implements FragmentVisitor {
	private final Stack<AbstractInternalCodeFragment> lastNode;
	private final String analyzedClass;
	private final IFile iFile;
	private final IJavaProject iJavaProject;
	
	protected GroupingVisitor(String analyzedClass, IFile iFile, IJavaProject iJavaProject) {
		lastNode = new Stack<>();
		this.analyzedClass = analyzedClass;
		this.iFile = iFile;
		this.iJavaProject = iJavaProject;
	}
	
	public AbstractInternalCodeFragment popLastNode() {
		return lastNode.pop();
	}
	
	protected void pushIntoLastNode(AbstractInternalCodeFragment fragment) {
		lastNode.push(fragment);
	}
	
	protected InternalCodeFragment newInternalCodeFragment() {
		return new InternalCodeFragment(analyzedClass, iFile, iJavaProject);
	}
	
	protected InternalCodeFragmentLeaf newInternalCodeFragmentLeaf() {
		return new InternalCodeFragmentLeaf(analyzedClass, iFile, iJavaProject);
	}
	
	protected InternalCodeFragmentLeaf mergeLeaves(InternalCodeFragmentLeaf leaf1, InternalCodeFragmentLeaf leaf2) {
		InternalCodeFragmentLeaf resultedLeaf = newInternalCodeFragmentLeaf();
		resultedLeaf.addInternalStatementsOfFragment(leaf1);
		resultedLeaf.addInternalStatementsOfFragment(leaf2);
		return resultedLeaf;
	}
}
