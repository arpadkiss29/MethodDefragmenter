package tests.switch_statement;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import methoddefragmenter.metamodel.entity.MFragment;
import tests.TestRunner;

public class CorrectCodeContent {
	
	@Test
	public void correctCodeContent() {
		List<MFragment> mFragments = TestRunner.findClass("three_letter_class.SwitchStatement").enviousFragmentGroup().getElements();
		String[] correctValues = {
				"[ABC abc=new ABC();\n"
				+ ", abc.setA(n);\n"
				+ ", abc.setB(n);\n"
				+ ", abc.setC(n);\n"
				+ ", i=i < 0 ? -1 : i == 0 ? 0 : 1;\n"
				+ ", int s=0;\n"
				+ ", switch (i) {\n"
				+ "case -1:\n"
				+ "  s+=3 * abc.getA();\n"
				+ "break;\n"
				+ "case 0:\n"
				+ "s+=3 * abc.getB();\n"
				+ "break;\n"
				+ "case 1:\n"
				+ "s+=3 * abc.getC();\n"
				+ "}\n"
				+ "]",
				"[def.setD(s);\n"
				+ ", def.setE(s);\n"
				+ ", def.setF(s);\n"
				+ "]"
		};
		for (MFragment mFragment : mFragments) {
			assertEquals(
					"Error at fragment number " + mFragments.indexOf(mFragment) + "!", 
					mFragment.toString(), 
					correctValues[mFragments.indexOf(mFragment)]
			);
		}
	}
}
