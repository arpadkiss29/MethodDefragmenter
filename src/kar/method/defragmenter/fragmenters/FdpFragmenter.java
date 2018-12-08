package kar.method.defragmenter.fragmenters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
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
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
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

import kar.method.defragmenter.utils.CodeFragmentLeaf;
import kar.method.defragmenter.utils.AbstractCodeFragment;
import kar.method.defragmenter.utils.DontGetHereException;
import kar.method.defragmenter.utils.FixedStructureTypes;
import kar.method.defragmenter.utils.InternalCodeFragment;

@SuppressWarnings("unchecked")
public class FdpFragmenter extends AbstractFragmenter {

	private CompilationUnit unit;
	private boolean considerStaticFields;
	private String analyzedClass;
	private int FDP_TRESHOLD;

	public FdpFragmenter(CompilationUnit unit, boolean considerStaticFields, String analyzedClass, int FDP_TREHSOLD) {
		this.analyzedClass = analyzedClass;
		this.unit = unit;
		this.considerStaticFields = considerStaticFields;
		this.FDP_TRESHOLD = FDP_TREHSOLD;
	}

	private boolean canBeMerged(AbstractCodeFragment parent, AbstractCodeFragment toBeMerged) {
		if (toBeMerged.getChildrenSize() != 1 || !(toBeMerged.getChild(0) instanceof CodeFragmentLeaf)
				|| !canBeAddedToBlock(parent, toBeMerged.getAllSubTreeASTNodes())) {
			return false;
		}
		return true;
	}

	private boolean canBeAddedToBlock(AbstractCodeFragment currentFragment, Statement currentStatements) {
		List<ASTNode> statement = new ArrayList<ASTNode>();
		statement.add(currentStatements);
		return canBeAddedToBlock(currentFragment, statement);
	}

	// could I use Statement in ArrayList? generics?
	private boolean canBeAddedToBlock(AbstractCodeFragment currentFragment, List<ASTNode> currentStatements) {
		if (currentFragment.getStoredFDP() == null) {
			currentFragment.getFdp(analyzedClass, considerStaticFields, null, true);
		}
		HashMap<String, Integer> fdpBefore = currentFragment.getStoredFDP();
		CodeFragmentLeaf tempFragment = new CodeFragmentLeaf();
		tempFragment.addStatements(currentStatements);
		HashMap<String, Integer> fdpAfter = tempFragment.getFdp(analyzedClass, considerStaticFields, null, true);
		if (fdpBefore.size() == 0) {
			currentFragment.setStoredFDP(fdpAfter);
			return true;
		} else {
			if (fdpAfter.size()!=0&&fdpBefore.size() != fdpAfter.size()) {
				return false;
			}
		}
		for (String key : fdpAfter.keySet()) {
			if (!fdpBefore.containsKey(key)) {
				return false;
			}
		}
		fdpBefore.putAll(fdpAfter);
		currentFragment.setStoredFDP(fdpBefore);
		return true;
	}

	@Override
	public boolean visit(Block node) {

		InternalCodeFragment parent = new InternalCodeFragment();

		parent.setStartNode(node.getStartPosition());
		parent.setEndNode(node.getStartPosition() + node.getLength());

		CodeFragmentLeaf currentFragment = null;
		List<Statement> currentStatements = node.statements();

		for (int i = 0; i < currentStatements.size(); i++) {
			Statement statement = currentStatements.get(i);

			// Visit each statement in current block
			((ASTNode) statement).accept(this);

			// Get subtree from below
			AbstractCodeFragment res = null;
			if (!lastNode.isEmpty()) {
				res = lastNode.pop();
			}

			// If there is no block below, this means there are only statements
			if (res == null) {
				// If current statement belongs to the part of code before the block;
				if (currentFragment == null) {
					currentFragment = new CodeFragmentLeaf();
				}
				boolean canBeAdded = canBeAddedToBlock(currentFragment, statement);
				if (!canBeAdded) {
					parent.addChild(currentFragment);
					currentFragment = new CodeFragmentLeaf();
				}

				// Adding it to the current leaf
				currentFragment.addStatement(statement);
			} else {
				// There is a block below, if there are statements before and they are grouped
				// in a leaf, the leaf must be added.
				if (currentFragment != null) {
					parent.addChild(currentFragment);
					currentFragment = null;
				}
				// Adding the node which was below and contains a block in it.
				parent.addChild(res);
			}
		}

		// Fragment constructed based on statements but never added to parent, this is
		// why its needed to be added here.
		if (currentFragment != null) {
			parent.addChild(currentFragment);
			currentFragment = null;
		}

		lastNode.push(parent);
		return false;
	}

