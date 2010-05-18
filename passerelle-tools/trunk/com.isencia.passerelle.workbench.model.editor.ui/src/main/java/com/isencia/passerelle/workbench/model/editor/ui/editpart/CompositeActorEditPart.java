package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.draw2d.ChangeEvent;
import org.eclipse.draw2d.ChangeListener;
import org.eclipse.draw2d.Clickable;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.AccessibleAnchorProvider;
import org.eclipse.gef.AccessibleEditPart;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.requests.DropRequest;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.Actor;
import ptolemy.actor.IORelation;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.ChangeRequest;

import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.editor.CompositeModelEditor;
import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelEditor;
import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelSampleEditor;
import com.isencia.passerelle.workbench.model.editor.ui.editpolicy.ComponentNodeDeletePolicy;
import com.isencia.passerelle.workbench.model.editor.ui.editpolicy.CompositeActorEditPolicy;
import com.isencia.passerelle.workbench.model.editor.ui.figure.ActorFigure;
import com.isencia.passerelle.workbench.model.editor.ui.figure.CompositeActorFigure;
import com.isencia.passerelle.workbench.model.ui.command.CreateConnectionCommand;
import com.isencia.passerelle.workbench.model.ui.command.DeleteComponentCommand;
import com.isencia.passerelle.workbench.model.ui.command.DeleteConnectionCommand;
import com.isencia.passerelle.workbench.model.utils.ModelChangeRequest;
import com.isencia.passerelle.workbench.model.utils.ModelUtils;

