package tests.recorder;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import methoddefragmenter.metamodel.entity.MClass;
import methoddefragmenter.metamodel.entity.MFragment;
import tests.TestRunner;

public class CorrectForeignDataProviders {
	
	@Test
	public void correctForeignDataProviders() {
		List<MFragment> mFragments = TestRunner.findClass("recorder.Recorder").enviousFragmentGroup().getElements();
		String[][] correctValues = {{"recorder.data.Circle"}, {"recorder.data.Rectangle"}};
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
