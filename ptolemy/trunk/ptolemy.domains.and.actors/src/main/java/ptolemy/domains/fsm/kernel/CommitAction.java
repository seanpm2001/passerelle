/* Marker interface of commit action.

 Copyright (c) 1999-2005 The Regents of the University of California.
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
package ptolemy.domains.fsm.kernel;

//////////////////////////////////////////////////////////////////////////
//// CommitAction

/**
 An action implementing this interface is a commit action. When an FSMActor
 is postfired, the chosen transition of the latest firing of the actor is
 committed. The commit actions contained by the transition are executed and
 the current state of the actor is set to the destination state of the
 transition.

 @author Xiaojun Liu
 @version $Id: CommitAction.java,v 1.17 2005/07/08 19:58:19 cxh Exp $
 @since Ptolemy II 0.4
 @Pt.ProposedRating Yellow (liuxj)
 @Pt.AcceptedRating Yellow (liuxj)
 @see Action
 @see Transition
 @see FSMActor
 */
public interface CommitAction {
}
