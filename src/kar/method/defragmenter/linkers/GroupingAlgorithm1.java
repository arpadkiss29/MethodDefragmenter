package kar.method.defragmenter.linkers;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

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
	
	public Stack<AbstractCodeFragment> fullEnviousNodes = new Stack<AbstractCodeFragment>();
	public boolean expand = false;
	
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
			boolean allChildrenEnvious = true;
			
			for (int i = 0; i < node.getChildrenSize(); i++) {
				if (!node.getChild(i).isEnvy()) {
					allChildrenEnvious = false;
				}
				tryToLinkBlocks(node.getChild(i));
				
//				if (node.getChild(i).isEnvy()) {
//					InternalCodeFragment icf = new InternalCodeFragment();
//					icf.addChild(node.getChild(i));
//					int j = i + 1;
//					boolean res = true;
//					while (res && j < node.getChildrenSize()) {
//						icf.addChild(node.getChild(j));
//						res = icf.verifyFeatureEnvy(ATFDTreshold, FDPTreshold, analyzedClass, staticFields, minBlockSize, libraryCheck, true);
//						j++;
//					}
//					if (i != j) {
//						
//					}
//				}
			}
			if(allChildrenEnvious){
				// try expanding
				fullEnviousNodes.push(node);
			}
			if(expand){
				System.out.println("current node");
				System.out.println(node);
				expand = false;
				
				if(!fullEnviousNodes.isEmpty()){
					// presume its only one..
					AbstractCodeFragment focused = fullEnviousNodes.pop();
					int nodeIndex = -1;
					for(int i = 0; i < node.getChildrenSize(); i++){
						if(node.getChild(i).equals(focused)){
							nodeIndex = i;
							break;
						}
					}
					if(nodeIndex != -1){
						System.out.println("Expanding from: " + nodeIndex);
						
						InternalCodeFragment icf = new InternalCodeFragment();
					
						
						List<AbstractCodeFragment> toDelete = new ArrayList<AbstractCodeFragment>();
						
						int forward = nodeIndex;
						int backward = nodeIndex;
						
						boolean testEnvy = true;
						while(forward + 1 < node.getChildrenSize() && testEnvy){
							forward++;
							icf.addChild(node.getChild(forward));
							if(!icf.verifyFeatureEnvy(ATFDTreshold, FDPTreshold, analyzedClass, staticFields, minBlockSize, libraryCheck, true)){
								testEnvy = false;
								icf.removeChild(node.getChild(forward));
							}else{
								toDelete.add(node.getChild(forward));
							}
						}
								
						testEnvy = true;
						while(backward - 1 >= 0 && testEnvy){
							backward--;
							icf.addChild(node.getChild(backward));
							if(!icf.verifyFeatureEnvy(ATFDTreshold, FDPTreshold, analyzedClass, staticFields, minBlockSize, libraryCheck, true)){
								testEnvy = false;
								icf.removeChild(node.getChild(backward));
							}else{
								toDelete.add(node.getChild(backward));
							}
						}
						toDelete.add(focused);
						icf.addChild(focused);
						if(!toDelete.isEmpty()){
							node.removeChildren(toDelete);
							toDelete.clear();
							icf.setEnvy(true);
							icf.calculteFirstLastLine();
							node.addChild(icf);
							
						}
					}
				}
				
			}else if(!fullEnviousNodes.isEmpty() && fullEnviousNodes.size() == node.getChildrenSize()){
				boolean fullMatch = true;
				for (int i = 0; i < node.getChildrenSize(); i++) {
					if(!fullEnviousNodes.contains(node.getChild(i))){
						fullMatch = false;
					}
				}
				if(fullMatch && !expand){
					fullEnviousNodes.clear();
					fullEnviousNodes.push(node);
					expand = true;
				}
				
			}
		}
		return node;
	}

}
