package ro.lrg.method.defragmenter.metamodel.methods;

import java.util.List;
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
	
	private Block findMethodBody(String methodName, String className, CompilationUnit compilationUnit) {
		List<TypeDeclaration> typeDeclarations = compilationUnit.types();
        for(TypeDeclaration typeDeclaration : typeDeclarations) {
        	if(typeDeclaration.getName().getIdentifier().equals(className)) {
        		MethodDeclaration[] methodDeclarations = typeDeclaration.getMethods();
                for (MethodDeclaration methodDeclaration : methodDeclarations) {
                	if(methodDeclaration.getName().getIdentifier().equals(methodName)) {
                		return methodDeclaration.getBody();
                	}
                }
        	}
        }
        return null;
	}
	
	@Override
    public Group<MFragment> buildGroup(MMethod arg0) {
		IMethod method = arg0.getUnderlyingObject();
		ASTParser parser = ASTParser.newParser(AST.JLS4);
        parser.setSource(method.getCompilationUnit());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);
        CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
        
        String className = method.getDeclaringType().getElementName();
        String methodName = method.getElementName();
        Block block = findMethodBody(methodName, className, compilationUnit);
        
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





