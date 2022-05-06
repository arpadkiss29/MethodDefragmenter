package ro.lrg.method.defragmenter.startup;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.ui.IStartup;

import methoddefragmenter.metamodel.factory.Factory;
import ro.lrg.insider.view.ToolRegistration;
import ro.lrg.method.defragmenter.utils.AbstractInternalCodeFragment;
import ro.lrg.xcore.metametamodel.XEntity;

public class Startup implements IStartup {

	@Override
	public void earlyStartup() {
		ToolRegistration.getInstance().registerXEntityConverter(
				new ToolRegistration.XEntityConverter() {
					@Override
					public XEntity convert(Object element) {
						if (element instanceof IJavaElement) {
							IJavaElement iJavaElement = (IJavaElement) element;
							switch (iJavaElement.getElementType()) {
								case IJavaElement.JAVA_PROJECT:
									return Factory.getInstance().createMProject((IJavaProject) iJavaElement);
								case IJavaElement.TYPE:
									return Factory.getInstance().createMClass((IType) iJavaElement);
								case IJavaElement.COMPILATION_UNIT:
									ICompilationUnit unit = (ICompilationUnit) iJavaElement;
									return Factory.getInstance().createMClass(unit.findPrimaryType());
								case IJavaElement.METHOD:
									return Factory.getInstance().createMMethod((IMethod) iJavaElement);
								default:
									System.err.println("There is something else!");
							}
						} else if (element instanceof AbstractInternalCodeFragment) {
							return Factory.getInstance().createMFragment((AbstractInternalCodeFragment) element);
						}
						return null;
					}
				}
		);
	}
}
