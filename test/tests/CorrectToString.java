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
		String[] correctValues = {"Rectangle", "Recorder", "Circle"};
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
		String[] correctValues = {"Rectangle", "getX1", "getY1", "getX2", "getY2", "Recorder", "persist", "Circle", "getX", "getY", "getR"};
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
