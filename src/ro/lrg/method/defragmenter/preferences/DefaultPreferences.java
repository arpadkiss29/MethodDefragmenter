package ro.lrg.method.defragmenter.preferences;

import java.util.HashMap;
import java.util.Map;

public interface DefaultPreferences {
	String GROUPING_ALGORITHM_PREFERENCE_NAME = "grouping_algorithm";
	String ATFD_PREFERENCE_NAME = "ATFDTreshold";
	String FDP_PREFERENCE_NAME = "FDPTreshold";
	String LAA_PREFERENCE_NAME = "LAATreshold";
	String CONSIDER_STATIC_FIELD_ACCESS_PREFERENCE_NAME = "consider_static_field_acces";
	String LIBRARY_CHECK_PREFERENCE_NAME = "library_check";
	
	String GROUPING_ALGORITHM_DEFAULT_VALUE = String.valueOf(GroupingAlgorithmsConstants.DEFAULT_ALGORITHM);
	String ATFD_DEFAULT_VALUE = "2";
	String FDP_DEFAULT_VALUE = "1";
	String LAA_DEFAULT_VALUE = String.valueOf(1.0/3);
	String CONSIDER_STATIC_FIELD_ACCESS_DEFAULT_VALUE = "false";
	String LIBRARY_CHECK_DEFAULT_VALUE = "true";
	
	@SuppressWarnings("serial")
	Map<String,String> DEFAULT_VALUES_MAP = new HashMap<String, String>() {{
		put(GROUPING_ALGORITHM_PREFERENCE_NAME, GROUPING_ALGORITHM_DEFAULT_VALUE);
		put(ATFD_PREFERENCE_NAME, ATFD_DEFAULT_VALUE);
		put(FDP_PREFERENCE_NAME, FDP_DEFAULT_VALUE);
		put(LAA_PREFERENCE_NAME, LAA_DEFAULT_VALUE);
		put(CONSIDER_STATIC_FIELD_ACCESS_PREFERENCE_NAME, CONSIDER_STATIC_FIELD_ACCESS_DEFAULT_VALUE);
		put(LIBRARY_CHECK_PREFERENCE_NAME, LIBRARY_CHECK_DEFAULT_VALUE);
	}};
}
