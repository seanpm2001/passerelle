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
package com.isencia.passerelle.actor.io;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.message.IReceiverChannel;
import com.isencia.message.extractor.EndOfMsgCharMsgExtractor;
import com.isencia.message.extractor.IMessageExtractor;
import com.isencia.message.extractor.TextLineMessageExtractor;
import com.isencia.message.extractor.XmlMessageExtractor;
import com.isencia.message.interceptor.IMessageInterceptorChain;
import com.isencia.message.interceptor.MessageInterceptorChain;
import com.isencia.message.io.FileReceiverChannel;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.TriggeredChannelSource;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.interceptor.ManagedMessageConverter;
import com.isencia.passerelle.message.interceptor.TextToMessageConverter;
import com.isencia.passerelle.message.interceptor.XMLToMessageConverter;
import com.isencia.passerelle.util.EnvironmentUtils;
import com.isencia.util.StringConvertor;

//////////////////////////////////////////////////////////////////////////
//// FileReader
/**
 * This actor reads tokens from a file and sends them out.
 *
 */
public class FileReader extends TriggeredChannelSource {
    
	/**
	 * Classes outside might extend FileReader and need the InputType class
	 */
    protected static class InputType {
        
        public final static InputType 
                        TEXT_LINES = new InputType("text-lines","text/plain", new TextLineMessageExtractor(), new TextToMessageConverter(null)),
                        TEXT_FILE = new InputType("text-file","text/plain", new EndOfMsgCharMsgExtractor(), new TextToMessageConverter(null)),
                        XML_DOC = new InputType("xml-doc","text/xml", new XmlMessageExtractor(), new XMLToMessageConverter(null));
        
        public final static InputType[] choices = new InputType[] {TEXT_LINES,TEXT_FILE,XML_DOC};
        
        public static InputType getInputTypeForLabel(String label) {
            InputType res = null;
            for (int i = 0; i < choices.length; i++) {
                InputType type = choices[i];
                if(type.getLabel().equals(label)) {
                    res = choices[i];
                    break;
                }
            }
            return res;
        }
        
        private String label;
        private String mimeType;
        private IMessageExtractor msgExtractor;
        private ManagedMessageConverter msgConverter;
        
        private InputType(String label, String mimeType, IMessageExtractor msgExtractor, ManagedMessageConverter msgConverter) {
            this.label = label;
            this.mimeType = mimeType;
            this.msgExtractor = msgExtractor;
            this.msgConverter = msgConverter;
        }
        
        public String getLabel() {
            return label;
        }
        
        public String getMimeType() {
            return mimeType;
        }
        
        public IMessageExtractor getMsgExtractor() {
            return msgExtractor;
        }
        public ManagedMessageConverter getMsgConverter() {
            return msgConverter;
        }
    }

	private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(FileReader.class);

	public final static String PATH_PARAM = "Path";
	public final static String INPUTTYPE_PARAM = "Input Type";
	public final static String ENCODING_PARAM = "Encoding";
	
	private String sourcePath = null;
	private String fileEncoding = null;
	private InputType inputType = null;
	public FileParameter sourcePathParam = null;
	public Parameter inputTypeParam = null;
	public Parameter fileEncodingParam = null;
//	private IMessageInterceptorChain interceptorsOnLeave = null;

	/** Construct an actor with the given container and name.
	 *  @param container The container.
	 *  @param name The name of this actor.
	 *  @exception IllegalActionException If the actor cannot be contained
	 *   by the proposed container.
	 *  @exception NameDuplicationException If the container already has an
	 *   actor with this name.
	 */
	public FileReader(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
		super(container, name);

		sourcePathParam = new FileParameter(this, PATH_PARAM);
		try {
			URI baseURI = new URI("file://"+StringConvertor.convertPathDelimiters(EnvironmentUtils.getApplicationRootFolder()));
			sourcePathParam.setBaseDirectory(baseURI);
		} catch (URISyntaxException e) {
			// just give up
		}
		registerConfigurableParameter(sourcePathParam);
		fileEncodingParam= new StringParameter(this, ENCODING_PARAM);
		registerConfigurableParameter(fileEncodingParam);

        inputType = InputType.TEXT_LINES;
		inputTypeParam = new StringParameter(this, INPUTTYPE_PARAM);
		for(int i=0;i<InputType.choices.length;++i) {
			inputTypeParam.addChoice(InputType.choices[i].getLabel());
		}
		registerConfigurableParameter(inputTypeParam);
	}

