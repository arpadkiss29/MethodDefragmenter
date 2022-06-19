package tests;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import methoddefragmenter.metamodel.entity.MClass;

public class CorrectNumberOfElementsInProjectClassGroup {
	@Test
	public void CorrectNumberOfElementsInClassGroup() {
		List<MClass> mClasses = TestRunner.getProject().classGroup().getElements();
		assertEquals("Error at project's class group!", mClasses.size(), 11);
	}
}
