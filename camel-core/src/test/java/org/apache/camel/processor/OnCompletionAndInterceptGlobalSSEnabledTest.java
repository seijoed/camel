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
package org.apache.camel.processor;

import org.apache.camel.builder.RouteBuilder;

/**
 * @version $Revision$
 */
public class OnCompletionAndInterceptGlobalSSEnabledTest extends OnCompletionAndInterceptGlobalTest {

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // enabled stream cache
                context.setStreamCaching(true);

                intercept().to("mock:intercept");

                // define a global on completion that is invoked when the exchage is complete
                onCompletion().to("log:global").to("mock:sync");

                // START SNIPPET: e1
                from("direct:start")
                    .process(new MyProcessor())
                    .to("mock:result");
                // END SNIPPET: e1
            }
        };
    }

}