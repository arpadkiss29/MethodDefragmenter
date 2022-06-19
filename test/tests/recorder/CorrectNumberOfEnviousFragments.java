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
		String[] parameterTypeSignatures = {"QDataOutputStream;"};
		assertEquals(TestRunner.findMethod("recorder.Recorder", "Recorder", parameterTypeSignatures).enviousFragmentGroup().getElements().size(), 0);
	}
	
	@Test
	public void correctNumberOfEnviousFragments_persist() {
		String[] parameterTypeSignatures = {"QObject;"};
		assertEquals(TestRunner.findMethod("recorder.Recorder", "persist", parameterTypeSignatures).enviousFragmentGroup().getElements().size(), 2);
	}
}
