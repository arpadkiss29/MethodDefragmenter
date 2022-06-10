package ro.lrg.method.defragmenter.metamodel.classes;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import methoddefragmenter.metamodel.entity.MClass;
import methoddefragmenter.metamodel.entity.MMethod;
import methoddefragmenter.metamodel.factory.Factory;
import ro.lrg.xcore.metametamodel.Group;
import ro.lrg.xcore.metametamodel.IRelationBuilder;
import ro.lrg.xcore.metametamodel.RelationBuilder;

@RelationBuilder
public class MethodGroup implements IRelationBuilder<MMethod, MClass> {
	@Override
	public Group<MMethod> buildGroup(MClass arg0) {
		Group<MMethod> group = new Group<>();
		try {
			IMethod[] iMethods = arg0.getUnderlyingObject().getMethods();
			for (IMethod iMethod : iMethods) {
				MMethod mMethod = Factory.getInstance().createMMethod(iMethod);
				group.add(mMethod);
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return group;
	}
}


