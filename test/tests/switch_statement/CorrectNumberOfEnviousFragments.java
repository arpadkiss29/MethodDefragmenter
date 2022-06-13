package tests.switch_statement;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import tests.TestRunner;

public class CorrectNumberOfEnviousFragments {

	@Test
	public void correctNumberOfEnviousFragments() {
		assertEquals(TestRunner.findClass("three_letter_class.SwitchStatement").enviousFragmentGroup().getElements().size(), 2);
	}
	
	@Test
	public void correctNumberOfEnviousFragments_switchStatement() {
		assertEquals(TestRunner.findMethod("three_letter_class.SwitchStatement", "switchStatement").enviousFragmentGroup().getElements().size(), 2);
	}
}
