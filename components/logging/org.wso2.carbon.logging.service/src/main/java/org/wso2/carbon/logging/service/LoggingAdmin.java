/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.wso2.carbon.logging.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;
import org.apache.log4j.net.SyslogAppender;
import org.wso2.carbon.logging.service.data.LogData;
import org.wso2.carbon.logging.service.data.AppenderData;
import org.wso2.carbon.logging.service.data.LoggerData;
import org.wso2.carbon.logging.service.data.SyslogData;
import org.wso2.carbon.logging.config.SyslogConfigManager;
import org.wso2.carbon.logging.registry.RegistryManager;
import org.wso2.carbon.logging.util.LoggingConstants;
import org.wso2.carbon.logging.util.LoggingUtil;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.Collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.io.File;
import java.io.IOException;

/**
 * This is the Admin service used for obtaining Log4J information about the system and also used for
 * managing the system Log4J configuration
 */
public class LoggingAdmin {
    private static final Log log = LogFactory.getLog(LoggingAdmin.class);

    private static String[] logLevels = new String[]{"OFF", "TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL"};

    private RegistryManager registryManager;

    public LoggingAdmin() throws Exception {
        registryManager = new RegistryManager();
    }

    /**
     * Get all the information regarding the system log4j information but Logger information
     * such as logger, parent logger, effective level and additivity. As this information is
     * abundant, rendering SOAP env will be expensive and time consuming.
     *
     * @return System log information
     * @throws Exception
     */
    public LogData getSystemLog() throws Exception {
        boolean  isLogFileFound = Boolean.valueOf(LoggingUtil.getConfigurationProperty(
                LoggingConstants.LOG4J_FILE_FOUND));
        if(!isLogFileFound){
            throw new Exception("Log4j.properties file not found, please put a log4j.properties file to the classpath");
        }

        LogData logData = new LogData();

        // loading initial data
        logData.setLogLevel(LoggingUtil.getSystemLogLevel());
        logData.setLogPattern(LoggingUtil.getSystemLogPattern());
        
        // set the appenders
        AppenderData[] appenderData = getAllAppenderData();
        if (appenderData.length > 0) {
            logData.setAppenderData(appenderData);
            logData.setSelectedAppenderData(appenderData[0]);
        }
        return logData;
    }

    public LoggerData[] getAllLoggerData(String logNameFilter, boolean beginsWith) {
        if (logNameFilter != null) {
            logNameFilter = logNameFilter.trim();
        }
        Enumeration loggers = LogManager.getCurrentLoggers();
        List<LoggerData> list = new ArrayList<LoggerData>();
        while (loggers.hasMoreElements()) {
            Logger logger = (Logger) loggers.nextElement();
            if ((logNameFilter != null && beginsWith && logger.getName().startsWith(logNameFilter)) || // Logger name begins with logNameFilter
                    (logNameFilter != null && !beginsWith && logger.getName().indexOf(logNameFilter) != -1) || // Logger name contains logNameFilter
                    (logNameFilter == null || logNameFilter.trim().length() == 0)) {  // No logNameFilter specified
                String parentName =
                        (logger.getParent() == null ? "-" : logger.getParent().getName());
                LoggerData loggerData = new LoggerData(logger.getName(),
                        logger.getEffectiveLevel().toString(),
                        logger.getAdditivity(),
                        parentName);
                list.add(loggerData);
            }
        }
        Collections.sort(list,
                new Comparator<LoggerData>() {
                    public int compare(LoggerData arg0, LoggerData arg1) {
                        return arg0.getName().compareTo(arg1.getName());
                    }
                });
        Logger rootLogger = LogManager.getRootLogger();
        if ((logNameFilter != null && beginsWith && rootLogger.getName().startsWith(logNameFilter)) || // Logger name begins with logNameFilter
                (logNameFilter != null && !beginsWith && rootLogger.getName().indexOf(logNameFilter) != -1) || // Logger name contains logNameFilter
                (logNameFilter == null || logNameFilter.trim().length() == 0)) {  // No logNameFilter specified
            LoggerData loggerData = new LoggerData(rootLogger.getName(),
                    rootLogger.getEffectiveLevel().toString(),
                    rootLogger.getAdditivity(),
                    "-");
            list.add(0, loggerData);
        }
        return list.toArray(new LoggerData[list.size()]);
    }

