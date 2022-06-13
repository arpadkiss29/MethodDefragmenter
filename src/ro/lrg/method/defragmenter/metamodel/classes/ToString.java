package ro.lrg.method.defragmenter.metamodel.classes;

import methoddefragmenter.metamodel.entity.MClass;
import ro.lrg.xcore.metametamodel.IPropertyComputer;
import ro.lrg.xcore.metametamodel.PropertyComputer;

@PropertyComputer
public class ToString implements IPropertyComputer<String, MClass> {
	@Override
	public String compute(MClass arg0) {
		return arg0.getUnderlyingObject().getFullyQualifiedName();
	}
}