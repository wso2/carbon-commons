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
package org.wso2.carbon.logging.appender;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.wso2.carbon.bootstrap.logging.LoggingBridge;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.logging.CircularBuffer;
import org.wso2.carbon.utils.logging.LoggingUtils;
import org.wso2.carbon.logging.internal.LoggingServiceComponent;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.logging.TenantAwareLoggingEvent;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.logging.handler.TenantDomainSetter;

import java.util.logging.LogRecord;

/**
 * This appender will be used to capture the logs and later send to clients, if
 * requested via the logging web service. This maintains a circular buffer, of
 * some fixed amount (say 100).
 */
    public class CarbonMemoryAppender extends AppenderSkeleton  implements LoggingBridge {

    private CircularBuffer circularBuffer;
    private int bufferSize = -1;
    private String columnList;

    public String getColumnList() {
        return columnList;
    }

    public void setColumnList(String columnList) {
        this.columnList = columnList;
    }

    public CarbonMemoryAppender() {
    }

    public CarbonMemoryAppender(CircularBuffer circularBuffer) {
        this.circularBuffer = circularBuffer;
    }

    protected synchronized void append(LoggingEvent loggingEvent) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        if (tenantId == MultitenantConstants.INVALID_TENANT_ID) {
            String tenantDomain = TenantDomainSetter.getTenantDomain();
            if (tenantDomain != null && !tenantDomain.equals("")) {
                try {
                    tenantId = getTenantIdForDomain(tenantDomain);
                } catch (UserStoreException e) {
                    System.err.println("Cannot find tenant id for the given tenant domain.");
                    e.printStackTrace();
                    //Ignore this exception.
                    //log.error("Cannot find tenant id for the given tenant domain.", e);
                }
            }
        }
        String appName = carbonContext.getApplicationName();
        if (appName == null) {
            appName = TenantDomainSetter.getServiceName();
        }
        Logger logger = Logger.getLogger(loggingEvent.getLoggerName());
        TenantAwareLoggingEvent tenantEvent;
        if (loggingEvent.getThrowableInformation() != null) {
            tenantEvent = new TenantAwareLoggingEvent(loggingEvent.fqnOfCategoryClass, logger,
                    loggingEvent.timeStamp, loggingEvent.getLevel(), loggingEvent.getMessage(),
                    loggingEvent.getThrowableInformation().getThrowable());
        } else {
            tenantEvent = new TenantAwareLoggingEvent(loggingEvent.fqnOfCategoryClass, logger,
                    loggingEvent.timeStamp, loggingEvent.getLevel(), loggingEvent.getMessage(),
                    null);
        }
        tenantEvent.setTenantId(Integer.toString(tenantId));
        tenantEvent.setServiceName(appName);
        if (circularBuffer != null) {
            circularBuffer.append(tenantEvent);
        }
    }

    public int getTenantIdForDomain(String tenantDomain) throws UserStoreException {
        int tenantId;
        TenantManager tenantManager = LoggingServiceComponent.getTenantManager();
        if (tenantDomain == null || tenantDomain.equals("")) {
            tenantId = MultitenantConstants.SUPER_TENANT_ID;
        } else {
            tenantId = tenantManager.getTenantId(tenantDomain);
        }
        return tenantId;
    }

    public void close() {
        // do we need to do anything here. I hope we do not need to reset the
        // queue
        // as it might still be exposed to others
    }

    public boolean requiresLayout() {
        return true;
    }

    public CircularBuffer getCircularQueue() {
        return circularBuffer;
    }

    public void setCircularBuffer(CircularBuffer circularBuffer) {
        this.circularBuffer = circularBuffer;
    }

    public void activateOptions() {
        if (bufferSize < 0) {
            if (circularBuffer == null) {
                this.circularBuffer = new CircularBuffer();
            }
        } else {
            this.circularBuffer = new CircularBuffer(bufferSize);
        }
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void push(LogRecord logRecord) {
        LoggingEvent loggingEvent = LoggingUtils.getLogEvent(logRecord);
        append(loggingEvent);
    }
}
