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
package com.isencia.passerelle.message.internal;

import com.isencia.passerelle.message.MessageException;


/**
 * TriggerMessageContainer
 * 
 * This is a special type of MessageContainer, that contains no usefull data,
 * but is just used as an "event" or "trigger" message.
 * 
 * @author erwin dl
 */
public class TriggerMessageContainer extends MessageContainer {

	/**
	 * 
	 */
	public TriggerMessageContainer() {
		super();
		try {
			setBodyContent("true","text/plain");
		} catch (MessageException e) {
			// if it fails, that's just too bad...
		}
	}

}
