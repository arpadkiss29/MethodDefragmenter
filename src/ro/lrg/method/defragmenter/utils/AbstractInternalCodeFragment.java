package ro.lrg.method.defragmenter.utils;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jface.text.Position;
import ro.lrg.method.defragmenter.visitors.fragment.FragmentVisitor;
import ro.lrg.method.defragmenter.visitors.fragment.collectors.AllEnviousLeavesVisitor;
import ro.lrg.method.defragmenter.visitors.fragment.collectors.AllInternalStatementsVisitor;
import ro.lrg.method.defragmenter.visitors.fragment.collectors.AllLeavesVisitor;
import ro.lrg.method.defragmenter.visitors.fragment.collectors.AllNodesVisitor;

public abstract class AbstractInternalCodeFragment {
	private final String analizedClass;
	private final IFile iFile;
	private final IJavaProject iJavaProject;
	private final List<ASTNode> internalStatements;
	
	protected AbstractInternalCodeFragment(String analizedClass, IFile iFile, IJavaProject iJavaProject) {
		this.analizedClass = analizedClass;
		this.iFile = iFile;
		this.iJavaProject = iJavaProject;
		internalStatements = new ArrayList<>();
	}
	
	public abstract void accept(FragmentVisitor visitor);
	
	public List<AbstractInternalCodeFragment> getAllEnviousFragmentsOfTree(int ATFDTreshold, int FDPTreshold, double LAATreshold) {
		AllEnviousLeavesVisitor allEnviousFragmentsVisitor = new AllEnviousLeavesVisitor(ATFDTreshold, FDPTreshold, LAATreshold);
		this.accept(allEnviousFragmentsVisitor);
		return allEnviousFragmentsVisitor.getAllEnviousFragments();
	}
	
	public List<AbstractInternalCodeFragment> getAllLeavesOfTree() {
		AllLeavesVisitor allLeavesVisitor = new AllLeavesVisitor();
		this.accept(allLeavesVisitor);
		return allLeavesVisitor.getAllLeaves();
	}
	
	public List<AbstractInternalCodeFragment> getAllNodesOfTree() {
		AllNodesVisitor allNodesVisitor = new AllNodesVisitor();
		this.accept(allNodesVisitor);
		return allNodesVisitor.getAllNodes();
	}
	
	public List<ASTNode> getAllInternalStatementsOfTree() {
		AllInternalStatementsVisitor allInternalStatementsVisitor = new AllInternalStatementsVisitor();
		this.accept(allInternalStatementsVisitor);
		return allInternalStatementsVisitor.getAllInternalStatements();
	}
	
	public Position getPosition() {
		int start = this.getFragmentFirstLineStartIndex();
		int end = this.getFragmentLastLineEndIndex();
		Position position = new Position(start, (end - start));
		return position;
	}

	@Override
	public String toString() {
		return internalStatements.toString();
	}
	
	protected int getFragmentFirstLineStartIndex() {
		if (getInternalStatements().isEmpty()) return 1;
		return getInternalStatement(0).getStartPosition();
	}

	protected int getFragmentLastLineEndIndex() {
		if (getInternalStatements().isEmpty()) return 1;
		return getInternalStatement(getInternalStatementsSize() - 1).getStartPosition() + getInternalStatement(getInternalStatementsSize() - 1).getLength();
	}
	
	public void addInternalStatement(ASTNode statement) {
		if(statement == null) {
			System.err.println("Found null statement!");
			return;
		}
		internalStatements.add(statement);
	}
	
	public void addInternalStatements(List<ASTNode> statements) {
		if (statements == null) throw new NullPointerException();
		this.internalStatements.addAll(statements);
	}
	public ASTNode getInternalStatement(int index) {
		return internalStatements.get(index);
	}
	public List<ASTNode> getInternalStatements() {
		return internalStatements;
	}
	public int getInternalStatementsSize() {
		return internalStatements.size();
	}
	public void removeInternalStatement(ASTNode statement) {
		internalStatements.remove(statement);
	}
	
	public String getAnalizedClass() {
		return analizedClass;
	}
	public IFile getIFile() {
		return iFile;
	}
	public IJavaProject getIJavaProject() {
		return iJavaProject;
	}
}
