/* Copyright 2010 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.isencia.passerelle.actor.net;




import com.isencia.message.ChannelException;
import com.isencia.message.IReceiverChannel;
import com.isencia.message.net.MulticastReceiverChannel;
import com.isencia.passerelle.actor.ChannelSource;

import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// MulticastReceiver
/**
 * This actor sends its input as a Datagram over the network 
 * using the UDP-multicast protocol.
 *
 * The group and port number towards which the datagram is sent are
 * given by optional inputs <i>group</i> and <i>port</i>.
 *
 */

/** 
 * Construct a MulticastReceiver actor with given name in the given
 * container.  Set up ports, parameters and default values.
 * 
 * @param container The parent container for this actor.
 * @param name The name for this actor.
 * 
 * @exception NameDuplicationException If the container already has an
 *  actor with this name.
 * @exception IllegalActionException If the actor cannot be contained by
 *  this container */
public class MulticastReceiver extends ChannelSource {

	private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MulticastReceiver.class);

	private int port = 4446;
	private String group = "230.0.0.1";
	public Parameter groupParam;
	public Parameter portParam;

	public MulticastReceiver(CompositeEntity container, String name)
		throws NameDuplicationException, IllegalActionException {
		super(container, name);

		// Parameters
		groupParam = new StringParameter(this, "group");
		groupParam.setExpression(getGroup());
		portParam = new Parameter(this, "port", new IntToken(getPort()));
		portParam.setTypeEquals(BaseType.INT);

 	}

	/**
	 * Gets the group.
	 * @return Returns a String
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * Sets the group.
	 * @param group The group to set
	 */
	protected void setGroup(String group) {
		this.group = group;
	}

	/**
	 * Gets the port.
	 * @return Returns a int
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Sets the port.
	 * @param port The port to set
	 */
	protected void setPort(int port) {
		this.port = port;
	}

	/**
	 * Triggered whenever e.g. a parameter has been modified.
	 * 
	 * @param attribute The attribute that changed.
	 * @exception IllegalActionException
	 */
	public void attributeChanged(Attribute attribute)
		throws IllegalActionException {

		if(logger.isTraceEnabled())
			logger.trace(getInfo()+" :"+attribute);
			
		if (attribute == portParam) {
			IntToken portToken = (IntToken) portParam.getToken();
			if (portToken != null && portToken.intValue() > 0) {
				setPort(portToken.intValue());
			}
		} else if (attribute == groupParam) {
			StringToken groupToken = (StringToken) groupParam.getToken();
			if (groupToken != null && groupToken.stringValue().length() > 0) {
				setGroup(groupToken.stringValue());
			}
		} else
			super.attributeChanged(attribute);

		if(logger.isTraceEnabled())
			logger.trace(getInfo()+" - exit ");
	}


	/**
	 * @see be.tuple.passerelle.engine.actor.Source#getInfo()
	 */
	protected String getExtendedInfo() {
		return getGroup() + ":" + getPort();
	}

	/**
	 * @see be.tuple.passerelle.engine.actor.ChannelSource#createChannel()
	 */
	protected IReceiverChannel createChannel() {
		IReceiverChannel res = null;
		try {
			res = new MulticastReceiverChannel(getGroup(), getPort());
		} catch (ChannelException e) {
			logger.error("Error creating MulticastReceiverChannel for "+getInfo(),e);
		}
		
		return res;
	}

}