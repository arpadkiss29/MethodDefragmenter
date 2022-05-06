package ro.lrg.method.defragmenter.utils;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IVariableBinding;
import ro.lrg.method.defragmenter.visitors.ast.VariableBindingVisitor;
import ro.lrg.method.defragmenter.visitors.fragment.FragmentVisitor;

public class InternalCodeFragmentLeaf extends AbstractInternalCodeFragment {
	
	public InternalCodeFragmentLeaf(String analizedClass, IFile iFile, IJavaProject iJavaProject) {
		super(analizedClass, iFile, iJavaProject);
	}
	
	@Override
	public void accept(FragmentVisitor visitor) {
		visitor.visit(this);
	}
	
	public List<IVariableBinding> getVariablesBindings() {
		VariableBindingVisitor variableBindingVisitor = new VariableBindingVisitor();
		for (ASTNode statement : getInternalStatements()) {
			statement.accept(variableBindingVisitor);
		}
		List<IVariableBinding> variablesBindings = new ArrayList<>();
		variablesBindings.addAll(variableBindingVisitor.getVariableBindings());
		return variablesBindings;
	}
}
