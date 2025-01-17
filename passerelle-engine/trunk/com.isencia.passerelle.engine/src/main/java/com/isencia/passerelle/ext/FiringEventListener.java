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
package com.isencia.passerelle.ext;

import ptolemy.actor.FiringEvent;

/**
 * Contract for any party wanting to 
 * react to transitions in an actor's and/or
 * director's firing cycle.
 * 
 * @see ptolemy.actor.FiringEvent
 *
 * @author erwin dl
 */
public interface FiringEventListener {
    void onEvent(FiringEvent e);
}
