package kar.method.defragmenter.visittors;

import java.util.HashSet;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;

public class VariableBindingVisitor extends ASTVisitor {
	
	private HashSet<IVariableBinding> variableBindings = new HashSet<IVariableBinding>();

	
	public boolean visit(SimpleName node) {
	    IBinding binding = node.resolveBinding();
	    if (binding instanceof IVariableBinding) {
	        IVariableBinding variable = (IVariableBinding) binding;
	        if(!variableBindings.contains(variable)){	
	        	variableBindings.add(variable);
	        }else{
	        	System.out.println("already contains");
	        }
	       
	    }
	    return super.visit(node);
	}
	
	public HashSet<IVariableBinding> getVariableBindings(){
		return variableBindings;
	}
}
