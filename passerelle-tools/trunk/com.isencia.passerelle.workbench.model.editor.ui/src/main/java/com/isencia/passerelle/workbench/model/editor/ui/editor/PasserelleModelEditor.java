package com.isencia.passerelle.workbench.model.editor.ui.editor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.parts.ScrollableThumbnail;
import org.eclipse.draw2d.parts.Thumbnail;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.MouseWheelHandler;
import org.eclipse.gef.MouseWheelZoomHandler;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.dnd.TemplateTransferDragSourceListener;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.AlignmentAction;
import org.eclipse.gef.ui.actions.DirectEditAction;
import org.eclipse.gef.ui.actions.EditorPartAction;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.MatchHeightAction;
import org.eclipse.gef.ui.actions.MatchWidthAction;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.gef.ui.actions.StackAction;
import org.eclipse.gef.ui.actions.ToggleGridAction;
import org.eclipse.gef.ui.actions.ToggleRulerVisibilityAction;
import org.eclipse.gef.ui.actions.ToggleSnapToGeometryAction;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;
import org.eclipse.gef.ui.parts.ContentOutlinePage;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.gef.ui.parts.TreeViewer;
import org.eclipse.gef.ui.rulers.RulerComposite;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.CompositeActor;
import ptolemy.moml.MoMLParser;

import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.CloseEditorAction;
import com.isencia.passerelle.workbench.model.editor.ui.CopyNodeAction;
import com.isencia.passerelle.workbench.model.editor.ui.CutNodeAction;
import com.isencia.passerelle.workbench.model.editor.ui.PasteNodeAction;
import com.isencia.passerelle.workbench.model.editor.ui.dnd.PasserelleTemplateTransferDropTargetListener;
import com.isencia.passerelle.workbench.model.editor.ui.editpart.EditPartFactory;
import com.isencia.passerelle.workbench.model.editor.ui.editpart.OutlinePartFactory;
import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteBuilder;

