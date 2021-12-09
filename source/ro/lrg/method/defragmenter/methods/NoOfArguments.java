package ro.lrg.method.defragmenter.methods;

import org.eclipse.jdt.core.IMethod;

import methoddefragmenter.metamodel.entity.MMethod;
import ro.lrg.xcore.metametamodel.IPropertyComputer;
import ro.lrg.xcore.metametamodel.PropertyComputer;

@PropertyComputer
public class NoOfArguments implements IPropertyComputer<Integer,MMethod>{
	@Override
	public Integer compute(MMethod arg0) {
		IMethod m = arg0.getUnderlyingObject();
		return m.getNumberOfParameters();
	}
}
