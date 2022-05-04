package ro.lrg.method.defragmenter.utils;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.ITextEditor;

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
			if (Selector.getColorCounter() < 17) {
				Selector.incrementColorCounter();
				Position fragmentPosition = getPosition();
				IMarker mymarker = Selector.createIMarker(file, fragmentPosition);
				Selector.addAnnotation(textEditor, mymarker, fragmentPosition, this);

				for (AbstractInternalCodeFragment node : cohesivlyRelatedNodes) {
					if (node instanceof InternalCodeFragmentLeaf) {
						Position fragmentPositionPoss = node.getPosition();
						IMarker mymarkerPoss = Selector.createIMarker(file, fragmentPositionPoss);
						Selector.addAnnotation(textEditor, mymarkerPoss, fragmentPosition, node);
					}
				}
			} else {
				Position fragmentPosition = getPosition();
				IMarker mymarker = Selector.createIMarker(file, fragmentPosition);
//					SelectionView.addAnnotation(mymarker, textEditor, "annotationColor_17", fragmentPosition);
				Selector.addAnnotation(textEditor, mymarker, fragmentPosition, this);
			}
		}
	}
}
