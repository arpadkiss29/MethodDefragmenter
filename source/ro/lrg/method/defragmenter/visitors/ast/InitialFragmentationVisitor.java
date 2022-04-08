package ro.lrg.method.defragmenter.visitors.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import ro.lrg.method.defragmenter.utils.AbstractInternalCodeFragment;
import ro.lrg.method.defragmenter.utils.InternalCodeFragmentLeaf;
import ro.lrg.method.defragmenter.utils.DontGetHereException;
import ro.lrg.method.defragmenter.utils.InternalCodeFragment;

@SuppressWarnings("unchecked")
public class InitialFragmentationVisitor extends ASTVisitor {
	private final Stack<AbstractInternalCodeFragment> lastNode;
	private final String analyzedClass;
	private final IFile iFile;
	private final IJavaProject iJavaProject;
	
	public InitialFragmentationVisitor(String analyzedClass, IFile iFile, IJavaProject iJavaProject) {
		lastNode = new Stack<>();
		this.analyzedClass = analyzedClass;
		this.iFile = iFile;
		this.iJavaProject = iJavaProject;
	}
	
	public AbstractInternalCodeFragment popLastNode() {
		return lastNode.pop();
	}
	
	private InternalCodeFragment newInternalCodeFragment() {
		return new InternalCodeFragment(analyzedClass, iFile, iJavaProject);
	}
	
	private InternalCodeFragmentLeaf newInternalCodeFragmentLeaf() {
		return new InternalCodeFragmentLeaf(analyzedClass, iFile, iJavaProject);
	}
	
	private void addStatementAsGrandchildToParent(InternalCodeFragment parent, Statement statement) {
		InternalCodeFragment node = newInternalCodeFragment();
		InternalCodeFragmentLeaf leaf = newInternalCodeFragmentLeaf();
		leaf.addInternalStatement(statement);
		node.addChild(leaf);
		parent.addChild(node);
	}
	
	//--------------------------------------------------------------------------main algorithm
	private List<AbstractInternalCodeFragment> getRawAbstractInternalCodeFragments(Block node) {
		List<AbstractInternalCodeFragment> fragments = new ArrayList<>();
		List<Statement> statements = node.statements();
		for (Statement statement : statements) {
			((ASTNode) statement).accept(this);
			AbstractInternalCodeFragment fragment = null;
			if(!lastNode.isEmpty()) fragment = lastNode.pop();
			if (fragment == null) {
				fragment = newInternalCodeFragmentLeaf();
				fragment.addInternalStatement(statement);
			}
			fragments.add(fragment);
		}
		return fragments;
	}
	
	@Override
	public boolean visit(Block node) {
		AbstractInternalCodeFragment parent = newInternalCodeFragment();
		parent.addInternalStatement(node);
		
//		parent.setStartPosition(node.getStartPosition());
//		parent.setEndPosition(node.getStartPosition() + node.getLength());
		
//		List<AbstractInternalCodeFragment> fragments = new ArrayList<>();
		
		List<Statement> statements = node.statements();
		for (Statement statement : statements) {
			visitAux((InternalCodeFragment) parent, statement);
		}
		
		parent = checkIfParentShouldBeLeaf(parent);
		
//		List<AbstractInternalCodeFragment> fragments = getRawAbstractInternalCodeFragments(node);
//		fragments.forEach(f->parent.addChild(f));
        
		lastNode.push(parent);
		return false;
	}
	
	//this method helps some visit methods
	private void visitAux(InternalCodeFragment parent, ASTNode statement) {
		if (statement != null) {
			statement.accept(this);
			AbstractInternalCodeFragment fragment = lastNode.pop();
			parent.addChild(fragment);
		}
	}
	
	private AbstractInternalCodeFragment checkIfParentShouldBeLeaf(AbstractInternalCodeFragment parent) {
		if (((InternalCodeFragment) parent).getChildrenSize() == 0) {
			InternalCodeFragmentLeaf leaf = newInternalCodeFragmentLeaf();
			leaf.addInternalStatement(parent.getInternalStatement(0));
			return leaf;
		} else {
			return parent;
		}
	}
	
	//--------------------------------------------------------------------------visit methods
	@Override
	public boolean visit(ArrayAccess node) {
		AbstractInternalCodeFragment parent = newInternalCodeFragment();
		parent.addInternalStatement(node);
		
		visitAux((InternalCodeFragment) parent, node.getArray());
		visitAux((InternalCodeFragment) parent, node.getIndex());
		
		parent = checkIfParentShouldBeLeaf(parent);
		
		lastNode.push(parent);
		return false;
	}
	
	@Override
	public boolean visit(ArrayInitializer node) {
		AbstractInternalCodeFragment parent = newInternalCodeFragment();
		parent.addInternalStatement(node);
		
		List<Statement> statements = node.expressions();
		for (Statement statement : statements) {
			visitAux((InternalCodeFragment) parent, statement);
		}
		
		parent = checkIfParentShouldBeLeaf(parent);
		
		lastNode.push(parent);
		return false;
	}
	
	//this method does not override a method from ASTVisitor
	public boolean visit(CatchClause node) {
		AbstractInternalCodeFragment parent = newInternalCodeFragment();
		parent.addInternalStatement(node);

		visitAux((InternalCodeFragment) parent, node.getBody());
		
		parent = checkIfParentShouldBeLeaf(parent);
		
		lastNode.push(parent);
		return false;
	}
	
	@Override
	public boolean visit(DoStatement node) {
		AbstractInternalCodeFragment parent = newInternalCodeFragment();
		parent.addInternalStatement(node);
		
		visitAux((InternalCodeFragment) parent, node.getBody());
		
		parent = checkIfParentShouldBeLeaf(parent);
		
		lastNode.push(parent);
		return false;
	}
	
