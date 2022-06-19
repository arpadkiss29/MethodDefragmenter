package tests.do_while_statement;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import methoddefragmenter.metamodel.entity.MClass;
import methoddefragmenter.metamodel.entity.MFragment;
import tests.TestRunner;

public class CorrectForeignDataProviders {
	
	@Test
	public void correctForeignDataProviders_ProjectLevelCall() {
		List<MFragment> mFragments = TestRunner.findClass("three_letter_class.DoWhileStatement").enviousFragmentGroup().getElements();
		String[][] correctValues = {{"three_letter_class.data.ABC"}, {"three_letter_class.data.DEF"}};
		for (MFragment mFragment : mFragments) {
			List<MClass> FDPClasses = mFragment.foreignDataProviderClassGroup().getElements();
			for (MClass FDPClass : FDPClasses) {
				assertEquals(
						"Error at fragment number " + mFragments.indexOf(mFragment) + ", FDP class number " + FDPClasses.indexOf(FDPClass) + "!",
						FDPClass.getUnderlyingObject().getFullyQualifiedName(), 
						correctValues[mFragments.indexOf(mFragment)][FDPClasses.indexOf(FDPClass)]
				);
			};
		}
	}
}
