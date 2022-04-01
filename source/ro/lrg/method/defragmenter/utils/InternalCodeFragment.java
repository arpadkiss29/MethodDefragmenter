package ro.lrg.method.defragmenter.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.ITextEditor;

import ro.lrg.method.defragmenter.views.SelectionView;
import ro.lrg.method.defragmenter.visitors.fragment.FragmentVisitor;

public class InternalCodeFragment extends AbstractInternalCodeFragment {
	private final List<AbstractInternalCodeFragment> children = new ArrayList<>();
	
	private static final List<AbstractInternalCodeFragment> allNodesLeafs = new ArrayList<>();
	private static final double NCOCP2Treshold = 0.75;
	private double nodeNCOCP2 = 0.0;
	private double nodeCOCP = 0.0;
	private List<InternalCodeFragmentLeaf> leafsReceived = new ArrayList<>();
	
	public InternalCodeFragment(String analizedClass, IFile iFile, IJavaProject iJavaProject) {
		super(analizedClass, iFile, iJavaProject);
	}
	
	//--------------------------------------------------------------------------overrode methods
	@Override
	public void accept(FragmentVisitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public void colorFragment(ITextEditor textEditor, IFile file) throws CoreException {
		for (AbstractInternalCodeFragment child : children) {
			child.colorFragment(textEditor, file);
		}
	}
	
	@Override
	public int getFragmentFirstLineStartIndex() {
		AbstractInternalCodeFragment firstChild = children.get(0);
		if (firstChild instanceof InternalCodeFragmentLeaf) {
			return firstChild.getFragmentFirstLineStartIndex();
		} else {
			int tmp = firstChild.getFragmentFirstLineStartIndex();
			if (startPosition != 0 && startPosition < tmp)
				tmp = startPosition;
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
			if (endPosition != 0 && endPosition > tmp)
				tmp = endPosition;
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
	
	//--------------------------------------------------------------------------children related methods
	public void addChild(AbstractInternalCodeFragment child) {
		if(child == null) {
			System.err.println("Found null child!");
			return;
		}
		children.add(child);
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

	//--------------------------------------------------------------------------NCOCP2
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
			System.out.println("From line : " + (unit.getLineNumber(startPosition)) + " to line: " + (unit.getLineNumber(endPosition)));
			System.out.println();
		}
		return temp;
	}
	//--------------------------------------------------------------------------NCOCP2: overrode methods
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
	public AbstractInternalCodeFragment constructTree() {
		for (AbstractInternalCodeFragment child : children) {
			allNodesLeafs.add(child.constructTree());
		}
		return this;
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
	//--------------------------------------------------------------------------NCOCP2: private methods
	private void calculteFirstLastLine() {
		if (children.size() > 0 && startPosition == 0 && endPosition == 0) {
			startPosition = getFragmentFirstLineStartIndex();
			endPosition = getFragmentLastLineEndIndex();
		} else {
			System.out.println("Already calculated!");
			System.out.println(this);
		}

		if (((startPosition == 0) || (endPosition == 0)) && leafsReceived.size() > 1) {
			if (startPosition == 0 && endPosition == 0) {
				startPosition = children.get(0).startPosition;
				endPosition = children.get(children.size() - 1).getEndPosition();
			} else if (startPosition == 0) {
				startPosition = leafsReceived.get(0).getFragmentFirstLineStartIndex();
			} else if (endPosition == 0) {
				endPosition = leafsReceived.get(leafsReceived.size() - 1).getFragmentLastLineEndIndex();
			}
		}
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
	//--------------------------------------------------------------------------NCOCP2: getters and setters
	public static List<AbstractInternalCodeFragment> getAllNodesLeafs() {
		return allNodesLeafs;
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
	public void setNodeNCOCP2(double nodeNCOCP2) {
		this.nodeNCOCP2 = nodeNCOCP2;
	}
	public void setLeafsReceived(List<InternalCodeFragmentLeaf> leafsReceived) {
		this.leafsReceived = leafsReceived;
	}
}
