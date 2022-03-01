package ro.lrg.method.defragmenter.fragments;

import java.util.Map.Entry;
import java.util.Set;

import methoddefragmenter.metamodel.entity.MClass;
import methoddefragmenter.metamodel.entity.MFragment;
import methoddefragmenter.metamodel.factory.Factory;
import ro.lrg.method.defragmenter.utils.ITypeAccesses;
import ro.lrg.xcore.metametamodel.Group;
import ro.lrg.xcore.metametamodel.IRelationBuilder;
import ro.lrg.xcore.metametamodel.RelationBuilder;

@RelationBuilder
public class GetForeignDataProviderClassGroup implements IRelationBuilder<MClass, MFragment>{
	@Override
	public Group<MClass> buildGroup(MFragment arg0) {
		Set<Entry<String, ITypeAccesses>> set = arg0.getUnderlyingObject().getAccessClassesMapping().entrySet();
		Group<MClass> providerClassGroup = new Group<>();
		for(Entry<String, ITypeAccesses> entry:set) {
			MClass m = Factory.getInstance().createMClass(entry.getValue().getIType());
			providerClassGroup.add(m);
		}
		return providerClassGroup;
	}
}