package tests;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class CorrectNumberOfEnviousFragmentsForProjectLevelCall {
	@Test
	public void resultedNumberOfFragmentsForProjectCallLevelIsCorrect() {
		assertEquals(TestRunner.getProject().enviousFragmentGroup().getElements().size(), 12);
	}
}
