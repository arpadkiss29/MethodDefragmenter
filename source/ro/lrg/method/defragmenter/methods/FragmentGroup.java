package ro.lrg.method.defragmenter.methods;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;

import methoddefragmenter.metamodel.entity.MFragment;
import methoddefragmenter.metamodel.entity.MMethod;
import methoddefragmenter.metamodel.factory.Factory;
import ro.lrg.method.defragmenter.visitors.BlockVisitor;
import ro.lrg.xcore.metametamodel.Group;
import ro.lrg.xcore.metametamodel.IRelationBuilder;
import ro.lrg.xcore.metametamodel.RelationBuilder;

@RelationBuilder
public class FragmentGroup implements IRelationBuilder<MFragment, MMethod>{
//	@Override
//	public Group<MFragment> buildGroup(MMethod arg0) {
//		Group<MFragment> group = new Group<>();
//		try {
//			String sourceCode = arg0.getUnderlyingObject().getSource();
//			arg0.getUnderlyingObject().getCompilationUnit();
//			group.add(Factory.getInstance().createMFragment(sourceCode));
//		} catch(JavaModelException e) {
//			e.printStackTrace();
//		}
//		return group;
//	}
	
	
	@Override
	public Group<MFragment> buildGroup(MMethod arg0) {
        @SuppressWarnings("deprecation")
		ASTParser parser = ASTParser.newParser(AST.JLS4);
        parser.setSource(arg0.getUnderlyingObject().getCompilationUnit());
        parser.setKind(ASTParser.K_STATEMENTS);
        
        ASTNode rootBlock = (ASTNode) parser.createAST(null);
        
        BlockVisitor blockVisitor = new BlockVisitor();
        rootBlock.accept(blockVisitor);
        
        ArrayList<Block> blocks = blockVisitor.getBlocks();
        
        Group<MFragment> group = new Group<>();
        System.err.println(blocks.size());
        for(Block block : blocks) {
        	System.err.println(block.getParent().toString());
        	System.err.println(block.getParent().getParent());
        	System.err.println(block.getParent().getParent().getParent());
        	System.err.println(block.getParent().getParent().getParent().getParent());
        	System.err.println(block.statements().size());
        	block.statements().forEach(System.err::println);
			MFragment m = Factory.getInstance().createMFragment(block);
			group.add(m);
		}
        
        return group;
    }
}





