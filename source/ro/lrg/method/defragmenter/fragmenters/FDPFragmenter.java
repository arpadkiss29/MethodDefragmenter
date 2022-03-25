package ro.lrg.method.defragmenter.fragmenters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
public class FDPFragmenter extends ASTVisitor {
	public Stack<AbstractInternalCodeFragment> lastNode;
	public Stack<AbstractInternalCodeFragment> getLastNode() {
		return lastNode;
	}
	
	private String analyzedClass;
	private IFile iFile;
	private IJavaProject iJavaProject;
	private boolean considerStaticFieldAccess;
	private boolean libraryCheck;
	private int minBlockSize;
	private int FDPTreshold;

	public FDPFragmenter(String analyzedClass, IFile iFile, IJavaProject iJavaProject,
			boolean considerStaticFieldsAccess, boolean libraryCheck, int minBlockSize, int FDPTreshold) {
		lastNode = new Stack<AbstractInternalCodeFragment>();
		this.analyzedClass = analyzedClass;
		this.iFile = iFile;
		this.iJavaProject = iJavaProject;
		this.considerStaticFieldAccess = considerStaticFieldsAccess;
		this.libraryCheck = libraryCheck;
		this.minBlockSize = minBlockSize;
		this.FDPTreshold = FDPTreshold;
	}
	
	private InternalCodeFragment newInternalCodeFragment() {
		return new InternalCodeFragment(iFile, iJavaProject);
	}
	
	private InternalCodeFragmentLeaf newInternalCodeFragmentLeaf() {
		return new InternalCodeFragmentLeaf(iFile, iJavaProject);
	}
	
	private Map<String, Integer> computeAndGetFDPMapOfFragment(AbstractInternalCodeFragment AICF) {
		return AICF.computeAndGetFDPMap(analyzedClass, considerStaticFieldAccess, libraryCheck, minBlockSize);
	}

	private void addStatementToFragment(InternalCodeFragment parent, Statement statement) {
		InternalCodeFragment node = newInternalCodeFragment();
		InternalCodeFragmentLeaf leaf = newInternalCodeFragmentLeaf();
		leaf.addInternalStatement(statement);
		node.addChild(leaf);
		parent.addChild(node);
	}

	private boolean canMergeFragmentWithParent(AbstractInternalCodeFragment parent, InternalCodeFragment fragmentToBeMerged) {
		if (fragmentToBeMerged.getChildrenSize() != 1 
				|| fragmentToBeMerged.getChild(0) instanceof InternalCodeFragment
				|| !canAddStatementsToFragment(parent, fragmentToBeMerged.getAllInternalStatements(), false)) {
			return false;
		}
		return true;
	}

	private boolean canAddStatementToFragment(AbstractInternalCodeFragment fragment, Statement statement) {
		List<ASTNode> statements = new ArrayList<ASTNode>();
		statements.add(statement);
		return canAddStatementsToFragment(fragment, statements, false);
	}

	private boolean canAddStatementsToFragment(AbstractInternalCodeFragment fragment, List<ASTNode> statements, boolean reduceLimits) {
		Map<String, Integer> FDPMap = fragment.getFDPMap();
		if (FDPMap == null) {
			FDPMap = computeAndGetFDPMapOfFragment(fragment);
		}

		InternalCodeFragmentLeaf tempFragment = newInternalCodeFragmentLeaf();
		tempFragment.addInternalStatements(statements);
		Map<String, Integer> tempFDPMap = computeAndGetFDPMapOfFragment(tempFragment);

		if (reduceLimits
				&& fragment.getInternalStatementsSize() != 0
				&& FDPMap.size() == 0
				&& tempFDPMap.size() > 0) {
			return false;
		}

		//??????????????????????????????????????????????????????????????????
		if (FDPMap.size() == 0) return true;
		if (FDPMap.size() > FDPTreshold || tempFDPMap.size() > FDPTreshold) return false;
		Map<String, Integer> auxFDPMap = combineFDPMaps(FDPMap, tempFDPMap);
		if (auxFDPMap.size() > FDPTreshold) return false;
		//??????????????????????????????????????????????????????????????????
		//FDPMap.putAll(tempFDPMap);
		FDPMap.putAll(auxFDPMap);
		
		return true;
	}

