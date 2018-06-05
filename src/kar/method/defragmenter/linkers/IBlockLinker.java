package kar.method.defragmenter.linkers;

import kar.method.defragmenter.utils.AbstractCodeFragment;

public interface IBlockLinker {
	
	public boolean tryToLinkBlocks(AbstractCodeFragment node);
	
}
