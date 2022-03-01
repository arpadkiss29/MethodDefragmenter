package ro.lrg.method.defragmenter.projects;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import methoddefragmenter.metamodel.entity.MClass;
import methoddefragmenter.metamodel.entity.MProject;
import methoddefragmenter.metamodel.factory.Factory;
import ro.lrg.xcore.metametamodel.Group;
import ro.lrg.xcore.metametamodel.IRelationBuilder;
import ro.lrg.xcore.metametamodel.RelationBuilder;

@RelationBuilder
public class ClassGroup implements IRelationBuilder<MClass, MProject> {
	@Override
	public Group<MClass> buildGroup(MProject arg0) {
		Group<MClass> group = new Group<>();
		try {
			IPackageFragment[] iPackageFragments = arg0.getUnderlyingObject().getPackageFragments();
			for (IPackageFragment iPackageFragment : iPackageFragments) {
				boolean isLocalPackage = true;
				IJavaElement parent = iPackageFragment.getParent();
				if (parent instanceof IPackageFragmentRoot && ((IPackageFragmentRoot) parent).isExternal()) {
					isLocalPackage = false;
				}
				if (isLocalPackage) {
					ICompilationUnit[] files = iPackageFragment.getCompilationUnits();
					for (ICompilationUnit file : files) {
						IType[] classes = file.getAllTypes();
						for (IType c : classes) {
							group.add(Factory.getInstance().createMClass(c));
						}
					}
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return group;
	}
}