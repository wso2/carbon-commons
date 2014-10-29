package org.wso2.carbon.logging.summarizer.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.logging.summarizer.internal.SummaryDataHolder;
import org.wso2.carbon.logging.summarizer.utils.SummarizingConstants;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SummaryScheduler {
    private static final Log log = LogFactory.getLog(SummaryScheduler.class);

    public void invokeSummaryGneration(String cron) {

        if (cron != null && !cron.equals("")) {
            TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo();
            triggerInfo.setCronExpression(cron);

            TaskInfo info = new TaskInfo();

            info.setName(SummarizingConstants.TASK_NAME);
            info.setTriggerInfo(triggerInfo);
            info.setTaskClass(SummarizingConstants.TASK_CLASS_NAME);

            Map<String, String> properties = new HashMap<String, String>();

            properties.put(SummarizingConstants.TASK_TENANT_ID_KEY, String.valueOf(CarbonContext.getCurrentContext().getTenantId()));

            info.setProperties(properties);

            int tenantId = CarbonContext.getCurrentContext().getTenantId();
            try {
                SummaryDataHolder.getTaskManager().registerTask(info);
                SummaryDataHolder.getTaskManager().rescheduleTask(info.getName());
            } catch (TaskException e) {
                log.error("Error while scheduling script : " + info.getName() + " for tenant : " +
                        tenantId + "..", e);

            }
        }
    }
}
