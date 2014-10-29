/*
 * Copyright 2005,2014 WSO2, Inc. http://www.wso2.org
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

package org.wso2.carbon.logging.service.config;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.logging.service.LogViewerException;
import org.wso2.carbon.logging.service.util.LoggingConstants;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ServiceConfigManager {

    private static final Log log = LogFactory.getLog(ServiceConfigManager.class);

    public static String[] getServiceNames() throws LogViewerException {
        String configFileName = CarbonUtils.getCarbonConfigDirPath() + File.separator +
                                LoggingConstants.MULTITENANCY_CONFIG_FOLDER + File.separator +
                                LoggingConstants.CONFIG_FILENAME;
        List<String> serviceNames = new ArrayList<String>();
        File configFile = new File(configFileName);
        if (configFile.exists()) {
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(configFile);
                XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(
                        inputStream);
                StAXOMBuilder builder = new StAXOMBuilder(parser);
                OMElement documentElement = builder.getDocumentElement();
                @SuppressWarnings("unchecked")
                Iterator<OMElement> properties = documentElement.getChildrenWithName(new QName(
                        "cloudService"));
                while (properties.hasNext()) {
                    OMElement element = properties.next();
                    Iterator<OMElement> child = element.getChildElements();
                    while (child.hasNext()) {
                        OMElement element1 = (OMElement) child.next();
                        if ("key".equalsIgnoreCase(element1.getLocalName())) {
                            serviceNames.add(element1.getText());
                        }
                    }
                }
            } catch (Exception e) {
                String msg = "Error in loading Stratos Configurations File: " + configFileName;
                throw new LogViewerException(msg, e);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        log.error("Could not close the Configuration File " + configFileName, e);
                    }
                }
            }
        }
        return serviceNames.toArray(new String[serviceNames.size()]);
    }

    public static boolean isStratosService(String serviceName) throws LogViewerException {
        String services[] = getServiceNames();
        for (String service : services) {
            if (service.equals(serviceName)) {
                return true;
            }
        }
        return false;
    }
}
