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
package org.wso2.carbon.application.deployer.synapse.internal;

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.application.deployer.AppDeployerConstants;
import org.wso2.carbon.application.deployer.AppDeployerUtils;
import org.wso2.carbon.application.deployer.Feature;
import org.wso2.carbon.application.deployer.handler.AppDeploymentHandler;
import org.wso2.carbon.application.deployer.synapse.SynapseAppDeployer;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * @scr.component name="application.deployer.synapse" immediate="true"
 */

public class SynapseAppDeployerDSComponent {

    private static Log log = LogFactory.getLog(SynapseAppDeployerDSComponent.class);

    private static Map<String, List<Feature>> requiredFeatures;

    private static ServiceRegistration appHandlerRegistration;

    protected void activate(ComponentContext ctxt) {
        try {
            // Register synapse deployer as an OSGi service
            SynapseAppDeployer synapseDeployer = new SynapseAppDeployer();
            appHandlerRegistration = ctxt.getBundleContext().registerService(
                    AppDeploymentHandler.class.getName(), synapseDeployer, null);

            // read required-features.xml
            URL reqFeaturesResource = ctxt.getBundleContext().getBundle()
                    .getResource(AppDeployerConstants.REQ_FEATURES_XML);
            if (reqFeaturesResource != null) {
                InputStream xmlStream = reqFeaturesResource.openStream();
                requiredFeatures = AppDeployerUtils
                        .readRequiredFeaturs(new StAXOMBuilder(xmlStream).getDocumentElement());
            }
        } catch (Throwable e) {
            log.error("Failed to activate Synapse Application Deployer", e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        // Unregister the OSGi service
        if (appHandlerRegistration != null) {
            appHandlerRegistration.unregister();
        }
    }

    public static Map<String, List<Feature>> getRequiredFeatures() {
        return requiredFeatures;
    }

}
