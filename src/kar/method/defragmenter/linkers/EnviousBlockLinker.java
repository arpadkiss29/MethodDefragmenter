package kar.method.defragmenter.linkers;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;

import kar.method.defragmenter.utils.CodeFragmentTreeNode;
import kar.method.defragmenter.visittors.MethodInvocationVisitor;
import kar.method.defragmenter.visittors.VariableBindingVisitor;

public class EnviousBlockLinker implements IBlockLinker{
	
	private int ATFDTreshold;
	private int FDPTreshold;
	
	private String analyzedClass;
	
	private HashMap<String, Integer> accessClassesMapping = new HashMap<String, Integer>();;
	private int accessForeignData    = 0;
	private int localAttrAccess      = 0;
	private int foreignDataProviders = 0;
	private String targetClass;
	
	public EnviousBlockLinker(String analyzedClass, int ATFDTreshold, int FDPTreshold) {
		this.ATFDTreshold = ATFDTreshold;
		this.FDPTreshold = FDPTreshold;
		this.analyzedClass = analyzedClass;
	}
	

	@Override
	public boolean tryToLinkBlocks(CodeFragmentTreeNode node) {
		List<ASTNode> allSubTreeNodes = node.getAllSubTreeASTNodes();
		calculateDataAccesses(allSubTreeNodes);
		boolean enviousNode = verifyFeatureEnvy();
		if(enviousNode){
			node.setNodeAccessForeignData(accessForeignData);
			node.setNodeLocalAttrAccess(localAttrAccess);
			node.setNodeForeignDataProviders(foreignDataProviders);
			node.setNodeTargetClass(targetClass);
			node.setEnviousNode(true);
			node.calculteFirstLastLine();
		}

		cleanUpInternalData();
		return enviousNode;
	}
	
	
	private void calculateDataAccesses(List<ASTNode> allSubTreeNodes){
		
		for(ASTNode node:allSubTreeNodes){
			MethodInvocationVisitor invocationVisitor = new MethodInvocationVisitor();
			node.accept(invocationVisitor);
			List<MethodInvocation> methodInvocations = invocationVisitor.getMethodInvocations();
			for(MethodInvocation invocation: methodInvocations){
				if(invocation.getName().getFullyQualifiedName().startsWith("get")){
					IMethodBinding methodBinding = invocation.resolveMethodBinding();
					if(methodBinding.getParameterTypes().length == 0){
						if(invocation.getExpression() != null){
							incrementAccesses(analyzedClass,invocation.getExpression().resolveTypeBinding());
						}
					}
				}
			}
	
			VariableBindingVisitor variableVisitor = new VariableBindingVisitor();
			node.accept(variableVisitor);
			Set<IVariableBinding> variables = variableVisitor.getVariableBindings();
			for(IVariableBinding binding: variables){

				ITypeBinding typeBinding = binding.getDeclaringClass();
				if(typeBinding != null){
					if(!Modifier.isStatic(binding.getModifiers())){
						incrementAccesses(analyzedClass, typeBinding);
					}
				}
			}
		}
	}
	
	private void incrementAccesses(String analyzedClass, ITypeBinding accessClassBinding){
		if(checkLocalAccess(analyzedClass, accessClassBinding)){
			localAttrAccess++;
		}else{
			if(accessClassesMapping.get(accessClassBinding.getName()) != null){
				int nrAccesses = accessClassesMapping.get(accessClassBinding.getName());
				accessClassesMapping.put(accessClassBinding.getName(), nrAccesses + 1);
			}else{
				accessClassesMapping.put(accessClassBinding.getName(), 1);
			}
		}
	}
	
	
	private boolean verifyFeatureEnvy() {
		boolean isEnviousLeaf = false;
		for(Integer numberOfAcc: accessClassesMapping.values()){
			accessForeignData += numberOfAcc;
		}
		int totalAccesses = accessForeignData + localAttrAccess;
		foreignDataProviders = accessClassesMapping.size();
		
		if( accessForeignData >= ATFDTreshold &&
			//(localAttrAccess / totalAccesses)  < (1.0 / 3) &&
				(localAttrAccess > 0 ? (localAttrAccess * 1.0) / totalAccesses : 0) < (1.0 / 3) &&
				foreignDataProviders <= FDPTreshold){
			
//			System.out.println("for nodes : " + myASTNodes);
			System.out.println("accessForeignData : " + accessForeignData);
			System.out.println("foreignDataProviders : " + foreignDataProviders);
			System.out.println("localAttrAccess : " + localAttrAccess);
			System.out.println("totalAccesses: " + totalAccesses);
			System.out.println();
			
			String enviousClass  = "";
			int maxAccess = Integer.MIN_VALUE;
			for(Entry<String, Integer> accessedClassEntry: accessClassesMapping.entrySet()){
				if(accessedClassEntry.getValue() > maxAccess){
					maxAccess = accessedClassEntry.getValue();
					enviousClass = accessedClassEntry.getKey();
				}
			}
			targetClass = enviousClass;
			isEnviousLeaf = true;
		}else{
			accessForeignData    = 0;
			foreignDataProviders = 0;
			localAttrAccess      = 0;
		}
	
		return isEnviousLeaf;
	}
	
	private boolean checkLocalAccess(String analyzedClass, ITypeBinding accessClassBinding){
		if(accessClassBinding.getSuperclass() != null){
			if(accessClassBinding.getName().equals(analyzedClass)) return true;
			checkLocalAccess(analyzedClass, accessClassBinding.getSuperclass());
		}
		return false;	
	}
	
	private void cleanUpInternalData(){
		accessClassesMapping.clear();
		
		accessForeignData    = 0;
		foreignDataProviders = 0;
		localAttrAccess      = 0;
	}
	
	
}
