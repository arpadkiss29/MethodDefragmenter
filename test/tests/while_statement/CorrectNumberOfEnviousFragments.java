package tests.while_statement;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import tests.TestRunner;

public class CorrectNumberOfEnviousFragments {

	@Test
	public void correctNumberOfEnviousFragments() {
		assertEquals(TestRunner.findClass("three_letter_class.WhileStatement").enviousFragmentGroup().getElements().size(), 2);
	}
	
	@Test
	public void correctNumberOfEnviousFragments_whileStatement() {
		assertEquals(TestRunner.findMethod("three_letter_class.WhileStatement", "whileStatement").enviousFragmentGroup().getElements().size(), 2);
	}
}
