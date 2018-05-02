package kar.method.defragmenter.views;

public class EnviousNodeData {
	private String lines;
	private int accessForeignData;
	private int localAttrAccess;
	private int foreignDataProviders;
	private String targetClass;
	
	public EnviousNodeData() {
	}
	
	public String getLines() {
		return lines;
	}
	public void setLines(String lines) {
		this.lines = lines;
	}
	public int getAccessForeignData() {
		return accessForeignData;
	}
	public void setAccessForeignData(int accessForeignData) {
		this.accessForeignData = accessForeignData;
	}
	public int getLocalAttrAccess() {
		return localAttrAccess;
	}
	public void setLocalAttrAccess(int localAttrAccess) {
		this.localAttrAccess = localAttrAccess;
	}
	public int getForeignDataProviders() {
		return foreignDataProviders;
	}
	public void setForeignDataProviders(int foreignDataProviders) {
		this.foreignDataProviders = foreignDataProviders;
	}
	public String getTargetClass() {
		return targetClass;
	}
	public void setTargetClass(String targetClass) {
		this.targetClass = targetClass;
	}
	@Override
	public String toString() {
		return "EnviousNodeData [lines=" + lines + ", accessForeignData=" + accessForeignData + ", localAttrAccess="
				+ localAttrAccess + ", foreignDataProviders=" + foreignDataProviders + ", targetClass=" + targetClass
				+ "]";
	}
	
	
}
