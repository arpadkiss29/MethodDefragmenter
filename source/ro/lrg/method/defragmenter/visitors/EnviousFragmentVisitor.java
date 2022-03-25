package ro.lrg.method.defragmenter.visitors;

import java.util.ArrayList;
import java.util.List;

import ro.lrg.method.defragmenter.utils.InternalCodeFragment;
import ro.lrg.method.defragmenter.utils.InternalCodeFragmentLeaf;

public class EnviousFragmentVisitor implements FragmentVisitor {
	
	List<InternalCodeFragment> enviousFragment = new ArrayList<>();

	@Override
	public void visit(InternalCodeFragment fragment) {
		// TODO Auto-generated method stub
	}

	@Override
	public void visit(InternalCodeFragmentLeaf fragment) {
		// TODO Auto-generated method stub
	}
	
	public List<InternalCodeFragment> getEnviousFragments() {
		return enviousFragment;
	}
}
