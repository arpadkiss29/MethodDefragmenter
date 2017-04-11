package kar.method.defragmenter.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.ITextEditor;

import kar.method.defragmenter.views.SelectionView;

public class CodeFragmentTreeNode {
	private static final double NCOCP2Treshold = 0.5;
	private static final double NCOCP2TresholdIdentif = 0.5;
	public static int colorCounter = 0;

	public static List<CodeFragmentTreeNode> allNodesLeafs = new ArrayList<CodeFragmentTreeNode>();
	private List<CodeFragmentTreeNode> children = null;
	private double nodeNCOCP2 = 0.0;
	private double nodeCOCP = 0.0;
	private int startNode = 0;
	private int endNode = 0;
	private List<CodeFragmentLeaf> leafsReceived = new ArrayList<CodeFragmentLeaf>(); 

	protected boolean possiblyRelatedFlag = false;
	protected List<CodeFragmentTreeNode> possiblyRelatedNodes = new ArrayList<CodeFragmentTreeNode>();

	public CodeFragmentTreeNode()
	{
		this.children = new ArrayList<>();
	}


	public void addChild(CodeFragmentTreeNode child)
	{
		children.add(child);
	}


	public int getChildrenSize(){
		return children.size();
	}


	private double calculateMetric(List<CodeFragmentLeaf> fragments){
		Map<IVariableBinding, Integer> variableOccuarenceInSet = new HashMap<IVariableBinding, Integer>();
		double cocp = 0;
		double ncopc2 = 0.0;
		int numberOfFragemts = fragments.size();
		for (int i = 0; i < fragments.size(); i++){
			HashSet<IVariableBinding> varFromSet = fragments.get(i).getMyVariables();
			for(IVariableBinding each: varFromSet){
				if (variableOccuarenceInSet.get(each) == null){
					variableOccuarenceInSet.put(each, 1);
				}else{
					variableOccuarenceInSet.put(each, variableOccuarenceInSet.get(each) + 1);
				}
			}
		}

		int sum = 0;
		int varNumber = variableOccuarenceInSet.entrySet().size();
		for(Map.Entry<IVariableBinding, Integer> entry: variableOccuarenceInSet.entrySet()){
			sum += entry.getValue();
		}
		cocp = (((double)1/numberOfFragemts) * ((double)1 / varNumber) * sum);
		ncopc2 = ((numberOfFragemts - ((double)1/cocp)) / (numberOfFragemts - 1));
		this.nodeCOCP = cocp;

		return ncopc2;

	}

	public List<CodeFragmentLeaf> getCohesionMetric(CompilationUnit unit){
//		List<CodeFragmentLeaf> receivedNodes = new ArrayList<CodeFragmentLeaf>();

		for (int i = 0; i < children.size(); i++){
			if (!(children.get(i) instanceof CodeFragmentLeaf)){ 
				leafsReceived.addAll(children.get(i).getCohesionMetric(unit));
				
			}
		}

		int numberOfFragemts = 0;
		numberOfFragemts = leafsReceived.size();
//		if (this.leafsReceived == null){
//			this.leafsReceived = new ArrayList<CodeFragmentLeaf>();
//		}else{
//			numberOfFragemts = this.leafsReceived.size();
//		}

		List<CodeFragmentLeaf> temp = new ArrayList<CodeFragmentLeaf>();
		temp.addAll(leafsReceived);

		for (int i = 0; i < children.size(); i++){
			if (children.get(i) instanceof CodeFragmentLeaf){
				temp.add((CodeFragmentLeaf)children.get(i));
				numberOfFragemts++;
			}
		}

		if (numberOfFragemts > 1){
			this.nodeNCOCP2 = calculateMetric(temp);

			calculteFirstLastLine();

			System.out.println("Currently in node: " + toString() + " COCP: " + this.nodeCOCP + " NCOCP2: " + this.nodeNCOCP2);
			System.out.println("From line : " + (unit.getLineNumber(startNode)) + " to line: " + (unit.getLineNumber(endNode)));
			System.out.println();

		}
		return temp;
	}

