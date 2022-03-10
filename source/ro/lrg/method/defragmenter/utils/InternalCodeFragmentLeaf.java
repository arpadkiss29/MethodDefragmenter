package ro.lrg.method.defragmenter.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.ITextEditor;

import ro.lrg.method.defragmenter.views.SelectionView;
import ro.lrg.method.defragmenter.visitors.VariableBindingVisitor;

public class InternalCodeFragmentLeaf extends AbstractInternalCodeFragment {
	
	public InternalCodeFragmentLeaf(IFile iFile, IJavaProject iJavaProject) {
		super(iFile, iJavaProject);
	}

	public void joinWithLeaf(InternalCodeFragmentLeaf internalCodeFragmentLeaf) {
		addInternalStatements(internalCodeFragmentLeaf.getInternalStatements());
		Map<String, Integer> storedFDP = this.getStoredFDP();
		Map<String, Integer> leafFDP = internalCodeFragmentLeaf.getStoredFDP();
		for (Entry<String, Integer> entry : leafFDP.entrySet()) {
			if(storedFDP.get(entry.getKey())!=null) {
				storedFDP.replace(entry.getKey(), storedFDP.get(entry.getKey()) + entry.getValue());
			} else {
				storedFDP.put(entry.getKey(), entry.getValue());
			}
		}
	}
	
	public List<IVariableBinding> getVariablesBindings() {
		VariableBindingVisitor variableBindingVisitor = new VariableBindingVisitor();
		for (ASTNode statement : getInternalStatements()) {
			statement.accept(variableBindingVisitor);
		}
		List<IVariableBinding> variablesBindings = new ArrayList<>();
		variablesBindings.addAll(variableBindingVisitor.getVariableBindings());
		return variablesBindings;
	}
	
	//override abstract methods
	
	@Override
	public void clearData() {
		accessClassesMapping.clear();
		ATFD = 0;
		FDP = 0;
		LAA = 0;
	}
	
	@Override
	public void colorEnvyLeafNodes(ITextEditor textEditor, IFile file) throws CoreException {
		if (isEnvy()) {
			String colorType = "annotationColor_17";
			if (colorCounter < 17) {
				colorType = "annotationColor_" + colorCounter;
				colorCounter++;
			}
			int start = getFragmentFirstLineStartIndex();
			int end = getFragmentLastLineEndIndex();
			Position fragmentPosition = new Position(start, (end - start));
			IMarker mymarker = SelectionView.createMarker(file, fragmentPosition);
			SelectionView.addAnnotation(mymarker, textEditor, colorType, fragmentPosition);
		}
	}
	
