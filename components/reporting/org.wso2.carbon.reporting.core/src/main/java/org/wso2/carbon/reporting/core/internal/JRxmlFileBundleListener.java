/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.reporting.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.core.ReportConstants;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;


/**
 * if there are .jrxml file of any bundle it will put to registry
 */
public class JRxmlFileBundleListener implements BundleListener {

    private static Log log = LogFactory.getLog(JRxmlFileBundleListener.class);

    public void bundleChanged(BundleEvent bundleEvent) {
        Bundle bundle = bundleEvent.getBundle();
        try {
            if (bundleEvent.getType() == BundleEvent.STARTED) {
                addJrXmlToRegistry(bundle);
            }
        } catch (Exception e) {
            log.error("Error occurred when putting jrXml file to registry in bundle "
                    + bundle.getSymbolicName(), e);
        }
    }

    /**
     * used to add .jrxml files to registry at bundle deployment time
     *
     * @param bundle Bundle
     * @throws ReportingException error occurred adding .jrxml file to registry
     */
    public void addJrXmlToRegistry(Bundle bundle) throws ReportingException {

        BundleContext bundleContext = bundle.getBundleContext();
        String reportResource = "/reports/";
        Enumeration enumeration = bundleContext.getBundle().getEntryPaths(reportResource);
        if (enumeration == null) {
            return;
        }
        try {
            RegistryService registryService = ReportingComponent.getRegistryService();
            Registry registry = registryService.getConfigSystemRegistry();
            registry.beginTransaction();
            Resource reportFilesResource = registry.newResource();
            InputStream xmlStream = null;
            try{
            while (enumeration.hasMoreElements()) {
                String path = enumeration.nextElement().toString();
                URL url = bundleContext.getBundle().getResource(path);
                if (url == null) {
                    return;
                }
                 xmlStream = url.openStream();
                if (xmlStream == null) {
                    return;
                }
                reportFilesResource.setContentStream(xmlStream);
                String location = ReportConstants.REPORT_BASE_PATH + bundle.getSymbolicName() + "/" + path.split("/")[1];
                if (!registry.resourceExists(location)) {
                    registry.put(location, reportFilesResource);
                }
            }
            }finally {
              xmlStream.close();
            }
            registry.commitTransaction();
        } catch (Exception e) {
            String msg = "Error occurred adding .jrxml file from " +
                    bundle.getSymbolicName() + " to registry";
            throw new ReportingException(msg, e);
        }

    }
}
