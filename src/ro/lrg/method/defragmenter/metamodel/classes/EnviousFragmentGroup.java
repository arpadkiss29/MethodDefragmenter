package ro.lrg.method.defragmenter.metamodel.classes;

import java.util.List;

import methoddefragmenter.metamodel.entity.MClass;
import methoddefragmenter.metamodel.entity.MFragment;
import methoddefragmenter.metamodel.entity.MMethod;
import ro.lrg.xcore.metametamodel.Group;
import ro.lrg.xcore.metametamodel.IRelationBuilder;
import ro.lrg.xcore.metametamodel.RelationBuilder;

@RelationBuilder
public class EnviousFragmentGroup implements IRelationBuilder<MFragment, MClass>{
	@Override
	public Group<MFragment> buildGroup(MClass arg0) {
		List<MMethod> methods = arg0.methodGroup().getElements();
		Group<MFragment> enviousFragmentGroup = new Group<>();
		methods.forEach(method->enviousFragmentGroup.addAll(method.enviousFragmentGroup().getElements()));
		return enviousFragmentGroup;
	}
}