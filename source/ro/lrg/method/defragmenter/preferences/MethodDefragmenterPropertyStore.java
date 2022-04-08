package ro.lrg.method.defragmenter.preferences;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.preference.PreferenceStore;
import org.osgi.service.prefs.BackingStoreException;

import ro.lrg.method.defragmenter.Activator;

public class MethodDefragmenterPropertyStore extends PreferenceStore implements DefaultPreferences {
	private static final String PROP_KEY = "methodDefragmenterProperties";
	private IJavaProject preferenceStoreProject;
	
	public MethodDefragmenterPropertyStore(IJavaProject preferenceStoreProject) {
		this.preferenceStoreProject = preferenceStoreProject;
		load();
	}
	
	@Override
	public void load() {
		IScopeContext projectScope = new ProjectScope(preferenceStoreProject.getProject());
		IEclipsePreferences projectNode = projectScope.getNode(Activator.PLUGIN_ID);
		String value = projectNode.get(PROP_KEY,null);
		if(value == null || value.equals("")) {
			for (Entry<String, String> entry : DEFAULT_VALUES_MAP.entrySet()) {
				super.setValue(entry.getKey(), entry.getValue());
				super.setDefault(entry.getKey(), entry.getValue());
			}
		} else {
			String[] allPreferences = value.split(";");
			for(String preference : allPreferences) {
				String[] parts = preference.split(",");
				super.setValue(parts[0], parts[1]);
				super.setDefault(parts[0], DEFAULT_VALUES_MAP.get(parts[0]));
			}
		}
	}

	public void doSave() {
		try {
			String[] allPreferencesNames = super.preferenceNames();
			String value = "";
			for(String preferenceName : allPreferencesNames) {
				value += preferenceName + "," + super.getString(preferenceName) + ";";
			}
			IScopeContext projectScope = new ProjectScope(preferenceStoreProject.getProject());
			IEclipsePreferences projectNode = projectScope.getNode(Activator.PLUGIN_ID);
			projectNode.put(PROP_KEY, value);
			projectNode.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}
	
	public void setBindings(String preferenceName, String value) {
		super.setValue(preferenceName, value);
		super.setDefault(preferenceName, DEFAULT_VALUES_MAP.get(preferenceName));
	}
	
	public void setDefaultBindings(String preferenceName) {
		super.setDefault(preferenceName, DEFAULT_VALUES_MAP.get(preferenceName));
		super.setToDefault(preferenceName);
	}
	
	private String getPreferenceValue(String preferenceName) {
		String preferenceValue = super.getString(preferenceName);
		return !preferenceValue.equals("") ? preferenceValue : DEFAULT_VALUES_MAP.get(preferenceName);
	}
			
	public Map<String, String> toMap() {
		String[] allPreferencesNames = super.preferenceNames();
		Map<String, String> map = new HashMap<>();
		for(String preferenceName : allPreferencesNames) {
			map.put(preferenceName, this.getPreferenceValue(preferenceName));
		}
		return map;
	}
	
	public IJavaProject getProject() {
		return preferenceStoreProject;
	}
	
	public int getGroupingAlgorithm() {
		if(toMap().get(GROUPING_ALGORITHM_PREFERENCE_NAME) == null) return Integer.parseInt(DEFAULT_VALUES_MAP.get(GROUPING_ALGORITHM_PREFERENCE_NAME));
		return Integer.parseInt(toMap().get(GROUPING_ALGORITHM_PREFERENCE_NAME));
	}
	
	public int getATFDTreshold() {
		if(toMap().get(ATFD_PREFERENCE_NAME) == null) return Integer.parseInt(DEFAULT_VALUES_MAP.get(ATFD_PREFERENCE_NAME));
		return Integer.parseInt(toMap().get(ATFD_PREFERENCE_NAME));
	}
	
	public int getFDPTreshold() {
		if(toMap().get(FDP_PREFERENCE_NAME) == null) return Integer.parseInt(DEFAULT_VALUES_MAP.get(FDP_PREFERENCE_NAME));
		return Integer.parseInt(toMap().get(FDP_PREFERENCE_NAME));
	}
	
	public double getLAATreshold() {
		if(toMap().get(LAA_PREFERENCE_NAME) == null) return Double.parseDouble(DEFAULT_VALUES_MAP.get(LAA_PREFERENCE_NAME));
		return Double.parseDouble(toMap().get(LAA_PREFERENCE_NAME));
	}
	
	public double getNCOCP2Treshold() {
		if(toMap().get(NCOCP2_PREFERENCE_NAME) == null) return Double.parseDouble(DEFAULT_VALUES_MAP.get(NCOCP2_PREFERENCE_NAME));
		return Double.parseDouble(toMap().get(NCOCP2_PREFERENCE_NAME));
	}

	public boolean isConsiderStaticFieldAccesses() {
		if(toMap().get(CONSIDER_STATIC_FIELD_ACCESS_PREFERENCE_NAME) == null) return Boolean.parseBoolean(DEFAULT_VALUES_MAP.get(CONSIDER_STATIC_FIELD_ACCESS_PREFERENCE_NAME));
		return Boolean.parseBoolean(toMap().get(CONSIDER_STATIC_FIELD_ACCESS_PREFERENCE_NAME));
	}

	public int getMinBlockSize() {
		String minBlockSize = toMap().get(MIN_BLOCK_SIZE_PREFERENCE_NAME);
		if (minBlockSize == null) 
			return DEFAULT_VALUES_MAP.get(MIN_BLOCK_SIZE_PREFERENCE_NAME)
				.equals(NULL)?0:Integer.parseInt(toMap().get(APPLY_LONG_METHOD_IDENTIFICATION_PREFERENCE_NAME));
		return toMap().get(MIN_BLOCK_SIZE_PREFERENCE_NAME).equals(NULL)?0:Integer.parseInt(toMap().get(APPLY_LONG_METHOD_IDENTIFICATION_PREFERENCE_NAME));
	}

	public boolean isLibraryCheck() {
		if(toMap().get(LIBRARY_CHECK_PREFERENCE_NAME) == null) return Boolean.parseBoolean(DEFAULT_VALUES_MAP.get(LIBRARY_CHECK_PREFERENCE_NAME));
		return Boolean.parseBoolean(toMap().get(LIBRARY_CHECK_PREFERENCE_NAME));
	}

	public boolean isApplyLongMethodIdentification() {
		if(toMap().get(APPLY_LONG_METHOD_IDENTIFICATION_PREFERENCE_NAME) == null) return Boolean.parseBoolean(DEFAULT_VALUES_MAP.get(APPLY_LONG_METHOD_IDENTIFICATION_PREFERENCE_NAME));
		return Boolean.parseBoolean(toMap().get(APPLY_LONG_METHOD_IDENTIFICATION_PREFERENCE_NAME));
	}
}