package ro.lrg.method.defragmenter.utils;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.ui.texteditor.ITextEditor;

import ro.lrg.method.defragmenter.preferences.MethodDefragmenterPropertyStore;
import ro.lrg.method.defragmenter.visitors.fragment.AllEnviousFragmentsVisitor;
import ro.lrg.method.defragmenter.visitors.fragment.AllInternalStatementsVisitor;
import ro.lrg.method.defragmenter.visitors.fragment.AllLeavesVisitor;
import ro.lrg.method.defragmenter.visitors.fragment.FragmentVisitor;

public abstract class AbstractInternalCodeFragment {
	private final String analizedClass;
	private final IFile iFile;
	private final IJavaProject iJavaProject;
	private final List<ASTNode> internalStatements = new ArrayList<ASTNode>();
	protected int startPosition = 0;
	protected int endPosition = 0;
	
	protected static int colorCounter = 0;
	
	protected boolean possiblyRelatedFlag = false;
	protected List<AbstractInternalCodeFragment> cohesivlyRelatedNodes = new ArrayList<AbstractInternalCodeFragment>();
	
	protected AbstractInternalCodeFragment(String analizedClass, IFile iFile, IJavaProject iJavaProject) {
		this.analizedClass = analizedClass;
		this.iFile = iFile;
		this.iJavaProject = iJavaProject;
	}
	
	public List<AbstractInternalCodeFragment> getAllEnviousFragmentsOfTree() {
		MethodDefragmenterPropertyStore propertyStore = new MethodDefragmenterPropertyStore(iJavaProject);
		AllEnviousFragmentsVisitor allEnviousFragmentsVisitor = new AllEnviousFragmentsVisitor(propertyStore.getATFDTreshold(), 
				propertyStore.getFDPTreshold(), propertyStore.getLAATreshold(), propertyStore.isConsiderStaticFieldAccesses(),
				propertyStore.isLibraryCheck(), propertyStore.getMinBlockSize());
		this.accept(allEnviousFragmentsVisitor);
		return allEnviousFragmentsVisitor.getAllEnviousFragments();
	}
	
	public List<AbstractInternalCodeFragment> getAllLeavesOfTree() {
		AllLeavesVisitor allLeavesVisitor = new AllLeavesVisitor();
		this.accept(allLeavesVisitor);
		return allLeavesVisitor.getAllLeaves();
	}
	
	public List<ASTNode> getAllInternalStatementsOfTree() {
		AllInternalStatementsVisitor visitor = new AllInternalStatementsVisitor();
		this.accept(visitor);
		return visitor.getAllInternalStatements();
	}

	@Override
	public String toString() {
		return internalStatements.toString();
	}
	
	//--------------------------------------------------------------------------protected methods
	
	protected void initAux() {
		colorCounter = 0;
		possiblyRelatedFlag = false;
		cohesivlyRelatedNodes.clear();
	}
	
	//--------------------------------------------------------------------------abstract methods
	public abstract void accept(FragmentVisitor visitor);
	public abstract void colorFragment(ITextEditor textEditor, IFile file) throws CoreException;
	public abstract int getFragmentFirstLineStartIndex();
	public abstract int getFragmentLastLineEndIndex();
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
	public static void setColorCounter(int colorCounter) {
		AbstractInternalCodeFragment.colorCounter = colorCounter;
	}
	public int getEndPosition() {
		return endPosition;
	}
	public IFile getIFile() {
		return iFile;
	}
	public IJavaProject getIJavaProject() {
		return iJavaProject;
	}
	public int getStartPosition() {
		return startPosition;
	}
	public void setEndPosition(int endPosition) {
		this.endPosition = endPosition;
	}	
	public void setStartPosition(int startPosition) {
		this.startPosition = startPosition;
	}	
	
	//--------------------------------------------------------------------------NCOCP2
	//--------------------------------------------------------------------------NCOCP2: abstract methods
	public abstract void colorLongMethodFragments(ITextEditor textEditor, IFile file, List<AbstractInternalCodeFragment> functionalSegmentNodes);
	public abstract AbstractInternalCodeFragment constructTree();
	public abstract List<AbstractInternalCodeFragment> identifyFunctionalSegments();
	//--------------------------------------------------------------------------NCOCP2: getters and setters
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
