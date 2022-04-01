package ro.lrg.method.defragmenter.metamodel.fragments;

import methoddefragmenter.metamodel.entity.MClass;
import methoddefragmenter.metamodel.entity.MFragment;
import methoddefragmenter.metamodel.factory.Factory;
import ro.lrg.method.defragmenter.preferences.MethodDefragmenterPropertyStore;
import ro.lrg.method.defragmenter.utils.AbstractInternalCodeFragment;
import ro.lrg.method.defragmenter.utils.MetricsComputer;
import ro.lrg.xcore.metametamodel.Group;
import ro.lrg.xcore.metametamodel.IRelationBuilder;
import ro.lrg.xcore.metametamodel.RelationBuilder;

@RelationBuilder
public class GetForeignDataProviderClassGroup implements IRelationBuilder<MClass, MFragment>{
	@Override
	public Group<MClass> buildGroup(MFragment arg0) {
		AbstractInternalCodeFragment abstractInternalCodeFragment = arg0.getUnderlyingObject();
		MetricsComputer metricsComputer = new MetricsComputer();
		MethodDefragmenterPropertyStore propertyStore = new MethodDefragmenterPropertyStore(abstractInternalCodeFragment.getIJavaProject());
		metricsComputer.computeDataAccesses(abstractInternalCodeFragment, abstractInternalCodeFragment.getAnalizedClass(), 
				propertyStore.isConsiderStaticFieldAccesses(), propertyStore.isLibraryCheck(), propertyStore.getMinBlockSize());
		
		Group<MClass> FDPClassGroup = new Group<>();
		FDPClassGroup.addAll(metricsComputer.getFDPITypesList().stream().map(e->{
			return Factory.getInstance().createMClass(e);
		}).toList());

		return FDPClassGroup;
	}
}