    /**
     * @param appenderName The name of the appender
     * @return The appender information the given appender with name <code>appenderName</code>
     */
    public AppenderData getAppenderData(String appenderName) {
        Logger rootLogger = Logger.getRootLogger();
        Appender targetAppender;
        if (appenderName == null || appenderName.length() == 0) {
            targetAppender = getTheFirstAppenderInLogger(rootLogger);
        } else {
            targetAppender = getAppenderInLoggerWithName(rootLogger, appenderName);
        }

        if (targetAppender == null) {
            Enumeration loggers = LogManager.getCurrentLoggers();
            while (loggers.hasMoreElements()) {
                Logger logger = (Logger) loggers.nextElement();
                targetAppender = getAppenderInLoggerWithName(logger, appenderName);
                if (targetAppender != null) {
                    break;
                }
            }
        }
        return toAppenderData(targetAppender);
    }

    public LoggerData getLoggerData(String loggerName) {

        Logger logger = LogManager.getLogger(loggerName);
        String parentName =
                (logger.getParent() == null ? "empty" : logger.getParent().getName());
        return new LoggerData(logger.getName(),
                logger.getEffectiveLevel().toString(),
                logger.getAdditivity(),
                parentName);
    }

	public void updateSyslogConfig(String url, String port, String realm,
			String userName, String password) throws LoggingAdminException {
		try {
			registryManager.updateSyslogConfig(url, port, realm, userName,
					password);
		} catch (Exception e) {
			throw new LoggingAdminException("Cannot update the syslog config",
					e);
		}

	}
	
	public SyslogData getSyslogData() throws LoggingAdminException {
		try {
			return registryManager.getSyslogData();
		} catch (Exception e) {
			throw new LoggingAdminException("Cannot retretrieve syslog configuration",
					e);
		}
	}
	
	public String removeSyslogPattern (String appenderPattern) {
		return appenderPattern.replace(SyslogConfigManager.getSyslogPattern().trim(), "");
	}
	
	private String  addSyslogPattern (String appenderPattern) {
		return SyslogConfigManager.getSyslogPattern()+" "+appenderPattern;
	}
    
