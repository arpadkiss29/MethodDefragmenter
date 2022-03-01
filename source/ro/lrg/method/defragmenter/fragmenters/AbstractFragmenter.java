package ro.lrg.method.defragmenter.fragmenters;

import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTVisitor;

import ro.lrg.method.defragmenter.utils.AbstractInternalCodeFragment;

public abstract class AbstractFragmenter extends ASTVisitor{
	public Stack<AbstractInternalCodeFragment> lastNode = new Stack<AbstractInternalCodeFragment>();
	
	public Stack<AbstractInternalCodeFragment> getLastNode() {
		return lastNode;
	}
}
