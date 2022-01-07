package ro.lrg.method.defragmenter.methods;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import methoddefragmenter.metamodel.entity.MFragment;
import methoddefragmenter.metamodel.entity.MMethod;
import methoddefragmenter.metamodel.factory.Factory;
import ro.lrg.xcore.metametamodel.Group;
import ro.lrg.xcore.metametamodel.IRelationBuilder;
import ro.lrg.xcore.metametamodel.RelationBuilder;

@RelationBuilder
public class FragmentGroup implements IRelationBuilder<MFragment, MMethod>{
	@Override
	public Group<MFragment> buildGroup(MMethod arg0) {
        @SuppressWarnings("deprecation")
		ASTParser parser = ASTParser.newParser(AST.JLS4);
        String s=null;
		try {
			s = ((IMethod)arg0.getUnderlyingObject()).getSource().substring(((IMethod)arg0.getUnderlyingObject()).getSource().indexOf("{"));
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
        parser.setSource(s.toCharArray());
        parser.setKind(ASTParser.K_STATEMENTS);
        
        Block block = (Block) parser.createAST(null);
        Group<MFragment> group = new Group<>();
        MFragment m = Factory.getInstance().createMFragment(block);
        group.add(m);
        
        return group;
    }
}





