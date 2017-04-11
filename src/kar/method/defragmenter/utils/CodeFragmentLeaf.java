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
import kar.method.defragmenter.visittors.SimpleNameVisitor;

public class CodeFragmentLeaf extends CodeFragmentTreeNode {

	private ArrayList<ASTNode> myASTNodes = new ArrayList<ASTNode>();
	
	
	public void addStatement(ASTNode node){	
		myASTNodes.add(node);
	}
	
	public void removeStatement(ASTNode node){
		myASTNodes.remove(node);
	}
	
	public void print(int tabs) {
		for(int i = 0; i < tabs+1; i++) System.out.print("\t");
		String statement = myASTNodes.toString();
		statement = statement.replace("\n", "");
		System.out.print(statement);
		System.out.println();
	}
	
	public HashSet<IVariableBinding> getMyVariables(){
		HashSet<IVariableBinding> vars = new HashSet<IVariableBinding>();
		SimpleNameVisitor visitorVariableName = new SimpleNameVisitor();
		for (ASTNode eachNode : myASTNodes){
			eachNode.accept(visitorVariableName);
			vars = visitorVariableName.getVariableNames();
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
	

	public CodeFragmentTreeNode getAllTreeData(){
		return this;
	}
	
	

	

	@Override
	public List<CodeFragmentTreeNode> identifyFunctionalSegments() {
		List<CodeFragmentTreeNode> temp = new ArrayList<CodeFragmentTreeNode>();
		temp.add(this);
		return temp;
	}

	@Override
	public void colorFragemnts(ITextEditor textEditor, IFile file,
			List<CodeFragmentTreeNode> functionalSegmentNodes) {
		
		if((functionalSegmentNodes.contains(this)) && (possiblyRelatedFlag != true)){
			try {
				if (colorCounter < 6){
					colorCounter++;	
					int start = this.getFragmentFirstLine();
					int end = this.getFragmentLastLine();
					Position fragmentPosition = new Position(start, (end - start));
					IMarker mymarker = SelectionView.createMarker(file, fragmentPosition);
					SelectionView.addAnnotation(mymarker, textEditor, "com.ibm.example.myannotation" + colorCounter, fragmentPosition);
					
					for(int i=0; i<possiblyRelatedNodes.size(); i++){
						if (possiblyRelatedNodes.get(i) instanceof CodeFragmentLeaf){
							int startPoss = ((CodeFragmentLeaf)possiblyRelatedNodes.get(i)).getFragmentFirstLine();
							int endPoss = ((CodeFragmentLeaf)possiblyRelatedNodes.get(i)).getFragmentLastLine();
							Position fragmentPositionPoss = new Position(startPoss, (endPoss - startPoss));
							IMarker mymarkerPoss = SelectionView.createMarker(file, fragmentPositionPoss);
							SelectionView.addAnnotation(mymarkerPoss, textEditor, "com.ibm.example.myannotation" + colorCounter, fragmentPositionPoss);
						}
					}
					
				}else{
					int start = this.getFragmentFirstLine();
					int end = this.getFragmentLastLine();
					Position fragmentPosition = new Position(start, (end - start));
					IMarker mymarker = SelectionView.createMarker(file, fragmentPosition);
					SelectionView.addAnnotation(mymarker, textEditor, "com.ibm.example.myannotation6", fragmentPosition);
				}

				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
	}
	
	
	
	
}
