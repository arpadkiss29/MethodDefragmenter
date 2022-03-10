package ro.lrg.method.defragmenter.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.ITextEditor;

import ro.lrg.method.defragmenter.views.SelectionView;

public class InternalCodeFragment extends AbstractInternalCodeFragment {
	private final List<AbstractInternalCodeFragment> children = new ArrayList<>();
	private static final List<AbstractInternalCodeFragment> allNodesLeafs = new ArrayList<>();
	private static final double NCOCP2Treshold = 0.75;
	private double nodeNCOCP2 = 0.0;
	private double nodeCOCP = 0.0;
	
	private List<InternalCodeFragmentLeaf> leafsReceived = new ArrayList<InternalCodeFragmentLeaf>();
	
	private FixedStructureTypes type;
	
	public InternalCodeFragment(IFile iFile, IJavaProject iJavaProject) {
		super(iFile, iJavaProject);
	}
	
	public void calculteFirstLastLine() {
		if (children.size() > 0 && startNode == 0 && endNode == 0) {
			startNode = getFragmentFirstLineStartIndex();
			endNode = getFragmentLastLineEndIndex();
		} else {
			System.out.println("Already calculated!");
			System.out.println(this);
		}

		if (((startNode == 0) || (endNode == 0)) && leafsReceived.size() > 1) {
			if (startNode == 0 && endNode == 0) {
				startNode = children.get(0).startNode;
				endNode = children.get(children.size() - 1).getEndNode();
			} else if (startNode == 0) {
				startNode = leafsReceived.get(0).getFragmentFirstLineStartIndex();
			} else if (endNode == 0) {
				endNode = leafsReceived.get(leafsReceived.size() - 1).getFragmentLastLineEndIndex();
			}

		}

	}
	
	public void combineNodes(List<AbstractInternalCodeFragment> nodes) {
		for (int i = 0; i < nodes.size() - 1; i++) {
			for (int j = i + 1; j < nodes.size(); j++) {
				List<InternalCodeFragmentLeaf> fragmentsCombination = new ArrayList<InternalCodeFragmentLeaf>();

				if (nodes.get(i) instanceof InternalCodeFragmentLeaf) {
					fragmentsCombination.add((InternalCodeFragmentLeaf) nodes.get(i));
				} else {
					fragmentsCombination.addAll(((InternalCodeFragment) nodes.get(i)).getLeafsReceived());
					for (AbstractInternalCodeFragment eachNode : ((InternalCodeFragment) nodes.get(i)).children) {
						if (eachNode instanceof InternalCodeFragmentLeaf)
							fragmentsCombination.add((InternalCodeFragmentLeaf) eachNode);
					}
				}

				if (nodes.get(j) instanceof InternalCodeFragmentLeaf) {
					fragmentsCombination.add((InternalCodeFragmentLeaf) nodes.get(j));
				} else {
					fragmentsCombination.addAll(((InternalCodeFragment) nodes.get(j)).getLeafsReceived());
					for (AbstractInternalCodeFragment eachNode : ((InternalCodeFragment) nodes.get(j)).children) {
						if (eachNode instanceof InternalCodeFragmentLeaf)
							fragmentsCombination.add((InternalCodeFragmentLeaf) eachNode);
					}
				}

				double combinationNCOPC2 = calculateMetric(fragmentsCombination);
				if (combinationNCOPC2 > NCOCP2Treshold) {
					if (nodes.get(i) instanceof InternalCodeFragmentLeaf && nodes.get(j) instanceof InternalCodeFragmentLeaf) {
						nodes.get(i).getPossiblyRelatedNodes().add(nodes.get(j));
						nodes.get(j).setPossiblyRelatedFlag(true);
					} else if (!(nodes.get(i) instanceof InternalCodeFragmentLeaf)) {
						nodes.get(j).setPossiblyRelatedFlag(true);
						nodes.get(i).getPossiblyRelatedNodes().add(nodes.get(j));
					} else if (!(nodes.get(j) instanceof InternalCodeFragmentLeaf)) {
						nodes.get(i).setPossiblyRelatedFlag(true);
						nodes.get(j).getPossiblyRelatedNodes().add(nodes.get(i));
					}
				}
			}
		}
	}
	
