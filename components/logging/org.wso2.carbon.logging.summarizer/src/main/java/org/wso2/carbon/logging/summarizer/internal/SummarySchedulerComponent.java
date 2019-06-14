package org.wso2.carbon.logging.summarizer.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.analytics.hive.service.HiveExecutorService;
import org.wso2.carbon.logging.summarizer.scheduler.SummaryScheduler;
import org.wso2.carbon.logging.summarizer.utils.LoggingConfig;
import org.wso2.carbon.logging.summarizer.utils.LoggingConfigManager;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.ConfigurationContextService;

@Component(
        name = "logsummary.component",
        immediate = true)
public class SummarySchedulerComponent {

    private static final Log log = LogFactory.getLog(SummarySchedulerComponent.class);

    @Activate
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

    @Reference(
            name = "ntask.component",
            service = org.wso2.carbon.ntask.core.service.TaskService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetTaskService")
    protected void setTaskService(TaskService taskService) throws RegistryException {

        SummaryDataHolder.getInstance().setTask(taskService);
    }

    protected void unsetTaskService(TaskService taskService) {

        SummaryDataHolder.setTaskService(null);
    }

    @Reference(
            name = "config.context.service",
            service = org.wso2.carbon.utils.ConfigurationContextService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService contextService) {

        SummaryDataHolder.getInstance().setServerConfigContext(contextService.getServerConfigContext());
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {

        SummaryDataHolder.getInstance().setServerConfigContext(null);
    }

    @Reference(
            name = "hive.executor.service",
            service = org.wso2.carbon.analytics.hive.service.HiveExecutorService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetHiveExecutorService")
    protected void setHiveExecutorService(HiveExecutorService hiveExecutorService) {

        SummaryDataHolder.getInstance().setHiveExecutorService(hiveExecutorService);
    }

    protected void unsetHiveExecutorService(HiveExecutorService hiveExecutorService) {

        SummaryDataHolder.getInstance().setHiveExecutorService(null);
    }
}
