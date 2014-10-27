package org.wso2.carbon.logging.summarizer.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.analytics.hive.service.HiveExecutorService;
import org.wso2.carbon.logging.summarizer.scheduler.SummaryScheduler;
import org.wso2.carbon.logging.summarizer.utils.LoggingConfig;
import org.wso2.carbon.logging.summarizer.utils.LoggingConfigManager;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="logsummary.component" immediate="true"
 * @scr.reference name="ntask.component"
 * interface="org.wso2.carbon.ntask.core.service.TaskService"
 * cardinality="1..1" policy="dynamic" bind="setTaskService"
 * unbind="unsetTaskService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"  bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 * @scr.reference name="hive.executor.service"
 * interface="org.wso2.carbon.analytics.hive.service.HiveExecutorService"
 * cardinality="1..1" policy="dynamic"  bind="setHiveExecutorService"
 * unbind="unsetHiveExecutorService"
 */

public class SummarySchedulerComponent {

    private static final Log log = LogFactory.getLog(SummarySchedulerComponent.class);

    protected void activate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("Starting SummarySchedulerComponent");
        }

        LoggingConfig config = LoggingConfigManager.loadLoggingConfiguration();
        boolean isCassandraAvailable = config.isCassandraServerAvailable();
        if (isCassandraAvailable) {
            SummaryScheduler summaryScheduler = new SummaryScheduler();
            String cronExpression = config.getCronExpression();
            summaryScheduler.invokeSummaryGneration(cronExpression);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Logs will not be reading from the Cassandra Store");
            }

        }

    }

    protected void setTaskService(TaskService taskService) throws RegistryException {
        SummaryDataHolder.getInstance().setTask(taskService);

    }

    protected void unsetTaskService(TaskService taskService) {
        SummaryDataHolder.setTaskService(null);
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        SummaryDataHolder.getInstance().setServerConfigContext(contextService.getServerConfigContext());
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        SummaryDataHolder.getInstance().setServerConfigContext(null);
    }

    protected void setHiveExecutorService(HiveExecutorService hiveExecutorService){
        SummaryDataHolder.getInstance().setHiveExecutorService(hiveExecutorService);
    }
    protected void unsetHiveExecutorService(HiveExecutorService hiveExecutorService){
        SummaryDataHolder.getInstance().setHiveExecutorService(null);
    }
}