	private Map<String, Integer> combineFDPMaps(Map<String, Integer> FDPMap1, Map<String, Integer> FDPMap2) {
		Map<String, Integer> resultedMap = new HashMap<>(FDPMap1);
		for (Entry<String, Integer> entry : FDPMap2.entrySet()) {
			if (!FDPMap1.containsKey(entry.getKey())) {
				resultedMap.put(entry.getKey(), entry.getValue());
			} else {
				resultedMap.put(entry.getKey(), resultedMap.get(entry.getKey()) + entry.getValue());
			}
		}
		return resultedMap;
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
		parent.setStartPosition(node.getStartPosition());
		parent.setEndPosition(node.getStartPosition() + node.getLength());
		
		List<AbstractInternalCodeFragment> fragments = getRawAbstractInternalCodeFragments(node);
		
		InternalCodeFragmentLeaf acumulatingLeaf = newInternalCodeFragmentLeaf();
		Map<String, Integer> FDPMapOfAcumulatingLeaf = computeAndGetFDPMapOfFragment(acumulatingLeaf);
		
		for (AbstractInternalCodeFragment fragment : fragments) {
			
			if (acumulatingLeaf == null) {
				acumulatingLeaf = newInternalCodeFragmentLeaf();
				FDPMapOfAcumulatingLeaf = new HashMap<String, Integer>();
			}
			
			FDPMapOfAcumulatingLeaf = acumulatingLeaf.getFDPMap();
			if(FDPMapOfAcumulatingLeaf == null){
				FDPMapOfAcumulatingLeaf = computeAndGetFDPMapOfFragment(acumulatingLeaf);
			}
			
			if (fragment instanceof InternalCodeFragment) {
//				//for node
//				//executes every time
				if (acumulatingLeaf != null) {
					parent.addChild(acumulatingLeaf);
					acumulatingLeaf = newInternalCodeFragmentLeaf();
					FDPMapOfAcumulatingLeaf = new HashMap<String, Integer>();
				}
				parent.addChild(fragment);
			} else {
				InternalCodeFragmentLeaf leaf = (InternalCodeFragmentLeaf) fragment;
				Map<String, Integer> FDPMapOfLeaf = computeAndGetFDPMapOfFragment(leaf);

				if (FDPMapOfAcumulatingLeaf.size() == 0 || FDPMapOfLeaf.size() > 0)  {
					List<ASTNode> leafsStatements = leaf.getAllInternalStatements();
					if (!canAddStatementsToFragment(acumulatingLeaf, leafsStatements, true)) {
						parent.addChild(acumulatingLeaf);
						acumulatingLeaf = newInternalCodeFragmentLeaf();
					}
					// Adding it to the current leaf
					acumulatingLeaf.addInternalStatements(leafsStatements);
					FDPMapOfAcumulatingLeaf = computeAndGetFDPMapOfFragment(acumulatingLeaf);
//				//-------------------------------------------------------------------------------------
				} else if (FDPMapOfAcumulatingLeaf.size() > 0 && FDPMapOfLeaf.size() == 0) {
					int i = fragments.indexOf(fragment);
					int statementsSize = node.statements().size();
					if (i == statementsSize - 1) {
			        	parent.addChild(acumulatingLeaf);
			        	parent.addChild(leaf);
			        	acumulatingLeaf = null;
					}
					
					for (int j = i + 1; j < fragments.size(); j++) {
						AbstractInternalCodeFragment nextFragment = fragments.get(j);

						if (nextFragment instanceof InternalCodeFragment) {
							parent.addChild(acumulatingLeaf);
							acumulatingLeaf = null;
							parent.addChild(leaf);
							parent.addChild(nextFragment);
							i = j;
							break;
						} else {
							InternalCodeFragmentLeaf nextLeaf = (InternalCodeFragmentLeaf) nextFragment;
							Map<String, Integer> FDPMapOfNextLeaf = computeAndGetFDPMapOfFragment(nextLeaf);
							
							if (FDPMapOfNextLeaf.size() != 0) {
								HashMap<String, Integer> tempStoredFDP = new HashMap<String, Integer>();
								
								//?????????????????????
								tempStoredFDP.putAll(FDPMapOfNextLeaf);
								tempStoredFDP.putAll(FDPMapOfAcumulatingLeaf);
								
								if (tempStoredFDP.size() > FDPTreshold) {
									parent.addChild(acumulatingLeaf);
									acumulatingLeaf = newInternalCodeFragmentLeaf();
									parent.addChild(leaf);
									i = j;
									acumulatingLeaf.addInternalStatements(nextLeaf.getAllInternalStatements());
									acumulatingLeaf.setFDPMap(FDPMapOfNextLeaf);
									break;
								} else {
									acumulatingLeaf.addInternalStatements(leaf.getAllInternalStatements());
									acumulatingLeaf.addInternalStatements(nextLeaf.getAllInternalStatements());
									
									//?????????????????????????????????????????????
									acumulatingLeaf.setFDPMap(tempStoredFDP);
									
									i = j;
									break;
								}

							} else {
								if (j == statementsSize - 1) {
									parent.addChild(acumulatingLeaf);
									acumulatingLeaf = null;
									
									leaf.addInternalStatements(nextLeaf.getAllInternalStatements());
									parent.addChild(leaf);
									i = j;
									break;
								} else {
									leaf.addInternalStatements(nextLeaf.getAllInternalStatements());
									
								}
							}
						}
					}
				}
//				//-------------------------------------------------------------------------------------
			}
		}
		
        if(acumulatingLeaf!=null){
        	parent.addChild(acumulatingLeaf);
        }
        
		lastNode.push(parent);
		return false;

//		InternalCodeFragmentLeaf leaf = newInternalCodeFragmentLeaf();
//		Map<String, Integer> fdpOfCurrentLeaf;
//		Map<String, Integer> storedFDP = new HashMap<String, Integer>();
//		for (int i = 0; i < fragments.size(); i++) {
//			AbstractInternalCodeFragment currentAbstractFragment = fragments.get(i);
//			
//			if (leaf == null) {
//				leaf = newInternalCodeFragmentLeaf();
//				storedFDP = new HashMap<String, Integer>();
//			}
//			storedFDP = leaf.getFDPMap();
//			if(storedFDP==null){
//				storedFDP = computeAndGetFDPMapOfFragment(leaf);
//			}
//
//			if (currentAbstractFragment instanceof InternalCodeFragmentLeaf) {
//				InternalCodeFragmentLeaf currentLeaf = (InternalCodeFragmentLeaf) currentAbstractFragment;
//				fdpOfCurrentLeaf = computeAndGetFDPMapOfFragment(currentLeaf);
//
//				if (storedFDP.size() > 0 && fdpOfCurrentLeaf.size() == 0) {
//					InternalCodeFragmentLeaf accumulatorLeaf = currentLeaf;
//					int statementsSize = node.statements().size();
//					if (i == statementsSize - 1) {
//			        	parent.addChild(leaf);
//			        	leaf=null;
//						parent.addChild(currentLeaf);
//					}
//					for (int j = i + 1; j < fragments.size(); j++) {
//						AbstractInternalCodeFragment nextFragment = fragments.get(j);
//
//						if (!(nextFragment instanceof InternalCodeFragmentLeaf)) {
//							parent.addChild(leaf);
//							leaf = null;
//							parent.addChild(accumulatorLeaf);
//							accumulatorLeaf = null;
//							parent.addChild(nextFragment);
//							i = j;
//							break;
//						} else {
//							InternalCodeFragmentLeaf nextLeaf = (InternalCodeFragmentLeaf) nextFragment;
//							Map<String, Integer> nextStatementFdp = computeAndGetFDPMapOfFragment(nextLeaf);
//							if (nextStatementFdp.size() != 0) {
//								Map<String, Integer> tempStoredFDP = new HashMap<String, Integer>();
//								tempStoredFDP.putAll(nextStatementFdp);
//								tempStoredFDP.putAll(storedFDP);
//								if (tempStoredFDP.size() > 2) {
//									parent.addChild(leaf);
//									leaf = newInternalCodeFragmentLeaf();
//									parent.addChild(accumulatorLeaf);
//									accumulatorLeaf = null;
//									i = j;
//									leaf.addInternalStatements(nextLeaf.getInternalStatements());
//									leaf.setFDPMap(nextStatementFdp);
//									break;
//								} else {
//									leaf.addInternalStatements(accumulatorLeaf.getInternalStatements());
//									leaf.addInternalStatements(nextLeaf.getInternalStatements());
//									leaf.setFDPMap(tempStoredFDP);
//									i = j;
//									break;
//								}
//
//							} else {
//								if (j == statementsSize - 1) {
//									parent.addChild(leaf);
//									leaf = null;
//									accumulatorLeaf.addInternalStatements(nextLeaf.getInternalStatements());
//									parent.addChild(accumulatorLeaf);
//									i = j;
//									break;
//								} else {
//									accumulatorLeaf.addInternalStatements(nextLeaf.getInternalStatements());
//								}
//							}
//						}
//					}
//				} else {
//					List<ASTNode> statements = currentLeaf.getInternalStatements();
//					boolean canBeAdded = canAddStatementsToFragment(leaf, statements, true);
//					if (!canBeAdded) {
//						parent.addChild(leaf);
//						leaf = newInternalCodeFragmentLeaf();
//					}
//
//					// Adding it to the current leaf
//					leaf.addInternalStatements(statements);
//					storedFDP = computeAndGetFDPMapOfFragment(leaf);
//				}
//			} else {
//				if (leaf != null) {
//					parent.addChild(leaf);
//					leaf = newInternalCodeFragmentLeaf();
//					storedFDP = new HashMap<String, Integer>();
//				}
//				parent.addChild(currentAbstractFragment);
//			}
//		}
//        if(leaf!=null){
//        	parent.addChild(leaf);
//        }
//		lastNode.push(parent);
//		return false;
	}
	
