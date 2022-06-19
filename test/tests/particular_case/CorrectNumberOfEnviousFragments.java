package tests.particular_case;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import tests.TestRunner;

public class CorrectNumberOfEnviousFragments {

	@Test
	public void correctNumberOfEnviousFragments() {
		assertEquals(TestRunner.findClass("three_letter_class.ParticularCase").enviousFragmentGroup().getElements().size(), 2);
	}
	
	@Test
	public void correctNumberOfEnviousFragments_particularCase() {
		String[] parameterTypeSignatures = {"I"};
		assertEquals(TestRunner.findMethod("three_letter_class.ParticularCase", "particularCase", parameterTypeSignatures).enviousFragmentGroup().getElements().size(), 2);
	}
}
