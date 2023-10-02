/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.logging.service.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.logging.service.RemoteLoggingConfig;
import org.wso2.carbon.logging.service.RemoteLoggingConfigService;
import org.wso2.carbon.logging.service.clustering.ClusterRemoteLoggerConfigInvalidationRequestSender;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.io.IOException;

/**
 * Remote Logging Config Service Component.
 */
@Component(name = "org.wso2.carbon.logging.service.component", immediate = true)
public class RemoteLoggingConfigServiceComponent {

    private static final Log LOG = LogFactory.getLog(RemoteLoggingConfigServiceComponent.class);

    @Activate
    protected void activate(ComponentContext componentContext) {

        try {
            RemoteLoggingConfigService remoteLoggingConfig = new RemoteLoggingConfig();
            componentContext.getBundleContext().registerService(
                    RemoteLoggingConfigService.class, remoteLoggingConfig, null);
            RemoteLoggingConfigDataHolder.getInstance().setRemoteLoggingConfigService(remoteLoggingConfig);
            ClusterRemoteLoggerConfigInvalidationRequestSender clusterRemoteLoggerConfigInvalidationRequestSender =
                    new ClusterRemoteLoggerConfigInvalidationRequestSender();
            RemoteLoggingConfigDataHolder.getInstance().setClusterRemoteLoggerConfigInvalidationRequestSender(
                    clusterRemoteLoggerConfigInvalidationRequestSender);
        } catch (IOException e) {
            LOG.error("IO exception occurred when creating RemoteLoggingConfig instance", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Remote Logging Config Service Component bundle is deactivated");
        }
    }

    @Reference(
            name = "registry.service",
            service = RegistryService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService"
    )
    protected void setRegistryService(RegistryService registryService) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Setting the RegistryService");
        }
        RemoteLoggingConfigDataHolder.getInstance().setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Unsetting the RegistryService");
        }
        RemoteLoggingConfigDataHolder.getInstance().setRegistryService(null);
    }

    @Reference(
            name = "config.context.service",
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetClusteringAgent"
    )
    protected void setClusteringAgent(ConfigurationContextService configurationContextService) {

        RemoteLoggingConfigDataHolder.getInstance()
                .setClusteringAgent(configurationContextService.getServerConfigContext().getAxisConfiguration().
                        getClusteringAgent());
    }

    protected void unsetClusteringAgent(ConfigurationContextService configurationContextService) {

        RemoteLoggingConfigDataHolder.getInstance().setClusteringAgent(null);
    }
}
