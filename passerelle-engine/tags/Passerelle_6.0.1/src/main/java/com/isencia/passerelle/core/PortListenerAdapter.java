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
package com.isencia.passerelle.core;


/**
 * PortListenerAdapter is a utility class for implementers of PortListeners.
 * A bit similar in goals as the adapter classes in Swing, e.g. for WindowListener etc.
 * 
 * @author erwin dl
 */
public abstract class PortListenerAdapter implements PortListener {

	public void noMoreTokens() {
	}
	public void tokenReceived() {
	}
}