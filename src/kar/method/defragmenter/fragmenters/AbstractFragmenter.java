package kar.method.defragmenter.fragmenters;

import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTVisitor;

import kar.method.defragmenter.utils.AbstractCodeFragment;

public abstract class AbstractFragmenter extends ASTVisitor{
	public Stack<AbstractCodeFragment> lastNode = new Stack<AbstractCodeFragment>(); 
	
}
