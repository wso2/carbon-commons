package org.wso2.carbon.logging.summarizer.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.NoStreamDefinitionExistException;
import org.wso2.carbon.databridge.commons.exception.StreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.logging.summarizer.core.SummarizerException;
import org.wso2.carbon.logging.summarizer.scriptCreator.SummaryGenerator;
import org.wso2.carbon.logging.summarizer.utils.LoggingConfig;
import org.wso2.carbon.logging.summarizer.utils.LoggingConfigManager;
import org.wso2.carbon.logging.summarizer.utils.SummarizingConstants;
import org.wso2.carbon.ntask.core.Task;

import java.util.Date;
import java.util.Map;

public class SummaryGenerationTask implements Task {

    private static Log log = LogFactory.getLog(SummaryGenerationTask.class);

    private Map<String, String> properties;

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public void init() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void execute() {

        LoggingConfig config = LoggingConfigManager.loadLoggingConfiguration();
        boolean isCassandraAvailable = config.isCassandraServerAvailable();
        if (isCassandraAvailable) {

            log.info("Running script executor task " + SummarizingConstants.TASK_NAME + ". [" + new Date() + "]");
            SummaryGenerator summaryGenerator = new SummaryGenerator();
            try {
                summaryGenerator.connectToBAM();
            } catch (SummarizerException e) {
               log.error("Error while Daily Log Summary Generation ", e);
                e.printStackTrace();
            } catch (AgentException e) {
                log.error("Error while Daily Log Summary Generation ", e);
                e.printStackTrace();
            } catch (AuthenticationException e) {
                log.error("Error while Daily Log Summary Generation ", e);
                e.printStackTrace();
            } catch (TransportException e) {
                log.error("Error while Daily Log Summary Generation ", e);
                e.printStackTrace();
            } catch (StreamDefinitionException e) {
                log.error("Error while Daily Log Summary Generation ", e);
                e.printStackTrace();
            } catch (NoStreamDefinitionExistException e) {
                log.error("Error while Daily Log Summary Generation ", e);
                e.printStackTrace();
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Logs will not be reading from the Cassandra Store");
            }


        }

    }

}