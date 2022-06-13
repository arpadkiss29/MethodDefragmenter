package tests.while_statement;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import methoddefragmenter.metamodel.entity.MFragment;
import tests.TestRunner;

public class CorrectCodeContent {
	
	@Test
	public void correctCodeContent() {
		List<MFragment> mFragments = TestRunner.findClass("three_letter_class.WhileStatement").enviousFragmentGroup().getElements();
		String[] correctValues = {
				"[ABC abc=new ABC();\n"
				+ ", abc.setA(n);\n"
				+ ", abc.setB(n);\n"
				+ ", abc.setC(n);\n"
				+ ", int s=0;\n"
				+ ", int i=1;\n"
				+ ", while (i++ <= n) {\n"
				+ "  s=2;\n"
				+ "  s+=abc.getA() + abc.getB() + abc.getC();\n"
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
