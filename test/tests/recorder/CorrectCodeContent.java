package tests.recorder;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import methoddefragmenter.metamodel.entity.MFragment;
import tests.TestRunner;

public class CorrectCodeContent {
	
	@Test
	public void correctCodeContent() {
		List<MFragment> mFragments = TestRunner.findClass("recorder.Recorder").enviousFragmentGroup().getElements();
		String[] correctValues = {
				"[if (o instanceof Circle) {\n"
				+ "  this.stream.writeInt(((Circle)o).getX());\n"
				+ "  this.stream.writeInt(((Circle)o).getY());\n"
				+ "  this.stream.writeInt(((Circle)o).getR());\n"
				+ "}\n"
				+ "]", 
				"[if (o instanceof Rectangle) {\n"
				+ "  this.stream.writeInt(((Rectangle)o).getX1());\n"
				+ "  this.stream.writeInt(((Rectangle)o).getY1());\n"
				+ "  this.stream.writeInt(((Rectangle)o).getX2());\n"
				+ "  this.stream.writeInt(((Rectangle)o).getY2());\n"
				+ "}\n"
				+ "]"};
		for (MFragment mFragment : mFragments) {
			assertEquals(
					"Error at fragment number " + mFragments.indexOf(mFragment) + "!", 
					mFragment.toString(), 
					correctValues[mFragments.indexOf(mFragment)]
			);
		}
	}
}
