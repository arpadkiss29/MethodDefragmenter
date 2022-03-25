package ro.lrg.method.defragmenter.metamodel.methods;

import methoddefragmenter.metamodel.entity.MMethod;
import ro.lrg.xcore.metametamodel.IPropertyComputer;
import ro.lrg.xcore.metametamodel.PropertyComputer;

@PropertyComputer
public class ToString implements IPropertyComputer<String, MMethod> {
	@Override
	public String compute(MMethod arg0) {
		return arg0.getUnderlyingObject().getElementName();
	}
}
