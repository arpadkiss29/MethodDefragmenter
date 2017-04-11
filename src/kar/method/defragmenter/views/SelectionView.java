package kar.method.defragmenter.views;



import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
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

import kar.method.defragmenter.utils.CodeFragmentLeaf;
import kar.method.defragmenter.utils.CodeFragmentTreeNode;
import kar.method.defragmenter.visittors.BlockVisitor;
import kar.method.defragmenter.visittors.MethodVisitor;


public class SelectionView extends ViewPart {

	public static final String MARKER = "com.ibm.mymarkers.mymarker";
	private MultipleMethodViewer tableViewer;
	private MethodTableViewer methodTableViewer;
	private HashMap<String, ICompilationUnit> classList = new HashMap<String, ICompilationUnit>();

	private ISelectionListener listener = new ISelectionListener() {
		public void selectionChanged(IWorkbenchPart sourcepart, ISelection selection) {
			if (sourcepart != SelectionView.this) {
				try {
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
		setContentDescription(sourcepart.getTitle() + " (" + selection.getClass().getName() + ")");
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			if (ss.getFirstElement() instanceof IJavaProject){
				tableViewer.setInput(null);
				tableViewer.refresh();
				System.out.println("A project was selected!");
				List<MethodBasicItem> methodItems =  new ArrayList<MethodBasicItem>();
				IProject selectedProject = ((IJavaProject)ss.getFirstElement()).getProject();

				IPackageFragment[] packages = JavaCore.create(selectedProject).getPackageFragments();
				for (IPackageFragment mypackage : packages) {
					if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
						for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
							CompilationUnit parsedUnit = parse(unit);
							List<AbstractTypeDeclaration> dcls = parsedUnit.types();
							this.classList.put(dcls.get(0).getName().getIdentifier(), unit);
							methodItems.addAll(applyDefragmenterForCompUnit(parse(unit)));
						}
					}
				}


				Collections.sort(methodItems, new Comparator<MethodBasicItem>() {

					@Override
					public int compare(MethodBasicItem o1, MethodBasicItem o2) {

						return o2.getRootNCOCP2().compareTo(o1.getRootNCOCP2());
					}
				});


				tableViewer.setInput(methodItems);



			}
			if (ss.getFirstElement() instanceof ICompilationUnit){
				tableViewer.setInput(null);
				tableViewer.refresh();
				ICompilationUnit unit = (ICompilationUnit) ss.getFirstElement();
				CompilationUnit parsedUnit = parse(unit);
				List<MethodBasicItem> methodItems =  new ArrayList<MethodBasicItem>();
				List<AbstractTypeDeclaration> dcls = parsedUnit.types();
				this.classList.put(dcls.get(0).getName().getIdentifier(), unit);
				methodItems.addAll(applyDefragmenterForCompUnit(parse(unit)));
				Collections.sort(methodItems, new Comparator<MethodBasicItem>() {

					@Override
					public int compare(MethodBasicItem o1, MethodBasicItem o2) {

						return o2.getRootNCOCP2().compareTo(o1.getRootNCOCP2());
					}
				});
				tableViewer.setInput(methodItems);
				tableViewer.refresh();
			}	
		}
	}

