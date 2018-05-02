package kar.method.defragmenter.fragmenters;

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
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import kar.method.defragmenter.utils.CodeFragmentLeaf;
import kar.method.defragmenter.utils.CodeFragmentTreeNode;
import kar.method.defragmenter.utils.DontGetHereException;
import kar.method.defragmenter.utils.FixedStructureTypes;

@SuppressWarnings("unchecked")
public class ChunkFragmenter extends AbstractFragmenter{
	
	private CompilationUnit unit;
	private boolean considerBlankLines;
	
	
	public ChunkFragmenter(CompilationUnit unit, boolean considerBlankLines) {
		this.unit = unit;
		this.considerBlankLines = considerBlankLines;
	}

	@Override
	public boolean visit(Block node) {
		
		CodeFragmentTreeNode parent = new CodeFragmentTreeNode();

		CodeFragmentLeaf currentFragment = null;
		List<Statement> currentStatements = node.statements();
		int previousLine = 0;
		int lineDiff = 0;
		
		for(int i=0; i<currentStatements.size(); i++){
			
			// Visit each statement in current block
			((ASTNode) currentStatements.get(i)).accept(this);
			
			// Get subtree from below
			CodeFragmentTreeNode res = null;
			if(!lastNode.isEmpty()){
				res = lastNode.pop();
			}
			
			
			// If there is no block below, this means there are only statements
			if(res == null) {		
				// If current statement belongs to the part of code before the block; 
				if(currentFragment == null) {
					currentFragment = new CodeFragmentLeaf();
				}
				
				if(considerBlankLines){
					if(previousLine != 0 && previousLine + (lineDiff + 1) != unit.getLineNumber(currentStatements.get(i).getStartPosition())){
						parent.addChild(currentFragment);
						currentFragment = new CodeFragmentLeaf();
					}
					previousLine = unit.getLineNumber(currentStatements.get(i).getStartPosition());
					int endLine = unit.getLineNumber(currentStatements.get(i).getStartPosition() + currentStatements.get(i).getLength());
					if(endLine - previousLine > 0){
						lineDiff = endLine - previousLine;
						//System.out.println("Line diff: " + lineDiff);
					}else{
						lineDiff = 0;
					}
				}

				// Adding it to the current leaf
				currentFragment.addStatement(currentStatements.get(i));
			} else {
				// There is a block below, if there are statements before and they are grouped in a leaf, the leaf must be added.
				if(currentFragment != null) {		
					parent.addChild(currentFragment);
					currentFragment = null;
				}
				// Adding the node which was below and contains a block in it.
				parent.addChild(res);
			}
		}
		
		// Fragment constructed based on statements but never added to parent, this is why its needed to be added here.
		if(currentFragment != null) {
			parent.addChild(currentFragment);
			currentFragment = null;
		}
		
		lastNode.push(parent);
		return false;
	}

	public boolean visit(Assignment assignment){
		lastNode.push(null);
		return false;
	}
	

	public boolean visit(Expression expression){
		lastNode.push(null);
		return false;
	}
	
	
	public boolean visit(IfStatement ifStatement){
		CodeFragmentTreeNode parent = new CodeFragmentTreeNode();
		
		Expression expr = ifStatement.getExpression();
		if (expr != null){
			parent.addInternalStatement(expr);
		}

		if(ifStatement.getThenStatement() != null){
			ifStatement.getThenStatement().accept(this);
			CodeFragmentTreeNode resThen = lastNode.pop();
			if (resThen != null){
				resThen.setType(FixedStructureTypes.IF_THEN);
				parent.addChild(resThen);
			}else{
				CodeFragmentTreeNode thenNode = new CodeFragmentTreeNode();
				CodeFragmentLeaf thenStatement = new CodeFragmentLeaf();
				thenStatement.addStatement(ifStatement.getThenStatement());
				thenNode.addChild(thenStatement);
				thenNode.setType(FixedStructureTypes.IF_THEN);
				parent.addChild(thenNode);
			}
		}

		if(ifStatement.getElseStatement() != null){
			ifStatement.getElseStatement().accept(this);
			CodeFragmentTreeNode resElse = lastNode.pop();
			if (resElse != null){
				resElse.setType(FixedStructureTypes.IF_ELSE);
				parent.addChild(resElse);
			}else{
				CodeFragmentTreeNode elseNode = new CodeFragmentTreeNode();
				CodeFragmentLeaf elseStatement = new CodeFragmentLeaf();
				elseStatement.addStatement(ifStatement.getElseStatement());
				elseNode.addChild(elseStatement);
				elseNode.setType(FixedStructureTypes.IF_ELSE);
				parent.addChild(elseNode);
			}
		}
		
		parent.setType(FixedStructureTypes.IF);
		lastNode.push(parent);
		return false;
	}
	
