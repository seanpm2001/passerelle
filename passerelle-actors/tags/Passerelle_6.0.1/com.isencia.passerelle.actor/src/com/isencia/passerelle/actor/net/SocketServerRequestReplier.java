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

import java.io.IOException;
import java.net.ServerSocket;




import com.isencia.message.extractor.EndOfMsgCharMsgExtractor;
import com.isencia.message.extractor.IMessageExtractor;
import com.isencia.message.generator.IMessageGenerator;
import com.isencia.message.generator.MessageTextLineGenerator;
import com.isencia.message.requestreply.IRequestReplyChannel;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ReqReplyChannelSource;
import com.isencia.passerelle.actor.gui.IOptionsFactory.Option;
import com.isencia.passerelle.core.PasserelleException;

import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;

/**
 * 
 * A socket server that is able to send responses to incoming msgs.
 * It's a bit of a mix of a source and a sink...
 *
 * @author erwin
 */
public class SocketServerRequestReplier extends ReqReplyChannelSource {
    //~ Static variables/initializers ��������������������������������������������������������������������������������������������������������������������������

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SocketServerRequestReplier.class);

    //~ Instance variables �������������������������������������������������������������������������������������������������������������������������������������

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The server listen socket port. */
    public Parameter socketPort;
    private int port = 3333;
    
    public Parameter msgExtractorType;
    final static String MSG_EXTRACTOR_PARAM_NAME = "Msg End";


    //~ Constructors �������������������������������������������������������������������������������������������������������������������������������������������

    /**
     * SocketServerSource constructor comment.
     * 
     * @param container ptolemy.kernel.CompositeEntity
     * @param name java.lang.String
     * 
     * @exception ptolemy.kernel.util.IllegalActionException The exception description.
     * @exception ptolemy.kernel.util.NameDuplicationException The exception description.
     */
    public SocketServerRequestReplier(ptolemy.kernel.CompositeEntity container, String name)
                         throws ptolemy.kernel.util.IllegalActionException, ptolemy.kernel.util.NameDuplicationException {
        super(container, name);

        // Parameters
        socketPort = new Parameter(this, "port", new IntToken(getPort()));
        socketPort.setTypeEquals(BaseType.INT);
        msgExtractorType = new StringParameter(this, MSG_EXTRACTOR_PARAM_NAME);
    }

    //~ Methods ������������������������������������������������������������������������������������������������������������������������������������������������

    /**
     * Sets the port.
     * 
     * @param port The port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Gets the port.
     * 
     * @return Returns a int
     */
    public int getPort() {
        return port;
    }

    /**
     * @param attribute The attribute that changed.
     * 
     * @exception IllegalActionException
     */
    public void attributeChanged(Attribute attribute) throws IllegalActionException {
        if (logger.isTraceEnabled()) {
            logger.trace(getInfo() + " :" + attribute);
        }

        if (attribute == socketPort) {
            IntToken portToken = (IntToken) socketPort.getToken();

            if ((portToken != null) && (portToken.intValue() > 0)) {
                setPort(portToken.intValue());
            }
        } else {
            super.attributeChanged(attribute);
        }

        if (logger.isTraceEnabled()) {
            logger.trace(getInfo()+" - exit ");
        }
    }

    protected String getExtendedInfo() {
        return "localhost:" + getPort();
    }

    protected IRequestReplyChannel createChannel() throws InitializationException {
        IRequestReplyChannel res = null;
        IMessageExtractor extractor = getExtractorFromSelectedOption();
        IMessageGenerator generator = null;
        
        if(EndOfMsgCharMsgExtractor.class.isInstance(extractor)) {
            generator = new MessageTextLineGenerator(String.valueOf('\u001C'));
        } else {
            generator = new MessageTextLineGenerator();
        }
        try {
            ServerSocket sSocket = new ServerSocket(getPort());
            res = new com.isencia.message.net.requestreply.SocketServerRequestReplier(sSocket, extractor, generator);
        } catch (IOException e) {
            throw new InitializationException(PasserelleException.Severity.FATAL, "Error opening server socket on port" + getPort(), this, e);
        }
        return res;
    }
    
    /**
     * @param extractor
     * @return
     * @throws InitializationException
     */
    private IMessageExtractor getExtractorFromSelectedOption() throws InitializationException {
        IMessageExtractor extractor = null;
        // we wait as long as possible before checking
        // the options factory settings, so don't do it
        // in the constructor or attributeChanged or...
        try {
            if(getOptionsFactory()==null) {
                setOptionsFactory(new SocketSvrRcvOptionsFactory(this,OPTIONS_FACTORY_CFG_NAME));
            }
            Option o = getOptionsFactory().getOption(msgExtractorType,msgExtractorType.getExpression());
            if(o==null) {
                o = getOptionsFactory().getDefaultOption(msgExtractorType);
            }
            extractor = ((IMessageExtractor)o.getAssociatedObject()).cloneExtractor();
        } catch (Exception e) {
            throw new InitializationException(PasserelleException.Severity.FATAL, "Error setting Parameter options factory", this, e);
        }
        
        return extractor;
    }

}