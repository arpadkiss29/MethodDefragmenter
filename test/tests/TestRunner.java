	package tests;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
import ro.lrg.xcore.metametamodel.Group;

@RunWith(Suite.class)
@SuiteClasses({
	CorrectColoring.class, 
	CorrectNumberOfElementsInProjectClassGroup.class, 
	CorrectNumberOfElementsInMethodGroups.class, 
	CorrectNumberOfEnviousFragmentsForProjectLevelCall.class, 
	CorrectToString.class, 
	
	tests.do_while_statement.CorrectCodeContent.class, 
	tests.for_statement.CorrectCodeContent.class, 
	tests.particular_case.CorrectCodeContent.class, 
	tests.recorder.CorrectCodeContent.class, 
	tests.switch_statement.CorrectCodeContent.class, 
	tests.while_statement.CorrectCodeContent.class, 
	
	tests.do_while_statement.CorrectForeignDataProviders.class, 
	tests.for_statement.CorrectForeignDataProviders.class, 
	tests.particular_case.CorrectForeignDataProviders.class, 
	tests.recorder.CorrectForeignDataProviders.class, 
	tests.switch_statement.CorrectForeignDataProviders.class, 
	tests.while_statement.CorrectForeignDataProviders.class, 
	
	tests.do_while_statement.CorrectMetricsOfFragments.class, 
	tests.for_statement.CorrectMetricsOfFragments.class, 
	tests.particular_case.CorrectMetricsOfFragments.class, 
	tests.recorder.CorrectMetricsOfFragments.class, 
	tests.switch_statement.CorrectMetricsOfFragments.class, 
	tests.while_statement.CorrectMetricsOfFragments.class, 
	
	tests.do_while_statement.CorrectNumberOfEnviousFragments.class, 
	tests.for_statement.CorrectNumberOfEnviousFragments.class, 
	tests.particular_case.CorrectNumberOfEnviousFragments.class, 
	tests.recorder.CorrectNumberOfEnviousFragments.class, 
	tests.switch_statement.CorrectNumberOfEnviousFragments.class, 
	tests.while_statement.CorrectNumberOfEnviousFragments.class
	})
public class TestRunner {
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
		Optional<IJavaProject> project = TestUtil.getProject(PROJECT_NAME);
		if(project.isEmpty()) throw new NullPointerException();
		return Factory.getInstance().createMProject(project.get());
	}
	
	public static MClass findClass(String className) {
		for (MClass projectClass : projectClasses.getElements()) {
			if (projectClass.toString().equals(className)) {
				return projectClass;
			}
		}
		return null;
	}
	
	public static MMethod findMethod(String className, String methodName, String[] parameterTypeSignatures) {		
		MClass mClass = findClass(className);
		try {
			IMethod[] methods = mClass.getUnderlyingObject().getMethods();
			for (IMethod method : methods) {
				List<String> parameterTypeSignatures2 = Arrays.asList(method.getParameterTypes());
				if (method.getElementName().equals(methodName) && parameterTypeSignatures2.size() == parameterTypeSignatures.length) {
					boolean found = true;
					for (String signature : parameterTypeSignatures2) {
						if (!signature.equals(parameterTypeSignatures[parameterTypeSignatures2.indexOf(signature)])) {
							found = false;
							break;
						}
					}
					if (found) return Factory.getInstance().createMMethod(method);
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
				IJavaElement parent = iPackageFragment.getParent();
				if (parent instanceof IPackageFragmentRoot && ((IPackageFragmentRoot) parent).isExternal()) {
					continue;
				}
				ICompilationUnit[] files = iPackageFragment.getCompilationUnits();
				for (ICompilationUnit file : files) {
					IType[] classes = file.getAllTypes();
					for (IType class_ : classes) {
						projectClasses.add(Factory.getInstance().createMClass(class_));
					}
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}
}