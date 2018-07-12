package kar.method.defragmenter.views;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Table;

public class EnviousNodeTableViewer extends TableViewer{

	 
	 public ColumnLabelProvider linesColumnLabel =  new ColumnLabelProvider(){
		 @Override
		 public String getText(Object element) {
			 EnviousNodeData item = (EnviousNodeData) element;
			 return item.getLines();
		 }
	 };

	 public ColumnLabelProvider acdColumnLabel =  new ColumnLabelProvider(){
		 @Override
		 public String getText(Object element) {
			 EnviousNodeData item = (EnviousNodeData) element;
			 return item.getAccessForeignData() + "";
		 }
	 };
	 
	 public ColumnLabelProvider localAttrColumnLabel =  new ColumnLabelProvider(){
		 @Override
		 public String getText(Object element) {
			 EnviousNodeData item = (EnviousNodeData) element;
			 return 1.0 * item.getLocalAttrAccess() / (item.getLocalAttrAccess() + item.getAccessForeignData()) + "";
		 }
	 };
	 
	 public ColumnLabelProvider foreignDataColumnLabel =  new ColumnLabelProvider(){
		 @Override
		 public String getText(Object element) {
			 EnviousNodeData item = (EnviousNodeData) element;
			 return item.getForeignDataProviders() + "";
		 }
	 };
	 
	 public ColumnLabelProvider targetClassColumnLabel =  new ColumnLabelProvider(){
		 @Override
		 public String getText(Object element) {
			 EnviousNodeData item = (EnviousNodeData) element;
			 return item.getTargetClass();
		 }
	 };
	 
	 
	 
	 
	 
	public EnviousNodeTableViewer(Composite parent, int style) {
		super(parent, style);
		Table table = getTable();
		GridData gridData = new GridData(SWT.V_SCROLL, SWT.V_SCROLL, true, true);
		table.setLayoutData(gridData);
		createColumns();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		setContentProvider(new ArrayContentProvider());
	
		
	}
	


	private void createColumns() {

		  TableViewerColumn lineType = createTableViewerColumn("Lines (From - To)", 100, 0);
		  lineType.setLabelProvider(linesColumnLabel);
		  
		  TableViewerColumn acdType = createTableViewerColumn("ATFD", 50, 1);
		  acdType.setLabelProvider(acdColumnLabel);
		  
		  TableViewerColumn localAttrType = createTableViewerColumn("LAA", 50, 2);
		  localAttrType.setLabelProvider(localAttrColumnLabel);
		  
		  TableViewerColumn foreignDataAttrType = createTableViewerColumn("FDP", 50, 3);
		  foreignDataAttrType.setLabelProvider(foreignDataColumnLabel);
		  		  
		  TableViewerColumn targetClassType = createTableViewerColumn("Providers", 250, 4);
		  targetClassType.setLabelProvider(targetClassColumnLabel);
		  
		  Layout layout = this.getControl().getParent().getLayout();
		  if (layout instanceof TableColumnLayout) {
		    ((TableColumnLayout)layout).setColumnData(lineType.getColumn(),new ColumnWeightData(0,100,true));
		    ((TableColumnLayout)layout).setColumnData(acdType.getColumn(),new ColumnWeightData(1,50,true));
		    ((TableColumnLayout)layout).setColumnData(localAttrType.getColumn(),new ColumnWeightData(2,50,true));
		    ((TableColumnLayout)layout).setColumnData(foreignDataAttrType.getColumn(),new ColumnWeightData(3,50,true));
		    ((TableColumnLayout)layout).setColumnData(targetClassType.getColumn(),new ColumnWeightData(4,250,true));

		  }
	}
	
	private TableViewerColumn createTableViewerColumn(String header, int width, int idx) 
	{
	    TableViewerColumn column = new TableViewerColumn(this, SWT.LEFT, idx);
	    column.getColumn().setText(header);
	    column.getColumn().setWidth(width);
	    column.getColumn().setResizable(true);
	    column.getColumn().setMoveable(true);

	    return column;
	}
	
}
