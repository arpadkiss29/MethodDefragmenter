package kar.method.defragmenter.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;

import kar.method.defragmenter.fragmenters.AbstractFragmenter;
import kar.method.defragmenter.fragmenters.ChunkFragmenter;
import kar.method.defragmenter.linkers.GroupingAlgorithm2;
import kar.method.defragmenter.utils.AbstractCodeFragment;
import kar.method.defragmenter.utils.CodeFragmentLeaf;
import kar.method.defragmenter.visittors.MethodVisitor;

public class SelectionView extends ViewPart {

	private static final String METHOD_CHUNK_FRAGMENTER = "ChunkFragmenter";
	private static final String METHOD_GRAIN_FRAGMENTER = "GrainFragmenter";
	private static final String MARKER = "kar.method.defragmenter.marker";

	private static final int ATFD_TRESHOLD = 2;
	private static final int FDP_TREHSOLD  = 2;
	
	private MultipleMethodViewer tableViewer;
	private MethodTableViewer methodTableViewer;
	private EnviousNodeTableViewer enviousNodeTableViewer;
	private Map<String, ICompilationUnit> classList = new HashMap<String, ICompilationUnit>();

	private boolean applyLongMethodIdentification = false;
	private boolean expandedFeatureEnvyVerification = true;
	private boolean considerBlankLines = true;
	private boolean considerStaticFieldAccesses = false;
	private boolean libraryCheck = true;
	private String selectedParsingMethod = "";
	
	private Integer minBlockSize = null;
	
	private boolean wholeProjectAnalyzed = false;
	
	private ISelection selectedItem;

	private ISelectionListener listener = new ISelectionListener() {
		
		public void selectionChanged(IWorkbenchPart sourcepart, ISelection selection) {
			if (sourcepart != SelectionView.this && selection instanceof ITreeSelection
					&& (selectedItem == null || !selection.equals(selectedItem))) {
				try {
					selectedItem = selection;
					showSelection(sourcepart, selection);
				} catch (JavaModelException e) {
					e.printStackTrace();
				}
			}
		}
		
		
	};
	

	public static IMarker createMarker(IResource res, Position position)
			throws CoreException {
		IMarker marker = null;
		marker = res.createMarker(MARKER);
		int start = position.getOffset();
		int end = position.getOffset() + position.getLength();
		marker.setAttribute(IMarker.CHAR_START, start);
		marker.setAttribute(IMarker.CHAR_END, end);
		return marker;
	}


