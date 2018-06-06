package kar.method.defragmenter.linkers;

import kar.method.defragmenter.utils.AbstractCodeFragment;

public interface IBlockLinker {
	
	public AbstractCodeFragment tryToLinkBlocks(AbstractCodeFragment node);
	
}
