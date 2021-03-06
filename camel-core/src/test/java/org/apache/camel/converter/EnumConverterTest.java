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
package org.apache.camel.converter;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.NoTypeConversionAvailableException;
import org.apache.camel.impl.DefaultExchange;

/**
 * @version $Revision$
 */
public class EnumConverterTest extends ContextTestSupport {

    public void testMandatoryConvertEnum() throws Exception {
        LoggingLevel level = context.getTypeConverter().mandatoryConvertTo(LoggingLevel.class, "DEBUG");
        assertEquals(LoggingLevel.DEBUG, level);
    }

    public void testMandatoryConvertWithExchangeEnum() throws Exception {
        Exchange exchange = new DefaultExchange(context);
        LoggingLevel level = context.getTypeConverter().mandatoryConvertTo(LoggingLevel.class, exchange, "WARN");
        assertEquals(LoggingLevel.WARN, level);
    }

    public void testMandatoryConvertFailed() throws Exception {
        try {
            context.getTypeConverter().mandatoryConvertTo(LoggingLevel.class, "XXX");
            fail("Should have thrown an exception");
        } catch (NoTypeConversionAvailableException e) {
            // expected
        }
    }

}
