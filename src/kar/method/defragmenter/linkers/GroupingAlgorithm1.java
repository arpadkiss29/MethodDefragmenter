package kar.method.defragmenter.linkers;

import kar.method.defragmenter.utils.AbstractCodeFragment;
import kar.method.defragmenter.utils.CodeFragmentLeaf;
import kar.method.defragmenter.utils.InternalCodeFragment;

public class GroupingAlgorithm1 implements IBlockLinker {

	private int ATFDTreshold;
	private int FDPTreshold;
	private boolean staticFields;
	private Integer minBlockSize;
	private String analyzedClass;
	private boolean libraryCheck;

	public GroupingAlgorithm1(int ATFDTreshold, int FDPTreshold, String analyzedClass, boolean staticFields, Integer minBlockSize, boolean libraryCheck) {
		super();
		this.ATFDTreshold = ATFDTreshold;
		this.FDPTreshold = FDPTreshold;
		this.staticFields = staticFields;
		this.minBlockSize = minBlockSize;
		this.analyzedClass = analyzedClass;
		this.libraryCheck = libraryCheck;
	}

	@Override
	public AbstractCodeFragment tryToLinkBlocks(AbstractCodeFragment node) {
		if (node instanceof CodeFragmentLeaf) {
			return node;
		} else {
			for (int i = 0; i < node.getChildrenSize(); i++) {
				if (node.getChild(i).isEnvy()) {
					InternalCodeFragment icf = new InternalCodeFragment();
					icf.addChild(node.getChild(i));
					int j = i + 1;
					boolean res = true;
					while (res && j < node.getChildrenSize()) {
						icf.addChild(node.getChild(j));
						res = icf.verifyFeatureEnvy(ATFDTreshold, FDPTreshold, analyzedClass, staticFields, minBlockSize, libraryCheck, true);
						j++;
					}
					if (i != j) {
						
					}
				}
			}
		}
		return null;
	}

}
