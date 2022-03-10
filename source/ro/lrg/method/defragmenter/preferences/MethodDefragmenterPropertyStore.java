package ro.lrg.method.defragmenter.preferences;

import java.util.HashMap;
import java.util.Map;

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
		if(value == null || value.equals("")) return;
		String[] allPreferences = value.split(";");
		for(String preference : allPreferences) {
			String[] parts = preference.split(",");
			super.setValue(parts[0], parts[1]);
			super.setDefault(parts[0], DEFAULT_VALUES.get(parts[0]));
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
		super.setDefault(preferenceName, DEFAULT_VALUES.get(preferenceName));
	}
	
	public void setDefaultBindings(String preferenceName) {
		super.setDefault(preferenceName, DEFAULT_VALUES.get(preferenceName));
		super.setToDefault(preferenceName);
	}
	
	private String getPreferenceValue(String preferenceName) {
		String preferenceValue = super.getString(preferenceName);
		return !preferenceValue.equals("") ? preferenceValue : DEFAULT_VALUES.get(preferenceName);
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
	
	public int getATFD() {
		return Integer.parseInt(toMap().get(ATFD_PREFERENCE_NAME));
	}
	
	public int getFDP() {
		return Integer.parseInt(toMap().get(FDP_PREFERENCE_NAME));
	}
	
	public Double getLAA() {
		return Double.parseDouble(toMap().get(LAA_PREFERENCE_NAME));
	}

	public boolean isConsiderStaticFieldAccesses() {
		return Boolean.parseBoolean(toMap().get(CONSIDER_STATIC_FIELD_ACCESS_PREFERENCE_NAME));
	}

	public int getMinBlockSize() {
		return toMap().get(MIN_BLOCK_SIZE_PREFERENCE_NAME).equals(NULL)?0:Integer.parseInt(toMap().get(APPLY_LONG_METHOD_IDENTIFICATION_PREFERENCE_NAME));
	}

	public boolean isLibraryCheck() {
		return Boolean.parseBoolean(toMap().get(LIBRARY_CHECK_PREFERENCE_NAME));
	}

	public boolean isApplyLongMethodIdentification() {
		return Boolean.parseBoolean(toMap().get(APPLY_LONG_METHOD_IDENTIFICATION_PREFERENCE_NAME));
	}
}