/*
*  Copyright (c), WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.url.mapper.clustermessage.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.local.LocalTransportReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.tomcat.api.CarbonTomcatService;
import org.wso2.carbon.url.mapper.clustermessage.util.DataHolder;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * Service Component to sending cluster message
 * @scr.component name="org.wso2.carbon.url.mapper.clustermessage" immediate="true"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"  bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 * @scr.reference name="tomcat.service.provider"
 * interface="org.wso2.carbon.tomcat.api.CarbonTomcatService"
 * cardinality="1..1" policy="dynamic" bind="setCarbonTomcatService"
 * unbind="unsetCarbonTomcatService"

 * */
public class VirtualHostClusterServiceComponent {
    private static Log log = LogFactory.getLog(VirtualHostClusterServiceComponent.class);


    protected void activate(ComponentContext context) {
        try {
            DataHolder.getInstance().registerRetrieverServices(context.getBundleContext());
            if(log.isDebugEnabled()){
                log.debug("******* Tenant Activity bundle is activated ******* ");
            }
        } catch (Throwable e) {
            log.error("******* Error in activating Tenant Activity bundle ******* ", e);
        }
    }

    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("******* Tenant Activity is deactivated ******* ");
        }
    }

    protected void setConfigurationContextService(ConfigurationContextService ccService) {
        ConfigurationContext serverCtx = ccService.getServerConfigContext();
        AxisConfiguration serverConfig = serverCtx.getAxisConfiguration();
        LocalTransportReceiver.CONFIG_CONTEXT = new ConfigurationContext(serverConfig);
        LocalTransportReceiver.CONFIG_CONTEXT.setServicePath("services");
        LocalTransportReceiver.CONFIG_CONTEXT.setContextRoot("local:/");

        DataHolder.getInstance().setConfigurationContextService(ccService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService ccService) {
        DataHolder.getInstance().setConfigurationContextService(null);
    }

    protected void setCarbonTomcatService(CarbonTomcatService carbonTomcatService) {
        //keeping the carbonTomcatService in UrlMapperAdminService class
        DataHolder.getInstance().setCarbonTomcatService(carbonTomcatService);
    }

    protected void unsetCarbonTomcatService(CarbonTomcatService carbonTomcatService) {
        DataHolder.getInstance().setCarbonTomcatService(null);
    }

}
