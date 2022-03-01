package ro.lrg.method.defragmenter.projects;

import methoddefragmenter.metamodel.entity.MProject;
import ro.lrg.xcore.metametamodel.IPropertyComputer;
import ro.lrg.xcore.metametamodel.PropertyComputer;

@PropertyComputer
public class ToString implements IPropertyComputer<String, MProject> {
	@Override
	public String compute(MProject arg0) {
		return arg0.getUnderlyingObject().getElementName();
	}
}

