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

package org.apache.camel.web.groovy;

/**
 * 
 */
public class InterceptFromDSLTest extends GroovyRendererTestSupport {

    public void testInterceptFromChoice() throws Exception {
        String DSL = "interceptFrom().choice().when(header(\"foo\").isEqualTo(\"bar\")).to(\"mock:b\").stop().end();from(\"direct:start\").to(\"mock:a\")";
        String expectedDSL = DSL;

        assertEquals(expectedDSL, render(DSL));
    }

    public void testInterceptFromPredicateWithStop() throws Exception {
        String DSL = "interceptFrom().when(header(\"usertype\").isEqualTo(\"test\")).stop();from(\"direct:start\").to(\"mock:result\")";
        String expectedDSL = "interceptFrom().choice().when(header(\"usertype\").isEqualTo(\"test\")).stop().end();from(\"direct:start\").to(\"mock:result\")";

        assertEquals(expectedDSL, render(DSL));
    }

    public void testInterceptFromToLog() throws Exception {
        String DSL = "interceptFrom().to(\"log:received\");from(\"direct:start\").to(\"mock:result\")";
        String expectedDSL = DSL;

        assertEquals(expectedDSL, render(DSL));
    }

    public void testInterceptFromUriRegex() throws Exception {
        String DSL = "interceptFrom(\"seda:(bar|foo)\").to(\"mock:intercept\");from(\"direct:start\").to(\"mock:result\");from(\"seda:bar\").to(\"mock:result\");from(\"seda:foo\").to(\"mock:result\");from(\"seda:cheese\").to(\"mock:result\")";
        String expectedDSL = "from(\"direct:start\").to(\"mock:result\");" +
        		"interceptFrom(\"seda:(bar|foo)\").to(\"mock:intercept\");from(\"seda:bar\").to(\"mock:result\");" +
        		"interceptFrom(\"seda:(bar|foo)\").to(\"mock:intercept\");from(\"seda:foo\").to(\"mock:result\");" +
        		"from(\"seda:cheese\").to(\"mock:result\")";

        assertEquals(expectedDSL, renderRoutes(DSL));
    }

    public void testInterceptFromUriSimpleLog() throws Exception {
        String DSL = "interceptFrom(\"seda:bar\").to(\"mock:bar\");from(\"direct:start\").to(\"mock:first\").to(\"seda:bar\");from(\"seda:bar\").to(\"mock:result\");from(\"seda:foo\").to(\"mock:result\")";
        String expectedDSL = "from(\"direct:start\").to(\"mock:first\").to(\"seda:bar\");interceptFrom(\"seda:bar\").to(\"mock:bar\");from(\"seda:bar\").to(\"mock:result\");from(\"seda:foo\").to(\"mock:result\")";

        assertEquals(expectedDSL, renderRoutes(DSL));
    }

    public void testInterceptFromUriWildcard() throws Exception {
        String DSL = "interceptFrom(\"seda*\").to(\"mock:intercept\");from(\"direct:start\").to(\"mock:result\");from(\"seda:bar\").to(\"mock:result\");from(\"seda:foo\").to(\"mock:result\")";
        String expectedDSL = "from(\"direct:start\").to(\"mock:result\");" +
        		"interceptFrom(\"seda*\").to(\"mock:intercept\");from(\"seda:bar\").to(\"mock:result\");" +
        		"interceptFrom(\"seda*\").to(\"mock:intercept\");from(\"seda:foo\").to(\"mock:result\")";
        
        assertEquals(expectedDSL, renderRoutes(DSL));
    }

    public void testInterceptFromWithPredicate() throws Exception {
        String DSL = "interceptFrom().when(header(\"foo\").isEqualTo(\"bar\")).to(\"mock:b\").stop();from(\"direct:start\").to(\"mock:a\")";
        String expectedDSL = "interceptFrom().choice().when(header(\"foo\").isEqualTo(\"bar\")).to(\"mock:b\").stop().end();from(\"direct:start\").to(\"mock:a\")";

        assertEquals(expectedDSL, render(DSL));
    }

}