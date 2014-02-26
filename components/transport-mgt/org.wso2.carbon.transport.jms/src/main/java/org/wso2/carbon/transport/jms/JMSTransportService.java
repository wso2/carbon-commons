/*
 * Copyright 2005-2008 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.transport.jms;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.util.Loader;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.core.transports.AbstractTransportService;
import org.wso2.carbon.core.transports.util.TransportParameter;

import javax.xml.namespace.QName;
import java.util.Iterator;

/**
 * 
 */
public class JMSTransportService extends AbstractTransportService {

	/**
	 * Instantiates JMSTransportService with a reference to the AxisConfiguration.
	 */
	public JMSTransportService() {
		super(JMSTransportAdmin.TRANSPORT_NAME);
	}

    public boolean dependenciesAvailable(TransportParameter[] params) {
        try {
            for (TransportParameter p : params) {
                OMElement element = AXIOMUtil.stringToOM(p.getParamElement());
                Iterator parameters = element.getChildrenWithName(new QName("parameter"));
                while (parameters.hasNext()) {
                    OMElement parameterElement = (OMElement) parameters.next();
                    OMAttribute paramName = parameterElement.getAttribute(new QName("name"));
                    if (paramName != null) {
                        if (paramName.getAttributeValue().equalsIgnoreCase(
                                "java.naming.factory.initial")) {
                            String factoryName = parameterElement.getText().trim();
                            Loader.loadClass(factoryName);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
			return false;
        }
        return true;
    }
}