public class PasserelleModelEditor extends GraphicalEditorWithFlyoutPalette
		implements ITabbedPropertySheetPageContributor {

	protected static final String PALETTE_DOCK_LOCATION = "Dock location"; //$NON-NLS-1$
	protected static final String PALETTE_SIZE = "Palette Size"; //$NON-NLS-1$
	protected static final String PALETTE_STATE = "Palette state"; //$NON-NLS-1$
	protected static final int DEFAULT_PALETTE_SIZE = 130;
	private int index;
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	private List stackActionIDs = new ArrayList();
	private List editorActionIDs = new ArrayList();
	private List editPartActionIDs = new ArrayList();
	private static Logger logger = LoggerFactory
			.getLogger(PasserelleModelEditor.class);
	public static final int DELETE_KEYCODE = 127;
	public static final int COPY_KEYCODE = 99;
	public static final int PASTE_KEYCODE = 112;

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		updateActions(getSelectionActions());
	}

	private ISelectionListener selectionListener = new ISelectionListener() {
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			updateActions(editPartActionIDs);
		}
	};

	/**
	 * Returns the selection listener.
	 * 
	 * @return the <code>ISelectionListener</code>
	 */
	protected ISelectionListener getSelectionListener() {
		return selectionListener;
	}

	static {
		// LogicPlugin.getDefault().getPreferenceStore().setDefault(
		// PALETTE_SIZE, DEFAULT_PALETTE_SIZE);
	}

	private KeyHandler sharedKeyHandler;
	private PaletteRoot root;
	protected OutlinePage outlinePage;
	protected boolean editorSaving = false;

	private CompositeActor model = new CompositeActor();
	private ResourceTracker resourceListener = new ResourceTracker();
	private RulerComposite rulerComp;
	private MultiPageEditorPart parent;

	public MultiPageEditorPart getParent() {
		return parent;

	}

	public PasserelleModelEditor() {
		setEditDomain(new DefaultEditDomain(this));
	}

	public PasserelleModelEditor(MultiPageEditorPart parent) {
		setEditDomain(new DefaultEditDomain(this));
		this.parent = parent;
	}

	public Logger getLogger() {
		return logger;
	}

	@Override
	public String getContributorId() {
		return "com.isencia.passerelle.workbench.model.editor.ui.editors.modelEditor";
	}

	protected void addStackAction(StackAction action) {
		getActionRegistry().registerAction(action);
		stackActionIDs.add(action.getId());
	}

	protected void addEditPartAction(SelectionAction action) {
		getActionRegistry().registerAction(action);
		editPartActionIDs.add(action.getId());
	}

	protected void addSelectionAction(SelectionAction action) {
		getSelectionActions().add(action);
	}

	protected void addEditorAction(EditorPartAction action) {
		getActionRegistry().registerAction(action);
		editorActionIDs.add(action.getId());
	}

	protected void addAction(IAction action) {
		getActionRegistry().registerAction(action);
	}

	private IPartListener partListener = new IPartListener() {
		// If an open, unsaved file was deleted, query the user to either do a
		// "Save As"
		// or close the editor.
		public void partActivated(IWorkbenchPart part) {
			if (part != PasserelleModelEditor.this)
				return;
			if (!((IFileEditorInput) getEditorInput()).getFile().exists()) {
				Shell shell = getSite().getShell();
				// //// String title =
				// LogicMessages.GraphicalEditor_FILE_DELETED_TITLE_UI;
				// //// String message =
				// LogicMessages.GraphicalEditor_FILE_DELETED_WITHOUT_SAVE_INFO;
				// //// String[] buttons = {
				// LogicMessages.GraphicalEditor_SAVE_BUTTON_UI,
				// //// LogicMessages.GraphicalEditor_CLOSE_BUTTON_UI };
				// // MessageDialog dialog = new MessageDialog(
				// // shell, title, null, message, MessageDialog.QUESTION,
				// buttons, 0);
				// if (dialog.open() == 0) {
				// if (!performSaveAs())
				// partActivated(part);
				// }
				// else {
				// closeEditor(false);
				// }
			}
		}

		public void partBroughtToTop(IWorkbenchPart part) {
		}

		public void partClosed(IWorkbenchPart part) {
		}

		public void partDeactivated(IWorkbenchPart part) {
		}

		public void partOpened(IWorkbenchPart part) {
		}
	};

	protected void closeEditor(boolean save) {
		getSite().getPage().closeEditor(PasserelleModelEditor.this, save);
	}

	public void commandStackChanged(EventObject event) {
		firePropertyChange(IEditorPart.PROP_DIRTY);
		super.commandStackChanged(event);
	}

	protected void configureGraphicalViewer() {
		super.configureGraphicalViewer();
		ScrollingGraphicalViewer viewer = (ScrollingGraphicalViewer) getGraphicalViewer();

		ScalableFreeformRootEditPart root = new ScalableFreeformRootEditPart();

		List<String> zoomLevels = new ArrayList<String>(3);
		zoomLevels.add(ZoomManager.FIT_ALL);
		zoomLevels.add(ZoomManager.FIT_WIDTH);
		zoomLevels.add(ZoomManager.FIT_HEIGHT);
		root.getZoomManager().setZoomLevelContributions(zoomLevels);

		IAction zoomIn = new ZoomInAction(root.getZoomManager());
		IAction zoomOut = new ZoomOutAction(root.getZoomManager());
		getActionRegistry().registerAction(zoomIn);
		getActionRegistry().registerAction(zoomOut);
		getSite().getKeyBindingService().registerAction(zoomIn);
		getSite().getKeyBindingService().registerAction(zoomOut);

		viewer.setRootEditPart(root);

		viewer.setEditPartFactory(createEditPartFactory());
		ContextMenuProvider provider = new PasserelleContextMenuProvider(
				viewer, getActionRegistry());
		viewer.setContextMenu(provider);
		getSite()
				.registerContextMenu(
						"com.isencia.passerelle.workbench.model.editor.ui.editor.contextmenu", //$NON-NLS-1$
						provider, viewer);
		GraphicalViewerKeyHandler graphicalViewerKeyHandler = new GraphicalViewerKeyHandler(
				viewer);
		graphicalViewerKeyHandler.setParent(getCommonKeyHandler());
		
		graphicalViewerKeyHandler.put(KeyStroke.getPressed('X', SWT.CTRL, 0),
				getActionRegistry().getAction(ActionFactory.CUT.getId()));
		viewer.setKeyHandler(graphicalViewerKeyHandler);

		// Zoom with the mouse wheel
		viewer.setProperty(MouseWheelHandler.KeyGenerator.getKey(SWT.CTRL),
				MouseWheelZoomHandler.SINGLETON);

		loadProperties();

		// Actions
		IAction showRulers = new ToggleRulerVisibilityAction(
				getGraphicalViewer());
		getActionRegistry().registerAction(showRulers);

		IAction snapAction = new ToggleSnapToGeometryAction(
				getGraphicalViewer());
		getActionRegistry().registerAction(snapAction);

		IAction showGrid = new ToggleGridAction(getGraphicalViewer());
		getActionRegistry().registerAction(showGrid);

		Listener listener = new Listener() {
			public void handleEvent(Event event) {
				handleActivationChanged(event);
			}
		};
		getGraphicalControl().addListener(SWT.Activate, listener);
		getGraphicalControl().addListener(SWT.Deactivate, listener);
	}

	protected EditPartFactory createEditPartFactory() {
		return new EditPartFactory(parent);
	}

	protected CustomPalettePage createPalettePage() {
		return new CustomPalettePage(getPaletteViewerProvider()) {
			public void init(IPageSite pageSite) {
			}
		};
	}

	protected PaletteViewerProvider createPaletteViewerProvider() {
		return new PaletteViewerProvider(getEditDomain()) {
			private IMenuListener menuListener;

			protected void configurePaletteViewer(PaletteViewer viewer) {
				super.configurePaletteViewer(viewer);
				// viewer.setCustomizer(new LogicPaletteCustomizer());
				viewer
						.addDragSourceListener(new TemplateTransferDragSourceListener(
								viewer));
			}

			protected void hookPaletteViewer(PaletteViewer viewer) {
				super.hookPaletteViewer(viewer);
			}
		};
	}

	public void dispose() {
		getSite().getWorkbenchWindow().getPartService().removePartListener(
				partListener);
		partListener = null;
		((IFileEditorInput) getEditorInput()).getFile().getWorkspace()
				.removeResourceChangeListener(resourceListener);
		super.dispose();
	}

	public void doSave(final IProgressMonitor progressMonitor) {
		editorSaving = true;
		SafeRunner.run(new SafeRunnable() {
			public void run() throws Exception {
				saveProperties();
				CompositeActor diagram = getDiagram();
				StringWriter writer = new StringWriter();
				diagram.exportMoML(writer);
				IFile file = ((IFileEditorInput) getEditorInput()).getFile();
				file.setContents(new ByteArrayInputStream(writer.toString()
						.getBytes()), true, false, progressMonitor);
				getCommandStack().markSaveLocation();
			}
		});

		editorSaving = false;
	}

	public void doSaveAs() {
		performSaveAs();
	}

	public Object getAdapter(Class type) {
		if (type == IContentOutlinePage.class) {
			outlinePage = new OutlinePage(new TreeViewer());
			return outlinePage;
		}
		if (type == ZoomManager.class)
			return getGraphicalViewer().getProperty(
					ZoomManager.class.toString());

		if (IPropertySheetPage.class == type) {
			return new TabbedPropertySheetPage(this);
		}

		return super.getAdapter(type);
	}

	protected Control getGraphicalControl() {
		return rulerComp;
	}

	/**
	 * Returns the KeyHandler with common bindings for both the Outline and
	 * Graphical Views. For example, delete is a common action.
	 */
	protected KeyHandler getCommonKeyHandler() {
		if (sharedKeyHandler == null) {
			sharedKeyHandler = new KeyHandler();
			sharedKeyHandler.put(KeyStroke.getPressed(SWT.F2, 0),
					getActionRegistry().getAction(
							GEFActionConstants.DIRECT_EDIT));
			sharedKeyHandler
					.put(KeyStroke.getPressed(SWT.DEL, 127, 0),
							getActionRegistry().getAction(
									ActionFactory.DELETE.getId()));
			sharedKeyHandler.put(KeyStroke.getPressed('+', SWT.KEYPAD_ADD, 0),
					getActionRegistry().getAction(GEFActionConstants.ZOOM_IN));
			sharedKeyHandler.put(KeyStroke.getPressed('-', SWT.KEYPAD_SUBTRACT,
					0), getActionRegistry().getAction(
					GEFActionConstants.ZOOM_OUT));
		}
		return sharedKeyHandler;
	}

	public CompositeActor getDiagram() {
		return model;
	}

	protected PaletteRoot getPaletteRoot() {
		if (root == null) {
			root = PaletteBuilder.createPalette(this);
		}
		return root;
	}

	public void gotoMarker(IMarker marker) {
	}

	protected void handleActivationChanged(Event event) {
//		IAction copy = null;
//		if (event.type == SWT.Deactivate)
//			copy = getActionRegistry().getAction(ActionFactory.COPY.getId());
//		if (getEditorSite().getActionBars().getGlobalActionHandler(
//				ActionFactory.COPY.getId()) != copy) {
//			getEditorSite().getActionBars().setGlobalActionHandler(
//					ActionFactory.COPY.getId(), copy);
//			getEditorSite().getActionBars().updateActionBars();
//		}
	}

	protected void initializeGraphicalViewer() {
		super.initializeGraphicalViewer();
		getGraphicalViewer().setContents(getDiagram());
		getGraphicalViewer().addDropTargetListener(
				new PasserelleTemplateTransferDropTargetListener(
						getGraphicalViewer()));
		// getGraphicalViewer().addDropTargetListener((TransferDropTargetListener)
		// new TextTransferDropTargetListener(
		// getGraphicalViewer(), TextTransfer.getInstance()));
	}

	protected void createActions() {
		super.createActions();
		ActionRegistry registry = getActionRegistry();
		IAction action;

		// action = new CopyTemplateAction(this);
		// registry.registerAction(action);

		action = new MatchWidthAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new MatchHeightAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		// action = new LogicPasteTemplateAction(this);
		// registry.registerAction(action);
		// getSelectionActions().add(action.getId());
		//

		action = new DirectEditAction((IWorkbenchPart) this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this,
				PositionConstants.LEFT);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this,
				PositionConstants.RIGHT);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this,
				PositionConstants.TOP);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this,
				PositionConstants.BOTTOM);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this,
				PositionConstants.CENTER);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this,
				PositionConstants.MIDDLE);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		CopyNodeAction copyAction = new CopyNodeAction(this);
		registry.registerAction(copyAction);
		getSelectionActions().add(copyAction.getId());
		CutNodeAction cutAction = new CutNodeAction(this);
		registry.registerAction(cutAction);
		getSelectionActions().add(cutAction.getId());
		PasteNodeAction pasteAction = setPasteNodeAction();
		registry.registerAction(pasteAction);
		getSelectionActions().add(pasteAction.getId());

	}

	protected PasteNodeAction setPasteNodeAction() {
		return new PasteNodeAction(this, getDiagram());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.ui.parts.GraphicalEditor#createGraphicalViewer(org.eclipse
	 * .swt.widgets.Composite)
	 */
	protected void createGraphicalViewer(Composite parent) {
		rulerComp = new RulerComposite(parent, SWT.NONE);
		super.createGraphicalViewer(rulerComp);
		ScrollingGraphicalViewer graphicalViewer = (ScrollingGraphicalViewer) getGraphicalViewer();
		rulerComp.setGraphicalViewer(graphicalViewer);

		GraphicalViewerKeyHandler graphicalViewerKeyHandler = new GraphicalViewerKeyHandler(
				graphicalViewer);
		KeyHandler keyHandler = new KeyHandler();

		keyHandler.put(KeyStroke.getPressed(SWT.DEL, 127, 0),
				getActionRegistry().getAction(GEFActionConstants.DELETE));
		graphicalViewerKeyHandler.setParent(keyHandler);
		graphicalViewer.setKeyHandler(graphicalViewerKeyHandler);

	}

	protected FigureCanvas getEditor() {
		return (FigureCanvas) getGraphicalViewer().getControl();
	}

	public boolean isSaveAsAllowed() {
		return true;
	}

	protected void loadProperties() {
		// Ruler properties
		/*
		 * LogicRuler ruler =
		 * getLogicDiagram().getRuler(PositionConstants.WEST); RulerProvider
		 * provider = null; if (ruler != null) { provider = new
		 * LogicRulerProvider(ruler); }
		 * getGraphicalViewer().setProperty(RulerProvider
		 * .PROPERTY_VERTICAL_RULER, provider); ruler =
		 * getLogicDiagram().getRuler(PositionConstants.NORTH); provider = null;
		 * if (ruler != null) { provider = new LogicRulerProvider(ruler); }
		 * getGraphicalViewer
		 * ().setProperty(RulerProvider.PROPERTY_HORIZONTAL_RULER, provider);
		 * getGraphicalViewer
		 * ().setProperty(RulerProvider.PROPERTY_RULER_VISIBILITY, new
		 * Boolean(getLogicDiagram().getRulerVisibility()));
		 * 
		 * // Snap to Geometry property
		 * getGraphicalViewer().setProperty(SnapToGeometry
		 * .PROPERTY_SNAP_ENABLED, new
		 * Boolean(getLogicDiagram().isSnapToGeometryEnabled()));
		 * 
		 * // Grid properties
		 * getGraphicalViewer().setProperty(SnapToGrid.PROPERTY_GRID_ENABLED,
		 * new Boolean(getLogicDiagram().isGridEnabled())); // We keep grid
		 * visibility and enablement in sync
		 * getGraphicalViewer().setProperty(SnapToGrid.PROPERTY_GRID_VISIBLE,
		 * new Boolean(getLogicDiagram().isGridEnabled()));
		 * 
		 * // Zoom ZoomManager manager = (ZoomManager)getGraphicalViewer()
		 * .getProperty(ZoomManager.class.toString()); if (manager != null)
		 * manager.setZoom(getLogicDiagram().getZoom()); // Scroll-wheel Zoom
		 * getGraphicalViewer
		 * ().setProperty(MouseWheelHandler.KeyGenerator.getKey(SWT.MOD1),
		 * MouseWheelZoomHandler.SINGLETON);
		 */

	}

	protected boolean performSaveAs() {
		SaveAsDialog dialog = new SaveAsDialog(getSite().getWorkbenchWindow()
				.getShell());
		dialog.setOriginalFile(((IFileEditorInput) getEditorInput()).getFile());
		dialog.open();
		IPath path = dialog.getResult();

		if (path == null)
			return false;

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IFile file = workspace.getRoot().getFile(path);

		if (!file.exists()) {
			WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
				public void execute(final IProgressMonitor monitor) {
					saveProperties();
					try {
						CompositeActor diagram = getDiagram();
						StringWriter writer = new StringWriter();
						diagram.exportMoML(writer);
						file.create(new ByteArrayInputStream(writer.toString()
								.getBytes()), true, monitor);
						writer.close();
					} catch (Exception e) {
						getLogger().error(
								"Error saving model file : " + file.getName(),
								e);
					}
				}
			};
			try {
				new ProgressMonitorDialog(getSite().getWorkbenchWindow()
						.getShell()).run(false, true, op);
			} catch (Exception e) {
				getLogger().error(
						"Error showing progress monitor during saving of model file : "
								+ file.getName(), e);
			}
		}

		try {
			superSetInput(new FileEditorInput(file));
			getCommandStack().markSaveLocation();
		} catch (Exception e) {
			getLogger().error(
					"Error during re-read of saved model file : "
							+ file.getName(), e);
		}
		return true;
	}

	protected void saveProperties() {
		// getDiagram().setRulerVisibility(((Boolean)getGraphicalViewer()
		// .getProperty(RulerProvider.PROPERTY_RULER_VISIBILITY)).booleanValue());
		// getLogicDiagram().setGridEnabled(((Boolean)getGraphicalViewer()
		// .getProperty(SnapToGrid.PROPERTY_GRID_ENABLED)).booleanValue());
		// getLogicDiagram().setSnapToGeometry(((Boolean)getGraphicalViewer()
		// .getProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED)).booleanValue());
		// ZoomManager manager = (ZoomManager)getGraphicalViewer()
		// .getProperty(ZoomManager.class.toString());
		// if (manager != null)
		// getLogicDiagram().setZoom(manager.getZoom());
	}

	protected void setInput(IEditorInput input) {
		superSetInput(input);

		IFile file = ((IFileEditorInput) input).getFile();
		InputStream is = null;
		try {
			is = file.getContents();
			MoMLParser moMLParser = new MoMLParser();
			CompositeActor compositeActor = (CompositeActor) moMLParser.parse(
					null, is);
			setDiagram(compositeActor);
		} catch (Exception e) {
			getLogger().error(
					"Error during reading/parsing of model file : "
							+ file.getName(), e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// Do Nothing
				}
			}

		}

		if (!editorSaving) {
			if (getGraphicalViewer() != null) {
				getGraphicalViewer().setContents(getDiagram());
				loadProperties();
			}
			if (outlinePage != null) {
				outlinePage.setContents(getDiagram());
			}
		}
	}

	public void setDiagram(CompositeActor diagram) {
		model = diagram;
	}

	protected void superSetInput(IEditorInput input) {
		// The workspace never changes for an editor. So, removing and re-adding
		// the
		// resourceListener is not necessary. But it is being done here for the
		// sake
		// of proper implementation. Plus, the resourceListener needs to be
		// added
		// to the workspace the first time around.
		if (getEditorInput() != null) {
			IFile file = ((IFileEditorInput) getEditorInput()).getFile();
			file.getWorkspace().removeResourceChangeListener(resourceListener);
		}

		super.setInput(input);

		if (getEditorInput() != null) {
			IFile file = ((IFileEditorInput) getEditorInput()).getFile();
			file.getWorkspace().addResourceChangeListener(resourceListener);
			setPartName(file.getName());
		}
	}

	protected void setSite(IWorkbenchPartSite site) {
		super.setSite(site);
		getSite().getWorkbenchWindow().getPartService().addPartListener(
				partListener);
	}

	class OutlinePage extends ContentOutlinePage implements IAdaptable {

		private PageBook pageBook;
		private Control outline;
		private Canvas overview;
		private IAction showOutlineAction, showOverviewAction;
		static final int ID_OUTLINE = 0;
		static final int ID_OVERVIEW = 1;
		private Thumbnail thumbnail;
		private DisposeListener disposeListener;

		public OutlinePage(EditPartViewer viewer) {
			super(viewer);
		}

		public void init(IPageSite pageSite) {
			super.init(pageSite);
			// ActionRegistry registry = getActionRegistry();
			// IActionBars bars = pageSite.getActionBars();
			// String id = ActionFactory.UNDO.getId();
			// bars.setGlobalActionHandler(id, registry.getAction(id));
			// id = ActionFactory.REDO.getId();
			// bars.setGlobalActionHandler(id, registry.getAction(id));
			// id = ActionFactory.DELETE.getId();
			// bars.setGlobalActionHandler(id, registry.getAction(id));
			// bars.updateActionBars();
		}

		protected void configureOutlineViewer() {
			getViewer().setEditDomain(getEditDomain());
			getViewer().setEditPartFactory(new OutlinePartFactory());
			ContextMenuProvider provider = new PasserelleContextMenuProvider(
					getViewer(), getActionRegistry());
			getViewer().setContextMenu(provider);
			getSite()
					.registerContextMenu(
							"com.isencia.passerelle.workbench.model.editor.ui.editor.outline.contextmenu", //$NON-NLS-1$  
							provider, getSite().getSelectionProvider());
			getViewer().setKeyHandler(getCommonKeyHandler());
			IToolBarManager tbm = getSite().getActionBars().getToolBarManager();
			showOutlineAction = new Action() {
				public void run() {
					showPage(ID_OUTLINE);
				}
			};
			showOutlineAction.setImageDescriptor(Activator
					.getImageDescriptor("icons/outline.gif"));
			// showOutlineAction.setToolTipText(LogicMessages.LogicEditor_outline_show_outline);
			tbm.add(showOutlineAction);
			showOverviewAction = new Action() {
				public void run() {
					showPage(ID_OVERVIEW);
				}
			};
			showOverviewAction.setImageDescriptor(Activator
					.getImageDescriptor("icons/overview.gif")); //$NON-NLS-1$
			// showOverviewAction.setToolTipText(LogicMessages.LogicEditor_outline_show_overview);
			tbm.add(showOverviewAction);
			showPage(ID_OVERVIEW);
		}

		public void createControl(Composite parent) {
			pageBook = new PageBook(parent, SWT.NONE);
			outline = getViewer().createControl(pageBook);
			overview = new Canvas(pageBook, SWT.NONE);
			pageBook.showPage(outline);
			configureOutlineViewer();
			hookOutlineViewer();
			initializeOutlineViewer();
			// IActionBars bars = getSite().getActionBars();
			// ActionRegistry ar = getActionRegistry();
			// bars.setGlobalActionHandler(ActionFactory.COPY.getId(), ar
			// .getAction(ActionFactory.COPY.getId()));
			// bars.setGlobalActionHandler(ActionFactory.PASTE.getId(), ar
			// .getAction(ActionFactory.PASTE.getId()));
		}

		public void dispose() {
			unhookOutlineViewer();
			if (thumbnail != null) {
				thumbnail.deactivate();
				thumbnail = null;
			}
			super.dispose();
			PasserelleModelEditor.this.outlinePage = null;
			outlinePage = null;
		}

		public Object getAdapter(Class type) {
			if (type == ZoomManager.class)
				return getGraphicalViewer().getProperty(
						ZoomManager.class.toString());
			return null;
		}

		public Control getControl() {
			return pageBook;
		}

		protected void hookOutlineViewer() {
			getSelectionSynchronizer().addViewer(getViewer());
		}

		protected void initializeOutlineViewer() {
			setContents(getDiagram());
		}

		protected void initializeOverview() {
			LightweightSystem lws = new LightweightSystem(overview);
			RootEditPart rep = getGraphicalViewer().getRootEditPart();
			if (rep instanceof ScalableFreeformRootEditPart) {
				ScalableFreeformRootEditPart root = (ScalableFreeformRootEditPart) rep;
				thumbnail = new ScrollableThumbnail((Viewport) root.getFigure());
				thumbnail.setBorder(new MarginBorder(3));
				thumbnail.setSource(root
						.getLayer(LayerConstants.PRINTABLE_LAYERS));
				lws.setContents(thumbnail);
				disposeListener = new DisposeListener() {
					public void widgetDisposed(DisposeEvent e) {
						if (thumbnail != null) {
							thumbnail.deactivate();
							thumbnail = null;
						}
					}
				};
				getEditor().addDisposeListener(disposeListener);
			}
		}

		public void setContents(Object contents) {
			getViewer().setContents(contents);
		}

		protected void showPage(int id) {
			if (id == ID_OUTLINE) {
				showOutlineAction.setChecked(true);
				showOverviewAction.setChecked(false);
				pageBook.showPage(outline);
				if (thumbnail != null)
					thumbnail.setVisible(false);
			} else if (id == ID_OVERVIEW) {
				if (thumbnail == null)
					initializeOverview();
				showOutlineAction.setChecked(false);
				showOverviewAction.setChecked(true);
				pageBook.showPage(overview);
				thumbnail.setVisible(true);
			}
		}

		protected void unhookOutlineViewer() {
			getSelectionSynchronizer().removeViewer(getViewer());
			if (disposeListener != null && getEditor() != null
					&& !getEditor().isDisposed())
				getEditor().removeDisposeListener(disposeListener);
		}
	}

	// This class listens to changes to the file system in the workspace, and
	// makes changes accordingly.
	// 1) An open, saved file gets deleted -> close the editor
	// 2) An open file gets renamed or moved -> change the editor's input
	// accordingly
	class ResourceTracker implements IResourceChangeListener,
			IResourceDeltaVisitor {
		public void resourceChanged(IResourceChangeEvent event) {
			IResourceDelta delta = event.getDelta();
			try {
				if (delta != null)
					delta.accept(this);
			} catch (CoreException exception) {
				// What should be done here?
			}
		}

		public boolean visit(IResourceDelta delta) {
			if (delta == null
					|| !delta.getResource().equals(
							((IFileEditorInput) getEditorInput()).getFile()))
				return true;

			if (delta.getKind() == IResourceDelta.REMOVED) {
				Display display = getSite().getShell().getDisplay();
				if ((IResourceDelta.MOVED_TO & delta.getFlags()) == 0) { // if
					// the
					// file
					// was
					// deleted
					// NOTE: The case where an open, unsaved file is deleted is
					// being handled by the
					// PartListener added to the Workbench in the initialize()
					// method.
					display.asyncExec(new Runnable() {
						public void run() {
							if (!isDirty())
								closeEditor(false);
						}
					});
				} else { // else if it was moved or renamed
					final IFile newFile = ResourcesPlugin.getWorkspace()
							.getRoot().getFile(delta.getMovedToPath());
					display.asyncExec(new Runnable() {
						public void run() {
							superSetInput(new FileEditorInput(newFile));
						}
					});
				}
			} else if (delta.getKind() == IResourceDelta.CHANGED) {
				if (!editorSaving) {
					// the file was overwritten somehow (could have been
					// replaced by another
					// version in the respository)
					final IFile newFile = ResourcesPlugin.getWorkspace()
							.getRoot().getFile(delta.getFullPath());
					Display display = getSite().getShell().getDisplay();
					display.asyncExec(new Runnable() {
						public void run() {
							setInput(new FileEditorInput(newFile));
							getCommandStack().flush();
						}
					});
				}
			}
			return false;
		}
	}

}
