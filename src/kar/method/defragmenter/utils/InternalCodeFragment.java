package kar.method.defragmenter.utils;

public class InternalCodeFragment extends AbstractCodeFragment{

	public void addChild(AbstractCodeFragment child)
	{
		children.add(child);
	}

	public void removeChild(AbstractCodeFragment child)
	{
		children.remove(child);
	}
}
