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
package com.isencia.passerelle.actor.mail;




import com.isencia.message.ISenderChannel;
import com.isencia.message.interceptor.IMessageInterceptor;
import com.isencia.message.interceptor.IMessageInterceptorChain;
import com.isencia.message.interceptor.MessageInterceptorChain;
import com.isencia.message.mail.MailSenderChannel;
import com.isencia.passerelle.actor.ChannelSink;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;

import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// SMTPSender

public class SMTPSender extends ChannelSink {

	public final static String MAILSERVER_PARAM = "MailServer";
	public final static String FROM_PARAM = "From";
	public final static String TO_PARAM = "To";
	public final static String SUBJECT_PARAM = "Subject";

	public final static String MAILHOST_HEADER = "MailHost";
	public final static String FROM_HEADER = "From";
	public final static String TO_HEADER = "To";
	public final static String CC_HEADER = "Cc";
	public final static String BCC_HEADER = "Bcc";
	public final static String SUBJECT_HEADER = "Subject";

	//~ Instance/static variables .............................................

	private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SMTPSender.class);

	///////////////////////////////////////////////////////////////////
	////                     ports and parameters                  ////
	public Parameter mailServerParam;
	public Parameter fromParam;
	public Parameter toParam;
	public Parameter subjectParam;

	///////////////////////////////////////////////////////////////////
	////                         private variables                 ////
	private String mailHost = null;
	private String from = null;
	private String to = null;
	private String subject = null;

	//~ Constructors ..........................................................

	public SMTPSender(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
		super(container, name);

		// Parameters
		mailServerParam = new StringParameter(this, MAILSERVER_PARAM);
		mailServerParam.setExpression(System.getProperty("mail.host", "host"));
		fromParam = new StringParameter(this, FROM_PARAM);
		fromParam.setExpression(System.getProperty("mail.from", "host"));
		toParam = new StringParameter(this, TO_PARAM);
		registerConfigurableParameter(toParam);
		subjectParam = new StringParameter(this, SUBJECT_PARAM);
		registerConfigurableParameter(subjectParam);
	}

	//~ Methods ...............................................................

	///////////////////////////////////////////////////////////////////
	////                     public methods                        ////

	/**
	 *  @param attribute The attribute that changed.
	 *  @exception IllegalActionException   */
	public void attributeChanged(Attribute attribute) throws IllegalActionException {

		if(logger.isTraceEnabled())
			logger.trace(getInfo()+" :"+attribute);
			
		if (attribute == mailServerParam) {

			StringToken mailServerToken = (StringToken) mailServerParam.getToken();

				if (mailServerToken != null && mailServerToken.stringValue().length()>0) {
					mailHost =  mailServerToken.stringValue();
					logger.debug("Mailhost Attribute changed to : " + mailHost);
				}
		} else if (attribute == fromParam) {

			StringToken fromToken = (StringToken) fromParam.getToken();

			if (fromToken != null) {
				from = fromToken.stringValue();
				logger.debug("From Attribute changed to : " + from);
			}
		} else if (attribute == toParam) {

			StringToken toToken = (StringToken) toParam.getToken();

			if (toToken != null) {
				to = toToken.stringValue();
				logger.debug("To Attribute changed to : " + to);
			}
		} else if (attribute == subjectParam) {

			StringToken subjectToken = (StringToken) subjectParam.getToken();

			if (subjectToken != null) {
				subject = subjectToken.stringValue();
				logger.debug("Subject Attribute changed to : " + subject);
			}
		} else
			super.attributeChanged(attribute);
			
		if (logger.isTraceEnabled())
			logger.trace(getInfo()+" - exit ");
	}

	protected String getExtendedInfo() {
		return mailHost;
	}

	protected ISenderChannel createChannel() {
		return new MailSenderChannel();
	}

    protected IMessageInterceptorChain createInterceptorChain() {
        IMessageInterceptorChain interceptors = new MessageInterceptorChain();
        interceptors.add(new IMessageInterceptor() {
            public Object accept(Object message) throws MessageException {

                ManagedMessage managedMsg = (ManagedMessage) message;

                // Check mailhost
                if (!managedMsg.hasBodyHeader(MAILHOST_HEADER) && mailHost != null && mailHost.length() > 0)
                    managedMsg.addBodyHeader(MAILHOST_HEADER, mailHost);
                    
                // Check from
                if (!managedMsg.hasBodyHeader(FROM_HEADER) && from != null && from.length() > 0)
                    managedMsg.addBodyHeader(FROM_HEADER, from);

                // Check to
                if (!managedMsg.hasBodyHeader(TO_HEADER) && to != null && to.length() > 0)
                    managedMsg.addBodyHeader(TO_HEADER, to);

                // Check subject
                if (!managedMsg.hasBodyHeader(SUBJECT_HEADER) && subject != null && subject.length() > 0)
                    managedMsg.addBodyHeader(SUBJECT_HEADER, subject);
                return managedMsg;
            }

        });

        interceptors.add(new MessageToMailMessageConverter(isPassThrough()));
        return interceptors;
    }
}