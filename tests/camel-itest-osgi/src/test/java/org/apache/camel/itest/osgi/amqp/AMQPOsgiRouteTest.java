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

package org.apache.camel.itest.osgi.amqp;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.itest.osgi.OSGiIntegrationTestSupport;
import org.apache.commons.lang.SerializationUtils;
import org.apache.qpid.client.transport.TransportConnection;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;


import static org.apache.camel.component.amqp.AMQPComponent.amqpComponent;
import static org.ops4j.pax.exam.CoreOptions.equinox;
import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.profile;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.scanFeatures;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.workingDirectory;

@RunWith(JUnit4TestRunner.class)
public class AMQPOsgiRouteTest extends OSGiIntegrationTestSupport {
    protected MockEndpoint resultEndpoint;

    @BeforeClass
    public static void startBroker() throws Exception {
        // lets create an in JVM broker
        try {
            TransportConnection.createVMBroker(1);
        } catch (Exception e) {
            // fails the first time, so create it again
            TransportConnection.createVMBroker(1);
        }
    }

    @AfterClass
    public static void shutdownBroker() {
        TransportConnection.killVMBroker(1);
    }

    @Test
    @Ignore("AMQP testing is a bit unstable")
    public void testJmsRouteWithTextMessage() throws Exception {
        String expectedBody = "Hello there!";

        boolean windows = System.getProperty("os.name").startsWith("Windows");

        if (windows) {
            // it could sometimes send it twice so we expect at least 1 msg
            resultEndpoint.expectedMinimumMessageCount(1);
        } else {
            resultEndpoint.expectedBodiesReceived(expectedBody);
        }

        resultEndpoint.message(0).header("cheese").isEqualTo(123);

        sendExchange(expectedBody);

        if (windows) {
            // send the message twice to walk around the AMQP's drop first message issue on Windows box
            sendExchange(expectedBody);
        }

        resultEndpoint.assertIsSatisfied();
    }

    @Test
    @Ignore("AMQP testing is a bit unstable")
    public void testJmsRouteWithObjectMessage() throws Exception {
        PurchaseOrder expectedBody = new PurchaseOrder("Beer", 10);

        resultEndpoint.expectedBodiesReceived(expectedBody);
        resultEndpoint.message(0).header("cheese").isEqualTo(123);

        sendExchange(expectedBody);

        resultEndpoint.assertIsSatisfied();
    }

    @Test
    @Ignore("AMQP testing is a bit unstable")
    public void testJmsRouteWithByteArrayMessage() throws Exception {
        PurchaseOrder aPO = new PurchaseOrder("Beer", 10);
        byte[] expectedBody = SerializationUtils.serialize(aPO);

        resultEndpoint.expectedBodiesReceived(expectedBody);
        resultEndpoint.message(0).header("cheese").isEqualTo(123);

        sendExchange(expectedBody);

        resultEndpoint.assertIsSatisfied();
    }

    protected void sendExchange(final Object expectedBody) {
        template.sendBodyAndHeader("amqp:queue:test.a", expectedBody, "cheese", 123);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        resultEndpoint = (MockEndpoint) context.getEndpoint("mock:result");
    }

    @Configuration
    public static Option[] configure() {
        Option[] options = options(

                // install the spring dm profile
                profile("spring.dm").version("1.2.0"),
                // this is how you set the default log level when using pax logging (logProfile)
                org.ops4j.pax.exam.CoreOptions.systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("INFO"),

                // using the features to install the camel components
                scanFeatures(getCamelKarafFeatureUrl(),
                        "camel-core", "camel-test", "camel-amqp"),

                workingDirectory("target/paxrunner/"),

                felix(), equinox());

        return options;
    }

    protected CamelContext createCamelContext() throws Exception {
        CamelContext camelContext = super.createCamelContext();

        camelContext.addComponent("amqp", amqpComponent("amqp://guest:guest@/test?brokerlist='vm://:1'"));

        return camelContext;
    }

    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from("amqp:test.a").to("amqp:test.b");
                from("amqp:test.b").to("mock:result");
            }
        };
    }
}

