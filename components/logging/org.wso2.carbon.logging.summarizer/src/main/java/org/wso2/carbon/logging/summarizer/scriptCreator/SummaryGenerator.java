package org.wso2.carbon.logging.summarizer.scriptCreator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.hive.exception.HiveExecutionException;
import org.wso2.carbon.analytics.hive.service.HiveExecutorService;
import org.wso2.carbon.databridge.agent.thrift.conf.AgentConfiguration;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.NoStreamDefinitionExistException;
import org.wso2.carbon.databridge.commons.exception.StreamDefinitionException;
import org.wso2.carbon.logging.summarizer.core.SummarizerException;
import org.wso2.carbon.logging.summarizer.internal.SummaryDataHolder;
import org.wso2.carbon.logging.summarizer.utils.LoggingConfig;
import org.wso2.carbon.logging.summarizer.utils.LoggingConfigManager;
import org.wso2.carbon.logging.summarizer.utils.SummarizingConstants;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.TransportException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/*
 * Copyright 2005,2006 WSO2, Inc. http://www.wso2.org
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
public class SummaryGenerator {
	
	private static final Log log = LogFactory.getLog(SummaryGenerator.class);

	LoggingConfig config = LoggingConfigManager.loadLoggingConfiguration();
	private DataPublisher dataPublisher = null;
	
	public void connectToBAM() throws SummarizerException, AgentException,
			AuthenticationException, TransportException,
			StreamDefinitionException, NoStreamDefinitionExistException {

		log.info("Daily Summary Generation started for the "
				+ SummarizingConstants.TASK_NAME);

		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		long startTime = System.currentTimeMillis();
		log.info("Summary Generation started at "
				+ dateFormat.format(new Date(System.currentTimeMillis())));
		try {
			QueryGenerator queryGenerator = new QueryGenerator();
            Map<String, String> queryList = queryGenerator.createQuery();
			
			for (Map.Entry<String, String> entry : queryList.entrySet()) {
				HiveExecutorService hiveExecutorService = SummaryDataHolder
						.getInstance().getHiveExecutorService();
				log.info("Executing hive Query for CF : "+entry.getKey());
				hiveExecutorService.execute(null, entry.getValue());
            }

            ColumnFamilyHandler columnFamilyHandler = new ColumnFamilyHandler();

            for (Map.Entry<String, String> entry : queryList.entrySet()) {
				OutputFileHandler outputFileHandler = new OutputFileHandler();
                boolean isDeleteColFamily = config.isDeleteColFamily();
                String colFamilyName = entry.getKey();

				log.info("Restructuring archive log file : " + colFamilyName);
				outputFileHandler.fileReStructure(colFamilyName);

                //Deleting all data from the receiver for particular column family after archiving the log files
                if (isDeleteColFamily) {
					String truststorePath = CarbonUtils.getCarbonHome()
							+ "/repository/resources/security/client-truststore.jks";
					System.setProperty("javax.net.ssl.trustStore",
							truststorePath);
					System.setProperty("javax.net.ssl.trustStorePassword",
							"wso2carbon");
					if(dataPublisher == null) {
						dataPublisher = new DataPublisher(
								config.getPublisherURL(),
								config.getPublisherUser(),
								config.getPublisherPassword());
					}
                    String rectifiedCFName = colFamilyName.replace("_", ".");
                    try {
                        //deleting the stream
						log.info("Deleting stream: " + rectifiedCFName);
						dataPublisher.deleteStream(rectifiedCFName, "1.0.0");
                        //deleting the column family
                        columnFamilyHandler.deleteColumnFamily(colFamilyName);
                    } catch (AgentException e) {
						log.warn("Unable to delete stream definition : "
								+ rectifiedCFName);
					} catch (Exception e) {
                        log.warn("error while deleting the stream or column family. " +
                                "But continuing file restructuring....");
                    }

				}
			
			}

			log.info("Summary Generation completed at "
					+ dateFormat.format(new Date(System.currentTimeMillis())));
			long timeTaken = System.currentTimeMillis() - startTime;
			log.info("Time taken for Summary Generation: " + timeTaken / 1000
					+ "s");
        } catch (HiveExecutionException e) {
			log.error("Error while Daily Log Summary Generation ", e);
			throw new SummarizerException(
					"Error while Daily Log Summary Generation ", e);
		} catch (IOException e) {
			log.error("Error while Daily Log Summary Generation ", e);
			throw new SummarizerException(
					"Error while Daily Log Summary Generation ", e);
		}
	}

}
