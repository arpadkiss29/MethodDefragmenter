package ro.lrg.method.defragmenter.utils;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.ITextEditor;

import ro.lrg.method.defragmenter.visitors.fragment.FragmentVisitor;

public class InternalCodeFragment extends AbstractInternalCodeFragment {
	private final List<AbstractInternalCodeFragment> children = new ArrayList<>();
	
	private List<InternalCodeFragmentLeaf> leavesReceived = new ArrayList<>();
	
	public InternalCodeFragment(String analizedClass, IFile iFile, IJavaProject iJavaProject) {
		super(analizedClass, iFile, iJavaProject);
	}
	
	//--------------------------------------------------------------------------overrode methods
	@Override
	public void accept(FragmentVisitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public void init() {
		initAux();
		leavesReceived.clear();
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
	public void combineNodes(List<AbstractInternalCodeFragment> nodes, double NCOCP2Treshold) {
		for (int i = 0; i < nodes.size() - 1; i++) {
			for (int j = i + 1; j < nodes.size(); j++) {
				List<InternalCodeFragmentLeaf> fragmentsCombination = new ArrayList<>();

				if (nodes.get(i) instanceof InternalCodeFragmentLeaf) {
					fragmentsCombination.add((InternalCodeFragmentLeaf) nodes.get(i));
				} else {
					fragmentsCombination.addAll(((InternalCodeFragment) nodes.get(i)).getLeavesReceived());
					for (AbstractInternalCodeFragment eachNode : ((InternalCodeFragment) nodes.get(i)).children) {
						if (eachNode instanceof InternalCodeFragmentLeaf)
							fragmentsCombination.add((InternalCodeFragmentLeaf) eachNode);
					}
				}

				if (nodes.get(j) instanceof InternalCodeFragmentLeaf) {
					fragmentsCombination.add((InternalCodeFragmentLeaf) nodes.get(j));
				} else {
					fragmentsCombination.addAll(((InternalCodeFragment) nodes.get(j)).getLeavesReceived());
					for (AbstractInternalCodeFragment eachNode : ((InternalCodeFragment) nodes.get(j)).children) {
						if (eachNode instanceof InternalCodeFragmentLeaf)
							fragmentsCombination.add((InternalCodeFragmentLeaf) eachNode);
					}
				}

				MetricsComputer metricsComputer = new MetricsComputer();
				metricsComputer.computeCOCPAndNCOCP2(fragmentsCombination);
				double combinationNCOPC2 = metricsComputer.getNCOCP2();
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
		leavesReceived = new ArrayList<>();
		List<InternalCodeFragmentLeaf> childLeaves = new ArrayList<>();
		for (AbstractInternalCodeFragment child : children) {
			if (child instanceof InternalCodeFragment) {
				leavesReceived.addAll(((InternalCodeFragment) child).getCohesionMetric(unit));
			} else {
				childLeaves.add((InternalCodeFragmentLeaf) child);
			}
		}

		List<InternalCodeFragmentLeaf> temp = new ArrayList<>();
		temp.addAll(leavesReceived);
		temp.addAll(childLeaves);
		int numberOfFragments = temp.size();

		if (numberOfFragments > 1) {
			MetricsComputer metricsComputer = new MetricsComputer();
			metricsComputer.computeCOCPAndNCOCP2(temp);
			calculteFirstLastLine();
			System.out.println("Currently in node: " + toString() + " COCP: " + metricsComputer.getCOCP() + " NCOCP2: " + metricsComputer.getNCOCP2());
			System.out.println("From line : " + (unit.getLineNumber(startPosition)) + " to line: " + (unit.getLineNumber(endPosition)));
			System.out.println();
		}
		return temp;
	}
	
	@Override
	public void colorLongMethodFragments(ITextEditor textEditor, IFile file, List<AbstractInternalCodeFragment> functionalSegmentNodes) {
		for (AbstractInternalCodeFragment child : children) {
			child.colorLongMethodFragments(textEditor, file, functionalSegmentNodes);
		}
		if (functionalSegmentNodes.contains(this) && (possiblyRelatedFlag != true)) {
			if (Selector.getColorCounter() < 17) {
				Selector.incrementColorCounter();
				for (AbstractInternalCodeFragment child : children) {
					if (child instanceof InternalCodeFragmentLeaf) {
						int start = child.getFragmentFirstLineStartIndex();
						int end = child.getFragmentLastLineEndIndex();
						Position fragmentPosition = new Position(start, (end - start));
						IMarker mymarker = Selector.createIMarker(file, fragmentPosition);
						Selector.addAnnotation(textEditor, mymarker, fragmentPosition, child);
					}
				}
				for (InternalCodeFragmentLeaf leafReceived : leavesReceived) {
					int start = leafReceived.getFragmentFirstLineStartIndex();
					int end = leafReceived.getFragmentLastLineEndIndex();
					Position fragmentPosition = new Position(start, (end - start));
					IMarker mymarker = Selector.createIMarker(file, fragmentPosition);
					Selector.addAnnotation(textEditor, mymarker, fragmentPosition, leafReceived);
				}
				for (AbstractInternalCodeFragment cohesivlyRelatedNode : cohesivlyRelatedNodes) {
					if (cohesivlyRelatedNode instanceof InternalCodeFragmentLeaf) {
						int start = cohesivlyRelatedNode.getFragmentFirstLineStartIndex();
						int end = cohesivlyRelatedNode.getFragmentLastLineEndIndex();
						Position fragmentPosition = new Position(start, (end - start));
						IMarker mymarker = Selector.createIMarker(file, fragmentPosition);
						Selector.addAnnotation(textEditor, mymarker, fragmentPosition, cohesivlyRelatedNode);
					}
				}
			} else {
				for (AbstractInternalCodeFragment child : children) {
					if (child instanceof InternalCodeFragmentLeaf) {
						int start = child.getFragmentFirstLineStartIndex();
						int end = child.getFragmentLastLineEndIndex();
						Position fragmentPosition = new Position(start, (end - start));
						IMarker mymarker = Selector.createIMarker(file, fragmentPosition);
//							SelectionView.addAnnotation(mymarker, textEditor, "annotationColor_17", fragmentPosition);
						Selector.addAnnotation(textEditor, mymarker, fragmentPosition, child);
					}
				}
			}
		}
	}
	
	public double calculateNCOCP2() {
		List<InternalCodeFragmentLeaf> temp = new ArrayList<>();
		temp.addAll(leavesReceived);
		for (AbstractInternalCodeFragment child : children) {
			if (child instanceof InternalCodeFragmentLeaf) {
				temp.add((InternalCodeFragmentLeaf) child);
			}
		}
		MetricsComputer metricsComputer = new MetricsComputer();
		metricsComputer.computeCOCPAndNCOCP2(temp);
		return metricsComputer.getNCOCP2();
	}
	
	private void calculteFirstLastLine() {
		int startPosition = getFragmentFirstLineStartIndex();
		int endPosition = getFragmentLastLineEndIndex();

		if (leavesReceived.size() > 1) {
			if (startPosition == 0 && endPosition == 0) {
				startPosition = children.get(0).startPosition;
				endPosition = children.get(children.size() - 1).endPosition;
			} else if (startPosition == 0) {
				startPosition = leavesReceived.get(0).getFragmentFirstLineStartIndex();
			} else if (endPosition == 0) {
				endPosition = leavesReceived.get(leavesReceived.size() - 1).getFragmentLastLineEndIndex();
			}
		}
	}

	public List<InternalCodeFragmentLeaf> getLeavesReceived() {
		return leavesReceived;
	}
}
