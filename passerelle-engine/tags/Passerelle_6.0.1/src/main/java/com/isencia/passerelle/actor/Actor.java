/* Copyright 2010 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.isencia.passerelle.actor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.Receiver;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

import com.isencia.passerelle.actor.gui.EditorIcon;
import com.isencia.passerelle.actor.gui.IOptionsFactory;
import com.isencia.passerelle.actor.gui.OptionsFactory;
import com.isencia.passerelle.core.ControlPort;
import com.isencia.passerelle.core.ErrorPort;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.PasserelleToken;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PortHandler;
import com.isencia.passerelle.core.PortListenerAdapter;
import com.isencia.passerelle.domain.cap.BlockingQueueReceiver;
import com.isencia.passerelle.domain.cap.Director;
import com.isencia.passerelle.ext.ErrorControlStrategy;
import com.isencia.passerelle.ext.impl.DefaultActorErrorControlStrategy;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageAndPort;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageFactory;
import com.isencia.passerelle.message.interceptor.IMessageCreator;
import com.isencia.passerelle.statistics.ActorStatistics;
import com.isencia.passerelle.statistics.StatisticsServiceFactory;

/**
 * Base class for all Passerelle Actors. Uses Passerelle's custom parameter
 * panes. Defines a getInfo() method, combining the actor's name with extended
 * info that can be defined in actor subclasses.
 * <p>
 * An actor's life cycle is determined/executed through the following methods,
 * that are being invoked by the Passerelle engine.
 * <br>
 * 1. Constructor : 
 * <ul>
 * <li>invoked once 
 * <li>this is where IO ports and parameters must be constructed 
 * <li>Actor subclasses must call the super(...) constructor
 * </ul> 
 * 2. preInitialize() 
 * <ul>
 * <li>invoked once for every model execution 
 * <li>this is where model-dependent resource activation/configuration must be done 
 * <li>Actor subclasses must call super.preInitialize() 
 * </ul>
 * <p>
 * In the standard Passerelle execution mode, the above methods are invoked for all
 * actors in a model in one common thread, e.g. the preInitialize() of all actors
 * is called sequentially. As a consequence, none of these methods should block!
 * </p>
 * <p> 
 * In the standard Passerelle execution mode, all methods below are invoked
 * concurrently on all actors in a model, i.e. each actor's fireing cycle has
 * its own thread.
 * </p>
 * 3. initialize() 
 * <ul>
 * <li>invoked once for every model execution AND for every dynamic model adjustment (not supported by engine in Passerelle v1.x) 
 * <li>this is where all run initialization must be done 
 * <li>Actor subclasses must call super.initialize()
 * </ul> 
 * 4. preFire() 
 * <ul>
 * <li>invoked once before every fire() 
 * <li>used to test cycle preconditions and (re)set cycle parameters 
 * <li>return true to allow call to fire() 
 * <li>return false to indicate preconditions are not OK and firing cycle should be retried later 
 * <li>Actor subclasses must call super.preFire() and include the returned boolean in their logical result expression 
 * </ul> 
 * 5. fire()
 * <ul>
 * <li>invoked once between every preFire() and postFire() 
 * <li>this is where the real actor behaviour must be implemented: read inputs, do something and possibly send results 
 * <li>fire() is implemented by Actor base classes as a template method, and some specific methods (doFire(),...) must be implemented to fill in the custom behaviour. 
 * </ul> 
 * 6. postFire() 
 * <ul>
 * <li>invoked once after every fire() 
 * <li>used to test cycle postconditions 
 * <li>return true if actor's processing cycle should continue 
 * <li>return false if actor's processing cycle should stop, after which wrapUp() will be invoked by the Passerelle Engine 
 * <li>Actor subclasses must call super.postFire() and include the returned boolean in their logical result expression 
 * </ul> 
 * 7a. wrapUp() 
 * <ul>
 * <li>invoked once at the end of an actor's processing cycle 
 * <li>this is where all resources should be released, and all extra threads that the actor has launched should be properly terminated
 * <li>after this method invocation, the actor will leave from the running model
 * <li>Actor subclasses must call super.wrapUp() 
 * </ul> 
 * 7b. terminate() 
 * <ul>
 * <li>invoked when normal termination via wrapUp() does not work 
 * <li>should also release all resources and ensure that all threads for the actor are terminated. 
 * <li>should not block, a terminate() procedure should be swift and reliable. 
 * <li>Actor subclasses must call super.terminate()
 * </ul> 
 * </p>
 * <p>
 * An actor normally determines by itself at what moment it can leave from a
 * running model, i.e. when it is of no more use. This is typically related to
 * the status of the actor's input feeds: input ports (for Transformers and
 * Sinks) or external data feeds (for Sources).
 * </p>
 * A Passerelle input may signal that it has reached the end of its feed by
 * returning a "null" message. For actors with only 1 input, a "null" message
 * indicates that the actor can safely decide that it can leave the running
 * model. The actor implementation code can signal that to the Passerelle
 * infrastructure by calling Actor.requestFinish(). Then the Actor.postFire()
 * will return false, wrapUp() will be called etc. Actors with multiple inputs
 * must determine which inputs are critical for the actors' processing and which
 * are optional. If a situation arises where the critical inputs are no longer
 * alive, again requestFinish() can be invoked after which the actor will
 * gracefully leave the running model.
 * </p>
 * <p>
 * Some actors also have a "trigger" port. These have the following behaviour:
 * <ul> 
 * <li>the trigger port is not connected: the trigger port is completely ignored, i.e. the Actor behaves as a non-triggered one 
 * <li>the trigger port is connected:
 * <ul> 
 * <li>1. the actor may only start generating output messages after receiving a trigger message 
 * <li>2. the actor may only invoke requestFinish() when its (critical) input(s) have run dry AND the trigger port as well. 
 * <li>3. the actor must be able to restart getting input data and generating results after its
 * inputs have run dry, when a next trigger message is received.
 * <br> 
 * E.g. a FileReader actor will re-read the same file and send the contents as output
 * messages every time a new trigger is received.
 * </ul>
 * </ul>
 * </p>
 * @author erwin
 */
