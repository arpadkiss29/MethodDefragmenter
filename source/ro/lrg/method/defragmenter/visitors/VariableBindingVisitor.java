package ro.lrg.method.defragmenter.visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;

public class VariableBindingVisitor extends ASTVisitor {

	private List<IVariableBinding> variableBindings = new ArrayList<>();

	public boolean visit(SimpleName node) {
		IBinding binding = node.resolveBinding();
		if (binding instanceof IVariableBinding) {
			IVariableBinding variable = (IVariableBinding) binding;
			if (!variableBindings.contains(variable)) {
				variableBindings.add(variable);
			}
		}
		return super.visit(node);
	}

	public List<IVariableBinding> getVariableBindings() {
		return variableBindings;
	}
}
