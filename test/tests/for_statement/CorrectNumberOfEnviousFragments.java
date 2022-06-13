package tests.for_statement;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import tests.TestRunner;

public class CorrectNumberOfEnviousFragments {

	@Test
	public void correctNumberOfEnviousFragments() {
		assertEquals(TestRunner.findClass("three_letter_class.ForStatement").enviousFragmentGroup().getElements().size(), 2);
	}
	
	@Test
	public void correctNumberOfEnviousFragments_forStatement() {
		assertEquals(TestRunner.findMethod("three_letter_class.ForStatement", "forStatement").enviousFragmentGroup().getElements().size(), 2);
	}
}
