package tests;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.Test;

import methoddefragmenter.metamodel.entity.MFragment;
import methoddefragmenter.metamodel.entity.MMethod;
import ro.lrg.method.defragmenter.utils.Selector;

public class CorrectColoring {
	
	@Test
	public void correctColoring_Recorder_persist() {
		List<MFragment> mFragments = TestRunner.findMethod("recorder.Recorder", "persist").enviousFragmentGroup().getElements();
		mFragments.get(0).color();
		assertEquals("Error when coloring the first envious fragment of method Recorder.persist!", Selector.getColoredFragments().size(), 1);
		mFragments.get(0).color();
		assertEquals("Error when repeat the coloring of the first envious fragment of method Recorder.persist!", Selector.getColoredFragments().size(), 1);
		mFragments.get(0).uncolor();
		assertEquals("Error when uncoloring the first envious fragment of method Recorder.persist!", Selector.getColoredFragments().size(), 0);
		mFragments.get(0).uncolor();
		assertEquals("Error when repeat the uncoloring of the first envious fragment of method Recorder.persist!", Selector.getColoredFragments().size(), 0);
		
		mFragments.get(0).color();
		mFragments.get(1).color();
		assertEquals("Error when coloring the a different envious fragment of method Recorder.persist!", Selector.getColoredFragments().size(), 1);
		
	}
	
	@Test
	public void correctMultiColoring_Recorder_persist() {
		MMethod mMethod = TestRunner.findMethod("recorder.Recorder", "persist");
		mMethod.colorEnviousFragments();
		assertEquals("Error when coloring all envious fragments of method Recorder.persist!", Selector.getColoredFragments().size(), 2);
		mMethod.colorEnviousFragments();
		assertEquals("Error when repeat the coloring of all envious fragments of method Recorder.persist!", Selector.getColoredFragments().size(), 2);
		mMethod.uncolorEnviousFragments();
		assertEquals("Error when uncoloring all envious fragments of method Recorder.persist!", Selector.getColoredFragments().size(), 0);
		mMethod.uncolorEnviousFragments();
		assertEquals("Error when repeat the uncoloring of all envious fragments of method Recorder.persist!", Selector.getColoredFragments().size(), 0);
	}
	
	@Test
	public void correctCombinedColoring_Recorder_persist() {
		MMethod mMethod = TestRunner.findMethod("recorder.Recorder", "persist");
		List<MFragment> mFragments = TestRunner.findMethod("recorder.Recorder", "persist").enviousFragmentGroup().getElements();
		mFragments.get(0).color();
		mMethod.colorEnviousFragments();
		assertEquals("Error when coloring all envious fragments after coloring a single fragment!", Selector.getColoredFragments().size(), 2);
		mFragments.get(0).color();
		assertEquals("Error when coloring a single envious fragment after coloring all envious fragments!", Selector.getColoredFragments().size(), 1);
		mMethod.uncolorEnviousFragments();
		assertEquals("Error when uncoloring all envious fragments after coloring a single envious fragment!", Selector.getColoredFragments().size(), 0);
		
		mMethod.colorEnviousFragments();
		mFragments.get(0).uncolor();
		assertEquals("Error when uncoloring a envious fragment after coloring all envious fragments!", Selector.getColoredFragments().size(), 1);
	}
}
