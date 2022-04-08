package ro.lrg.method.defragmenter.views;

import java.util.ArrayList;
import java.util.List;

import methoddefragmenter.metamodel.entity.MFragment;

public class ColouredFragments {
	private static int colorCounter = 0;
	private static List<MFragment> colouredFragments = new ArrayList<>();
	private static boolean colorMultipleFragments = false;
	
	public static void addFragment(MFragment fragment) {
		colouredFragments.add(fragment);
	}
	
	public static boolean isColorMultipleFragments() {
		return colorMultipleFragments;
	}
	
	public static void setColorMultipleFragments(boolean colorMultipleFragments) {
		ColouredFragments.colorMultipleFragments = colorMultipleFragments;
	}
}
