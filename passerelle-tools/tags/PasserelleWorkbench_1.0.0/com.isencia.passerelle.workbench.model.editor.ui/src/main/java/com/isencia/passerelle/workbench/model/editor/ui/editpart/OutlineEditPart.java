package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import org.eclipse.gef.EditPolicy;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Tree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.Changeable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptolemy.moml.Vertex;
import ptolemy.vergil.kernel.attributes.TextAttribute;

import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteBuilder;

/**
 * EditPart for components in the Tree.
 */
public class OutlineEditPart extends
		org.eclipse.gef.editparts.AbstractTreeEditPart implements
		ValueListener, ChangeListener {
	private static HashMap<String, Image> modelImages = new HashMap<String, Image>();
	private static Logger logger = LoggerFactory
			.getLogger(OutlineEditPart.class);

	/**
	 * Constructor initializes this with the given model.
	 * 
	 * @param model
	 *            Model for this outline
	 */
	public OutlineEditPart(Object model) {
		super(model);

		if (model instanceof Parameter) {
			Parameter parameter = (Parameter) model;
			parameter.addValueListener(this);
		}

	}

	public Logger getLogger() {
		return logger;
	}

	public void activate() {
		if (isActive())
			return;
		super.activate();

		if (getNamedObjectModel() instanceof Changeable) {
			Changeable changeable = (Changeable) getNamedObjectModel();
			changeable.addChangeListener(this);
		}
	}

	public void deactivate() {
		if (!isActive())
			return;
		if (getNamedObjectModel() instanceof Changeable) {
			Changeable changeable = (Changeable) getNamedObjectModel();
			changeable.removeChangeListener(this);
		}
		super.deactivate();
	}

	/**
	 * Creates and installs pertinent EditPolicies for this.
	 */
	protected void createEditPolicies() {
		EditPolicy component;
		// if (getModel() instanceof LED)
		// component = new LEDEditPolicy();
		// else
		// component = new LogicElementEditPolicy();
		// installEditPolicy(EditPolicy.COMPONENT_ROLE, component);
		// installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE, new
		// LogicTreeEditPolicy());
	}

	/**
	 * Returns the model of this as a NamedObj.
	 * 
	 * @return Model of this.
	 */
	protected NamedObj getNamedObjectModel() {
		return (NamedObj) getModel();
	}

	/**
	 * Returns <code>null</code> as a Tree EditPart holds no children under it.
	 * 
	 * @return <code>null</code>
	 */
	protected List getModelChildren() {
		NamedObj namedObjectModel = getNamedObjectModel();

		List children = new ArrayList();
		if (namedObjectModel instanceof AtomicActor) {
			AtomicActor actor = (AtomicActor) namedObjectModel;
			children.addAll(actor.attributeList(Parameter.class));
			children.addAll(actor.inputPortList());
			children.addAll(actor.outputPortList());
		} else if (namedObjectModel instanceof CompositeActor) {
			CompositeActor composite = (CompositeActor) namedObjectModel;
			children.addAll(composite.attributeList(AtomicActor.class));
			children.addAll(composite.attributeList(Parameter.class));
			children.addAll(composite.inputPortList());

			Enumeration enumeration = composite.getEntities();
			while (enumeration.hasMoreElements()) {
				children.add(enumeration.nextElement());
			}
		} else if (namedObjectModel instanceof IOPort) {
			IOPort text = (IOPort) namedObjectModel;
			children.addAll(text
					.attributeList(ptolemy.kernel.util.StringAttribute.class));
		} else if (namedObjectModel instanceof Vertex) {
			Vertex text = (Vertex) namedObjectModel;
			children.addAll(text.attributeList(Vertex.class));
		} else if (namedObjectModel instanceof TextAttribute) {
			TextAttribute text = (TextAttribute) namedObjectModel;
			children.addAll(text
					.attributeList(ptolemy.kernel.util.StringAttribute.class));
		} else if (namedObjectModel instanceof Director) {
			Director director = (Director) namedObjectModel;
			children.addAll(director.attributeList(Parameter.class));
		}
		return children;
	}

	// public void propertyChange(PropertyChangeEvent change){
	// if (change.getPropertyName().equals(LogicDiagram.CHILDREN)) {
	// if (change.getOldValue() instanceof Integer)
	// // new child
	// addChild(createChild(change.getNewValue()),
	// ((Integer)change.getOldValue()).intValue());
	// else
	// // remove child
	// removeChild((EditPart)getViewer().getEditPartRegistry().get(change.getOldValue()));
	// } else
	// refreshVisuals();
	// }

	/**
	 * Refreshes the visual properties of the TreeItem for this part.
	 */
	protected void refreshVisuals() {
		if (getWidget() instanceof Tree)
			return;
		NamedObj model = getNamedObjectModel();
		// Set Image
		if (model instanceof Director)
			setWidgetImage(DirectorEditPart.IMAGE_DESCRIPTOR_DIRECTOR, model);
		else if (model instanceof Parameter)
			setWidgetImage(ActorEditPart.IMAGE_DESCRIPTOR_PARAMETER, model);
		else if (model instanceof IOPort) {
			IOPort port = (IOPort) model;
			if (port.isInput())
				setWidgetImage(ActorEditPart.IMAGE_DESCRIPTOR_INPUTPORT, model);
			else
				setWidgetImage(ActorEditPart.IMAGE_DESCRIPTOR_OUTPUTPORT, model);
		} else if (model instanceof TypedAtomicActor) {
			setWidgetImage(ActorEditPart.IMAGE_DESCRIPTOR_ACTOR, model);
		} else if (model instanceof CompositeActor) {
			setWidgetImage(
					CompositeActorEditPart.IMAGE_DESCRIPTOR_COMPOSITEACTOR,
					model);
		} else if (model instanceof Vertex) {
			setWidgetImage(CommentEditPart.IMAGE_COMMENT, model);
		} else if (model instanceof TextAttribute) {
			setWidgetImage(CommentEditPart.IMAGE_COMMENT, model);
		} else if (model instanceof TypedIOPort) {
			if (((IOPort) model).isInput())
				setWidgetImage(
						PaletteBuilder
								.getIcon("com.isencia.passerelle.actor.general.InputIOPort"),
						model);
			else
				setWidgetImage(
						PaletteBuilder
								.getIcon("com.isencia.passerelle.actor.general.OutputIOPort"),
						model);
		}
		// Set Text
		if (model instanceof Parameter) {
			Parameter param = (Parameter) model;
			String name = param.getName();
			String value = param.getExpression();
			setWidgetText(name + "=" + (value == null ? "" : value));
		} else
			setWidgetText(model.getName());
	}

	/**
	 * NOTE This can be called from non-UI thread!
	 */
	@Override
	public void valueChanged(Settable settable) {
		getRoot().getViewer().getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				refreshVisuals();
			}
		});
	}

	@Override
	public void changeExecuted(ChangeRequest changerequest) {
		try {
			refreshVisuals();
			refreshChildren();
		} catch (Exception e) {

		}
	}

	@Override
	public void changeFailed(ChangeRequest changerequest, Exception exception) {
		getLogger().error("Error during execution of ChangeRequest", exception);
	}

	protected void setWidgetImage(ImageDescriptor image, NamedObj obj) {
		if (modelImages.get(obj.getClass().getName()) == null) {
			Image createImage = image.createImage();
			setWidgetImage(createImage);
			modelImages.put(obj.getClass().getName(), createImage);
		} else {
			// modelImages.get(obj.getClass().getName()).
			setWidgetImage(modelImages.get(obj.getClass().getName()));
		}
	}
}
