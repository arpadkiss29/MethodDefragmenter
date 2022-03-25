package ro.lrg.method.defragmenter.metamodel.projects;

import java.util.List;

import methoddefragmenter.metamodel.entity.MClass;
import methoddefragmenter.metamodel.entity.MFragment;
import methoddefragmenter.metamodel.entity.MProject;
import ro.lrg.xcore.metametamodel.Group;
import ro.lrg.xcore.metametamodel.IRelationBuilder;
import ro.lrg.xcore.metametamodel.RelationBuilder;

@RelationBuilder
public class EnviousFragmentGroup implements IRelationBuilder<MFragment, MProject>{
	@Override
	public Group<MFragment> buildGroup(MProject arg0) {
		List<MClass> classes = arg0.classGroup().getElements();
		Group<MFragment> fragmentGroup = new Group<>();
		classes.forEach(class_->fragmentGroup.addAll(class_.enviousFragmentGroup().getElements()));
		return fragmentGroup;
	}
}