	//private methods
	
	public List<InternalCodeFragmentLeaf> getCohesionMetric(CompilationUnit unit) {
		for (int i = 0; i < children.size(); i++) {
			if (children.get(i) instanceof InternalCodeFragment) {
				leafsReceived.addAll(((InternalCodeFragment) children.get(i)).getCohesionMetric(unit));
			}
		}
		int numberOfFragemts = 0;
		numberOfFragemts = leafsReceived.size();

		List<InternalCodeFragmentLeaf> temp = new ArrayList<InternalCodeFragmentLeaf>();
		temp.addAll(leafsReceived);

		for (int i = 0; i < children.size(); i++) {
			if (children.get(i) instanceof InternalCodeFragmentLeaf) {
				temp.add((InternalCodeFragmentLeaf) children.get(i));
				numberOfFragemts++;
			}
		}

		if (numberOfFragemts > 1) {
			nodeNCOCP2 = calculateMetric(temp);
			calculteFirstLastLine();
			System.out.println("Currently in node: " + toString() + " COCP: " + nodeCOCP + " NCOCP2: " + nodeNCOCP2);
			System.out.println("From line : " + (unit.getLineNumber(startNode)) + " to line: " + (unit.getLineNumber(endNode)));
			System.out.println();
		}
		return temp;
	}
	
	private double calculateMetric(List<InternalCodeFragmentLeaf> leafs) {
		Map<IVariableBinding, Integer> variableOccuarenceInSet = new HashMap<IVariableBinding, Integer>();
		double cocp = 0;
		double ncopc2 = 0.0;
		int numberOfFragments = leafs.size();
		for (InternalCodeFragmentLeaf leaf : leafs) {
			List<IVariableBinding> varFromSet = leaf.getVariablesBindings();
			for (IVariableBinding each : varFromSet) {
				if (variableOccuarenceInSet.get(each) == null) {
					variableOccuarenceInSet.put(each, 1);
				} else {
					variableOccuarenceInSet.put(each, variableOccuarenceInSet.get(each) + 1);
				}
			}
		}

		int sum = 0;
		int varNumber = variableOccuarenceInSet.entrySet().size();
		for (Map.Entry<IVariableBinding, Integer> entry : variableOccuarenceInSet.entrySet()) {
			sum += entry.getValue();
		}
		cocp = (((double) 1 / numberOfFragments) * ((double) 1 / varNumber) * sum);
		ncopc2 = ((numberOfFragments - ((double) 1 / cocp)) / (numberOfFragments - 1));
		nodeCOCP = cocp;

		return ncopc2;
	}
	
	//override abstract methods
	
	@Override
	public void clearData() {
		accessClassesMapping.clear();
		ATFD = 0;
		FDP = 0;
		LAA = 0;
		for (AbstractInternalCodeFragment child : children) {
			child.clearData();
		}
	}
	
	@Override
	public void colorEnvyLeafNodes(ITextEditor textEditor, IFile file) throws CoreException {
		for (AbstractInternalCodeFragment child : children) {
			child.colorEnvyLeafNodes(textEditor, file);
		}
	}
	
