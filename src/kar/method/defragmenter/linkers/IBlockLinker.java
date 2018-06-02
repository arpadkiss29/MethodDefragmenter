package kar.method.defragmenter.linkers;

import kar.method.defragmenter.utils.CodeFragmentTreeNode;

public interface IBlockLinker {
	
	public boolean tryToLinkBlocks(CodeFragmentTreeNode node);
	
}
