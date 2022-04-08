package ro.lrg.method.defragmenter.visitors.fragment.groupers;

import java.util.Stack;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;

import ro.lrg.method.defragmenter.utils.AbstractInternalCodeFragment;
import ro.lrg.method.defragmenter.utils.InternalCodeFragment;
import ro.lrg.method.defragmenter.utils.InternalCodeFragmentLeaf;
import ro.lrg.method.defragmenter.utils.MetricsComputer;
import ro.lrg.method.defragmenter.visitors.fragment.FragmentVisitor;

public abstract class GroupingVisitor implements FragmentVisitor {
	private final Stack<AbstractInternalCodeFragment> lastNode;
	private final String analyzedClass;
	private final IFile iFile;
	private final IJavaProject iJavaProject;
	private final boolean considerStaticFieldAccess;
	private final boolean libraryCheck;
	private final int minBlockSize;
	
	protected GroupingVisitor(String analyzedClass, IFile iFile, IJavaProject iJavaProject,
			boolean considerStaticFieldsAccess, boolean libraryCheck, int minBlockSize) {
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
	
	protected void pushIntoLastNode(AbstractInternalCodeFragment fragment) {
		lastNode.push(fragment);
	}
	
	protected InternalCodeFragment newInternalCodeFragment() {
		return new InternalCodeFragment(analyzedClass, iFile, iJavaProject);
	}
	
	protected InternalCodeFragmentLeaf newInternalCodeFragmentLeaf() {
		return new InternalCodeFragmentLeaf(analyzedClass, iFile, iJavaProject);
	}
	
	protected MetricsComputer computeMetricsOfFragment(AbstractInternalCodeFragment fragment) {
		MetricsComputer metricsComputer = new MetricsComputer();
		metricsComputer.computeDataAccesses(fragment, analyzedClass, considerStaticFieldAccess, libraryCheck, minBlockSize);
		return metricsComputer;
	}
}
