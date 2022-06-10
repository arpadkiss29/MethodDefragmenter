package tests;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import methoddefragmenter.metamodel.entity.MClass;
import methoddefragmenter.metamodel.entity.MMethod;

public class CorrectNumberOfElementsOfGroups {
	
	@Test
	public void CorrectNumberOfElementsOfClassGroup() {
		List<MClass> mClasses = TestRunner.getProject().classGroup().getElements();
		assertEquals("Error at project's class group!", mClasses.size(), 3);
	}
	
	@Test
	public void CorrectNumberOfElementsOfMethodGroup_Circle() {
		List<MMethod> mMethods = TestRunner.findClass("Circle").methodGroup().getElements();
		assertEquals("Error at Circle class  method group!", mMethods.size(), 4);
	}
	
	@Test
	public void CorrectNumberOfElementsOfMethodGroup_Recorder() {
		List<MMethod> mMethods = TestRunner.findClass("Recorder").methodGroup().getElements();
		assertEquals("Error at Recorder class  method group!", mMethods.size(), 2);
	}
	
	@Test
	public void CorrectNumberOfElementsOfMethodGroup_Rectangle() {
		List<MMethod> mMethods = TestRunner.findClass("Rectangle").methodGroup().getElements();
		assertEquals("Error at Rectangle class method group!", mMethods.size(), 5);
	}
}
