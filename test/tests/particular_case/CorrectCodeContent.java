package tests.particular_case;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import methoddefragmenter.metamodel.entity.MFragment;
import tests.TestRunner;

public class CorrectCodeContent {
	
	@Test
	public void correctCodeContent() {
		List<MFragment> mFragments = TestRunner.findClass("three_letter_class.ParticularCase").enviousFragmentGroup().getElements();
		String[] correctValues = {
				"[ABC abc=new ABC();\n"
				+ ", abc.setA(n);\n"
				+ ", abc.setB(n);\n"
				+ ", abc.setC(n);\n"
				+ ", int new_n=n;\n"
				+ ", for (int i=1; i <= k; i++) {\n"
				+ "  new_n*=2;\n"
				+ "  new_n++;\n"
				+ "}\n"
				+ ", int a=abc.getA();\n"
				+ ", int b=abc.getB();\n"
				+ ", int c=abc.getC();\n"
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