	public boolean visit(Assignment assignment) {
		lastNode.push(null);
		return false;
	}

	public boolean visit(Expression expression) {
		lastNode.push(null);
		return false;
	}

	public boolean visit(IfStatement ifStatement) {
		AbstractCodeFragment parent = new InternalCodeFragment();

		parent.setStartNode(ifStatement.getStartPosition());
		parent.setEndNode(ifStatement.getStartPosition() + ifStatement.getLength());

		Expression expr = ifStatement.getExpression();
		if (expr != null) {
			parent.addInternalStatement(expr);
		}
		boolean canBeMerged = true;
		Statement thenStatement = ifStatement.getThenStatement();
		if (thenStatement != null) {
			thenStatement.accept(this);
			AbstractCodeFragment resThen = lastNode.pop();
			if (resThen != null) {
				if (!canBeMerged(parent, resThen)) {
					canBeMerged = false;
				}
				parent.addChild(resThen);
			} else {
				if (!canBeAddedToBlock(parent, thenStatement)) {
					canBeMerged = false;
					InternalCodeFragment thenNode = new InternalCodeFragment();
					CodeFragmentLeaf thenStatementLeaf = new CodeFragmentLeaf();
					thenStatementLeaf.addStatement(thenStatement);
					thenNode.addChild(thenStatementLeaf);
					parent.addChild(thenNode);
				}

			}
		}

		if (ifStatement.getElseStatement() != null) {
			Statement elseStatement = ifStatement.getElseStatement();
			elseStatement.accept(this);
			AbstractCodeFragment resElse = lastNode.pop();
			if (resElse != null) {
				if (!canBeMerged(parent, resElse)) {
					canBeMerged = false;
				}
				parent.addChild(resElse);
			} else {
				if (!canBeMerged || !canBeAddedToBlock(parent, elseStatement)) {
					canBeMerged = false;
					InternalCodeFragment elseNode = new InternalCodeFragment();
					CodeFragmentLeaf elseStatementLeaf = new CodeFragmentLeaf();
					elseStatementLeaf.addStatement(elseStatement);
					elseNode.addChild(elseStatementLeaf);
					parent.addChild(elseNode);
				}
			}
		}

		if (canBeMerged) {
			lastNode.push(null);
			return false;
		}

		lastNode.push(parent);
		return false;
	}

	@Override
	public boolean visit(DoStatement doStatement) {
		InternalCodeFragment parent = new InternalCodeFragment();

		Expression expr = doStatement.getExpression();
		if (expr != null) {
			parent.addInternalStatement(expr);
		}

		boolean canBeMerged = true;
		doStatement.getBody().accept(this);
		AbstractCodeFragment doWhileBody = lastNode.pop();
		if (doWhileBody != null) {
			if (!canBeMerged(parent, doWhileBody))
				canBeMerged = false;
			parent.addChild(doWhileBody);
		} else {
			if (!canBeAddedToBlock(parent, doStatement.getBody())) {
				canBeMerged = false;
				InternalCodeFragment doNode = new InternalCodeFragment();
				CodeFragmentLeaf simpleDoWhileStatement = new CodeFragmentLeaf();
				simpleDoWhileStatement.addStatement(doStatement.getBody());
				doNode.addChild(simpleDoWhileStatement);
				parent.addChild(doNode);
			}
		}

		if (canBeMerged) {
			lastNode.push(null);
			return false;
		}

		lastNode.push(parent);
		return false;
	}

	@Override
	public boolean visit(ExpressionStatement node) {
		lastNode.push(null);
		return false;
	}

