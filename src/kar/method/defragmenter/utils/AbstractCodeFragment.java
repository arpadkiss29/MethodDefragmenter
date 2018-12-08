package kar.method.defragmenter.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.ITextEditor;

import kar.method.defragmenter.views.SelectionView;
import kar.method.defragmenter.visittors.MethodInvocationVisitor;
import kar.method.defragmenter.visittors.VariableBindingVisitor;

public abstract class AbstractCodeFragment {

	public static double NCOCP2Treshold = 0.75;
	// public static final double NCOCP2TresholdIdentif = 0.5;
	public static int colorCounter = 0;

	public static List<AbstractCodeFragment> allNodesLeafs = new ArrayList<AbstractCodeFragment>();
	protected List<AbstractCodeFragment> children = null;
	private double nodeNCOCP2 = 0.0;
	private double nodeCOCP = 0.0;
	private int startNode = 0;
	private int endNode = 0;
	private List<CodeFragmentLeaf> leafsReceived = new ArrayList<CodeFragmentLeaf>();

	protected boolean possiblyRelatedFlag = false;
	protected List<AbstractCodeFragment> cohesivlyRelatedNodes = new ArrayList<AbstractCodeFragment>();

	private List<ASTNode> internalASTNodes = new ArrayList<ASTNode>();

	private FixedStructureTypes type;

	public void setType(FixedStructureTypes type) {
		this.type = type;
	}

	public FixedStructureTypes getType() {
		return type;
	}

	public AbstractCodeFragment() {
		this.children = new ArrayList<>();
	}

	public void addInternalStatement(ASTNode node) {
		internalASTNodes.add(node);
	}

	public void removeInternalStatement(ASTNode node) {
		internalASTNodes.remove(node);
	}

	public int getChildrenSize() {
		return children.size();
	}

	public AbstractCodeFragment getChild(int i) {
		return children.get(i);
	}

	public List<AbstractCodeFragment> getChildren() {
		return children;
	}

	public void init() {
		colorCounter = 0;
		allNodesLeafs.clear();
		nodeNCOCP2 = 0;
		nodeCOCP = 0;
		leafsReceived.clear();
		possiblyRelatedFlag = false;
		cohesivlyRelatedNodes.clear();
	}

	private double calculateMetric(List<CodeFragmentLeaf> fragments) {
		Map<IVariableBinding, Integer> variableOccuarenceInSet = new HashMap<IVariableBinding, Integer>();
		double cocp = 0;
		double ncopc2 = 0.0;
		int numberOfFragemts = fragments.size();
		for (int i = 0; i < fragments.size(); i++) {
			HashSet<IVariableBinding> varFromSet = fragments.get(i).getMyVariables();
			for (IVariableBinding each : varFromSet) {
				if (variableOccuarenceInSet.get(each) == null) {
					variableOccuarenceInSet.put(each, 1);
				} else {
					variableOccuarenceInSet.put(each, variableOccuarenceInSet.get(each) + 1);
				}
			}
		}

		int sum = 0;
		int varNumber = variableOccuarenceInSet.entrySet().size();
		for (Map.Entry<IVariableBinding, Integer> entry : variableOccuarenceInSet.entrySet()) {
			sum += entry.getValue();
		}
		cocp = (((double) 1 / numberOfFragemts) * ((double) 1 / varNumber) * sum);
		ncopc2 = ((numberOfFragemts - ((double) 1 / cocp)) / (numberOfFragemts - 1));
		this.nodeCOCP = cocp;

		return ncopc2;

	}

