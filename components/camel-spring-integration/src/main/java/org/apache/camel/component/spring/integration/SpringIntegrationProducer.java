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
package org.apache.camel.component.spring.integration;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.spring.SpringCamelContext;
import org.apache.camel.util.ObjectHelper;
import org.springframework.integration.channel.BeanFactoryChannelResolver;
import org.springframework.integration.channel.ChannelResolver;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageChannel;
import org.springframework.integration.core.MessageHeaders;
import org.springframework.integration.message.MessageHandler;

/**
 * A producer of exchanges for the Spring Integration
 * Please specify the outputChannel in the endpoint url for this producer.
 * If the message pattern is inOut, the inputChannel property
 * should be set for receiving the response message.
 * @version $Revision$
 */
public class SpringIntegrationProducer extends DefaultProducer implements Processor {    

    private SpringCamelContext context;
    private DirectChannel inputChannel;
    private MessageChannel outputChannel;
    private String outputChannelName;
    private ChannelResolver channelResolver;


    public SpringIntegrationProducer(SpringIntegrationEndpoint endpoint) {
        super(endpoint);
        context = (SpringCamelContext) endpoint.getCamelContext();
        if (context != null && endpoint.getMessageChannel() == null) {
            outputChannelName = endpoint.getDefaultChannel();
            channelResolver = new BeanFactoryChannelResolver(context.getApplicationContext());
            if (ObjectHelper.isEmpty(outputChannelName)) {
                outputChannelName = endpoint.getInputChannel();
            }
            if (ObjectHelper.isEmpty(outputChannelName)) {
                throw new RuntimeCamelException("Cannot find outputChannelName, please check the endpoint uri outputChannel part!");
            } else {
                outputChannel = channelResolver.resolveChannelName(outputChannelName);
            }
        } else {
            if (endpoint.getMessageChannel() != null) {
                outputChannel = endpoint.getMessageChannel();
            } else {
                throw new RuntimeCamelException("Cannot find message channel, please check your configuration.");
            }
        }
        if (endpoint.isInOut()) {
            endpoint.setExchangePattern(ExchangePattern.InOut);
            // we need to setup right inputChannel for further processing
            if (ObjectHelper.isEmpty(endpoint.getInputChannel())) {
                throw new RuntimeCamelException("Cannot find inputChannel, please check the endpoint uri inputChannel part!");
            } else {
                inputChannel = (DirectChannel)channelResolver.resolveChannelName(endpoint.getInputChannel());
            }
        } else {
            endpoint.setExchangePattern(ExchangePattern.InOnly);
        }
    }

    public void process(final Exchange exchange) throws Exception {
        if (exchange.getPattern().isOutCapable()) {
            exchange.getIn().getHeaders().put(MessageHeaders.REPLY_CHANNEL , inputChannel);
            inputChannel.subscribe(new MessageHandler() {
                public void handleMessage(Message<?> message) {                    
                    SpringIntegrationBinding.storeToCamelMessage(message, exchange.getOut());
                }
            });
        }
        org.springframework.integration.core.Message siOutmessage = SpringIntegrationBinding.createSpringIntegrationMessage(exchange);
        
        outputChannel.send(siOutmessage);
    }

}