public abstract class Actor extends TypedAtomicActor implements IMessageCreator {
	// ~ Static variables/initializers
	// ��������������������������������������������������������������������������������������������������������������������������

	private static Logger logger = LoggerFactory.getLogger(Actor.class);

	private static Logger auditLogger = LoggerFactory.getLogger("audit");
	
	private ActorStatistics statistics;
	
	private ErrorControlStrategy errorControlStrategy;

	/**
	 * Flag indicating that a polite request has arrived to finish this actors
	 * processing cycle. The actor will react on this by returning false from
	 * its next invocation of postFire().
	 */
	private boolean finishRequested = false;

	/**
	 * Flag indicating that the actor is used in a model that
	 * is executing in mock mode. In order to improve testability,
	 * some actor implementations may contain alternative behaviour
	 * for mock executions, e.g. to allow off-line testing of models
	 * containing actors that normally connect to networked resources etc.
	 */
	private boolean mockMode = false;
	
	/**
	 * CONTROL input port, used to request an actor to finish its processing
	 */
	private ControlPort requestFinishPort = null;

	private PortHandler requestFinishHandler;

	/**
	 * CONTROL output port, used by an actor to indicate that a TECHNICAL error
	 * occurred during its processing. FUNCTIONAL errors should be handled by
	 * extra output ports, specific to the functional domain of each actor. The
	 * basic implementation of Actor.fire() uses a Template Method pattern that
	 * catches all checked and unchecked exceptions from the abstract doFire()
	 * method. In the catch-block, an error message is generated on the error
	 * port, containing some error information, if the error is
	 * {@link com.isencia.passerelle.core.PasserelleException#NON_FATAL NON_FATAL}.
	 */
	protected ErrorPort errorPort = null;

	/**
	 * CONTROL output port, used by an actor to indicate that it has finished
	 * its processing, and is starting its wrapup() handling.
	 */
	private ControlPort hasFinishedPort = null;

	/**
	 * CONTROL output port, used by an actor to indicate that it has finished
	 * one fire() execution
	 */
	private ControlPort hasFiredPort = null;

	protected boolean isFiring = false;

    /**
     * The options factory can be used to extend/modify options for actor parameters.
     * It is typically set in the configuration files, so we don't need to modify actor
     * source code for options extensions.
     */
    public final static String OPTIONS_FACTORY_CFG_NAME = "_optionsFactory";
    private IOptionsFactory optionsFactory;
    
	/**
	 * The collection of parameters that are meant to be available to a model
	 * configurer tool. The actor's parameters that are not in this collection
	 * are not meant to be configurable, but are only meant to be used during
	 * model assembly (in addition to the public ones).
	 */
	private Collection<Parameter> configurableParameters = new ArrayList<Parameter>();

	/**
	 * The collection of parameters that are meant to be available to an expert
	 * only, inside the modeling tool. All parameters that are not in this
	 * collection will always be visible in the modeling tool...
	 */
	private Collection<Parameter> expertParameters = new ArrayList<Parameter>();

	/**
	 * The collection of standard headers for each message generated by this
	 * actor
	 */
	protected Map<String,String> actorMsgHeaders = new HashMap<String, String>();

	/**
	 * Parameter to set a size for input port queues, starting at which
	 * a warning message will be logged.
	 * This can be useful to determine processing hot-spots in Passerelle
	 * sequences, where actors may become flooded by input messages that they
	 * are unable to process in time.
	 * <p>
	 * Default value = -1 indicates that no such warning logs are generated.
	 * </p>
	 */
	public Parameter receiverQueueWarningSizeParam;
	/**
	 * Parameter to set a max capacity for input port queues.
	 * When a queue reaches its max capacity, any new tokens trying to 
	 * reach the input port will be refused, and a NoRoomException will be thrown.
	 * <p>
	 * Should only be used in very specific cases, as it does not correspond to
	 * the theoretical semantics of Kahn process networks, the basis for Passerelle's
	 * execution model (cfr Ptolemy project docs).
	 * </p>
	 * <p>
	 * Default value = -1 indicates that received queues have unlimited capacity.
	 * </p>
	 */
	public Parameter receiverQueueCapacityParam;
	
