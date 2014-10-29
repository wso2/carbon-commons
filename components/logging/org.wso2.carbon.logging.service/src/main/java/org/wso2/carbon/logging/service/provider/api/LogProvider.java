/*
 * Copyright 2005,2014 WSO2, Inc. http://www.wso2.org
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

package org.wso2.carbon.logging.service.provider.api;

import org.wso2.carbon.logging.service.LogViewerException;
import org.wso2.carbon.logging.service.data.LogEvent;
import org.wso2.carbon.logging.service.data.LoggingConfig;

import java.util.List;

/**
 * This interface should be inherited by all the log providers, This will allow you to plug any log
 * provider to carbon server. Carbon log viewer will use this interface to get the LogEvents and
 * show it on Log View UI.
 * <p/>
 * use logging-config.xml file to provide all the configuration which is required to initialized the
 * log provider implementation for an example, cassandra base log provider need to know about
 * keyspace, column family , etc ... You can configure these details in logging-config.xml file and
 * access using LoggingConfig class which contain all configuration parameter at runtime.
 */
public interface LogProvider {

    /**
     * Initialize the log provider by reading the property comes with logging configuration file
     * This will be called immediately after creating new instance of LogProvider
     *
     * @param loggingConfig
     */
    public void init(LoggingConfig loggingConfig);

    /**
     * Return a list of all application names deployed under provided tenant domain and server key
     *
     * @param tenantDomain
     *         - Tenant domain eg: t1.com
     * @param serverKey
     *         - Server key
     * @return - string List which has all application names, empty String List if there is no
     * application registered under given tenant domain and server key
     * @throws LogViewerException
     */
    public List<String> getApplicationNames(String tenantDomain, String serverKey)
            throws LogViewerException;

    /**
     * Return a list of system LogEvents
     *
     * @return - List of LogEvent , <code>null</code> if no system LogEvents available.
     * @throws LogViewerException
     */
    public List<LogEvent> getSystemLogs() throws LogViewerException;

    /**
     * Return a list of all the logs available under given domain and server key
     *
     * @param tenantDomain
     *         - Tenant domain eg: t1.com
     * @param serverKey
     *         - server key
     * @return - List of all LogEvents available under the tenant domain and server key. empty
     * LogEvent array if there is no LogEvents available.
     * @throws LogViewerException
     */
    public List<LogEvent> getAllLogs(String tenantDomain, String serverKey)
            throws LogViewerException;

    /**
     * Return a list of all the LogEvents belongs to the application, which is deployed under given
     * tenant domain and server key.
     *
     * @param appName
     *         - application name
     * @param tenantDomain
     *         - Tenant domain eg: t1.com
     * @param serverKey
     *         - Server key
     * @return all the LogEvents belongs to the application, which is deployed under given tenant
     * domain and server key, return  empty LogEvent array if there is no such LogEvents available.
     * @throws LogViewerException
     */
    public List<LogEvent> getLogsByAppName(String appName, String tenantDomain, String serverKey)
            throws LogViewerException;

    /**
     * Returns a list of all LogEvents related to the given application, which match to given type
     * and LogEvent message has given key word with it. User can use this api for search
     * operations.
     *
     * @param type
     *         - Log type , eg: ALL , INFO , DEBUG etc ....
     * @param keyword
     *         - Key word
     * @param appName
     *         - Application name
     * @param tenantDomain
     *         - Tenant domain eg: t1.com
     * @param serverKey
     *         - Server key
     * @return - all LogEvents related to the given application, which match to given type and
     * LogEvent message has given key word with it.  empty LogEvent array if there is no LogEvents
     * available.
     * @throws LogViewerException
     */
    public List<LogEvent> getLogs(String type, String keyword, String appName, String tenantDomain,
                                  String serverKey) throws LogViewerException;

    /**
     * Return LogEvent count.
     *
     * @param tenantDomain
     *         - Tenant domain eg: t1.com
     * @param serverKey
     *         - Server key
     * @return - LogEvent count, <code>0</code> if there is no LogEvent.
     * @throws LogViewerException
     */
    public int logsCount(String tenantDomain, String serverKey) throws LogViewerException;

    /**
     * Do clear operation, eg: if it is a in memory log provider then this can be used to clear the
     * memory.
     *
     * @return - <code>true</code>  if clear operation success, <code>false</code>  if not.
     */
    public boolean clearLogs();

}
