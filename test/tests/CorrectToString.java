package tests;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

import methoddefragmenter.metamodel.entity.MClass;
import methoddefragmenter.metamodel.entity.MMethod;

public class CorrectToString {
	
	@Test
	public void correctToStringForProject() {
		assertEquals(TestRunner.getProject().toString(), "MethodDefragmenterTestProject");
	}
	
	@Test
	public void correctToStringForClasses() {
		String[] correctValues = {
				"recorder.Recorder",
				"recorder.data.Rectangle",
				"recorder.data.Circle",
				"three_letter_class.ForStatement",
				"three_letter_class.DoWhileStatement",
				"three_letter_class.ParticularCase",
				"three_letter_class.SwitchStatement",
				"three_letter_class.WhileStatement",
				"three_letter_class.data.ThreeLetterClass",
				"three_letter_class.data.DEF",
				"three_letter_class.data.ABC"
		};
		
		List<MClass> mClasses = TestRunner.getProject().classGroup().getElements();
		for (MClass mclass : mClasses) {
			assertEquals(
					"Error at class " + correctValues[mClasses.indexOf(mclass)] + "!", 
					mclass.toString(), 
					correctValues[mClasses.indexOf(mclass)]
			);
		}
	}
	
	@Test
	public void correctToStringForMethods() {
		String[] correctValues = {
				"Recorder", "persist",
				"Rectangle", "getX1", "getY1", "getX2", "getY2",
				"Circle", "getX", "getY", "getR",
				"forStatement", "doWhileStatement", "particularCase", "switchStatement", "whileStatement",
				"getD", "setD", "getE", "setE", "getF", "setF",
				"getA", "setA", "getB", "setB", "getC", "setC"
		};
				
		List<MMethod> mMethods = TestRunner.getProject().classGroup().getElements().stream()
				.map(mClass -> mClass.methodGroup().getElements())
				.flatMap(array -> array.stream())
				.collect(Collectors.toList());
		for (MMethod mMethod : mMethods) {
			String className = mMethod.getUnderlyingObject().getDeclaringType().getElementName();
			assertEquals(
					"Error at method " + className + "." + correctValues[mMethods.indexOf(mMethod)] + "!",
					mMethod.toString(), 
					correctValues[mMethods.indexOf(mMethod)]
			);
		}
	}
}
