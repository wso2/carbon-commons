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

import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.NoStreamDefinitionExistException;
import org.wso2.carbon.databridge.commons.exception.StreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.logging.summarizer.core.SummarizerException;
import org.wso2.carbon.logging.summarizer.utils.LoggingConfig;
import org.wso2.carbon.logging.summarizer.utils.LoggingConfigManager;
import org.wso2.carbon.logging.summarizer.utils.SummarizingConstants;

import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColumnFamilyHandler {

    private static final Log log = LogFactory.getLog(ColumnFamilyHandler.class);
    private Cluster cluster;
    LoggingConfig config = LoggingConfigManager.loadLoggingConfiguration();
    String keyspaceName = config.getKeyspace();


    public Cluster newConnection() {
        if (cluster == null) {
            Map<String, String> credentials = new HashMap<String, String>();
            String cassandraUserName = config.getCassUsername();
            String cassandraPassword = config.getCassPassword();
            String cassandraHost = config.getCassandraHost();
            String clusterName = config.getCluster();

            if (cassandraUserName != null && !cassandraUserName.equals("")) {
                if (cassandraPassword != null && !cassandraPassword.equals("")) {
                    if (cassandraHost != null && !cassandraHost.equals("") && clusterName != null &&
                            !clusterName.equals("")) {
                        credentials.put(SummarizingConstants.USERNAME_KEY, cassandraUserName);
                        credentials.put(SummarizingConstants.PASSWORD_KEY, cassandraPassword);

//                        cluster = HFactory.createCluster(clusterName,
//                                new CassandraHostConfigurator(cassandraHost),
//                                credentials);
                        try {
                            cluster = getCurrentCassandraCluster();
                        } catch (SummarizerException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                    } else {
                        try {
                            throw new SummarizerException("Error occured while connecting to Cassandra");
                        } catch (SummarizerException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                    }
                }
            }

        }
        return cluster;
    }

    private Cluster retrieveCassandraCluster(String clusterName, String connectionUrl,
                                             Map<String, String> credentials) throws SummarizerException {
        LoggingConfig config;
        try {
            config = LoggingConfigManager.loadLoggingConfiguration();
        } catch (Exception e) {
            throw new SummarizerException("Cannot read the Summarizer config file", e);
        }
        CassandraHostConfigurator hostConfigurator = new CassandraHostConfigurator(connectionUrl);
        hostConfigurator.setRetryDownedHosts(config.isRetryDownedHostsEnable());
        hostConfigurator.setRetryDownedHostsQueueSize(config.getRetryDownedHostsQueueSize());
        hostConfigurator.setAutoDiscoverHosts(config.isAutoDiscoveryEnable());
        hostConfigurator.setAutoDiscoveryDelayInSeconds(config.getAutoDiscoveryDelay());
        Cluster cluster = HFactory.createCluster(clusterName, hostConfigurator, credentials);
        return cluster;
    }


    private Cluster getCluster(String clusterName, String connectionUrl,
                               Map<String, String> credentials) throws SummarizerException {
        //removed getting cluster from session, since we dont aware of BAM shutting down time
        return retrieveCassandraCluster(clusterName, connectionUrl, credentials);
    }

    private Cluster getCurrentCassandraCluster() throws SummarizerException {
        LoggingConfig config;
        try {
            config = LoggingConfigManager.loadLoggingConfiguration();
        } catch (Exception e) {
            throw new SummarizerException("Cannot read the Summarizer config file", e);
        }
        String connectionUrl = config.getCassandraHost();
        String userName = config.getCassUsername();
        String password = config.getCassPassword();
        String clusterName = config.getCluster();
        Map<String, String> credentials = new HashMap<String, String>();
        credentials.put(SummarizingConstants.USERNAME_KEY, userName);
        credentials.put(SummarizingConstants.PASSWORD_KEY, password);
        return getCluster(clusterName, connectionUrl, credentials);
    }

    public List<ColumnFamilyDefinition> getColumnFamilies(String keyspaceName) throws SummarizerException {
        KeyspaceDefinition keyspaceDefinition = newConnection().describeKeyspace(keyspaceName);
        if (keyspaceDefinition != null && !keyspaceDefinition.equals("")) {
            return keyspaceDefinition.getCfDefs();
        } else {
            Exception e = null;
            log.error("There are no key spaces in the server yet");
            throw new SummarizerException("There are no key spaces in the server yet", e);
        }
    }


    public List<String> filterColumnFamilies(String keyspaceName) throws SummarizerException {
        ColumnFamilyHandler columnFamilyHandler = new ColumnFamilyHandler();
        List<ColumnFamilyDefinition> columnFamilyDefinitionList = columnFamilyHandler.getColumnFamilies(keyspaceName);
        List<String> selectedColumnFamilies = new ArrayList<String>(columnFamilyDefinitionList.size());

        //Retrieve Column Families that has previous date
        for (ColumnFamilyDefinition aColumnFamilyDefinitionList : columnFamilyDefinitionList) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
            Date date = new Date();
            String currentDate = dateFormat.format(date.getTime());
            String columnFamilyName = aColumnFamilyDefinitionList.getName();
            if (!columnFamilyName.contains(currentDate)) {
                Pattern pattern = Pattern.compile("(log_)([\\d]+)(_)([A-Z|a-z]+)(_)\\d{4}(_)\\d{2}(_)\\d{2}");
                    Matcher matcher = pattern.matcher(columnFamilyName);
                if(matcher.matches()) {
                    selectedColumnFamilies.add(columnFamilyName);
                }
            }
        }
        return selectedColumnFamilies;
    }


    public void deleteColumnFamily(String columnFamily) throws SummarizerException, AgentException, MalformedURLException, AuthenticationException, TransportException, StreamDefinitionException, NoStreamDefinitionExistException {
        log.info("Deleting CF " + columnFamily);
        newConnection().dropColumnFamily(keyspaceName, columnFamily, true);
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
        Date date = new Date();
        String prevDate = dateFormat.format(date.getTime() - 1000 * 60 * 60 * 24);
        if (log.isDebugEnabled()) {
            log.debug("The column family dated " + prevDate + " was deleted successfully.");
        }
    }


}