	//this method helps some visit methods
	private boolean canBeMerged(InternalCodeFragment parent, Statement statement, boolean resultAfterVisitCanBeNull) {
		statement.accept(this);
		InternalCodeFragment fragment = (InternalCodeFragment) lastNode.pop();
		boolean canMerge = true;
		if (fragment != null) {
			if (!canMergeFragmentWithParent(parent, fragment)) {
				canMerge = false;
			}
			parent.addChild(fragment);
		} else if (resultAfterVisitCanBeNull) {
			if (!canAddStatementToFragment(parent, statement)) {
				canMerge = false;
				addStatementToFragment(parent, statement);
			}
			
		}
		return canMerge;
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
		
		if (canBeMerged(parent, doStatement.getBody(), true)) {
			lastNode.push(null);
		} else {
			lastNode.push(parent);
		}

		return false;
	}
	
	@Override
	public boolean visit(EnhancedForStatement enhancedForStatement) {
		InternalCodeFragment parent = newInternalCodeFragment();

		parent.addInternalStatement(enhancedForStatement.getExpression());
		parent.addInternalStatement(enhancedForStatement.getParameter());

		if (canBeMerged(parent, enhancedForStatement.getBody(), true)) {
			lastNode.push(null);
		} else {
			lastNode.push(parent);
		}
		
		return false;
	}
	
