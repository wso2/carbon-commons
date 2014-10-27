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
package org.wso2.carbon.logging.appender;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogRecord;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.bootstrap.logging.LoggingBridge;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.conf.AgentConfiguration;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.StreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.utils.logging.LoggingUtils;
import org.wso2.carbon.logging.internal.LoggingServiceComponent;
import org.wso2.carbon.logging.util.LoggingConstants;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.logging.TenantAwareLoggingEvent;
import org.wso2.carbon.utils.logging.TenantAwarePatternLayout;
import org.wso2.carbon.utils.logging.handler.TenantDomainSetter;

/**
 * BAMLogEventAppender - appends logs to BAM
 */
public class LogEventAppender extends AppenderSkeleton implements Appender, LoggingBridge {
    private final List<TenantAwareLoggingEvent> loggingEvents = new CopyOnWriteArrayList<TenantAwareLoggingEvent>();
    private String url;
    private String password;
    private String userName;
    private String columnList;
    private int maxTolerableConsecutiveFailure;
    private int processingLimit;

    public LogEventAppender() {
        init();
    }

    /**
     * init will get called to initialize agent and start scheduling.
     */
    public void init() {

        String truststorePath = CarbonUtils.getCarbonHome()
                + "/repository/resources/security/client-truststore.jks";
        System.setProperty("javax.net.ssl.trustStore", truststorePath);
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
        scheduler.scheduleWithFixedDelay(new LogPublisherTask(), 10, 10, TimeUnit.MILLISECONDS);
    }

    private String getCurrentServerName() {
        String serverName = ServerConfiguration.getInstance().getFirstProperty("ServerKey");
        return serverName;
    }

    private String getCurrentDate() {
        Date now = new Date();
        DateFormat formatter = new SimpleDateFormat(LoggingConstants.DATE_FORMATTER);
        String formattedDate = formatter.format(now);
        return formattedDate.replace("-", ".");
    }

    private String getStacktrace(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString().trim();
    }


    public void close() {

    }

    @Override
    public void push(LogRecord record) {
        LoggingEvent loggingEvent = LoggingUtils.getLogEvent(record);
        append(loggingEvent);
    }

