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
package com.isencia.passerelle.actor;

import com.isencia.passerelle.core.PasserelleException;


/**
 * InitializationException
 * 
 * TODO: class comment
 * 
 * @author erwin dl
 */
public class InitializationException extends PasserelleException {

	/**
	 * Creates a new InitializationException with NON_FATAL severity,
	 * and the given parameters.
	 * 
	 * @param message
	 * @param context
	 * @param rootException
	 */
	public InitializationException(String message, Object context, Throwable rootException) {
		super(Severity.NON_FATAL, message, context, rootException);
	}
	/**
	 * @param severity
	 * @param message
	 * @param context
	 * @param rootException
	 */
	public InitializationException(Severity severity, String message, Object context, Throwable rootException) {
		super(severity, message, context, rootException);
	}

}
