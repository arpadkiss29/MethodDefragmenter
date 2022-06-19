package ro.lrg.method.defragmenter.metamodel.methods;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import methoddefragmenter.metamodel.entity.MFragment;
import methoddefragmenter.metamodel.entity.MMethod;
import methoddefragmenter.metamodel.factory.Factory;
import ro.lrg.method.defragmenter.preferences.GroupingAlgorithmsConstants;
import ro.lrg.method.defragmenter.preferences.MethodDefragmenterPropertyStore;
import ro.lrg.method.defragmenter.utils.AbstractInternalCodeFragment;
import ro.lrg.method.defragmenter.visitors.ast.InitialFragmentationVisitor;
import ro.lrg.method.defragmenter.visitors.fragment.collectors.AllEnviousLeavesVisitor;
import ro.lrg.method.defragmenter.visitors.fragment.groupers.ArpiGroupingVisitor;
import ro.lrg.method.defragmenter.visitors.fragment.groupers.GroupingVisitor;
import ro.lrg.method.defragmenter.visitors.fragment.groupers.Saleh1GroupingVisitor;
import ro.lrg.method.defragmenter.visitors.fragment.groupers.Saleh2GroupingVisitor;
import ro.lrg.xcore.metametamodel.Group;
import ro.lrg.xcore.metametamodel.IRelationBuilder;
import ro.lrg.xcore.metametamodel.RelationBuilder;

@RelationBuilder
public class EnviousFragmentGroup implements IRelationBuilder<MFragment, MMethod> {
	
	@SuppressWarnings("unchecked")
	private Block findMethodBody(IMethod method, CompilationUnit compilationUnit) {
		String className = method.getDeclaringType().getElementName();
		String methodName = method.getElementName();
        String[] parameterTypeSignatures = method.getParameterTypes();
		
		Optional<TypeDeclaration> correctTypeDeclaration = ((List<TypeDeclaration>) compilationUnit.types()).stream()
				.filter(typeDeclaration -> typeDeclaration.getName().getIdentifier().equals(className)).findFirst();
		if (correctTypeDeclaration.isEmpty()) return null;
		List<MethodDeclaration> methodDeclarations = Arrays.asList(correctTypeDeclaration.get().getMethods());
		
		for (MethodDeclaration methodDeclaration : methodDeclarations) {
			List<String> parameterTypeSignatures2 = Arrays.asList(((IMethod) methodDeclaration.resolveBinding()
					.getMethodDeclaration().getJavaElement()).getParameterTypes());
			if (methodDeclaration.getName().getIdentifier().equals(methodName) 
					&& parameterTypeSignatures2.size() == parameterTypeSignatures.length) {
				boolean found = true;
				for (String signature : parameterTypeSignatures2) {
					if (!signature.equals(parameterTypeSignatures[parameterTypeSignatures2.indexOf(signature)])) {
						found = false;
						break;
					}
				}
				if (found) return methodDeclaration.getBody();
			}
		}
        return null;
	}
	
	@SuppressWarnings("deprecation")
	@Override
    public Group<MFragment> buildGroup(MMethod arg0) {
		IMethod method = arg0.getUnderlyingObject();
		ASTParser parser = ASTParser.newParser(AST.JLS4);
        parser.setSource(method.getCompilationUnit());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);
        CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
        
        Block block = findMethodBody(method, compilationUnit);
        if (block == null) return new Group<>();
        
        String className = method.getDeclaringType().getElementName();
        IFile iFile = (IFile) method.getResource();
        IJavaProject iJavaProject = method.getJavaProject();
        
        InitialFragmentationVisitor fragmenter = new InitialFragmentationVisitor(className, iFile, iJavaProject);
        block.accept(fragmenter);
        AbstractInternalCodeFragment root = fragmenter.popLastNode();
        
        MethodDefragmenterPropertyStore propertyStore = new MethodDefragmenterPropertyStore(iJavaProject);
        
        GroupingVisitor groupingVisitor = null;
        switch(propertyStore.getGroupingAlgorithm()) {
        	case GroupingAlgorithmsConstants.ARPI:
        		groupingVisitor = new ArpiGroupingVisitor(className, iFile, iJavaProject);
        		break;
        	case GroupingAlgorithmsConstants.SALEH1:
        		groupingVisitor = new Saleh1GroupingVisitor(className, iFile, iJavaProject, propertyStore.getFDPTreshold());
        		break;
        	case GroupingAlgorithmsConstants.SALEH2:
        		groupingVisitor = new Saleh2GroupingVisitor(className, iFile, iJavaProject, propertyStore.getFDPTreshold());
        		break;
        	default: System.err.println("Unknown algorithm!");
        }
        root.accept(groupingVisitor);
        AbstractInternalCodeFragment groupedRoot = groupingVisitor.popLastNode(); 
        
        AllEnviousLeavesVisitor allEnviousFragmentsVisitor = new AllEnviousLeavesVisitor(propertyStore.getATFDTreshold(), 
        		propertyStore.getFDPTreshold(), propertyStore.getLAATreshold());
		groupedRoot.accept(allEnviousFragmentsVisitor);
		List<AbstractInternalCodeFragment> AICFs = allEnviousFragmentsVisitor.getAllEnviousFragments();
		
        Group<MFragment> group = new Group<>();
        for(AbstractInternalCodeFragment AICF : AICFs) {
        	MFragment m = Factory.getInstance().createMFragment(AICF);
            group.add(m);
        }
        return group;
	}
}





