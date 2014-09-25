/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.java2wsdl.ui.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.ui.CarbonUIUtil;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;

public class Util {

    private static Log log = LogFactory.getLog(Util.class);

    public static String getRelativeUrl() {
        BundleContext context = CarbonUIUtil.getBundleContext();
        ServiceReference reference = context.getServiceReference(RegistryService.class
                .getName());
        RegistryService registryService = (RegistryService) context.getService(reference);
        String url = null;
        try {
            Registry systemRegistry = registryService.getConfigSystemRegistry();
            Resource resource = systemRegistry.get("/carbon/connection/props");
            String servicePath = resource.getProperty("service-path");
            String contextRoot = resource.getProperty("context-root");
            contextRoot = contextRoot.equals("/") ? "" : contextRoot;
            url = contextRoot + servicePath + "/Java2WSDLService";
        } catch (Exception e) {
            log.error(e);
        }
        return url;
    }

    public static OMElement getJava2WsdlOptions() {
        InputStream stream = Util.class.getResourceAsStream(
                "/org/wso2/carbon/java2wsdl/ui/client/java2wsdl-options.xml");
        OMElement omElement = null;
        try {
            StAXOMBuilder builder = new StAXOMBuilder(stream);
            omElement = builder.getDocumentElement();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return omElement;
    }
}