    @Override
    protected void append(LoggingEvent event) {
        Logger logger = Logger.getLogger(event.getLoggerName());
        TenantAwareLoggingEvent tenantEvent;
        if (event.getThrowableInformation() != null) {
            tenantEvent = new TenantAwareLoggingEvent(event.fqnOfCategoryClass, logger,
                    event.timeStamp, event.getLevel(), event.getMessage(),
                    event.getThrowableInformation().getThrowable());
        } else {
            tenantEvent = new TenantAwareLoggingEvent(event.fqnOfCategoryClass, logger,
                    event.timeStamp, event.getLevel(), event.getMessage(), null);
        }
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
        tenantEvent.setTenantId(String.valueOf(tenantId));
        String serviceName = TenantDomainSetter.getServiceName();
        String appName = carbonContext.getApplicationName();
        if (appName != null) {
            tenantEvent.setServiceName(carbonContext.getApplicationName());
        } else if (serviceName != null) {
            tenantEvent.setServiceName(serviceName);
        } else {
            tenantEvent.setServiceName("");
        }
        loggingEvents.add(tenantEvent);
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

    @Override
    public boolean requiresLayout() {
        return false;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getColumnList() {
        return columnList;
    }

    public void setColumnList(String columnList) {
        this.columnList = columnList;
    }

    public int getMaxTolerableConsecutiveFailure() {
        return maxTolerableConsecutiveFailure;
    }

    public void setMaxTolerableConsecutiveFailure(int maxTolerableConsecutiveFailure) {
        this.maxTolerableConsecutiveFailure = maxTolerableConsecutiveFailure;
    }

    public int getProcessingLimit() {
        return processingLimit;
    }

    public void setProcessingLimit(int processingLimit) {
        this.processingLimit = processingLimit;
    }

    private final class LogPublisherTask implements Runnable {
        private DataPublisher dataPublisher = null;
        private int numOfConsecutiveFailures;

        public void run() {
            try {
                for (int i = 0; i < loggingEvents.size(); i++) {
                    TenantAwareLoggingEvent tenantAwareLoggingEvent = loggingEvents.get(i);
                    if (i >= processingLimit) {
                        return;
                    }
                    publishLogEvent(tenantAwareLoggingEvent);
                    loggingEvents.remove(i);
                }
            } catch (Throwable t) {
                System.err.println("FATAL: LogEventAppender Cannot publish log events");
                t.printStackTrace();
                numOfConsecutiveFailures++;
                if(numOfConsecutiveFailures >= getMaxTolerableConsecutiveFailure()){
                    System.err.println("WARN: Number of consecutive log publishing failures reached the threshold of " +
                            getMaxTolerableConsecutiveFailure() + ". Purging log event array. Some logs will be lost.");
                    loggingEvents.clear();
                    numOfConsecutiveFailures = 0;
                }
            }
        }

        private void publishLogEvent(TenantAwareLoggingEvent event) throws ParseException,
                AgentException, MalformedURLException, AuthenticationException,
                TransportException, MalformedStreamDefinitionException,
                StreamDefinitionException,
                DifferentStreamDefinitionAlreadyDefinedException, ExecutionException {

            String streamId = "";
            String tenantId = event.getTenantId();

            if (tenantId.equals(String.valueOf(MultitenantConstants.INVALID_TENANT_ID))
                    || tenantId.equals(String.valueOf(MultitenantConstants.SUPER_TENANT_ID))) {
                tenantId = "0";
            }
            String serverKey = getCurrentServerName();
            String currDateStr = getCurrentDate();
            if (dataPublisher == null) {
            	 dataPublisher = new DataPublisher(url, userName, password);
            }
            StreamData data = StreamDefinitionCache.getStream(tenantId);
            if (currDateStr.equals(data.getDate())) {
            	streamId = data.getStreamId();
            } else {
            	  streamId = dataPublisher.defineStream("{" + "'name':'log" + "." + tenantId + "."
                          + serverKey + "." + currDateStr + "'," + "  'version':'1.0.0',"
                          + "  'nickName': 'Logs'," + "  'description': 'Logging Event',"
                          + "  'metaData':[" + "          {'name':'clientType','type':'STRING'}" + "  ],"
                          + "  'payloadData':[" + "          {'name':'tenantID','type':'STRING'},"
                          + "          {'name':'serverName','type':'STRING'},"
                          + "          {'name':'appName','type':'STRING'},"
                          + "          {'name':'logTime','type':'LONG'},"
                          + "          {'name':'priority','type':'STRING'},"
                          + "          {'name':'message','type':'STRING'},"
                          + "          {'name':'logger','type':'STRING'},"
                          + "          {'name':'ip','type':'STRING'},"
                          + "          {'name':'instance','type':'STRING'},"
                          + "          {'name':'stacktrace','type':'STRING'}" + "  ]" + "}");
            	  StreamDefinitionCache.putStream(tenantId, streamId, currDateStr);
            }
            List<String> patterns = Arrays.asList(columnList.split(","));
            String tenantID = "";
            String serverName = "";
            String appName = "";
            String logTime = "";
            String logger = "";
            String priority = "";
            String message = "";
            String stacktrace = "";
            String ip = "";
            String instance = "";
            for (String pattern : patterns) {
                String currEle = (pattern);
                TenantAwarePatternLayout patternLayout = new TenantAwarePatternLayout(currEle);
                if (currEle.equals("%T")) {
                    tenantID = patternLayout.format(event);
                    continue;
                }
                if (currEle.equals("%S")) {
                    serverName = patternLayout.format(event);
                    continue;
                }
                if (currEle.equals("%A")) {
                    appName = patternLayout.format(event);
                    if (appName == null || appName.equals("")) {
                        appName = "";
                    }
                    continue;
                }
                if (currEle.equals("%d")) {

                    logTime = patternLayout.format(event);
                    continue;
                }
                if (currEle.equals("%c")) {
                    logger = patternLayout.format(event);
                    continue;
                }
                if (currEle.equals("%p")) {
                    priority = patternLayout.format(event);
                    continue;
                }
                if (currEle.equals("%m")) {
                    message = patternLayout.format(event);
                    continue;
                }
                if (currEle.equals("%I")) {
                    instance = patternLayout.format(event);
                    continue;
                }
                if (currEle.equals("%Stacktrace")) {
                    if (event.getThrowableInformation() != null) {
                        stacktrace = getStacktrace(event.getThrowableInformation().getThrowable());
                    } else {
                        stacktrace = "";
                    }
                }
            }
            Date date;
            DateFormat formatter;
            formatter = new SimpleDateFormat(LoggingConstants.DATE_TIME_FORMATTER);
            date = formatter.parse(logTime);

            if (tenantID != null && serverName != null && logTime != null) {
                // String key = tenantID + "_" + serverName + "_" + logTime + "_"
                // + UUID.randomUUID().toString();
                if (!streamId.isEmpty()) {
                    Event logEvent = new Event(streamId, System.currentTimeMillis(),
                            new Object[]{"external"}, null, new Object[]{tenantID, serverName,
                            appName, date.getTime(), priority, message, logger, ip, instance,
                            stacktrace});
                    dataPublisher.publish(logEvent);
                }
            }
        }
    }
}