	@Override
	public boolean visit(DoStatement doStatement) {
		CodeFragmentTreeNode parent = new CodeFragmentTreeNode();
		
		Expression expr = doStatement.getExpression();
		if (expr != null){
			parent.addInternalStatement(expr);
		}
		
		doStatement.getBody().accept(this);
		CodeFragmentTreeNode doWhileBody = lastNode.pop();
		if (doWhileBody != null){
			parent.addChild(doWhileBody);
		}else{
			CodeFragmentTreeNode doNode = new CodeFragmentTreeNode();
			CodeFragmentLeaf simpleDoWhileStatement = new CodeFragmentLeaf();
			simpleDoWhileStatement.addStatement(doStatement.getBody());
			doNode.addChild(simpleDoWhileStatement);
			parent.addChild(doNode);
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
		CodeFragmentTreeNode parent = new CodeFragmentTreeNode();
		
		Expression expr = forStatement.getExpression();
		if (expr != null){
			parent.addInternalStatement(expr);
		}
		
		List<Expression> inits = forStatement.initializers();
		for(Expression each : inits){
			parent.addInternalStatement(each);
		}
		
		List<Expression> updates = forStatement.updaters();
		for(Expression each : updates){
			parent.addInternalStatement(each);
		}

		forStatement.getBody().accept(this);
		CodeFragmentTreeNode forBody = lastNode.pop();
		if (forBody != null){
			parent.addChild(forBody);
		}else{
			CodeFragmentTreeNode forNode = new CodeFragmentTreeNode();
			CodeFragmentLeaf simpleForStatement = new CodeFragmentLeaf();
			simpleForStatement.addStatement(forStatement.getBody());
			forNode.addChild(simpleForStatement);
			parent.addChild(forNode);
		}
		
		lastNode.push(parent);
		return false;
	}

	@Override
	public boolean visit(WhileStatement whileStatement) {
		CodeFragmentTreeNode parent = new CodeFragmentTreeNode();
		
		Expression expr = whileStatement.getExpression();
		if (expr != null){
			parent.addInternalStatement(expr);
		}
		
		
		whileStatement.getBody().accept(this);
		CodeFragmentTreeNode bodyWhile = lastNode.pop();
		if (bodyWhile != null){
			parent.addChild(bodyWhile);
		}else{
			CodeFragmentTreeNode whileNode = new CodeFragmentTreeNode();
			CodeFragmentLeaf simpleWhileStatement = new CodeFragmentLeaf();
			simpleWhileStatement.addStatement(whileStatement.getBody());
			whileNode.addChild(simpleWhileStatement);
			parent.addChild(whileNode);
		}
		
		lastNode.push(parent);
		return false;
	}
	
	
	
	@Override
	public boolean visit(ArrayAccess node) {
		CodeFragmentTreeNode parent = new CodeFragmentTreeNode();
		CodeFragmentLeaf currentExpressionFragment = new CodeFragmentLeaf();
		
		Expression exprArray = node.getArray();
		Expression exprIndex = node.getIndex();
		if ((exprArray != null) &&(exprIndex != null)){
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
		CodeFragmentTreeNode parent = new CodeFragmentTreeNode();
		CodeFragmentLeaf currentExpressionFragment = new CodeFragmentLeaf();
		
		List<Expression> exprList = node.expressions();
		if (exprList != null){
			for (Expression e : exprList){
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
		CodeFragmentTreeNode parent = new CodeFragmentTreeNode();
		
		SingleVariableDeclaration exceptVar = node.getException();
		if (exceptVar != null){
			parent.addInternalStatement(exceptVar);
		}
		
		node.getBody().accept(this);
		CodeFragmentTreeNode bodyCatch = lastNode.pop();
		if (bodyCatch != null){
			parent.addChild(bodyCatch);
		}else{
			CodeFragmentTreeNode CatchNode = new CodeFragmentTreeNode();
			CodeFragmentLeaf simpleCatchStatement = new CodeFragmentLeaf();
			simpleCatchStatement.addStatement(node.getBody());
			CatchNode.addChild(simpleCatchStatement);
			parent.addChild(CatchNode);
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
	public boolean visit(CompilationUnit node){
		throw new DontGetHereException("CompilationUnit - It should never get here inside a Block!");
	}

	@Override
	public boolean visit(ContinueStatement node) {
		lastNode.push(null);
		return false;
	}

	
	@Override
	public boolean visit(MethodInvocation node) {
		CodeFragmentTreeNode parent = new CodeFragmentTreeNode();
		CodeFragmentLeaf currentFragment = new CodeFragmentLeaf();
		
		List<Expression> exprList = node.arguments();
		if (exprList != null){
			for (Expression e : exprList){
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
		CodeFragmentTreeNode parent = new CodeFragmentTreeNode();
		
		List<CatchClause> catchList = tryStatement.catchClauses();
		if (catchList != null){
			for (CatchClause c : catchList){
				parent.addInternalStatement(c);
			}
		}

		tryStatement.getBody().accept(this);
		CodeFragmentTreeNode bodyTry = lastNode.pop();
		if (bodyTry != null){
			parent.addChild(bodyTry);
		}else{
			CodeFragmentTreeNode tryNode = new CodeFragmentTreeNode();
			CodeFragmentLeaf simpleTryStatement = new CodeFragmentLeaf();
			simpleTryStatement.addStatement(tryStatement.getBody());
			tryNode.addChild(simpleTryStatement);
			parent.addChild(tryNode);
		}
		
		if(tryStatement.getFinally() != null){
			tryStatement.getFinally().accept(this);
			CodeFragmentTreeNode bodyFinally = lastNode.pop();
			if (bodyFinally != null){
				parent.addChild(bodyFinally);
			}else{
				CodeFragmentTreeNode finallyNode = new CodeFragmentTreeNode();
				CodeFragmentLeaf simpleFinallyStatement = new CodeFragmentLeaf();
				simpleFinallyStatement.addStatement(tryStatement.getFinally());
				finallyNode.addChild(simpleFinallyStatement);
				parent.addChild(finallyNode);
			}
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
		CodeFragmentTreeNode parent = new CodeFragmentTreeNode();
		CodeFragmentLeaf currentExpressionFragment = new CodeFragmentLeaf();
		
		Expression expr = switchCase.getExpression();
		if (expr != null){
			currentExpressionFragment.addStatement(expr);
			parent.addChild(currentExpressionFragment);
			lastNode.push(parent);
		}else{
			lastNode.push(null);
		}
		
		return false;
	}

	
	@Override
	public boolean visit(SwitchStatement switchStatement) {
		CodeFragmentTreeNode parent = new CodeFragmentTreeNode();
		
		Expression expr = switchStatement.getExpression();
		if (expr != null){
			parent.addInternalStatement(expr);
		}
		
		List<Statement> switchStatements = switchStatement.statements();
		CodeFragmentTreeNode nodeStatements = new CodeFragmentTreeNode();
		CodeFragmentLeaf allSwitchStatements = new CodeFragmentLeaf();
		for(int i=0; i< switchStatements.size(); i++){
			switchStatements.get(i).accept(this);
			
			CodeFragmentTreeNode res = lastNode.pop();
			if(res == null){
				allSwitchStatements.addStatement(switchStatements.get(i));
			}else{
				parent.addChild(res);
			}
		}
		
		if (allSwitchStatements.getChildrenSize() != 0){
			nodeStatements.addChild(allSwitchStatements);
			parent.addChild(nodeStatements);
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
}