    /**
     * Set the Appender information. We receive all the parameters from the update appenders method
     * but we have to only update the relevent data.
     *
     * @param appenderName    The name of the Appender
     * @param appenderPattern The log pattern
     * @param threshold       The logging threshold
     * @param logFileName     log file name - Only relevant to FileAppenders
     * @param sysLogHost      The Syslog host - Only relevant to SyslogAppenders
     * @param facility        The Syslog facility - Only relevant to SyslogAppenders
     * @param persist         true - indicates persist these changes to the DB; false - indicates
     *                        make changes only in memory and do not persist the changes to DB
     * @throws org.apache.axis2.AxisFault If failure occurs during setting of these values
     */
    public void updateAllAppenderData(String appenderName,
                                      String appenderPattern,
                                      String threshold,
                                      String logFileName,
                                      String sysLogHost,
                                      String facility,
                                      boolean persist) throws Exception {

        boolean isFileAppender = false;
        boolean isSysLogAppender = false;

        // update system appender data
        Set<Appender> appenderSet = new HashSet<Appender>();
        Logger rootLogger = Logger.getRootLogger();
        addAppendersToSet(rootLogger.getAllAppenders(), appenderSet);

        Enumeration loggers = LogManager.getCurrentLoggers();
        while (loggers.hasMoreElements()) {
            Logger logger = (Logger) loggers.nextElement();
            if (logger.getLevel() != null) {
                addAppendersToSet(logger.getAllAppenders(), appenderSet);
            }
        }
        Appender appender = null;
        for (Iterator<Appender> iter = appenderSet.iterator(); iter.hasNext();) {
            appender = iter.next();
            if (appender.getName().equals(appenderName)) {
                break;
            }
        }

        if (appender != null) {
            if (appender instanceof FileAppender) {
                isFileAppender = true;
            } else if (appender instanceof SyslogAppender) {
                isSysLogAppender = true;
            }

            //Registry operations are performed before updating the actual appender in the memory
            if (isFileAppender) {
            	//TODO add syslogPattern to  appenderPattern
            	appenderPattern = addSyslogPattern(appenderPattern);
                // Check if the file is valid
                logFileName = logFileName.replace('\\', '/');
                File logFile = new File(logFileName);
                if (!logFile.isAbsolute()) {
                    if (logFileName.startsWith("./")) {
                        logFileName = logFileName.substring(2);
                    }
                    logFileName = (System.getProperty(ServerConstants.CARBON_HOME) + "/" +
                            logFileName).replace('\\', '/');
                    logFile = new File(logFileName);
                }
                if (!logFile.exists()) {
                    int lastIndex = logFileName.lastIndexOf("/");
                    String msg = "Cannot create logfile " + logFileName +
                            ". Please verify that the logging directory exists, log file name is " +
                            "valid and that you have read-write access to this file.";
                    if (lastIndex != -1) {
                        String dirName = logFileName.substring(0, lastIndex);
                        File dir = new File(dirName);
                        if (!dir.exists() && !dir.mkdirs()) {
                            throw new Exception(msg);
                        }
                    }
                    try {
                        if (!logFile.createNewFile()) {
                            throw new Exception(msg);
                        }
                    } catch (IOException e) {
                        throw new Exception(msg);
                    }
                }
                if (persist) {
                    registryManager.updateAppender(appender, appenderName, appenderPattern, threshold,
                            logFileName, null, null, true, false);
                }
            } else if (isSysLogAppender) {
                if (persist) {
                    registryManager.updateAppender(appender, appenderName, appenderPattern, threshold,
                            null, sysLogHost, facility, false, true);
                }
            } else {
                if (persist) {
                    registryManager.updateAppender(appender, appenderName, appenderPattern, threshold,
                            null, null, null, false, false);
                }
            }

            if ((appender.getLayout() != null) &&
                    (appender.getLayout() instanceof PatternLayout)) {
                ((PatternLayout) appender.getLayout()).setConversionPattern(appenderPattern);
            }

            if (appender instanceof FileAppender) {

                ((FileAppender) appender).setFile(logFileName);
                if (LoggingAdmin.log.isDebugEnabled()) {
                    LoggingAdmin.log.debug("change the logfile of the appender ==> " +
                            appender.getName() + " to " + logFileName);
                }
                ((FileAppender) appender).activateOptions();
            }

            if (appender instanceof SyslogAppender) {
                SyslogAppender syslogAppender = (SyslogAppender) appender;
                syslogAppender.setSyslogHost(sysLogHost);
                syslogAppender.setFacility(facility);
            }

            // set the threshold
            if (appender instanceof AppenderSkeleton) {
                AppenderSkeleton appenderSkeleton = (AppenderSkeleton) appender;
                appenderSkeleton.setThreshold(Level.toLevel(threshold));
                appenderSkeleton.activateOptions();
            }
        }
    }
    
    public boolean isStratosService() throws Exception {
		return LoggingUtil.isStratosService();
	}

