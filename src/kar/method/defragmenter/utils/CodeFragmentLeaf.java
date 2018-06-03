package kar.method.defragmenter.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.ITextEditor;

import kar.method.defragmenter.linkers.IBlockLinker;
import kar.method.defragmenter.views.SelectionView;
import kar.method.defragmenter.visittors.MethodInvocationVisitor;
import kar.method.defragmenter.visittors.VariableBindingVisitor;

public class CodeFragmentLeaf extends CodeFragmentTreeNode {

	private ArrayList<ASTNode> myASTNodes = new ArrayList<ASTNode>();

	private boolean isEnviousLeaf;

	// Feature envy
	private HashMap<String, Integer> accessClassesMapping = new HashMap<String, Integer>();;
	private int accessForeignData    = 0;
	private int localAttrAccess      = 0;
	private int foreignDataProviders = 0;
	private String targetClass;

	public CodeFragmentLeaf() {
	}


	public void addStatement(ASTNode node){	
		myASTNodes.add(node);
	}

	public void removeStatement(ASTNode node){
		myASTNodes.remove(node);
	}

	public void print(int tabs) {
		for(int i = 0; i < tabs+1; i++) System.out.print("\t");
		String statement = myASTNodes.toString();
		statement = statement.replace("\n", "");
		System.out.print(statement);
		System.out.println();
	}

	public HashSet<IVariableBinding> getMyVariables(){
		HashSet<IVariableBinding> vars = new HashSet<IVariableBinding>();
		VariableBindingVisitor visitorVariableName = new VariableBindingVisitor();
		for (ASTNode eachNode : myASTNodes){
			eachNode.accept(visitorVariableName);
			vars = visitorVariableName.getVariableBindings();
		}
		return vars;
	}

	public int getFragmentFirstLine(){
		if(!myASTNodes.isEmpty()){
			return myASTNodes.get(0).getStartPosition();
		}
		return 1;

	}

	public int getFragmentLastLine(){
		if(!myASTNodes.isEmpty()){
			return myASTNodes.get(myASTNodes.size() - 1).getStartPosition() + myASTNodes.get(myASTNodes.size() - 1).getLength(); 
		}
		return 1;
	}


	@Override
	public List<ASTNode> getAllSubTreeASTNodes() {
		return myASTNodes;
	}

	public CodeFragmentTreeNode getAllTreeData(){
		return this;
	}


	@Override
	public List<CodeFragmentTreeNode> identifyFunctionalSegments() {
		List<CodeFragmentTreeNode> temp = new ArrayList<CodeFragmentTreeNode>();
		temp.add(this);
		return temp;
	}