	public List<CodeFragmentLeaf> getCohesionMetric(CompilationUnit unit) {
		// List<CodeFragmentLeaf> receivedNodes = new ArrayList<CodeFragmentLeaf>();

		for (int i = 0; i < children.size(); i++) {
			if (!(children.get(i) instanceof CodeFragmentLeaf)) {
				leafsReceived.addAll(children.get(i).getCohesionMetric(unit));

			}
		}

		int numberOfFragemts = 0;
		numberOfFragemts = leafsReceived.size();
		// if (this.leafsReceived == null){
		// this.leafsReceived = new ArrayList<CodeFragmentLeaf>();
		// }else{
		// numberOfFragemts = this.leafsReceived.size();
		// }

		List<CodeFragmentLeaf> temp = new ArrayList<CodeFragmentLeaf>();
		temp.addAll(leafsReceived);

		for (int i = 0; i < children.size(); i++) {
			if (children.get(i) instanceof CodeFragmentLeaf) {
				temp.add((CodeFragmentLeaf) children.get(i));
				numberOfFragemts++;
			}
		}

		if (numberOfFragemts > 1) {
			this.nodeNCOCP2 = calculateMetric(temp);

			calculteFirstLastLine();

			System.out.println(
					"Currently in node: " + toString() + " COCP: " + this.nodeCOCP + " NCOCP2: " + this.nodeNCOCP2);
			System.out.println(
					"From line : " + (unit.getLineNumber(startNode)) + " to line: " + (unit.getLineNumber(endNode)));
			System.out.println();

		}
		return temp;
	}

	public void calculteFirstLastLine() {
		if (children.size() > 0 && startNode == 0 && endNode == 0) {
			startNode = calculateFirstLine();
			endNode = calculateLastLine();
		} else {
			System.out.println("Already calculated!");
			System.out.println(this);
		}

		if (((startNode == 0) || (endNode == 0)) && leafsReceived.size() > 1) {
			if (startNode == 0 && endNode == 0) {
				startNode = children.get(0).getStartNode();
				endNode = children.get(children.size() - 1).getEndNode();
			} else if (startNode == 0) {
				startNode = leafsReceived.get(0).getFragmentFirstLine();
			} else if (endNode == 0) {
				endNode = leafsReceived.get(leafsReceived.size() - 1).getFragmentLastLine();
			}

		}

	}

	private int calculateFirstLine() {
		AbstractCodeFragment firstChild = children.get(0);
		if (firstChild instanceof CodeFragmentLeaf) {
			return ((CodeFragmentLeaf) firstChild).getFragmentFirstLine();
		} else {
			int tmp = firstChild.calculateFirstLine();
			if (startNode != 0 && startNode < tmp)
				tmp = startNode;
			if (internalASTNodes.size() > 0) {
				int tmp1 = internalASTNodes.get(0).getStartPosition();
//				if(internalASTNodes.get(0) instanceof Expression){
//					tmp1 -= 4;
//				}
				if (tmp1 < tmp)
					tmp = tmp1;
			}
			return tmp;
		}
	}

	private int calculateLastLine() {
		AbstractCodeFragment lastChild = children.get(children.size() - 1);
		if (lastChild instanceof CodeFragmentLeaf) {
			return ((CodeFragmentLeaf) lastChild).getFragmentLastLine();
		} else {
			int tmp = lastChild.calculateLastLine();
			if (endNode != 0 && endNode > tmp)
				tmp = endNode;
			if (internalASTNodes.size() > 0) {
				int tmp1 = internalASTNodes.get(internalASTNodes.size() - 1).getStartPosition()
						+ internalASTNodes.get(internalASTNodes.size() - 1).getLength();

				if (tmp1 > tmp)
					tmp = tmp1;
			}
//			if(type != null && type.equals(FixedStructureTypes.IF)){
//				//System.out.println("IF");
//				tmp += 11;
//			}
			return tmp;
		}
	}

	public List<AbstractCodeFragment> identifyFunctionalSegments() {
		List<AbstractCodeFragment> receivedNodes = new ArrayList<AbstractCodeFragment>();
		for (int i = 0; i < children.size(); i++) {
			receivedNodes.addAll(children.get(i).identifyFunctionalSegments());
		}
		if (this.nodeNCOCP2 >= NCOCP2Treshold) {
			receivedNodes.clear();
			receivedNodes.add(this);
			return receivedNodes;
		}
		return receivedNodes;
	}

	public boolean isPossiblyRelatedFlag() {
		return possiblyRelatedFlag;
	}

	public void setPossiblyRelatedFlag(boolean possiblyRelatedFlag) {
		this.possiblyRelatedFlag = possiblyRelatedFlag;
	}

