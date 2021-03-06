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
package org.apache.camel.component.restlet;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.junit.Test;

/**
 *
 * @version $Revision$
 */
public class RestletResponseTest extends RestletTestSupport {

    @Override
    protected RouteBuilder createRouteBuilder() {

        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("restlet:http://localhost:9080/users/{username}?restletMethod=POST").process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        String userName = exchange.getIn().getHeader("username", String.class);                        
                        assertNotNull("userName should not be null", userName);
                        exchange.getOut().setBody("{" + userName + "}");
                        exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, "417");
                        exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "application/JSON");
                    }        
                });
            }
        };
    }
    
    @Test
    public void testCustomResponse() throws Exception {
        HttpResponse response = doExecute(new HttpPost("http://localhost:9080/users/homer"));

        assertHttpResponse(response, 417, "application/JSON");
    }
    
    @Test
    public void testRestletProducer() throws Exception {
        Map<String, Object> headers = new HashMap<String, Object>();        
        headers.put("username", "homer");
        String response = (String)template.requestBodyAndHeaders("restlet:http://localhost:9080/users/{username}?restletMethod=POST", "<request>message</request>", headers);
        assertEquals("The response is wrong ", response, "{homer}");
    }
}