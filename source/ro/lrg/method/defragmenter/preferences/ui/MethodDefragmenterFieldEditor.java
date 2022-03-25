package ro.lrg.method.defragmenter.preferences.ui;

import java.util.Map;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

import ro.lrg.method.defragmenter.preferences.DefaultPreferences;
import ro.lrg.method.defragmenter.preferences.MethodDefragmenterPropertyStore;

public class MethodDefragmenterFieldEditor extends FieldEditor implements DefaultPreferences {
	private static final int numberOfDecimalsOfLAA = 2;
	private Spinner ATFDTresholdSpinner;
	private Spinner FDPTresholdSpinner;
	private Spinner LAATresholdSpinner;
	private Button applyLongMethodIdentificationCheckbox;
	private Button considerStaticFiledAccessCheckbox;
	private Button libraryCheckCheckbox;
	private Button minBlockSizeCheckbox;
	private Spinner minBlockSizeSpinner;

	public MethodDefragmenterFieldEditor(IJavaProject theProject, Composite parent) {
		createControl(parent);
	}

	@Override
	public int getNumberOfControls() {
		return 2;
	}
	
	private int LAATresholdSpinnerPositionOf(String number) {
		String stringWithSpecifiedNumberOfDecimals = number.substring(0, LAA_DEFAULT_VALUE.indexOf(".") + numberOfDecimalsOfLAA + 1);
		int position = (int) (Double.parseDouble(stringWithSpecifiedNumberOfDecimals) / (1.0 / Math.pow(10, numberOfDecimalsOfLAA)));
		return position;
	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		new Label(parent, SWT.NONE).setText("Accesses to foreign data limit (ATFD):");
		ATFDTresholdSpinner = new Spinner (parent, SWT.BORDER);
		ATFDTresholdSpinner.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		ATFDTresholdSpinner.setValues(Integer.parseInt(ATFD_DEFAULT_VALUE), 0, 10, 0, 1, 10);
		
		new Label(parent, SWT.NONE).setText("Foreign data providers limit (FDP):");
		FDPTresholdSpinner = new Spinner (parent, SWT.BORDER);
		FDPTresholdSpinner.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		FDPTresholdSpinner.setValues(Integer.parseInt(FDP_DEFAULT_VALUE), 0, 10, 0, 1, 10);
		
		new Label(parent, SWT.NONE).setText("Local atributes accesses limit (LAA):");
		LAATresholdSpinner = new Spinner (parent, SWT.BORDER);
		LAATresholdSpinner.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		LAATresholdSpinner.setValues(LAATresholdSpinnerPositionOf(LAA_DEFAULT_VALUE), 0, 
				(int) Math.pow(10, numberOfDecimalsOfLAA + 1), numberOfDecimalsOfLAA, 1, 10);
		
		new Label(parent, SWT.NONE).setText("Apply long method identification:");
		applyLongMethodIdentificationCheckbox = new Button(parent, SWT.CHECK);
		
		new Label(parent, SWT.NONE).setText("Consider static field access:");
		considerStaticFiledAccessCheckbox = new Button(parent, SWT.CHECK);
		
		new Label(parent, SWT.NONE).setText("Library check:");
		libraryCheckCheckbox = new Button(parent, SWT.CHECK);
		
		Label minBlockSizeLabel = new Label(parent, SWT.NONE);
		minBlockSizeLabel.setText("Minimum block size:");
		GridData tmp = new GridData();
		tmp.horizontalSpan = 2;
		minBlockSizeLabel.setLayoutData(tmp);
		
		minBlockSizeCheckbox = new Button(parent, SWT.CHECK);
		minBlockSizeCheckbox.setText("Null value");
		
		minBlockSizeSpinner = new Spinner (parent, SWT.BORDER);
		minBlockSizeSpinner.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		minBlockSizeSpinner.setValues(MIN_BLOCK_SIZE_DEFAULT_VALUE.equals(NULL)?0:Integer.parseInt(MIN_BLOCK_SIZE_DEFAULT_VALUE), 0, 10, 0, 1, 10);
		
		minBlockSizeCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
		    public void widgetSelected(SelectionEvent e)
		    {
		        minBlockSizeSpinner.setEnabled(!minBlockSizeSpinner.isEnabled());
		    }
		});
		
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
			ATFDTresholdSpinner.setSelection(Integer.parseInt(ATFD_DEFAULT_VALUE));
			FDPTresholdSpinner.setSelection(Integer.parseInt(FDP_DEFAULT_VALUE));
			LAATresholdSpinner.setSelection(LAATresholdSpinnerPositionOf(LAA_DEFAULT_VALUE));
			applyLongMethodIdentificationCheckbox.setSelection(Boolean.parseBoolean(APPLY_LONG_METHOD_IDENTIFICATION_DEFAULT_VALUE));
			considerStaticFiledAccessCheckbox.setSelection(Boolean.parseBoolean(CONSIDER_STATIC_FIELD_ACCESS_DEFAULT_VALUE));
			libraryCheckCheckbox.setSelection(Boolean.parseBoolean(LIBRARY_CHECK_DEFAULT_VALUE));
			minBlockSizeCheckbox.setSelection(MIN_BLOCK_SIZE_DEFAULT_VALUE.equals(NULL));
			minBlockSizeSpinner.setSelection(MIN_BLOCK_SIZE_DEFAULT_VALUE.equals(NULL)?0:Integer.parseInt(MIN_BLOCK_SIZE_DEFAULT_VALUE));
			minBlockSizeSpinner.setEnabled(!MIN_BLOCK_SIZE_DEFAULT_VALUE.equals(NULL));
		} else {
			ATFDTresholdSpinner.setSelection(Integer.parseInt(map.get(ATFD_PREFERENCE_NAME)));
			FDPTresholdSpinner.setSelection(Integer.parseInt(map.get(FDP_PREFERENCE_NAME)));
			LAATresholdSpinner.setSelection(LAATresholdSpinnerPositionOf(map.get(LAA_PREFERENCE_NAME)));
			applyLongMethodIdentificationCheckbox.setSelection(Boolean.parseBoolean(map.get(APPLY_LONG_METHOD_IDENTIFICATION_PREFERENCE_NAME)));
			considerStaticFiledAccessCheckbox.setSelection(Boolean.parseBoolean(map.get(CONSIDER_STATIC_FIELD_ACCESS_PREFERENCE_NAME)));
			libraryCheckCheckbox.setSelection(Boolean.parseBoolean(map.get(LIBRARY_CHECK_PREFERENCE_NAME)));
			minBlockSizeCheckbox.setSelection(map.get(MIN_BLOCK_SIZE_PREFERENCE_NAME).equals(NULL));
			minBlockSizeSpinner.setSelection(map.get(MIN_BLOCK_SIZE_PREFERENCE_NAME).equals(NULL)?0:Integer.parseInt(map.get(MIN_BLOCK_SIZE_PREFERENCE_NAME)));
			minBlockSizeSpinner.setEnabled(!map.get(MIN_BLOCK_SIZE_PREFERENCE_NAME).equals(NULL));
		}
	}

	@Override
	protected void doLoadDefault() {
		MethodDefragmenterPropertyStore propertyStore = (MethodDefragmenterPropertyStore) getPreferenceStore();
		propertyStore.setToDefault(ATFD_PREFERENCE_NAME);
		propertyStore.setToDefault(FDP_PREFERENCE_NAME);
		propertyStore.setToDefault(LAA_PREFERENCE_NAME);
		propertyStore.setToDefault(APPLY_LONG_METHOD_IDENTIFICATION_PREFERENCE_NAME);
		propertyStore.setToDefault(CONSIDER_STATIC_FIELD_ACCESS_PREFERENCE_NAME);
		propertyStore.setToDefault(LIBRARY_CHECK_PREFERENCE_NAME);
		propertyStore.setToDefault(MIN_BLOCK_SIZE_PREFERENCE_NAME);
		doLoad();
	}

	@Override
	protected void doStore() {
		MethodDefragmenterPropertyStore propertyStore = (MethodDefragmenterPropertyStore) getPreferenceStore();
		propertyStore.setBindings(ATFD_PREFERENCE_NAME, ATFDTresholdSpinner.getText());
		propertyStore.setBindings(FDP_PREFERENCE_NAME, FDPTresholdSpinner.getText());
		propertyStore.setBindings(LAA_PREFERENCE_NAME, LAATresholdSpinner.getText());
		propertyStore.setBindings(APPLY_LONG_METHOD_IDENTIFICATION_PREFERENCE_NAME, "" + applyLongMethodIdentificationCheckbox.getSelection());
		propertyStore.setBindings(CONSIDER_STATIC_FIELD_ACCESS_PREFERENCE_NAME, "" + considerStaticFiledAccessCheckbox.getSelection());
		propertyStore.setBindings(LIBRARY_CHECK_PREFERENCE_NAME, "" + libraryCheckCheckbox.getSelection());
		
		if(minBlockSizeCheckbox.getSelection()) {
			propertyStore.setBindings(MIN_BLOCK_SIZE_PREFERENCE_NAME, NULL);
		} else {
			propertyStore.setBindings(MIN_BLOCK_SIZE_PREFERENCE_NAME, "" + minBlockSizeSpinner.getSelection());
		}
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