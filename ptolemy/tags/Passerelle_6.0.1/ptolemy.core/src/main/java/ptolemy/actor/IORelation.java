/* Relation supporting message passing.

 Copyright (c) 1997-2007 The Regents of the University of California.
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

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// IORelation

/**
 This class mediates connections between ports that can send data to
 one another via message passing. One purpose of this relation is to
 ensure that IOPorts are only connected to IOPorts. A second purpose
 is to support the notion of a <i>width</i> to represent something
 like a bus. By default an IORelation is not a bus, which means that
 its width is one. Calling setWidth() with
 an argument larger than one makes the relation a bus of fixed width.
 Calling setWidth() with an argument of zero makes the relation
 a bus with indeterminate width, in which case the width will be
 inferred (if possible) from the context.  In particular,
 if this relation is linked on the inside to a port with some
 width, then the width of this relation will be inferred to
 be the enough so that the widths of all inside linked relations
 adds up to the outside width of the port.
 The actual width of an IORelation
 can never be less than one. If this IORelation is linked to another
 instance of IORelation, then the width of the two IORelations is
 constrained to be the same.
 <p>
 Instances of IORelation can only be linked to instances of IOPort
 or instances of IORelation.
 Derived classes may further constrain this to subclasses of IOPort
 of IORelation.
 Such derived classes should override the protected methods _checkPort()
 and _checkRelation() to throw an exception.
 <p>
 To link a IOPort to an IORelation, use the link() or
 liberalLink() method in the IOPort class.  To remove a link,
 use the unlink() method. To link (unlink) an IORelation to an IORelation,
 use the link() (unlink()) method of IORelation.
 <p>
 The container for instances of this class can only be instances of
 CompositeActor.  Derived classes may wish to further constrain the
 container to subclasses of ComponentEntity.  To do this, they should
 override the _checkContainer() method.

 @author Edward A. Lee, Jie Liu
 @version $Id: IORelation.java,v 1.101 2007/12/07 06:26:44 cxh Exp $
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (acataldo)
 */
public class IORelation extends ComponentRelation {
    /** Construct a relation in the default workspace with an empty string
     *  as its name. Add the relation to the directory of the workspace.
     */
    public IORelation() {
        super();
        _init();
    }

    /** Construct a relation in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  Add the relation to the workspace directory.
     *
     *  @param workspace The workspace that will list the relation.
     */
    public IORelation(Workspace workspace) {
        super(workspace);
        _init();
    }