	@Override
	public boolean visit(ForStatement forStatement) {
		InternalCodeFragment parent = newInternalCodeFragment();

		parent.addInternalStatement(forStatement.getExpression());
		parent.addInternalStatements(forStatement.initializers());
		parent.addInternalStatements(forStatement.updaters());

		if (canBeMerged(parent, forStatement.getBody(), true)) {
			lastNode.push(null);
		} else {
			lastNode.push(parent);
		}
		
		return false;
	}
	
	@Override
	public boolean visit(IfStatement ifStatement) {
		InternalCodeFragment parent = newInternalCodeFragment();
		parent.setStartPosition(ifStatement.getStartPosition());
		parent.setEndPosition(ifStatement.getStartPosition() + ifStatement.getLength());
		
		parent.addInternalStatement(ifStatement.getExpression());
		
		Statement thenStatement = ifStatement.getThenStatement();
		Statement elseStatement = ifStatement.getElseStatement();
		if ((thenStatement == null || thenStatement != null && canBeMerged(parent, thenStatement, true))
				&& (elseStatement == null || elseStatement != null && canBeMerged(parent, elseStatement, true))) {
			lastNode.push(null);
		} else {
			lastNode.push(parent);
		}

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
		InternalCodeFragment nodeStatements = newInternalCodeFragment();
		InternalCodeFragmentLeaf currentStatements = newInternalCodeFragmentLeaf();
		boolean canBeMerged = true;
		for (Statement statement : switchStatements) {
			statement.accept(this);
			InternalCodeFragment fragment = (InternalCodeFragment) lastNode.pop();
			if (fragment == null) {
				boolean canBeAddedToBlock = canAddStatementToFragment(currentStatements, statement);
				if (statement instanceof SwitchCase || !canBeAddedToBlock) {
					if (currentStatements.getInternalStatementsSize() != 0) {
						nodeStatements.addChild(currentStatements);
					}
					currentStatements = newInternalCodeFragmentLeaf();
					currentStatements.addInternalStatement(statement);
					if (!canBeAddedToBlock) {
						canBeMerged = false;
					}
				} else {
					currentStatements.addInternalStatement(statement);
				}
			} else {
				if (!canMergeFragmentWithParent(nodeStatements, fragment)) {
					canBeMerged = false;
				}
				if (currentStatements.getInternalStatementsSize() != 0) {
					nodeStatements.addChild(currentStatements);
					currentStatements = newInternalCodeFragmentLeaf();
				}
				nodeStatements.addChild(fragment);
			}
		}

		if (currentStatements.getInternalStatementsSize() != 0) {
			nodeStatements.addChild(currentStatements);
		}
		if (nodeStatements != null && nodeStatements.getChildrenSize() != 0) {
			parent.addChild(nodeStatements);
		}

		if (canBeMerged) {
			lastNode.push(null);
		} else {
			lastNode.push(parent);
		}
		
		return false;
	}
	
