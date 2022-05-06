package ro.lrg.method.defragmenter.utils;

import org.eclipse.jdt.core.IType;

public class FDPClass {
	private final IType iType;
	private int numberOfAccesses;
	
	public FDPClass(IType iType) {
		this.iType = iType;
		this.numberOfAccesses = 0;
	}
	
	public void incrementNumberOfAccesses() {
		this.numberOfAccesses++;
	}
	
	public String getClassName() {
		return iType.getElementName();
	}
	
	public IType getIType() {
		return iType;
	}
	
	public Integer getNumberOfAccesses() {
		return numberOfAccesses;
	}
}
