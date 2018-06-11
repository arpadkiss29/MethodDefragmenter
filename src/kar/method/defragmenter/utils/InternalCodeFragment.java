package kar.method.defragmenter.utils;

import java.util.List;

public class InternalCodeFragment extends AbstractCodeFragment{

	public boolean removeChildren(List<AbstractCodeFragment> childElements){
		return children.removeAll(childElements);
	}

	public void addChild(int index, AbstractCodeFragment child)
	{
		children.add(index, child);
	}

	public void addChild(AbstractCodeFragment child)
	{
		children.add(child);
	}

	public int removeChild(AbstractCodeFragment child)
	{
		int ind = children.indexOf(child);
		children.remove(child);
		return ind;
	}
}