	@Override
	public void computeDataAccesses(String analyzedClass, boolean staticFields, Integer minBlockSize) {

		if(minBlockSize != null){
			//System.out.println("Applying size filter");
			if(myASTNodes.size() < minBlockSize) return;
		}

		HashSet<IVariableBinding> variableBindingsCache = new  HashSet<IVariableBinding>();
		HashSet<IMethodBinding> methodBindingCache = new HashSet<IMethodBinding>();

		for(ASTNode node:myASTNodes){
			MethodInvocationVisitor invocationVisitor = new MethodInvocationVisitor();
			node.accept(invocationVisitor);
			List<MethodInvocation> methodInvocations = invocationVisitor.getMethodInvocations();
			for(MethodInvocation invocation: methodInvocations){
				if(invocation.getName().getFullyQualifiedName().startsWith("get")){

					IMethodBinding methodBinding = invocation.resolveMethodBinding();
					if(!methodBindingCache.contains(methodBinding)){
						if(methodBinding.getParameterTypes().length == 0){
							if(invocation.getExpression() != null){
								incrementAccesses(analyzedClass,invocation.getExpression().resolveTypeBinding());
							}
						}
						methodBindingCache.add(methodBinding);
					}

				}
			}

			VariableBindingVisitor variableVisitor = new VariableBindingVisitor();
			node.accept(variableVisitor);
			Set<IVariableBinding> variables = variableVisitor.getVariableBindings();
			for(IVariableBinding binding: variables){
				if(!variableBindingsCache.contains(binding)){
					ITypeBinding typeBinding = binding.getDeclaringClass();
					if(typeBinding != null){
						boolean staticCheck = true; 
						if(!staticFields){
							if(Modifier.isStatic(binding.getModifiers())) staticCheck = false;
						}
						if(staticCheck){
							incrementAccesses(analyzedClass, typeBinding);
						}
					}
					variableBindingsCache.add(binding);
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


	private boolean checkLocalAccess(String analyzedClass, ITypeBinding accessClassBinding){
		if(accessClassBinding.getSuperclass() != null){
			//System.out.println("Checking: " + accessClassBinding.getName());
			if(accessClassBinding.getName().equals(analyzedClass)) return true;
			checkLocalAccess(analyzedClass, accessClassBinding.getSuperclass());
		}
		return false;
	}

	private boolean checkLocalHierarchyAccess(String analyzedClass, ITypeBinding accessClassBinding) {
		IType accessType = (IType)accessClassBinding.getJavaElement();
		try {
			ITypeHierarchy typeHierarchy = accessType.newTypeHierarchy(new NullProgressMonitor());

			long startTime = System.currentTimeMillis();
			IType[] superTypes = typeHierarchy.getSupertypes(accessType);
			long estimatedTime = System.currentTimeMillis() - startTime;
			//System.out.println("Get supertypes(ms): " + estimatedTime);

			startTime = System.currentTimeMillis();
			for(IType classType: superTypes){
				//System.out.println("Checking: " + classType.getElementName());
				if(classType.getElementName().equals(analyzedClass)) return true;
			}
			estimatedTime = System.currentTimeMillis() - startTime;
			//System.out.println("For supertypes(ms): " + estimatedTime);

			startTime = System.currentTimeMillis();
			IType[] subTypes = typeHierarchy.getSubtypes(accessType);
			estimatedTime = System.currentTimeMillis() - startTime;
			//System.out.println("Get subtypes(ms): " + estimatedTime);

			startTime = System.currentTimeMillis();
			for(IType classType: subTypes){
				//System.out.println("Checking: " + classType.getElementName());
				if(classType.getElementName().equals(analyzedClass)) return true;
			}
			estimatedTime = System.currentTimeMillis() - startTime;
			//System.out.println("For subtypes(ms): " + estimatedTime);

		} catch (JavaModelException e) {
			e.printStackTrace();
		} 
		return false;
	}


	@Override
	public boolean verifyFeatureEnvy(int ATFDTreshold, int FDPTreshold, boolean expand, List<IBlockLinker> matchers) {
		for(Integer numberOfAcc: accessClassesMapping.values()){
			accessForeignData += numberOfAcc;
		}
		int totalAccesses = accessForeignData + localAttrAccess;
		foreignDataProviders = accessClassesMapping.size();


		System.out.println("for nodes : " + myASTNodes);
		System.out.println("accessForeignData : " + accessForeignData);
		System.out.println("foreignDataProviders : " + foreignDataProviders);
		System.out.println("localAttrAccess : " + localAttrAccess);
		System.out.println("totalAccesses: " + totalAccesses);
		System.out.println();

		if( accessForeignData >= ATFDTreshold &&
				//(localAttrAccess / totalAccesses)  < (1.0 / 3) &&
				(localAttrAccess > 0 ? (localAttrAccess * 1.0) / totalAccesses : 0) < (1.0 / 3) &&
				foreignDataProviders <= FDPTreshold){

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


	@Override
	public void clearChildrenData() {
		accessClassesMapping.clear();

		accessForeignData    = 0;
		foreignDataProviders = 0;
		localAttrAccess      = 0;
	}

	@Override
	public void colorEnvyLeafNodes(ITextEditor textEditor, IFile file) throws CoreException {	
		if(isEnviousLeaf){
			String colorType = "annotationColor_17";

			if (colorCounter < 17){
				colorType = "annotationColor_" + colorCounter;
				colorCounter++;
			}

			int start = this.getFragmentFirstLine();
			int end = this.getFragmentLastLine();
			Position fragmentPosition = new Position(start, (end - start));
			IMarker mymarker = SelectionView.createMarker(file, fragmentPosition);
			SelectionView.addAnnotation(mymarker, textEditor, colorType, fragmentPosition);
		}
	}

	@Override
	public void colorLongMethodFragments(ITextEditor textEditor, IFile file,
			List<CodeFragmentTreeNode> functionalSegmentNodes) {

		if((functionalSegmentNodes.contains(this)) && (possiblyRelatedFlag != true)){
			try {
				if (colorCounter < 17){
					colorCounter++;	
					int start = this.getFragmentFirstLine();
					int end = this.getFragmentLastLine();
					Position fragmentPosition = new Position(start, (end - start));
					IMarker mymarker = SelectionView.createMarker(file, fragmentPosition);
					SelectionView.addAnnotation(mymarker, textEditor, "annotationColor_" + colorCounter, fragmentPosition);

					for(int i=0; i<cohesivlyRelatedNodes.size(); i++){
						if (cohesivlyRelatedNodes.get(i) instanceof CodeFragmentLeaf){
							int startPoss = ((CodeFragmentLeaf)cohesivlyRelatedNodes.get(i)).getFragmentFirstLine();
							int endPoss = ((CodeFragmentLeaf)cohesivlyRelatedNodes.get(i)).getFragmentLastLine();
							Position fragmentPositionPoss = new Position(startPoss, (endPoss - startPoss));
							IMarker mymarkerPoss = SelectionView.createMarker(file, fragmentPositionPoss);
							SelectionView.addAnnotation(mymarkerPoss, textEditor, "annotationColor_" + colorCounter, fragmentPositionPoss);
						}
					}

				}else{
					int start = this.getFragmentFirstLine();
					int end = this.getFragmentLastLine();
					Position fragmentPosition = new Position(start, (end - start));
					IMarker mymarker = SelectionView.createMarker(file, fragmentPosition);
					SelectionView.addAnnotation(mymarker, textEditor, "annotationColor_17", fragmentPosition);
				}

			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}


	public boolean isEnvy() {
		return isEnviousLeaf;
	}

	public int getAccessForeignData() {
		return accessForeignData;
	}

	public int getForeignDataProviders() {
		return foreignDataProviders;
	}

	public int getLocalAttrAccess() {
		return localAttrAccess;
	}

	public String getTargetClass() {
		return targetClass;
	}

	public HashMap<String, Integer> getAccessClassesMapping() {
		return accessClassesMapping;
	}
}
