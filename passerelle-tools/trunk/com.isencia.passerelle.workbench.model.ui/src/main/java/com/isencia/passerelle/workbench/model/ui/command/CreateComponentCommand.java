package com.isencia.passerelle.workbench.model.ui.command;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.Vertex;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.workbench.model.ui.ComponentUtility;
import com.isencia.passerelle.workbench.model.ui.IPasserelleMultiPageEditor;
import com.isencia.passerelle.workbench.model.utils.ModelChangeRequest;
import com.isencia.passerelle.workbench.model.utils.ModelUtils;

public class CreateComponentCommand extends org.eclipse.gef.commands.Command {

	private static final String DEFAULT_OUTPUT_PORT = "OutputPort";

	private static final String DEFAULT_INPUT_PORT = "InputPort";

	private static Logger logger = LoggerFactory.getLogger(CreateComponentCommand.class);

	private Class<? extends NamedObj> clazz;
	private String name;

	private NamedObj model;
	private NamedObj parent;
	private NamedObj child;
	private IPasserelleMultiPageEditor editor;

	public NamedObj getChild() {
		return child;
	}

	private double[] location;

	public CreateComponentCommand(IPasserelleMultiPageEditor editor) {
		super("CreateComponent");
		this.editor = editor;
	}

	public void setModel(NamedObj model) {
		this.model = model;
	}

	public Logger getLogger() {
		return logger;
	}

	public boolean canExecute() {
		if (("com.isencia.passerelle.actor.general.InputIOPort".equals(clazz) || "com.isencia.passerelle.actor.general.OutputIOPort"
				.equals(clazz))
				&& parent != null && parent.getContainer() == null) {
			return false;
		}
		return (clazz != null && parent != null);
	}

	public void execute() {
		doExecute();
	}

	public void doExecute() {
		// Perform Change in a ChangeRequest so that all Listeners are notified
		parent.requestChange(new ModelChangeRequest(this.getClass(), parent,
				"create") {
			@Override
			protected void _execute() throws Exception {
				try {
					CompositeEntity parentModel = (CompositeEntity) parent;
					String componentName = null;
					if (model == null) {
						componentName = ModelUtils.findUniqueName(parentModel,
										                          clazz,
										                          name.equalsIgnoreCase("INPUT") ? DEFAULT_INPUT_PORT : DEFAULT_OUTPUT_PORT,
										                          name);
						componentName = ModelUtils.getLegalName(componentName);
						Class constructorClazz = CompositeEntity.class;
						if (clazz.getSimpleName().equals("TypedIOPort")) {
							constructorClazz = ComponentEntity.class;
						} else if (clazz.getSimpleName().equals(
								"TextAttribute")) {
							constructorClazz = NamedObj.class;
						}
						if (clazz.getSimpleName().equals("Vertex")) {
							TypedIORelation rel = new TypedIORelation(
									parentModel, componentName);
							child = new Vertex(rel, "Vertex");

						} else {
							Constructor constructor = clazz.getConstructor(
									constructorClazz, String.class);

							child = (NamedObj) constructor.newInstance(
									parentModel, componentName);
							if (child instanceof TypedIOPort) {
								boolean isInput = name
										.equalsIgnoreCase("INPUT");
								((TypedIOPort) child).setInput(isInput);
								((TypedIOPort) child).setOutput(!isInput);
							}
						}
					} else {
						if (model instanceof TypedIOPort) {
							name = ((TypedIOPort) model).isInput() ? DEFAULT_INPUT_PORT
									: DEFAULT_OUTPUT_PORT;
						}
						componentName = ModelUtils.findUniqueName(parentModel, model.getClass(), name, name);
						componentName = ModelUtils.getLegalName(componentName);
						if (model instanceof Vertex) {
							TypedIORelation rel = new TypedIORelation(
									parentModel, componentName);
							child = new Vertex(rel, "Vertex");
							
						} else {
							child = (NamedObj) model
									.clone(((CompositeEntity) parentModel)
											.workspace());
							child.setName(componentName);
						}
						ComponentUtility.setContainer(child, parentModel);
					}
					
					createDefaultValues(child);
					
					if (location != null) {
						ModelUtils.setLocation(child, location);
					}
					setChild(child);

				} catch (Exception e) {
					getLogger().error("Unable to create component", e);
				}

			}

		});
	}

	public void redo() {
		// Perform Change in a ChangeRequest so that all Listeners are notified
		parent.requestChange(new ModelChangeRequest(this.getClass(), parent,
				"create") {
			@Override
			protected void _execute() throws Exception {
				getLogger().debug("Redo create component");
				editor.selectPage((CompositeActor) parent);
				if (child instanceof NamedObj) {
					ComponentUtility.setContainer(child, parent);

				}

			}
		});
	}

	public void setClazz(Class<? extends NamedObj> clazz) {
		this.clazz = clazz;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParent(NamedObj newParent) {
		parent = newParent;
	}

	public void undo() {
		// Perform Change in a ChangeRequest so that all Listeners are notified
		parent.requestChange(new ModelChangeRequest(this.getClass(), parent,
				"create") {
			@Override
			protected void _execute() throws Exception {
				editor.selectPage((CompositeActor) parent);
				if (child instanceof NamedObj) {
					ComponentUtility.setContainer(child, null);
				}
			}
		});
	}

	public double[] getLocation() {
		return location;
	}

	public void setLocation(double[] location) {
		this.location = location;
	}

	private Map<Object, Object> defaultValueMap;
	
	/**
	 * This method can be used to default configurable parameter values, when
	 * the class is created.
	 * @param clazz
	 * @param filePath
	 */
	public void addConfigurableParameterValue(final Object clazzOrString,
			                                  final Object value) {
		
		if (defaultValueMap==null) defaultValueMap = new LinkedHashMap<Object, Object>(3);
		defaultValueMap.put(clazzOrString, value);
	}

	private void createDefaultValues(NamedObj child) throws Exception {
		
		if (defaultValueMap==null) return;
		if (child instanceof Actor) {
			final Actor actor = (Actor)child;
			
			for (Object key : defaultValueMap.keySet()) {
				Parameter param = null;
				if (key instanceof Class) {
					final Collection<? extends Parameter> params = actor.getConfigurableParameter((Class<? extends Parameter>)key);
					param = (params!=null && !params.isEmpty() && params.size()==1) ? params.iterator().next() : null;
				} else if (key instanceof String) {
					param = actor.getConfigurableParameter((String)key);
				}
				if (param==null) continue;
				
				final Object value = defaultValueMap.get(key);
				if (value instanceof Boolean) {
					param.setToken(new BooleanToken((Boolean)value));
				} else if (value instanceof String) {
					param.setExpression((String)value);
				}
			}
		}
		
	}
}
