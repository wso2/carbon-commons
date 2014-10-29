package org.wso2.carbon.logging.summarizer.scriptCreator;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.logging.summarizer.core.SummarizerException;
import org.wso2.carbon.logging.summarizer.utils.LoggingConfig;
import org.wso2.carbon.logging.summarizer.utils.LoggingConfigManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryGenerator {

    private static final Log log = LogFactory.getLog(QueryGenerator.class);
    LoggingConfig config = LoggingConfigManager.loadLoggingConfiguration();
    String keyspaceName = config.getKeyspace();
    String archivedLogLocation = config.getArchivedLogLocation();

    public List<String> createFilePaths() throws SummarizerException {
        ColumnFamilyHandler cfHandler = new ColumnFamilyHandler();
        List<String> selectedColFamilies = cfHandler.filterColumnFamilies(keyspaceName);
        String logFileLocation = config.getTmpLogsDirectory();
        List<String> filePathList = new ArrayList<String>(selectedColFamilies.size());


        for (int i = 0; i < selectedColFamilies.size(); i++) {
            String colFamilyName = selectedColFamilies.get(i);
            if (colFamilyName.contains("log")) {
                String[] strArrayColFamilyNameParts = colFamilyName.split("_");
                int strArrayLength = strArrayColFamilyNameParts.length;

                if (strArrayLength > 1) {
                    String tenantId = strArrayColFamilyNameParts[1];
                    if (strArrayLength > 2) {
                        String serverName = strArrayColFamilyNameParts[2];
                        for (int j = 3; j < strArrayLength - 3; j++) {
                            serverName += "_" + strArrayColFamilyNameParts[j];
                        }
                        String date = strArrayColFamilyNameParts[strArrayLength - 3] + "_" + strArrayColFamilyNameParts[strArrayLength - 2] + "_" + strArrayColFamilyNameParts[strArrayLength - 1];

                        filePathList.add(archivedLogLocation + tenantId + "/" + serverName + "/" + date);
                    }
                }
            }
        }
        return filePathList;
    }

    public Map<String, String> createQuery() throws SummarizerException {
        Map hiveQueryMap = new HashMap();
        List<String> filteredColFamilies = new ColumnFamilyHandler().filterColumnFamilies(keyspaceName);
        List<String> filePaths = createFilePaths();

        for (int i = 0; i < filePaths.size(); i++) {
            String filePath = filePaths.get(i);
            log.info(filePath);
            String columnFamilyName = "\"" + filteredColFamilies.get(i) + "\"";

            String hiveQueryTemplate = config.getHiveQuery();
            String cassandraHost = config.getCassandraHost();
            String cassandraUserName = config.getCassUsername();
            String cassandraPassword = config.getCassPassword();
            String cassandraKeySpace = config.getKeyspace();

            String hiveQuery = replaceVars(hiveQueryTemplate, filePath, columnFamilyName, cassandraHost,
                    cassandraUserName, cassandraPassword, cassandraKeySpace);
            hiveQueryMap.put(filteredColFamilies.get(i), hiveQuery);
        }

        return hiveQueryMap;
    }


    public String replaceVars(String queryTemplate, String filePath, String cfName, String cassHost, String cassUsrName,
                              String cassPswd, String cassKeySpace) {

        //When there are multiple cassandra hosts given in the summarizer-config.xml
        String[] strArrCassHostList = cassHost.split(",");
        String hosts = "";
        String cassPort = strArrCassHostList[0].substring(strArrCassHostList[0].indexOf(":") + 1,
                strArrCassHostList[0].length());
        for(String host : strArrCassHostList) {
            hosts += host.substring(0, host.indexOf(":")) + ",";
        }
        hosts = hosts.substring(0, hosts.length() - 1);

        queryTemplate = queryTemplate.replace("set logs_column_family = %s;", "set logs_column_family = " +
                cfName + ";");
        queryTemplate = queryTemplate.replace("set file_path= %s;", "set file_path = " + filePath + ";");
        queryTemplate = queryTemplate.replace("\"cassandra.host\" = %s,", "\"cassandra.host\" = \"" + hosts + "\",");
        queryTemplate = queryTemplate.replace("\"cassandra.port\" = %s,", "\"cassandra.port\" =\"" + cassPort + "\",");
        queryTemplate = queryTemplate.replace("\"cassandra.ks.name\" = %s,", "\"cassandra.ks.name\" = \"" +
                cassKeySpace + "\",");
        queryTemplate = queryTemplate.replace("\"cassandra.ks.username\" = %s,", "\"cassandra.ks.username\" = \"" +
                cassUsrName + "\",");
        queryTemplate = queryTemplate.replace("\"cassandra.ks.password\" = %s,", "\"cassandra.ks.password\" =\"" +
                cassPswd + "\",");


        return queryTemplate;
    }

}

