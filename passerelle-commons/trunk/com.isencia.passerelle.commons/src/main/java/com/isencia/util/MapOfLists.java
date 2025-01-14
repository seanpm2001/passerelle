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
package com.isencia.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MapOfLists
 * 
 * A java.util.Map implementation where multiple entries with
 * the same key can be maintained in Lists.
 * 
 * @author		erwin.de.ley@tuple.be
 */
public class MapOfLists implements Map {

	private Map map = null;
	private List values = null;

	/**
	 * Default constructor
	 *
	 */
	public MapOfLists() {
		map = new HashMap();
		values = new ArrayList();
	}

	/*
	 *  (non-Javadoc)
	 * @see java.util.Map#get(java.lang.Object)
	 */
	public Object get(Object key) {
		return map.get(key);
	}

	/*
	 *  (non-Javadoc)
	 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
	 */
	public Object put(Object key, Object value) {
		List valueList = (List) map.get(key);
		if(valueList==null) {
			// first time with this key,
			// so create List entry
			valueList = new ArrayList();
			map.put(key,valueList);
		}
		if(!valueList.contains(value)) {
			valueList.add(value);
			values.add(value);
			return null;
		} else {
			return value;
		}
	}
	/*
	 * @see Map#clear()
	 */
	public void clear() {
		Iterator listItr = map.values().iterator();
		while(listItr.hasNext()) {
			((List)listItr.next()).clear();
		}
		map.clear();
		values.clear();
	}

	/*
	 * @see Map#containsKey(Object)
	 */
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	/*
	 * @see Map#containsValue(Object)
	 */
	public boolean containsValue(Object value) {
		return values.contains(value);
	}

	/*
	 * @see Map#entrySet()
	 */
	public Set entrySet() {
		throw new Error("method not supported: "+getClass().getName()+".entrySet");
	}

	/*
	 * @see Map#isEmpty()
	 */
	public boolean isEmpty() {
		return values.isEmpty();
	}

	/*
	 * @see Map#keySet()
	 */
	public Set keySet() {
		return map.keySet();
	}

	/*
	 * @see Map#putAll(Map)
	 */
	public void putAll(Map t) {
		if(t!=null) {
			Iterator entryItr = t.entrySet().iterator();
			while (entryItr.hasNext()) {
				Entry e = (Entry) entryItr.next();
				put(e.getKey(),e.getValue());
			}
		}
	}

	/**
	 * Warning: removes the List entry from the map,
	 * i.e. the List entries are not cleared.
	 * 
	 * If the entries' object references need to be released,
	 * the returned List must be cleared.
	 * 
	 * @see Map#remove(Object)
	 */
	public Object remove(Object key) {
		Object e = map.remove(key);
		if(e!=null) {
			values.removeAll((List)e);
		}
		return e;
	}

	/*
	 * @see Map#size()
	 */
	public int size() {
		return values.size();
	}

	/*
	 * @see Map#values()
	 */
	public Collection values() {
		return values;
	}

	/**
	 * Returns a copy of this collection.
	 * @return MapOfLists
	 */
	public MapOfLists copy() {
		MapOfLists res = new MapOfLists();

		// alt 1: just copy all individual values
		Iterator entryItr = map.entrySet().iterator();
		while(entryItr.hasNext()) {
			Map.Entry e = (Map.Entry) entryItr.next();
			Object aKey = e.getKey();
			List aList = (List) e.getValue();
			if(aList!=null) {
				Iterator valItr = aList.iterator();
				while(valItr.hasNext()) {
					Object aValue = valItr.next();
					res.put(aKey, aValue);
				}
			} else {
				// should never happen...
				// if it does, the copy will be cleaner, 
				// i.e. no null entries are copied
			}
		}
		
		// alt 2: use clone()
		// probably faster, but need to hard-code casts to ArrayList...
		/*
		res.values = (List)((ArrayList)values).clone();
		Iterator entryItr = map.entrySet().iterator();
		while(entryItr.hasNext()) {
			Map.Entry e = (Map.Entry) entryItr.next();
			res.map.put(e.getKey(),((ArrayList)e.getValue()).clone());
		}
		*/
		return res;
	}


}
