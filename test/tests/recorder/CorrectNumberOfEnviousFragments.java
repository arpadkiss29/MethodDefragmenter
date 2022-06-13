package tests.recorder;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import tests.TestRunner;

public class CorrectNumberOfEnviousFragments {

	@Test
	public void correctNumberOfEnviousFragments_Class() {
		assertEquals(TestRunner.findClass("recorder.Recorder").enviousFragmentGroup().getElements().size(), 2);
	}
	
	@Test
	public void correctNumberOfEnviousFragments_Recorder() {
		assertEquals(TestRunner.findMethod("recorder.Recorder", "Recorder").enviousFragmentGroup().getElements().size(), 0);
	}
	
	@Test
	public void correctNumberOfEnviousFragments_persist() {
		assertEquals(TestRunner.findMethod("recorder.Recorder", "persist").enviousFragmentGroup().getElements().size(), 2);
	}
}
