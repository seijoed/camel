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
package org.apache.camel.component.cxf.mtom;

import java.awt.image.BufferedImage;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.imageio.ImageIO;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.ws.Endpoint;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Element;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.component.cxf.CxfPayload;
import org.apache.cxf.binding.soap.SoapHeader;
import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.helpers.XPathUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
* Unit test for exercising MTOM feature of a CxfProducer in PAYLOAD mode
* 
* @version $Revision$
*/
@ContextConfiguration
public class CxfMtomProducerPayloadModeTest extends AbstractJUnit4SpringContextTests {

    @Autowired
    protected CamelContext context;
    private Endpoint endpoint;

    @Before
    public void setUp() throws Exception {
        endpoint = Endpoint.publish("http://localhost:9092/jaxws-mtom/hello", new HelloImpl());
        SOAPBinding binding = (SOAPBinding)endpoint.getBinding();
        binding.setMTOMEnabled(true);
    }
    
    @After
    public void tearDown() throws Exception {
        if (endpoint != null) {
            endpoint.stop();
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testProducer() throws Exception {
        
        if (Boolean.getBoolean("java.awt.headless") || System.getProperty("os.name").startsWith("Mac OS")) {
            System.out.println("Running headless. Skipping test as Images may not work.");
            return;
        }     
        
        // START SNIPPET: producer

        Exchange exchange = context.createProducerTemplate().send("direct:testEndpoint", new Processor() {

            public void process(Exchange exchange) throws Exception {
                exchange.setPattern(ExchangePattern.InOut);
                List<Element> elements = new ArrayList<Element>();
                elements.add(DOMUtils.readXml(new StringReader(MtomTestHelper.REQ_MESSAGE)).getDocumentElement());
                CxfPayload<SoapHeader> body = new CxfPayload<SoapHeader>(new ArrayList<SoapHeader>(),
                    elements);
                exchange.getIn().setBody(body);
                exchange.getIn().addAttachment(MtomTestHelper.REQ_PHOTO_CID, 
                    new DataHandler(new ByteArrayDataSource(MtomTestHelper.REQ_PHOTO_DATA, "application/octet-stream")));

                exchange.getIn().addAttachment(MtomTestHelper.REQ_IMAGE_CID, 
                    new DataHandler(new ByteArrayDataSource(MtomTestHelper.requestJpeg, "image/jpeg")));

            }
            
        });
        
        // process response 
        
        CxfPayload<SoapHeader> out = exchange.getOut().getBody(CxfPayload.class);
        Assert.assertEquals(1, out.getBody().size());
        
        Map<String, String> ns = new HashMap<String, String>();
        ns.put("ns", MtomTestHelper.SERVICE_TYPES_NS);
        ns.put("xop", MtomTestHelper.XOP_NS);
        
        XPathUtils xu = new XPathUtils(ns);
        Element ele = (Element)xu.getValue("//ns:DetailResponse/ns:photo/xop:Include", out.getBody().get(0),
                                           XPathConstants.NODE);
        String photoId = ele.getAttribute("href").substring(4); // skip "cid:"

        ele = (Element)xu.getValue("//ns:DetailResponse/ns:image/xop:Include", out.getBody().get(0),
                                           XPathConstants.NODE);
        String imageId = ele.getAttribute("href").substring(4); // skip "cid:"

        DataHandler dr = exchange.getOut().getAttachment(photoId);
        Assert.assertEquals("application/octet-stream", dr.getContentType());
        MtomTestHelper.assertEquals(MtomTestHelper.RESP_PHOTO_DATA, IOUtils.readBytesFromStream(dr.getInputStream()));
   
        dr = exchange.getOut().getAttachment(imageId);
        Assert.assertEquals("image/jpeg", dr.getContentType());
        
        BufferedImage image = ImageIO.read(dr.getInputStream());
        Assert.assertEquals(560, image.getWidth());
        Assert.assertEquals(300, image.getHeight());
        
        // END SNIPPET: producer

    }
    
}