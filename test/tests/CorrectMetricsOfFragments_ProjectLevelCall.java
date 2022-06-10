package tests;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import methoddefragmenter.metamodel.entity.MFragment;

public class CorrectMetricsOfFragments_ProjectLevelCall {
	
	@Test
	public void correctATFD_ProjectLevelCall() {
		List<MFragment> mFragments = TestRunner.getProject().enviousFragmentGroup().getElements();
		int[] correctValues = {3, 4};
		for (MFragment mFragment : mFragments) {
			assertTrue(
					"Error at fragment number " + mFragments.indexOf(mFragment) + "!", 
					mFragment.aTFD() == correctValues[mFragments.indexOf(mFragment)]
			);
		}
	}
	
	@Test
	public void correctFDP_ProjectLevelCall() {
		List<MFragment> mFragments = TestRunner.getProject().enviousFragmentGroup().getElements();
		int[] correctValues = {1, 1};
		for (MFragment mFragment : mFragments) {
			assertTrue(
					"Error at fragment number " + mFragments.indexOf(mFragment) + "!",
					mFragment.fDP() == correctValues[mFragments.indexOf(mFragment)]
			);
		}
	}
	
	@Test
	public void correctLAA_ProjectLevelCall() {
		List<MFragment> mFragments = TestRunner.getProject().enviousFragmentGroup().getElements();
		double[] correctValues = {0.25, 0.2};
		for (MFragment mFragment : mFragments) {
			assertTrue(
					"Error at fragment number " + mFragments.indexOf(mFragment) + "!", 
					mFragment.lAA() == correctValues[mFragments.indexOf(mFragment)]
			);
		}
	}
}