	private void calculteFirstLastLine(){
		for (int i=0; i<children.size(); i++){
			if (children.get(i) instanceof CodeFragmentLeaf){
				startNode = ((CodeFragmentLeaf)children.get(i)).getFragmentFirstLine();
				break;
			}
		}

		for (int i=children.size()-1; i>0; i--){
			if (children.get(i) instanceof CodeFragmentLeaf){
				endNode = ((CodeFragmentLeaf)children.get(i)).getFragmentLastLine();
				break;
			}
		}

		if(((startNode == 0) || (endNode ==0))  && leafsReceived.size() > 1){
			if (startNode == 0 && endNode == 0){
				startNode = children.get(0).getStartNode();
				endNode = children.get(children.size() - 1).getEndNode();
			} else if (startNode == 0){
				startNode = leafsReceived.get(0).getFragmentFirstLine();
			} else if (endNode == 0){
				endNode = leafsReceived.get(leafsReceived.size()-1).getFragmentLastLine();
			}

		}

	}


	
	public List<CodeFragmentTreeNode> identifyFunctionalSegments(){
		List<CodeFragmentTreeNode> receivedNodes = new ArrayList<CodeFragmentTreeNode>();
		for (int i = 0; i < children.size(); i++){
			receivedNodes.addAll(children.get(i).identifyFunctionalSegments());
		}
		if (this.nodeNCOCP2 >= NCOCP2TresholdIdentif){
			receivedNodes.clear();
			receivedNodes.add(this);
			return receivedNodes;
		}
		return receivedNodes;
	}
	
	public boolean isPossiblyRelatedFlag() {
		return possiblyRelatedFlag;
	}

	public void setPossiblyRelatedFlag(boolean possiblyRelatedFlag) {
		this.possiblyRelatedFlag = possiblyRelatedFlag;
	}
	
	public void combineNodes(List<CodeFragmentTreeNode> nodes){
		for(int i = 0; i < nodes.size() - 1; i++){
			for(int j = i + 1; j < nodes.size(); j++){
				List<CodeFragmentLeaf> fragmentsCombination = new ArrayList<CodeFragmentLeaf>();
		
				if (nodes.get(i) instanceof CodeFragmentLeaf){
					fragmentsCombination.add((CodeFragmentLeaf) nodes.get(i));
				} else{
					fragmentsCombination.addAll(nodes.get(i).getLeafsReceived());
					for(CodeFragmentTreeNode eachNode: nodes.get(i).children){
						if(eachNode instanceof CodeFragmentLeaf) fragmentsCombination.add((CodeFragmentLeaf) eachNode);
					}
				}
				
				if (nodes.get(j) instanceof CodeFragmentLeaf){
					fragmentsCombination.add((CodeFragmentLeaf) nodes.get(j));
				} else {
					fragmentsCombination.addAll(nodes.get(j).getLeafsReceived());
					for(CodeFragmentTreeNode eachNode: nodes.get(j).children){
						if(eachNode instanceof CodeFragmentLeaf) fragmentsCombination.add((CodeFragmentLeaf) eachNode);
					}
				}
				
				double combinationNCOPC2 = calculateMetric(fragmentsCombination);
				if(combinationNCOPC2 > NCOCP2Treshold){
					if (nodes.get(i) instanceof CodeFragmentLeaf && nodes.get(j) instanceof CodeFragmentLeaf){
						nodes.get(i).getPossiblyRelatedNodes().add(nodes.get(j));
						nodes.get(j).setPossiblyRelatedFlag(true);
					}else if (!(nodes.get(i) instanceof CodeFragmentLeaf)){
						nodes.get(j).setPossiblyRelatedFlag(true);
						nodes.get(i).getPossiblyRelatedNodes().add(nodes.get(j));
					}else if (!(nodes.get(j) instanceof CodeFragmentLeaf)){
						nodes.get(i).setPossiblyRelatedFlag(true);
						nodes.get(j).getPossiblyRelatedNodes().add(nodes.get(i));
					}
				}
			}
		}
	}
	