	public void combineNodes(List<AbstractCodeFragment> nodes) {
		for (int i = 0; i < nodes.size() - 1; i++) {
			for (int j = i + 1; j < nodes.size(); j++) {
				List<CodeFragmentLeaf> fragmentsCombination = new ArrayList<CodeFragmentLeaf>();

				if (nodes.get(i) instanceof CodeFragmentLeaf) {
					fragmentsCombination.add((CodeFragmentLeaf) nodes.get(i));
				} else {
					fragmentsCombination.addAll(nodes.get(i).getLeafsReceived());
					for (AbstractCodeFragment eachNode : nodes.get(i).children) {
						if (eachNode instanceof CodeFragmentLeaf)
							fragmentsCombination.add((CodeFragmentLeaf) eachNode);
					}
				}

				if (nodes.get(j) instanceof CodeFragmentLeaf) {
					fragmentsCombination.add((CodeFragmentLeaf) nodes.get(j));
				} else {
					fragmentsCombination.addAll(nodes.get(j).getLeafsReceived());
					for (AbstractCodeFragment eachNode : nodes.get(j).children) {
						if (eachNode instanceof CodeFragmentLeaf)
							fragmentsCombination.add((CodeFragmentLeaf) eachNode);
					}
				}

				double combinationNCOPC2 = calculateMetric(fragmentsCombination);
				if (combinationNCOPC2 > NCOCP2Treshold) {
					if (nodes.get(i) instanceof CodeFragmentLeaf && nodes.get(j) instanceof CodeFragmentLeaf) {
						nodes.get(i).getPossiblyRelatedNodes().add(nodes.get(j));
						nodes.get(j).setPossiblyRelatedFlag(true);
					} else if (!(nodes.get(i) instanceof CodeFragmentLeaf)) {
						nodes.get(j).setPossiblyRelatedFlag(true);
						nodes.get(i).getPossiblyRelatedNodes().add(nodes.get(j));
					} else if (!(nodes.get(j) instanceof CodeFragmentLeaf)) {
						nodes.get(i).setPossiblyRelatedFlag(true);
						nodes.get(j).getPossiblyRelatedNodes().add(nodes.get(i));
					}
				}
			}
		}
	}

	public List<AbstractCodeFragment> getAllEnviousNodes() {
		List<AbstractCodeFragment> nodes = new ArrayList<AbstractCodeFragment>();
		for (AbstractCodeFragment node : children) {
			if (node instanceof CodeFragmentLeaf /* && ( (CodeFragmentLeaf)node).isEnvy() */) {
				nodes.add(node);
			} else {
				if (node.isEnvy()) {
					nodes.add(node);
				} else {
					nodes.addAll(node.getAllEnviousNodes());
				}
			}
		}
		return nodes;
	}

