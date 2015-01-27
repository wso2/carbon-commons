/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.wso2.carbon.tracer;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.xml.XMLPrettyPrinter;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

/**
 * A collection of utility methods
 */
public class TracerUtils {
    private static final Log log = LogFactory.getLog(TracerUtils.class);

    /**
     * Get a prettified XML string from the SOAPEnvelope
     *
     * @param env        The SOAPEnvelope to be prettified
     * @param msgContext The MessageContext
     * @return prettified XML string from the SOAPEnvelope
     */
    public static String getPrettyString(OMElement env, MessageContext msgContext) {
        String xml;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            env.serialize(baos);
            InputStream xmlIn = new ByteArrayInputStream(baos.toByteArray());
            String encoding =
                    (String) msgContext.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);
            XMLPrettyPrinter xmlPrettyPrinter = new XMLPrettyPrinter(xmlIn, encoding);
            xml = xmlPrettyPrinter.xmlFormat();
        } catch (Throwable e) {
            String error = "Error occurred while pretty printing message. " + e.getMessage();
            log.error(error, e);
            xml = error;
        }
        return xml;
    }

}