    /** Construct a relation with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This relation will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  This constructor write-synchronizes on the workspace.
     *
     *  @param container The container.
     *  @param name The name of the relation.
     *  @exception IllegalActionException If the container is incompatible
     *   with this relation.
     *  @exception NameDuplicationException If the name coincides with
     *   a relation already in the container.
     */
    public IORelation(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The width of this relation. This is an integer that defaults
     *  to one. Set it to zero to infer the width from that of ports
     *  to which this relation is linked on the inside.
     */
    public Parameter width;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute.  This method is called by
     *  a contained attribute when its value changes.  This overrides
     *  the base class so that if the attribute is an instance of
     *  Parameter and the name is "width", then the width of the Relation
     *  is set.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute instanceof Parameter
                && "width".equals(attribute.getName())) {
            IntToken t = (IntToken) ((Parameter) attribute).getToken();

            if (t != null) {
                int width = t.intValue();
                _setWidth(width);
            }
        }
    }

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new relation with no links and no container, but with
     *  the same width as the original.
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one or more of the attributes
     *   cannot be cloned.
     *  @return A new ComponentRelation.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        IORelation newObject = (IORelation) super.clone(workspace);
        newObject._inferredWidthVersion = -1;
        return newObject;
    }

    /** Return the receivers of all input ports linked to this
     *  relation, directly or indirectly through a relation group,
     *  except those in the port
     *  given as an argument. The returned value is an array of
     *  arrays. The first index (the row) specifies the group, where
     *  a group receives the same data from a channel.
     *  Each channel normally receives distinct data. The
     *  second index (the column) specifies the receiver number within
     *  the group of receivers that get copies from the same channel.
     *  <p>
     *  The number of groups (rows) is less than or equal to the
     *  width of the relation, which is always at least one. If
     *  there are no receivers then return null.
     *  <p>
     *  For each channel, there may be any number of receivers in the group.
     *  The individual receivers are selected using the second index of the
     *  returned array of arrays.  If there are no receivers in the group,
     *  then the channel is represented by null.  I.e., if the returned
     *  array of arrays is <i>x</i> and the channel number is <i>c</i>,
     *  then <i>x</i>[<i>c</i>] is null.  Otherwise, it is an array, where
     *  the size of the array is the number of receivers in the group.
     *  <p>
     *  NOTE: This method may have the effect of creating new receivers in the
     *  remote input ports and losing the previous receivers in those ports,
     *  together with any data they may contain.  This occurs only if the
     *  topology has changed since the receivers were created, and that change
     *  resulting in one of those ports not having the right number of
     *  receivers.
     *  <p>
     *  This method read-synchronizes on the workspace.
     *
     *  @see IOPort#getRemoteReceivers
     *  @param except The port to exclude, or null to not
     *   exclude any ports.
     *  @return The receivers associated with this relation.
     */
    public Receiver[][] deepReceivers(IOPort except) {
        try {
            _workspace.getReadAccess();

            Receiver[][] result = new Receiver[0][0];
            Iterator inputs = linkedDestinationPortList(except).iterator();
            Receiver[][] receivers; //= new Receiver[0][0];

            // NOTE: We have to be careful here to keep track of
            // multiple occurrences of a port in this list.
            // EAL 7/30/00.
            HashMap seen = new HashMap();

            while (inputs.hasNext()) {
                IOPort p = (IOPort) inputs.next();

                if (p.isInsideGroupLinked(this) && !p.isOpaque()) {
                    // if p is a transparent port and this relation links
                    // from the inside, then get the Receivers outside p.
                    try {
                        receivers = p.getRemoteReceivers(this);
                    } catch (IllegalActionException ex) {
                        throw new InternalErrorException(this, ex, null);
                    }
                } else {
                    // if p not a transparent port, or this relation is linked
                    // to p from the outside.
                    try {
                        // Note that this may be an inside or outside linked
                        // relation.
                        // NOTE: We have to be careful here to keep track of
                        // multiple occurrences of a port in this list.
                        // EAL 7/30/00.
                        int occurrence = 0;

                        if (seen.containsKey(p)) {
                            occurrence = ((Integer) (seen.get(p))).intValue();
                            occurrence++;
                        }

                        seen.put(p, Integer.valueOf(occurrence));

                        receivers = p._getReceiversLinkedToGroup(this,
                                occurrence);
                    } catch (IllegalActionException ex) {
                        throw new InternalErrorException(this, ex, null);
                    }
                }

                result = _cascade(result, receivers);
            }

            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return the width of the IORelation, which is always at least one.
     *  If the width has been set to zero, then the relation is a bus with
     *  unspecified width, and the width needs to be inferred from the
     *  way the relation is connected.  This is done by checking the
     *  ports that this relation is linked to from the inside and setting
     *  the width to the maximum of those port widths, minus the widths of
     *  other relations linked to those ports on the inside. Each such port is
     *  allowed to have at most one inside relation with an unspecified
     *  width, or an exception is thrown.  If this inference yields a width
     *  of zero, then return one.
     *
     *  @return The width, which is at least one.
     *  @see #setWidth(int)
     */
    public int getWidth() {
        if (_width == 0) {
            return _inferWidth();
        }

        return _width;
    }

    /** Return true if the relation has a definite width (i.e.,
     *  setWidth() has not been called with a zero argument).
     *  @return True if the width has been set to non-zero.
     */
    public boolean isWidthFixed() {
        return (_width != 0);
    }

    /** List the input ports that this relation connects to from the
     *  outside, and the output ports that it connects to from
     *  the inside. I.e., list the ports through or to which we
     *  could send data. Two ports are connected if they are
     *  linked to relations in the same relation group.
     *  This method read-synchronizes on the workspace.
     *  @see ptolemy.kernel.Relation#linkedPorts
     *  @return An enumeration of IOPort objects.
     */
    public List linkedDestinationPortList() {
        return linkedDestinationPortList(null);
    }

    /** List the input ports that this relation connects to from the
     *  outside and the output ports that it connects to from
     *  the inside, except the port given as an argument.
     *  I.e., list the ports through or to which we
     *  could send data. Two ports are connected if they are
     *  linked to relations in the same relation group.
     *  This method read-synchronizes on the workspace.
     *  @see ptolemy.kernel.Relation#linkedPortList(ptolemy.kernel.Port)
     *  @param except The port not included in the returned list, or
     *   null to not exclude any ports.
     *  @return A list of IOPort objects.
     */
    public List linkedDestinationPortList(IOPort except) {
        try {
            _workspace.getReadAccess();

            // NOTE: The result could be cached for efficiency, but
            // it would have to be cached in a hashtable indexed by the
            // except argument.  Probably not worth it.
            LinkedList resultPorts = new LinkedList();
            Iterator ports = linkedPortList().iterator();

            while (ports.hasNext()) {
                IOPort p = (IOPort) ports.next();

                if (p != except) {
                    if (p.isInsideGroupLinked(this)) {
                        // Linked from the inside
                        if (p.isOutput()) {
                            resultPorts.addLast(p);
                        }
                    } else {
                        if (p.isInput()) {
                            resultPorts.addLast(p);
                        }
                    }
                }
            }

            return resultPorts;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Enumerate the input ports that we are linked to from the outside,
     *  and the output ports that we are linked to from the inside.
     *  I.e., enumerate the ports through or to which we could send data.
     *  This method is deprecated and calls linkedDestinationPortList().
     *  This method read-synchronizes on the workspace.
     *  @see ptolemy.kernel.Relation#linkedPorts
     *  @deprecated Use linkedDestinationPortList() instead.
     *  @return An enumeration of IOPort objects.
     */
    public Enumeration linkedDestinationPorts() {
        return linkedDestinationPorts(null);
    }

    /** Enumerate the input ports that we are linked to from the
     *  outside, and the output ports that we are linked to from
     *  the inside, except the port given as an argument.
     *  I.e., enumerate the ports through or to which we could send data.
     *  This method is deprecated and calls
     *  linkedDestinationPortList(IOPort).
     *  This method read-synchronizes on the workspace.
     *  @see ptolemy.kernel.Relation#linkedPorts(ptolemy.kernel.Port)
     *  @param except The port not included in the returned Enumeration.
     *  @deprecated Use linkDestinationPortList(IOPort) instead.
     *  @return An enumeration of IOPort objects.
     */
    public Enumeration linkedDestinationPorts(IOPort except) {
        return Collections.enumeration(linkedDestinationPortList(except));
    }

    /** List the output ports that this relation connects to from the
     *  outside and the input ports that it connects to from
     *  the inside.
     *  I.e., list the ports through or from which we
     *  could receive data. Two ports are connected if they are
     *  linked to relations in the same relation group.
     *  This method read-synchronizes on the workspace.
     *  @see ptolemy.kernel.Relation#linkedPorts
     *  @return An enumeration of IOPort objects.
     */
    public List linkedSourcePortList() {
        return linkedSourcePortList(null);
    }

    /** List the output ports that this relation connects to from the
     *  outside and the input ports that it connects to from
     *  the inside, except the port given as an argument.
     *  I.e., list the ports through or from which we
     *  could receive data. Two ports are connected if they are
     *  linked to relations in the same relation group.
     *  This method read-synchronizes on the workspace.
     *  @see ptolemy.kernel.Relation#linkedPortList(ptolemy.kernel.Port)
     *  @param except The port not included in the returned list.
     *  @return A list of IOPort objects.
     */
    public List linkedSourcePortList(IOPort except) {
        try {
            _workspace.getReadAccess();

            // NOTE: The result could be cached for efficiency, but
            // it would have to be cached in a hashtable indexed by the
            // except argument.  Probably not worth it.
            LinkedList resultPorts = new LinkedList();
            Iterator ports = linkedPortList().iterator();

            while (ports.hasNext()) {
                IOPort p = (IOPort) ports.next();

                if (p != except) {
                    if (p.isInsideGroupLinked(this)) {
                        // Linked from the inside
                        if (p.isInput()) {
                            resultPorts.addLast(p);
                        }
                    } else {
                        if (p.isOutput()) {
                            resultPorts.addLast(p);
                        }
                    }
                }
            }

            return resultPorts;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Enumerate the output ports that we are linked to from the outside
     *  and the input ports that we are linked to from the inside.
     *  I.e. enumerate the ports from or through which we might receive
     *  data. This method is deprecated and calls
     *  linkedSourcePortList().
     *  This method read-synchronizes on the workspace.
     *  @see ptolemy.kernel.Relation#linkedPorts
     *  @deprecated Use linkedSourcePortList() instead.
     *  @return An enumeration of IOPort objects.
     */
    public Enumeration linkedSourcePorts() {
        return Collections.enumeration(linkedSourcePortList());
    }

    /** Enumerate the output ports that we are linked to from the outside
     *  and the input ports that we are linked to from the inside.
     *  I.e. enumerate the ports from or through which we might receive
     *  This method is deprecated and calls
     *  linkedSourcePortList(IOPort).
     *  This method read-synchronizes on the workspace.
     *  @see ptolemy.kernel.Relation#linkedPorts(ptolemy.kernel.Port)
     *  @param except The port not included in the returned Enumeration.
     *  @deprecated Use linkedSourcePortList(IOPort) instead.
     *  @return An enumeration of IOPort objects.
     */
    public Enumeration linkedSourcePorts(IOPort except) {
        return Collections.enumeration(linkedSourcePortList(except));
    }

    /** Specify the container, adding the relation to the list
     *  of relations in the container.
     *  If this relation already has a container, remove it
     *  from that container first.  Otherwise, remove it from
     *  the list of objects in the workspace. If the argument is null, then
     *  unlink the ports from the relation, remove it from
     *  its container, and add it to the list of objects in the workspace.
     *  If the relation is already contained by the container, do nothing.
     *  <p>
     *  The container must be an
     *  instance of CompositeActor or null, otherwise an exception is thrown.
     *  Derived classes may further constrain the class of the container
     *  to a subclass of CompositeActor.
     *  <p>
     *  This method invalidates the schedule and resolved types of the
     *  director of the container, if there is one.
     *  <p>
     *  This method is write-synchronized on the workspace.
     *
     *  @param container The proposed container.
     *  @exception IllegalActionException If the container is not a
     *   CompositeActor or null, or this entity and the container are not in
     *   the same workspace.
     *  @exception NameDuplicationException If the name collides with a name
     *   already on the relations list of the container.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        if (!(container instanceof CompositeActor) && (container != null)) {
            throw new IllegalActionException(this, container,
                    "IORelation can only be contained by CompositeActor.");
        }

        // Invalidate schedule and type resolution of the old container.
        Nameable oldContainer = getContainer();

        if (oldContainer instanceof CompositeActor) {
            Director director = ((CompositeActor) oldContainer).getDirector();

            if (director != null) {
                director.invalidateSchedule();
                director.invalidateResolvedTypes();
            }
        }

        // Invalidate schedule and type resolution of the new container.
        if (container instanceof CompositeActor) {
            Director director = ((CompositeActor) container).getDirector();

            if (director != null) {
                director.invalidateSchedule();
                director.invalidateResolvedTypes();
            }
        }

        super.setContainer(container);
    }

    /** Set the width of this relation and all relations in its
     *  relation group. The width is the number of
     *  channels that the relation represents.  If the argument
     *  is zero or less, then the relation becomes a bus with unspecified width,
     *  and the width will be inferred from the way the relation is used
     *  (but will never be less than one).
     *  This method invalidates
     *  the resolved types on the director of the container, if there is
     *  one, and notifies each connected actor that its connections
     *  have changed.
     *  This method write-synchronizes on the workspace.
     *
     *  @param widthValue The width of the relation.
     *  @exception IllegalActionException If the argument is greater than
     *   one and the relation is linked to a non-multiport, or it is zero and
     *   the relation is linked on the inside to a port that is already
     *   linked on the inside to a relation with unspecified width.
     *  @see ptolemy.kernel.util.Workspace#getWriteAccess()
     *  @see #getWidth()
     */
    public void setWidth(int widthValue) throws IllegalActionException {
        width.setToken(new IntToken(widthValue));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Indicate that the description(int) method should describe the width
     *  of the relation, and whether it has been fixed.
     */
    public static final int CONFIGURATION = 512;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Throw an exception if the specified port cannot be linked to this
     *  relation (is not of class IOPort).
     *  @param port The candidate port to link to.
     *  @exception IllegalActionException If the port is not an IOPort.
     */
    protected void _checkPort(Port port) throws IllegalActionException {
        if (!(port instanceof IOPort)) {
            throw new IllegalActionException(this, port,
                    "IORelation can only link to a IOPort.");
        }
    }

    /** Throw an exception if the specified relation is not an instance
     *  of IORelation or if it does not have the same width as this relation.
     *  @param relation The relation to link to.
     *  @param symmetric If true, the call _checkRelation() on the specified
     *   relation with this as an argument.
     *  @exception IllegalActionException If this relation has no container,
     *   or if this relation is not an acceptable relation for the specified
     *   relation, or if this relation and the specified relation do not
     *   have the same width.
     */
    protected void _checkRelation(Relation relation, boolean symmetric)
            throws IllegalActionException {
        if (!(relation instanceof IORelation)) {
            throw new IllegalActionException(this, relation,
                    "IORelation can only link to an IORelation.");
        }

        if (((IORelation) relation)._width != _width) {
            throw new IllegalActionException(this, relation,
                    "Relations have different widths: " + _width + " != "
                            + ((IORelation) relation)._width);
        }

        super._checkRelation(relation, symmetric);
    }

    /** Return a description of the object.  The level of detail depends
     *  on the argument, which is an or-ing of the static final constants
     *  defined in the NamedObj class and in this class.
     *  Lines are indented according to
     *  to the level argument using the protected method _getIndentPrefix().
     *  Zero, one or two brackets can be specified to surround the returned
     *  description.  If one is specified it is the the leading bracket.
     *  This is used by derived classes that will append to the description.
     *  Those derived classes are responsible for the closing bracket.
     *  An argument other than 0, 1, or 2 is taken to be equivalent to 0.
     *  <p>
     *  If the detail argument sets the bit defined by the constant
     *  CONFIGURATION, then append to the description is a field
     *  of the form "configuration {width <i>integer</i> ?fixed?}", where the
     *  word "fixed" is present if the relation has fixed width, and is
     *  absent if the relation is a bus with inferred width (isWidthFixed()
     *  returns false).
     *
     *  This method is read-synchronized on the workspace.
     *
     *  @param detail The level of detail.
     *  @param indent The amount of indenting.
     *  @param bracket The number of surrounding brackets (0, 1, or 2).
     *  @return A description of the object.
     */
    protected String _description(int detail, int indent, int bracket) {
        try {
            _workspace.getReadAccess();

            String result;

            if ((bracket == 1) || (bracket == 2)) {
                result = super._description(detail, indent, 1);
            } else {
                result = super._description(detail, indent, 0);
            }

            if ((detail & CONFIGURATION) != 0) {
                if (result.trim().length() > 0) {
                    result += " ";
                }

                result += "configuration {";
                result += ("width " + getWidth());

                if (isWidthFixed()) {
                    result += " fixed";
                }

                result += "}";
            }

            if (bracket == 2) {
                result += "}";
            }

            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Cascade two Receiver arrays to form a new array. For each row, each
     *  element of the second array is appended behind the elements of the
     *  first array. This method is solely for deepReceivers.
     *  The two input arrays must have the same number of rows.
     */
    private Receiver[][] _cascade(Receiver[][] array1, Receiver[][] array2)
            throws InvalidStateException {
        if ((array1 == null) || (array1.length <= 0)) {
            return array2;
        }

        if ((array2 == null) || (array2.length <= 0)) {
            return array1;
        }

        int width = getWidth();
        Receiver[][] result = new Receiver[width][0];

        for (int i = 0; i < width; i++) {
            if (array1[i] == null) {
                result[i] = array2[i];
            } else if (array1[i].length <= 0) {
                result[i] = array2[i];
            } else if (array2[i] == null) {
                result[i] = array1[i];
            } else if (array2[i].length <= 0) {
                result[i] = array1[i];
            } else {
                int m1 = array1[i].length;
                int m2 = array2[i].length;
                result[i] = new Receiver[m1 + m2];

                for (int j = 0; j < m1; j++) {
                    result[i][j] = array1[i][j];
                }

                for (int j = m1; j < (m1 + m2); j++) {
                    result[i][j] = array2[i][j - m1];
                }
            }
        }

        return result;
    }

    /** Infer the width of the port from how it is connected.
     *  Throw a runtime exception if this cannot be done (normally,
     *  the methods that construct a topology ensure that it can be
     *  be done).  The returned value is always at least one.
     *  This method is not read-synchronized on the workspace, so the caller
     *  should be.
     *  @return The inferred width.
     */
    private int _inferWidth() {
        long version = _workspace.getVersion();

        if (version != _inferredWidthVersion) {
            _inferredWidth = 1;

            Iterator ports = linkedPortList().iterator();

            // Note that depending on the order of the ports get iterated,
            // the inferred width may be different if different ports have
            // different widths. This is nondeterministic.
            // However, the model behavior is not affected by this because
            // the relation with the smallest width along a path decides
            // the number of signals that can be passed through.
            while (ports.hasNext()) {
                IOPort p = (IOPort) ports.next();

                // Infer the width of this port from the linked connections.
                // Note we assume that this method is only called upon a
                // multiport.
                // To guarantee this method successfully infer widths, we have
                // to check and ensure that there is at most one input relation
                // or one output relation whose width is not fixed (unknown).
                // This requirement is conservative. For example, an output
                // port may have two buses with their widths not fixed.
                // Furthermore, if one of the buses is connected to an input
                // port and its width can be determined from the internal
                // connections associated with that input port, the width of
                // the other bus can also be resolved. However, to support this,
                // a fixed-point iteration has to be performed, but there is
                // no guarantee of existence of a useful fixed-point whose
                // widths are all non-zero. Therefore, we take the conservative
                // approach.
                // To infer the unknown width, we resolve the equation where
                // the sum of the widths of input relations equals the sum of
                // those of output relations.
                int portInsideWidth = 0;
                int portOutsideWidth = 0;
                int difference = 0;

                if (p.isInsideGroupLinked(this)) {
                    // I am linked on the inside...
                    portInsideWidth = p._getInsideWidth(this);
                    portOutsideWidth = p._getOutsideWidth(null);

                    // the same as portOutsideWidth = p.getWidth();
                    difference = portOutsideWidth - portInsideWidth;
                } else if (p.isLinked(this)) {
                    // I am linked on the outside...
                    portInsideWidth = p._getInsideWidth(null);
                    portOutsideWidth = p._getOutsideWidth(this);
                    difference = portInsideWidth - portOutsideWidth;
                }

                if (difference > _inferredWidth) {
                    _inferredWidth = difference;
                }
            }

            _inferredWidthVersion = version;
        }

        return _inferredWidth;
    }

    /** Create an initialize the width parameter. */
    private void _init() {
        try {
            width = new Parameter(this, "width");
            width.setExpression("1");
            width.setTypeEquals(BaseType.INT);
        } catch (KernelException ex) {
            throw new InternalErrorException(ex);
        }
    }

    /** Set the width of this relation and all relations in its
     *  relation group. The width is the number of
     *  channels that the relation represents.  If the argument
     *  is zero or less, then the relation becomes a bus with unspecified width,
     *  and the width will be inferred from the way the relation is used
     *  (but will never be less than one).
     *  This method invalidates
     *  the resolved types on the director of the container, if there is
     *  one, and notifies each connected actor that its connections
     *  have changed.
     *  This method write-synchronizes on the workspace.
     *
     *  @param width The width of the relation.
     *  @exception IllegalActionException If the argument is greater than
     *   one and the relation is linked to a non-multiport, or it is zero and
     *   the relation is linked on the inside to a port that is already
     *   linked on the inside to a relation with unspecified width.
     *  @see ptolemy.kernel.util.Workspace#getWriteAccess()
     *  @see #getWidth()
     */
    private void _setWidth(int width) throws IllegalActionException {
        if (width == _width) {
            // No change.
            return;
        }
        try {
            _workspace.getWriteAccess();

            if (width <= 0) {
                // Check legitimacy of the change.
                try {
                    _inferWidth();
                } catch (InvalidStateException ex) {
                    throw new IllegalActionException(this,
                            "Cannot use unspecified width on this relation "
                                    + "because of its links.");
                }
            }

            // Check for non-multiports on a link.
            /* This is now allowed.
             if (width != 1) {
             Iterator ports = linkedPortList().iterator();

             while (ports.hasNext()) {
             IOPort p = (IOPort) ports.next();

             // Check for non-multiports.
             if (!p.isMultiport()) {
             throw new IllegalActionException(this, p,
             "Cannot make bus because the "
             + "relation is linked to a non-multiport.");
             }
             }
             }
             */
            _width = width;

            // Set the width of all relations in the relation group.
            Iterator relations = relationGroupList().iterator();

            while (!_suppressWidthPropagation && relations.hasNext()) {
                IORelation relation = (IORelation) relations.next();

                if (relation == this) {
                    continue;
                }

                // If the relation has a width parameter, set that
                // value. Otherwise, just set its width directly.
                // Have to disable back propagation.
                try {
                    relation._suppressWidthPropagation = true;
                    relation.width.setToken(new IntToken(width));
                } finally {
                    relation._suppressWidthPropagation = false;
                }
            }

            // Do this as a second pass so that it does not
            // get executed if the change is aborted
            // above by an exception.
            // FIXME: Haven't completely dealt with this
            // possibility since the above changes may have
            // partially completed.
            Iterator ports = linkedPortList().iterator();

            while (ports.hasNext()) {
                IOPort p = (IOPort) ports.next();
                Entity portContainer = (Entity) p.getContainer();

                if (portContainer != null) {
                    portContainer.connectionsChanged(p);
                }
            }

            // Invalidate schedule and type resolution.
            Nameable container = getContainer();

            if (container instanceof CompositeActor) {
                Director director = ((CompositeActor) container).getDirector();

                if (director != null) {
                    director.invalidateSchedule();
                    director.invalidateResolvedTypes();
                }
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // cached inferred width.
    private transient int _inferredWidth;

    private transient long _inferredWidthVersion = -1;

    // Suppress propagation of width changes.
    private boolean _suppressWidthPropagation = false;

    // The cached value of the width parameter.
    private int _width = 1;
}