	@Override
	public boolean visit(ForStatement forStatement) {
		InternalCodeFragment parent = new InternalCodeFragment();

		Expression expr = forStatement.getExpression();
		if (expr != null) {
			parent.addInternalStatement(expr);
		}

		List<Expression> inits = forStatement.initializers();
		for (Expression each : inits) {
			parent.addInternalStatement(each);
		}

		List<Expression> updates = forStatement.updaters();
		for (Expression each : updates) {
			parent.addInternalStatement(each);
		}

		boolean canBeMerged = true;
		Statement forBodyStatement = forStatement.getBody();
		forBodyStatement.accept(this);
		AbstractCodeFragment forBody = lastNode.pop();
		if (forBody != null) {
			if (!canBeMerged(parent, forBody))
				canBeMerged = false;
			parent.addChild(forBody);
		} else {
			if (!canBeMerged || !canBeAddedToBlock(parent, forBodyStatement)) {
				canBeMerged = false;
				InternalCodeFragment forNode = new InternalCodeFragment();
				CodeFragmentLeaf simpleForStatement = new CodeFragmentLeaf();
				simpleForStatement.addStatement(forBodyStatement);
				forNode.addChild(simpleForStatement);
				parent.addChild(forNode);
			}
		}
		if (canBeMerged) {
			lastNode.push(null);
			return false;
		}
		lastNode.push(parent);
		return false;
	}

	@Override
	public boolean visit(EnhancedForStatement forStatement) {
		InternalCodeFragment parent = new InternalCodeFragment();

		Expression expr = forStatement.getExpression();
		if (expr != null) {
			parent.addInternalStatement(expr);
		}

		SingleVariableDeclaration param = forStatement.getParameter();
		parent.addInternalStatement(param);

		boolean canBeMerged = true;
		Statement forBodyStatement = forStatement.getBody();
		forBodyStatement.accept(this);
		AbstractCodeFragment forBody = lastNode.pop();
		if (forBody != null) {
			if (!canBeMerged(parent, forBody))
				canBeMerged = false;
			parent.addChild(forBody);
		} else {
			if (!canBeMerged || !canBeAddedToBlock(parent, forBodyStatement)) {
				canBeMerged = false;
				InternalCodeFragment forNode = new InternalCodeFragment();
				CodeFragmentLeaf simpleForStatement = new CodeFragmentLeaf();
				simpleForStatement.addStatement(forBodyStatement);
				forNode.addChild(simpleForStatement);
				parent.addChild(forNode);
			}
		}
		if (canBeMerged) {
			lastNode.push(null);
			return false;
		}
		lastNode.push(parent);
		return false;
	}

	@Override
	public boolean visit(WhileStatement whileStatement) {
		InternalCodeFragment parent = new InternalCodeFragment();

		Expression expr = whileStatement.getExpression();
		if (expr != null) {
			parent.addInternalStatement(expr);
		}

		boolean canBeMerged = true;
		Statement body = whileStatement.getBody();
		body.accept(this);
		AbstractCodeFragment bodyWhile = lastNode.pop();
		if (bodyWhile != null) {
			if (!canBeMerged(parent, bodyWhile))
				canBeMerged = false;
			parent.addChild(bodyWhile);
		} else {
			if (!canBeMerged || !canBeAddedToBlock(parent, body)) {
				canBeMerged = false;
				InternalCodeFragment whileNode = new InternalCodeFragment();
				CodeFragmentLeaf simpleWhileStatement = new CodeFragmentLeaf();
				simpleWhileStatement.addStatement(body);
				whileNode.addChild(simpleWhileStatement);
				parent.addChild(whileNode);
			}
		}

		if (canBeMerged) {
			lastNode.push(null);
			return false;
		}

		lastNode.push(parent);
		return false;
	}

	@Override
	public boolean visit(ArrayAccess node) {
		InternalCodeFragment parent = new InternalCodeFragment();
		CodeFragmentLeaf currentExpressionFragment = new CodeFragmentLeaf();

		Expression exprArray = node.getArray();
		Expression exprIndex = node.getIndex();
		if ((exprArray != null) && (exprIndex != null)) {
			currentExpressionFragment.addStatement(exprArray);
			currentExpressionFragment.addStatement(exprIndex);
		}
		parent.addChild(currentExpressionFragment);

		lastNode.push(parent);
		return false;
	}

