package ro.lrg.method.defragmenter.utils;

import org.eclipse.jdt.core.IType;

public class ITypeAccesses {
	IType iType;
	int numberOfAccesses;
	
	public ITypeAccesses(IType iType) {
		this.iType = iType;
		this.numberOfAccesses = 1;
	}
	
	public void incrementNumberOfAccesses() {
		this.numberOfAccesses++;
	}
	
	public IType getIType() {
		return iType;
	}
	
	public Integer getNumberOfAccesses() {
		return numberOfAccesses;
	}
}