public class CompositeActorEditPart extends ContainerEditPart implements
		NodeEditPart {
	private MultiPageEditorPart multiPageEditorPart;

	public MultiPageEditorPart getMultiPageEditorPart() {
		return multiPageEditorPart;
	}

	public void setMultiPageEditorPart(MultiPageEditorPart multiPageEditorPart) {
		this.multiPageEditorPart = multiPageEditorPart;
	}

	private final static Logger logger = LoggerFactory
			.getLogger(ActorEditPart.class);
	private Map<TypedCompositeActor, PasserelleModelEditor> pages = new HashMap<TypedCompositeActor, PasserelleModelEditor>();
	public final static ImageDescriptor IMAGE_DESCRIPTOR_COMPOSITEACTOR = Activator
			.getImageDescriptor("icons/compound.gif");
	public final static ImageDescriptor IMAGE_DESCRIPTOR_DRILLDOWN = Activator
			.getImageDescriptor("icons/add.gif");

	public Logger getLogger() {
		return logger;
	}

	public CompositeActorEditPart() {
		super();

	}

	private void initPage() {
		PasserelleModelSampleEditor multiPageEditor = (PasserelleModelSampleEditor) searchPasserelleModelSampleEditor(getParent());
		try {
			if (multiPageEditor != null) {

				TypedCompositeActor model = (TypedCompositeActor) getModel();
				if (pages.get(model) == null) {
					CompositeModelEditor editor = new CompositeModelEditor(
							multiPageEditor, model);
					int index = multiPageEditor.addPage(editor, multiPageEditor
							.getEditorInput());
					multiPageEditor.setText(index, model.getDisplayName());
					pages.put(model, editor);
				}
			}
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public MultiPageEditorPart searchPasserelleModelSampleEditor(EditPart child) {
		if (child != null) {
			if (child instanceof DiagramEditPart) {
				return ((DiagramEditPart) child).getMultiPageEditorPart();
			}
			return searchPasserelleModelSampleEditor(child.getParent());
		}
		return null;
	}

	public CompositeActorEditPart(boolean showChildren,
			MultiPageEditorPart multiPageEditorPart) {
		super(showChildren);
		this.multiPageEditorPart = multiPageEditorPart;
	}

	public void changeExecuted(ChangeRequest changerequest) {
		super.changeExecuted(changerequest);

		Object source = changerequest.getSource();
		if (changerequest instanceof ModelChangeRequest) {
			Class<?> type = ((ModelChangeRequest) changerequest).getType();

			if (getModel() != source
					&& (DeleteConnectionCommand.class.equals(type)
							|| DeleteComponentCommand.class.equals(type) || CreateConnectionCommand.class
							.equals(type))) {
				refreshSourceConnections();
				refreshTargetConnections();
			}
		}
	}

	protected AccessibleEditPart createAccessible() {
		return new AccessibleGraphicalEditPart() {
			public void getName(AccessibleEvent e) {
				// e.result = LogicMessages.LogicDiagram_LabelText;
				e.result = "Test";
			}
		};
	}

	/**
	 * Installs EditPolicies specific to this.
	 */
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE,
				new ComponentNodeDeletePolicy());
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE,
				new CompositeActorEditPolicy());

		// installEditPolicy(EditPolicy.NODE_ROLE, null);
		// installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, null);
		// installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, null);
		// installEditPolicy(EditPolicy.COMPONENT_ROLE,
		// new RootComponentEditPolicy());
		// installEditPolicy(EditPolicy.LAYOUT_ROLE, new
		// DiagramXYLayoutEditPolicy(
		// (XYLayout)getContentPane().getLayoutManager()));
		//
		//		installEditPolicy("Snap Feedback", new SnapFeedbackPolicy()); //$NON-NLS-1$
	}

	/**
	 * Returns a Figure to represent this.
	 * 
	 * @return Figure.
	 */
	protected IFigure createFigure() {
		ImageFigure drillDownImageFigure = new ImageFigure(
				CompositeActorEditPart.IMAGE_DESCRIPTOR_DRILLDOWN.createImage());
		drillDownImageFigure.setAlignment(PositionConstants.SOUTH);
		drillDownImageFigure.setBorder(new MarginBorder(0, 0, 5, 0));

		Clickable button = new Clickable(drillDownImageFigure);
		button.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClicked(MouseEvent e) {
				initPage();

			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

		});
		// button.addChangeListener(new ChangeListener() {
		// public void handleStateChanged(ChangeEvent e) {
		// initPage();
		// if (getLogger().isDebugEnabled())
		// getLogger().debug("Clicked" + e.getPropertyName());
		// }
		// });

		Actor actorModel = getActorModel();
		CompositeActorFigure actorFigure = new CompositeActorFigure(actorModel
				.getDisplayName(), IMAGE_DESCRIPTOR_COMPOSITEACTOR
				.createImage(), button);
		// Add TargetConnectionAnchors
		List<TypedIOPort> inputPortList = actorModel.inputPortList();
		if (inputPortList != null) {
			for (TypedIOPort inputPort : inputPortList) {
				actorFigure.addInput(inputPort.getName(), inputPort
						.getDisplayName());
			}
		}
		// Add SourceConnectionAnchors
		List<TypedIOPort> outputPortList = actorModel.outputPortList();
		if (outputPortList != null) {
			for (TypedIOPort outputPort : outputPortList) {
				actorFigure.addOutput(outputPort.getName(), outputPort
						.getDisplayName());
			}
		}
		return actorFigure;
	}

	/**
	 * Returns the Figure of this as a ActorFigure.
	 * 
	 * @return ActorFigure of this.
	 */
	public ActorFigure getComponentFigure() {
		return (ActorFigure) getFigure();
	}

	/**
	 * Returns the model of this as a TypedCompositeActor.
	 * 
	 * @return Model of this as an TypedCompositeActor.
	 */
	protected TypedCompositeActor getActorModel() {
		return (TypedCompositeActor) getModel();
	}

	public void setSelected(int i) {
		super.setSelected(i);
		refreshVisuals();
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	/*
	 * public Object getAdapter(Class adapter) { if (adapter ==
	 * SnapToHelper.class) { List snapStrategies = new ArrayList(); Boolean val
	 * = (Boolean) getViewer().getProperty(
	 * RulerProvider.PROPERTY_RULER_VISIBILITY); if (val != null &&
	 * val.booleanValue()) snapStrategies.add(new SnapToGuides(this)); val =
	 * (Boolean) getViewer().getProperty( SnapToGeometry.PROPERTY_SNAP_ENABLED);
	 * if (val != null && val.booleanValue()) snapStrategies.add(new
	 * SnapToGeometry(this)); val = (Boolean) getViewer().getProperty(
	 * SnapToGrid.PROPERTY_GRID_ENABLED); if (val != null && val.booleanValue())
	 * snapStrategies.add(new SnapToGrid(this));
	 * 
	 * if (snapStrategies.size() == 0) return null; if (snapStrategies.size() ==
	 * 1) return snapStrategies.get(0);
	 * 
	 * SnapToHelper ss[] = new SnapToHelper[snapStrategies.size()]; for (int i =
	 * 0; i < snapStrategies.size(); i++) ss[i] = (SnapToHelper)
	 * snapStrategies.get(i); return new CompoundSnapToHelper(ss); } return
	 * super.getAdapter(adapter); }
	 * 
	 * public DragTracker getDragTracker(Request req) { if (req instanceof
	 * SelectionRequest && ((SelectionRequest) req).getLastButtonPressed() == 3)
	 * return new DeselectAllTracker(this); return new MarqueeDragTracker(); }
	 */
	public Object getAdapter(Class key) {
		if (key == AccessibleAnchorProvider.class)
			return new DefaultAccessibleAnchorProvider() {
				public List getSourceAnchorLocations() {
					List list = new ArrayList();
					Vector sourceAnchors = getComponentFigure()
							.getSourceConnectionAnchors();
					for (int i = 0; i < sourceAnchors.size(); i++) {
						ConnectionAnchor anchor = (ConnectionAnchor) sourceAnchors
								.get(i);
						list.add(anchor.getReferencePoint()
								.getTranslated(0, -3));
					}
					return list;
				}

				public List getTargetAnchorLocations() {
					List list = new ArrayList();
					Vector targetAnchors = getComponentFigure()
							.getTargetConnectionAnchors();
					for (int i = 0; i < targetAnchors.size(); i++) {
						ConnectionAnchor anchor = (ConnectionAnchor) targetAnchors
								.get(i);
						list
								.add(anchor.getReferencePoint().getTranslated(
										0, 3));
					}
					return list;
				}
			};
		return super.getAdapter(key);
	}

	@Override
	protected List getModelSourceConnections() {
		return ModelUtils.getConnectedRelations(getActorModel(),
				ModelUtils.ConnectionType.SOURCE);
	}

	@Override
	protected List getModelTargetConnections() {
		return ModelUtils.getConnectedRelations(getActorModel(),
				ModelUtils.ConnectionType.TARGET);
	}

	/**
	 * Returns the Output Port based on a given Anchor
	 * 
	 * @return Port.
	 */
	public Port getSourcePort(ConnectionAnchor anchor) {
		getLogger().debug("Get Source port  based on anchor");

		ActorFigure anchorFigure = getComponentFigure();
		List outputPortList = getActorModel().outputPortList();
		for (Iterator iterator = outputPortList.iterator(); iterator.hasNext();) {
			Port port = (Port) iterator.next();
			if (port.getName() != null
					&& port.getName().equals(
							anchorFigure.getConnectionAnchorName(anchor)))
				return port;
		}
		return null;
	}

	/**
	 * Returns the Input Port based on a given Anchor
	 * 
	 * @return Port.
	 */
	public Port getTargetPort(ConnectionAnchor anchor) {
		getLogger().debug("Get Target port  based on anchor");

		ActorFigure anchorFigure = getComponentFigure();
		List inputPortList = getActorModel().inputPortList();
		for (Iterator iterator = inputPortList.iterator(); iterator.hasNext();) {
			Port port = (Port) iterator.next();
			if (port.getName() != null
					&& port.getName().equals(
							anchorFigure.getConnectionAnchorName(anchor)))
				return port;
		}
		return null;
	}

	/**
	 * Returns the connection anchor for the given ConnectionEditPart's source.
	 * 
	 * @return ConnectionAnchor.
	 */
	public ConnectionAnchor getSourceConnectionAnchor(
			ConnectionEditPart connEditPart) {
		getLogger().debug(
				"Get SourceConnectionAnchor based on ConnectionEditPart");

		Relation relation = (Relation) connEditPart.getModel();
		List linkedPortList = ((IORelation) relation).linkedSourcePortList();
		if (linkedPortList == null || linkedPortList.size() == 0)
			return null;
		Port port = (Port) linkedPortList.get(0);
		ConnectionAnchor connectionAnchor = getComponentFigure()
				.getConnectionAnchor(port.getName());
		return connectionAnchor;
	}

	/**
	 * Returns the connection anchor of a source connection which is at the
	 * given point.
	 * 
	 * @return ConnectionAnchor.
	 */
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		Point pt = new Point(((DropRequest) request).getLocation());
		return getComponentFigure().getSourceConnectionAnchorAt(pt);
	}

	/**
	 * Returns the connection anchor for the given ConnectionEditPart's target.
	 * 
	 * @return ConnectionAnchor.
	 */
	public ConnectionAnchor getTargetConnectionAnchor(
			ConnectionEditPart connEditPart) {
		Relation relation = (Relation) connEditPart.getModel();
		List linkedPortList = ((IORelation) relation)
				.linkedDestinationPortList();
		if (linkedPortList == null || linkedPortList.size() == 0)
			return null;
		Port port = (Port) linkedPortList.get(0);
		ConnectionAnchor connectionAnchor = getComponentFigure()
				.getConnectionAnchor(port.getName());
		return connectionAnchor;
	}

	/**
	 * Returns the connection anchor of a terget connection which is at the
	 * given point.
	 * 
	 * @return ConnectionAnchor.
	 */
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		Point pt = new Point(((DropRequest) request).getLocation());
		return getComponentFigure().getTargetConnectionAnchorAt(pt);
	}

	/**
	 * Returns the name of the given connection anchor.
	 * 
	 * @return The name of the ConnectionAnchor as a String.
	 */
	final protected String mapConnectionAnchorToTerminal(ConnectionAnchor c) {
		return getComponentFigure().getConnectionAnchorName(c);
	}

}