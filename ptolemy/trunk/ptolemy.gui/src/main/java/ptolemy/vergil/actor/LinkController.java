/* The edge controller for links.

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

import ptolemy.actor.gui.ColorAttribute;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.PtolemyPreferences;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.moml.Vertex;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.basic.PopupMouseFilter;
import ptolemy.vergil.kernel.Link;
import ptolemy.vergil.toolbox.ConfigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import ptolemy.vergil.toolbox.PtolemyMenuFactory;
import diva.canvas.Figure;
import diva.canvas.Site;
import diva.canvas.connector.Connector;
import diva.canvas.connector.ConnectorAdapter;
import diva.canvas.connector.ConnectorEvent;
import diva.canvas.connector.ConnectorManipulator;
import diva.canvas.connector.ConnectorTarget;
import diva.canvas.connector.ManhattanConnector;
import diva.canvas.connector.PerimeterTarget;
import diva.canvas.connector.Terminal;
import diva.canvas.event.MouseFilter;
import diva.canvas.interactor.ActionInteractor;
import diva.canvas.interactor.SelectionInteractor;
import diva.canvas.interactor.SelectionModel;
import diva.canvas.toolbox.SVGUtilities;
import diva.graph.BasicEdgeController;
import diva.graph.EdgeRenderer;
import diva.graph.GraphController;
import diva.gui.toolbox.MenuCreator;

//////////////////////////////////////////////////////////////////////////
//// LinkController

/**
 This class provides interaction techniques for edges that are to be
 connected between ports and relations.  Standard interaction
 techniques for an undirected edge are allowed.

 @author Steve Neuendorffer, Contributor: Edward A. Lee
 @version $Id: LinkController.java,v 1.53 2007/12/16 07:29:48 cxh Exp $
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class LinkController extends BasicEdgeController {
    /** Create a link controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     */
    public LinkController(final GraphController controller) {
        super(controller);

        SelectionModel sm = controller.getSelectionModel();
        SelectionInteractor interactor = (SelectionInteractor) getEdgeInteractor();
        interactor.setSelectionModel(sm);

        // Create and set up the manipulator for connectors
        ConnectorManipulator manipulator = new ConnectorManipulator();
        manipulator.setSnapHalo(4.0);
        manipulator.addConnectorListener(new LinkDropper());
        interactor.setPrototypeDecorator(manipulator);

        // The mouse filter needs to accept regular click or control click
        MouseFilter handleFilter = new MouseFilter(1, 0, 0);
        manipulator.setHandleFilter(handleFilter);

        ConnectorTarget ct = new LinkTarget();
        setConnectorTarget(ct);
        setEdgeRenderer(new LinkRenderer());

        _menuCreator = new MenuCreator(null);
        _menuCreator.setMouseFilter(new PopupMouseFilter());
        interactor.addInteractor(_menuCreator);

        // The contents of the menu is determined by the associated
        // menu factory, which is a protected member of this class.
        // Derived classes can add menu items to it.
        _menuFactory = new PtolemyMenuFactory(controller);
        _menuFactory
                .addMenuItemFactory(new MenuActionFactory(_configureAction));
        _menuCreator.setMenuFactory(_menuFactory);

        // Add a double click interactor.
        ActionInteractor doubleClickInteractor = new ActionInteractor(
                _configureAction);
        doubleClickInteractor.setConsuming(false);
        doubleClickInteractor.setMouseFilter(new MouseFilter(1, 0, 0, 2));

        interactor.addInteractor(doubleClickInteractor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set the configuration.  This is may be used by derived controllers
     *  to open files or URLs.
     *  @param configuration The configuration.
     */
    public void setConfiguration(Configuration configuration) {
        _configuration = configuration;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected members                     ////

    /** The configuration. */
    protected Configuration _configuration;

    /** The configure action, which handles edit parameters requests. */
    protected static ConfigureAction _configureAction = new ConfigureAction(
            "Configure");

    /** The menu creator. */
    protected MenuCreator _menuCreator;

    /** The factory belonging to the menu creator. */
    protected PtolemyMenuFactory _menuFactory;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    /** A connector target that returns sites on a link. */
    public static class LinkTarget extends PerimeterTarget {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        /** Accept the head of the connector.
         *  @param c The connector.
         *  @param f The figure.
         *  @return True if the object is a Port, a Vertex or a Locatable
         *  contained by a Port and the super class accepts the head.
         *  Otherwise, return false.
         */
        public boolean acceptHead(Connector c, Figure f) {
            Object object = f.getUserObject();

            if (object instanceof Port
                    || object instanceof Vertex
                    || (object instanceof Locatable && ((Locatable) object)
                            .getContainer() instanceof Port)) {
                return super.acceptHead(c, f);
            } else {
                return false;
            }
        }

        /** Accept the tail of the connector.
         *  @param c The connector.
         *  @param f The figure.
         *  @return True if the object is a Port, a Vertex or a Locatable
         *  contained by a Port and the super class accepts the tail
         *  Otherwise, return false.
         */
        public boolean acceptTail(Connector c, Figure f) {
            Object object = f.getUserObject();

            if (object instanceof Port
                    || object instanceof Vertex
                    || (object instanceof Locatable && ((Locatable) object)
                            .getContainer() instanceof Port)) {
                return super.acceptTail(c, f);
            } else {
                return false;
            }
        }

        /** Get the head site.
         *  @param f The figure.
         *  @param x The x location.
         *  @param y The y location.
         *  @return The head site.
         */
        public Site getHeadSite(Figure f, double x, double y) {
            if (f instanceof Terminal) {
                Site site = ((Terminal) f).getConnectSite();
                return site;
            } else {
                return super.getHeadSite(f, x, y);
            }
        }

        // Tail sites are the same as head sites.
    }

    /** Render a visual representation of a link. */
    public static class LinkRenderer implements EdgeRenderer {

        /** Render a visual representation of the given edge. If the
         *  StringAttribute or ColorAttribute named
         *  _color of the edge is set then use that
         *  color to draw the line. If the StringAttribute
         *  _explanation of the edge is set then use it to set the
         *  tooltip.
         *  @param edge The edge.
         *  @param tailSite The tail site.
         *  @param headSite The head site.
         *  @return The Connector that represents the edge.
         */
        public Connector render(Object edge, Site tailSite, Site headSite) {
            Link link = (Link) edge;
            ManhattanConnector connector = new LinkManhattanConnector(tailSite,
                    headSite, link);

            if ((link.getHead() != null) && (link.getTail() != null)) {
                connector.setLineWidth((float) 2.0);
            }

            connector.setUserObject(edge);

            // The default bend radius of 50 is too large...
            // parallel curves look bad.
            connector.setBendRadius(20);

            Relation relation = link.getRelation();

            if (relation != null) {
                String tipText = relation.getName();
                String displayName = relation.getDisplayName();
                if (!tipText.equals(displayName)) {
                    tipText = displayName + " (" + tipText + ")";
                }
                connector.setToolTipText(tipText);

                try {
                    // FIXME: This isn't quite right for relation groups.
                    StringAttribute colorAttribute = (StringAttribute) (relation
                            .getAttribute("_color", StringAttribute.class));

                    if (colorAttribute != null) {
                        String color = colorAttribute.getExpression();
                        if (color != null && !color.trim().equals("")) {
                            connector.setStrokePaint(SVGUtilities
                                    .getColor(color));
                        }
                    }
                } catch (IllegalActionException e) {
                    // Ignore;
                }
                try {
                    // FIXME: This isn't quite right for relation groups.
                    ColorAttribute colorAttribute = (ColorAttribute) (relation
                            .getAttribute("color", ColorAttribute.class));

                    if (colorAttribute != null) {
                        Color color = colorAttribute.asColor();
                        connector.setStrokePaint(color);
                    }
                } catch (IllegalActionException e) {
                    // Ignore;
                }

                StringAttribute _explAttr = (StringAttribute) (relation
                        .getAttribute("_explanation"));

                if (_explAttr != null) {
                    connector.setToolTipText(_explAttr.getExpression());
                }

                // NOTE: The preferences mechanism may set this.
                Token radiusValue = PtolemyPreferences.preferenceValue(
                        relation, "_linkBendRadius");

                if (radiusValue instanceof DoubleToken) {
                    double overrideRadius = ((DoubleToken) radiusValue)
                            .doubleValue();
                    connector.setBendRadius(overrideRadius);
                }
            }

            return connector;
        }
    }

    /** An inner class that handles interactive changes to connectivity.
     */
    protected class LinkDropper extends ConnectorAdapter {
        /**
         * Called when a connector end is dropped--attach or
         * detach the edge as appropriate.
         * @param evt The connector event.
         */
        public void connectorDropped(ConnectorEvent evt) {
            Connector c = evt.getConnector();
            Figure f = evt.getTarget();
            Link link = (Link) c.getUserObject();
            Object node = (f == null) ? null : f.getUserObject();
            ActorGraphModel model = (ActorGraphModel) getController()
                    .getGraphModel();

            switch (evt.getEnd()) {
            case ConnectorEvent.HEAD_END:
                if (node == link.getTail()) {
                    MessageHandler
                            .error("Cannot link both ends to the same object.");
                    // FIXME: The panner needs to repaint.  How to get it to do that?
                    return;
                }
                model.getLinkModel().setHead(link, node);
                break;

            case ConnectorEvent.TAIL_END:
                if (node == link.getHead()) {
                    MessageHandler
                            .error("Cannot link both ends to the same object.");
                    // FIXME: The panner needs to repaint.  How to get it to do that?
                    return;
                }
                model.getLinkModel().setTail(link, node);
                break;

            default:
                throw new IllegalStateException(
                        "Cannot handle both ends of an edge being dragged.");
            }

            // Set the width correctly, so we know whether or not it
            // is connected.  Note that this happens *after* the model
            // is modified.
            if ((link.getHead() != null) && (link.getTail() != null)) {
                ((ManhattanConnector) c).setLineWidth((float) 2.0);
            } else {
                ((ManhattanConnector) c).setLineWidth((float) 1.0);
            }
        }
    }
}