	public void colorLongMethodFragments(ITextEditor textEditor, IFile file,
			List<AbstractCodeFragment> functionalSegmentNodes) {
		for (int i = 0; i < children.size(); i++) {
			children.get(i).colorLongMethodFragments(textEditor, file, functionalSegmentNodes);
		}

		if (functionalSegmentNodes.contains(this) && (possiblyRelatedFlag != true)) {
			try {
				if (colorCounter < 17) {
					colorCounter++;
					for (int i = 0; i < children.size(); i++) {
						if (children.get(i) instanceof CodeFragmentLeaf) {
							int start = ((CodeFragmentLeaf) children.get(i)).getFragmentFirstLine();
							int end = ((CodeFragmentLeaf) children.get(i)).getFragmentLastLine();
							Position fragmentPosition = new Position(start, (end - start));
							IMarker mymarker = SelectionView.createMarker(file, fragmentPosition);
							SelectionView.addAnnotation(mymarker, textEditor, "annotationColor_" + colorCounter,
									fragmentPosition);
						}
					}
					for (int i = 0; i < leafsReceived.size(); i++) {
						int start = ((CodeFragmentLeaf) leafsReceived.get(i)).getFragmentFirstLine();
						int end = ((CodeFragmentLeaf) leafsReceived.get(i)).getFragmentLastLine();
						Position fragmentPosition = new Position(start, (end - start));
						IMarker mymarker = SelectionView.createMarker(file, fragmentPosition);
						SelectionView.addAnnotation(mymarker, textEditor, "annotationColor_" + colorCounter,
								fragmentPosition);

					}
					for (int i = 0; i < cohesivlyRelatedNodes.size(); i++) {
						if (cohesivlyRelatedNodes.get(i) instanceof CodeFragmentLeaf) {
							int start = ((CodeFragmentLeaf) cohesivlyRelatedNodes.get(i)).getFragmentFirstLine();
							int end = ((CodeFragmentLeaf) cohesivlyRelatedNodes.get(i)).getFragmentLastLine();
							Position fragmentPosition = new Position(start, (end - start));
							IMarker mymarker = SelectionView.createMarker(file, fragmentPosition);
							SelectionView.addAnnotation(mymarker, textEditor, "annotationColor_" + colorCounter,
									fragmentPosition);
						}
					}

				} else {
					for (int i = 0; i < children.size(); i++) {
						if (children.get(i) instanceof CodeFragmentLeaf) {
							int start = ((CodeFragmentLeaf) children.get(i)).getFragmentFirstLine();
							int end = ((CodeFragmentLeaf) children.get(i)).getFragmentLastLine();
							Position fragmentPosition = new Position(start, (end - start));
							IMarker mymarker = SelectionView.createMarker(file, fragmentPosition);
							SelectionView.addAnnotation(mymarker, textEditor, "annotationColor_17", fragmentPosition);
						}
					}
				}

			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

	}

	public void print(int tabs) {
		for (int j = 0; j < tabs; j++)
			System.out.print("\t");
		System.out.println(this.toString().replaceAll("\n", ""));
		for (int i = 0; i < children.size(); i++) {
			children.get(i).print(tabs + 1);
		}
	}

	public AbstractCodeFragment getAllTreeData() {
		for (int i = 0; i < children.size(); i++) {
			allNodesLeafs.add(children.get(i).getAllTreeData());
		}
		return this;
	}

	public List<ASTNode> getAllSubTreeASTNodes() {
		List<ASTNode> nodes = new ArrayList<ASTNode>();
		for (int i = 0; i < children.size(); i++) {
			nodes.addAll(children.get(i).getAllSubTreeASTNodes());
		}
		nodes.addAll(internalASTNodes);
		return nodes;
	}

	public double getNodeNCOCP2() {
		return nodeNCOCP2;
	}

	public double getNodeCOCP() {
		return nodeCOCP;
	}

	public void setNodeNCOCP2(double nodeNCOCP2) {
		this.nodeNCOCP2 = nodeNCOCP2;
	}

	public List<CodeFragmentLeaf> getLeafsReceived() {
		return leafsReceived;
	}

	public void setLeafsReceived(List<CodeFragmentLeaf> leafsReceived) {
		this.leafsReceived = leafsReceived;
	}

	public int getStartNode() {
		return startNode;
	}

	public void setStartNode(int startNode) {
		this.startNode = startNode;
	}

	public int getEndNode() {
		return endNode;
	}

	public void setEndNode(int endNode) {
		this.endNode = endNode;
	}

	public List<AbstractCodeFragment> getPossiblyRelatedNodes() {
		return cohesivlyRelatedNodes;
	}

	// Feature envy
	private boolean isEnvy;
	private HashMap<String, Integer> accessClassesMapping = new HashMap<String, Integer>();;
	private int accessForeignData = 0;
	private int localAttrAccess = 0;
	private int foreignDataProviders = 0;
	private String targetClass;
	private HashMap<String, Integer> storedFDP=null;
	
	public HashMap<String, Integer> getStoredFDP() {
		return storedFDP;
	}

	public void setStoredFDP(HashMap<String, Integer> storedFDP) {
		this.storedFDP = storedFDP;
	}

	public int getFdpSize(String analyzedClass, boolean staticFields, Integer minBlockSize, boolean libraryCheck) {
		computeDataAccesses(analyzedClass, staticFields, minBlockSize, libraryCheck);
		foreignDataProviders = accessClassesMapping.size();
		return foreignDataProviders;
	}

	public HashMap<String, Integer> getFdp(String analyzedClass, boolean staticFields, Integer minBlockSize,
			boolean libraryCheck) {
		computeDataAccesses(analyzedClass, staticFields, minBlockSize, libraryCheck);
		storedFDP=accessClassesMapping;
		return accessClassesMapping;
	}

	public boolean verifyFeatureEnvy(int ATFDTreshold, int FDPTreshold, String analyzedClass, boolean staticFields,
			Integer minBlockSize, boolean libraryCheck, boolean local) {
		if (local) {
			isEnvy = false;
			accessClassesMapping.clear();
			accessForeignData = 0;
			foreignDataProviders = 0;
			localAttrAccess = 0;
			targetClass = "";

			computeDataAccesses(analyzedClass, staticFields, minBlockSize, libraryCheck);
			for (Integer numberOfAcc : accessClassesMapping.values()) {
				accessForeignData += numberOfAcc;
			}
			int totalAccesses = accessForeignData + localAttrAccess;
			foreignDataProviders = accessClassesMapping.size();

			if (accessForeignData > ATFDTreshold
					&& (localAttrAccess > 0 ? (localAttrAccess * 1.0) / totalAccesses : 0) < (1.0 / 3)
					&& foreignDataProviders <= FDPTreshold) {

				String enviousClass = "";
				int maxAccess = Integer.MIN_VALUE;
				if (accessClassesMapping.entrySet().size() == 1) {
					for (Entry<String, Integer> accessedClassEntry : accessClassesMapping.entrySet()) {
						if (accessedClassEntry.getValue() > maxAccess) {
							maxAccess = accessedClassEntry.getValue();
							enviousClass = accessedClassEntry.getKey();
						}
					}
				} else {
					int count = 0;
					for (Entry<String, Integer> accessedClassEntry : accessClassesMapping.entrySet()) {
						if (count > 0)
							enviousClass += ";";
						enviousClass += accessedClassEntry.getKey() + " - " + accessedClassEntry.getValue();
						count++;
					}
				}

				targetClass = enviousClass;
				isEnvy = true;
			}

			return isEnvy;

		} else {
			boolean containsEnvy = false;
			for (int i = 0; i < children.size(); i++) {
				if (children.get(i).verifyFeatureEnvy(ATFDTreshold, FDPTreshold, analyzedClass, staticFields,
						minBlockSize, libraryCheck, local)) {
					containsEnvy = true;
				}
			}
			return containsEnvy;
		}
	}

	protected void computeDataAccesses(String analyzedClass, boolean staticFields, Integer minBlockSize,
			boolean libraryCheck) {
//what is going on here
		HashSet<IVariableBinding> variableBindingsCache = new HashSet<IVariableBinding>();
		HashSet<IMethodBinding> methodBindingCache = new HashSet<IMethodBinding>();

		for (ASTNode node : this.getAllSubTreeASTNodes()) {
			MethodInvocationVisitor invocationVisitor = new MethodInvocationVisitor();
			node.accept(invocationVisitor);
			List<MethodInvocation> methodInvocations = invocationVisitor.getMethodInvocations();
			for (MethodInvocation invocation : methodInvocations) {
				boolean isGetter = invocation.getName().getFullyQualifiedName().startsWith("get");
				boolean isSetter = invocation.getName().getFullyQualifiedName().startsWith("set");
				if (isGetter || isSetter) {
					IMethodBinding methodBinding = invocation.resolveMethodBinding();
					IJavaElement element = methodBinding.getJavaElement();
					IPackageFragmentRoot root = (IPackageFragmentRoot) element
							.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
					IClasspathEntry classpathEntry;
					try {
						classpathEntry = root.getRawClasspathEntry();
						if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE || !libraryCheck) {
							if (!methodBindingCache.contains(methodBinding)) {
								boolean already = false;
								for (IMethodBinding mb : methodBindingCache) {
									if (mb.getDeclaringClass().equals(methodBinding.getDeclaringClass())) {
										if (mb.getName().substring(3).equals(methodBinding.getName().substring(3))) {
											already = true;
										}
									}
								}
								if (!already) {
									// why do we care about params?
									if (isGetter && methodBinding.getParameterTypes().length == 0) {
										incrementAccesses(analyzedClass, methodBinding.getDeclaringClass());
									}
									if (isSetter && methodBinding.getParameterTypes().length == 1) {
										incrementAccesses(analyzedClass, methodBinding.getDeclaringClass());
									}
								}
								methodBindingCache.add(methodBinding);
							}
						}
					} catch (JavaModelException e) {
						e.printStackTrace();
					}
				}
			}

			VariableBindingVisitor variableVisitor = new VariableBindingVisitor();
			node.accept(variableVisitor);
			Set<IVariableBinding> variables = variableVisitor.getVariableBindings();
			for (IVariableBinding binding : variables) {

				IJavaElement element = binding.getJavaElement();
				if (element != null) {
					IPackageFragmentRoot root = (IPackageFragmentRoot) element
							.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
					IClasspathEntry classpathEntry;
					try {
						classpathEntry = root.getRawClasspathEntry();

						if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE || !libraryCheck) {
							if (!variableBindingsCache.contains(binding)) {
								ITypeBinding typeBinding = binding.getDeclaringClass();
								if (typeBinding != null) {
									boolean staticCheck = true;
									if (!staticFields) {
										if (Modifier.isStatic(binding.getModifiers()))
											staticCheck = false;
									}
									if (staticCheck) {
										incrementAccesses(analyzedClass, typeBinding);
									}
								}
								variableBindingsCache.add(binding);
							}
						}

					} catch (JavaModelException e) {
						e.printStackTrace();
					}

				}
			}
		}
	}

