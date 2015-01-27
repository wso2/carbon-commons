package org.wso2.carbon.logging.summarizer.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.hive.service.HiveExecutorService;
import org.wso2.carbon.logging.summarizer.utils.SummarizingConstants;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.service.TaskService;

public class SummaryDataHolder {

     private static final Log log = LogFactory.getLog(SummaryDataHolder.class);
    private static SummaryDataHolder dataHolder = new SummaryDataHolder();
    private static TaskService taskService;
    private ConfigurationContext contextService;
    private HiveExecutorService hiveExecutorService;

    public static  SummaryDataHolder getInstance() {
        return dataHolder;
    }

    private SummaryDataHolder() {
    }

    public void setTask(TaskService taskService) {
          this.taskService = taskService;
    }

    public static void setTaskService(TaskService taskService) {
        SummaryDataHolder.taskService = taskService;
    }

    public static TaskService getTask() {
        return taskService;
    }

    public void setServerConfigContext(ConfigurationContext configContext) {
		this.contextService = configContext;
	}

	public ConfigurationContext getServerConfigContext() {
		return this.contextService;
	}


    public static TaskManager getTaskManager() {
        TaskService taskService = SummaryDataHolder.getTask();
        try {
            return taskService.getTaskManager(SummarizingConstants.TASK_NAME);
        } catch (TaskException e) {
            log.error("Error while initializing TaskManager. Script scheduling may not" +
                      " work properly..", e);
            return null;
        }

    }

    public void setHiveExecutorService(HiveExecutorService hiveExecutorService) {
        this.hiveExecutorService = hiveExecutorService;
    }

    public HiveExecutorService getHiveExecutorService() {
		return this.hiveExecutorService;
	}
}