	@Override
	public boolean visit(SynchronizedStatement synchronizedStatement) {
		InternalCodeFragment parent = newInternalCodeFragment();

		parent.addInternalStatement(synchronizedStatement.getExpression());

		if (canBeMerged(parent, synchronizedStatement.getBody(), false)) {
			lastNode.push(null);
		} else {
			lastNode.push(parent);
		}
		
		return false;
	}
	
	@Override
	public boolean visit(TryStatement tryStatement) {
		InternalCodeFragment parent = newInternalCodeFragment();
		
		List<CatchClause> catchClauses = tryStatement.catchClauses();
		boolean canBeMerged = true;
		for (CatchClause catchClause : catchClauses) {
			catchClause.accept(this);
			InternalCodeFragment fragment = (InternalCodeFragment) lastNode.pop();
			if (fragment != null) {
				if(!canMergeFragmentWithParent(parent, fragment)) {
					canBeMerged = false;
				}
				parent.addChild(fragment);
			}
		}
		
		Statement tryBody = tryStatement.getBody();
		Statement finallyBody = tryStatement.getFinally();
		if (canBeMerged
				&& (tryBody == null || tryBody != null && canBeMerged(parent, tryBody, true))
				&& (finallyBody == null || finallyBody != null && canBeMerged(parent, finallyBody, true))) {
			lastNode.push(null);
		} else {
			lastNode.push(parent);
		}
		
		return false;
	}
	
	@Override
	public boolean visit(WhileStatement whileStatement) {
		InternalCodeFragment parent = newInternalCodeFragment();

		parent.addInternalStatement(whileStatement.getExpression());

		if (canBeMerged(parent, whileStatement.getBody(), true)) {
			lastNode.push(null);
		} else {
			lastNode.push(parent);
		}
		
		return false;
	}

	//--------------------------------------------------------------------------simple visit methods
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
