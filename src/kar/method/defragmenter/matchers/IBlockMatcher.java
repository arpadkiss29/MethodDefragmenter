package kar.method.defragmenter.matchers;

import kar.method.defragmenter.utils.CodeFragmentTreeNode;

public interface IBlockMatcher {
	
	public boolean tryToMatchBlocks(CodeFragmentTreeNode node);
	
}
