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
package org.apache.camel;

/**
 * A <a href="http://camel.apache.org/component.html">component</a> is
 * a factory of {@link Endpoint} objects.
 * 
 * @version $Revision$
 */
public interface Component extends CamelContextAware {

    /**
     * Attempt to resolve an endpoint for the given URI if the component is
     * capable of handling the URI
     * 
     * @param uri the URI to create
     * @return a newly created endpoint or null if this component cannot create
     *         instances of the given uri
     * @throws Exception is thrown if error creating the endpoint
     */
    Endpoint createEndpoint(String uri) throws Exception;
    
}