	@Override
	public boolean visit(ArrayCreation node) {
		lastNode.push(null);
		return false;
	}

	@Override
	public boolean visit(ArrayInitializer node) {
		InternalCodeFragment parent = new InternalCodeFragment();
		CodeFragmentLeaf currentExpressionFragment = new CodeFragmentLeaf();

		List<Expression> exprList = node.expressions();
		if (exprList != null) {
			for (Expression e : exprList) {
				currentExpressionFragment.addStatement(e);
			}
		}

		parent.addChild(currentExpressionFragment);
		lastNode.push(parent);
		return false;
	}

	@Override
	public boolean visit(ArrayType node) {
		lastNode.push(null);
		return false;
	}

	@Override
	public boolean visit(BreakStatement node) {
		lastNode.push(null);
		return false;
	}

	@Override
	public boolean visit(CatchClause node) {
		AbstractCodeFragment parent;

		node.getBody().accept(this);
		AbstractCodeFragment bodyCatch = lastNode.pop();
		if (bodyCatch != null) {
			parent = bodyCatch;
		} else {
			parent = new InternalCodeFragment();
			CodeFragmentLeaf simpleCatchStatement = new CodeFragmentLeaf();
			simpleCatchStatement.addStatement(node.getBody());
			parent.addChild(simpleCatchStatement);
		}
		SingleVariableDeclaration exceptVar = node.getException();
		if (exceptVar != null) {
			parent.addInternalStatement(exceptVar);
		}
		lastNode.push(parent);
		return false;
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		lastNode.push(null);
		return false;
	}

	@Override
	public boolean visit(CompilationUnit node) {
		throw new DontGetHereException("CompilationUnit - It should never get here inside a Block!");
	}

	@Override
	public boolean visit(ContinueStatement node) {
		lastNode.push(null);
		return false;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		InternalCodeFragment parent = new InternalCodeFragment();
		CodeFragmentLeaf currentFragment = new CodeFragmentLeaf();
		// what happend here?
		List<Expression> exprList = node.arguments();
		if (exprList != null) {
			for (Expression e : exprList) {
				currentFragment.addStatement(e);
			}
		}

		parent.addChild(currentFragment);
		lastNode.push(parent);
		return false;
	}

