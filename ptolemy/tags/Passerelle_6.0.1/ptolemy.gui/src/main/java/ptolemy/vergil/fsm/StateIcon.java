/* An icon specialized for states of a state machine.

 Copyright (c) 2006-2007 The Regents of the University of California.
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
package ptolemy.vergil.fsm;

import java.awt.Color;
import java.awt.Paint;

import ptolemy.actor.TypedActor;
import ptolemy.data.BooleanToken;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.icon.NameIcon;

//////////////////////////////////////////////////////////////////////////
//// StateIcon

/**
 An icon that displays the name of the container in an appropriately
 sized rounded box. This is designed to be contained by an instance
 of State, and if it is, and if the state is the initial state, then
 the rounded box will be bold. If it is a final state, then it will
 be double.

 @author Edward A. Lee
 @version $Id: StateIcon.java,v 1.6 2007/12/06 18:33:53 cxh Exp $
 @since Ptolemy II 6.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class StateIcon extends NameIcon {

    /** Create a new icon with the given name in the given container.
     *  The container is required to implement Settable, or an exception
     *  will be thrown.
     *  @param container The container for this attribute.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If thrown by the parent
     *  class or while setting an attribute.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public StateIcon(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Change the default rounding to 20.
        rounding.setExpression("20");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the paint to use to fill the icon.
     *  This class returns Color.white, unless the refinement name
     *  is not empty, in which case it returns a light green.
     *  @return The paint to use to fill the icon.
     */
    protected Paint _getFill() {
        NamedObj container = getContainer();
        if (container instanceof State) {
            try {
                TypedActor[] refinement = ((State) container).getRefinement();
                if (refinement != null && refinement.length > 0) {
                    return _REFINEMENT_COLOR;
                }
            } catch (IllegalActionException e) {
                // Ignore and return the default.
            }
        }
        return Color.white;
    }

    /** Return the line width to use in rendering the box.
     *  This returns 1.0f, unless the container is an instance of State
     *  and its <i>isInitialState</i> parameter is set to true.
     *  @return The line width to use in rendering the box.
     */
    protected float _getLineWidth() {
        NamedObj container = getContainer();
        if (container instanceof State) {
            try {
                if (((BooleanToken) (((State) container).isInitialState
                        .getToken())).booleanValue()) {
                    return 2.0f;
                }
            } catch (IllegalActionException e) {
                // Ignore and return the default.
            }
        }
        return 1.0f;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The fill color for states with refinements. */
    private static Color _REFINEMENT_COLOR = new Color(0.8f, 1.0f, 0.8f, 1.0f);
}
