/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.logging.view.appenders;

import org.ops4j.pax.logging.spi.PaxAppender;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.logging.view.data.LogEvent;
import org.wso2.carbon.utils.logging.CircularBuffer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.SimpleDateFormat;

/**
 * Logging appender for LogViewer
 */
public class LoggingAppender implements PaxAppender {

    private CircularBuffer<LogEvent> circularBuffer;

    public LoggingAppender(CircularBuffer<LogEvent> logBuffer) {

        this.circularBuffer = logBuffer;
    }

    public void doAppend(PaxLoggingEvent paxLoggingEvent) {

        LogEvent logEvent = new LogEvent();
        logEvent.setMessage(paxLoggingEvent.getMessage());
        logEvent.setLogger(paxLoggingEvent.getLoggerName());
        logEvent.setPriority(paxLoggingEvent.getLevel().toString());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        logEvent.setLogTime(simpleDateFormat.format(paxLoggingEvent.getTimeStamp()));
        logEvent.setServerName(getServerName());
        logEvent.setTenantId(getTenantId());
        logEvent.setIp(getIp());
        logEvent.setAppName(getAppName());
        if (paxLoggingEvent.getThrowableStrRep() != null) {
            logEvent.setStacktrace(String.join("\n", paxLoggingEvent.getThrowableStrRep()));
        } else {
            logEvent.setStacktrace("");
        }
        circularBuffer.append(logEvent);
    }

    private String getServerName() {

        return AccessController.doPrivileged((PrivilegedAction<String>) () -> ServerConfiguration.getInstance().getFirstProperty("ServerKey"));
    }

    private String getIp() {

        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostAddress();
        } catch (UnknownHostException var3) {
            return "127.0.0.1";
        }
    }

    private String getTenantId() {

        int tenantId =
                AccessController.doPrivileged((PrivilegedAction<Integer>) () -> CarbonContext.getThreadLocalCarbonContext().getTenantId());
        return String.valueOf(tenantId);
    }

    private String getAppName() {

        String appName = CarbonContext.getThreadLocalCarbonContext().getApplicationName();
        return appName != null ? appName : "";
    }

}