    /**
     * Globally update the System Logging configuration. The global logging level & the log pattern
     * will be changed by this method
     *
     * @param logLevel   The global log level to be set
     * @param logPattern The global log pattern to be set
     * @param persist    true - indicates persist these changes to the DB; false - indicates make
     *                   changes only in memory and do not persist the changes to DB
     * @throws Exception
     */
    public void updateSystemLog(String logLevel, String logPattern, boolean persist) throws Exception {

        if ((logPattern == null) || (logPattern.trim().length() == 0)) {
            throw new Exception("Invalid Log Pattern");
        }

        if (!isALogLevel(logLevel)) {
            throw new Exception("Invalid Log Level");
        }

        Set<Appender> appenderSet = new HashSet<Appender>();

        if (persist) {
            registryManager.updateConfigurationProperty(LoggingConstants.SYSTEM_LOG_LEVEL,
                    logLevel);
            registryManager.updateConfigurationProperty(LoggingConstants.SYSTEM_LOG_PATTERN,
                    logPattern);
        }
        // update root logger details
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.toLevel(logLevel));
        addAppendersToSet(rootLogger.getAllAppenders(), appenderSet);

        // update logger and appender data, following are set
        // 1. log level of all the loggers to logLevel
        // 2. pattern of all the appenders to logpattern
        Enumeration loggersEnum = LogManager.getCurrentLoggers();
        Level systemLevel = Level.toLevel(logLevel);
        while (loggersEnum.hasMoreElements()) {
            Logger logger = (Logger) loggersEnum.nextElement();
            // we ignore all class level defined loggers
            addAppendersToSet(logger.getAllAppenders(), appenderSet);
            logger.setLevel(systemLevel);
        }

        //Update the logger data according stored in the registry
        Collection loggerCollection = registryManager.getLoggers();
        if (loggerCollection != null) {
            String[] loggerResourcePaths = loggerCollection.getChildren();
            for (String loggerResourcePath : loggerResourcePaths) {
                Logger logger = LogManager.getLogger(loggerResourcePath.substring(LoggingConstants.LOGGERS.length()));
                Resource loggerResource = registryManager.getLogger(loggerResourcePath);
                if (loggerResource != null && logger != null) {
                    loggerResource.setProperty(LoggingConstants.LoggerProperties.LOG_LEVEL, logLevel);
                }
            }
        }

        Layout patternLayout = new PatternLayout(logPattern);
        for(Appender appender:appenderSet){
            if (appender instanceof AppenderSkeleton) {
                AppenderSkeleton appenderSkeleton = (AppenderSkeleton) appender;
                appenderSkeleton.setThreshold(systemLevel);
                appenderSkeleton.setLayout(patternLayout);
                appenderSkeleton.activateOptions();
            }
        }

