package kar.method.defragmenter.visittors;

import java.util.HashSet;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;

public class SimpleNameVisitor extends ASTVisitor {
	
	private HashSet<IVariableBinding> variableNames = new HashSet<IVariableBinding>();
	
	public boolean visit(SimpleName node) {
	    IBinding binding = node.resolveBinding();
	    if (binding instanceof IVariableBinding) {
	        IVariableBinding variable = (IVariableBinding) binding;
	        variableNames.add(variable);
	    }
	    return super.visit(node);
	}
	
	public HashSet<IVariableBinding> getVariableNames(){
		return variableNames;
	}
}
