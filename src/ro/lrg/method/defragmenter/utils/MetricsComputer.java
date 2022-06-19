package ro.lrg.method.defragmenter.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;

import ro.lrg.method.defragmenter.preferences.MethodDefragmenterPropertyStore;
import ro.lrg.method.defragmenter.visitors.ast.MethodInvocationVisitor;
import ro.lrg.method.defragmenter.visitors.ast.VariableBindingVisitor;

public class MetricsComputer {
	private final Map<String, FDPClass> detailedFDPMap = new HashMap<>();
	private final Map<String, Integer> FDPMap = new HashMap<>();
	private int localAccesses;
	private int ATFD;
	private double LAA;
	
	public static MetricsComputer getComputedMetrics(AbstractInternalCodeFragment abstractInternalCodeFragment) {
		MethodDefragmenterPropertyStore propertyStore = new MethodDefragmenterPropertyStore(abstractInternalCodeFragment.getIJavaProject());
		MetricsComputer metricsComputer = new MetricsComputer();
		metricsComputer.computeDataAccesses(abstractInternalCodeFragment, abstractInternalCodeFragment.getAnalizedClass(), 
				propertyStore.isConsiderStaticFieldAccesses(), propertyStore.isLibraryCheck());
		return metricsComputer;
	}
	
	private void computeDataAccesses(AbstractInternalCodeFragment fragment, String analyzedClass, boolean considerStaticFieldAccess, boolean libraryCheck) {
		detailedFDPMap.clear();
		FDPMap.clear();
		localAccesses = 0;
		
		Set<IVariableBinding> variableBindingsCache = new HashSet<IVariableBinding>();
		Set<IMethodBinding> methodBindingCache = new HashSet<IMethodBinding>();
		
		List<ASTNode> nodes = fragment.getInternalStatements();
		
		for (ASTNode node : nodes) {
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
						if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE || libraryCheck) {
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
						if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE || libraryCheck) {
							if (!variableBindingsCache.contains(variableBinding)) {
								ITypeBinding typeBinding = variableBinding.getDeclaringClass();
								if (typeBinding != null) {
									if (!Modifier.isStatic(variableBinding.getModifiers())
											|| considerStaticFieldAccess && Modifier.isStatic(variableBinding.getModifiers())) {
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
		
		detailedFDPMap.entrySet().stream().forEach(e -> FDPMap.put(e.getKey(), e.getValue().getNumberOfAccesses()));
		ATFD = FDPMap.entrySet().stream().map(e -> e.getValue()).reduce(0, Integer::sum);
		LAA = localAccesses == 0 ? 0 : localAccesses * 1.0 / (localAccesses + ATFD);
	}
	
	private void incrementAccesses(String analyzedClass, ITypeBinding accessClassBinding) {
		if (checkLocalAccess(analyzedClass, accessClassBinding)) {
			localAccesses++;
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
		if (accessClassBinding.getName().equals(analyzedClass)) return true;
		if (accessClassBinding.getSuperclass() != null) {
			checkLocalAccess(analyzedClass, accessClassBinding.getSuperclass());
		}
		return false;
	}
	
	public boolean includesFDPMapOf(MetricsComputer metricsComputer) {
		if(metricsComputer == null) return false;
		Map<String, Integer> map = metricsComputer.getFDPMap();
		for(String key : map.keySet()) {
			if(!FDPMap.containsKey(key)) 
				return false;
		}
		return true;
	}
	
	public boolean hasTheSameFDPMapWith(MetricsComputer metricsComputer) {
		if(metricsComputer == null) return false;
		Map<String, Integer> map = metricsComputer.getFDPMap();
		if (FDPMap.size() != map.size()) return false;
		for(String key : map.keySet()) {
			if(!FDPMap.containsKey(key)) 
				return false;
		}
		return true;
	}
	
	public int getATFD() {
		return ATFD;
	}
	
	public Integer getFDP() {
		return FDPMap.size();
	}
	
	public List<IType> getFDPITypesList() {
		return detailedFDPMap.entrySet().stream().map(entry -> entry.getValue().getIType()).toList();
	}
	
	private Map<String, Integer> getFDPMap() {
		return FDPMap;
	}
	
	public double getLAA() {
		return LAA;
	}
}
