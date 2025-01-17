package com.isencia.passerelle.workbench.model.editor.ui.editpolicy;

import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.dnd.TemplateTransfer;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.slf4j.Logger;

import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelMultiPageEditor;
import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteItemDefinition;
import com.isencia.passerelle.workbench.model.editor.ui.views.DragTargetListener;
import com.isencia.passerelle.workbench.model.ui.command.CreateComponentCommand;
import com.isencia.passerelle.workbench.model.ui.command.SetConstraintCommand;

public class DiagramXYLayoutEditPolicy extends
		org.eclipse.gef.editpolicies.XYLayoutEditPolicy {

	private SetConstraintCommand getSetConstraintCommand() {
		return new SetConstraintCommand();
	}

	private CreateComponentCommand getCreateComponentCommand() {
		return new CreateComponentCommand(editor);
	}

	private static Logger logger;
	private PasserelleModelMultiPageEditor editor;

	public DiagramXYLayoutEditPolicy(XYLayout layout,
			PasserelleModelMultiPageEditor editor) {
		super();
		setXyLayout(layout);
		this.editor = editor;
	}

	Logger getLogger() {
		return logger;
	}

	protected Command chainGuideAttachmentCommand(Request request,
			NamedObj model, Command cmd, boolean horizontal) {
		Command result = cmd;
		return result;
	}

	protected Command chainGuideDetachmentCommand(Request request,
			NamedObj model, Command cmd, boolean horizontal) {
		Command result = cmd;
		return result;
	}

	protected Command createAddCommand(Request request, EditPart childEditPart,
			Object constraint) {
		if (getLogger().isDebugEnabled())
			getLogger().debug(
					"createAddCommand for editPart : " + childEditPart);
		return null;
	}

	/**
	 * @see org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy#createChangeConstraintCommand(org.eclipse.gef.EditPart,
	 *      java.lang.Object)
	 */
	protected Command createChangeConstraintCommand(EditPart child,
			Object constraint) {
		SetConstraintCommand locationCommand = getSetConstraintCommand();
		locationCommand.setModel((NamedObj) child.getModel());
		Rectangle rectangle = (Rectangle) constraint;
		Point location = rectangle.getLocation();
		locationCommand.setLocation(new double[] { location.x, location.y });
		return locationCommand;
	}

	/**
	 * Create Command that will be executed after a move or resize
	 */
	protected Command createChangeConstraintCommand(
			ChangeBoundsRequest request, EditPart child, Object constraint) {
		SetConstraintCommand cmd = getSetConstraintCommand();
		cmd.setModel((NamedObj) child.getModel());
		Rectangle rectangle = (Rectangle) constraint;
		Point location = rectangle.getLocation();
		cmd.setLocation(new double[] { location.x, location.y });
		Command result = cmd;

		return result;
	}

	protected EditPolicy createChildEditPolicy(EditPart child) {
		return new DiagramNonResizableEditPolicy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.editpolicies.LayoutEditPolicy#createSizeOnDropFeedback
	 * (org.eclipse.gef.requests.CreateRequest)
	 */
	protected IFigure createSizeOnDropFeedback(CreateRequest createRequest) {
		IFigure figure;

		if (getLogger().isDebugEnabled())
			getLogger().debug("createSizeOnDropFeedback");

		// TODO Check if we shouldn't return a more meaningful figure during DND
		figure = new RectangleFigure();
		((RectangleFigure) figure).setXOR(true);
		((RectangleFigure) figure).setFill(true);
		figure.setBackgroundColor(ColorConstants.cyan);
		figure.setForegroundColor(ColorConstants.white);
		// }

		addFeedback(figure);
		// No Figure for the moment
		return null;
	}

	/*
	 * protected LogicGuide findGuideAt(int pos, boolean horizontal) {
	 * RulerProvider provider =
	 * ((RulerProvider)getHost().getViewer().getProperty( horizontal ?
	 * RulerProvider.PROPERTY_VERTICAL_RULER :
	 * RulerProvider.PROPERTY_HORIZONTAL_RULER)); return
	 * (LogicGuide)provider.getGuideAt(pos); }
	 */
	protected Command getAddCommand(Request generic) {
		if (getLogger().isDebugEnabled())
			getLogger().debug("getAddCommand");
		ChangeBoundsRequest request = (ChangeBoundsRequest) generic;
		List<?> editParts = request.getEditParts();
		CompoundCommand command = new CompoundCommand();
		command.setDebugLabel("Add in ConstrainedLayoutEditPolicy");//$NON-NLS-1$
		GraphicalEditPart childPart;
		Rectangle r;
		Object constraint;

		for (int i = 0; i < editParts.size(); i++) {
			childPart = (GraphicalEditPart) editParts.get(i);
			r = childPart.getFigure().getBounds().getCopy();
			// convert r to absolute from childpart figure
			childPart.getFigure().translateToAbsolute(r);
			r = request.getTransformedRectangle(r);
			// convert this figure to relative
			getLayoutContainer().translateToRelative(r);
			getLayoutContainer().translateFromParent(r);
			r.translate(getLayoutOrigin().getNegated());
			constraint = getConstraintFor(r);
			command.add(createAddCommand(generic, childPart,
					translateToModelConstraint(constraint)));
		}
		return command.unwrap();
	}

	/**
	 * Override to return the <code>Command</code> to perform an
	 * {@link RequestConstants#REQ_CLONE CLONE}. By default, <code>null</code>
	 * is returned.
	 * 
	 * @param request
	 *            the Clone Request
	 * @return A command to perform the Clone.
	 */
	protected Command getCloneCommand(ChangeBoundsRequest request) {
		if (getLogger().isDebugEnabled())
			getLogger().debug("getCloneCommand");

		/*
		 * CloneCommand clone = new CloneCommand();
		 * 
		 * clone.setParent((LogicDiagram)getHost().getModel());
		 * 
		 * Iterator i = request.getEditParts().iterator(); GraphicalEditPart
		 * currPart = null;
		 * 
		 * while (i.hasNext()) { currPart = (GraphicalEditPart)i.next();
		 * clone.addPart((LogicSubpart)currPart.getModel(),
		 * (Rectangle)getConstraintForClone(currPart, request)); }
		 * 
		 * // Attach to horizontal guide, if one is given Integer guidePos =
		 * (Integer)request.getExtendedData()
		 * .get(SnapToGuides.KEY_HORIZONTAL_GUIDE); if (guidePos != null) { int
		 * hAlignment = ((Integer)request.getExtendedData()
		 * .get(SnapToGuides.KEY_HORIZONTAL_ANCHOR)).intValue();
		 * clone.setGuide(findGuideAt(guidePos.intValue(), true), hAlignment,
		 * true); }
		 * 
		 * // Attach to vertical guide, if one is given guidePos =
		 * (Integer)request.getExtendedData()
		 * .get(SnapToGuides.KEY_VERTICAL_GUIDE); if (guidePos != null) { int
		 * vAlignment = ((Integer)request.getExtendedData()
		 * .get(SnapToGuides.KEY_VERTICAL_ANCHOR)).intValue();
		 * clone.setGuide(findGuideAt(guidePos.intValue(), false), vAlignment,
		 * false); }
		 * 
		 * return clone;
		 */
		return null;
	}

	protected Command getCreateCommand(CreateRequest request) {
		CreateComponentCommand create = null;
		Object template = TemplateTransfer.getInstance( ).getTemplate();
		PaletteItemDefinition def = null;
//		if (template instanceof PaletteItemDefinition){
//			def = (PaletteItemDefinition)template;
//		}
		try {
			create = getCreateComponentCommand();

			create.setParent(editor.getSelectedContainer());
			String name = null;
			
//			if (def != null) {
//				name = def.getName();
//			} else {
				name = (String) request.getNewObject();
//			}
			Class<? extends NamedObj> clazz = null;
//			if (def != null) {
//				clazz = def.getClazz();
//			} else {
				clazz = (Class<? extends NamedObj>) request.getNewObjectType();
//			}

			create.setClazz(clazz);
			create.setName(name);
			Rectangle constraint = (Rectangle) getConstraintFor(request);
			create.setLocation(new double[] {
					constraint.getLocation().preciseX(),
					constraint.getLocation().preciseY() });
			create.setLabel("new");
//			TemplateTransfer.getInstance( ).setTemplate( null );
		} catch (Exception e) {
			getLogger().error("Error creating CreateComponentCommand", e);
		}

		// Command cmd = chainGuideAttachmentCommand(request, newPart, create,
		// true);
		// return chainGuideAttachmentCommand(request, newPart, cmd, false);
		return create;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.editpolicies.LayoutEditPolicy#getCreationFeedbackOffset
	 * (org.eclipse.gef.requests.CreateRequest)
	 */
	protected Insets getCreationFeedbackOffset(CreateRequest request) {
		// No Insets
		return new Insets();
	}

	/**
	 * Returns the layer used for displaying feedback.
	 * 
	 * @return the feedback layer
	 */
	protected IFigure getFeedbackLayer() {
		return getLayer(LayerConstants.SCALED_FEEDBACK_LAYER);
	}

}
