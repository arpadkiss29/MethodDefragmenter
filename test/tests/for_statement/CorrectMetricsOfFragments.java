package tests.for_statement;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import methoddefragmenter.metamodel.entity.MFragment;
import tests.TestRunner;

public class CorrectMetricsOfFragments {
	
	@Test
	public void correctATFD() {
		List<MFragment> mFragments = TestRunner.findClass("three_letter_class.ForStatement").enviousFragmentGroup().getElements();
		int[] correctValues = {3, 3};
		for (MFragment mFragment : mFragments) {
			assertTrue(
					"Error at fragment number " + mFragments.indexOf(mFragment) + "!", 
					mFragment.aTFD() == correctValues[mFragments.indexOf(mFragment)]
			);
		}
	}
	
	@Test
	public void correctFDP() {
		List<MFragment> mFragments = TestRunner.findClass("three_letter_class.ForStatement").enviousFragmentGroup().getElements();
		int[] correctValues = {1, 1};
		for (MFragment mFragment : mFragments) {
			assertTrue(
					"Error at fragment number " + mFragments.indexOf(mFragment) + "!",
					mFragment.fDP() == correctValues[mFragments.indexOf(mFragment)]
			);
		}
	}
	
	@Test
	public void correctLAA() {
		List<MFragment> mFragments = TestRunner.findClass("three_letter_class.ForStatement").enviousFragmentGroup().getElements();
		double[] correctValues = {0.25, 0.0};
		for (MFragment mFragment : mFragments) {
			assertTrue(
					"Error at fragment number " + mFragments.indexOf(mFragment) + "!", 
					mFragment.lAA() == correctValues[mFragments.indexOf(mFragment)]
			);
		}
	}
}
