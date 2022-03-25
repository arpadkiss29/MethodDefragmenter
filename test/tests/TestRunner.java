package tests;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import methoddefragmenter.metamodel.entity.MClass;
import methoddefragmenter.metamodel.entity.MMethod;
import methoddefragmenter.metamodel.entity.MProject;
import methoddefragmenter.metamodel.factory.Factory;
import ro.lrg.method.defragmenter.preferences.DefaultPreferences;
import ro.lrg.xcore.metametamodel.Group;

@RunWith(Suite.class)
@SuiteClasses({CorrectNumberOfEnviousFragments.class})
public class TestRunner implements DefaultPreferences{
	private static final String PROJECT_NAME = "MethodDefragmenterTestProject";
	private static Group<MClass> projectClasses;
	
	@BeforeClass
	public static void setUp() {
		TestUtil.importProject(PROJECT_NAME, PROJECT_NAME + ".zip");
		IJavaProject project = TestUtil.getProject(PROJECT_NAME).get();
		setProjectClasses(project);
	}

	@AfterClass
	public static void tearDown() {
		TestUtil.deleteProject(PROJECT_NAME);
	}

	public static MProject getProject() {
		if(TestUtil.getProject(PROJECT_NAME).isEmpty()) throw new NullPointerException();
		System.err.println(TestUtil.getProject(PROJECT_NAME).isEmpty() + TestUtil.getProject(PROJECT_NAME).get().getElementName());
		return Factory.getInstance().createMProject(TestUtil.getProject(PROJECT_NAME).get());
	}
	
	public static MClass findClass(String className) {
		for (MClass projectClass : projectClasses.getElements()) {
			if (projectClass.getUnderlyingObject().getFullyQualifiedName().equals(className)) {
				return projectClass;
			}
		}
		return null;
	}
	
	public static MMethod findMethod(String className, String methodName) {		
		MClass mClass = findClass(className);
		try {
			IMethod[] classMethods = mClass.getUnderlyingObject().getMethods();
			for (IMethod classMethod : classMethods) {
				if (classMethod.getElementName().equals(methodName)) {
					return Factory.getInstance().createMMethod(classMethod);
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return null;	
	}
	
	private static void setProjectClasses(IJavaProject project) {
		try {
			projectClasses = new Group<>();
			IPackageFragment[] iPackageFragments = project.getPackageFragments();
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
							projectClasses.add(Factory.getInstance().createMClass(c));
						}
					}
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}
}