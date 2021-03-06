package kar.method.defragmenter.views;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Table;

public class MultipleMethodViewer extends TableViewer{

	public ColumnLabelProvider classNameColumnLabel =  new ColumnLabelProvider(){
		 @Override
		 public String getText(Object element) {
			 MethodBasicItem item = (MethodBasicItem) element;
			 return item.getClassName();
		 }
	 };
	
	public ColumnLabelProvider methodNameColumnLabel =  new ColumnLabelProvider(){
		 @Override
		 public String getText(Object element) {
			 MethodBasicItem item = (MethodBasicItem) element;
			 return item.getName();
		 }
	 };
	 
	 public ColumnLabelProvider methodReturnTypeColumnLabel =  new ColumnLabelProvider(){
		 @Override
		 public String getText(Object element) {
			 MethodBasicItem item = (MethodBasicItem) element;
			 return item.getReturnType();
		 }
	 };
	
	 public ColumnLabelProvider methodLinesColumnLabel =  new ColumnLabelProvider(){
		 @Override
		 public String getText(Object element) {
			 MethodBasicItem item = (MethodBasicItem) element;
			 return item.getLines();
		 }
	 };
	 
	 public ColumnLabelProvider methodLengthColumnLabel = new ColumnLabelProvider(){
		 
		 @Override
		 public String getText(Object element) {
			 MethodBasicItem item = (MethodBasicItem) element;
			 return item.getLength() + "";
		 };
	 };
	 
	 public ColumnLabelProvider rootMetricColumnLabel =  new ColumnLabelProvider(){
		 @Override
		 public String getText(Object element) {
			 MethodBasicItem item = (MethodBasicItem) element;
			 return item.getRootNCOCP2();
		 }
	 };
	 
	 public ColumnLabelProvider containEnviousBlocksLabel =  new ColumnLabelProvider(){
		 @Override
		 public String getText(Object element) {
			 MethodBasicItem item = (MethodBasicItem) element;
			 return item.containEnviousBlocks() + "";
		 }
	 };
	 
	public MultipleMethodViewer(Composite parent, int style) {
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
		  TableViewerColumn colClassName = createTableViewerColumn("Class name", 100, 0);
		  colClassName.setLabelProvider(classNameColumnLabel);

		  TableViewerColumn colType = createTableViewerColumn("Method name", 100, 1);
		  colType.setLabelProvider(methodNameColumnLabel);

		  TableViewerColumn colReturnType = createTableViewerColumn("Return type", 100, 2);
		  colReturnType.setLabelProvider(methodReturnTypeColumnLabel);
		  
		  TableViewerColumn colLines = createTableViewerColumn("Lines (From - To)", 100, 3);
		  colLines.setLabelProvider(methodLinesColumnLabel);
		  
		  //TableViewerColumn colLength = createTableViewerColumn("Length", 100, 4);
		  //colLength.setLabelProvider(methodLengthColumnLabel);
		  
		  //TableViewerColumn colMetric = createTableViewerColumn("Method NCOCP2", 100, 5);
		  //colMetric.setLabelProvider(rootMetricColumnLabel);
		  
		  TableViewerColumn colEnvy = createTableViewerColumn("Envy Blocks", 100, 4);
		  colEnvy.setLabelProvider(containEnviousBlocksLabel);
		  
		  Layout layout = this.getControl().getParent().getLayout();
		  if (layout instanceof TableColumnLayout) {
			((TableColumnLayout)layout).setColumnData(colClassName.getColumn(),new ColumnWeightData(0,100,true));
		    ((TableColumnLayout)layout).setColumnData(colType.getColumn(),new ColumnWeightData(1,100,true));
		    ((TableColumnLayout)layout).setColumnData(colReturnType.getColumn(),new ColumnWeightData(2,100,true));
		    ((TableColumnLayout)layout).setColumnData(colLines.getColumn(),new ColumnWeightData(3,100,true));
		    //((TableColumnLayout)layout).setColumnData(colLength.getColumn(),new ColumnWeightData(4,100,true));
		    //((TableColumnLayout)layout).setColumnData(colMetric.getColumn(),new ColumnWeightData(5,100,true));
		    ((TableColumnLayout)layout).setColumnData(colEnvy.getColumn(),new ColumnWeightData(4,100,true));
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
