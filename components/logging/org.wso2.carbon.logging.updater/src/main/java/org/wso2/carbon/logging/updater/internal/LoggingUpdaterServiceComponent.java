/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */


package org.wso2.carbon.logging.updater.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.logging.updater.LogConfigUpdater;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component(name = "org.wso2.carbon.logging.updater", immediate = true)
public class LoggingUpdaterServiceComponent {

    final static Log log = LogFactory.getLog(LoggingUpdaterServiceComponent.class);
    private ConfigurationAdmin configurationAdmin;

    @Reference(name = "osgi.configadmin.service",
            service = ConfigurationAdmin.class,
            unbind = "unsetConfigAdminService",
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC)
    public void setConfigAdminService(ConfigurationAdmin configAdminService) {

        ServiceReferenceHolder.getInstance().setConfigurationAdmin(configurationAdmin);
    }

    @Activate
    public void activate(BundleContext bundleContext) {

        LogConfigUpdater logConfigUpdater =
                new LogConfigUpdater(ServiceReferenceHolder.getInstance().getConfigurationAdmin());
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        ServiceReferenceHolder.getInstance().setScheduledExecutorService(scheduledExecutorService);
        scheduledExecutorService.schedule(logConfigUpdater, 5000L, TimeUnit.MILLISECONDS);
    }

    @Deactivate
    public void deactivate() {

        ServiceReferenceHolder.getInstance().getScheduledExecutorService().shutdown();
    }

    public void unsetConfigAdminService(ConfigurationAdmin configurationAdmin) {

        ServiceReferenceHolder.getInstance().setConfigurationAdmin(null);
    }

}
