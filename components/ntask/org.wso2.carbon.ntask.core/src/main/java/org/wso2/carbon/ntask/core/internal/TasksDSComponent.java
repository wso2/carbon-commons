/**
 *  Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.ntask.core.internal;

import com.hazelcast.core.HazelcastInstance;

import org.apache.axis2.engine.ListenerManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.ntask.core.TaskStartupHandler;
import org.wso2.carbon.ntask.core.impl.QuartzCachedThreadPool;
import org.wso2.carbon.ntask.core.impl.TaskAxis2ConfigurationContextObserver;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.ntask.core.service.impl.TaskServiceImpl;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.securevault.SecretCallbackHandlerService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.io.File;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class represents the Tasks declarative service component.
 * @scr.component name="tasks.component" immediate="true"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic"  bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService" cardinality="1..1" policy="dynamic"
 * bind="setRealmService" unbind="unsetRealmService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1" policy="dynamic" 
 * bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 * @scr.reference name="secret.callback.handler.service"
 * interface="org.wso2.carbon.securevault.SecretCallbackHandlerService"
 * cardinality="1..1" policy="dynamic"
 * bind="setSecretCallbackHandlerService" unbind="unsetSecretCallbackHandlerService"
 * @scr.reference name="listener.manager.service" interface="org.apache.axis2.engine.ListenerManager"
 * cardinality="1..1" policy="dynamic"  bind="setListenerManager" unbind="unsetListenerManager"
 */
public class TasksDSComponent {

    private static final String QUARTZ_PROPERTIES_FILE_NAME = "quartz.properties";

    private final Log log = LogFactory.getLog(TasksDSComponent.class);

    private static RegistryService registryService;

    private static RealmService realmService;

    private static Scheduler scheduler;

    private static ConfigurationContextService configCtxService;

    private static SecretCallbackHandlerService secretCallbackHandlerService;

    private static TaskService taskService;
    
    private static ExecutorService executor = Executors.newCachedThreadPool();

    protected void activate(ComponentContext ctx) {
        try {
            if (executor.isShutdown()) {
                executor = Executors.newCachedThreadPool();
            }
            String quartzConfigFilePath = CarbonUtils.getCarbonConfigDirPath() + File.separator
                    + "etc" + File.separator + QUARTZ_PROPERTIES_FILE_NAME;
            StdSchedulerFactory fac;
            if (new File(quartzConfigFilePath).exists()) {
                fac = new StdSchedulerFactory(quartzConfigFilePath);
            } else {
                fac = new StdSchedulerFactory(this.getStandardQuartzProps());
            }
            TasksDSComponent.scheduler = fac.getScheduler();
            TasksDSComponent.getScheduler().start();
            if (getTaskService() == null) {
                taskService = new TaskServiceImpl();
            }
            BundleContext bundleContext = ctx.getBundleContext();
            bundleContext.registerService(ServerStartupObserver.class.getName(),
                    new TaskStartupHandler(taskService), null);
            bundleContext.registerService(TaskService.class.getName(), getTaskService(), null);
            bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(),
                    new TaskAxis2ConfigurationContextObserver(getTaskService()), null);
            taskService.runAfterRegistrationActions();
        } catch (Throwable e) {
            log.error("Error in intializing Tasks component: " + e.getMessage(), e);
        }
    }

    private Properties getStandardQuartzProps() {
        Properties result = new Properties();
        result.put("org.quartz.scheduler.skipUpdateCheck", "true");
        result.put("org.quartz.threadPool.class", QuartzCachedThreadPool.class.getName());
        return result;
    }

    public static void executeTask(Runnable runnable) {
        executor.submit(runnable);
    }

    protected void deactivate(ComponentContext ctx) {
        if (TasksDSComponent.getScheduler() != null) {
            try {
                TasksDSComponent.getScheduler().shutdown();
            } catch (Exception e) {
                log.error(e);
            }
        }
        executor.shutdown();
        taskService = null;
    }

    public static TaskService getTaskService() {
        return taskService;
    }

    public static Scheduler getScheduler() {
        return scheduler;
    }

    protected void setRegistryService(RegistryService registryService) {
        TasksDSComponent.registryService = registryService;
    }

    protected void unsetRegistryService(RegistryService registryService) {
        TasksDSComponent.registryService = null;
    }

    public static RegistryService getRegistryService() {
        return TasksDSComponent.registryService;
    }

    protected void setRealmService(RealmService realmService) {
        TasksDSComponent.realmService = realmService;
    }

    protected void unsetRealmService(RealmService realmService) {
        TasksDSComponent.realmService = null;
    }

    public static RealmService getRealmService() {
        return TasksDSComponent.realmService;
    }

    protected void setConfigurationContextService(ConfigurationContextService configCtxService) {
        TasksDSComponent.configCtxService = configCtxService;
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configCtxService) {
        TasksDSComponent.configCtxService = null;
    }

    public static ConfigurationContextService getConfigurationContextService() {
        return TasksDSComponent.configCtxService;
    }

    public static SecretCallbackHandlerService getSecretCallbackHandlerService() {
        return TasksDSComponent.secretCallbackHandlerService;
    }

    protected void setSecretCallbackHandlerService(
            SecretCallbackHandlerService secretCallbackHandlerService) {
        TasksDSComponent.secretCallbackHandlerService = secretCallbackHandlerService;
    }

    protected void unsetSecretCallbackHandlerService(
            SecretCallbackHandlerService secretCallbackHandlerService) {
        TasksDSComponent.secretCallbackHandlerService = null;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static HazelcastInstance getHazelcastInstance() {
        BundleContext ctx = FrameworkUtil.getBundle(TasksDSComponent.class).getBundleContext();
        ServiceReference ref = ctx.getServiceReference(HazelcastInstance.class);
        if (ref == null) {
            return null;
        }
        return (HazelcastInstance) ctx.getService(ref);
    }
    
    protected void setListenerManager(ListenerManager lm) {
        /* we don't really need this, the listener manager service is acquired
         * to make sure, as a workaround, that the task component is initialized 
         * after the axis2 clustering agent is initialized */
    }
    
    protected void unsetListenerManager(ListenerManager lm) {
        /* empty */
    }

}
