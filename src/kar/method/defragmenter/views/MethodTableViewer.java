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

public class MethodTableViewer extends TableViewer{
	 
	public ColumnLabelProvider typeColumnLabel =  new ColumnLabelProvider(){
		 @Override
		 public String getText(Object element) {
			 MethodDescribingItem item = (MethodDescribingItem) element;
			 return item.getNodeType();
		 }
	 };
	 
	 public ColumnLabelProvider linesColumnLabel =  new ColumnLabelProvider(){
		 @Override
		 public String getText(Object element) {
			 MethodDescribingItem item = (MethodDescribingItem) element;
			 return item.getLines();
		 }
	 };

	 public ColumnLabelProvider cocpColumnLabel =  new ColumnLabelProvider(){
		 @Override
		 public String getText(Object element) {
			 MethodDescribingItem item = (MethodDescribingItem) element;
			 return item.getCocp();
		 }
	 };
	 
	 public ColumnLabelProvider ncocp2ColumnLabel =  new ColumnLabelProvider(){
		 @Override
		 public String getText(Object element) {
			 MethodDescribingItem item = (MethodDescribingItem) element;
			 return item.getNcocp2();
		 }
	 };
	 
	public MethodTableViewer(Composite parent, int style) {
		super(parent, style);
		Table table = getTable();
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		table.setLayoutData(gridData);
		createColumns();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		setContentProvider(new ArrayContentProvider());
	
		
	}

	private void createColumns() {
	

		  TableViewerColumn colType = createTableViewerColumn("Node Type", 100, 0);
		  colType.setLabelProvider(typeColumnLabel);

		  TableViewerColumn lineType = createTableViewerColumn("Lines (From - To)", 100, 1);
		  lineType.setLabelProvider(linesColumnLabel);
		  
		  TableViewerColumn cocpType = createTableViewerColumn("COCP", 100, 2);
		  cocpType.setLabelProvider(cocpColumnLabel);
		  
		  TableViewerColumn ncocp2Type = createTableViewerColumn("NCOCP2", 100, 3);
		  ncocp2Type.setLabelProvider(ncocp2ColumnLabel);
		  
		  Layout layout = this.getControl().getParent().getLayout();
		  if (layout instanceof TableColumnLayout) {
		    ((TableColumnLayout)layout).setColumnData(colType.getColumn(),new ColumnWeightData(0,100,true));
		    ((TableColumnLayout)layout).setColumnData(lineType.getColumn(),new ColumnWeightData(1,100,true));
		    ((TableColumnLayout)layout).setColumnData(cocpType.getColumn(),new ColumnWeightData(2,100,true));
		    ((TableColumnLayout)layout).setColumnData(ncocp2Type.getColumn(),new ColumnWeightData(3,100,true));
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
