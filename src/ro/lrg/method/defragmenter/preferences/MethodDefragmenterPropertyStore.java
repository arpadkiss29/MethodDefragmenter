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

public class MethodDefragmenterPropertyStore extends PreferenceStore {
	private IJavaProject preferenceStoreProject;
	
	public MethodDefragmenterPropertyStore(IJavaProject preferenceStoreProject) {
		this.preferenceStoreProject = preferenceStoreProject;
		load();
	}
	
	@Override
	public void load() {
		IScopeContext projectScope = new ProjectScope(preferenceStoreProject.getProject());
		IEclipsePreferences projectNode = projectScope.getNode(Activator.PLUGIN_ID);
		for(Entry<String, String> entry : DefaultPreferences.DEFAULT_VALUES_MAP.entrySet()) {
			String value = projectNode == null ? null : projectNode.get(entry.getKey(), null);
			if(value == null || value.equals("")) super.setValue(entry.getKey(), entry.getValue());
			else super.setValue(entry.getKey(), value);
			super.setDefault(entry.getKey(), entry.getValue());
		}
	}

	public void doSave() {
		try {
			String[] allPreferencesNames = super.preferenceNames();
			IScopeContext projectScope = new ProjectScope(preferenceStoreProject.getProject());
			IEclipsePreferences projectNode = projectScope.getNode(Activator.PLUGIN_ID);
			for(String preferenceName : allPreferencesNames) {
				projectNode.put(preferenceName, super.getString(preferenceName));
			}
			projectNode.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}
	
	public void setBindings(String preferenceName, String value) {
		super.setValue(preferenceName, value);
		super.setDefault(preferenceName, DefaultPreferences.DEFAULT_VALUES_MAP.get(preferenceName));
	}
	
	public void setDefaultBindings(String preferenceName) {
		super.setDefault(preferenceName, DefaultPreferences.DEFAULT_VALUES_MAP.get(preferenceName));
		super.setToDefault(preferenceName);
	}
	
	private String getPreferenceValue(String preferenceName) {
		String preferenceValue = super.getString(preferenceName);
		return !preferenceValue.equals("") ? preferenceValue : DefaultPreferences.DEFAULT_VALUES_MAP.get(preferenceName);
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
		if(toMap().get(DefaultPreferences.GROUPING_ALGORITHM_PREFERENCE_NAME) == null) 
			return Integer.parseInt(DefaultPreferences.DEFAULT_VALUES_MAP.get(DefaultPreferences.GROUPING_ALGORITHM_PREFERENCE_NAME));
		return Integer.parseInt(toMap().get(DefaultPreferences.GROUPING_ALGORITHM_PREFERENCE_NAME));
	}
	
	public int getATFDTreshold() {
		if(toMap().get(DefaultPreferences.ATFD_PREFERENCE_NAME) == null) 
			return Integer.parseInt(DefaultPreferences.DEFAULT_VALUES_MAP.get(DefaultPreferences.ATFD_PREFERENCE_NAME));
		return Integer.parseInt(toMap().get(DefaultPreferences.ATFD_PREFERENCE_NAME));
	}
	
	public int getFDPTreshold() {
		if(toMap().get(DefaultPreferences.FDP_PREFERENCE_NAME) == null) 
			return Integer.parseInt(DefaultPreferences.DEFAULT_VALUES_MAP.get(DefaultPreferences.FDP_PREFERENCE_NAME));
		return Integer.parseInt(toMap().get(DefaultPreferences.FDP_PREFERENCE_NAME));
	}
	
	public double getLAATreshold() {
		if(toMap().get(DefaultPreferences.LAA_PREFERENCE_NAME) == null) 
			return Double.parseDouble(DefaultPreferences.DEFAULT_VALUES_MAP.get(DefaultPreferences.LAA_PREFERENCE_NAME));
		return Double.parseDouble(toMap().get(DefaultPreferences.LAA_PREFERENCE_NAME));
	}

	public boolean isConsiderStaticFieldAccesses() {
		if(toMap().get(DefaultPreferences.CONSIDER_STATIC_FIELD_ACCESS_PREFERENCE_NAME) == null) 
			return Boolean.parseBoolean(DefaultPreferences.DEFAULT_VALUES_MAP.get(DefaultPreferences.CONSIDER_STATIC_FIELD_ACCESS_PREFERENCE_NAME));
		return Boolean.parseBoolean(toMap().get(DefaultPreferences.CONSIDER_STATIC_FIELD_ACCESS_PREFERENCE_NAME));
	}

	public boolean isLibraryCheck() {
		if(toMap().get(DefaultPreferences.LIBRARY_CHECK_PREFERENCE_NAME) == null) 
			return Boolean.parseBoolean(DefaultPreferences.DEFAULT_VALUES_MAP.get(DefaultPreferences.LIBRARY_CHECK_PREFERENCE_NAME));
		return Boolean.parseBoolean(toMap().get(DefaultPreferences.LIBRARY_CHECK_PREFERENCE_NAME));
	}
}