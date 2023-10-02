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
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.logging.service.RemoteLoggingConfigService;
import org.wso2.carbon.logging.updater.LogConfigUpdater;
import org.wso2.carbon.logging.updater.LoggingUpdaterConstants;
import org.wso2.carbon.logging.updater.LoggingUpdaterException;
import org.wso2.carbon.logging.updater.LoggingUpdaterUtil;
import org.wso2.carbon.logging.updater.RemoteLoggingConfigUpdater;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * Logging updater service component
 */
@Component(name = "org.wso2.carbon.logging.updater", immediate = true, property = EventConstants.EVENT_TOPIC
        + "=" + LoggingUpdaterConstants.PAX_LOGGING_CONFIGURATION_TOPIC)
public class LoggingUpdaterServiceComponent implements EventHandler {

    final static Log log = LogFactory.getLog(LoggingUpdaterServiceComponent.class);
    private ConfigurationAdmin configurationAdmin;
    private static final String REMOTE_LOGIN_PERIODIC_DB_SYNC_PERIOD = "RemoteLogging.PeriodicDBSync.Period";

    @Reference(name = "osgi.configadmin.service",
            service = ConfigurationAdmin.class,
            unbind = "unsetConfigAdminService",
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC)
    public void setConfigAdminService(ConfigurationAdmin configAdminService) {

        DataHolder.getInstance().setConfigurationAdmin(configAdminService);
    }

    @Activate
    public void activate(ComponentContext componentContext) {

        try {
            DataHolder.getInstance().setModifiedTime(LoggingUpdaterUtil.readModifiedTime());
            LogConfigUpdater logConfigUpdater =
                    new LogConfigUpdater(DataHolder.getInstance().getConfigurationAdmin());
            ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
            DataHolder.getInstance().setScheduledExecutorService(scheduledExecutorService);
            scheduledExecutorService.scheduleAtFixedRate(logConfigUpdater, 5000L, 5000L, TimeUnit.MILLISECONDS);

            RemoteLoggingConfigUpdater remoteLoggingConfigUpdater =
                    new RemoteLoggingConfigUpdater(DataHolder.getInstance().getRemoteLoggingConfigService());
            ScheduledExecutorService remoteLoggingConfigUpdaterExecutorService = Executors.newScheduledThreadPool(1);
            DataHolder.getInstance()
                    .setRemoteLoggingConfigUpdaterExecutorService(remoteLoggingConfigUpdaterExecutorService);
            int syncPeriod = Integer.parseInt(DataHolder.getInstance().getServerConfigurationService()
                    .getFirstProperty(REMOTE_LOGIN_PERIODIC_DB_SYNC_PERIOD));
            remoteLoggingConfigUpdaterExecutorService.scheduleAtFixedRate(remoteLoggingConfigUpdater, 60 * 1000L,
                    syncPeriod * 60 * 1000L, TimeUnit.MILLISECONDS);
        } catch (LoggingUpdaterException e) {
            log.error("Error while Activating LoggingUpdater component", e);
        }
    }

    @Deactivate
    public void deactivate() {

        DataHolder.getInstance().getScheduledExecutorService().shutdown();
        DataHolder.getInstance().getRemoteLoggingConfigUpdaterExecutorService().shutdown();
    }

    public void unsetConfigAdminService(ConfigurationAdmin configurationAdmin) {

        DataHolder.getInstance().setConfigurationAdmin(null);
    }

    @Override
    public void handleEvent(Event event) {

        if (event.containsProperty(LoggingUpdaterConstants.EXCEPTIONS_PROPERTY)) {
            Object property = event.getProperty(LoggingUpdaterConstants.EXCEPTIONS_PROPERTY);
            Exception exception = (Exception) property;
            log.error("Unable to apply logging configuration " + exception.getMessage());
            log.error("Continuing with previous logging configuration");
        } else {
            log.info("Logging configuration applied successfully");
        }
    }

    @Reference(
            name = "remote.logging.config.service",
            service = RemoteLoggingConfigService.class,
            unbind = "unsetRemoteLoggingConfigService",
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC
    )
    public void setRemoteLoggingConfigService(RemoteLoggingConfigService remoteLoggingConfigService) {

        DataHolder.getInstance().setRemoteLoggingConfigService(remoteLoggingConfigService);
    }

    public void unsetRemoteLoggingConfigService(RemoteLoggingConfigService remoteLoggingConfigService) {

        DataHolder.getInstance().setRemoteLoggingConfigService(null);
    }

    @Reference(
            name = "server.configuration.service",
            policy = ReferencePolicy.DYNAMIC,
            cardinality = ReferenceCardinality.MANDATORY,
            unbind = "unsetServerConfigurationService"
    )
    protected void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {

        DataHolder.getInstance().setServerConfigurationService(serverConfigurationService);
    }

    protected void unsetServerConfigurationService(ServerConfigurationService serverConfigurationService) {

        DataHolder.getInstance().setServerConfigurationService(null);
    }
}
