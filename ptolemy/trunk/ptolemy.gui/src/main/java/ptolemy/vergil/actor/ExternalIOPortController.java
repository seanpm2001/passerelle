/* The graph controller for the ptolemy schematic editor ports

 Copyright (c) 1998-2007 The Regents of the University of California.
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
package ptolemy.vergil.actor;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import javax.swing.SwingConstants;

import ptolemy.actor.IOPort;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.data.type.Typeable;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.kernel.AttributeController;
import diva.canvas.CanvasUtilities;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.Site;
import diva.canvas.connector.FixedNormalSite;
import diva.canvas.connector.PerimeterSite;
import diva.canvas.connector.TerminalFigure;
import diva.canvas.toolbox.BasicFigure;
import diva.canvas.toolbox.LabelFigure;
import diva.graph.GraphController;
import diva.graph.NodeRenderer;
import diva.util.java2d.Polygon2D;

//////////////////////////////////////////////////////////////////////////
//// ExternalIOPortController

/**
 This class provides interaction with nodes that represent Ptolemy II
 ports inside a composite.  It provides a double click binding and context
 menu entry to edit the parameters of the port ("Configure") and a
 command to get documentation.
 It can have one of two access levels, FULL or PARTIAL.
 If the access level is FULL, the the context menu also
 contains a command to rename the node.
 Note that whether the port is an input or output or multiport cannot
 be controlled via this interface.  The "Configure Ports" command of
 the container should be invoked instead.

 @author Steve Neuendorffer and Edward A. Lee, Elaine Cheong
 @version $Id: ExternalIOPortController.java,v 1.53 2007/12/06 21:56:28 cxh Exp $
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class ExternalIOPortController extends AttributeController {
    /** Create a port controller associated with the specified graph
     *  controller.  The controller is given full access.
     *  @param controller The associated graph controller.
     */
    public ExternalIOPortController(GraphController controller) {
        this(controller, FULL);
    }

    /** Create a port controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     *  @param access The access level.
     */
    public ExternalIOPortController(GraphController controller, Access access) {
        super(controller, access);
        setNodeRenderer(new PortRenderer());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public members                    ////

    /** Prototype input port. */
    public static final IOPort _GENERIC_INPUT = new IOPort();

    /** Prototype output port. */
    public static final IOPort _GENERIC_OUTPUT = new IOPort();

    /** Prototype inout port. */
    public static final IOPort _GENERIC_INOUT = new IOPort();

    /** Prototype input multiport. */
    public static final IOPort _GENERIC_INPUT_MULTIPORT = new IOPort();

    /** Prototype output multiport. */
    public static final IOPort _GENERIC_OUTPUT_MULTIPORT = new IOPort();

    /** Prototype inout multiport. */
    public static final IOPort _GENERIC_INOUT_MULTIPORT = new IOPort();

    // Static initializer.
    static {
        try {
            _GENERIC_INPUT.setInput(true);
            _GENERIC_OUTPUT.setOutput(true);
            _GENERIC_INOUT.setInput(true);
            _GENERIC_INOUT.setOutput(true);
            _GENERIC_INPUT_MULTIPORT.setInput(true);
            _GENERIC_OUTPUT_MULTIPORT.setOutput(true);
            _GENERIC_INOUT_MULTIPORT.setInput(true);
            _GENERIC_INOUT_MULTIPORT.setOutput(true);
            _GENERIC_INPUT_MULTIPORT.setMultiport(true);
            _GENERIC_OUTPUT_MULTIPORT.setMultiport(true);
            _GENERIC_INOUT_MULTIPORT.setMultiport(true);

            // Need location attributes for these ports in order to
            // be able to render them.
            new Location(_GENERIC_INPUT, "_location");
            new Location(_GENERIC_OUTPUT, "_location");
            new Location(_GENERIC_INOUT, "_location");
            new Location(_GENERIC_INPUT_MULTIPORT, "_location");
            new Location(_GENERIC_OUTPUT_MULTIPORT, "_location");
            new Location(_GENERIC_INOUT_MULTIPORT, "_location");
        } catch (KernelException ex) {
            // Should not occur.
            throw new InternalErrorException(null, ex, null);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to return true if the specified node
     *  contains an attribute named "_hideInside".  This ensures that
     *  ports can be hidden on the outside while still being visible
     *  on the outside.
     */
    protected boolean _hide(java.lang.Object node) {
        if (node instanceof Locatable) {
            if ((((Locatable) node).getContainer()).getAttribute("_hideInside") != null) {
                return true;
            }
        }

        if (node instanceof NamedObj) {
            if (((NamedObj) node).getAttribute("_hideInside") != null) {
                return true;
            }
        }

        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Given a port, return a reasonable tooltip message for that port.
     *  @param port The port.
     *  @return The name, type, and whether it's a multiport.
     */
    private String _portTooltip(final Port port) {
        String tipText = port.getName();

        if (port instanceof IOPort) {
            IOPort ioport = (IOPort) port;

            if (ioport.isInput()) {
                tipText += ", Input";
            }

            if (ioport.isOutput()) {
                tipText += ", Output";
            }

            if (ioport.isMultiport()) {
                tipText += ", Multiport";
            }

            try {
                tipText = tipText + ", type:" + ((Typeable) port).getType();
            } catch (ClassCastException ex) {
                // Do nothing.
            } catch (IllegalActionException ex) {
                // Do nothing.
            }
        }

        return tipText;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    private static Font _labelFont = new Font("SansSerif", Font.PLAIN, 12);

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Render the external ports of a graph as a 5-sided tab thingy.
     *  Multiports are rendered hollow,
     *  while single ports are rendered filled.
     */
    public class PortRenderer implements NodeRenderer {
        /** Render a port.  If the argument implements Locatable,
         *  then render the port that is the container of that locatable.
         *  If the argument is an instance of _GENERIC_INPUT,
         *  _GENERIC_OUTPUT, or _GENERIC_INOUT, then render an input,
         *  output, or inout port with no name.  If the argument is null,
         *  then render a port that is neither an input nor an output.
         *  @param n An instance of Locatable or one of the objects
         *   _GENERIC_INPUT, _GENERIC_OUTPUT, or _GENERIC_INOUT.
         *  @return The figure that is rendered.
         */
        public Figure render(Object n) {
            Polygon2D.Double polygon = new Polygon2D.Double();

            Figure figure;

            // Wrap the figure in a TerminalFigure to set the direction that
            // connectors exit the port.  Note that this direction is the
            // OPPOSITE direction that is used to layout the port in the
            // Entity Controller.
            int direction;
            Locatable location = (Location) n;

            if (location != null) {
                final Port port = (Port) location.getContainer();

                Color fill;

                if (!(port instanceof IOPort)) {
                    polygon.moveTo(-6, 6);
                    polygon.lineTo(0, 6);
                    polygon.lineTo(8, 0);
                    polygon.lineTo(0, -6);
                    polygon.lineTo(-6, -6);
                    fill = Color.black;
                } else {
                    IOPort ioport = (IOPort) port;
                    polygon.moveTo(-8, 4);
                    if (ioport.isMultiport()) {
                        if (ioport instanceof ParameterPort) {
                            // It would be better to couple these to the
                            // parameters by position, but this is impossible
                            // in diva, so we assign a special color.
                            // FIXME: are Multiport ParameterPorts possible?
                            // FIXME: Should this be lightGrey?
                            fill = Color.darkGray;
                        } else {
                            fill = Color.white;
                        }
                        if (ioport.isOutput() && ioport.isInput()) {
                            polygon.lineTo(-4, 4);
                            polygon.lineTo(-4, 9);
                            polygon.lineTo(2, 4);
                            polygon.lineTo(2, 9);
                            polygon.lineTo(8, 4);
                            polygon.lineTo(12, 4);
                            polygon.lineTo(12, -4);
                            polygon.lineTo(8, -4);
                            polygon.lineTo(2, -9);
                            polygon.lineTo(2, -4);
                            polygon.lineTo(-4, -9);
                            polygon.lineTo(-4, -4);
                            polygon.lineTo(-8, -4);
                        } else if (ioport.isOutput()) {
                            polygon.lineTo(-8, 4);
                            polygon.lineTo(-8, 9);
                            polygon.lineTo(-2, 4);
                            polygon.lineTo(-2, 9);
                            polygon.lineTo(4, 4);
                            polygon.lineTo(12, 4);
                            polygon.lineTo(12, -4);
                            polygon.lineTo(4, -4);
                            polygon.lineTo(-2, -9);
                            polygon.lineTo(-2, -4);
                            polygon.lineTo(-8, -9);
                        } else if (ioport.isInput()) {
                            polygon.lineTo(-5, 4);
                            polygon.lineTo(-5, 9);
                            polygon.lineTo(1, 4);
                            polygon.lineTo(1, 9);
                            polygon.lineTo(7, 4);
                            polygon.lineTo(12, 0);
                            polygon.lineTo(7, -4);
                            polygon.lineTo(1, -9);
                            polygon.lineTo(1, -4);
                            polygon.lineTo(-5, -9);
                            polygon.lineTo(-5, -4);
                            polygon.lineTo(-8, -4);
                        } else {
                            throw new InternalErrorException(port, null,
                                    "Port is neither input nor output");
                        }
                    } else {
                        if (ioport instanceof ParameterPort) {
                            // It would be better to couple these to the
                            // parameters by position, but this is impossible
                            // in diva, so we assign a special color.
                            // FIXME: what about multiports para
                            fill = Color.lightGray;
                        } else {
                            fill = Color.black;
                        }
                        if (ioport.isOutput() && ioport.isInput()) {
                            polygon.lineTo(0, 4);
                            polygon.lineTo(0, 9);
                            polygon.lineTo(6, 4);
                            polygon.lineTo(12, 4);
                            polygon.lineTo(12, -4);
                            polygon.lineTo(6, -4);
                            polygon.lineTo(0, -9);
                            polygon.lineTo(0, -4);
                            polygon.lineTo(-8, -4);
                        } else if (ioport.isOutput()) {
                            polygon.lineTo(-8, 9);
                            polygon.lineTo(-2, 4);
                            polygon.lineTo(12, 4);
                            polygon.lineTo(12, -4);
                            polygon.lineTo(-2, -4);
                            polygon.lineTo(-8, -9);
                        } else if (ioport.isInput()) {
                            polygon.lineTo(0, 4);
                            polygon.lineTo(0, 9);
                            //polygon.lineTo(6, 4);
                            polygon.lineTo(12, 0);
                            //polygon.lineTo(6, -4);
                            polygon.lineTo(0, -9);
                            polygon.lineTo(0, -4);
                            polygon.lineTo(-8, -4);
                        } else {
                            throw new InternalErrorException(port, null,
                                    "Port is neither input nor output");
                        }
                    }
                }
                polygon.closePath();

                if (port instanceof ParameterPort) {
                    // Create a PaintedList that has two PaintedPaths,
                    // the usual icon and the > shape.
                    //                     PaintedList paintedList = new PaintedList();
                    //                     paintedList.add(new PaintedPath(polygon, (float) 1.5, fill));
                    //                     Polygon2D.Double polygon2 = new Polygon2D.Double();
                    //                     //polygon2.moveTo(-15,-15);
                    //                     //polygon2.lineTo(-3,-5);
                    //                     //polygon2.lineTo(-16,-5);

                    //                     polygon2.moveTo(5, 9);

                    //                     polygon2.lineTo(17, 0);
                    //                     polygon2.lineTo(5, -9);

                    //                     polygon2.lineTo(5, -9);
                    //                     polygon2.lineTo(17, 0);
                    //                     polygon2.lineTo(5, 9);

                    //                     //polygon2.lineTo(5, -9);
                    //                     //polygon2.lineTo(17, 0);
                    //                     //polygon2.lineTo(5, 9);

                    //                     polygon2.closePath();
                    //                     paintedList.add(new PaintedPath(polygon2, (float) 1.0, Color.black));
                    //                     figure = new PaintedFigure(paintedList);
                    figure = new BasicFigure(polygon, new Color(0, 0, 0, 0),
                            (float) 0.0);
                } else {
                    figure = new BasicFigure(polygon, fill, (float) 1.5);
                }

                if (!(port instanceof IOPort)) {
                    direction = SwingConstants.NORTH;
                } else {
                    IOPort ioport = (IOPort) port;

                    if (ioport.isInput() && ioport.isOutput()) {
                        direction = SwingConstants.NORTH;
                    } else if (ioport.isInput()) {
                        direction = SwingConstants.EAST;
                    } else if (ioport.isOutput()) {
                        direction = SwingConstants.WEST;
                    } else {
                        // should never happen
                        direction = SwingConstants.NORTH;
                    }
                }

                double normal = CanvasUtilities.getNormal(direction);

                String name = port.getName();
                Rectangle2D backBounds = figure.getBounds();
                figure = new CompositeFigure(figure) {
                    // Override this because we want to show the type.
                    // It doesn't work to set it once because the type
                    // has not been resolved, and anyway, it may
                    // change. NOTE: This is copied from above.
                    public String getToolTipText() {
                        return _portTooltip(port);
                    }
                };

                if ((name != null) && !name.equals("")
                        && !(port instanceof ParameterPort)) {
                    LabelFigure label = new LabelFigure(name, _labelFont, 1.0,
                            SwingConstants.SOUTH_WEST);

                    // Shift the label slightly right so it doesn't
                    // collide with ports.
                    label.translateTo(backBounds.getX(), backBounds.getY());
                    ((CompositeFigure) figure).add(label);
                }

                if (port instanceof IOPort) {
                    // Create a diagonal connector for multiports, if necessary.
                    IOPort ioPort = (IOPort) port;

                    if (ioPort.isMultiport()) {
                        int numberOfLinks = ioPort.insideRelationList().size();

                        if (numberOfLinks > 1) {
                            // The diagonal is necessary.
                            // Line depends on the orientation.
                            double startX;

                            // The diagonal is necessary.
                            // Line depends on the orientation.
                            double startY;

                            // The diagonal is necessary.
                            // Line depends on the orientation.
                            double endX;

                            // The diagonal is necessary.
                            // Line depends on the orientation.
                            double endY;
                            Rectangle2D bounds = figure.getShape()
                                    .getBounds2D();
                            double x = bounds.getX();
                            double y = bounds.getY();
                            double width = bounds.getWidth();
                            double height = bounds.getHeight();
                            int extent = numberOfLinks - 1;

                            if (direction == SwingConstants.EAST) {
                                startX = x + width;
                                startY = y + (height / 2);
                                endX = startX
                                        + (extent * IOPortController.MULTIPORT_CONNECTION_SPACING);
                                endY = startY
                                        + (extent * IOPortController.MULTIPORT_CONNECTION_SPACING);
                            } else if (direction == SwingConstants.WEST) {
                                startX = x;
                                startY = y + (height / 2);
                                endX = startX
                                        - (extent * IOPortController.MULTIPORT_CONNECTION_SPACING);
                                endY = startY
                                        - (extent * IOPortController.MULTIPORT_CONNECTION_SPACING);
                            } else if (direction == SwingConstants.NORTH) {
                                startX = x + (width / 2);
                                startY = y;
                                endX = startX
                                        - (extent * IOPortController.MULTIPORT_CONNECTION_SPACING);
                                endY = startY
                                        - (extent * IOPortController.MULTIPORT_CONNECTION_SPACING);
                            } else {
                                startX = x + (width / 2);
                                startY = y + height;
                                endX = startX
                                        + (extent * IOPortController.MULTIPORT_CONNECTION_SPACING);
                                endY = startY
                                        + (extent * IOPortController.MULTIPORT_CONNECTION_SPACING);
                            }

                            Line2D line = new Line2D.Double(startX, startY,
                                    endX, endY);
                            Figure lineFigure = new BasicFigure(line, fill,
                                    (float) 2.0);
                            ((CompositeFigure) figure).add(lineFigure);
                        }
                    }

                    figure = new PortTerminal(ioPort, figure, normal, true);
                } else {
                    Site tsite = new PerimeterSite(figure, 0);
                    tsite.setNormal(normal);
                    tsite = new FixedNormalSite(tsite);
                    figure = new TerminalFigure(figure, tsite) {
                        // Override this because the tooltip may
                        // change over time.  I.e., the port may
                        // change from being an input or output, etc.
                        public String getToolTipText() {
                            return _portTooltip(port);
                        }
                    };
                }

                // Have to do this as well or awt will not render a tooltip.
                figure.setToolTipText(port.getName());
            } else {
                polygon.moveTo(0, 0);
                polygon.lineTo(0, 10);
                polygon.lineTo(12, 0);
                polygon.lineTo(0, -10);
                polygon.closePath();

                figure = new BasicFigure(polygon, Color.black);
                figure.setToolTipText("Unknown port");
            }

            return figure;
        }
    }
}