	@Override
	public boolean visit(EnhancedForStatement node) {
		AbstractInternalCodeFragment parent = newInternalCodeFragment();
		parent.addInternalStatement(node);
		
		visitAux((InternalCodeFragment) parent, node.getBody());
		
		parent = checkIfParentShouldBeLeaf(parent);
		
		lastNode.push(parent);
		return false;
	}
	
	@Override
	public boolean visit(ForStatement node) {
		AbstractInternalCodeFragment parent = newInternalCodeFragment();
		parent.addInternalStatement(node);
		
		visitAux((InternalCodeFragment) parent, node.getBody());
		
		parent = checkIfParentShouldBeLeaf(parent);
		
		lastNode.push(parent);
		return false;
	}
	
	@Override
	public boolean visit(IfStatement node) {
		AbstractInternalCodeFragment parent = newInternalCodeFragment();
//		parent.setStartPosition(ifStatement.getStartPosition());
//		parent.setEndPosition(ifStatement.getStartPosition() + ifStatement.getLength());
		parent.addInternalStatement(node);
		
		visitAux((InternalCodeFragment) parent, node.getThenStatement());
		visitAux((InternalCodeFragment) parent, node.getElseStatement());
		
		parent = checkIfParentShouldBeLeaf(parent);
		
		lastNode.push(parent);
		return false;
	}
	
	@Override
	public boolean visit(MethodInvocation node) {
		AbstractInternalCodeFragment parent = newInternalCodeFragment();
		parent.addInternalStatement(node);
		
		List<Statement> statements = node.arguments();
		for (Statement statement : statements) {
			visitAux((InternalCodeFragment) parent, statement);
		}
		
		parent = checkIfParentShouldBeLeaf(parent);
		
		lastNode.push(parent);
		return false;
	}
	
	@Override
	public boolean visit(SwitchStatement node) {
		AbstractInternalCodeFragment parent = newInternalCodeFragment();
		parent.addInternalStatement(node);

		List<Statement> statements = node.statements();
		for (Statement statement : statements) {
			visitAux((InternalCodeFragment) parent, statement);
		}
		
		parent = checkIfParentShouldBeLeaf(parent);

		lastNode.push(parent);
		return false;
	}
	
	@Override
	public boolean visit(SynchronizedStatement node) {
		AbstractInternalCodeFragment parent = newInternalCodeFragment();
		parent.addInternalStatement(node);
		
		visitAux((InternalCodeFragment) parent, node.getBody());
	
		parent = checkIfParentShouldBeLeaf(parent);
		
		lastNode.push(parent);
		return false;
	}
	
	@Override
	public boolean visit(TryStatement node) {
		AbstractInternalCodeFragment parent = newInternalCodeFragment();
		parent.addInternalStatement(node);
		
		visitAux((InternalCodeFragment) parent, node.getBody());
		List<ASTNode> statements = node.catchClauses();
		for (ASTNode statement : statements) {
			visitAux((InternalCodeFragment) parent, statement);
		}
		visitAux((InternalCodeFragment) parent, node.getFinally());
		
		parent = checkIfParentShouldBeLeaf(parent);
		
		lastNode.push(parent);
		return false;
	}
	
	@Override
	public boolean visit(WhileStatement node) {
		AbstractInternalCodeFragment parent = newInternalCodeFragment();
		parent.addInternalStatement(node);
		
		visitAux((InternalCodeFragment) parent, node.getBody());
		
		parent = checkIfParentShouldBeLeaf(parent);
		
		lastNode.push(parent);
		return false;
	}

	//--------------------------------------------------------------------------simple visit methods
	private void pushAsLeaf(ASTNode node) {
		InternalCodeFragmentLeaf leaf = newInternalCodeFragmentLeaf();
		leaf.addInternalStatement(node);
		lastNode.push(leaf);
	}
	
	@Override
	public boolean visit(ArrayCreation node) {
		pushAsLeaf(node);
		return false;
	}
	@Override
	public boolean visit(ArrayType node) {
		pushAsLeaf(node);
		return false;
	}
	@Override
	public boolean visit(Assignment node) {
		pushAsLeaf(node);
		return false;
	}
	@Override
	public boolean visit(BreakStatement node) {
		pushAsLeaf(node);
		return false;
	}
	@Override
	public boolean visit(CompilationUnit node) {
		throw new DontGetHereException("A CompilationUnit can't exist inside a block inside a Block!");
	}
	@Override
	public boolean visit(ContinueStatement node) {
		pushAsLeaf(node);
		return false;
	}
	@Override
	public boolean visit(EmptyStatement node) {
		pushAsLeaf(node);
		return false;
	}
	public boolean visit(Expression node) {
		pushAsLeaf(node);
		return false;
	}
	@Override
	public boolean visit(ExpressionStatement node) {
		pushAsLeaf(node);
		return false;
	}
	@Override
	public boolean visit(ReturnStatement node) {
		pushAsLeaf(node);
		return false;
	}
	@Override
	public boolean visit(SuperConstructorInvocation node) {
		pushAsLeaf(node);
		return false;
	}
	@Override
	public boolean visit(SwitchCase node) {
		pushAsLeaf(node);
		return false;
	}
	@Override
	public boolean visit(ThisExpression node) {
		pushAsLeaf(node);
		return false;
	}
	@Override
	public boolean visit(ThrowStatement node) {
		pushAsLeaf(node);
		return false;
	}
	@Override
	public boolean visit(VariableDeclarationStatement node) {
		pushAsLeaf(node);;
		return false;
	}
}
