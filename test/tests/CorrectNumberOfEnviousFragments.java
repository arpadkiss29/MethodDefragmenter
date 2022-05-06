package tests;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import ro.lrg.method.defragmenter.preferences.DefaultPreferences;


public class CorrectNumberOfEnviousFragments implements DefaultPreferences{
	@Test
	public void resultedNumberOfFragmentsForProjectCallLevelIsCorrect() {
		assertEquals(TestRunner.getProject().enviousFragmentGroup().getElements().size(), 1);
	}
	@Test
	public void resultedNumberOfFragmentsForClassCallLevelIsCorrect_Circle() {
		assertEquals(TestRunner.findClass("Circle").enviousFragmentGroup().getElements().size(), 0);
	}
	@Test
	public void resultedNumberOfFragmentsForClassCallLevelIsCorrect_Recorder() {
		assertEquals(TestRunner.findClass("Recorder").enviousFragmentGroup().getElements().size(), 1);
	}
	@Test
	public void resultedNumberOfFragmentsForClassCallLevelIsCorrect_Square() {
		assertEquals(TestRunner.findClass("Rectangle").enviousFragmentGroup().getElements().size(), 0);
	}
	@Test
	public void resultedNumberOfFragmentsForMethodCallLevelIsCorrect_Circle_Circle() {
		assertEquals(TestRunner.findMethod("Circle", "Circle").enviousFragmentGroup().getElements().size(), 0);
	}
	@Test
	public void resultedNumberOfFragmentsForMethodCallLevelIsCorrect_Circle_getX() {
		assertEquals(TestRunner.findMethod("Circle", "getX").enviousFragmentGroup().getElements().size(), 0);
	}
	@Test
	public void resultedNumberOfFragmentsForMethodCallLevelIsCorrect_Circle_getY() {
		assertEquals(TestRunner.findMethod("Circle", "getY").enviousFragmentGroup().getElements().size(), 0);
	}
	@Test
	public void resultedNumberOfFragmentsForMethodCallLevelIsCorrect_Circle_getR() {
		assertEquals(TestRunner.findMethod("Circle", "getR").enviousFragmentGroup().getElements().size(), 0);
	}
	@Test
	public void resultedNumberOfFragmentsForMethodCallLevelIsCorrect_Recorder_Circle() {
		assertEquals(TestRunner.findMethod("Recorder", "Recorder").enviousFragmentGroup().getElements().size(), 0);
	}
	@Test
	public void resultedNumberOfFragmentsForMethodCallLevelIsCorrect_Recorder_Persist() {
		assertEquals(TestRunner.findMethod("Recorder", "persist").enviousFragmentGroup().getElements().size(), 1);
	}
	@Test
	public void resultedNumberOfFragmentsForMethodCallLevelIsCorrect_Rectangle_Rectangle() {
		assertEquals(TestRunner.findMethod("Rectangle", "Rectangle").enviousFragmentGroup().getElements().size(), 0);
	}
	@Test
	public void resultedNumberOfFragmentsForMethodCallLevelIsCorrect_Rectangle_getX1() {
		assertEquals(TestRunner.findMethod("Rectangle", "getX1").enviousFragmentGroup().getElements().size(), 0);
	}
	@Test
	public void resultedNumberOfFragmentsForMethodCallLevelIsCorrect_Rectangle_getY1() {
		assertEquals(TestRunner.findMethod("Rectangle", "getY1").enviousFragmentGroup().getElements().size(), 0);
	}
	@Test
	public void resultedNumberOfFragmentsForMethodCallLevelIsCorrect_Rectangle_getX2() {
		assertEquals(TestRunner.findMethod("Rectangle", "getX2").enviousFragmentGroup().getElements().size(), 0);
	}
	@Test
	public void resultedNumberOfFragmentsForMethodCallLevelIsCorrect_Rectangle_getY2() {
		assertEquals(TestRunner.findMethod("Rectangle", "getY2").enviousFragmentGroup().getElements().size(), 0);
	}
}