	private void incrementAccesses(String analyzedClass, ITypeBinding accessClassBinding) {
		if (checkLocalAccess(analyzedClass, accessClassBinding)) {
			localAttrAccess++;
		} else {
			if (accessClassesMapping.get(accessClassBinding.getName()) != null) {
				int nrAccesses = accessClassesMapping.get(accessClassBinding.getName());
				accessClassesMapping.put(accessClassBinding.getName(), nrAccesses + 1);
			} else {
				accessClassesMapping.put(accessClassBinding.getName(), 1);
			}
		}
	}

	private boolean checkLocalAccess(String analyzedClass, ITypeBinding accessClassBinding) {
		if (accessClassBinding.getName().equals(analyzedClass))
			return true;
		if (accessClassBinding.getSuperclass() != null) {
			checkLocalAccess(analyzedClass, accessClassBinding.getSuperclass());
		}
		return false;
	}

	public void clearChildrenData() {
		for (int i = 0; i < children.size(); i++) {
			children.get(i).clearChildrenData();
		}
		accessClassesMapping.clear();
		accessForeignData = 0;
		foreignDataProviders = 0;
		localAttrAccess = 0;
	}

	public void colorEnvyLeafNodes(ITextEditor textEditor, IFile file) throws CoreException {
		if (/* isEnvy&& */this instanceof CodeFragmentLeaf) {
			String colorType = "annotationColor_17";
			if (colorCounter < 17) {
				colorType = "annotationColor_" + colorCounter;
				colorCounter++;
			}
			int start = this.getStartNode();
			int end = this.getEndNode();
			try {
				Position fragmentPosition = new Position(start, (end - start));
				IMarker mymarker = SelectionView.createMarker(file, fragmentPosition);
				SelectionView.addAnnotation(mymarker, textEditor, colorType, fragmentPosition);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			for (int i = 0; i < children.size(); i++) {
				children.get(i).colorEnvyLeafNodes(textEditor, file);
			}
		}

	}

	public void addChild(AbstractCodeFragment child) {
		children.add(child);
	}

	public boolean isEnvy() {
		return isEnvy;
	}

	public void setEnvy(boolean isEnvy) {
		this.isEnvy = isEnvy;
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

	public String toString() {
		return internalASTNodes.toString();
	}

}
