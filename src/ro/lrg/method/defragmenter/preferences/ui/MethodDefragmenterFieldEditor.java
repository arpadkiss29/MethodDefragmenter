package ro.lrg.method.defragmenter.preferences.ui;

import java.util.Map;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

import ro.lrg.method.defragmenter.preferences.GroupingAlgorithmsConstants;
import ro.lrg.method.defragmenter.preferences.DefaultPreferences;
import ro.lrg.method.defragmenter.preferences.MethodDefragmenterPropertyStore;

public class MethodDefragmenterFieldEditor extends FieldEditor {
	private static final int numberOfDecimals = 2;
	private Combo groupingAlgorithmSelectionCombo;
	private Spinner ATFDTresholdSpinner;
	private Spinner FDPTresholdSpinner;
	private Spinner LAATresholdSpinner;
	private Button considerStaticFiledAccessCheckbox;
	private Button libraryCheckCheckbox;
	
	public MethodDefragmenterFieldEditor(IJavaProject theProject, Composite parent) {
		createControl(parent);
	}

	@Override
	public int getNumberOfControls() {
		return 2;
	}
	
	private int LAATresholdSpinnerPositionOf(String number) {
		String stringWithSpecifiedNumberOfDecimals = number.substring(0, DefaultPreferences.LAA_DEFAULT_VALUE.indexOf(".") + numberOfDecimals + 1);
		int position = (int) (Double.parseDouble(stringWithSpecifiedNumberOfDecimals) / (1.0 / Math.pow(10, numberOfDecimals)));
		return position;
	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		
		new Label(parent, SWT.NONE).setText("Grouping algorithm:");
		groupingAlgorithmSelectionCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		groupingAlgorithmSelectionCombo.setItems(GroupingAlgorithmsConstants.GROUPING_ALGORITHMS_NAMES);
		groupingAlgorithmSelectionCombo.select(GroupingAlgorithmsConstants.DEFAULT_ALGORITHM);
		
		new Label(parent, SWT.NONE).setText("Accesses to foreign data treshold (ATFD):");
		ATFDTresholdSpinner = new Spinner (parent, SWT.BORDER);
		ATFDTresholdSpinner.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		ATFDTresholdSpinner.setValues(Integer.parseInt(DefaultPreferences.ATFD_DEFAULT_VALUE), 0, 10, 0, 1, 10);
		
		new Label(parent, SWT.NONE).setText("Foreign data providers treshold (FDP):");
		FDPTresholdSpinner = new Spinner (parent, SWT.BORDER);
		FDPTresholdSpinner.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		FDPTresholdSpinner.setValues(Integer.parseInt(DefaultPreferences.FDP_DEFAULT_VALUE), 0, 10, 0, 1, 10);
		
		new Label(parent, SWT.NONE).setText("Local atributes accesses treshold (LAA):");
		LAATresholdSpinner = new Spinner (parent, SWT.BORDER);
		LAATresholdSpinner.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		LAATresholdSpinner.setValues(LAATresholdSpinnerPositionOf(DefaultPreferences.LAA_DEFAULT_VALUE), 0, 
				(int) Math.pow(10, numberOfDecimals + 1), numberOfDecimals, 1, 10);
		
		new Label(parent, SWT.NONE).setText("Consider static field access:");
		considerStaticFiledAccessCheckbox = new Button(parent, SWT.CHECK);
		
		new Label(parent, SWT.NONE).setText("Library check:");
		libraryCheckCheckbox = new Button(parent, SWT.CHECK);

		parent.addControlListener(new ControlListener() {		
			@Override
			public void controlResized(ControlEvent e) {
				parent.pack();
			}
			@Override
			public void controlMoved(ControlEvent e) {}
		});
		
		parent.pack();
	}

