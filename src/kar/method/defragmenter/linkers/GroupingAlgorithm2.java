package kar.method.defragmenter.linkers;

import java.util.ArrayList;
import java.util.Set;
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
			ArrayList<Set<String>> tmpEnviedClasses = new ArrayList<Set<String>>();	
			for (int i = 0; i < node.getChildrenSize(); i++) {				
				tryToLinkBlocks(node.getChild(i));
				tmpEnv.add(hasAllChildrenEnvious.pop());
				tmpEnviedClasses.add(node.getChild(i).getAccessClassesMapping().keySet());
			}

			for (int i = 0; i < node.getChildrenSize(); i++) {				
				AbstractCodeFragment aChild = node.getChild(i);
				if (tmpEnv.get(i))
				{
					Set<String> enviedClasses = aChild.getAccessClassesMapping().keySet();
					InternalCodeFragment icf = new InternalCodeFragment();
					icf.addChild(aChild);

					int forward = i;
					int backward = i;

					boolean testEnvy = true;
					while(testEnvy && backward - 1 >= 0) {
						backward--;
						icf.addChild(0, node.getChild(backward));
						if(!icf.verifyFeatureEnvy(ATFDTreshold, FDPTreshold, analyzedClass, staticFields, minBlockSize, libraryCheck, true)
							|| !enviedClasses.containsAll(icf.getAccessClassesMapping().keySet())) {
							backward++;
							testEnvy = false;
							icf.removeChild(node.getChild(backward));
						}
					}

					testEnvy = true;
					while(testEnvy && forward + 1 < node.getChildrenSize()) {
						forward++;
						icf.addChild(node.getChild(forward));
						if(!icf.verifyFeatureEnvy(ATFDTreshold, FDPTreshold, analyzedClass, staticFields, minBlockSize, libraryCheck, true)
							|| !enviedClasses.containsAll(icf.getAccessClassesMapping().keySet())){
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
							tmpEnviedClasses.remove(ind);
						}	
						icf.setEnvy(true);
						icf.calculteFirstLastLine();
						node.addChild(icf);
						tmpEnv.add(true);
						tmpEnviedClasses.add(icf.getAccessClassesMapping().keySet());
						i = backward;
					}
				}
			}	
				
			boolean allChildEnvious = true;
			Set<String> enviousFixed = null;
			for (int i = 0; i < tmpEnv.size(); i++) {
				allChildEnvious &= tmpEnv.get(i);
				if (tmpEnv.get(i)) {
					if (enviousFixed == null) {
						enviousFixed = tmpEnviedClasses.get(i);
					}
				}
			}
			if (allChildEnvious) {
				node.verifyFeatureEnvy(ATFDTreshold, FDPTreshold, analyzedClass, staticFields, minBlockSize, libraryCheck, true);
				if (node.isEnvy() && enviousFixed.containsAll(node.getAccessClassesMapping().keySet())) {
					node.calculteFirstLastLine();
				} else {
					node.setEnvy(false);
				}
				hasAllChildrenEnvious.push(node.isEnvy());
			} else {
				hasAllChildrenEnvious.push(false);
			}
			return node;
		}
	}
}
