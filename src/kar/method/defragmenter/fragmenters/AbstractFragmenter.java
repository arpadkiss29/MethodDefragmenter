package kar.method.defragmenter.fragmenters;

import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTVisitor;

import kar.method.defragmenter.utils.CodeFragmentTreeNode;

public abstract class AbstractFragmenter extends ASTVisitor{
	public Stack<CodeFragmentTreeNode> lastNode = new Stack<CodeFragmentTreeNode>(); 
	
}
