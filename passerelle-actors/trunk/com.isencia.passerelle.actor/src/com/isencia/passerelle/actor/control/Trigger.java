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
package com.isencia.passerelle.actor.control;




import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Source;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.message.ManagedMessage;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * Produce a single trigger pulse, and then stops.
 *
 * @author erwin dl
 */

public class Trigger extends Source {

	private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Trigger.class);
	private boolean messageSent = false;


	public Trigger(CompositeEntity container, String name)
		throws NameDuplicationException, IllegalActionException {
		super(container, name);

		_attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"-20\" y=\"-20\" width=\"40\" " +
                "height=\"40\" style=\"fill:lightgrey;stroke:lightgrey\"/>\n" +
                "<line x1=\"-19\" y1=\"-19\" x2=\"19\" y2=\"-19\" " +
                "style=\"stroke-width:1.0;stroke:white\"/>\n" +
                "<line x1=\"-19\" y1=\"-19\" x2=\"-19\" y2=\"19\" " +
                "style=\"stroke-width:1.0;stroke:white\"/>\n" +
                "<line x1=\"20\" y1=\"-19\" x2=\"20\" y2=\"20\" " +
                "style=\"stroke-width:1.0;stroke:black\"/>\n" +
                "<line x1=\"-19\" y1=\"20\" x2=\"20\" y2=\"20\" " +
                "style=\"stroke-width:1.0;stroke:black\"/>\n" +
                "<line x1=\"19\" y1=\"-18\" x2=\"19\" y2=\"19\" " +
                "style=\"stroke-width:1.0;stroke:grey\"/>\n" +
                "<line x1=\"-18\" y1=\"19\" x2=\"19\" y2=\"19\" " +
                "style=\"stroke-width:1.0;stroke:grey\"/>\n" +
                
                "<rect x=\"-15\" y=\"-9\" width=\"28\" " +
                "height=\"18\" style=\"fill:white;stroke-width:1.0;stroke:white\"/>\n" +
                "<line x1=\"-14\" y1=\"-9\" x2=\"13\" y2=\"-9\" " +
                "style=\"stroke-width:1.5;stroke:grey\"/>\n" +
                "<line x1=\"-14\" y1=\"-9\" x2=\"-14\" y2=\"9\" " +
                "style=\"stroke-width:1.5;stroke:grey\"/>\n" +
                "<line x1=\"15\" y1=\"-9\" x2=\"15\" y2=\"11\" " +
                "style=\"stroke-width:1.0;stroke:white\"/>\n" +
                "<line x1=\"-15\" y1=\"11\" x2=\"15\" y2=\"11\" " +
                "style=\"stroke-width:1.0;stroke:white\"/>\n" +
                
                "<line x1=\"-10\" y1=\"-6\" x2=\"-10\" y2=\"6\" " +
                "style=\"stroke-width:1.0\"/>\n" +
                "<line x1=\"-10\" y1=\"-6\" x2=\"-5\" y2=\"-6\" " +
                "style=\"stroke-width:1.0\"/>\n" +
                "<line x1=\"-5\" y1=\"-6\" x2=\"-5\" y2=\"6\" " +
                "style=\"stroke-width:1.0\"/>\n" +
                "<line x1=\"-5\" y1=\"6\" x2=\"13\" y2=\"6\" " +
                "style=\"stroke-width:1.0\"/>\n" +
                "</svg>\n");
	}
	/*
	 *  (non-Javadoc)
	 * @see be.isencia.passerelle.actor.Actor#doInitialize()
	 */
	protected void doInitialize() throws InitializationException {
		if (logger.isTraceEnabled())
			logger.trace(getInfo());

		messageSent = false;
		super.doInitialize();

		if (logger.isTraceEnabled())
			logger.trace(getInfo()+" - exit ");
	}

	protected ManagedMessage getMessage() throws ProcessingException {

		if (logger.isTraceEnabled())
			logger.trace(getInfo());
		
		if( messageSent)
			return null;

		ManagedMessage dataMsg = null;
		try {
			dataMsg = createTriggerMessage();
		} finally {
			messageSent = true;
		}
		
		if (logger.isTraceEnabled())
			logger.trace(getInfo()+" - exit "+" : "+dataMsg);
			
		return dataMsg;
	}

	/* (non-Javadoc)
	 * @see be.isencia.passerelle.actor.Actor#getAuditTrailMessage(be.isencia.passerelle.message.ManagedMessage, be.isencia.passerelle.core.Port)
	 */
	protected String getAuditTrailMessage(ManagedMessage message, Port port) throws Exception {
		return "generated trigger.";
	}

	/**
	 * @see be.tuple.passerelle.engine.actor.Source#getInfo()
	 */
	protected String getExtendedInfo() {
		return "";
	}


}