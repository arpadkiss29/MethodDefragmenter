package tests;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import methoddefragmenter.metamodel.entity.MMethod;

public class CorrectNumberOfElementsInMethodGroups {
	
	@Test
	public void CorrectNumberOfElementsInMethodGroup_Circle() {
		List<MMethod> mMethods = TestRunner.findClass("recorder.data.Circle").methodGroup().getElements();
		assertEquals("Error at Circle class  method group!", mMethods.size(), 4);
	}
	
	@Test
	public void CorrectNumberOfElementsInMethodGroup_Rectangle() {
		List<MMethod> mMethods = TestRunner.findClass("recorder.data.Rectangle").methodGroup().getElements();
		assertEquals("Error at Rectangle class method group!", mMethods.size(), 5);
	}
	
	@Test
	public void CorrectNumberOfElementsInMethodGroup_Recorder() {
		List<MMethod> mMethods = TestRunner.findClass("recorder.Recorder").methodGroup().getElements();
		assertEquals("Error at Recorder class  method group!", mMethods.size(), 2);
	}
	
	@Test
	public void CorrectNumberOfElementsInMethodGroup_ThreeLetterClass() {
		List<MMethod> mMethods = TestRunner.findClass("three_letter_class.data.ThreeLetterClass").methodGroup().getElements();
		assertEquals("Error at ThreeLetterClass class method group!", mMethods.size(), 0);
	}
	
	@Test
	public void CorrectNumberOfElementsInMethodGroup_ABC() {
		List<MMethod> mMethods = TestRunner.findClass("three_letter_class.data.ABC").methodGroup().getElements();
		assertEquals("Error at ABC class method group!", mMethods.size(), 6);
	}
	
	@Test
	public void CorrectNumberOfElementsInMethodGroup_DEF() {
		List<MMethod> mMethods = TestRunner.findClass("three_letter_class.data.DEF").methodGroup().getElements();
		assertEquals("Error at DEF class method group!", mMethods.size(), 6);
	}
	
	@Test
	public void CorrectNumberOfElementsInMethodGroup_DoWhileStatement() {
		List<MMethod> mMethods = TestRunner.findClass("three_letter_class.DoWhileStatement").methodGroup().getElements();
		assertEquals("Error at DoWhileStatement class method group!", mMethods.size(), 1);
	}
	
	@Test
	public void CorrectNumberOfElementsInMethodGroup_ForStatement() {
		List<MMethod> mMethods = TestRunner.findClass("three_letter_class.ForStatement").methodGroup().getElements();
		assertEquals("Error at ForStatement class method group!", mMethods.size(), 1);
	}
	
	@Test
	public void CorrectNumberOfElementsInMethodGroup_ParticularCase() {
		List<MMethod> mMethods = TestRunner.findClass("three_letter_class.ParticularCase").methodGroup().getElements();
		assertEquals("Error at ParticularCase class method group!", mMethods.size(), 1);
	}
	
	@Test
	public void CorrectNumberOfElementsInMethodGroup_SwitchStatement() {
		List<MMethod> mMethods = TestRunner.findClass("three_letter_class.SwitchStatement").methodGroup().getElements();
		assertEquals("Error at SwitchStatement class method group!", mMethods.size(), 1);
	}
	
	@Test
	public void CorrectNumberOfElementsInMethodGroup_WhileStatement() {
		List<MMethod> mMethods = TestRunner.findClass("three_letter_class.WhileStatement").methodGroup().getElements();
		assertEquals("Error at WhileStatement class method group!", mMethods.size(), 1);
	}
}
