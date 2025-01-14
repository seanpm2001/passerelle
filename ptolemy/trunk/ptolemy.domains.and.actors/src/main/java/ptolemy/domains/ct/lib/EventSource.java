/* Generate discrete events at prespecified time instants.

 Copyright (c) 1998-2006 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.domains.ct.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ct.kernel.CTDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// EventSource

/**
 This actor outputs a set of events at a discrete set of time points.
 It can be used to generate impulses in CT models.
 <p>
 This actor only generates predictable events and that is why it does not
 implement the CTStepSizeControlActor interface. This actor requests a
 refiring at its initialize method, then only fires at discrete phase of
 execution to produce events. During its postfire method, it requests
 further firings to produce more events if necessary.

 @author Jie Liu, Haiyang Zheng
 @version $Id: EventSource.java,v 1.41 2006/10/26 21:44:26 cxh Exp $
 @since Ptolemy II 2.0
 @Pt.ProposedRating Yellow (hyzheng)
 @Pt.AcceptedRating Red (liuj)
 */
public class EventSource extends TypedAtomicActor {
    /** Construct an actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The actor's name
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If name coincides with
     *   an entity already in the container.
     */
    public EventSource(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Create port and parameters.
        output = new TypedIOPort(this, "output", false, true);
        new Parameter(output, "signalType", new StringToken("DISCRETE"));

        period = new Parameter(this, "period");
        period.setExpression("2.0");
        period.setTypeEquals(BaseType.DOUBLE);

        offsets = new Parameter(this, "offsets");
        offsets.setExpression("{0.0, 1.0}");
        offsets.setTypeEquals(new ArrayType(BaseType.DOUBLE));

        // Call this so that we don't have to copy its code here...
        attributeChanged(offsets);

        // set the values parameter
        values = new Parameter(this, "values");
        values.setExpression("{1, 0}");

        // Set type constraints.
        output.setTypeAtLeast(ArrayType.elementType(values));

        // Call this so that we don't have to copy its code here...
        attributeChanged(values);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The output port.  The type of this port is determined by from
     *  the <i>values</i> parameter.
     */
    public TypedIOPort output = null;

    /** The offsets at which the specified values will be produced.
     *  This parameter must contain an array of doubles, and it defaults
     *  to {0.0, 1.0}.
     */
    public Parameter offsets;

    /** The period of the output events.
     *  This parameter must contain a DoubleToken, and defaults to 2.0.
     */
    public Parameter period;

    /** The values that will be produced at the specified offsets.
     *  This parameter must contain an ArrayToken, and defaults to {1, 0}.
     */
    public Parameter values;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the <i>offsets</i> parameter, check that the
     *  array is nondecreasing and has the right dimension; if the
     *  argument is <i>period</i>, check that it is positive. Other
     *  sanity checks with <i>period</i> and <i>values</i> are done in
     *  the fire() method.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the offsets array is not
     *   nondecreasing and nonnegative, or it is not a row vector.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == offsets) {
            ArrayToken offsetsValue = (ArrayToken) offsets.getToken();
            _offsets = new double[offsetsValue.length()];

            double previous = 0.0;

            for (int i = 0; i < offsetsValue.length(); i++) {
                _offsets[i] = ((DoubleToken) offsetsValue.getElement(i))
                        .doubleValue();

                // Check nondecreasing property.
                if (_offsets[i] < previous) {
                    throw new IllegalActionException(this,
                            "Value of offsets is not nondecreasing "
                                    + "and nonnegative.");
                }

                previous = _offsets[i];
            }
        } else if (attribute == period) {
            double periodValue = ((DoubleToken) period.getToken())
                    .doubleValue();

            if (periodValue <= 0.0) {
                throw new IllegalActionException(this,
                        "Period is required to be positive.  "
                                + "Period given: " + periodValue);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the parameter public members to refer
     *  to the parameters of the new actor.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        EventSource newObject = (EventSource) super.clone(workspace);

        try {
            newObject.output.setTypeAtLeast(ArrayType
                    .elementType(newObject.values));
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }

        return newObject;
    }

    /** Emit the discrete event that happens at the current time. If there
     *  is no such events, do nothing.
     *  @exception IllegalActionException If the event cannot be sent.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        output.send(0, ((ArrayToken) values.getToken()).getElement(_phase));
    }

    /** Schedule the first firing and initialize local variables.
     *  @exception IllegalActionException If the parent class throws it,
     *   or if the <i>values</i> parameter is not a row vector, or if the
     *   fireAt() method of the director throws it.
     */
    public synchronized void initialize() throws IllegalActionException {
        super.initialize();

        _cycleStartTime = getDirector().getModelTime();
        _phase = 0;
        _nextOutputTime = _cycleStartTime.add(_offsets[0]);

        // Schedule the first firing.
        getDirector().fireAt(this, _nextOutputTime);
    }

    /** Return true if this actor is scheduled to fire at the current time.
     *  @return True if this actor is scheduled to fire at the current time.
     *  @exception IllegalActionException If thrown by the super class.
     */
    public boolean prefire() throws IllegalActionException {
        CTDirector director = (CTDirector) getDirector();
        boolean result = (director.getModelTime().compareTo(_nextOutputTime) == 0);
        return result && super.prefire();
    }

    /** Update the state of the actor and schedule the next firing,
     *  if the director is in the discrete phase.
     *  @exception IllegalActionException If the director throws it when
     *   scheduling the next firing, or if the length of the values and
     *   offsets parameters don't match.
     */
    public boolean postfire() throws IllegalActionException {
        CTDirector director = (CTDirector) getDirector();
        double periodValue = ((DoubleToken) period.getToken()).doubleValue();

        // Increment to the next phase.
        _phase++;

        if (_phase >= _offsets.length) {
            _phase = 0;
            _cycleStartTime = _cycleStartTime.add(periodValue);
        }

        if (_offsets[_phase] >= periodValue) {
            throw new IllegalActionException(this, "Offset number " + _phase
                    + " with value " + _offsets[_phase]
                    + " must be less than the " + "period, which is "
                    + periodValue);
        }

        _nextOutputTime = _cycleStartTime.add(_offsets[_phase]);
        director.fireAt(this, _nextOutputTime);
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The following are all transient because they need not be cloned.
    // Either the clone method or the initialize() method sets them.
    // The most recent cycle start time.
    private transient Time _cycleStartTime;

    // Cache of offsets array value.
    private transient double[] _offsets;

    // The phase of the next output.
    private transient int _phase;

    // The next time point when the output should be emitted.
    private transient Time _nextOutputTime;
}
