package ro.lrg.method.defragmenter.utils;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;
import ro.lrg.method.defragmenter.visitors.fragment.FragmentVisitor;

public class InternalCodeFragment extends AbstractInternalCodeFragment {
	private final List<AbstractInternalCodeFragment> children = new ArrayList<>();
	
	public InternalCodeFragment(String analizedClass, IFile iFile, IJavaProject iJavaProject) {
		super(analizedClass, iFile, iJavaProject);
	}
	
	@Override
	public void accept(FragmentVisitor visitor) {
		visitor.visit(this);
	}
	
	public void addChild(AbstractInternalCodeFragment child) {
		if(child == null) {
			System.err.println("Found null child!");
			return;
		}
		children.add(child);
	}
	
	public AbstractInternalCodeFragment getChild(int i) {
		return children.get(i);
	}
	
	public List<AbstractInternalCodeFragment> getChildren() {
		return children;
	}
	
	public int getChildrenSize() {
		return children.size();
	}
}