	@Override
	public void colorLongMethodFragments(ITextEditor textEditor, IFile file, List<AbstractInternalCodeFragment> functionalSegmentNodes) {
		for (AbstractInternalCodeFragment child : children) {
			child.colorLongMethodFragments(textEditor, file, functionalSegmentNodes);
		}
		if (functionalSegmentNodes.contains(this) && (possiblyRelatedFlag != true)) {
			try {
				if (colorCounter < 17) {
					colorCounter++;
					for (AbstractInternalCodeFragment child : children) {
						if (child instanceof InternalCodeFragmentLeaf) {
							int start = child.getFragmentFirstLineStartIndex();
							int end = child.getFragmentLastLineEndIndex();
							Position fragmentPosition = new Position(start, (end - start));
							IMarker mymarker = SelectionView.createMarker(file, fragmentPosition);
							SelectionView.addAnnotation(mymarker, textEditor, "annotationColor_" + colorCounter, fragmentPosition);
						}
					}
					for (InternalCodeFragmentLeaf leafReceived : leafsReceived) {
						int start = leafReceived.getFragmentFirstLineStartIndex();
						int end = leafReceived.getFragmentLastLineEndIndex();
						Position fragmentPosition = new Position(start, (end - start));
						IMarker mymarker = SelectionView.createMarker(file, fragmentPosition);
						SelectionView.addAnnotation(mymarker, textEditor, "annotationColor_" + colorCounter, fragmentPosition);
					}
					for (AbstractInternalCodeFragment cohesivlyRelatedNode : cohesivlyRelatedNodes) {
						if (cohesivlyRelatedNode instanceof InternalCodeFragmentLeaf) {
							int start = cohesivlyRelatedNode.getFragmentFirstLineStartIndex();
							int end = cohesivlyRelatedNode.getFragmentLastLineEndIndex();
							Position fragmentPosition = new Position(start, (end - start));
							IMarker mymarker = SelectionView.createMarker(file, fragmentPosition);
							SelectionView.addAnnotation(mymarker, textEditor, "annotationColor_" + colorCounter, fragmentPosition);
						}
					}
				} else {
					for (AbstractInternalCodeFragment child : children) {
						if (child instanceof InternalCodeFragmentLeaf) {
							int start = child.getFragmentFirstLineStartIndex();
							int end = child.getFragmentLastLineEndIndex();
							Position fragmentPosition = new Position(start, (end - start));
							IMarker mymarker = SelectionView.createMarker(file, fragmentPosition);
							SelectionView.addAnnotation(mymarker, textEditor, "annotationColor_17", fragmentPosition);
						}
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void computeDataAccesses(String analyzedClass, boolean considerStaticFieldAccess, boolean libraryCheck, Integer minBlockSize) {
		super.computeDataAccessesAux(analyzedClass, considerStaticFieldAccess, libraryCheck, minBlockSize);
	}
	
	@Override
	public AbstractInternalCodeFragment constructTree() {
		for (AbstractInternalCodeFragment child : children) {
			allNodesLeafs.add(child.constructTree());
		}
		return this;
	}
	
	@Override
	public List<AbstractInternalCodeFragment> getAllEnviousNodes() {
		List<AbstractInternalCodeFragment> nodes = new ArrayList<AbstractInternalCodeFragment>();
		for (AbstractInternalCodeFragment child : children) {
			if (child.isEnvy()) {
				nodes.add(child);
			} else {
				nodes.addAll(child.getAllEnviousNodes());
			}
		}
		return nodes;
	}
	
	@Override
	public List<ASTNode> getAllInternalStatements() {
		List<ASTNode> allInternalStatements = new ArrayList<ASTNode>();
		for (AbstractInternalCodeFragment child : children) {
			allInternalStatements.addAll(child.getAllInternalStatements());
		}
		allInternalStatements.addAll(getInternalStatements());
		return allInternalStatements;
	}
	
	@Override
	public int getFragmentFirstLineStartIndex() {
		AbstractInternalCodeFragment firstChild = children.get(0);
		if (firstChild instanceof InternalCodeFragmentLeaf) {
			return firstChild.getFragmentFirstLineStartIndex();
		} else {
			int tmp = firstChild.getFragmentFirstLineStartIndex();
			if (startNode != 0 && startNode < tmp)
				tmp = startNode;
			if (getInternalStatementsSize() > 0) {
				int tmp1 = getInternalStatement(0).getStartPosition();
				// if(internalASTNodes.get(0) instanceof Expression){
				// tmp1 -= 4;
				// }
				if (tmp1 < tmp) tmp = tmp1;
			}
			return tmp;
		}
	}
	
	@Override
	public int getFragmentLastLineEndIndex() {
		AbstractInternalCodeFragment lastChild = children.get(children.size() - 1);
		if (lastChild instanceof InternalCodeFragmentLeaf) {
			return lastChild.getFragmentLastLineEndIndex();
		} else {
			int tmp = lastChild.getFragmentLastLineEndIndex();
			if (endNode != 0 && endNode > tmp)
				tmp = endNode;
			if (getInternalStatementsSize() > 0) {
				int tmp1 = getInternalStatement(getInternalStatementsSize() - 1).getStartPosition()
						+ getInternalStatement(getInternalStatementsSize() - 1).getLength();
				if (tmp1 > tmp) tmp = tmp1;
			}
			// if(type != null && type.equals(FixedStructureTypes.IF)){
			// //System.out.println("IF");
			// tmp += 11;
			// }
			return tmp;
		}
	}
	
	@Override
	public List<AbstractInternalCodeFragment> identifyFunctionalSegments() {
		List<AbstractInternalCodeFragment> receivedNodes = new ArrayList<>();
		for (AbstractInternalCodeFragment child : children) {
			receivedNodes.addAll(child.identifyFunctionalSegments());
		}
		if (this.nodeNCOCP2 >= NCOCP2Treshold) {
			receivedNodes.clear();
			receivedNodes.add(this);
			return receivedNodes;
		}
		return receivedNodes;
	}
	
	@Override
	public void init() {
		initAux();
		allNodesLeafs.clear();
		nodeNCOCP2 = 0;
		nodeCOCP = 0;
		leafsReceived.clear();
	}
	
	@Override
	public void print(int tabs) {
		for (int j = 0; j < tabs; j++)
			System.out.print("\t");
		System.out.println(toString().replaceAll("\n", ""));
		for (AbstractInternalCodeFragment child : children) {
			child.print(tabs + 1);
		}
	}
	
	@Override
	public boolean verifyFeatureEnvy(int ATFDTreshold, int FDPTreshold, double LAATreshold, String analyzedClass,
			boolean considerStaticFieldAccess, boolean libraryCheck, Integer minBlockSize, boolean local) {
		boolean containsEnvy = false;
		for (AbstractInternalCodeFragment child : children) {
			if (child.verifyFeatureEnvy(ATFDTreshold, FDPTreshold, LAATreshold, analyzedClass,
					considerStaticFieldAccess, libraryCheck, minBlockSize, local)) {
				containsEnvy = true;
			}
		}
		return containsEnvy;
	}

	//accessory methods
	
	public void addChild(AbstractInternalCodeFragment child) {
		children.add(child);
	}
	
	public void addChild(int index, AbstractInternalCodeFragment child) {
		children.add(index, child);
	}

	public static List<AbstractInternalCodeFragment> getAllNodesLeafs() {
		return allNodesLeafs;
	}

	public AbstractInternalCodeFragment getChild(int i) {
		return children.get(i);
	}

	public List<AbstractInternalCodeFragment> getChildren() {
		return children;
	}
	
	public int getChildrenSize() {
		return children.size();
	}
	
	public List<InternalCodeFragmentLeaf> getLeafsReceived() {
		return leafsReceived;
	}
	
	public static double getNcocp2treshold() {
		return NCOCP2Treshold;
	}
	
	public double getNodeCOCP() {
		return nodeCOCP;
	}

	public double getNodeNCOCP2() {
		return nodeNCOCP2;
	}
	
	public FixedStructureTypes getType() {
		return type;
	}
	
	public boolean isEmptyNode() {
		return children.size() == 0;
	}
	
	public boolean removeAllChildren(List<AbstractInternalCodeFragment> childElements) {
		return children.removeAll(childElements);
	}
	
	public int removeChild(AbstractInternalCodeFragment child) {
		int index = children.indexOf(child);
		children.remove(child);
		return index;
	}
	
	public void setNodeNCOCP2(double nodeNCOCP2) {
		this.nodeNCOCP2 = nodeNCOCP2;
	}
	
	public void setLeafsReceived(List<InternalCodeFragmentLeaf> leafsReceived) {
		this.leafsReceived = leafsReceived;
	}
		
	public void setType(FixedStructureTypes type) {
		this.type = type;
	}
}