	public static void addAnnotation(IMarker marker, ITextEditor editor, String annotationType, Position pos) {
		//The DocumentProvider enables to get the document currently loaded in the editor
		IDocumentProvider idp = editor.getDocumentProvider();

		//This is the document we want to connect to. This is taken from 
		//the current editor input.
		IDocument document = idp.getDocument(editor.getEditorInput());

		//The IannotationModel enables to add/remove/change annotation to a Document 
		//loaded in an Editor
		IAnnotationModel iamf = idp.getAnnotationModel(editor.getEditorInput());

		//Note: The annotation type id specify that you want to create one of your 
		//annotations
		SimpleMarkerAnnotation ma = new SimpleMarkerAnnotation(annotationType,marker);

		//Finally add the new annotation to the model
		iamf.connect(document);
		iamf.addAnnotation(ma, pos);
		iamf.disconnect(document);
	}

	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}

	public void showSelection(IWorkbenchPart sourcepart, ISelection selection) throws JavaModelException {
		//setContentDescription(sourcepart.getTitle() + " (" + selection.getClass().getName() + ")");
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			if (ss.getFirstElement() instanceof IJavaProject && !wholeProjectAnalyzed){
//				wholeProjectAnalyzed = true;
//				tableViewer.setInput(null);
//				tableViewer.refresh();
//				System.out.println("A project was selected!");
//				List<MethodBasicItem> methodItems =  new ArrayList<MethodBasicItem>();
//				IProject selectedProject = ((IJavaProject)ss.getFirstElement()).getProject();
//
//				IPackageFragment[] packages = JavaCore.create(selectedProject).getPackageFragments();
//				for (IPackageFragment mypackage : packages) {
//					if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
//						for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
//							CompilationUnit parsedUnit = parse(unit);
//							List<AbstractTypeDeclaration> dcls = parsedUnit.types();
//							if(!dcls.isEmpty()){
//								this.classList.put(dcls.get(0).getName().getIdentifier(), unit);
//								methodItems.addAll(applyDefragmenterForCompUnit(parse(unit)));
//							}
//						}
//					}
//				}
//				  
//				Collections.sort(methodItems, new Comparator<MethodBasicItem>() {
//
//					@Override
//					public int compare(MethodBasicItem o1, MethodBasicItem o2) {
//						return Integer.compare(o2.getLength(), o1.getLength());
//					}
//				});
//				
//				Collections.sort(methodItems, new Comparator<MethodBasicItem>() {
//
//					@Override
//					public int compare(MethodBasicItem o1, MethodBasicItem o2) {
//						return  Boolean.compare(o2.containEnviousBlocks(), o1.containEnviousBlocks());
//					}
//				});
//
//				tableViewer.setInput(methodItems);
			}
			
			if(ss.getFirstElement() instanceof IPackageFragment){
//				wholeProjectAnalyzed = false;
//				tableViewer.setInput(null);
//				tableViewer.refresh();
//				
//				IPackageFragment mypackage = (IPackageFragment) ss.getFirstElement();
//				List<MethodBasicItem> methodItems =  new ArrayList<MethodBasicItem>();
//
//				if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
//					for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
//						CompilationUnit parsedUnit = parse(unit);
//						List<AbstractTypeDeclaration> dcls = parsedUnit.types();
//						if(!dcls.isEmpty()){
//							this.classList.put(dcls.get(0).getName().getIdentifier(), unit);
//							methodItems.addAll(applyDefragmenterForCompUnit(parse(unit)));
//						}
//					}
//					Collections.sort(methodItems, new Comparator<MethodBasicItem>() {
//
//						@Override
//						public int compare(MethodBasicItem o1, MethodBasicItem o2) {
//							return Integer.compare(o2.getLength(), o1.getLength());
//						}
//					});
//					
//					Collections.sort(methodItems, new Comparator<MethodBasicItem>() {
//
//						@Override
//						public int compare(MethodBasicItem o1, MethodBasicItem o2) {
//							return  Boolean.compare(o2.containEnviousBlocks(), o1.containEnviousBlocks());
//						}
//					});
//					tableViewer.setInput(methodItems);
//					tableViewer.refresh();
//				}
			}
			
			
			if (ss.getFirstElement() instanceof ICompilationUnit){
				wholeProjectAnalyzed = false;
				tableViewer.setInput(null);
				tableViewer.refresh();
				ICompilationUnit unit = (ICompilationUnit) ss.getFirstElement();
				CompilationUnit parsedUnit = parse(unit);
				List<MethodBasicItem> methodItems =  new ArrayList<MethodBasicItem>();
				List<AbstractTypeDeclaration> dcls = parsedUnit.types();
				if(!dcls.isEmpty()){
					this.classList.put(dcls.get(0).getName().getIdentifier(), unit);
					methodItems.addAll(applyDefragmenterForCompUnit(parse(unit)));
				}

				
				Collections.sort(methodItems, new Comparator<MethodBasicItem>() {

					@Override
					public int compare(MethodBasicItem o1, MethodBasicItem o2) {
						return Integer.compare(o2.getLength(), o1.getLength());
					}
				});
				
				Collections.sort(methodItems, new Comparator<MethodBasicItem>() {

					@Override
					public int compare(MethodBasicItem o1, MethodBasicItem o2) {
						return  Boolean.compare(o2.containEnviousBlocks(), o1.containEnviousBlocks());
					}
				});
				tableViewer.setInput(methodItems);
				tableViewer.refresh();
			}	
		}
	}

	private List<MethodBasicItem> applyDefragmenterForCompUnit(CompilationUnit unit){
		
		MethodVisitor visitorMethod = new MethodVisitor();
		List<MethodBasicItem> methodItems = new ArrayList<MethodBasicItem>();
		unit.accept(visitorMethod);
		
		for (MethodDeclaration method : visitorMethod.getMethods()) {

			if (method.getBody() == null) continue;
			
			MethodBasicItem item = new MethodBasicItem();

			AbstractFragmenter newVisitorBlock = new ChunkFragmenter(unit, considerBlankLines);
			method.accept(newVisitorBlock);
			AbstractCodeFragment newRoot = newVisitorBlock.lastNode.pop();

			List<AbstractTypeDeclaration> dcls = unit.types();
			item.setClassName(dcls.get(0).getName().getIdentifier());
			item.setName(method.getName().toString());	
			item.setLines(unit.getLineNumber(method.getStartPosition()) + " - " + unit.getLineNumber(method.getStartPosition() + method.getLength()));
			int methodLines = unit.getLineNumber(method.getStartPosition() + method.getLength()) - unit.getLineNumber(method.getStartPosition());
			item.setLength(methodLines);
			item.setNumberOfParams(method.parameters().size());
			if(method.getReturnType2() == null){
				item.setReturnType("");
			}else{
				item.setReturnType(method.getReturnType2().toString());
			}

			if (!expandedFeatureEnvyVerification) {
				String analyzedClass = dcls.get(0).getName().getIdentifier();
				boolean res = newRoot.verifyFeatureEnvy(ATFD_TRESHOLD, FDP_TREHSOLD,  analyzedClass, 
						considerStaticFieldAccesses, minBlockSize, libraryCheck, false);
				item.setMethodRoot(newRoot);
				item.setContainsEnviousBlocks(res);
			} else {
				String analyzedClass = dcls.get(0).getName().getIdentifier();
				boolean res = newRoot.verifyFeatureEnvy(ATFD_TRESHOLD, FDP_TREHSOLD,  analyzedClass, considerStaticFieldAccesses,
						minBlockSize, libraryCheck, false);			
				AbstractCodeFragment linkedRoot =  new GroupingAlgorithm2(ATFD_TRESHOLD, FDP_TREHSOLD,  analyzedClass, 
						considerStaticFieldAccesses, minBlockSize, libraryCheck).tryToLinkBlocks(newRoot);
				item.setMethodRoot(linkedRoot);
				item.setContainsEnviousBlocks(res);
			}
			item.setIMtehodReference((IMethod) method.resolveBinding().getJavaElement());
			methodItems.add(item);
		}
		return methodItems;
	}

	private void populateMethodTable(MethodBasicItem clickedItem){		
		AbstractCodeFragment root = clickedItem.getMethodRoot();

		ICompilationUnit unit = classList.get(clickedItem.getClassName());
		if (unit != null){
			IFile ifile = null;
			CompilationUnit parsedUnit = parse(unit);
			if (unit.getResource().getType()== IResource.FILE) {
				ifile = (IFile) unit.getResource();
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IEditorPart editor = IDE.openEditor(page, ifile, true);

					JavaUI.revealInEditor(editor, (IJavaElement)clickedItem.getIMtehodReference());
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}

			ITextEditor textEditor = (ITextEditor) getActiveWorkbenchWindow().getActivePage().getActiveEditor();

			if(!applyLongMethodIdentification){
				System.out.println("Method coloring will be performed here!");
				AbstractCodeFragment.colorCounter = 0;
				try {
					root.init();
					enviousNodeTableViewer.setInput(null);
					List<AbstractCodeFragment> nodes = root.getAllEnviousNodes();
					
					List<EnviousNodeData> enviousNodeItems = new ArrayList<>();
					
					for(AbstractCodeFragment node : nodes){
						if(node instanceof CodeFragmentLeaf && ((CodeFragmentLeaf)node).isEnvy()){
							CodeFragmentLeaf leaf = (CodeFragmentLeaf)node;
							EnviousNodeData enviousItem = new EnviousNodeData();
							enviousItem.setLines(parsedUnit.getLineNumber(((CodeFragmentLeaf)node).getFragmentFirstLine()) + " - " + parsedUnit.getLineNumber(((CodeFragmentLeaf)node).getFragmentLastLine()));
							enviousItem.setAccessForeignData(leaf.getAccessForeignData());
							enviousItem.setForeignDataProviders(leaf.getForeignDataProviders());
							enviousItem.setLocalAttrAccess(leaf.getLocalAttrAccess());
							enviousItem.setTargetClass(leaf.getTargetClass());
							enviousNodeItems.add(enviousItem);
						}else if(expandedFeatureEnvyVerification && node.isEnvy()){
							EnviousNodeData enviousItem = new EnviousNodeData();
							enviousItem.setLines(parsedUnit.getLineNumber(node.getStartNode()) + " - " + parsedUnit.getLineNumber(node.getEndNode()));
							enviousItem.setAccessForeignData(node.getAccessForeignData());
							enviousItem.setForeignDataProviders(node.getForeignDataProviders());
							enviousItem.setLocalAttrAccess(node.getLocalAttrAccess());
							enviousItem.setTargetClass(node.getTargetClass());
							enviousNodeItems.add(enviousItem);
						}
					}
					
					root.colorEnvyLeafNodes(textEditor, ifile);
					
					enviousNodeTableViewer.setInput(enviousNodeItems);
				} catch (CoreException e) {
					e.printStackTrace();
				}
				
			}else{
				System.out.println("Calculating Long Method Fragmentation! Threshold: " + AbstractCodeFragment.NCOCP2Treshold);
				
				root.init();
				
				List<MethodDescribingItem> methodTableItems = new ArrayList<>();
				List<AbstractCodeFragment> nodes = null;

				AbstractCodeFragment.colorCounter = 0;
				AbstractCodeFragment.allNodesLeafs.clear();
				root.getCohesionMetric(parsedUnit);
				List<AbstractCodeFragment> identifiedNodes = root.identifyFunctionalSegments();
				root.combineNodes(identifiedNodes);

				root.colorLongMethodFragments(textEditor, ifile, identifiedNodes);
				root.getAllTreeData();
				nodes = AbstractCodeFragment.allNodesLeafs;
				nodes.add(root);

				MethodDescribingItem item = null;
				for (int i = nodes.size() - 1; i >= 0; i--){
					item = new MethodDescribingItem();
					if (nodes.get(i) instanceof CodeFragmentLeaf){
						item.setNodeType("Leaf fragement");
						item.setLines(parsedUnit.getLineNumber(((CodeFragmentLeaf)nodes.get(i)).getFragmentFirstLine()) + " - " + parsedUnit.getLineNumber(((CodeFragmentLeaf)nodes.get(i)).getFragmentLastLine()));
						item.setCocp("");
						item.setNcocp2("");
					}else{
						item.setNodeType("Node fragement");
						item.setLines(parsedUnit.getLineNumber(nodes.get(i).getStartNode()) + " - " + parsedUnit.getLineNumber(nodes.get(i).getEndNode()));
						item.setCocp(Double.toString(nodes.get(i).getNodeCOCP()));
						item.setNcocp2(Double.toString(nodes.get(i).getNodeNCOCP2()));
					}
					methodTableItems.add(item);
				}
				methodTableViewer.setInput(methodTableItems);
			}
		}

	}


	public void createPartControl(Composite parent) {
		GridLayout mainLayout = new GridLayout(1,false);
		parent.setLayout(mainLayout);

		Composite firstRow = new Composite(parent, SWT.FILL);
		firstRow.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout firstRowLayout = new GridLayout();
		firstRowLayout.numColumns = 4;
		firstRow.setLayout(firstRowLayout);

		GridData gridData = new GridData(GridData.FILL);
		gridData.widthHint = 250;

//		Label lblDetectionMethod = new Label(firstRow, SWT.NULL);
//		lblDetectionMethod.setText("Detection Method:");
		GridData detectionMethodGridData = new GridData();
		detectionMethodGridData.widthHint = 250;
//		lblDetectionMethod.setLayoutData(detectionMethodGridData);
//
//		final Combo cmbMethods = new Combo(firstRow, SWT.NULL);
//		cmbMethods.setItems(new String[] {METHOD_GRAIN_FRAGMENTER, METHOD_CHUNK_FRAGMENTER});
//		GridData cmbGridData = new GridData();
//		gridData.widthHint = 150;
//		cmbMethods.setLayoutData(cmbGridData);
//		cmbMethods.addSelectionListener(new SelectionListener() {
//			
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				selectedParsingMethod = cmbMethods.getText();
//			}
//			
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {	
//			}
//		});
//		cmbMethods.select(1);
		selectedParsingMethod = METHOD_CHUNK_FRAGMENTER;
		
		Button btnExpandedFeatureEnvy = new Button(firstRow, SWT.CHECK | SWT.BORDER);
		btnExpandedFeatureEnvy.setText("Expanded Feature Envy");
		btnExpandedFeatureEnvy.setLayoutData(detectionMethodGridData);
		btnExpandedFeatureEnvy.setSelection(expandedFeatureEnvyVerification);
		btnExpandedFeatureEnvy.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				Button btn = (Button) e.getSource();
				expandedFeatureEnvyVerification = btn.getSelection();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		Button btnConsiderBlankLines = new Button(firstRow, SWT.CHECK | SWT.BORDER);
		btnConsiderBlankLines.setText("Consider Blank Lines");
		btnConsiderBlankLines.setLayoutData(detectionMethodGridData);
		btnConsiderBlankLines.setSelection(considerBlankLines);
		btnConsiderBlankLines.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				Button btn = (Button) e.getSource();
				considerBlankLines = btn.getSelection();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
			
//		Button btnConsiderStaticFieldAccesses = new Button(firstRow, SWT.CHECK | SWT.WRAP | SWT.BORDER);
//		btnConsiderStaticFieldAccesses.setText("Consider Static Field Accesses");
//		btnConsiderStaticFieldAccesses.setLayoutData(detectionMethodGridData);
//		btnConsiderStaticFieldAccesses.setSelection(considerStaticFieldAccesses);
//		btnConsiderStaticFieldAccesses.addSelectionListener(new SelectionListener() {
//
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				Button btn = (Button) e.getSource();
//				considerStaticFieldAccesses = btn.getSelection();
//			}
//
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//			}
//		});
			
		
//		Label lblMinBlockSize = new Label(firstRow, SWT.NULL);
//		lblMinBlockSize.setText("Min. Block Size: ");
//		GridData thresholdGridData = new GridData();
//		thresholdGridData.widthHint = 80;
//		lblMinBlockSize.setLayoutData(thresholdGridData);
//		
//		final Text txtMinBlockSize = new Text(firstRow,  SWT.NULL | SWT.BORDER);
//		GridData txtGridData = new GridData(GridData.FILL);
//		txtGridData.widthHint = 30;
//		txtMinBlockSize.setLayoutData(txtGridData);
//		txtMinBlockSize.addModifyListener(new ModifyListener() {
//			
//			@Override
//			public void modifyText(ModifyEvent e) {
//				minBlockSize = Integer.parseInt(txtMinBlockSize.getText() + "");
////				applyDefragmenterForCompUnit
//			}
//		});
		
	
		
//		Button btnLongMethodIdentif = new Button(firstRow, SWT.CHECK | SWT.WRAP | SWT.BORDER);
//		btnLongMethodIdentif.setAlignment(SWT.RIGHT);
//		btnLongMethodIdentif.setOrientation(SWT.RIGHT_TO_LEFT);
////		btnLongMethodIdentif.setBackground(new org.eclipse.swt.graphics.Color(btnLongMethodIdentif., 0, 0, 0));
//		btnLongMethodIdentif.setText("Long Method Identification");
//		btnLongMethodIdentif.setLayoutData(detectionMethodGridData);
//		btnLongMethodIdentif.setSelection(applyLongMethodIdentification);
//		btnLongMethodIdentif.addSelectionListener(new SelectionListener() {
//
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				Button btn = (Button) e.getSource();
//				applyLongMethodIdentification = btn.getSelection();
//			}
//
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//			}
//		});
		

		
		
//		Label lblThreshold = new Label(firstRow, SWT.NULL);
//		lblThreshold.setText("Set Treshold: ");
//		GridData thresholdGridData = new GridData();
//		thresholdGridData.widthHint = 80;
//		lblThreshold.setLayoutData(thresholdGridData);
//
//		final Text txtTreshold = new Text(firstRow,  SWT.NULL | SWT.BORDER);
//		GridData txtGridData = new GridData(GridData.FILL);
//		txtGridData.widthHint = 30;
//		txtTreshold.setLayoutData(txtGridData);
//		txtTreshold.setText(CodeFragmentTreeNode.NCOCP2Treshold + "");
		
		

//		final Button btnConfirmTreshold = new Button(firstRow, SWT.BORDER | SWT.Selection);
//		btnConfirmTreshold.setText("CONFIRM");
//		btnConfirmTreshold.setLayoutData(gridData);
//		btnConfirmTreshold.addMouseListener(new MouseListener() {
//
//			@Override
//			public void mouseUp(MouseEvent e) {
//
//			}
//
//			@Override
//			public void mouseDown(MouseEvent e) {
//
//				if(sourcepartFld != null && selectionFld != null && !txtTreshold.getText().isEmpty()){
//					try {
//						System.out.println("refresh");
//						CodeFragmentTreeNode.NCOCP2Treshold = Double.parseDouble(txtTreshold.getText());
//						showSelection(sourcepartFld, selectionFld);
//
//					} catch (Exception e1) {
//						e1.printStackTrace();
//					}
//					txtTreshold.setEnabled(false);
//
//					btnConfirmTreshold.forceFocus();
//					btnConfirmTreshold.setFocus();
//					btnConfirmTreshold.requestLayout();
//					btnConfirmTreshold.setSelection(true);
//
//					txtTreshold.setEnabled(true);
//				}
//
//			}
//
//			@Override
//			public void mouseDoubleClick(MouseEvent e) {
//			}
//		});


//		final Button btnExtractMethod = new Button(firstRow, SWT.BORDER | SWT.Selection);
//		btnExtractMethod.setText("EXTRACT");
//		btnExtractMethod.setLayoutData(gridData);
//		btnExtractMethod.addMouseListener(new MouseListener() {
//
//			@Override
//			public void mouseUp(MouseEvent e) {
//			}
//
//			@Override
//			public void mouseDown(MouseEvent e) {
//				System.out.println("Will perfom extraction");
//
//			}
//
//			@Override
//			public void mouseDoubleClick(MouseEvent e) {
//			}
//		});

//		final Button btnUndoExtract = new Button(firstRow, SWT.BORDER | SWT.Selection);
//		btnUndoExtract.setText("UNDO");
//		btnUndoExtract.setLayoutData(gridData);
//		btnUndoExtract.addMouseListener(new MouseListener() {
//
//			@Override
//			public void mouseUp(MouseEvent e) {
//			}
//
//			@Override
//			public void mouseDown(MouseEvent e) {
//				System.out.println("Undo extract");
//
//			}
//
//			@Override
//			public void mouseDoubleClick(MouseEvent e) {
//			}
//		});

		Composite tableComposite = new Composite(parent, SWT.FILL);
		tableComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout tableLayout = new GridLayout();
		tableLayout.numColumns = 2;
		tableComposite.setLayout(tableLayout);

		GridData tableGrid = new GridData(GridData.FILL_BOTH);
		tableGrid.grabExcessVerticalSpace = true;
		tableGrid.grabExcessHorizontalSpace = true;

		Composite tableComp=new Composite(tableComposite,SWT.FILL);
		tableComp.setLayout(new TableColumnLayout());
		GridData tblGrid = new GridData(GridData.FILL_BOTH);
		tblGrid.widthHint = 700;
		tableComp.setLayoutData(tblGrid);
		tableViewer=new MultipleMethodViewer(tableComp,SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				StructuredSelection selection = (StructuredSelection)event.getSelection();
				MethodBasicItem selectedItem = (MethodBasicItem)selection.getFirstElement();
				populateMethodTable(selectedItem);
			}
		});

//		Composite methodTableComp = new Composite(tableComposite,SWT.FILL);
//		methodTableComp.setLayout(new TableColumnLayout());
//		methodTableComp.setLayoutData(tableGrid);
//		methodTableViewer = new MethodTableViewer(methodTableComp,SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
//		
//		methodTableComp.setVisible(false);
		
		Composite enviousNodeTableComp = new Composite(tableComposite,SWT.FILL);
		enviousNodeTableComp.setLayout(new TableColumnLayout());
		enviousNodeTableComp.setLayoutData(tableGrid);
		enviousNodeTableViewer = new EnviousNodeTableViewer(enviousNodeTableComp,SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		
	
		if(listener != null){
			getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(listener);
		}
		getSite().setSelectionProvider(enviousNodeTableViewer);
		getSite().setSelectionProvider(tableViewer);
	}

	public void setFocus() {
		tableViewer.getTable().setFocus();
	}

	public void dispose() {
		getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(listener);
		super.dispose();
	}


	@SuppressWarnings("deprecation")
	private static CompilationUnit parse(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null);
	}

}