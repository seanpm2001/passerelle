/* An action that opens a dialog to rename an object.

 Copyright (c) 2006 The Regents of the University of California.
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
package ptolemy.vergil.kernel;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import ptolemy.actor.gui.RenameDialog;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.toolbox.FigureAction;

//////////////////////////////////////////////////////////////////////////
//// RenameDialogAction

/**
 An action that creates a dialog to rename an object.

 @author Edward A. Lee and Steve Neuendorffer
 @version $Id: RenameDialogAction.java,v 1.4 2006/09/21 15:39:55 cxh Exp $
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class RenameDialogAction extends FigureAction {

    /** Construct a rename dialog action with the specified name,
     *  which will appear in the menu that uses this action.
     *  @param name The name.
     */
    public RenameDialogAction(String name) {
        super(name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Open a dialog to rename the target.
     *  @param event The action event.
     */
    public void actionPerformed(ActionEvent event) {
        try {
            // Determine which entity was selected for the look inside action.
            super.actionPerformed(event);
            NamedObj target = getTarget();
            if (target == null) {
                return;
            }
            // Create a dialog for configuring the object.
            // First, identify the top parent frame.
            Frame parent = getFrame();
            new RenameDialog(parent, target);
        } catch (Throwable throwable) {
            // Giotto code generator on giotto/demo/Hierarchy/Hierarchy.xml
            // was throwing an exception here that was not being displayed
            // in the UI.
            MessageHandler.error(
                    "Failed to open a dialog to rename the target.", throwable);
        }
    }
}
