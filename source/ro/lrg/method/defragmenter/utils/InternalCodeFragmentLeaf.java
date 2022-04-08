package ro.lrg.method.defragmenter.utils;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.ITextEditor;

import ro.lrg.method.defragmenter.views.SelectionView;
import ro.lrg.method.defragmenter.visitors.ast.VariableBindingVisitor;
import ro.lrg.method.defragmenter.visitors.fragment.FragmentVisitor;

public class InternalCodeFragmentLeaf extends AbstractInternalCodeFragment {
	
	public InternalCodeFragmentLeaf(String analizedClass, IFile iFile, IJavaProject iJavaProject) {
		super(analizedClass, iFile, iJavaProject);
	}
	
	//--------------------------------------------------------------------------overrode methods
	@Override
	public void accept(FragmentVisitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public void colorFragment(ITextEditor textEditor, IFile file) throws CoreException {
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
	public void init() {
		initAux();
	}
	
	@Override
	public void print(int tabs) {
		for (int j = 0; j < tabs; j++)
			System.out.print("\t");
		System.out.println(toString().replaceAll("\n", ""));
	}
	
	//--------------------------------------------------------------------------NCOCP2
	public List<IVariableBinding> getVariablesBindings() {
		VariableBindingVisitor variableBindingVisitor = new VariableBindingVisitor();
		for (ASTNode statement : getInternalStatements()) {
			statement.accept(variableBindingVisitor);
		}
		List<IVariableBinding> variablesBindings = new ArrayList<>();
		variablesBindings.addAll(variableBindingVisitor.getVariableBindings());
		return variablesBindings;
	}
	//--------------------------------------------------------------------------NCOCP2: overrode methods
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
	public AbstractInternalCodeFragment constructTree() {
		return this;
	}
	@Override
	public List<AbstractInternalCodeFragment> identifyFunctionalSegments() {
		List<AbstractInternalCodeFragment> functionalSegments = new ArrayList<>();
		functionalSegments.add(this);
		return functionalSegments;
	}
}