	/**
	 * Constructor for Actor.
	 * 
	 * @param container
	 * @param name
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
	public Actor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
		super(container, name);

		createPaneFactory();

		requestFinishPort = PortFactory.getInstance().createInputControlPort(this, "requestFinish");

		errorPort = PortFactory.getInstance().createOutputErrorPort(this);

		hasFiredPort = PortFactory.getInstance().createOutputControlPort(this, "hasFired");

		hasFinishedPort = PortFactory.getInstance().createOutputControlPort(this, "hasFinished");
		
		receiverQueueCapacityParam = new Parameter(this,"Receiver Q Capacity (-1)",new IntToken(-1));
		receiverQueueWarningSizeParam = new Parameter(this,"Receiver Q warning size (-1)",new IntToken(-1));
		registerExpertParameter(receiverQueueCapacityParam);
		registerExpertParameter(receiverQueueWarningSizeParam);
		
		try {
			new EditorIcon(this, "_icon");
		} catch (Throwable t) {
			// ignore, probably means that we're on a server with no display
		}

		actorMsgHeaders.put(ManagedMessage.SystemHeader.HEADER_SOURCE_REF, getFullName());

		statistics = new ActorStatistics(this);
	}

	public String getFullNameButWithoutModelName() {
		// the first string is the name of the model
		String fullName = getFullName();
		int i = fullName.indexOf(".", 1);
		if (i > 0) {
			// there's always an extra '.' in front of the model name...
			// and a trailing '.' just behind it...
			fullName = fullName.substring(i + 1);
		}

		return fullName;
	}

	/**
	 * Returns a unique and informative description of this actor instance.
	 * 
	 * @return A unique description of this actor instance
	 */
	final public String getInfo() {
		return getName() + " - " + getExtendedInfo();
	}

	/**
	 * Returns a part of the unique description, often combining a number of
	 * parameter settings. This part is appended to the actor name in the
	 * getInfo() method.
	 * 
	 * @return A part of the unique description, often combining a number of
	 *         parameter settings.
	 */
	protected abstract String getExtendedInfo();

	/**
	 * Creates a custom pane factory for the configuration forms of Passerelle
	 * actors. It enhances the user experience a bit, compared to the default
	 * pane of Ptolemy.
	 * 
	 * @deprecated this feature is no longer supported in Ptolemy II 4.x
	 * 
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
	protected void createPaneFactory() throws IllegalActionException, NameDuplicationException {
		// Passerelle's default parameter pane implementation
		// no longer supported in Ptolemy II 4.x
		// PaneFactoryCreator.createPaneFactory(this, null);
	}
    
    public IOptionsFactory getOptionsFactory() {
        try {
            Attribute attribute = getAttribute(OPTIONS_FACTORY_CFG_NAME, OptionsFactory.class);
            if(attribute != optionsFactory) {
                optionsFactory = (OptionsFactory) attribute;
            }
        } catch(IllegalActionException e) {
            logger.error("Error during getting of OptionsFactory attribute", e);
        }
        
        return optionsFactory;
    }
        
    protected void setOptionsFactory(IOptionsFactory optionsFactory) {
        this.optionsFactory = optionsFactory;
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see ptolemy.actor.Executable#preinitialize()
	 */
	final public void preinitialize() throws IllegalActionException {
		if (logger.isTraceEnabled()) {
			logger.trace(getInfo() + " preinitialize() - entry");
		}

		super.preinitialize();
		try {
			doPreInitialize();
		} catch (InitializationException e) {
			logger.error(getInfo() + " generated exception during doPreInitialize()", e);
			throw new IllegalActionException(this, e.toString());
		}

		if (logger.isTraceEnabled()) {
			logger.trace(getInfo() + " preinitialize() - exit ");
		}
	}

	
	@Override
	public Receiver newReceiver() throws IllegalActionException {
		// TODO continue implementing capacity/warning level based on params above
		Receiver rcver = super.newReceiver();
		if(rcver instanceof BlockingQueueReceiver) {
			BlockingQueueReceiver qRcvr = (BlockingQueueReceiver) rcver;
			int qCapacity = ((IntToken)receiverQueueCapacityParam.getToken()).intValue();
			qRcvr.setCapacity(qCapacity);
			
			int qWarningSize = ((IntToken)receiverQueueWarningSizeParam.getToken()).intValue();
			qRcvr.setSizeWarningThreshold(qWarningSize);
		}
		
		return rcver;
	}