        // update the appender data stored in the registry
        Collection appenderCollection = registryManager.getAppenders();
        if (appenderCollection != null) {
            String[] appenderResourcePaths = appenderCollection.getChildren();
            for (String appenderResourcePath : appenderResourcePaths) {
                Appender appender = LoggingUtil.getAppenderFromSet(appenderSet, appenderResourcePath.substring(
                        LoggingConstants.APPENDERS.length()));
                Resource appenderResource = registryManager.getAppender(appenderResourcePath);
                if (appenderResource != null && appender != null) {
                    if (appender instanceof AppenderSkeleton) {
                        appenderResource.setProperty(LoggingConstants.AppenderProperties.THRESHOLD, logLevel);
                        appenderResource.setProperty(LoggingConstants.AppenderProperties.PATTERN, logPattern);
                    }
                }
            }
        }
    }

    public void updateLoggerData(String loggerName,
                                 String loggerLevel,
                                 boolean additivity,
                                 boolean persist) throws Exception {

        //update logger data in current system
        Logger logger = LogManager.getLogger(loggerName);

        if (logger != null) {
            if (persist) {
                registryManager.updateLogger(loggerName, loggerLevel, additivity);
            }

            logger.setLevel(Level.toLevel(loggerLevel));
            logger.setAdditivity(additivity);
            if (LoggingAdmin.log.isDebugEnabled()) {
                LoggingAdmin.log.debug("Set the log level of logger ==>" + logger.getName() +
                        " to " + logger.getLevel().toString());
            }
        }
    }

    public void restoreDefaults() throws Exception {
        LoggingUtil.restoreDefaults();
    }

    private AppenderData[] getAllAppenderData() {
        Set<Appender> appenderSet = new HashSet<Appender>();
        Logger rootLogger = Logger.getRootLogger();
        Enumeration appenders = rootLogger.getAllAppenders();
        addAppendersToSet(appenders, appenderSet);

        Enumeration loggers = LogManager.getCurrentLoggers();
        while (loggers.hasMoreElements()) {
            Logger logger = (Logger) loggers.nextElement();
            addAppendersToSet(logger.getAllAppenders(), appenderSet);
        }
        AppenderData[] appenderDataArray = new AppenderData[appenderSet.size()];
        int i = 0;
        for (Iterator<Appender> iterator = appenderSet.iterator(); iterator.hasNext();) {
            appenderDataArray[i] = toAppenderData(iterator.next());
            i++;
        }
        Arrays.sort(appenderDataArray,
                new Comparator() {
                    public int compare(Object arg0, Object arg1) {
                        AppenderData a = (AppenderData) arg0;
                        AppenderData b = (AppenderData) arg1;
                        return a.getName().compareTo(b.getName());
                    }
                });
        return appenderDataArray;
    }

    private void addAppendersToSet(Enumeration appenders, Set<Appender> appenderSet) {
        Appender appender;
        while (appenders.hasMoreElements()) {
            appender = (Appender) appenders.nextElement();
            appenderSet.add(appender);
            if (LoggingAdmin.log.isDebugEnabled()) {
                LoggingAdmin.log.debug("Add appender ==> " + appender.getName() + " to appender set");
            }
        }
    }

    /**
     * Convert a Log$J Appender to an instance of {@link AppenderData}
     *
     * @param targetAppender The Appender to be converted
     * @return The {@link AppenderData} instance corresponding to <code>targetAppender</code>
     */
    private AppenderData toAppenderData(Appender targetAppender) {
        AppenderData appenderData = null;
        if (targetAppender != null) {
            appenderData = new AppenderData();
            appenderData.setName(targetAppender.getName());
            Layout layout = targetAppender.getLayout();
            if (layout instanceof PatternLayout) {
                appenderData.setPattern(((PatternLayout) layout).getConversionPattern());
            }
            if (targetAppender instanceof AppenderSkeleton) {          // normally all the appenders inherit from AppenderSkelton
                AppenderSkeleton appender = (AppenderSkeleton) targetAppender;
                Priority priority = appender.getThreshold();
                if (priority != null) {
                    appenderData.setThreshold(priority.toString());
                } else {
                    appender.setThreshold(Level.toLevel(Priority.DEBUG_INT));
                    appenderData.setThreshold("DEBUG");
                }
            }
            if (targetAppender instanceof SyslogAppender) { //NOTE: Don't make this an else if
                SyslogAppender appender = (SyslogAppender) targetAppender;
                appenderData.setIsSysLogAppender(true);
                appenderData.setFacility(appender.getFacility());
                appenderData.setSysLogHost(appender.getSyslogHost());
            } else if (targetAppender instanceof FileAppender) {
                appenderData.setIsFileAppender(true);
                appenderData.setLogFile(((FileAppender) targetAppender).getFile());
            }
        }
        return appenderData;
    }

    private Appender getAppenderInLoggerWithName(Logger logger, String appenderName) {
        Enumeration appenders = logger.getAllAppenders();
        Appender targetAppender = null;
        while (appenders.hasMoreElements()) {
            Appender appender = (Appender) appenders.nextElement();
            if (appender.getName().equals(appenderName)) {
                targetAppender = appender;
                break;
            }
        }
        return targetAppender;
    }

    private Appender getTheFirstAppenderInLogger(Logger logger) {
        Enumeration appenders = logger.getAllAppenders();
        Appender targetAppender = null;
        if(appenders.hasMoreElements()) {
            targetAppender = (Appender) appenders.nextElement();
        }
        return targetAppender;
    }

    private boolean isALogLevel(String logLevelToTest) {
        boolean returnValue = false;
        for (String logLevel : logLevels) {
            if (logLevel.equalsIgnoreCase(logLevelToTest))
                returnValue = true;
        }
        return returnValue;
    }
}
