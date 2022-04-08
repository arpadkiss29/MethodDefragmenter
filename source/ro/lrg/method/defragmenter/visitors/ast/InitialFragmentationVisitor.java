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
		InternalCodeFragment parent = newInternalCodeFragment();
		//parent.addInternalStatement(node);
		
//		parent.setStartPosition(node.getStartPosition());
//		parent.setEndPosition(node.getStartPosition() + node.getLength());
		
		List<AbstractInternalCodeFragment> fragments = getRawAbstractInternalCodeFragments(node);
		fragments.forEach(f->parent.addChild(f));
        
		lastNode.push(parent);
		return false;
	}
	
	//this method helps some visit methods
	private void visitAux(InternalCodeFragment parent, Statement statement, boolean resultAfterVisitCanBeNull) {
		statement.accept(this);
		InternalCodeFragment fragment = (InternalCodeFragment) lastNode.pop();
		if (fragment != null) {
			parent.addChild(fragment);
		} else if (resultAfterVisitCanBeNull) {
			addStatementAsGrandchildToParent(parent, statement);
		}
	}
	
	//--------------------------------------------------------------------------visit methods
	@Override
	public boolean visit(ArrayAccess node) {
		InternalCodeFragment parent = newInternalCodeFragment();
		
		InternalCodeFragmentLeaf fragment = newInternalCodeFragmentLeaf();
		fragment.addInternalStatement(node.getArray());
		fragment.addInternalStatement(node.getIndex());
		parent.addChild(fragment);
		
		lastNode.push(parent);
		return false;
	}
	
	@Override
	public boolean visit(ArrayInitializer node) {
		InternalCodeFragment parent = newInternalCodeFragment();
		
		InternalCodeFragmentLeaf fragment = newInternalCodeFragmentLeaf();
		fragment.addInternalStatements(node.expressions());
		parent.addChild(fragment);
		
		lastNode.push(parent);
		return false;
	}
	
	//this method does not override a method from ASTVisitor
	public boolean visit(CatchClause node) {
		InternalCodeFragment parent;

		Statement body = node.getBody();
		body.accept(this);
		parent = (InternalCodeFragment) lastNode.pop();
		if(parent == null) {
			parent = newInternalCodeFragment();
			InternalCodeFragmentLeaf simpleCatchStatement = newInternalCodeFragmentLeaf();
			simpleCatchStatement.addInternalStatement(body);
			parent.addChild(simpleCatchStatement);
		}
		parent.addInternalStatement(node.getException());
		
		lastNode.push(parent);
		return false;
	}
	
	@Override
	public boolean visit(DoStatement doStatement) {
		InternalCodeFragment parent = newInternalCodeFragment();

		parent.addInternalStatement(doStatement.getExpression());
		visitAux(parent, doStatement.getBody(), true);
		
		lastNode.push(parent);
		return false;
	}
	
	@Override
	public boolean visit(EnhancedForStatement enhancedForStatement) {
		InternalCodeFragment parent = newInternalCodeFragment();

		parent.addInternalStatement(enhancedForStatement.getExpression());
		parent.addInternalStatement(enhancedForStatement.getParameter());
		visitAux(parent, enhancedForStatement.getBody(), true);
		
		lastNode.push(parent);
		return false;
	}
	
	@Override
	public boolean visit(ForStatement forStatement) {
		InternalCodeFragment parent = newInternalCodeFragment();

		parent.addInternalStatement(forStatement.getExpression());
		parent.addInternalStatements(forStatement.initializers());
		parent.addInternalStatements(forStatement.updaters());
		visitAux(parent, forStatement.getBody(), true);
		
		lastNode.push(parent);
		return false;
	}
	
	@Override
	public boolean visit(IfStatement ifStatement) {
		InternalCodeFragment parent = newInternalCodeFragment();
		parent.setStartPosition(ifStatement.getStartPosition());
		parent.setEndPosition(ifStatement.getStartPosition() + ifStatement.getLength());
		
		parent.addInternalStatement(ifStatement.getExpression());
		if(ifStatement.getThenStatement() != null) visitAux(parent, ifStatement.getThenStatement(), true);
		if(ifStatement.getElseStatement() != null) visitAux(parent, ifStatement.getElseStatement(), true);
		
		lastNode.push(parent);
		return false;
	}
	
	@Override
	public boolean visit(MethodInvocation node) {
		InternalCodeFragment parent = newInternalCodeFragment();
		
		InternalCodeFragmentLeaf fragment = newInternalCodeFragmentLeaf();
		fragment.addInternalStatements(node.arguments());
		parent.addChild(fragment);
		
		lastNode.push(parent);
		return false;
	}
	
	@Override
	public boolean visit(SwitchStatement switchStatement) {
		InternalCodeFragment parent = newInternalCodeFragment();

		parent.addInternalStatement(switchStatement.getExpression());

		List<Statement> switchStatements = switchStatement.statements();
		InternalCodeFragment node = newInternalCodeFragment();
		
		for (Statement statement : switchStatements) {
			statement.accept(this);
			InternalCodeFragment fragment = (InternalCodeFragment) lastNode.pop();
			if (fragment != null) {
				node.addChild(fragment);
			} else {
				InternalCodeFragmentLeaf leaf = newInternalCodeFragmentLeaf();
				leaf.addInternalStatement(statement);
				node.addChild(leaf);
			}
		}

		if (node != null && node.getChildrenSize() != 0) {
			parent.addChild(node);
		}

		lastNode.push(parent);
		return false;
	}
	
	@Override
	public boolean visit(SynchronizedStatement synchronizedStatement) {
		InternalCodeFragment parent = newInternalCodeFragment();

		parent.addInternalStatement(synchronizedStatement.getExpression());
		visitAux(parent, synchronizedStatement.getBody(), false);
	
		lastNode.push(parent);
		return false;
	}
	
	@Override
	public boolean visit(TryStatement tryStatement) {
		InternalCodeFragment parent = newInternalCodeFragment();
		
		List<CatchClause> catchClauses = tryStatement.catchClauses();
		for (CatchClause catchClause : catchClauses) {
			catchClause.accept(this);
			InternalCodeFragment fragment = (InternalCodeFragment) lastNode.pop();
			if (fragment != null) {
				parent.addChild(fragment);
			}
		}
		visitAux(parent, tryStatement.getBody(), true);
		visitAux(parent, tryStatement.getFinally(), true);
		
		lastNode.push(parent);
		return false;
	}
	
	@Override
	public boolean visit(WhileStatement whileStatement) {
		InternalCodeFragment parent = newInternalCodeFragment();

		parent.addInternalStatement(whileStatement.getExpression());
		visitAux(parent, whileStatement.getBody(), true);
		
		lastNode.push(parent);
		return false;
	}

	//--------------------------------------------------------------------------simple visit methods
	private void pushAsLeaf(Statement statement) {
		
	}
	
	private void pushAsLeaf(Expression expression) {
		
	}
	
	@Override
	public boolean visit(ArrayCreation node) {
		lastNode.push(null);
		return false;
	}
	@Override
	public boolean visit(ArrayType node) {
		lastNode.push(null);
		return false;
	}
	@Override
	public boolean visit(Assignment node) {
		lastNode.push(null);
		return false;
	}
	@Override
	public boolean visit(BreakStatement node) {
		lastNode.push(null);
		return false;
	}
	@Override
	public boolean visit(CompilationUnit node) {
		throw new DontGetHereException("A CompilationUnit can't exist inside a block inside a Block!");
	}
	@Override
	public boolean visit(ContinueStatement node) {
		lastNode.push(null);
		return false;
	}
	@Override
	public boolean visit(EmptyStatement node) {
		lastNode.push(null);
		return false;
	}
	public boolean visit(Expression node) {
		lastNode.push(null);
		return false;
	}
	@Override
	public boolean visit(ExpressionStatement node) {
		lastNode.push(null);
		return false;
	}
	@Override
	public boolean visit(ReturnStatement node) {
		lastNode.push(null);
		return false;
	}
	@Override
	public boolean visit(SuperConstructorInvocation node) {
		lastNode.push(null);
		return false;
	}
	@Override
	public boolean visit(SwitchCase switchCase) {
		lastNode.push(null);
		return false;
	}
	@Override
	public boolean visit(ThisExpression node) {
		lastNode.push(null);
		return false;
	}
	@Override
	public boolean visit(ThrowStatement node) {
		lastNode.push(null);
		return false;
	}
	@Override
	public boolean visit(VariableDeclarationStatement node) {
		lastNode.push(null);
		return false;
	}
}
