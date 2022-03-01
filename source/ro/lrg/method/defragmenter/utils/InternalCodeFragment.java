package ro.lrg.method.defragmenter.utils;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;

public class InternalCodeFragment extends AbstractInternalCodeFragment {
	
	public InternalCodeFragment(IFile iFile, IJavaProject iJavaProject) {
		super(iFile, iJavaProject);
	}

	public boolean removeChildren(List<AbstractInternalCodeFragment> childElements) {
		return children.removeAll(childElements);
	}

	public void addChild(int index, AbstractInternalCodeFragment child) {
		children.add(index, child);
	}

	@Override
	public void addChild(AbstractInternalCodeFragment child) {
		children.add(child);
	}

	public int removeChild(AbstractInternalCodeFragment child) {
		int ind = children.indexOf(child);
		children.remove(child);
		return ind;
	}
}