	/**
	 * Template method implementation for preinitialize().
	 * 
	 * @throws InitializationException
	 * 
	 * @see ptolemy.actor.AtomicActor#preinitialize()
	 */
	protected void doPreInitialize() throws InitializationException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ptolemy.actor.Executable#initialize()
	 */
	final public void initialize() throws IllegalActionException {
		if (logger.isTraceEnabled()) {
			logger.trace(getInfo() + " initialize() - entry");
		}

		super.initialize();
		
		finishRequested = false;
		mockMode = false;
		try {
			mockMode = ((Director) getDirector()).isMockMode();
		} catch (ClassCastException ex) {
			// means the actor is used without a Passerelle Director
			// ignore this. Only consequence is that we'll never use
			// test mode
		}

		if (requestFinishPort.getWidth() > 0) {
			// If at least 1 channel is connected to the port
			// Install handler on input port
			requestFinishHandler = new PortHandler(requestFinishPort, new PortListenerAdapter() {
				public void tokenReceived() {
					if (logger.isTraceEnabled()) {
						logger.trace(getInfo() + " - requestFinishHandler.tokenReceived()");
					}
					requestFinishHandler.getToken();
					requestFinish();
					if (logger.isTraceEnabled()) {
						logger.trace(getInfo() + " - requestFinishHandler.tokenReceived()");
					}
				}
			});
			// Start handling the port
			requestFinishHandler.start();
		}
		try {
			doInitialize();
		} catch (InitializationException e) {
			getErrorControlStrategy().handleInitializationException(this, e);
		}

		statistics.reset();
		StatisticsServiceFactory.getService().registerStatistics(statistics);

		List<ptolemy.kernel.Port> ports = portList();
		for (Iterator<ptolemy.kernel.Port> portsItr = ports.iterator(); portsItr.hasNext();) {
			ptolemy.kernel.Port port = portsItr.next();
			if(port instanceof Port) {
				((Port)port).initialize();
			}
		}
		// audit logging for state per actor is on debug
		// NDC is not yet active during initialize, so we
		// show complete getInfo().
		if (getAuditLogger().isDebugEnabled())
			getAuditLogger().debug(getInfo() + " - INITIALIZED");

		if(mustValidateInitialization()) {
			try {
				validateInitialization();
				if (getAuditLogger().isDebugEnabled())
					getAuditLogger().debug(getInfo() + " - INITIALIZATION VALIDATED");
			} catch (ValidationException e) {
				getErrorControlStrategy().handleInitializationValidationException(this, e);
			}
		}
		
		if (logger.isTraceEnabled()) {
			logger.trace(getInfo() + " initialize() - exit ");
		}
	}

	/**
	 * Overridable method to determine if an actor should do 
	 * a validation of its initialization results.
	 * <br>
	 * By default, checks on its Passerelle director what must be done.
	 * If no Passerelle director is used (but e.g. a plain Ptolemy one),
	 * it returns true.
	 * 
	 * @see validateInitialization()
	 * @see initialize()
	 * @return
	 */
	protected boolean mustValidateInitialization() {
		try {
			return ((Director)getDirector()).mustValidateInitialization();
		} catch (ClassCastException e) {
			return true;
		}
	}

	/**
	 * Template method implementation for initialize().
	 * 
	 * @throws InitializationException
	 * 
	 * @see ptolemy.actor.AtomicActor#initialize()
	 */
	protected void doInitialize() throws InitializationException {
	}

	/**
	 * <p>
	 * Method that should be overridden for actors that need to be 
	 * able to validate their initial conditions, after the actor's doInitialize() is done
	 * and before their first iteration is executed when a model is launched.
	 * </p>
	 * <p>
	 * E.g. it can typically be used to validate parameter settings.
	 * </p>
	 * @throws ValidationException
	 */
	protected void validateInitialization() throws ValidationException {
	}