	/**
	 *  @param attribute The attribute that changed.
	 *  @exception IllegalActionException   */
	public void attributeChanged(Attribute attribute) throws IllegalActionException {

		if(logger.isTraceEnabled())
			logger.trace(getInfo()+" :"+attribute);
		if (attribute == fileEncodingParam) {
			try {
				setFileEncoding(fileEncodingParam.getExpression());
				logger.debug("fileEncoding changed to : " + getFileEncoding());
			} catch (NullPointerException e) {
				// Ignore. Means that path is not a valid URL.
			}
		}else if (attribute == sourcePathParam) {
			try {
				setSourcePath(sourcePathParam.asFile().getPath());
				logger.debug("Source Path changed to : " + getSourcePath());
                // set file path as extra header for all generated messages
                getStandardMessageHeaders().put(ManagedMessage.SystemHeader.HEADER_SOURCE_INFO,getSourcePath());
			} catch (NullPointerException e) {
				// Ignore. Means that path is not a valid URL.
			}
		} else if (attribute == inputTypeParam) {
			String inputTypeChoice = inputTypeParam.getExpression();
            String prevInputType = getInputType().getLabel();
			try {
				InputType newInputType = InputType.getInputTypeForLabel(inputTypeChoice);
                if(newInputType!=null) {
    				setInputType(newInputType);
    				logger.debug("Mime Type changed to : " + getInputType());
                } else {
                    inputTypeParam.setExpression(prevInputType);
                }
			} catch (Exception e) {
				inputTypeParam.setExpression(prevInputType);
			}
		} else
			super.attributeChanged(attribute);

		if(logger.isTraceEnabled())
			logger.trace(getInfo()+" - exit ");
	}

	protected String getExtendedInfo() {
		return getSourcePath();
	}

	/**
	 * Gets the srcPath.
	 * @return Returns a String
	 */
	public String getSourcePath() {
		return sourcePath;
	}

	/*
	 *  (non-Javadoc)
	 * @see be.isencia.passerelle.actor.Actor#doPreFire()
	 */
	protected boolean doPreFire() throws ProcessingException {
		if(logger.isTraceEnabled())
			logger.trace(getInfo());
		
		boolean res = true;
		if (!getChannel().isOpen()) {
			if (getSourcePath() == null || getSourcePath().length() == 0) {
				requestFinish();
				res = false;
			}
		}
		
		if(res)
			res = super.doPreFire();
			
		if(logger.isTraceEnabled())
			logger.trace(getInfo()+" - exit "+" :"+res);
			
		return res;
	}

	
	
	protected String getFileEncoding() {
		return fileEncoding;
	}

	protected void setFileEncoding(String fileEncoding) {
		this.fileEncoding = fileEncoding;
	}

	/**
	 * Sets the srcPath.
	 * @param srcPath The srcPath to set
	 */
	protected void setSourcePath(String srcPath) {
		this.sourcePath = srcPath;
	}

	/**
	 * Returns the inputType.
	 * @return InputType
	 */
	public InputType getInputType() {
		return inputType;
	}

	/**
	 * Sets the inputType.
	 * @param inputType The inputType to set
	 */
	public void setInputType(InputType inputType) {
		this.inputType = inputType;
	}

	/**
	 * @see be.tuple.passerelle.engine.actor.TriggeredChannelSource#createChannel()
	 */
	protected IReceiverChannel createChannel() {
		if(getFileEncoding()!=null && getFileEncoding().length()>0){
			return new FileReceiverChannel(new File(getSourcePath()),getFileEncoding(), getInputType().getMsgExtractor().cloneExtractor());
		}
		return new FileReceiverChannel(new File(getSourcePath()), getInputType().getMsgExtractor().cloneExtractor());
	}

    
    protected IMessageInterceptorChain createInterceptorChain() {
        IMessageInterceptorChain interceptors = new MessageInterceptorChain();
        interceptors.add(getInputType().getMsgConverter().cloneConverter(this));
        return interceptors;
    }
}