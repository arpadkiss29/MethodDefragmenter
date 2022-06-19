package ro.lrg.method.defragmenter.utils;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jface.text.Position;
import ro.lrg.method.defragmenter.visitors.fragment.FragmentVisitor;

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
	
	private int getFragmentFirstLineStartIndex() {
		if (getInternalStatements().isEmpty()) return 1;
		return getInternalStatement(0).getStartPosition();
	}

	private int getFragmentLastLineEndIndex() {
		if (getInternalStatements().isEmpty()) return 1;
		return getInternalStatement(getInternalStatementsSize() - 1).getStartPosition() + getInternalStatement(getInternalStatementsSize() - 1).getLength();
	}
	
	public void addInternalStatement(ASTNode statement) {
		if(statement == null) return;
		internalStatements.add(statement);
	}
	
	public void addInternalStatementsOfFragment(AbstractInternalCodeFragment fragment) {
		if (fragment == null || fragment.getInternalStatements() == null) throw new NullPointerException();
		internalStatements.addAll(fragment.getInternalStatements());
	}
	
	private ASTNode getInternalStatement(int index) {
		return internalStatements.get(index);
	}
	
	public List<ASTNode> getInternalStatements() {
		return internalStatements;
	}
	
	public int getInternalStatementsSize() {
		return internalStatements.size();
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