	public void colorFragemnts(ITextEditor textEditor, IFile file, List<CodeFragmentTreeNode> functionalSegmentNodes){
		for (int i = 0; i < children.size(); i++){ 
			children.get(i).colorFragemnts(textEditor, file, functionalSegmentNodes);
		}
		
		if(functionalSegmentNodes.contains(this) && (possiblyRelatedFlag != true)){
			try {
				if (colorCounter < 6){
					colorCounter++;						
					for(int i=0; i<children.size(); i++){
						if (children.get(i) instanceof CodeFragmentLeaf){
							int start = ((CodeFragmentLeaf)children.get(i)).getFragmentFirstLine();
							int end = ((CodeFragmentLeaf)children.get(i)).getFragmentLastLine();
							Position fragmentPosition = new Position(start, (end - start));
							IMarker mymarker = SelectionView.createMarker(file, fragmentPosition);
							SelectionView.addAnnotation(mymarker, textEditor, "com.ibm.example.myannotation" + colorCounter, fragmentPosition);
						}
					}
					for(int i=0; i<leafsReceived.size(); i++){
						int start = ((CodeFragmentLeaf)leafsReceived.get(i)).getFragmentFirstLine();
						int end = ((CodeFragmentLeaf)leafsReceived.get(i)).getFragmentLastLine();
						Position fragmentPosition = new Position(start, (end - start));
						IMarker mymarker = SelectionView.createMarker(file, fragmentPosition);
						SelectionView.addAnnotation(mymarker, textEditor, "com.ibm.example.myannotation" + colorCounter, fragmentPosition);

					}
					for(int i=0; i<possiblyRelatedNodes.size(); i++){
						if (possiblyRelatedNodes.get(i) instanceof CodeFragmentLeaf){
							int start = ((CodeFragmentLeaf)possiblyRelatedNodes.get(i)).getFragmentFirstLine();
							int end = ((CodeFragmentLeaf)possiblyRelatedNodes.get(i)).getFragmentLastLine();
							Position fragmentPosition = new Position(start, (end - start));
							IMarker mymarker = SelectionView.createMarker(file, fragmentPosition);
							SelectionView.addAnnotation(mymarker, textEditor, "com.ibm.example.myannotation" + colorCounter, fragmentPosition);
						}
					}

				}else{
					for(int i=0; i<children.size(); i++){
						if (children.get(i) instanceof CodeFragmentLeaf){
							int start = ((CodeFragmentLeaf)children.get(i)).getFragmentFirstLine();
							int end = ((CodeFragmentLeaf)children.get(i)).getFragmentLastLine();
							Position fragmentPosition = new Position(start, (end - start));
							IMarker mymarker = SelectionView.createMarker(file, fragmentPosition);
							SelectionView.addAnnotation(mymarker, textEditor, "com.ibm.example.myannotation6", fragmentPosition);
						}
					}
				}

			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

	}

	public void print(int tabs) {
		for(int i = 0; i < children.size(); i++){
			for(int j = 0; j < tabs+1; j++) System.out.print("\t");
			String child = children.get(i).toString();
			child = child.replace("\n", " ");
			System.out.print(child);
			System.out.println();
			children.get(i).print(tabs+1);
		}
		System.out.println();
	}

	public CodeFragmentTreeNode getAllTreeData(){
		for (int i = 0; i < children.size(); i++){	
			allNodesLeafs .add(children.get(i).getAllTreeData());
		}
		return this;
	}

	public double getNodeNCOCP2() {
		return nodeNCOCP2;
	}

	public double getNodeCOCP() {
		return nodeCOCP;
	}

	public void setNodeNCOCP2(double nodeNCOCP2) {
		this.nodeNCOCP2 = nodeNCOCP2;
	}

	public List<CodeFragmentLeaf> getLeafsReceived() {
		return leafsReceived;
	}


	public void setLeafsReceived(List<CodeFragmentLeaf> leafsReceived) {
		this.leafsReceived = leafsReceived;
	}

	public int getStartNode() {
		return startNode;
	}


	public void setStartNode(int startNode) {
		this.startNode = startNode;
	}


	public int getEndNode() {
		return endNode;
	}


	public void setEndNode(int endNode) {
		this.endNode = endNode;
	}


	public List<CodeFragmentTreeNode> getPossiblyRelatedNodes() {
		return possiblyRelatedNodes;
	}



}