	/**
	 * Non-threadsafe method that can be used as an indication whether this
	 * actor is in its fire() processing. Can be used for example in a
	 * monitoring UI to activate some kind of actor decoration.
	 * 
	 * TODO: better alternative is to implement an Observer for this feature.
	 * 
	 * @return a flag indicating whether this actor is in its fire() processing
	 */
	final public boolean isFiring() {
		return (isFiring);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ptolemy.actor.Executable#prefire()
	 */
	final public boolean prefire() throws IllegalActionException {
		if (logger.isTraceEnabled()) {
			logger.trace(getInfo() + " prefire() - entry");
		}
		boolean res = true;
		if (!isFinishRequested()) {
			try {
				res = doPreFire();
			} catch (ProcessingException e) {
				getErrorControlStrategy().handlePreFireException(this, e);
			} catch (TerminateProcessException e) {
				// delegate handling to domain execution process
				throw e;
			} catch (RuntimeException e) {
				getErrorControlStrategy().handlePreFireRuntimeException(this, e);
			}
		}

		if (logger.isTraceEnabled()) {
			logger.trace(getInfo() + " prefire() - exit :" + res);
		}

		return res;
	}

	/**
	 * Template method implementation for prefire().
	 * 
	 * Method that can be overriden to implement precondition checking for the
	 * fire() loop. By default, returns true.
	 * 
	 * If the method returns true, the actor's fire() method will be called. If
	 * the method returns false, preFire() will be called again repetitively
	 * till it returns true.
	 * 
	 * So it's important that for "false" results there is some blocking/waiting
	 * mechanism implemented to avoid wild looping!
	 * 
	 * @return flag indicating whether the actor is ready for fire()
	 * @see ptolemy.actor.AtomicActor#prefire()
	 */
	protected boolean doPreFire() throws ProcessingException {
		return true;
	}

	/**
	 * The basic implementation of Actor.fire() uses a Template Method pattern
	 * that catches all checked and unchecked exceptions from the abstract
	 * doFire() method. In the catch-block, an error message is generated on the
	 * error port, containing some error information, if the error is
	 * {@link com.isencia.passerelle.core.PasserelleException#NON_FATAL NON_FATAL}.
	 * For {@link com.isencia.passerelle.core.PasserelleException#FATAL FATAL}
	 * exceptions, an IllegalException is generated.
	 * 
	 * If the error port is not connected,
	 * {@link com.isencia.passerelle.core.PasserelleException#NON_FATAL NON_FATAL}
	 * errors are notified to the Passerelle Director.
	 * 
	 * The fire() method also generates notification messages on the
	 * {@link #hasFiredPort} for each successfull fire loop.
	 * 
	 * @throws IllegalActionException
	 */
	final public void fire() throws IllegalActionException {
		isFiring = true;

		try {
			if (logger.isTraceEnabled()) {
				logger.trace(getInfo() + " fire() - entry");
			}

			if (!isFinishRequested()) {
				try {
					if (!mockMode)
						doFire();
					else
						doMockFire();

				} catch (ProcessingException e) {
					getErrorControlStrategy().handleFireException(this, e);
				} catch (TerminateProcessException e) {
					requestFinish();
					// delegate handling to domain execution process
					throw e;
				} catch (RuntimeException e) {
					getErrorControlStrategy().handleFireRuntimeException(this, e);
				}
			}

			if (logger.isTraceEnabled()) {
				logger.trace(getInfo() + " fire() - exit ");
			}
		} finally {
			isFiring = false;
			if (hasFiredPort.getWidth() > 0) {
				try {
					hasFiredPort.broadcast(new PasserelleToken(MessageFactory.getInstance().createTriggerMessage()));
				} catch (Exception e) {
					logger.error(getInfo(), e);
				}
			}

		}
	}

	/**
	 * Template method implementation for fire().
	 * 
	 * The actual processing behaviour of the actor must be implemented by this
	 * method.
	 * 
	 * @throws ProcessingException
	 * @see ptolemy.actor.AtomicActor#fire()
	 */
	protected abstract void doFire() throws ProcessingException;

	/**
	 * Utility method to support developing actors that can run in mock mode. In
	 * that mode, they could e.g. simulate/mock some sample behaviour without
	 * needing to access external resources (databases, message buses etc).
	 * 
	 * By default, this method just calls doFire(). Complex actors with
	 * dependencies on external resources, may override this method to allow
	 * easy local testing in the IDE.
	 * 
	 * The mock mode is defined on the Passerelle director.
	 * 
	 * @throws ProcessingException
	 */
	protected void doMockFire() throws ProcessingException {
		doFire();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ptolemy.actor.Executable#postfire()
	 */
	final public boolean postfire() throws IllegalActionException {
		if (logger.isTraceEnabled()) {
			logger.trace(getInfo() + " postfire() - entry");
		}
		boolean res = true;
		try {
			res = doPostFire();
		} catch (ProcessingException e) {
			getErrorControlStrategy().handlePostFireException(this, e);
		} catch (TerminateProcessException e) {
			// delegate handling to domain execution process
			throw e;
		} catch (RuntimeException e) {
			getErrorControlStrategy().handlePostFireRuntimeException(this, e);
		}

		if (logger.isTraceEnabled()) {
			logger.trace(getInfo() + " postfire() - exit :" + res);
		}

		return res;
	}

	/**
	 * Template method implementation for postfire().
	 * 
	 * Method that can be overriden to implement postcondition checking for the
	 * fire() loop. By default, returns true unless a finish has been requested,
	 * i.e. it delegates to isFinishRequested().
	 * 
	 * If the method returns true, the actor's preFire/fire/postFire loop will
	 * be called again. If the method returns false, the fire loop will stop and
	 * the actor's wrapup() method will be called by the Passerelle/Ptolemy
	 * framework.
	 * 
	 * @return flag indicating whether the actor wants to continue with its fire
	 *         loop
	 * @see ptolemy.actor.AtomicActor#postfire()
	 */
	protected boolean doPostFire() throws ProcessingException {
		return !isFinishRequested();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ptolemy.actor.Executable#wrapup()
	 */
	final public void wrapup() throws IllegalActionException {
		if (logger.isTraceEnabled()) {
			logger.trace(getInfo() + " wrapup() - entry");
		}

		try {
			doWrapUp();
		} catch (TerminationException e) {
			getErrorControlStrategy().handleTerminationException(this, e);
		}

		try {
			hasFinishedPort.broadcast(new PasserelleToken(MessageFactory.getInstance().createTriggerMessage()));
		} catch (Exception e) {
			logger.error(getInfo(), e);
		}

		// Inform connected receivers that this actor has stopped
		Iterator ports = outputPortList().iterator();

		while (ports.hasNext()) {
			Port port = (Port) ports.next();
			Receiver[][] farReceivers = port.getRemoteReceivers();

			for (int i = 0; i < farReceivers.length; i++) {
				if (farReceivers[i] != null) {
					for (int j = 0; j < farReceivers[i].length; j++) {
						if(farReceivers[i][j].getContainer() instanceof Port) {
							((Port)farReceivers[i][j].getContainer()).notifySourcePortFinished(port);
						}
						// the below does not work well when "diamond" relations are used
						// as these can lead to different counts of source ports connecting
						// to the diamond, versus channels arriving at the destination port(s)
						// and then a source port could still try to send a msg to a
						// receiver that already received a requestFinish(), leading to TerminationExceptions...
//						if (farReceivers[i][j] instanceof ProcessReceiver) {
//							// Ensure that the model termination
//							// ripples through the complete model.
//							((ProcessReceiver) farReceivers[i][j]).requestFinish();
//						}
						// else {
						// When using a Passerelle actor in a
						// non-process-oriented model, we trust
						// the domain processing for determination
						// of model termination, so we don't do
						// anything special in this case...
						// }
					}
				} else {
					logger.warn(getInfo() + " wrapup() - port " + port.getName() + " has a remote receiver null on channel " + i);
				}
			}
		}

		super.wrapup();

		// audit logging for state per actor is on debug
		// NDC is active during wrapup(), so we just need to
		// show extended info as extra
		// edl : use getInfo() after all, as sometimes actors
		// don't have extended info
		if (getAuditLogger().isDebugEnabled())
			getAuditLogger().debug(getInfo() + " - WRAPPED UP");

		if (logger.isTraceEnabled()) {
			logger.trace(getInfo() + " wrapup() - exit ");
		}
	}

	/**
	 * Template method implementation for wrapup().
	 * 
	 * @throws TerminationException
	 * 
	 * @see ptolemy.actor.AtomicActor#wrapup()
	 */
	protected void doWrapUp() throws TerminationException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ptolemy.actor.Executable#terminate()
	 */
	final public void terminate() {
		if (logger.isTraceEnabled()) {
			logger.trace(getInfo() + " terminate() - entry");
		}

		super.terminate();

		if (logger.isTraceEnabled()) {
			logger.trace(getInfo() + " terminate() - exit ");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ptolemy.actor.AtomicActor#stopFire()
	 */
	final public void stopFire() {
		if (logger.isTraceEnabled()) {
			logger.trace(getInfo() + " stopfire() - entry()");
		}
//		if (!isFinishRequested()) {
//			requestFinish();
//		}
		doStopFire();
		if (logger.isTraceEnabled()) {
			logger.trace(getInfo() + " stopfire() - exit ");
		}
	}

    /*
     * (non-Javadoc)
     * 
     * @see ptolemy.actor.AtomicActor#stop()
     */
    final public void stop() {
        if (logger.isTraceEnabled()) {
            logger.trace(getInfo() + " stop() - entry()");
        }
        super.stop();
        if (!isFinishRequested()) {
            requestFinish();
        }
        doStop();
        if (logger.isTraceEnabled()) {
            logger.trace(getInfo() + " stop() - exit ");
        }
    }

	/**
	 * Template method implementation for stopFire().
	 * 
	 * @see ptolemy.actor.AtomicActor#stopFire()
	 */
	protected void doStopFire() {
	}

    /**
     * Template method implementation for stop().
     * 
     * @see ptolemy.actor.AtomicActor#stop()
     */
    protected void doStop() {
    }

	/**
	 * Method to request this actor to finish its processing. It invokes also
	 * the std Ptolemy method stopFire(). This method must be overridden by
	 * actors with blocking fire loop, in such a way that the block is relieved
	 * asap.
	 */
	final public void requestFinish() {
		if (logger.isTraceEnabled()) {
			logger.trace(getInfo() + " requestfinish() - entry");
		}
		finishRequested = true;

		stopFire();
		logger.info(getInfo() + " FINISH REQUESTED !!");

		if (logger.isTraceEnabled()) {
			logger.trace(getInfo() + " requestfinish() - exit ");
		}
	}

	/**
	 * @return a flag indicating whether a finish has already been requested for
	 *         this actor
	 */
	final public boolean isFinishRequested() {
		return finishRequested;
	}
	
	/**
	 * @return a flag indicating whether this actor is executing
	 * in a model that is launched in mock/test-mode
	 */
	final public boolean isMockMode() {
		return mockMode;
	}

	/**
	 * @return all configurable parameters
	 */
	final public Parameter[] getConfigurableParameters() {
		return (Parameter[]) configurableParameters.toArray(new Parameter[configurableParameters.size()]);
	}
	
	/**
	 * Method attempts to find a Configurable parameter by class type.
	 * This encapsulates finding a parameter by name rather than pushing that
	 * find (current just a loop) to outside classes to implement.
	 * 
	 * @param name
	 * @return
	 */
	final public Collection<Parameter> getConfigurableParameter(final Class<? extends Parameter> type) {
		if (configurableParameters==null)     return null;
		if (configurableParameters.isEmpty()) return null;
		
		final Parameter[] params  = getConfigurableParameters();
		final Collection<Parameter> ret = new HashSet<Parameter>(params.length);
		for (int i = 0; i < params.length; i++) {
			if (type.isAssignableFrom(params[i].getClass())) {
				ret.add(params[i]);
			}
		}
		return ret;
	}
	
	/**
	 * Method attempts to find a Configurable parameter by name.
	 * This encapsulates finding a parameter by name rather than pushing that
	 * find (current just a loop) to outside classes to implement.
	 * 
	 * @param name
	 * @return
	 */
	final public Parameter getConfigurableParameter(final String name) {
		if (name==null)                       return null;
		if (configurableParameters==null)     return null;
		if (configurableParameters.isEmpty()) return null;
		final Parameter[] params = getConfigurableParameters();
		for (int i = 0; i < params.length; i++) {
			if (name.equals(params[i].getName())) {
				return params[i];
			}
		}
		return null;
	}

	/**
	 * Register an actor parameter as configurable. Such parameters will be
	 * available in the Passerelle model configuration tools. All other actor
	 * parameters are only available in model assembly tools.
	 * 
	 * @param newParameter
	 */
	final protected void registerConfigurableParameter(Parameter newParameter) {
		if (newParameter != null && !configurableParameters.contains(newParameter) && newParameter.getContainer().equals(this)) {
			configurableParameters.add(newParameter);
		}
		// should already be FULL, but let's be a bit profilactic
		newParameter.setVisibility(Settable.FULL);
	}

	/**
	 * Register an actor parameter as visible for experts only in the modeling
	 * tool.
	 * 
	 * This also sets the parameter's visibility for Ptolemy to Settable.EXPERT
	 * 
	 * @param newParameter
	 */
	final protected void registerExpertParameter(Parameter newParameter) {
		if (newParameter != null && newParameter.getContainer().equals(this)) {
			if (!expertParameters.contains(newParameter))
				expertParameters.add(newParameter);

			newParameter.setVisibility(Settable.EXPERT);
		}
	}

	/**
	 * @return
	 */
	final public static Logger getAuditLogger() {
		return auditLogger;
	}
	
	final protected ErrorControlStrategy getErrorControlStrategy() {
		if(errorControlStrategy!=null) {
			return errorControlStrategy;
		} else {
			try {
				ErrorControlStrategy result = ((Director)getDirector()).getErrorControlStrategy();
				return result;
			} catch (ClassCastException e) {
				// it's not a Passerelle Director, so revert to default behaviour
				errorControlStrategy = new DefaultActorErrorControlStrategy();
				return errorControlStrategy;
			}
		}
	}

	/**
	 * Default implementation just creates a standard message using the
	 * MessageFactory. This method may be overridden by actor sub-classes to
	 * handle message creation differently
	 * 
	 * @return
	 */
	public ManagedMessage createMessage() {
		return MessageFactory.getInstance().createMessage(getStandardMessageHeaders());
	}

	/**
	 * Default implementation just creates a standard message using the
	 * MessageFactory. This method may be overridden by actor sub-classes to
	 * handle message creation differently
	 * 
	 * @return
	 * @throws MessageException
	 */
	public ManagedMessage createMessage(Object content, String contentType) throws MessageException {
		ManagedMessage message = MessageFactory.getInstance().createMessage(getStandardMessageHeaders());
		message.setBodyContent(content, contentType);
		return message;
	}

	/**
	 * Default implementation just creates a standard message using the
	 * MessageFactory. This method may be overridden by actor sub-classes to
	 * handle message creation differently
	 * 
	 * @return
	 */
	public ManagedMessage createTriggerMessage() {
		return MessageFactory.getInstance().createTriggerMessage(getStandardMessageHeaders());
	}

	/**
	 * Default implementation for creating an error message, based on some
	 * exception. This method may be overridden by actor sub-classes to handle
	 * message creation differently.
	 * 
	 * @param exception
	 * @return
	 */
	public ManagedMessage createErrorMessage(PasserelleException exception) {
		return MessageFactory.getInstance().createErrorMessage(exception, getStandardMessageHeaders());
	}

	/**
	 * Utility method, to be used by actor implementations that need to override
	 * createMessage(), or create ManagedMessages in another way...
	 * 
	 * They should always pass this Map in the
	 * MessageFactory.createSomeMessage() methods...
	 * 
	 * TODO find some better way to enforce this...
	 * 
	 * @return
	 */
	final protected Map<String, String> getStandardMessageHeaders() {
		return actorMsgHeaders;
	}

	/**
	 * TODO investigate if we can have an alternative to the 'public',
	 * which still allows an error strategy to somehow get an actor
	 * to send an error message...
	 * 
	 * @param exception
	 * @throws IllegalActionException
	 */
	final public void sendErrorMessage(PasserelleException exception) throws IllegalActionException {
		if (logger.isInfoEnabled()) {
			logger.info(getInfo() + " sendErrorMessage() - generatinq error msg for exception", exception);
		}
		if (errorPort.getWidth() > 0) {
			ManagedMessage errorMessage = createErrorMessage(exception);
			Token errorToken = new PasserelleToken(errorMessage);
			errorPort.broadcast(errorToken);
		} else {
			// notify our director about the problem
			try {
				((Director) getDirector()).reportError(exception);
			} catch (ClassCastException ex) {
				// means the actor is used without a Passerelle Director
				// just log this. Only consequence is that we'll never receive
				// any error messages via acceptError
				logger.error(getInfo() + " sendErrorMessage() - used without Passerelle Director!!, so automated error collecting does NOT work !!");
				logger.error(getInfo() + " sendErrorMessage() - received exception",exception);
			}

		}
	}

	/**
	 * Send a message on an output port.
	 * @see sendOutputMsg(MessageAndPort).
	 * 
	 * @param port
	 * @param message
	 * @throws ProcessingException
	 * @throws IllegalArgumentException if the port is not a valid output port of this actor
	 */
	protected void sendOutputMsg(Port port, ManagedMessage message) throws ProcessingException, IllegalArgumentException {
		sendOutputMsg(MessageAndPort.create(this,port,message));
	}

	/**
	 * Send a message on an output port.
	 * Logs msg sending on debug level and in the audit trail.
	 * The log msg detail for the audit trail can be defined in actor sub-classes by overriding the method getAuditTrailMessage().
	 * 
	 * @param messageAndPort
	 * @throws ProcessingException
	 * @throws IllegalArgumentException if the port is not a valid output port of this actor
	 */
	protected void sendOutputMsg(MessageAndPort messageAndPort) throws ProcessingException, IllegalArgumentException {
		if(messageAndPort.getPort().getContainer()!=this)
			throw new IllegalArgumentException("port "+messageAndPort.getPort().getFullName()+" not defined in actor "+this.getFullName());
		
		try {
			Token token = new PasserelleToken(messageAndPort.getMessage());
			messageAndPort.getPort().broadcast(token);
		    if(logger.isDebugEnabled())
		    	logger.debug(getInfo()+" sendOutputMsg() - Message "+messageAndPort.getMessage().getID()+
		    			" sent on port "+messageAndPort.getPort().getName());
		    
		    String auditDetail = null;
		    try {
		    	auditDetail = getAuditTrailMessage(messageAndPort.getMessage(), messageAndPort.getPort());
		    } catch (Exception e) {
		    	// simple hack to log a default msg anyway
		    	auditDetail = "sent message on port "+messageAndPort.getPort().getFullName();
		    }
			if(auditDetail!=null && getAuditLogger().isInfoEnabled()) {
				getAuditLogger().info(auditDetail);
			}

		} catch (Exception e) {
			throw new ProcessingException(getInfo()+ " sendOutputMsg() - generated exception for sending msg on port "+
					messageAndPort.getPort(),messageAndPort.getMessage(),e);
		}
	}
	
	/**
	 * Method to be overridden to specify custom audit logging messages.
	 * When it returns null, no audit trail is logged for an outgoing message.
	 * 
	 * @param message
	 * @param port
	 * @return
	 */
	protected String getAuditTrailMessage(ManagedMessage message, Port port) throws Exception{
		return getInfo()+" sent message on port "+port.getFullName();
	}
	
	/**
	 * Utility method for actor implementations that wish to send out multiple
	 * messages in one fire iteration, and send these in a message sequence.
	 * 
	 * The order of the entries in the array determines the order that will be
	 * assigned in the sequence.
	 * 
	 * It is possible to send multiple messages on the same port, by re-entering
	 * it multiple times in the array.
	 * 
	 * The MessageAndPort entries must be created via the
	 * MessageAndPort.create() factory method.
	 * 
	 * @param ports
	 * @param messagesAndPorts
	 * @throws ProcessingException 
	 * 
	 * @see MessageAndPort
	 */
	final protected void sendOutputMsgs(MessageAndPort[] messagesAndPorts) throws ProcessingException {
		if (logger.isTraceEnabled()) {
			logger.trace(getInfo() + " sendMessages() - entry");
		}

		if (messagesAndPorts == null || messagesAndPorts.length==0) {
			return;
		}

		if (messagesAndPorts.length > 1) {
			Long seqID = MessageFactory.getInstance().createSequenceID();
			for (int i = 0; i < messagesAndPorts.length; i++) {
				MessageAndPort msgPort = messagesAndPorts[i];
				boolean isLastMsg = (i == (messagesAndPorts.length - 1));
				try {
					ManagedMessage msgInSeq = 
						MessageFactory.getInstance().createMessageCopyInSequence(msgPort.getMessage(), seqID, new Long(i), isLastMsg);
					sendOutputMsg(msgPort.getPort(), msgInSeq);
				} catch (MessageException e) {
					throw new ProcessingException("Error creating output sequence msg for msg " + msgPort.getMessage().getID(),
							msgPort.getMessage(), e);
				}
			}
		} else {
			sendOutputMsg(messagesAndPorts[0].getPort(), messagesAndPorts[0].getMessage());
		}

		if (logger.isTraceEnabled()) {
			logger.trace(getInfo() + " sendMessages() - exit");
		}
	}

	/**
	 * To be invoked by actors when the actual fire() processing is starting.
	 * The actor developer must ensure that this method is called 
	 * before the actual processing logic is being executed, after
	 * having received the relevant input messages (or leaving the blocked state for any other reason).
	 */
	protected void notifyStartingFireProcessing() {
		if(logger.isTraceEnabled()) {
			logger.trace(getInfo()+" notifyStartingFireProcessing() - entry");
		}
		statistics.beginCycle();
		isFiring=true;
		if(logger.isTraceEnabled()) {
			logger.trace(getInfo()+" notifyStartingFireProcessing() - exit");
		}
	}
	
	/**
	 * To be invoked by actors when the actual fire() processing is finished.
	 * The actor developer must ensure that this method is called 
	 * before the actor gets blocked, waiting for new input messages.
	 *
	 */
	protected void notifyFinishedFireProcessing() {
		if(logger.isTraceEnabled()) {
			logger.trace(getInfo()+" notifyFinishedFireProcessing() - entry");
		}
		statistics.endCycle();
		isFiring=false;
		if(logger.isTraceEnabled()) {
			logger.trace(getInfo()+" notifyFinishedFireProcessing() - exit");
		}
	}
}