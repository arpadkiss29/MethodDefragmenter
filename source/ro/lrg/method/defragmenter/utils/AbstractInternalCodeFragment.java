package ro.lrg.method.defragmenter.utils;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.ITextEditor;

import ro.lrg.method.defragmenter.visitors.fragment.FragmentVisitor;
import ro.lrg.method.defragmenter.visitors.fragment.collectors.AllEnviousLeavesVisitor;
import ro.lrg.method.defragmenter.visitors.fragment.collectors.AllInternalStatementsVisitor;
import ro.lrg.method.defragmenter.visitors.fragment.collectors.AllLeavesVisitor;
import ro.lrg.method.defragmenter.visitors.fragment.collectors.AllNodesVisitor;
import ro.lrg.method.defragmenter.visitors.fragment.collectors.IdentifyFunctionalSegmentsVisitor;

public abstract class AbstractInternalCodeFragment {
	private final String analizedClass;
	private final IFile iFile;
	private final IJavaProject iJavaProject;
	private final List<ASTNode> internalStatements = new ArrayList<>();
	protected int startPosition = 0;
	protected int endPosition = 0;
	
	protected boolean possiblyRelatedFlag = false;
	protected List<AbstractInternalCodeFragment> cohesivlyRelatedNodes = new ArrayList<>();
	
	protected AbstractInternalCodeFragment(String analizedClass, IFile iFile, IJavaProject iJavaProject) {
		this.analizedClass = analizedClass;
		this.iFile = iFile;
		this.iJavaProject = iJavaProject;
	}
	
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
	
	//--------------------------------------------------------------------------protected methods
	
	protected void initAux() {
		possiblyRelatedFlag = false;
		cohesivlyRelatedNodes.clear();
	}
	
	protected int getFragmentFirstLineStartIndex() {
		if (getInternalStatements().isEmpty()) return 1;
		return getInternalStatement(0).getStartPosition();
	}

	protected int getFragmentLastLineEndIndex() {
		if (getInternalStatements().isEmpty()) return 1;
		return getInternalStatement(getInternalStatementsSize() - 1).getStartPosition() + getInternalStatement(getInternalStatementsSize() - 1).getLength();
	}
	
	//--------------------------------------------------------------------------abstract methods
	public abstract void accept(FragmentVisitor visitor);
	public abstract void init();
	public abstract void print(int tabs);
	
	//--------------------------------------------------------------------------internal statements related methods
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
	
	//--------------------------------------------------------------------------getters and setters
	public String getAnalizedClass() {
		return analizedClass;
	}
	public IFile getIFile() {
		return iFile;
	}
	public IJavaProject getIJavaProject() {
		return iJavaProject;
	}
	//--------------------------------------------------------------------------NCOCP2
	public abstract void colorLongMethodFragments(ITextEditor textEditor, IFile file, List<AbstractInternalCodeFragment> functionalSegmentNodes);
	
	public List<AbstractInternalCodeFragment> identifyFunctionalSegments(double NCOCP2Treshold) {
		IdentifyFunctionalSegmentsVisitor identifyFunctionalSegmentsVisitor = new IdentifyFunctionalSegmentsVisitor(NCOCP2Treshold);
		this.accept(identifyFunctionalSegmentsVisitor);
		return identifyFunctionalSegmentsVisitor.getFunctionalSegments();
	}

	public List<AbstractInternalCodeFragment> getPossiblyRelatedNodes() {
		return cohesivlyRelatedNodes;
	}
	public boolean isPossiblyRelatedFlag() {
		return possiblyRelatedFlag;
	}
	public void setPossiblyRelatedFlag(boolean possiblyRelatedFlag) {
		this.possiblyRelatedFlag = possiblyRelatedFlag;
	}
}
