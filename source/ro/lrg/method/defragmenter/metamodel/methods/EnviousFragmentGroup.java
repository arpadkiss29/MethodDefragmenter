package ro.lrg.method.defragmenter.metamodel.methods;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import methoddefragmenter.metamodel.entity.MFragment;
import methoddefragmenter.metamodel.entity.MMethod;
import methoddefragmenter.metamodel.factory.Factory;
import ro.lrg.method.defragmenter.fragmenters.FDPFragmenter;
import ro.lrg.method.defragmenter.preferences.MethodDefragmenterPropertyStore;
import ro.lrg.method.defragmenter.utils.AbstractInternalCodeFragment;
import ro.lrg.method.defragmenter.utils.InternalCodeFragment;
import ro.lrg.xcore.metametamodel.Group;
import ro.lrg.xcore.metametamodel.IRelationBuilder;
import ro.lrg.xcore.metametamodel.RelationBuilder;

@RelationBuilder
public class EnviousFragmentGroup implements IRelationBuilder<MFragment, MMethod> {
	
	@Override
    public Group<MFragment> buildGroup(MMethod arg0) {
		IMethod method = arg0.getUnderlyingObject();
		
        @SuppressWarnings("deprecation")
		ASTParser parser = ASTParser.newParser(AST.JLS4);
        parser.setSource(method.getCompilationUnit());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);
        CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
        
        String className = ((IType) method.getParent()).getElementName();
        String methodName = method.getElementName();
        Block block = findBlock(compilationUnit, className, methodName);
        
        IFile iFile = (IFile) method.getResource();
        IJavaProject iJavaProject = method.getJavaProject();
        MethodDefragmenterPropertyStore propertyStore = new MethodDefragmenterPropertyStore(iJavaProject);
        
        FDPFragmenter fdpFragmenter = new FDPFragmenter(className, iFile, iJavaProject, propertyStore.isConsiderStaticFieldAccesses(),
        		propertyStore.isLibraryCheck(), propertyStore.getMinBlockSize(), propertyStore.getFDPTreshold());
        block.accept(fdpFragmenter);
        
        InternalCodeFragment root = (InternalCodeFragment) fdpFragmenter.getLastNode().pop();
        root.verifyFeatureEnvy(propertyStore.getATFDTreshold(), propertyStore.getFDPTreshold(), propertyStore.getLAATreshold(), className, 
        		propertyStore.isConsiderStaticFieldAccesses(), propertyStore.isLibraryCheck(), propertyStore.getMinBlockSize(), false);

		List<AbstractInternalCodeFragment> ACFs = null;
		
		root.init();
		if(!propertyStore.isApplyLongMethodIdentification()) {
			ACFs = root.getAllEnviousNodes();
		} else {
			System.out.println("Calculating Long Method Fragmentation! Threshold: " + InternalCodeFragment.getNcocp2treshold());
			root.getCohesionMetric(compilationUnit);
			List<AbstractInternalCodeFragment> identifiedNodes = root.identifyFunctionalSegments();
			root.combineNodes(identifiedNodes);
			root.constructTree();
			ACFs = InternalCodeFragment.getAllNodesLeafs();
			ACFs.add(root);
		}
		
        Group<MFragment> group = new Group<>();
        ACFs.forEach(ACF->{
        	MFragment m = Factory.getInstance().createMFragment(ACF);
            group.add(m);
        });
        return group;
    }
	
	private Block findBlock(CompilationUnit compilationUnit, String className, String methodName) {
        @SuppressWarnings("unchecked")
		List<TypeDeclaration> ATDs = compilationUnit.types();
        for(TypeDeclaration ATD:ATDs) {
        	if(ATD.getName().getIdentifier().equals(className)) {
        		MethodDeclaration[] methodDeclarations = ATD.getMethods();
                for (MethodDeclaration methodDeclaration : methodDeclarations) {
                	if(methodDeclaration.getName().getIdentifier().equals(methodName)) {
                		return methodDeclaration.getBody();
                	}
                }
        	}
        }
        return null;
	}
}





