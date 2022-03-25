package ro.lrg.method.defragmenter.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.ui.texteditor.ITextEditor;

import ro.lrg.method.defragmenter.visitors.MethodInvocationVisitor;
import ro.lrg.method.defragmenter.visitors.VariableBindingVisitor;

public abstract class AbstractInternalCodeFragment {
	private final IFile iFile;
	private final IJavaProject iJavaProject;
	private final List<ASTNode> internalStatements = new ArrayList<ASTNode>();
	protected static int colorCounter = 0;
	protected boolean possiblyRelatedFlag = false;
	protected int startPosition = 0;
	protected int endPosition = 0;
	protected List<AbstractInternalCodeFragment> cohesivlyRelatedNodes = new ArrayList<AbstractInternalCodeFragment>();
	
	protected AbstractInternalCodeFragment(IFile iFile, IJavaProject iJavaProject) {
		this.iFile = iFile;
		this.iJavaProject = iJavaProject;
	}

	// Feature envy
	
	protected boolean isEnvy = false;
	protected int ATFD = 0;
	protected int FDP = 0;
	protected int LAA = 0;
	protected Map<String, FDPClass> detailedFDPMap = new HashMap<>();
	private Map<String, Integer> FDPMap;

	public Map<String, Integer> getFDPMap(String analyzedClass, boolean considerStaticFieldAccess, boolean libraryCheck, Integer minBlockSize) {
		computeDataAccesses(analyzedClass, considerStaticFieldAccess, libraryCheck, minBlockSize);
		FDPMap = new HashMap<>();
		for (Entry<String, FDPClass> entry : detailedFDPMap.entrySet()) {
			FDPMap.put(entry.getKey(), entry.getValue().getNumberOfAccesses());
		}
		return FDPMap;
	}
	
	public List<IType> getFDPITypesList() {
		return detailedFDPMap.entrySet().stream().map(entry -> {
			return entry.getValue().getIType();
		}).toList();
	}

	@Override
	public String toString() {
		return internalStatements.toString();
	}
	
	//protected methods
	
	protected void clearDataAux() {
		detailedFDPMap.clear();
		ATFD = 0;
		FDP = 0;
		LAA = 0;
		isEnvy = false;
	}
	
	protected void initAux() {
		colorCounter = 0;
	}
	
