package tests;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import methoddefragmenter.metamodel.entity.MClass;
import methoddefragmenter.metamodel.entity.MFragment;

public class CorrectForeignDataProviders_ProjectLevelCall {
	
	@Test
	public void correctForeignDataProviders_ProjectLevelCall() {
		List<MFragment> mFragments = TestRunner.getProject().enviousFragmentGroup().getElements();
		String[][] correctValues = {{"Circle"}, {"Rectangle"}};
		for (MFragment mFragment : mFragments) {
			List<MClass> FDPClasses = mFragment.foreignDataProviderClassGroup().getElements();
			for (MClass FDPClass : FDPClasses) {
				assertEquals(
						"Error at fragment number " + mFragments.indexOf(mFragment) + ", FDP class number " + FDPClasses.indexOf(FDPClass) + "!",
						FDPClass.getUnderlyingObject().getElementName(), 
						correctValues[mFragments.indexOf(mFragment)][FDPClasses.indexOf(FDPClass)]
				);
			};
		}
	}
}
