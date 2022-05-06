package ro.lrg.method.defragmenter.preferences;

public interface GroupingAlgorithmsConstants {
	int ARPI = 0;
	int SALEH1 = 1;
	int SALEH2 = 2;
	String[] GROUPING_ALGORITHMS_NAMES = new String[] {
			"Arpi Grouping Algorithm", "Saleh1 Grouping Algorithm", "Saleh2 Grouping Algorithm"};
	int DEFAULT_ALGORITHM = SALEH2;
}