	protected void computeDataAccessesAux(String analyzedClass, boolean considerStaticFieldAccess, boolean libraryCheck, Integer minBlockSize) {
		Set<IVariableBinding> variableBindingsCache = new HashSet<IVariableBinding>();
		Set<IMethodBinding> methodBindingCache = new HashSet<IMethodBinding>();

		for (ASTNode node : getAllInternalStatements()) {
			MethodInvocationVisitor invocationVisitor = new MethodInvocationVisitor();
			node.accept(invocationVisitor);
			List<MethodInvocation> methodInvocations = invocationVisitor.getMethodInvocations();
			for (MethodInvocation invocation : methodInvocations) {
				boolean isGetter = invocation.getName().getFullyQualifiedName().startsWith("get")
						&& invocation.resolveMethodBinding().getParameterTypes().length == 0;
				boolean isSetter = invocation.getName().getFullyQualifiedName().startsWith("set")
						&& invocation.resolveMethodBinding().getParameterTypes().length == 1;
				if (isGetter || isSetter) {
					IMethodBinding methodBinding = invocation.resolveMethodBinding();
					IJavaElement element = methodBinding.getJavaElement();
					IPackageFragmentRoot root = (IPackageFragmentRoot) element.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
					try {
						IClasspathEntry classpathEntry = root.getRawClasspathEntry();
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
									incrementAccesses(analyzedClass, methodBinding.getDeclaringClass());
								}
								methodBindingCache.add(methodBinding);
							}
						}
					} catch (JavaModelException e) {
						e.printStackTrace();
					}
				}
			}

			VariableBindingVisitor variableBindingVisitor = new VariableBindingVisitor();
			node.accept(variableBindingVisitor);
			List<IVariableBinding> variableBindings = variableBindingVisitor.getVariableBindings();
			for (IVariableBinding variableBinding : variableBindings) {
				IJavaElement element = variableBinding.getJavaElement();
				if (element != null) {
					IPackageFragmentRoot root = (IPackageFragmentRoot) element.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
					try {
						IClasspathEntry classpathEntry = root.getRawClasspathEntry();
						if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE || !libraryCheck) {
							if (!variableBindingsCache.contains(variableBinding)) {
								ITypeBinding typeBinding = variableBinding.getDeclaringClass();
								if (typeBinding != null) {
									boolean staticCheck = true;
									if (!considerStaticFieldAccess) {
										if (Modifier.isStatic(variableBinding.getModifiers()))
											staticCheck = false;
									}
									if (staticCheck) {
										incrementAccesses(analyzedClass, typeBinding);
									}
								}
								variableBindingsCache.add(variableBinding);
							}
						}
					} catch (JavaModelException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	//private methods

	private void incrementAccesses(String analyzedClass, ITypeBinding accessClassBinding) {
		if (checkLocalAccess(analyzedClass, accessClassBinding)) {
			LAA++;
		} else {
			if (detailedFDPMap.get(accessClassBinding.getName()) != null) {
				FDPClass providerClass = detailedFDPMap.get(accessClassBinding.getName());
				providerClass.incrementNumberOfAccesses();
			} else {
				FDPClass providerClass = new FDPClass((IType) accessClassBinding.getJavaElement());
				providerClass.incrementNumberOfAccesses();
				detailedFDPMap.put(providerClass.getClassName(), providerClass);
			}
		}
	}

	private boolean checkLocalAccess(String analyzedClass, ITypeBinding accessClassBinding) {
		//System.err.println("deep in code: " + accessClassBinding.getName());
		if (accessClassBinding.getName().equals(analyzedClass)) return true;
		if (accessClassBinding.getSuperclass() != null) {
			checkLocalAccess(analyzedClass, accessClassBinding.getSuperclass());
		}
		return false;
	}
	
	//abstract methods
	
	public abstract void clearData();
	
	public abstract void colorEnvyLeafNodes(ITextEditor textEditor, IFile file) throws CoreException;
	
	public abstract void colorLongMethodFragments(ITextEditor textEditor, IFile file, List<AbstractInternalCodeFragment> functionalSegmentNodes);
	
	public abstract void computeDataAccesses(String analyzedClass, boolean considerStaticFieldAccess, boolean libraryCheck, Integer minBlockSize);
	
	public abstract AbstractInternalCodeFragment constructTree();
	
	public abstract List<AbstractInternalCodeFragment> getAllEnviousNodes();
	
	public abstract List<ASTNode> getAllInternalStatements();
	
	public abstract int getFragmentFirstLineStartIndex();
	
	public abstract int getFragmentLastLineEndIndex();
	
	public abstract List<AbstractInternalCodeFragment> identifyFunctionalSegments();
	
	public abstract void init();
	
	public abstract void print(int tabs);
	
	public abstract boolean verifyFeatureEnvy(int ATFDTreshold, int FDPTreshold, double LAATreshold, String analyzedClass,
			boolean considerStaticFieldAccess, boolean libraryCheck, Integer minBlockSize, boolean local);
	
	//accessory methods
	
	public void addInternalStatement(ASTNode statement) {
		if(statement == null) {
			System.err.println("Found null statement!");
			return;
		}
		internalStatements.add(statement);
	}
	
	public void addInternalStatements(List<ASTNode> statements) {
		this.internalStatements.addAll(statements);
	}
	
	public int getATFD() {
		return ATFD;
	}

	public static void setColorCounter(int colorCounter) {
		AbstractInternalCodeFragment.colorCounter = colorCounter;
	}

	public int getEndPosition() {
		return endPosition;
	}
	
	public Integer getFDP() {
		return FDP;
	}

	public IFile getIFile() {
		return iFile;
	}
	
	public IJavaProject getIJavaProject() {
		return iJavaProject;
	}
	
	public ASTNode getInternalStatement(int index) {
		return internalStatements.get(index);
	}
	
	protected List<ASTNode> getInternalStatements() {
		return internalStatements;
	}
	
	public int getInternalStatementsSize() {
		return internalStatements.size();
	}
	
	public int getLAA() {
		return LAA;
	}
	
	public List<AbstractInternalCodeFragment> getPossiblyRelatedNodes() {
		return cohesivlyRelatedNodes;
	}
	
	public int getStartPosition() {
		return startPosition;
	}

	public Map<String, Integer> getFDPMap() {
		return FDPMap;
	}
	
	public boolean isEnvy() {
		return isEnvy;
	}

	public boolean isPossiblyRelatedFlag() {
		return possiblyRelatedFlag;
	}
	
	public void removeInternalStatement(ASTNode statement) {
		internalStatements.remove(statement);
	}
	
	public void setATFD(int ATFD) {
		this.ATFD = ATFD;
	}
	
	public void setEndPosition(int endPosition) {
		this.endPosition = endPosition;
	}
	
	public void setEnvy(boolean isEnvy) {
		this.isEnvy = isEnvy;
	}
	
	public void setFDP(int FDP) {
		this.FDP = FDP;
	}

	public void setLAA(int LAA) {
		this.LAA = LAA;
	}
	
	public void setPossiblyRelatedFlag(boolean possiblyRelatedFlag) {
		this.possiblyRelatedFlag = possiblyRelatedFlag;
	}
	
	public void setStartPosition(int startPosition) {
		this.startPosition = startPosition;
	}
	
	public void setFDPMap(Map<String, Integer> storedFDP) {
		this.FDPMap = storedFDP;
	}
}