	@Override
	public void colorLongMethodFragments(ITextEditor textEditor, IFile file, List<AbstractInternalCodeFragment> functionalSegments) {
		if (functionalSegments.contains(this) && possiblyRelatedFlag != true) {
			try {
				if (colorCounter < 17) {
					colorCounter++;
					int start = getFragmentFirstLineStartIndex();
					int end = getFragmentLastLineEndIndex();
					Position fragmentPosition = new Position(start, (end - start));
					IMarker mymarker = SelectionView.createMarker(file, fragmentPosition);
					SelectionView.addAnnotation(mymarker, textEditor, "annotationColor_" + colorCounter, fragmentPosition);

					for (int i = 0; i < cohesivlyRelatedNodes.size(); i++) {
						if (cohesivlyRelatedNodes.get(i) instanceof InternalCodeFragmentLeaf) {
							int startPoss = cohesivlyRelatedNodes.get(i).getFragmentFirstLineStartIndex();
							int endPoss = cohesivlyRelatedNodes.get(i).getFragmentLastLineEndIndex();
							Position fragmentPositionPoss = new Position(startPoss, (endPoss - startPoss));
							IMarker mymarkerPoss = SelectionView.createMarker(file, fragmentPositionPoss);
							SelectionView.addAnnotation(mymarkerPoss, textEditor, "annotationColor_" + colorCounter, fragmentPositionPoss);
						}
					}
				} else {
					int start = getFragmentFirstLineStartIndex();
					int end = getFragmentLastLineEndIndex();
					Position fragmentPosition = new Position(start, (end - start));
					IMarker mymarker = SelectionView.createMarker(file, fragmentPosition);
					SelectionView.addAnnotation(mymarker, textEditor, "annotationColor_17", fragmentPosition);
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void computeDataAccesses(String analyzedClass, boolean considerStaticFieldAccess, boolean libraryCheck, Integer minBlockSize) {
		if (minBlockSize != null) {
			if (getInternalStatementsSize() < minBlockSize) return;
		}
		computeDataAccessesAux(analyzedClass, considerStaticFieldAccess, libraryCheck, minBlockSize);
	}
	
	@Override
	public AbstractInternalCodeFragment constructTree() {
		return this;
	}
	
	@Override
	public List<AbstractInternalCodeFragment> getAllEnviousNodes() {
		List<AbstractInternalCodeFragment> nodes = new ArrayList<AbstractInternalCodeFragment>();
		if(isEnvy()) nodes.add(this);
		return nodes;
	}
	
	@Override
	public List<ASTNode> getAllInternalStatements() {
		return getInternalStatements();
	}
	
	@Override
	public int getFragmentFirstLineStartIndex() {
		if (getInternalStatements().isEmpty()) return 1;
		return getInternalStatement(0).getStartPosition();
	}

	@Override
	public int getFragmentLastLineEndIndex() {
		if (getInternalStatements().isEmpty()) return 1;
		return getInternalStatement(getInternalStatementsSize() - 1).getStartPosition() + getInternalStatement(getInternalStatementsSize() - 1).getLength();
	}
	
	@Override
	public List<AbstractInternalCodeFragment> identifyFunctionalSegments() {
		List<AbstractInternalCodeFragment> functionalSegments = new ArrayList<>();
		functionalSegments.add(this);
		return functionalSegments;
	}

	@Override
	public void init() {
		initAux();
	}
	
	@Override
	public void print(int tabs) {
		for (int j = 0; j < tabs; j++)
			System.out.print("\t");
		System.out.println(toString().replaceAll("\n", ""));
	}
	
	@Override
	public boolean verifyFeatureEnvy(int ATFDTreshold, int FDPTreshold, double LAATreshold, String analyzedClass,
			boolean considerStaticFieldAccess, boolean libraryCheck, Integer minBlockSize, boolean local) {
		isEnvy = false;
		Map<String, ITypeAccesses> accessClassesMapping = getAccessClassesMapping();
		accessClassesMapping.clear();
		ATFD = 0;
		FDP = 0;
		LAA = 0;
		targetClass = "";

		computeDataAccesses(analyzedClass, considerStaticFieldAccess, libraryCheck, minBlockSize);
		for (ITypeAccesses iTypeAccesses : accessClassesMapping.values()) {
			ATFD += iTypeAccesses.getNumberOfAccesses();
		}
		int totalAccesses = ATFD + LAA;
		FDP = accessClassesMapping.size();

		if (ATFD > ATFDTreshold
				&& (LAA > 0 ? (LAA * 1.0) / totalAccesses : 0) < (LAATreshold)
				&& FDP <= FDPTreshold) {

			String enviousClass = "";
			int maxAccess = Integer.MIN_VALUE;
			if (accessClassesMapping.entrySet().size() == 1) {
				for (Entry<String, ITypeAccesses> accessedClassEntry : accessClassesMapping.entrySet()) {
					if (accessedClassEntry.getValue().getNumberOfAccesses() > maxAccess) {
						maxAccess = accessedClassEntry.getValue().getNumberOfAccesses();
						enviousClass = accessedClassEntry.getKey();
					}
				}
			} else {
				int count = 0;
				for (Entry<String, ITypeAccesses> accessedClassEntry : accessClassesMapping.entrySet()) {
					if (count > 0)
						enviousClass += ";";
					enviousClass += accessedClassEntry.getKey() + " - " + accessedClassEntry.getValue().getNumberOfAccesses();
					count++;
				}
			}

			targetClass = enviousClass;
			isEnvy = true;
		}

		return isEnvy;

	}

}
