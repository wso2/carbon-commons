/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.logging.admin.ui;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Options;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.logging.admin.stub.LoggingAdminStub;
import org.wso2.carbon.logging.admin.stub.types.carbon.LoggerData;


import java.lang.Exception;
import java.rmi.RemoteException;

/**
 * This is the Admin client used for retrieving and updating Log4J2 loggers.
 */
public class LoggingAdminClient {
    private static final Log log = LogFactory.getLog(LoggingAdminClient.class);

    public LoggingAdminStub stub;

    public LoggingAdminClient(String cookie, String backendServerURL,
                              ConfigurationContext configCtx) throws AxisFault {

        String serviceURL = backendServerURL + "LoggingAdmin";
        stub = new LoggingAdminStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public LoggerData[] getAllLoggerData(boolean beginsWith, String logNameFilter) throws Exception {
        try {
            return stub.getAllLoggerData(beginsWith, logNameFilter);
        } catch (RemoteException e) {
            String msg = "Error occurred while getting logger data. Backend service may be unavailable";
            log.error(msg, e);
            throw e;
        }
    }

    public String[] getLogLevels() {
        return new String[]{"OFF", "TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL"};
    }

    public void updateLoggerData(String loggerName, String logLevel) throws Exception {
        try {
            stub.updateLoggerData(loggerName, logLevel);
        } catch (Exception e) {
            String msg = "Error occurred while updating log4j2 logger configuration.";
            log.error(msg, e);
            throw e;
        }
    }

    public void addLogger(String loggerName, String loggerClass, String logLevel) throws Exception {
        try {
            stub.addLogger(loggerName, loggerClass, logLevel);
        } catch (Exception e) {
            String msg = "Error occurred while adding logger configuration.";
            log.error(msg, e);
            throw e;
        }
    }

    public boolean isLoggerExist(String loggerName) throws Exception {
        try {
            return stub.isLoggerExist(loggerName);
        } catch (Exception e) {
            String msg = "Error occurred while getting logger.";
            log.error(msg, e);
            throw e;
        }
    }
}
