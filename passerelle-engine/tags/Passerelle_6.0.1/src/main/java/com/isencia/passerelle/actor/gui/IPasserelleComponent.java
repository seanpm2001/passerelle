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
package com.isencia.passerelle.actor.gui;

import ptolemy.gui.CloseListener;
/**
 * Interface for the View component of the MVC implementation of parameter editing in Passerelle
 * Extends from CloseListener so we're notified when the parameter window in Passerelle is closed  
 * @author wim
 *
 */
public interface IPasserelleComponent extends  CloseListener {
	void addListener(IPasserelleComponentCloseListener closeListener);
}
