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
package org.apache.camel.impl;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Holder object for sending an exchange over a remote wire as a serialized object.
 * This is usually configured using the <tt>transferExchange=true</tt> option on the endpoint.
 * <p/>
 * As opposed to normal usage where only the body part of the exchange is transfered over the wire,
 * this holder object serializes the following fields over the wire:
 * <ul>
 * <li>in body</li>
 * <li>out body</li>
 * <li>in headers</li>
 * <li>out headers</li>
 * <li>fault body </li>
 * <li>fault headers</li>
 * <li>exchange properties</li>
 * <li>exception</li>
 * </ul>
 * Any object that is not serializable will be skipped and Camel will log this at WARN level.
 *
 * @version $Revision$
 */
public class DefaultExchangeHolder implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final transient Log LOG = LogFactory.getLog(DefaultExchangeHolder.class);

    private Object inBody;
    private Object outBody;
    private Boolean outFaultFlag = Boolean.FALSE;
    private final Map<String, Object> inHeaders = new LinkedHashMap<String, Object>();
    private final Map<String, Object> outHeaders = new LinkedHashMap<String, Object>();
    private final Map<String, Object> properties = new LinkedHashMap<String, Object>();
    private Exception exception;

    /**
     * Creates a payload object with the information from the given exchange.
     * Only marshal the Serializable object
     *
     * @param exchange the exchange
     * @return the holder object with information copied form the exchange
     */
    public static DefaultExchangeHolder marshal(Exchange exchange) {
        DefaultExchangeHolder payload = new DefaultExchangeHolder();

        payload.inBody = checkSerializableObject("in body", exchange, exchange.getIn().getBody());
        payload.inHeaders.putAll(checkMapSerializableObjects("in headers", exchange, exchange.getIn().getHeaders()));
        if (exchange.hasOut()) {
            payload.outBody = checkSerializableObject("out body", exchange, exchange.getOut().getBody());
            payload.outHeaders.putAll(checkMapSerializableObjects("out headers", exchange, exchange.getOut().getHeaders()));
            payload.outFaultFlag = exchange.getOut().isFault();
        }
        payload.properties.putAll(checkMapSerializableObjects("exchange properties", exchange, exchange.getProperties()));
        payload.exception = exchange.getException();

        return payload;
    }

    /**
     * Transfers the information from the payload to the exchange.
     *
     * @param exchange the exchange to set values from the payload
     * @param payload  the payload with the values
     */
    public static void unmarshal(Exchange exchange, DefaultExchangeHolder payload) {
        exchange.getIn().setBody(payload.inBody);
        exchange.getIn().setHeaders(payload.inHeaders);
        if (payload.outBody != null) {
            exchange.getOut().setBody(payload.outBody);
            exchange.getOut().setHeaders(payload.outHeaders);
            exchange.getOut().setFault(payload.outFaultFlag.booleanValue());
        }
        for (String key : payload.properties.keySet()) {
            exchange.setProperty(key, payload.properties.get(key));
        }
        exchange.setException(payload.exception);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("DefaultExchangeHolder[");
        sb.append("inBody=").append(inBody).append(", outBody=").append(outBody);
        sb.append(", inHeaders=").append(inHeaders).append(", outHeaders=").append(outHeaders);
        sb.append(", properties=").append(properties).append(", exception=").append(exception);
        return sb.append(']').toString();
    }

    private static Object checkSerializableObject(String type, Exchange exchange, Object object) {
        if (object == null) {
            return null;
        }

        Serializable converted = exchange.getContext().getTypeConverter().convertTo(Serializable.class, exchange, object);
        if (converted != null) {
            return converted;
        } else {
            LOG.warn(type + " containing object: " + object + " of type: " + object.getClass().getCanonicalName() + " cannot be serialized, it will be excluded by the holder.");
            return null;
        }
    }

    private static Map<String, Object> checkMapSerializableObjects(String type, Exchange exchange, Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Serializable converted = exchange.getContext().getTypeConverter().convertTo(Serializable.class, exchange, entry.getValue());
            if (converted != null) {
                result.put(entry.getKey(), converted);
            } else {
                LOG.warn(type + " containing object: " + entry.getValue() + " with key: " + entry.getKey()
                        + " cannot be serialized, it will be excluded by the holder.");
            }
        }

        return result;
    }
}