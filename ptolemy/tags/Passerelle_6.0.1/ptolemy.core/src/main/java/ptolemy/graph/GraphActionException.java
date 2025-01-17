/* Exception for graph action errors.

 Copyright (c) 2002-2005 The University of Maryland.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 */
package ptolemy.graph;

import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// GraphActionException

/**
 Exception for graph action errors. This is a non-RuntimeException and
 should be caught by calling methods.

 @author Mingyung Ko
 @version $Id: GraphActionException.java,v 1.16 2005/07/08 19:58:59 cxh Exp $
 @since Ptolemy II 2.1
 @Pt.ProposedRating Red (myko)
 @Pt.AcceptedRating Red (ssb)
 */
public class GraphActionException extends IllegalActionException {
    /** Constructor with an argument of text description.
     *  @param message Detailed description of the error.
     */
    public GraphActionException(String message) {
        super(message);
    }
}
