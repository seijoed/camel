/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.spi;

/**
 * An interface to represent an object being managed.
 * <p/>
 * This allows you to gain fine grained control of managing objects with Camel.
 * For example various Camel components will implement this interface to provide
 * management to their endpoints and consumers.
 * <p/>
 * Camel will by default use generic management objects if objects do not implement
 * this interface. These defaults are located in <tt>org.apache.camel.management.mbean</tt>.
 *
 * @version $Revision$
 */
public interface ManagementAware<T> {

    /**
     * Gets the managed object
     *
     * @param object the object to be managed
     * @return the managed object
     */
    Object getManagedObject(T object);
}
