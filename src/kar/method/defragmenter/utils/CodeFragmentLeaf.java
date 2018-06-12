package kar.method.defragmenter.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.ITextEditor;

import kar.method.defragmenter.views.SelectionView;
import kar.method.defragmenter.visittors.VariableBindingVisitor;

public class CodeFragmentLeaf extends AbstractCodeFragment {

	private ArrayList<ASTNode> myASTNodes = new ArrayList<ASTNode>();

	public CodeFragmentLeaf() {}

	public void addStatement(ASTNode node){	
		myASTNodes.add(node);
	}

	public void removeStatement(ASTNode node){
		myASTNodes.remove(node);
	}

	public HashSet<IVariableBinding> getMyVariables(){
		HashSet<IVariableBinding> vars = new HashSet<IVariableBinding>();
		VariableBindingVisitor visitorVariableName = new VariableBindingVisitor();
		for (ASTNode eachNode : myASTNodes){
			eachNode.accept(visitorVariableName);
			vars = visitorVariableName.getVariableBindings();
		}
		return vars;
	}

	public int getFragmentFirstLine(){
		if(!myASTNodes.isEmpty()){
			return myASTNodes.get(0).getStartPosition();
		}
		return 1;

	}

	public int getFragmentLastLine(){
		if(!myASTNodes.isEmpty()){
			return myASTNodes.get(myASTNodes.size() - 1).getStartPosition() + myASTNodes.get(myASTNodes.size() - 1).getLength(); 
		}
		return 1;
	}


	@Override
	public List<ASTNode> getAllSubTreeASTNodes() {
		return myASTNodes;
	}

	public AbstractCodeFragment getAllTreeData(){
		return this;
	}


	@Override
	public List<AbstractCodeFragment> identifyFunctionalSegments() {
		List<AbstractCodeFragment> temp = new ArrayList<AbstractCodeFragment>();
		temp.add(this);
		return temp;
	}

	@Override
	public void colorLongMethodFragments(ITextEditor textEditor, IFile file,
			List<AbstractCodeFragment> functionalSegmentNodes) {

		if((functionalSegmentNodes.contains(this)) && (possiblyRelatedFlag != true)){
			try {
				if (colorCounter < 17){
					colorCounter++;	
					int start = this.getFragmentFirstLine();
					int end = this.getFragmentLastLine();
					Position fragmentPosition = new Position(start, (end - start));
					IMarker mymarker = SelectionView.createMarker(file, fragmentPosition);
					SelectionView.addAnnotation(mymarker, textEditor, "annotationColor_" + colorCounter, fragmentPosition);

					for(int i=0; i<cohesivlyRelatedNodes.size(); i++){
						if (cohesivlyRelatedNodes.get(i) instanceof CodeFragmentLeaf){
							int startPoss = ((CodeFragmentLeaf)cohesivlyRelatedNodes.get(i)).getFragmentFirstLine();
							int endPoss = ((CodeFragmentLeaf)cohesivlyRelatedNodes.get(i)).getFragmentLastLine();
							Position fragmentPositionPoss = new Position(startPoss, (endPoss - startPoss));
							IMarker mymarkerPoss = SelectionView.createMarker(file, fragmentPositionPoss);
							SelectionView.addAnnotation(mymarkerPoss, textEditor, "annotationColor_" + colorCounter, fragmentPositionPoss);
						}
					}

				}else{
					int start = this.getFragmentFirstLine();
					int end = this.getFragmentLastLine();
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
	public boolean verifyFeatureEnvy(int ATFDTreshold, int FDPTreshold, String analyzedClass, boolean staticFields, Integer minBlockSize, boolean libraryCheck, boolean local) {
		return super.verifyFeatureEnvy(ATFDTreshold, FDPTreshold, analyzedClass, staticFields, minBlockSize, libraryCheck, true);
	}
	
	@Override
	protected void computeDataAccesses(String analyzedClass, boolean staticFields, Integer minBlockSize, boolean libraryCheck) {
		if(minBlockSize != null) {
			if(myASTNodes.size() < minBlockSize) return;
		}
		super.computeDataAccesses(analyzedClass, staticFields, minBlockSize, libraryCheck);
	}
	
	@Override
	public void colorEnvyLeafNodes(ITextEditor textEditor, IFile file) throws CoreException {	
		if(isEnvy()){
			String colorType = "annotationColor_17";

			if (colorCounter < 17){
				colorType = "annotationColor_" + colorCounter;
				colorCounter++;
			}

			int start = this.getFragmentFirstLine();
			int end = this.getFragmentLastLine();
			Position fragmentPosition = new Position(start, (end - start));
			IMarker mymarker = SelectionView.createMarker(file, fragmentPosition);
			SelectionView.addAnnotation(mymarker, textEditor, colorType, fragmentPosition);
		}		
	}

	public String toString() {
		return super.toString() + myASTNodes.toString();
	}

}
