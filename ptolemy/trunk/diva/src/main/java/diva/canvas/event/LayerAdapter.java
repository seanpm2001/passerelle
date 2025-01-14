/*
 Copyright (c) 1998-2005 The Regents of the University of California
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
 PROVIDED HEREUNDER IS ON AN  BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 *
 */
package diva.canvas.event;

/** An adapter for layer listeners. The methods in this class
 * are empty -- the class is provided to make it easier to
 * produce anonymous LayerListeners.
 *
 * @version        $Id: LayerAdapter.java,v 1.8 2005/07/08 19:54:52 cxh Exp $
 * @author         John Reekie
 */
public class LayerAdapter implements LayerListener {
    /** Invoked when the mouse moves while the button is still held
     * down.
     */
    public void mouseDragged(LayerEvent e) {
    }

    /** Invoked when the mouse is pressed on a layer or figure.
     */
    public void mousePressed(LayerEvent e) {
    }

    /** Invoked when the mouse is released on a layer or figure.
     */
    public void mouseReleased(LayerEvent e) {
    }

    /** Invoked when the mouse is clicked on a layer or figure.
     */
    public void mouseClicked(LayerEvent e) {
    }
}
