package ro.lrg.method.defragmenter.views;

import java.util.ArrayList;
import java.util.List;

import methoddefragmenter.metamodel.entity.MFragment;

public class ColouredFragments {
	private static List<MFragment> colouredFragments = new ArrayList<>();
	public static void addFragment(MFragment fragment) {
		colouredFragments.add(fragment);
	}
}
