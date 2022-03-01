package ro.lrg.method.defragmenter.fragmenters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;
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

import ro.lrg.method.defragmenter.utils.AbstractInternalCodeFragment;
import ro.lrg.method.defragmenter.utils.InternalCodeFragmentLeaf;
import ro.lrg.method.defragmenter.utils.DontGetHereException;
import ro.lrg.method.defragmenter.utils.InternalCodeFragment;

@SuppressWarnings("unchecked")
public class FdpFragmenter extends AbstractFragmenter {

	private IFile iFile;
	private IJavaProject iJavaProject;
	private boolean considerStaticFields;
	private String analyzedClass;

	public FdpFragmenter(boolean considerStaticFields, String analyzedClass, IFile iFile, IJavaProject iJavaProject) {
		this.analyzedClass = analyzedClass;
		this.considerStaticFields = considerStaticFields;
		this.iFile = iFile;
		this.iJavaProject = iJavaProject;
	}

	private HashMap<String, Integer> getFdpOfFragment(AbstractInternalCodeFragment fragment) {
		return fragment.getFdp(analyzedClass, considerStaticFields, null, true);
	}

	private void addStatementToCodeFragment(AbstractInternalCodeFragment parent, Statement statement) {
		InternalCodeFragment childNode = new InternalCodeFragment(iFile, iJavaProject);
		InternalCodeFragmentLeaf childLeaf = new InternalCodeFragmentLeaf(iFile, iJavaProject);
		childLeaf.addStatement(statement);
		childNode.addChild(childLeaf);
		parent.addChild(childNode);
	}

	private boolean canBeMerged(AbstractInternalCodeFragment parent, AbstractInternalCodeFragment toBeMerged) {
		if (toBeMerged.getChildrenSize() != 1 || !(toBeMerged.getChild(0) instanceof InternalCodeFragmentLeaf)
				|| !canBeAddedToBlockWith2FDP(parent, toBeMerged.getAllSubTreeASTNodes(), false)) {
			return false;
		}
		return true;
	}

	private boolean canBeAddedToBlock(AbstractInternalCodeFragment currentFragment, Statement currentStatements) {
		List<ASTNode> statement = new ArrayList<ASTNode>();
		statement.add(currentStatements);
		return canBeAddedToBlockWith2FDP(currentFragment, statement, false);
	}

	private boolean canBeAddedToBlockWith2FDP(AbstractInternalCodeFragment currentFragment, List<ASTNode> currentStatements,
			boolean reduceLimits) {
		if (currentFragment.getStoredFDP() == null) {
			currentFragment.getFdp(analyzedClass, considerStaticFields, null, true);
		}
		HashMap<String, Integer> fdpBefore = currentFragment.getStoredFDP();
		InternalCodeFragmentLeaf tempFragment = new InternalCodeFragmentLeaf(iFile, iJavaProject);
		tempFragment.addStatements(currentStatements);
		return canBeAddedToBlockWith2FDP(currentFragment, reduceLimits, fdpBefore, tempFragment);
	}

	private boolean canBeAddedToBlockWith2FDP(AbstractInternalCodeFragment currentFragment, boolean reduceLimits,
			HashMap<String, Integer> fdpBefore, InternalCodeFragmentLeaf tempFragment) {
		HashMap<String, Integer> fdpAfter = tempFragment.getFdp(analyzedClass, considerStaticFields, null, true);

		if (reduceLimits
				&& ((currentFragment instanceof InternalCodeFragmentLeaf
						&& ((InternalCodeFragmentLeaf) currentFragment).getStatementsLength() != 0)
						|| currentFragment.getInternalAstNodesSize() != 0)
				&& fdpBefore.size() == 0 && fdpAfter.size() >= 1) {
			return false;
		}

		if (fdpBefore.size() == 0) {
			return true;
		}
		if (fdpBefore.size() > 2 || fdpAfter.size() > 2) {
			return false;
		}

		int i = fdpBefore.size();
		for (String key : fdpAfter.keySet()) {
			if (!fdpBefore.containsKey(key)) {
				i++;
			}
		}
		if (i > 2) {
			return false;
		}
		fdpBefore.putAll(fdpAfter);
		return true;
	}

