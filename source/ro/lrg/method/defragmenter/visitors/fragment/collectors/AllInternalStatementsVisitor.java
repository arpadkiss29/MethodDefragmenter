package ro.lrg.method.defragmenter.visitors.fragment.collectors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

import ro.lrg.method.defragmenter.utils.AbstractInternalCodeFragment;
import ro.lrg.method.defragmenter.utils.InternalCodeFragment;
import ro.lrg.method.defragmenter.utils.InternalCodeFragmentLeaf;
import ro.lrg.method.defragmenter.visitors.fragment.FragmentVisitor;

public class AllInternalStatementsVisitor implements FragmentVisitor {
	
	private List<ASTNode> allInternalStatements = new ArrayList<ASTNode>();

	@Override
	public void visit(InternalCodeFragment fragment) {
		List<AbstractInternalCodeFragment> children = fragment.getChildren();
		for (AbstractInternalCodeFragment child : children) {
			child.accept(this);
		}
		allInternalStatements.addAll(fragment.getInternalStatements());
	}

	@Override
	public void visit(InternalCodeFragmentLeaf fragment) {
		allInternalStatements.addAll(fragment.getInternalStatements());
	}
	
	public List<ASTNode> getAllInternalStatements() {
		return allInternalStatements;
	}
}
