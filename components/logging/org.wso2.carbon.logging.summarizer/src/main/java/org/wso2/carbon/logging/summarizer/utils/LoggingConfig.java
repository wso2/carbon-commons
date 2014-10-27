/*
 * Copyright The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// TOD0 Change the packaging to org.wso2.carbon.logging.common
package org.wso2.carbon.logging.summarizer.utils;

public class LoggingConfig {
    private boolean isCassandraServerAvailable;
    private boolean deleteColFamily;

    private String cassandraHost;
    private String keyspace;
    private String colFamily;
    private String cassUsername;
    private String cassPassword;
    private String cluster;
    private String publisherURL;
    private String publisherUser;
    private String publisherPassword;
    private String archivedHost;
    private String cronExpression;
    private String archivedUser;
    private String archivedPassword;
    private String archivedPort;
    private String archivedRealm;
    private String logDirectory;
    private String tmpLogsDirectory;
    private String hiveQuery;

    private String bamUserName;
    private String bamPassword;

    private String hdfsConfig;
    private String archivedLogLocation;

    private String consistencyLevel;
    private boolean isAutoDiscoveryEnable;
    private int autoDiscoveryDelay;
    private boolean retryDownedHostsEnable;
    private int retryDownedHostsQueueSize;

    public LoggingConfig() {

    }

    public LoggingConfig(String keyspace, String cassUsername, String cassPassword, String colFamily, String url) {
        super();
        this.keyspace = keyspace;
        this.cassUsername = cassUsername;
        this.cassPassword = cassPassword;
        this.colFamily = colFamily;
        this.publisherURL = url;
    }

    public String getKeyspace() {
        return keyspace;
    }

    public void setKeyspace(String keyspace) {
        this.keyspace = keyspace;
    }

    public String getCassUsername() {
        return cassUsername;
    }

    public void setCassUsername(String cassUsername) {
        this.cassUsername = cassUsername;
    }

    public String getCassPassword() {
        return cassPassword;
    }

    public void setCassPassword(String cassPassword) {
        this.cassPassword = cassPassword;
    }

    public String getColFamily() {
        return colFamily;
    }

    public void setColFamily(String colFamily) {
        this.colFamily = colFamily;
    }



    public boolean isCassandraServerAvailable() {
        return isCassandraServerAvailable;
    }

    public void setCassandraServerAvailable(boolean isCassandraServerAvailable) {
        this.isCassandraServerAvailable = isCassandraServerAvailable;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getPublisherURL() {
        return publisherURL;
    }

    public void setPublisherURL(String publisherURL) {
        this.publisherURL = publisherURL;
    }

    public String getPublisherUser() {
        return publisherUser;
    }

    public void setPublisherUser(String publisherUser) {
        this.publisherUser = publisherUser;
    }

    public String getPublisherPassword() {
        return publisherPassword;
    }

    public void setPublisherPassword(String publisherPassword) {
        this.publisherPassword = publisherPassword;
    }

    public String getArchivedHost() {
        return archivedHost;
    }

    public void setArchivedHost(String archivedHost) {
        this.archivedHost = archivedHost;
    }

    public String getArchivedUser() {
        return archivedUser;
    }

    public void setArchivedUser(String archivedUser) {
        this.archivedUser = archivedUser;
    }

    public String getArchivedPassword() {
        return archivedPassword;
    }

    public void setArchivedPassword(String archivedPassword) {
        this.archivedPassword = archivedPassword;
    }

    public String getArchivedPort() {
        return archivedPort;
    }

    public void setArchivedPort(String archivedPort) {
        this.archivedPort = archivedPort;
    }

    public String getArchivedRealm() {
        return archivedRealm;
    }

    public void setArchivedRealm(String archivedRealm) {
        this.archivedRealm = archivedRealm;
    }

    public String getHiveQuery() {
        return hiveQuery;
    }

    public void setHiveQuery(String hiveQuery) {
        this.hiveQuery = hiveQuery;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getCassandraHost() {
        return cassandraHost;
    }

    public void setCassandraHost(String cassandraHost) {
        this.cassandraHost = cassandraHost;
    }

    public String getLogDirectory() {
           return logDirectory;
       }

       public void setLogDirectory(String logDirectory) {
           this.logDirectory = logDirectory;
       }

    public String getBamUserName() {
        return bamUserName;
    }

    public void setBamUserName(String bamUserName) {
        this.bamUserName = bamUserName;
    }

    public String getBamPassword() {
        return bamPassword;
    }

    public void setBamPassword(String bamPassword) {
        this.bamPassword = bamPassword;
    }

    public boolean isDeleteColFamily() {
        return deleteColFamily;
    }

    public void setDeleteColFamily(boolean deleteColFamily) {
        this.deleteColFamily = deleteColFamily;
    }

    public String getTmpLogsDirectory() {
        return tmpLogsDirectory;
    }

    public void setTmpLogsDirectory(String tmpLogsDirectory) {
        this.tmpLogsDirectory = tmpLogsDirectory;
    }

    public String getHdfsConfig() {
        return hdfsConfig;
    }

    public void setHdfsConfig(String hdfsConfig) {
        this.hdfsConfig = hdfsConfig;
    }

    public String getArchivedLogLocation() {
        return archivedLogLocation;
    }

    public void setArchivedLogLocation(String archivedLogLocation) {
        this.archivedLogLocation = archivedLogLocation;
    }

    public String getConsistencyLevel() {
        return consistencyLevel;
    }

    public void setConsistencyLevel(String consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
    }

    public boolean isAutoDiscoveryEnable() {
        return isAutoDiscoveryEnable;
    }

    public void setAutoDiscoveryEnable(boolean autoDiscoveryEnable) {
        isAutoDiscoveryEnable = autoDiscoveryEnable;
    }


    public int getAutoDiscoveryDelay() {
        return autoDiscoveryDelay;
    }

    public void setAutoDiscoveryDelay(int autoDiscoveryDelay) {
        this.autoDiscoveryDelay = autoDiscoveryDelay;
    }

    public boolean isRetryDownedHostsEnable() {
        return retryDownedHostsEnable;
    }

    public void setRetryDownedHostsEnable(boolean retryDownedHostsEnable) {
        this.retryDownedHostsEnable = retryDownedHostsEnable;
    }

    public int getRetryDownedHostsQueueSize() {
        return retryDownedHostsQueueSize;
    }

    public void setRetryDownedHostsQueueSize(int retryDownedHostsQueueSize) {
        this.retryDownedHostsQueueSize = retryDownedHostsQueueSize;
    }
}
