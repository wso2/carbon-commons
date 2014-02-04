/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
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
package org.wso2.carbon.application.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.application.deployer.service.ApplicationManagerService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.osgi.service.component.ComponentContext;

/**
 * @scr.component name="application.mgt.dscomponent" immediate="true"
 * @scr.reference name="application.manager" interface="org.wso2.carbon.application.deployer.service.ApplicationManagerService"
 * cardinality="1..1" policy="dynamic" bind="setAppManager" unbind="unsetAppManager"
 */
public class AppManagementServiceComponent {

    private static Log log = LogFactory.getLog(AppManagementServiceComponent.class);

    private static ApplicationManagerService applicationManager;

    protected void activate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("Activated AppManagementServiceComponent");
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("Deactivated AppManagementServiceComponent");
        }
    }

    protected void setAppManager(ApplicationManagerService appManager) {
        applicationManager = appManager;
    }

    protected void unsetAppManager(ApplicationManagerService appManager) {
        applicationManager = null;
    }

    public static ApplicationManagerService getAppManager() throws Exception {
        if (applicationManager == null) {
            String msg = "Before activating App management service bundle, an instance of "
                    + "ApplicationManager should be in existance";
            log.error(msg);
            throw new Exception(msg);
        }
        return applicationManager;
    }

}
