/* The graph controller for vergil.

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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;

import ptolemy.actor.gui.Configuration;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.basic.NamedObjController;
import ptolemy.vergil.kernel.AttributeController;
import ptolemy.vergil.kernel.ConfigureUnitsAction;
import ptolemy.vergil.kernel.Link;
import ptolemy.vergil.kernel.PortDialogAction;
import ptolemy.vergil.kernel.RelationController;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuItemFactory;
import ptolemy.vergil.toolbox.SnapConstraint;
import diva.canvas.Figure;
import diva.canvas.FigureLayer;
import diva.canvas.Site;
import diva.canvas.connector.AutonomousSite;
import diva.canvas.connector.Connector;
import diva.canvas.connector.ConnectorManipulator;
import diva.canvas.event.LayerEvent;
import diva.canvas.event.MouseFilter;
import diva.canvas.interactor.AbstractInteractor;
import diva.canvas.interactor.ActionInteractor;
import diva.canvas.interactor.CompositeInteractor;
import diva.canvas.interactor.GrabHandle;
import diva.canvas.interactor.Interactor;
import diva.graph.GraphPane;
import diva.graph.NodeRenderer;
import diva.gui.GUIUtilities;
import diva.gui.toolbox.FigureIcon;
import diva.gui.toolbox.JContextMenu;

//////////////////////////////////////////////////////////////////////////
//// ActorEditorGraphController

/**
 A Graph Controller for the Ptolemy II schematic editor.  In addition to the
 interaction allowed in the viewer, this controller allows nodes to be
 dragged and dropped onto its graph.  Relations can be created by
 control-clicking on the background.  Links can be created by control-clicking
 and dragging on a port or a relation.  In addition links can be created by
 clicking and dragging on the ports that are inside an entity.
 Anything can be deleted by selecting it and pressing
 the delete key on the keyboard.

 @author Steve Neuendorffer, Contributor: Edward A. Lee
 @version $Id: ActorEditorGraphController.java,v 1.65.4.1 2008/03/25 23:38:51 cxh Exp $
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class ActorEditorGraphController extends ActorViewerGraphController {
    /** Create a new basic controller with default
     *  terminal and edge interactors.
     */
    public ActorEditorGraphController() {
        super();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add commands to the specified menu and toolbar, as appropriate
     *  for this controller.  In this class, commands are added to create
     *  ports and relations.
     *  @param menu The menu to add to, or null if none.
     *  @param toolbar The toolbar to add to, or null if none.
     */
    public void addToMenuAndToolbar(JMenu menu, JToolBar toolbar) {
        super.addToMenuAndToolbar(menu, toolbar);
        diva.gui.GUIUtilities.addMenuItem(menu, _newInputPortAction);
        diva.gui.GUIUtilities.addToolBarButton(toolbar, _newInputPortAction);
        diva.gui.GUIUtilities.addMenuItem(menu, _newOutputPortAction);
        diva.gui.GUIUtilities.addToolBarButton(toolbar, _newOutputPortAction);
        diva.gui.GUIUtilities.addMenuItem(menu, _newInoutPortAction);
        diva.gui.GUIUtilities.addToolBarButton(toolbar, _newInoutPortAction);
        diva.gui.GUIUtilities.addMenuItem(menu, _newInputMultiportAction);
        diva.gui.GUIUtilities.addToolBarButton(toolbar,
                _newInputMultiportAction);
        diva.gui.GUIUtilities.addMenuItem(menu, _newOutputMultiportAction);
        diva.gui.GUIUtilities.addToolBarButton(toolbar,
                _newOutputMultiportAction);
        diva.gui.GUIUtilities.addMenuItem(menu, _newInoutMultiportAction);
        diva.gui.GUIUtilities.addToolBarButton(toolbar,
                _newInoutMultiportAction);

        menu.addSeparator();

        // Add an item that adds new relations.
        diva.gui.GUIUtilities.addMenuItem(menu, _newRelationAction);
        diva.gui.GUIUtilities.addToolBarButton(toolbar, _newRelationAction);

        // Add hot key for create instance action.
        diva.gui.GUIUtilities
                .addHotKey(
                        getFrame().getJGraph(),
                        ((ClassDefinitionController) _classDefinitionController)._createInstanceAction);

        // Add hot key for create subclass action.
        diva.gui.GUIUtilities
                .addHotKey(
                        getFrame().getJGraph(),
                        ((ClassDefinitionController) _classDefinitionController)._createSubclassAction);
    }

    /** Set the configuration.  The configuration is used when
     *  opening documentation files.
     *  @param configuration The configuration.
     */
    public void setConfiguration(Configuration configuration) {
        super.setConfiguration(configuration);

        if (_portDialogAction != null) {
            _portDialogAction.setConfiguration(configuration);
        }

        if (_configureUnitsAction != null) {
            _configureUnitsAction.setConfiguration(configuration);
        }

        if (_listenToActorFactory != null) {
            _listenToActorFactory.setConfiguration(configuration);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the controllers for nodes in this graph.
     *  In this class, controllers with FULL access are created.
     *  This is called by the constructor, so derived classes that
     *  override this must be careful not to reference local variables
     *  defined in the derived classes, because the derived classes
     *  will not have been fully constructed by the time this is called.
     */
    protected void _createControllers() {
        _attributeController = new AttributeController(this,
                AttributeController.FULL);

        _classDefinitionController = new ClassDefinitionController(this);

        _entityController = new ActorInstanceController(this);
        _entityPortController = new IOPortController(this,
                AttributeController.FULL);
        _portController = new ExternalIOPortController(this,
                AttributeController.FULL);
        _relationController = new RelationController(this);
        _linkController = new LinkController(this);
    }

    /** Initialize all interaction on the graph pane. This method
     *  is called by the setGraphPane() method of the superclass.
     *  This initialization cannot be done in the constructor because
     *  the controller does not yet have a reference to its pane
     *  at that time.
     */
    protected void initializeInteraction() {
        super.initializeInteraction();

        GraphPane pane = getGraphPane();

        // Add a menu command to configure the ports.
        _portDialogAction = new PortDialogAction("Ports");
        _portDialogAction.setConfiguration(getConfiguration());

        _configureMenuFactory.addAction(_portDialogAction, "Customize");
        _configureUnitsAction = new ConfigureUnitsAction("Units Constraints");
        _configureMenuFactory.addAction(_configureUnitsAction, "Customize");
        _configureUnitsAction.setConfiguration(getConfiguration());

        // Add a menu command to list to the actor.
        _listenToActorFactory = new ListenToActorFactory();
        _menuFactory.addMenuItemFactory(_listenToActorFactory);
        _listenToActorFactory.setConfiguration(getConfiguration());

        // Create listeners that creates new relations.
        _relationCreator = new RelationCreator();
        _relationCreator.setMouseFilter(_shortcutFilter);

        pane.getBackgroundEventLayer().addInteractor(_relationCreator);

        // Note that shift-click is already bound to the dragSelection
        // interactor when adding things to a selection.
        // Create the interactor that drags new edges.
        _linkCreator = new LinkCreator();
        _linkCreator.setMouseFilter(_shortcutFilter);

        // NOTE: Do not use _initializeInteraction() because we are
        // still in the constructor, and that method is overloaded in
        // derived classes.
        ((CompositeInteractor) _portController.getNodeInteractor())
                .addInteractor(_linkCreator);
        ((CompositeInteractor) _entityPortController.getNodeInteractor())
                .addInteractor(_linkCreator);
        ((CompositeInteractor) _relationController.getNodeInteractor())
                .addInteractor(_linkCreator);

        LinkCreator linkCreator2 = new LinkCreator();
        linkCreator2
                .setMouseFilter(new MouseFilter(InputEvent.BUTTON1_MASK, 0));
        ((CompositeInteractor) _entityPortController.getNodeInteractor())
                .addInteractor(linkCreator2);
    }

    /** Initialize interactions for the specified controller.  This
     *  method is called when a new controller is constructed. In this
     *  class, this method attaches a link creator to the controller
     *  if the controller is an instance of ExternalIOPortController,
     *  IOPortController, or RelationController.
     *  @param controller The controller for which to initialize interaction.
     */
    protected void _initializeInteraction(NamedObjController controller) {
        super._initializeInteraction(controller);

        if (controller instanceof ExternalIOPortController
                || controller instanceof IOPortController
                || controller instanceof RelationController) {
            Interactor interactor = controller.getNodeInteractor();

            if (interactor instanceof CompositeInteractor) {
                ((CompositeInteractor) interactor).addInteractor(_linkCreator);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private ConfigureUnitsAction _configureUnitsAction;

    /** The interactors that interactively creates edges. */
    private LinkCreator _linkCreator; // For control-click

    //   private LinkCreator _linkCreator2;  // For shift-click

    /** Factory for listen to actor menu item. */
    private ListenToActorFactory _listenToActorFactory;

    /** Action for creating a new input port. */
    private Action _newInputPortAction = new NewPortAction(
            ExternalIOPortController._GENERIC_INPUT, "New input port",
            KeyEvent.VK_I, new String[][] {
                    { "/ptolemy/vergil/actor/img/single_in.gif",
                            GUIUtilities.LARGE_ICON },
                    { "/ptolemy/vergil/actor/img/single_in_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                    { "/ptolemy/vergil/actor/img/single_in_ov.gif",
                            GUIUtilities.ROLLOVER_SELECTED_ICON },
                    { "/ptolemy/vergil/actor/img/single_in_on.gif",
                            GUIUtilities.SELECTED_ICON } });

    /** Action for creating a new output port. */
    private Action _newOutputPortAction = new NewPortAction(
            ExternalIOPortController._GENERIC_OUTPUT, "New output port",
            KeyEvent.VK_O, new String[][] {
                    { "/ptolemy/vergil/actor/img/single_out.gif",
                            GUIUtilities.LARGE_ICON },
                    { "/ptolemy/vergil/actor/img/single_out_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                    { "/ptolemy/vergil/actor/img/single_out_ov.gif",
                            GUIUtilities.ROLLOVER_SELECTED_ICON },
                    { "/ptolemy/vergil/actor/img/single_out_on.gif",
                            GUIUtilities.SELECTED_ICON } });

    /** Action for creating a new input/output port. */
    private Action _newInoutPortAction = new NewPortAction(
            ExternalIOPortController._GENERIC_INOUT, "New input/output port",
            KeyEvent.VK_P, new String[][] {
                    { "/ptolemy/vergil/actor/img/single_inout.gif",
                            GUIUtilities.LARGE_ICON },
                    { "/ptolemy/vergil/actor/img/single_inout_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                    { "/ptolemy/vergil/actor/img/single_inout_ov.gif",
                            GUIUtilities.ROLLOVER_SELECTED_ICON },
                    { "/ptolemy/vergil/actor/img/single_inout_on.gif",
                            GUIUtilities.SELECTED_ICON } });

    /** Action for creating a new input multiport. */
    private Action _newInputMultiportAction = new NewPortAction(
            ExternalIOPortController._GENERIC_INPUT_MULTIPORT,
            "New input multiport", KeyEvent.VK_N, new String[][] {
                    { "/ptolemy/vergil/actor/img/multi_in.gif",
                            GUIUtilities.LARGE_ICON },
                    { "/ptolemy/vergil/actor/img/multi_in_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                    { "/ptolemy/vergil/actor/img/multi_in_ov.gif",
                            GUIUtilities.ROLLOVER_SELECTED_ICON },
                    { "/ptolemy/vergil/actor/img/multi_in_on.gif",
                            GUIUtilities.SELECTED_ICON } });

    /** Action for creating a new output multiport. */
    private Action _newOutputMultiportAction = new NewPortAction(
            ExternalIOPortController._GENERIC_OUTPUT_MULTIPORT,
            "New output multiport", KeyEvent.VK_U, new String[][] {
                    { "/ptolemy/vergil/actor/img/multi_out.gif",
                            GUIUtilities.LARGE_ICON },
                    { "/ptolemy/vergil/actor/img/multi_out_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                    { "/ptolemy/vergil/actor/img/multi_out_ov.gif",
                            GUIUtilities.ROLLOVER_SELECTED_ICON },
                    { "/ptolemy/vergil/actor/img/multi_out_on.gif",
                            GUIUtilities.SELECTED_ICON } });

    /** Action for creating a new inout multiport. */
    private Action _newInoutMultiportAction = new NewPortAction(
            ExternalIOPortController._GENERIC_INOUT_MULTIPORT,
            "New input/output multiport", KeyEvent.VK_T, new String[][] {
                    { "/ptolemy/vergil/actor/img/multi_inout.gif",
                            GUIUtilities.LARGE_ICON },
                    { "/ptolemy/vergil/actor/img/multi_inout_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                    { "/ptolemy/vergil/actor/img/multi_inout_ov.gif",
                            GUIUtilities.ROLLOVER_SELECTED_ICON },
                    { "/ptolemy/vergil/actor/img/multi_inout_on.gif",
                            GUIUtilities.SELECTED_ICON } });

    /** Action for creating a new relation. */
    protected Action _newRelationAction = new NewRelationAction(
            new String[][] {
                    { "/ptolemy/vergil/actor/img/relation.gif",
                            GUIUtilities.LARGE_ICON },
                    { "/ptolemy/vergil/actor/img/relation_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                    { "/ptolemy/vergil/actor/img/relation_ov.gif",
                            GUIUtilities.ROLLOVER_SELECTED_ICON },
                    { "/ptolemy/vergil/actor/img/relation_on.gif",
                            GUIUtilities.SELECTED_ICON } });

    /** The port dialog factory. */
    private PortDialogAction _portDialogAction;

    /** The interactor for creating new relations. */
    private RelationCreator _relationCreator;

    /** The filter for shortcut operations.  This is used for creation
     *  of relations and creation of links from relations. Under PC,
     *  this is a control-1 click.  Under Mac OS X, the control key is
     *  used for context menus and this corresponds to the command-1
     *  click.  For details, see the Apple java archive
     *  http://lists.apple.com/archives/java-dev User: archives,
     *  passwd: archives
     */
    private MouseFilter _shortcutFilter = new MouseFilter(
            InputEvent.BUTTON1_MASK, Toolkit.getDefaultToolkit()
                    .getMenuShortcutKeyMask(), Toolkit.getDefaultToolkit()
                    .getMenuShortcutKeyMask());

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    ///////////////////////////////////////////////////////////////////
    //// LinkCreator

    /** This class is an interactor that interactively drags edges from
     *  one terminal to another, creating a link to connect them.
     */
    protected class LinkCreator extends AbstractInteractor {
        /** Create a new edge when the mouse is pressed. */
        public void mousePressed(LayerEvent event) {
            Figure source = event.getFigureSource();
            NamedObj sourceObject = (NamedObj) source.getUserObject();

            // Create the new edge.
            Link link = new Link();

            // Set the tail, going through the model so the link is added
            // to the list of links.
            ActorGraphModel model = (ActorGraphModel) getGraphModel();
            model.getLinkModel().setTail(link, sourceObject);

            try {
                // add it to the foreground layer.
                FigureLayer layer = getGraphPane().getForegroundLayer();
                Site headSite;
                Site tailSite;

                // Temporary sites.  One of these will get blown away later.
                headSite = new AutonomousSite(layer, event.getLayerX(), event
                        .getLayerY());
                tailSite = new AutonomousSite(layer, event.getLayerX(), event
                        .getLayerY());

                // Render the edge.
                Connector c = getEdgeController(link).render(link, layer,
                        tailSite, headSite);

                // get the actual attach site.
                tailSite = getEdgeController(link).getConnectorTarget()
                        .getTailSite(c, source, event.getLayerX(),
                                event.getLayerY());

                if (tailSite == null) {
                    throw new RuntimeException("Invalid connector target: "
                            + "no valid site found for tail of new connector.");
                }

                // And reattach the connector.
                c.setTailSite(tailSite);

                // Add it to the selection so it gets a manipulator, and
                // make events go to the grab-handle under the mouse
                getSelectionModel().addSelection(c);

                ConnectorManipulator cm = (ConnectorManipulator) c.getParent();
                GrabHandle gh = cm.getHeadHandle();
                layer.grabPointer(event, gh);
            } catch (Exception ex) {
                MessageHandler.error("Drag connection failed:", ex);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// ListenToActorFactory
    private class ListenToActorFactory implements MenuItemFactory {
        /** Add an item to the given context menu that will open a listen
         *  to actor window.
         *  @param menu The context menu.
         *  @param object The object whose ports are being manipulated.
         */
        public JMenuItem create(final JContextMenu menu, NamedObj object) {
            String name = "Listen to Actor";
            final NamedObj target = object;

            _action = new ActorController.ListenToActorAction(target,
                    ActorEditorGraphController.this);
            _action.setConfiguration(_configuration);
            return menu.add(_action, name);
        }

        /** Set the configuration for use by the help screen.
         *  @param configuration The configuration.
         */
        public void setConfiguration(Configuration configuration) {
            _configuration = configuration;

            if (_action != null) {
                _action.setConfiguration(_configuration);
            }
        }

        private ActorController.ListenToActorAction _action;

        private Configuration _configuration;
    }

    ///////////////////////////////////////////////////////////////////
    //// NewRelationAction
    /** An action to create a new relation. */
    public class NewRelationAction extends FigureAction {
        /** Create an action that creates a new relation.
         */
        public NewRelationAction() {
            this(null);
        }

        /** Create an action that creates a new relation.
         *  @param iconRoles A matrix of Strings, where each element
         *  consists of two Strings, the absolute URL of the icon
         *  and the key that represents the role of the icon.  The keys
         *  are usually static fields from this class, such as
         *  {@link diva.gui.GUIUtilities#LARGE_ICON},
         *  {@link diva.gui.GUIUtilities#ROLLOVER_ICON},
         *  {@link diva.gui.GUIUtilities#ROLLOVER_SELECTED_ICON} or
         *  {@link diva.gui.GUIUtilities#SELECTED_ICON}.
         *  If this parameter is null, then the icon comes from
         *  the calling getNodeRenderer() on the {@link #_portController}.
         *  @see diva.gui.GUIUtilities#addIcons(Action, String[][])
         */
        public NewRelationAction(String[][] iconRoles) {
            super("New Relation");

            if (iconRoles != null) {
                GUIUtilities.addIcons(this, iconRoles);
            } else {
                // Standard toolbar icons are 25x25 pixels.
                NodeRenderer renderer = _relationController.getNodeRenderer();
                Figure figure = renderer.render(null);

                FigureIcon icon = new FigureIcon(figure, 25, 25, 1, true);
                putValue(diva.gui.GUIUtilities.LARGE_ICON, icon);
            }
            putValue("tooltip", "Control-click to create a new relation");
            putValue(diva.gui.GUIUtilities.MNEMONIC_KEY, Integer
                    .valueOf(KeyEvent.VK_R));
        }

        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            double x;
            double y;

            if ((getSourceType() == TOOLBAR_TYPE)
                    || (getSourceType() == MENUBAR_TYPE)) {
                // No location in the action, so put it in the middle.
                BasicGraphFrame frame = ActorEditorGraphController.this
                        .getFrame();
                Point2D center;

                if (frame != null) {
                    // Put in the middle of the visible part.
                    center = frame.getCenter();
                    x = center.getX();
                    y = center.getY();
                } else {
                    // Put in the middle of the pane.
                    GraphPane pane = getGraphPane();
                    center = pane.getSize();
                    x = center.getX() / 2;
                    y = center.getY() / 2;
                }
            } else {
                // Transform
                AffineTransform current = getGraphPane().getTransformContext()
                        .getTransform();
                AffineTransform inverse;

                try {
                    inverse = current.createInverse();
                } catch (NoninvertibleTransformException ex) {
                    throw new RuntimeException(ex.toString());
                }

                Point2D point = new Point2D.Double(getX(), getY());

                inverse.transform(point, point);
                x = point.getX();
                y = point.getY();
            }

            ActorGraphModel graphModel = (ActorGraphModel) getGraphModel();
            double[] point = SnapConstraint.constrainPoint(x, y);
            final NamedObj toplevel = graphModel.getPtolemyModel();

            if (!(toplevel instanceof CompositeEntity)) {
                throw new InternalErrorException(
                        "Cannot invoke NewRelationAction on an object "
                                + "that is not a CompositeEntity.");
            }

            final String relationName = toplevel.uniqueName("relation");
            final String vertexName = "vertex1";

            // Create the relation.
            StringBuffer moml = new StringBuffer();
            moml.append("<relation name=\"" + relationName + "\">\n");
            moml.append("<vertex name=\"" + vertexName + "\" value=\"{");
            moml.append(point[0] + ", " + point[1]);
            moml.append("}\"/>\n");
            moml.append("</relation>");

            MoMLChangeRequest request = new MoMLChangeRequest(this, toplevel,
                    moml.toString());
            request.setUndoable(true);
            toplevel.requestChange(request);
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// RelationCreator

    /** An interactor for creating relations upon control clicking.
     */
    protected class RelationCreator extends ActionInteractor {
        public RelationCreator() {
            super();
            setAction(_newRelationAction);
        }
    }
}
