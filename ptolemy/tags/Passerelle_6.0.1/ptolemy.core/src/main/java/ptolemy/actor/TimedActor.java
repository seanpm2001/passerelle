/* Marker interface for actors that operate on time-based signals.

 Copyright (c) 2004-2005 The Regents of the University of California.
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
package ptolemy.actor;

//////////////////////////////////////////////////////////////////////////
//// TimedActor

/**
 This is a marker interface for actors that operate on time-based signals.
 That is, logically, all signals observed or produced are tokens placed
 in time. Actors that implement this marker interface can only be executed
 by the directors that implement the TimedDirector interface.

 @see TimedDirector
 @author Edward A. Lee
 @version $Id: TimedActor.java,v 1.7 2005/07/08 19:37:37 cxh Exp $
 @since Ptolemy II 4.1
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (bilung)
 */
public interface TimedActor {
}
