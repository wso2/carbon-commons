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

package org.wso2.carbon.discovery;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.discovery.util.ConfigHolder;
import org.wso2.carbon.discovery.util.Util;
import org.wso2.carbon.core.ServerShutdownHandler;

/**
 * This shutdown handler is responsible for checking whether WS-D is enabled at server
 * shutdown and send the BYE messages for all the deployed services in the super tenant
 * AxisConfiguration.
 */
public class DiscoveryShutdownHandler implements ServerShutdownHandler {

    private Log log = LogFactory.getLog(DiscoveryShutdownHandler.class);

    public void invoke() {

        ConfigurationContext mainCfgCtx =
                ConfigHolder.getInstance().getServerConfigurationContext();
        if (mainCfgCtx != null) {
            AxisConfiguration mainAxisConfig = mainCfgCtx.getAxisConfiguration();
            if (mainAxisConfig.getParameter(DiscoveryConstants.DISCOVERY_PROXY) != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Sending BYE messages for services deployed in the super tenant");
                }
                Util.unregisterServiceObserver(mainAxisConfig, true);
            }
        } else {
            log.warn("Unable to notify service undeployment. ConfigurationContext is " +
                    "unavailable.");
        }
    }
}
