package ro.lrg.method.defragmenter.methods;

import org.eclipse.jdt.core.JavaModelException;

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
		Group<MFragment> group = new Group<>();
		try {
			String sourceCode = arg0.getUnderlyingObject().getSource();
			arg0.getUnderlyingObject().getCompilationUnit();
			group.add(Factory.getInstance().createMFragment(sourceCode));
		} catch(JavaModelException e) {
			e.printStackTrace();
		}
		return group;
	}
}