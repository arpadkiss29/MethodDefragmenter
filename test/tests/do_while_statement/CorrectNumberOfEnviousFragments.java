package tests.do_while_statement;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import tests.TestRunner;

public class CorrectNumberOfEnviousFragments {

	@Test
	public void correctNumberOfEnviousFragments() {
		assertEquals(TestRunner.findClass("three_letter_class.DoWhileStatement").enviousFragmentGroup().getElements().size(), 2);
	}
	
	@Test
	public void correctNumberOfEnviousFragments_doWhileStatement() {
		String[] parameterTypeSignatures = {};
		assertEquals(TestRunner.findMethod("three_letter_class.DoWhileStatement", "doWhileStatement", parameterTypeSignatures).enviousFragmentGroup().getElements().size(), 2);
	}
}
