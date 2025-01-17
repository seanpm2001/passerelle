/* An actor that hold the last event and outputs a constant signal.

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

import ptolemy.actor.lib.Transformer;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.ct.kernel.CTDirector;
import ptolemy.domains.ct.kernel.CTWaveformGenerator;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// ZeroOrderHold

/**
 Convert discrete events at the input to a continuous-time
 signal at the output by holding the value of the discrete
 event until the next discrete event arrives.

 @author Jie Liu
 @version $Id: ZeroOrderHold.java,v 1.51 2006/08/21 23:14:29 cxh Exp $
 @since Ptolemy II 0.4
 @Pt.ProposedRating Red (liuj)
 @Pt.AcceptedRating Red (cxh)
 */
public class ZeroOrderHold extends Transformer implements CTWaveformGenerator {
    /** Construct an actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The subsystem that this actor is lived in
     *  @param name The actor's name
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If name coincides with
     *   an entity already in the container.
     */
    public ZeroOrderHold(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        defaultValue = new Parameter(this, "defaultValue", new IntToken(0));
        output.setTypeAtLeast(input);
        output.setTypeAtLeast(defaultValue);
        new Parameter(input, "signalType", new StringToken("DISCRETE"));
        new Parameter(output, "signalType", new StringToken("CONTINUOUS"));

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-30\" y=\"-20\" " + "width=\"60\" height=\"40\" "
                + "style=\"fill:white\"/>\n"
                + "<polyline points=\"-25,10 -15,10 -15,-10 5,-10\"/>\n"
                + "<polyline points=\"5,-10 5,0 15,0 15,10 25,10\"/>\n"
                + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                       ////

    /**Default output before any input has received.
     * The default is an integer with value 0.
     * The type of the output is set to at least this type.
     */
    public Parameter defaultValue;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ZeroOrderHold newObject = (ZeroOrderHold) super.clone(workspace);
        newObject.output.setTypeAtLeast(newObject.input);
        newObject.output.setTypeAtLeast(newObject.defaultValue);
        return newObject;
    }

    /** Output the latest token consumed from the consumeCurrentEvents()
     *  call.
     *  @exception IllegalActionException If the token cannot be sent.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        CTDirector director = (CTDirector) getDirector();

        if (director.isDiscretePhase()) {
            if (input.hasToken(0)) {
                _lastToken = input.get(0);

                CTDirector dir = (CTDirector) getDirector();
                _debug(getFullName() + " receives an event at: "
                        + dir.getModelTime() + " with token "
                        + _lastToken.toString());
            }
        }

        output.send(0, _lastToken);
    }

    /** Initialize token. If there is no input, the initial token is
     *  a zero Double Token.
     *  @exception IllegalActionException If thrown by the super class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _lastToken = defaultValue.getToken();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Saved token.
    private Token _lastToken;
}
