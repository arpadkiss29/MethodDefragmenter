package ro.lrg.method.defragmenter.preferences.ui;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbenchPropertyPage;
import ro.lrg.method.defragmenter.preferences.MethodDefragmenterPropertyStore;

public class PreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPropertyPage {
	private IJavaProject project;
	
	public PreferencesPage() {
	    setDescription("Method defragmenter preferences");
	}
		
	@Override
	protected void createFieldEditors() {
		addField(new MethodDefragmenterFieldEditor(project, getFieldEditorParent()));
	}

	@Override
	public IAdaptable getElement() {
		return project;
	}

	@Override
	public void setElement(IAdaptable element) {
		project = (IJavaProject) element;
		setPreferenceStore(new MethodDefragmenterPropertyStore(project));
	}
}
