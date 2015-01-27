/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.logging.service.provider.api;

import org.wso2.carbon.logging.service.LogViewerException;
import org.wso2.carbon.logging.service.data.LogFileInfo;
import org.wso2.carbon.logging.service.data.LoggingConfig;

import javax.activation.DataHandler;
import java.util.List;

/**
 * All log file providers must inherit this interface. Log viewer use this to get all the details related to log file.
 */
public interface LogFileProvider {

    /**
     * Initialize the file log provider by reading the property comes with logging configuration file
     * This will be called immediately after creating a new instance of LogFileProvider.
     *
     * @param loggingConfig - logging configuration
     */
    public void init(LoggingConfig loggingConfig);

    /**
     * Return a list of information about the log files, which is available under given tenant domain and
     * serviceName
     *      eg: log name, log date, log size
     *
     * @param tenantDomain
     *         - Tenant domain eg: t1.com
     * @param serviceName
     *         - Service name or Server key
     * @return array of LogFileInfo, which is available under given tenant domain and serviceName,
     * empty LogFileInfo array if there  is no LogFileInfo available.
     * @throws LogViewerException
     */
    public List<LogFileInfo> getLogFileInfoList(String tenantDomain, String serviceName)
            throws LogViewerException;

    /**
     * Download the file
     *
     * @param logFile      - File name which need to download, this should not be null.
     * @param tenantDomain - Tenant domain eg: t1.com
     * @param serviceName  - Service name or Server key
     * @return DataHandler for the given logfile, return <code>null</code> if there is not such logfile.
     * @throws LogViewerException
     */
    public DataHandler downloadLogFile(String logFile, String tenantDomain, String serviceName) throws LogViewerException;


}