	private List<MethodBasicItem> applyDefragmenterForCompUnit(CompilationUnit unit){
		MethodVisitor visitorMethod = new MethodVisitor();
		BlockVisitor visitorBlock = new BlockVisitor();
		List<MethodBasicItem> methodItems = new ArrayList<MethodBasicItem>();
		unit.accept(visitorMethod);


		for (MethodDeclaration method : visitorMethod.getMethods()) {
			System.out.println("Method name: " + method.getName() + " Return type: " + method.getReturnType2());
			MethodBasicItem item = new MethodBasicItem();
			/*
			 * Visit all blocks of code in each method
			 */
			method.accept(visitorBlock);
			if(!visitorBlock.lastNode.empty()){
				CodeFragmentTreeNode root = visitorBlock.lastNode.pop();
				CodeFragmentTreeNode.colorCounter = 0;
				System.out.println(root);
				root.print(0);
				root.getCohesionMetric(unit);
				List<CodeFragmentTreeNode> identifiedNodes = root.identifyFunctionalSegments();
				root.combineNodes(identifiedNodes);

				//			root.colorFragemnts(textEditor, file, identifiedNodes);
				List<AbstractTypeDeclaration> dcls = unit.types();
				item.setClassName(dcls.get(0).getName().getIdentifier());
				item.setName(method.getName().toString());
				item.setLines(unit.getLineNumber(method.getStartPosition()) + " - " + unit.getLineNumber(method.getStartPosition() + method.getLength()));
				if(method.getReturnType2() == null){
					item.setReturnType("");
				}else{
					item.setReturnType(method.getReturnType2().toString());
				}
				item.setRootNCOCP2("" + root.getNodeNCOCP2());
				methodItems.add(item);
			}
		}

		return methodItems;
	}

	private List<CodeFragmentTreeNode> applyDefragmenterForMethod(MethodDeclaration method, BlockVisitor visitorBlock, ICompilationUnit unit){
		List<CodeFragmentTreeNode> nodes = new ArrayList<CodeFragmentTreeNode>();
		IFile ifile = null;
		if (unit.getResource().getType()== IResource.FILE) {
			ifile = (IFile) unit.getResource();
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			try {
				IDE.openEditor(page, ifile, true);
			} catch (PartInitException e) {

				e.printStackTrace();
			}
		}
		ITextEditor textEditor = (ITextEditor) getActiveWorkbenchWindow().getActivePage().getActiveEditor();


		CompilationUnit parsedUnit = parse(unit);
		method.accept(visitorBlock);
		CodeFragmentTreeNode root = visitorBlock.lastNode.pop();
		CodeFragmentTreeNode.colorCounter = 0;
		CodeFragmentTreeNode.allNodesLeafs.clear();
		root.getCohesionMetric(parsedUnit);
		List<CodeFragmentTreeNode> identifiedNodes = root.identifyFunctionalSegments();
		root.combineNodes(identifiedNodes);

		root.colorFragemnts(textEditor, ifile, identifiedNodes);
		root.getAllTreeData();
		nodes = CodeFragmentTreeNode.allNodesLeafs;
		nodes.add(root);

		return nodes;
	}

	private void populateMethodTable(String methodName, String className){
		List<MethodDescribingItem> methodTableItems = null;
		List<CodeFragmentTreeNode> nodes = null;
		ICompilationUnit unit = classList.get(className);
		CompilationUnit parsedUnit = parse(unit);
		if (unit != null){
			methodTableItems = new ArrayList<MethodDescribingItem>();
			MethodVisitor visitorMethod = new MethodVisitor();
			BlockVisitor visitorBlock = new BlockVisitor();
			parsedUnit.accept(visitorMethod);
			for (MethodDeclaration method : visitorMethod.getMethods()) {
				if (method.getName().toString().equalsIgnoreCase(methodName)){
					nodes = applyDefragmenterForMethod(method, visitorBlock, unit);
				}
			}
			if (nodes != null){
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

			}

		}
		methodTableViewer.setInput(methodTableItems);
	}


	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout(1, true);

		SashForm form = new SashForm(parent, SWT.VERTICAL);
		form.setLayout(layout);
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite methodTableComp = new Composite(form,SWT.NONE);
		methodTableComp.setLayout(new TableColumnLayout());
		methodTableViewer = new MethodTableViewer(methodTableComp,SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);

		Composite tableComp=new Composite(form,SWT.NONE);
		tableComp.setLayout(new TableColumnLayout());
		tableViewer=new MultipleMethodViewer(tableComp,SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				StructuredSelection selection = (StructuredSelection)event.getSelection();
				MethodBasicItem selectedItem = (MethodBasicItem)selection.getFirstElement();
				populateMethodTable(selectedItem.getName(), selectedItem.getClassName());
			}
		});
		getSite().setSelectionProvider(methodTableViewer);
		getSite().setSelectionProvider(tableViewer);
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(listener);
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