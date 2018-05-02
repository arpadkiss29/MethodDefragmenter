package kar.method.defragmenter.views;

import org.eclipse.jdt.core.IMethod;

import kar.method.defragmenter.utils.CodeFragmentTreeNode;

public class MethodBasicItem {
	private IMethod IMtehodReference;
	
	private CodeFragmentTreeNode methodRoot;
	
	private String className;
	private String name;
	private String returnType;
	private String lines;
	private int length;
	private int numberOfParams;
	private String rootNCOCP2;
	private boolean containsEnviousBlocks;
	
	public MethodBasicItem(){
	}
	
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getReturnType() {
		return returnType;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	public String getLines() {
		return lines;
	}

	public void setLines(String lines) {
		this.lines = lines;
	}

	public String getRootNCOCP2() {
		return rootNCOCP2;
	}

	public void setRootNCOCP2(String rootNCOCP2) {
		this.rootNCOCP2 = rootNCOCP2;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getNumberOfParams() {
		return numberOfParams;
	}

	public void setNumberOfParams(int numberOfParams) {
		this.numberOfParams = numberOfParams;
	}

	public IMethod getIMtehodReference() {
		return IMtehodReference;
	}

	public void setIMtehodReference(IMethod IMtehodReference) {
		this.IMtehodReference = IMtehodReference;
	}

	public CodeFragmentTreeNode getMethodRoot() {
		return methodRoot;
	}

	public void setMethodRoot(CodeFragmentTreeNode methodRoot) {
		this.methodRoot = methodRoot;
	}
	
	public boolean containEnviousBlocks(){
		return containsEnviousBlocks;
	}
	
	public void setContainsEnviousBlocks(boolean containsEnviousBlocks) {
		this.containsEnviousBlocks = containsEnviousBlocks;
	}
	
	
}
