/*
 * Copyright 2002-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans.factory.config;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.ObjectFactory;

/**
 * @author Juergen Hoeller
 */
@SuppressWarnings("serial")
public class SimpleMapScope implements Scope, Serializable {

	private final Map map = new HashMap();

	private final List callbacks = new LinkedList();


	public SimpleMapScope() {
	}

	public final Map getMap() {
		return this.map;
	}


	public Object get(String name, ObjectFactory objectFactory) {
		synchronized (this.map) {
			Object scopedObject = this.map.get(name);
			if (scopedObject == null) {
				scopedObject = objectFactory.getObject();
				this.map.put(name, scopedObject);
			}
			return scopedObject;
		}
	}

	public Object remove(String name) {
		synchronized (this.map) {
			return this.map.remove(name);
		}
	}

	public void registerDestructionCallback(String name, Runnable callback) {
		this.callbacks.add(callback);
	}

	public Object resolveContextualObject(String key) {
		return null;
	}

	public void close() {
		for (Iterator it = this.callbacks.iterator(); it.hasNext();) {
			Runnable runnable = (Runnable) it.next();
			runnable.run();
		}
	}

	public String getConversationId() {
		return null;
	}

}