	@Override
	protected void doLoad() {
		MethodDefragmenterPropertyStore propertyStore = (MethodDefragmenterPropertyStore) getPreferenceStore();
		Map<String, String> map = propertyStore.toMap();
		if(map.isEmpty()) {
			groupingAlgorithmSelectionCombo.select(Integer.parseInt(DefaultPreferences.GROUPING_ALGORITHM_DEFAULT_VALUE));
			ATFDTresholdSpinner.setSelection(Integer.parseInt(DefaultPreferences.ATFD_DEFAULT_VALUE));
			FDPTresholdSpinner.setSelection(Integer.parseInt(DefaultPreferences.FDP_DEFAULT_VALUE));
			LAATresholdSpinner.setSelection(LAATresholdSpinnerPositionOf(DefaultPreferences.LAA_DEFAULT_VALUE));
			considerStaticFiledAccessCheckbox.setSelection(Boolean.parseBoolean(DefaultPreferences.CONSIDER_STATIC_FIELD_ACCESS_DEFAULT_VALUE));
			libraryCheckCheckbox.setSelection(Boolean.parseBoolean(DefaultPreferences.LIBRARY_CHECK_DEFAULT_VALUE));
		} else {
			groupingAlgorithmSelectionCombo.select(Integer.parseInt(map.get(DefaultPreferences.GROUPING_ALGORITHM_PREFERENCE_NAME)));
			ATFDTresholdSpinner.setSelection(Integer.parseInt(map.get(DefaultPreferences.ATFD_PREFERENCE_NAME)));
			FDPTresholdSpinner.setSelection(Integer.parseInt(map.get(DefaultPreferences.FDP_PREFERENCE_NAME)));
			LAATresholdSpinner.setSelection(LAATresholdSpinnerPositionOf(map.get(DefaultPreferences.LAA_PREFERENCE_NAME)));
			considerStaticFiledAccessCheckbox.setSelection(Boolean.parseBoolean(map.get(DefaultPreferences.CONSIDER_STATIC_FIELD_ACCESS_PREFERENCE_NAME)));
			libraryCheckCheckbox.setSelection(Boolean.parseBoolean(map.get(DefaultPreferences.LIBRARY_CHECK_PREFERENCE_NAME)));
		}
	}

	@Override
	protected void doLoadDefault() {
		MethodDefragmenterPropertyStore propertyStore = (MethodDefragmenterPropertyStore) getPreferenceStore();
		propertyStore.setToDefault(DefaultPreferences.GROUPING_ALGORITHM_PREFERENCE_NAME);
		propertyStore.setToDefault(DefaultPreferences.ATFD_PREFERENCE_NAME);
		propertyStore.setToDefault(DefaultPreferences.FDP_PREFERENCE_NAME);
		propertyStore.setToDefault(DefaultPreferences.LAA_PREFERENCE_NAME);
		propertyStore.setToDefault(DefaultPreferences.CONSIDER_STATIC_FIELD_ACCESS_PREFERENCE_NAME);
		propertyStore.setToDefault(DefaultPreferences.LIBRARY_CHECK_PREFERENCE_NAME);
		doLoad();
	}

	@Override
	protected void doStore() {
		MethodDefragmenterPropertyStore propertyStore = (MethodDefragmenterPropertyStore) getPreferenceStore();
		propertyStore.setBindings(DefaultPreferences.GROUPING_ALGORITHM_PREFERENCE_NAME, String.valueOf(groupingAlgorithmSelectionCombo.getSelectionIndex()));
		propertyStore.setBindings(DefaultPreferences.ATFD_PREFERENCE_NAME, ATFDTresholdSpinner.getText());
		propertyStore.setBindings(DefaultPreferences.FDP_PREFERENCE_NAME, FDPTresholdSpinner.getText());
		propertyStore.setBindings(DefaultPreferences.LAA_PREFERENCE_NAME, LAATresholdSpinner.getText());
		propertyStore.setBindings(DefaultPreferences.CONSIDER_STATIC_FIELD_ACCESS_PREFERENCE_NAME, String.valueOf(considerStaticFiledAccessCheckbox.getSelection()));
		propertyStore.setBindings(DefaultPreferences.LIBRARY_CHECK_PREFERENCE_NAME, String.valueOf(libraryCheckCheckbox.getSelection()));
	}

	@Override
	public void store() {
		doStore();
		MethodDefragmenterPropertyStore propertyStore = (MethodDefragmenterPropertyStore) getPreferenceStore();
		propertyStore.doSave();
	}

	@Override
	protected void adjustForNumColumns(int numColumns) {
		return;
	}
}