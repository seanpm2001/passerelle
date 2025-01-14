/* An icon that renders the name of the container.

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
package ptolemy.vergil.icon;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;

import javax.swing.SwingConstants;

import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.toolbox.BasicRectangle;
import diva.canvas.toolbox.LabelFigure;
import diva.canvas.toolbox.RoundedRectangle;

//////////////////////////////////////////////////////////////////////////
//// NameIcon

/**
 An icon that displays the name of the container in an appropriately
 sized box. Put this into a composite actor or in any actor to
 convert the icon for that actor into a simple box with the name
 of the actor instance. You will probably also want to set the
 actor instance to not display its name above its icon. You can
 do that via the Customize Name dialog (obtained by right clicking
 on the icon) or by creating a parameter named "_hideName" with
 value true.

 @author Edward A. Lee
 @version $Id: NameIcon.java,v 1.15 2007/12/06 21:57:03 cxh Exp $
 @since Ptolemy II 5.2
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class NameIcon extends EditorIcon {

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
    public NameIcon(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Create an icon for this attribute.
        // This has the side effect of making it visible
        // in Vergil, and giving a reasonable rendition.
        TextIcon icon = new TextIcon(this, "_icon");
        icon.setIconText("-N-");
        icon
                .setText("NameIcon attribute: This sets the icon to be a box with the name.");
        icon.setPersistent(false);

        // Hide the name.
        SingletonParameter hide = new SingletonParameter(this, "_hideName");
        hide.setToken(BooleanToken.TRUE);
        hide.setVisibility(Settable.EXPERT);

        rounding = new Parameter(this, "rounding");
        rounding.setTypeEquals(BaseType.DOUBLE);
        rounding.setExpression("0.0");

        spacing = new Parameter(this, "spacing");
        spacing.setTypeEquals(BaseType.DOUBLE);
        spacing.setExpression("0.0");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The amount of rounding of the corners.
     *  This is a double that defaults to 0.0, which indicates no rounding.
     */
    public Parameter rounding;

    /** If greater than zero, then use a double box where the outside
     *  one is the specified size larger than the inside one.
     *  This is a double that defaults to 0.0, which indicates a single
     *  box.
     */
    public Parameter spacing;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a changes in the attributes by changing
     *  the icon.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (should not be thrown).
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == rounding) {
            // Make sure that the new rounding value is valid.
            double roundingValue = ((DoubleToken) rounding.getToken())
                    .doubleValue();

            if (roundingValue < 0.0) {
                throw new IllegalActionException(this,
                        "Invalid rounding value. Required to be non-negative.");
            }

            if (roundingValue != _roundingValue) {
                _roundingValue = roundingValue;
            }
        } else if (attribute == spacing) {
            // Make sure that the new spacing value is valid.
            double spacingValue = ((DoubleToken) spacing.getToken())
                    .doubleValue();

            if (spacingValue < 0.0) {
                throw new IllegalActionException(this,
                        "Invalid spacing value. Required to be non-negative.");
            }

            if (spacingValue != _spacingValue) {
                _spacingValue = spacingValue;
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Create a new background figure.  This overrides the base class
     *  to draw a box around the value display, where the width of the
     *  box depends on the value.
     *  @return A new figure.
     */
    public Figure createBackgroundFigure() {
        String name = "No Name";
        NamedObj container = getContainer();
        if (container != null) {
            name = container.getDisplayName();
        }

        double width = 60;
        double height = 30;

        // Measure width of the text.  Unfortunately, this
        // requires generating a label figure that we will not use.
        LabelFigure label = new LabelFigure(name, _labelFont, 1.0,
                SwingConstants.CENTER);
        Rectangle2D stringBounds = label.getBounds();

        // NOTE: Padding of 20. Quantize the height so that
        // snap to grid still works.
        width = Math.floor(stringBounds.getWidth()) + 20;
        height = Math.floor(stringBounds.getHeight()) + 10;

        if (_spacingValue == 0.0) {
            if (_roundingValue == 0.0) {
                return new BasicRectangle(0, 0, width, height, _getFill(),
                        _getLineWidth());
            } else {
                return new RoundedRectangle(0, 0, width, height, _getFill(),
                        _getLineWidth(), _roundingValue, _roundingValue);
            }
        } else {
            CompositeFigure result;
            if (_roundingValue == 0.0) {
                result = new CompositeFigure(new BasicRectangle(-_spacingValue,
                        -_spacingValue, width + 2 * _spacingValue, height + 2
                                * _spacingValue, null, _getLineWidth()));
                result.add(new BasicRectangle(0, 0, width, height, _getFill(),
                        _getLineWidth()));
            } else {
                result = new CompositeFigure(new RoundedRectangle(
                        -_spacingValue, -_spacingValue, width + 2
                                * _spacingValue, height + 2 * _spacingValue,
                        null, _getLineWidth(), _roundingValue + _spacingValue,
                        _roundingValue + _spacingValue));
                result.add(new RoundedRectangle(0, 0, width, height,
                        _getFill(), _getLineWidth(), _roundingValue,
                        _roundingValue));
            }
            return result;
        }
    }

    /** Create a new Diva figure that visually represents this icon.
     *  The figure will be an instance of LabelFigure that renders the
     *  name of the container.
     *  @return A new CompositeFigure consisting of the label.
     */
    public Figure createFigure() {
        CompositeFigure result = (CompositeFigure) super.createFigure();

        String name = "No Name";
        NamedObj container = getContainer();
        if (container != null) {
            name = container.getDisplayName();
        }
        LabelFigure label = new LabelFigure(name, _labelFont, 1.0,
                SwingConstants.CENTER);
        Rectangle2D backBounds = result.getBackgroundFigure().getBounds();
        label.translateTo(backBounds.getCenterX(), backBounds.getCenterY());
        result.add(label);
        return result;
    }

    /** Override the base class to add or set a _hideName parameter.
     *  @param container The container to attach this attribute to..
     *  @exception IllegalActionException If this attribute is not of the
     *   expected class for the container, or it has no name,
     *   or the attribute and container are not in the same workspace, or
     *   the proposed container would result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this attribute.
     */
    public void setContainer(NamedObj container) throws IllegalActionException,
            NameDuplicationException {
        NamedObj previousContainer = getContainer();
        if (previousContainer != container && previousContainer != null) {
            SingletonParameter hide = (SingletonParameter) previousContainer
                    .getAttribute("_hideName", SingletonParameter.class);
            if (hide != null) {
                hide.setToken(BooleanToken.FALSE);
            }
        }
        super.setContainer(container);
        if (previousContainer != container && container != null) {
            // Hide the name.
            SingletonParameter hide = new SingletonParameter(container,
                    "_hideName");
            hide.setToken(BooleanToken.TRUE);
            hide.setVisibility(Settable.EXPERT);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the paint to use to fill the icon.
     *  This base class returns Color.white.
     *  @return The paint to use to fill the icon.
     */
    protected Paint _getFill() {
        return Color.white;
    }

    /** Return the line width to use in rendering the box.
     *  This base class returns 1.0f.
     *  @return The line width to use in rendering the box.
     */
    protected float _getLineWidth() {
        return 1.0f;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** The font used. */
    protected static Font _labelFont = new Font("SansSerif", Font.PLAIN, 12);

    /** Most recent value of the rounding parameter. */
    protected double _roundingValue = 0.0;

    /** Most recent value of the spacing parameter. */
    protected double _spacingValue = 0.0;
}
