package ro.lrg.method.defragmenter.metamodel.fragments;

import methoddefragmenter.metamodel.entity.MClass;
import methoddefragmenter.metamodel.entity.MFragment;
import methoddefragmenter.metamodel.factory.Factory;
import ro.lrg.xcore.metametamodel.Group;
import ro.lrg.xcore.metametamodel.IRelationBuilder;
import ro.lrg.xcore.metametamodel.RelationBuilder;

@RelationBuilder
public class GetForeignDataProviderClassGroup implements IRelationBuilder<MClass, MFragment>{
	@Override
	public Group<MClass> buildGroup(MFragment arg0) {
		Group<MClass> FDPClassGroup = new Group<>();
		FDPClassGroup.addAll(arg0.getUnderlyingObject().getFDPITypesList().stream().map(e->{
			return Factory.getInstance().createMClass(e);
		}).toList());

		return FDPClassGroup;
	}
}