	@Override
	public boolean visit(ReturnStatement node) {
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
	public boolean visit(TryStatement tryStatement) {
		InternalCodeFragment parent = new InternalCodeFragment();

		List<CatchClause> catchList = tryStatement.catchClauses();
		boolean canBeMerged = true;
		if (catchList != null) {
			for (CatchClause c : catchList) {
				c.accept(this);
				AbstractCodeFragment catchNode = lastNode.pop();
				if (catchNode != null) {
					if (!canBeMerged(parent, catchNode))
						canBeMerged = false;
				}
				parent.addChild(catchNode);
			}
		}

		tryStatement.getBody().accept(this);
		AbstractCodeFragment bodyTry = lastNode.pop();
		if (bodyTry != null) {
			if (bodyTry != null) {
				if (!canBeMerged(parent, bodyTry))
					canBeMerged = false;
			}
			parent.addChild(bodyTry);
		} else {
			if (!canBeAddedToBlock(parent, tryStatement))
				canBeMerged = false;
			InternalCodeFragment tryNode = new InternalCodeFragment();
			CodeFragmentLeaf simpleTryStatement = new CodeFragmentLeaf();
			simpleTryStatement.addStatement(tryStatement.getBody());
			tryNode.addChild(simpleTryStatement);
			parent.addChild(tryNode);
		}

		if (tryStatement.getFinally() != null) {
			tryStatement.getFinally().accept(this);
			AbstractCodeFragment bodyFinally = lastNode.pop();
			if (bodyFinally != null) {
				if (bodyFinally != null) {
					if (!canBeMerged(parent, bodyFinally))
						canBeMerged = false;
				}
				parent.addChild(bodyFinally);
			} else {
				if (!canBeAddedToBlock(parent, tryStatement.getFinally()))
					canBeMerged = false;
				InternalCodeFragment finallyNode = new InternalCodeFragment();
				CodeFragmentLeaf simpleFinallyStatement = new CodeFragmentLeaf();
				simpleFinallyStatement.addStatement(tryStatement.getFinally());
				finallyNode.addChild(simpleFinallyStatement);
				parent.addChild(finallyNode);
			}
		}

		if (canBeMerged) {
			lastNode.push(null);
			return false;
		}

		lastNode.push(parent);
		return false;
	}

	@Override
	public boolean visit(SuperConstructorInvocation node) {
		lastNode.push(null);
		return false;
	}

	@Override
	public boolean visit(SwitchCase switchCase) {
		InternalCodeFragment parent = new InternalCodeFragment();
		CodeFragmentLeaf currentExpressionFragment = new CodeFragmentLeaf();

		// what do we care about expr?
		Expression expr = switchCase.getExpression();
		if (expr != null) {
			currentExpressionFragment.addStatement(expr);
			parent.addChild(currentExpressionFragment);
			lastNode.push(null);
		} else {
			lastNode.push(null);
		}

		return false;
	}

	@Override
	public boolean visit(SwitchStatement switchStatement) {
		InternalCodeFragment parent = new InternalCodeFragment();

		Expression expr = switchStatement.getExpression();
		if (expr != null) {
			parent.addInternalStatement(expr);
		}

		List<Statement> switchStatements = switchStatement.statements();
		InternalCodeFragment nodeStatements = new InternalCodeFragment();
		CodeFragmentLeaf currentStatements = new CodeFragmentLeaf();
		boolean canBeMerged = true;
		for (int i = 0; i < switchStatements.size(); i++) {
			Statement statement = switchStatements.get(i);

			statement.accept(this);
			AbstractCodeFragment res = lastNode.pop();
			if (res == null) {
				boolean canBeAddedToBlock = canBeAddedToBlock(currentStatements, statement);
				if (statement instanceof SwitchCase || !canBeAddedToBlock) {
					if (currentStatements.getStatementsLength() != 0) {
						nodeStatements.addChild(currentStatements);
					}
					currentStatements = new CodeFragmentLeaf();
					currentStatements.addStatement(statement);
					if (!canBeAddedToBlock)
						canBeMerged = false;
				} else {
					currentStatements.addStatement(statement);
				}
			} else {
				if (!canBeMerged(nodeStatements, res))
					canBeMerged = false;
				if (currentStatements.getStatementsLength() != 0) {
					nodeStatements.addChild(currentStatements);
					currentStatements = new CodeFragmentLeaf();
				}
				nodeStatements.addChild(res);
			}
		}

		if (currentStatements.getStatementsLength() != 0) {
			nodeStatements.addChild(currentStatements);
		}
		if (nodeStatements != null && nodeStatements.getChildrenSize() != 0) {
			parent.addChild(nodeStatements);
		}

		if (canBeMerged) {
			lastNode.push(null);
			return false;
		}

		lastNode.push(parent);
		return false;
	}

	@Override
	public boolean visit(EmptyStatement node) {
		lastNode.push(null);
		System.out.println("Found empty statement!!!!");
		return false;
	}

	@Override
	public boolean visit(SynchronizedStatement synchronizedStatement) {
		InternalCodeFragment parent = new InternalCodeFragment();

		Expression expr = synchronizedStatement.getExpression();
		if (expr != null) {
			parent.addInternalStatement(expr);
		}

		boolean canBeMerged = true;
		Statement body = synchronizedStatement.getBody();
		body.accept(this);
		AbstractCodeFragment bodySynchronized = lastNode.pop();
		if (bodySynchronized != null) {
			if (!canBeMerged(parent, bodySynchronized))
				canBeMerged = false;
			parent.addChild(bodySynchronized);
		} // else {
//			if (!canBeMerged || !canBeAddedToBlock(parent, body)) {
//				canBeMerged = false;
//				InternalCodeFragment synchronizedNode = new InternalCodeFragment();
//				CodeFragmentLeaf simpleSynchronizedStatement = new CodeFragmentLeaf();
//				simpleSynchronizedStatement.addStatement(body);
//				synchronizedNode.addChild(simpleSynchronizedStatement);
//				parent.addChild(synchronizedNode);
//			}
//		}

		if (canBeMerged) {
			lastNode.push(null);
			return false;
		}

		lastNode.push(parent);
		return false;
	}
}