	@Override
	public boolean visit(Block node) {

		InternalCodeFragment parent = new InternalCodeFragment(iFile, iJavaProject);

		parent.setStartNode(node.getStartPosition());
		parent.setEndNode(node.getStartPosition() + node.getLength());

		InternalCodeFragmentLeaf currentFragment = null;
		List<Statement> currentStatements = node.statements();

		List<AbstractInternalCodeFragment> codeFragments = new ArrayList<AbstractInternalCodeFragment>();
		for (int i = 0; i < currentStatements.size(); i++) {
			Statement statement = currentStatements.get(i);

			// Visit each statement in current block
			((ASTNode) statement).accept(this);

			// Get subtree from below
			AbstractInternalCodeFragment res = null;
			if (!lastNode.isEmpty()) {
				res = lastNode.pop();
			}

			// If there is no block below, this means there are only statements
			if (res == null) {
				currentFragment = new InternalCodeFragmentLeaf(iFile, iJavaProject);
				// Adding it to the current leaf
				currentFragment.addStatement(statement);
				codeFragments.add(currentFragment);
			} else {
				// Adding the node which was below and contains a block in it.
				codeFragments.add(res);
			}
		}

		currentFragment = new InternalCodeFragmentLeaf(iFile, iJavaProject);
		HashMap<String, Integer> fdpOfCurrentLeaf;
		HashMap<String, Integer> storedFDP = new HashMap<String, Integer>();
		for (int i = 0; i < codeFragments.size(); i++) {
			AbstractInternalCodeFragment currentAbstractFragment = codeFragments.get(i);
			
			if (currentFragment == null) {
				currentFragment = new InternalCodeFragmentLeaf(iFile, iJavaProject);
				storedFDP = new HashMap<String, Integer>();
			}
			storedFDP = currentFragment.getStoredFDP();
			if(storedFDP==null){
				storedFDP = getFdpOfFragment(currentFragment);
			}

			if (currentAbstractFragment instanceof InternalCodeFragmentLeaf) {
				InternalCodeFragmentLeaf currentLeaf = (InternalCodeFragmentLeaf) currentAbstractFragment;
				fdpOfCurrentLeaf = getFdpOfFragment(currentLeaf);

				if (storedFDP.size() > 0 && fdpOfCurrentLeaf.size() == 0) {
					InternalCodeFragmentLeaf accumulatorLeaf = currentLeaf;
					if (i == currentStatements.size() - 1) {
			        	parent.addChild(currentFragment);
			        	currentFragment=null;
						parent.addChild(currentLeaf);
					}
					for (int j = i + 1; j < codeFragments.size(); j++) {
						AbstractInternalCodeFragment nextFragment = codeFragments.get(j);

						if (!(nextFragment instanceof InternalCodeFragmentLeaf)) {
							parent.addChild(currentFragment);
							currentFragment = null;
							parent.addChild(accumulatorLeaf);
							accumulatorLeaf = null;
							parent.addChild(nextFragment);
							i = j;
							break;
						} else {
							InternalCodeFragmentLeaf nextLeaf = (InternalCodeFragmentLeaf) nextFragment;
							HashMap<String, Integer> nextStatementFdp = getFdpOfFragment(nextLeaf);
							if (nextStatementFdp.size() != 0) {
								HashMap<String, Integer> tempStoredFDP = new HashMap<String, Integer>();
								tempStoredFDP.putAll(nextStatementFdp);
								tempStoredFDP.putAll(storedFDP);
								if (tempStoredFDP.size() > 2) {
									parent.addChild(currentFragment);
									currentFragment = new InternalCodeFragmentLeaf(iFile, iJavaProject);
									parent.addChild(accumulatorLeaf);
									accumulatorLeaf = null;
									i = j;
									currentFragment.addStatements(nextLeaf.getStatements());
									currentFragment.setStoredFDP(nextStatementFdp);
									break;
								} else {
									currentFragment.addStatements(accumulatorLeaf.getStatements());
									currentFragment.addStatements(nextLeaf.getStatements());
									currentFragment.setStoredFDP(tempStoredFDP);
									i = j;
									break;
								}

							} else {
								if (j == currentStatements.size() - 1) {
									parent.addChild(currentFragment);
									currentFragment = null;
									accumulatorLeaf.addStatements(nextLeaf.getStatements());
									parent.addChild(accumulatorLeaf);
									i = j;
									break;
								} else {
									accumulatorLeaf.addStatements(nextLeaf.getStatements());
								}
							}
						}
					}
				} else {
					ArrayList<ASTNode> statements = currentLeaf.getStatements();
					boolean canBeAdded = canBeAddedToBlockWith2FDP(currentFragment, statements, true);
					if (!canBeAdded) {
						parent.addChild(currentFragment);
						currentFragment = new InternalCodeFragmentLeaf(iFile, iJavaProject);
					}

					// Adding it to the current leaf
					currentFragment.addStatements(statements);
					storedFDP = getFdpOfFragment(currentFragment);
				}
			} else {
				if (currentFragment != null) {
					parent.addChild(currentFragment);
					currentFragment = new InternalCodeFragmentLeaf(iFile, iJavaProject);
					storedFDP = new HashMap<String, Integer>();
				}
				parent.addChild(currentAbstractFragment);
			}
		}
        if(currentFragment!=null){
        	parent.addChild(currentFragment);
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
		AbstractInternalCodeFragment parent = new InternalCodeFragment(iFile, iJavaProject);

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
			AbstractInternalCodeFragment resThen = lastNode.pop();
			if (resThen != null) {
				if (!canBeMerged(parent, resThen)) {
					canBeMerged = false;
				}
				parent.addChild(resThen);
			} else {
				if (!canBeAddedToBlock(parent, thenStatement)) {
					canBeMerged = false;
					addStatementToCodeFragment(parent, thenStatement);
				}

			}
		}

		if (ifStatement.getElseStatement() != null) {
			Statement elseStatement = ifStatement.getElseStatement();
			elseStatement.accept(this);
			AbstractInternalCodeFragment resElse = lastNode.pop();
			if (resElse != null) {
				if (!canBeMerged(parent, resElse)) {
					canBeMerged = false;
				}
				parent.addChild(resElse);
			} else {
				if (!canBeMerged || !canBeAddedToBlock(parent, elseStatement)) {
					canBeMerged = false;
					addStatementToCodeFragment(parent, elseStatement);
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
		InternalCodeFragment parent = new InternalCodeFragment(iFile, iJavaProject);

		Expression expr = doStatement.getExpression();
		if (expr != null) {
			parent.addInternalStatement(expr);
		}

		boolean canBeMerged = true;
		doStatement.getBody().accept(this);
		AbstractInternalCodeFragment doWhileBody = lastNode.pop();
		if (doWhileBody != null) {
			if (!canBeMerged(parent, doWhileBody))
				canBeMerged = false;
			parent.addChild(doWhileBody);
		} else {
			if (!canBeAddedToBlock(parent, doStatement.getBody())) {
				canBeMerged = false;
				addStatementToCodeFragment(parent, doStatement.getBody());
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
		InternalCodeFragment parent = new InternalCodeFragment(iFile, iJavaProject);

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
		AbstractInternalCodeFragment forBody = lastNode.pop();
		if (forBody != null) {
			if (!canBeMerged(parent, forBody))
				canBeMerged = false;
			parent.addChild(forBody);
		} else {
			if (!canBeMerged || !canBeAddedToBlock(parent, forBodyStatement)) {
				canBeMerged = false;
				addStatementToCodeFragment(parent, forBodyStatement);

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
		InternalCodeFragment parent = new InternalCodeFragment(iFile, iJavaProject);

		Expression expr = forStatement.getExpression();
		if (expr != null) {
			parent.addInternalStatement(expr);
		}

		SingleVariableDeclaration param = forStatement.getParameter();
		parent.addInternalStatement(param);

		boolean canBeMerged = true;
		Statement forBodyStatement = forStatement.getBody();
		forBodyStatement.accept(this);
		AbstractInternalCodeFragment forBody = lastNode.pop();
		if (forBody != null) {
			if (!canBeMerged(parent, forBody))
				canBeMerged = false;
			parent.addChild(forBody);
		} else {
			if (!canBeMerged || !canBeAddedToBlock(parent, forBodyStatement)) {
				canBeMerged = false;
				addStatementToCodeFragment(parent, forBodyStatement);

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
		InternalCodeFragment parent = new InternalCodeFragment(iFile, iJavaProject);

		Expression expr = whileStatement.getExpression();
		if (expr != null) {
			parent.addInternalStatement(expr);
		}

		boolean canBeMerged = true;
		Statement body = whileStatement.getBody();
		body.accept(this);
		AbstractInternalCodeFragment bodyWhile = lastNode.pop();
		if (bodyWhile != null) {
			if (!canBeMerged(parent, bodyWhile))
				canBeMerged = false;
			parent.addChild(bodyWhile);
		} else {
			if (!canBeMerged || !canBeAddedToBlock(parent, body)) {
				canBeMerged = false;
				addStatementToCodeFragment(parent, body);
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
		InternalCodeFragment parent = new InternalCodeFragment(iFile, iJavaProject);
		InternalCodeFragmentLeaf currentExpressionFragment = new InternalCodeFragmentLeaf(iFile, iJavaProject);

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
		InternalCodeFragment parent = new InternalCodeFragment(iFile, iJavaProject);
		InternalCodeFragmentLeaf currentExpressionFragment = new InternalCodeFragmentLeaf(iFile, iJavaProject);

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
		AbstractInternalCodeFragment parent;

		node.getBody().accept(this);
		AbstractInternalCodeFragment bodyCatch = lastNode.pop();
		if (bodyCatch != null) {
			parent = bodyCatch;
		} else {
			parent = new InternalCodeFragment(iFile, iJavaProject);
			InternalCodeFragmentLeaf simpleCatchStatement = new InternalCodeFragmentLeaf(iFile, iJavaProject);
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
		InternalCodeFragment parent = new InternalCodeFragment(iFile, iJavaProject);
		InternalCodeFragmentLeaf currentFragment = new InternalCodeFragmentLeaf(iFile, iJavaProject);
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
		InternalCodeFragment parent = new InternalCodeFragment(iFile, iJavaProject);

		List<CatchClause> catchList = tryStatement.catchClauses();
		boolean canBeMerged = true;
		if (catchList != null) {
			for (CatchClause c : catchList) {
				c.accept(this);
				AbstractInternalCodeFragment catchNode = lastNode.pop();
				if (catchNode != null) {
					if (!canBeMerged(parent, catchNode))
						canBeMerged = false;
				}
				parent.addChild(catchNode);
			}
		}

		tryStatement.getBody().accept(this);
		AbstractInternalCodeFragment bodyTry = lastNode.pop();
		if (bodyTry != null) {
			if (bodyTry != null) {
				if (!canBeMerged(parent, bodyTry))
					canBeMerged = false;
			}
			parent.addChild(bodyTry);
		} else {
			if (!canBeAddedToBlock(parent, tryStatement.getBody()))
				canBeMerged = false;
			addStatementToCodeFragment(parent, tryStatement.getBody());
		}

		if (tryStatement.getFinally() != null) {
			tryStatement.getFinally().accept(this);
			AbstractInternalCodeFragment bodyFinally = lastNode.pop();
			if (bodyFinally != null) {
				if (bodyFinally != null) {
					if (!canBeMerged(parent, bodyFinally))
						canBeMerged = false;
				}
				parent.addChild(bodyFinally);
			} else {
				if (!canBeAddedToBlock(parent, tryStatement.getFinally()))
					canBeMerged = false;
				addStatementToCodeFragment(parent, tryStatement.getFinally());
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
		InternalCodeFragment parent = new InternalCodeFragment(iFile, iJavaProject);
		InternalCodeFragmentLeaf currentExpressionFragment = new InternalCodeFragmentLeaf(iFile, iJavaProject);

		// why do we care about expr?
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
		InternalCodeFragment parent = new InternalCodeFragment(iFile, iJavaProject);

		Expression expr = switchStatement.getExpression();
		if (expr != null) {
			parent.addInternalStatement(expr);
		}

		List<Statement> switchStatements = switchStatement.statements();
		InternalCodeFragment nodeStatements = new InternalCodeFragment(iFile, iJavaProject);
		InternalCodeFragmentLeaf currentStatements = new InternalCodeFragmentLeaf(iFile, iJavaProject);
		boolean canBeMerged = true;
		for (int i = 0; i < switchStatements.size(); i++) {
			Statement statement = switchStatements.get(i);

			statement.accept(this);
			AbstractInternalCodeFragment res = lastNode.pop();
			if (res == null) {
				boolean canBeAddedToBlock = canBeAddedToBlock(currentStatements, statement);
				if (statement instanceof SwitchCase || !canBeAddedToBlock) {
					if (currentStatements.getStatementsLength() != 0) {
						nodeStatements.addChild(currentStatements);
					}
					currentStatements = new InternalCodeFragmentLeaf(iFile, iJavaProject);
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
					currentStatements = new InternalCodeFragmentLeaf(iFile, iJavaProject);
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
		InternalCodeFragment parent = new InternalCodeFragment(iFile, iJavaProject);

		Expression expr = synchronizedStatement.getExpression();
		if (expr != null) {
			parent.addInternalStatement(expr);
		}

		boolean canBeMerged = true;
		Statement body = synchronizedStatement.getBody();
		body.accept(this);
		AbstractInternalCodeFragment bodySynchronized = lastNode.pop();
		if (bodySynchronized != null) {
			if (!canBeMerged(parent, bodySynchronized))
				canBeMerged = false;
			parent.addChild(bodySynchronized);
		}

		if (canBeMerged) {
			lastNode.push(null);
			return false;
		}

		lastNode.push(parent);
		return false;
	}
}
