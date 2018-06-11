package kar.method.defragmenter.linkers;

import java.util.ArrayList;
import java.util.Stack;

import kar.method.defragmenter.utils.AbstractCodeFragment;
import kar.method.defragmenter.utils.CodeFragmentLeaf;
import kar.method.defragmenter.utils.InternalCodeFragment;

public class GroupingAlgorithm2 implements IBlockLinker {

	private int ATFDTreshold;
	private int FDPTreshold;
	private boolean staticFields;
	private Integer minBlockSize;
	private String analyzedClass;
	private boolean libraryCheck;
		
	private Stack<Boolean> hasAllChildrenEnvious = new Stack<Boolean>();
	
	public GroupingAlgorithm2(int ATFDTreshold, int FDPTreshold, String analyzedClass, boolean staticFields, Integer minBlockSize, boolean libraryCheck) {
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
			
			hasAllChildrenEnvious.push(node.isEnvy());
			return node;
			
		} else {
			
			ArrayList<Boolean> tmpEnv = new ArrayList<Boolean>();	
			for (int i = 0; i < node.getChildrenSize(); i++) {				
				tryToLinkBlocks(node.getChild(i));
				tmpEnv.add(hasAllChildrenEnvious.pop());
			}

			for (int i = 0; i < node.getChildrenSize(); i++) {				
				AbstractCodeFragment aChild = node.getChild(i);
				if (tmpEnv.get(i))
				{
					InternalCodeFragment icf = new InternalCodeFragment();
					icf.addChild(aChild);

					int forward = i;
					int backward = i;

					boolean testEnvy = true;
					while(testEnvy && backward - 1 >= 0) {
						backward--;
						icf.addChild(0, node.getChild(backward));
						if(!icf.verifyFeatureEnvy(ATFDTreshold, FDPTreshold, analyzedClass, staticFields, minBlockSize, libraryCheck, true)){
							backward++;
							testEnvy = false;
							icf.removeChild(node.getChild(backward));
						}
					}

					testEnvy = true;
					while(testEnvy && forward + 1 < node.getChildrenSize()) {
						forward++;
						icf.addChild(node.getChild(forward));
						if(!icf.verifyFeatureEnvy(ATFDTreshold, FDPTreshold, analyzedClass, staticFields, minBlockSize, libraryCheck, true)){
							forward--;
							testEnvy = false;
							icf.removeChild(node.getChild(forward));
						}
					}

					if (icf.getChildren().size() > 1) {
						for(int j = 0; j < icf.getChildren().size(); j++)
						{
							int ind = ((InternalCodeFragment)node).removeChild(icf.getChildren().get(j));
							tmpEnv.remove(ind);
						}	
						icf.setEnvy(true);
						icf.calculteFirstLastLine();
						node.addChild(icf);
						tmpEnv.add(true);
						i = backward;
					}
				}
			}	
				
			boolean allChildEnvious = true;
			for (int i = 0; i < tmpEnv.size(); i++) {
				allChildEnvious &= tmpEnv.get(i);
			}
			hasAllChildrenEnvious.push(allChildEnvious);
			return node;
		}
	}
}
