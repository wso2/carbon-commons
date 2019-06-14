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
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.application.deployer.service.ApplicationManagerService;

@Component(
        name = "application.mgt.dscomponent",
        immediate = true)
public class AppManagementServiceComponent {

    private static Log log = LogFactory.getLog(AppManagementServiceComponent.class);

    private static ApplicationManagerService applicationManager;

    @Activate
    protected void activate(ComponentContext ctxt) {

        if (log.isDebugEnabled()) {
            log.debug("Activated AppManagementServiceComponent");
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

        if (log.isDebugEnabled()) {
            log.debug("Deactivated AppManagementServiceComponent");
        }
    }

    @Reference(
            name = "application.manager",
            service = org.wso2.carbon.application.deployer.service.ApplicationManagerService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetAppManager")
    protected void setAppManager(ApplicationManagerService appManager) {

        applicationManager = appManager;
    }

    protected void unsetAppManager(ApplicationManagerService appManager) {

        applicationManager = null;
    }

    public static ApplicationManagerService getAppManager() throws Exception {

        if (applicationManager == null) {
            String msg = "Before activating App management service bundle, an instance of " + "ApplicationManager " +
                    "should be in existance";
            log.error(msg);
            throw new Exception(msg);
        }
        return applicationManager;
    }
}
