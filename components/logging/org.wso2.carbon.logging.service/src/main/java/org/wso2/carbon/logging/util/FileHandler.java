package org.wso2.carbon.logging.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.logging.config.LoggingConfigManager;
import org.wso2.carbon.logging.internal.LoggingServiceComponent;
import org.wso2.carbon.logging.service.LogViewerException;
import org.wso2.carbon.logging.service.data.LogInfo;
import org.wso2.carbon.logging.service.data.LoggingConfig;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.CarbonUtils;

public class FileHandler {
	
	private static final Log log = LogFactory.getLog(FileHandler.class);
	private String getFileLocation(String serverURL, String logFile, String domain, String serverKey) throws
                                                                                            LogViewerException {
		String fileLocation = "";
        int tenantId = MultitenantConstants.SUPER_TENANT_ID;
        String serviceName = "";
		String lastChar = String.valueOf(serverURL.charAt(serverURL.length() - 1));
		if (lastChar.equals(LoggingConstants.URL_SEPARATOR)) { // http://my.log.server/logs/stratos/
			serverURL = serverURL.substring(0, serverURL.length() - 1);
		}
		fileLocation = serverURL.replaceAll("\\s", "%20");
		logFile = logFile.replaceAll("\\s", "%20");
        if (domain == null || domain.equals("")) {
            tenantId = CarbonContext.getCurrentContext()
                    .getTenantId();
        } else {
            try {
                tenantId = getTenantIdForDomain(domain);
            } catch (LogViewerException e) {
                throw new LogViewerException("error while getting tenant id from tenant domain", e);
            }
        }

	    String currTenant = "";
		if (tenantId == MultitenantConstants.INVALID_TENANT_ID
				|| tenantId == MultitenantConstants.SUPER_TENANT_ID) {
			currTenant = "0";
		} else {
            currTenant = String.valueOf(tenantId);
        }

        if(serverKey == null || serverKey.equals("")) {
            serviceName = getCurrentServerName();
        } else {
            serviceName = serverKey;
        }
		if (logFile != null && !logFile.equals("")) {
			return fileLocation + LoggingConstants.URL_SEPARATOR + currTenant
					+ LoggingConstants.URL_SEPARATOR + serviceName + LoggingConstants.URL_SEPARATOR
					+ logFile;
		} else {
			return fileLocation + LoggingConstants.URL_SEPARATOR + currTenant
			+ LoggingConstants.URL_SEPARATOR + serviceName;
		}
		
	}
	
	private String getCurrentServerName() {
		String serverName = ServerConfiguration.getInstance().getFirstProperty("ServerKey");
		return serverName;
	}

	private InputStream getLogDataStream(String fileName, String domain, String serverKey) throws Exception {
		LoggingConfig config = LoggingConfigManager.loadLoggingConfiguration();
		String url = "";
		// TODO this will change depending on the hive impl
		String hostUrl = config.getArchivedHost();
		url = getFileLocation(hostUrl, fileName, domain, serverKey);
		String password = config.getArchivedUser();
		String userName = config.getArchivedPassword();
		int port = Integer.parseInt(config.getArchivedPort());
		String realm = config.getArchivedRealm();
		URI uri = new URI(url);
		String host = uri.getHost();
		HttpClient client = new HttpClient();
		client.getState().setCredentials(new AuthScope(host, port, realm),
				new UsernamePasswordCredentials(userName, password));
		GetMethod get = new GetMethod(url);
		get.setDoAuthentication(true);
		client.executeMethod(get);
		return get.getResponseBodyAsStream();
	}
	
	private InputStream getHDFSLogDataStream (String fileName, String domain, String serverKey) throws LogViewerException {
	    Configuration conf = new Configuration(false);
        /**
         * Create HDFS Client configuration to use name node hosted on host master and port 9000.
         * Client configured to connect to a remote distributed file system.
         */
      //  conf.set("fs.default.name", "hdfs://master:9000");
     
        /**
         * Get connection to remote file sytem
         */
        try {
        	LoggingConfig config = LoggingConfigManager.loadLoggingConfiguration();
        	String url = "";
    		// TODO this will change depending on the hive impl
    		String hostUrl = config.getArchivedHost();
    	    conf.set("fs.default.name", hostUrl);
    	    conf.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
    		//creating the file path.
    		url = getFileLocation(config.getArchivedHDFSPath(), fileName, domain, serverKey);
			if (log.isDebugEnabled()) {
				log.debug("Connecting to hdfs file "+url);
			}
			FileSystem fs = FileSystem.get(conf);
			Path tenantFileName = new Path(url);
			if (!fs.exists(tenantFileName)) {
				if (log.isDebugEnabled()) {
					log.debug("The HDF file " + url + " does not exsist");
				}
				return null;
			}
			FSDataInputStream in = fs.open(tenantFileName);
			return in;
		} catch (IOException e) {
			log.error("Cannot find the specified hdfs file location to the log file",
					e);
			throw new LogViewerException("Cannot find the specified hdfs file location to the log file",
					e);
		}
	}

	public LogInfo[] getRemoteLogFiles(String domain, String serverKey)
			throws LogViewerException {
		String url = "";
		try {
			LoggingConfig config = LoggingConfigManager
					.loadLoggingConfiguration();
			url = getFileLocation(config.getArchivedHDFSPath(), "", domain,
					serverKey);
			Configuration conf = new Configuration(false);
			conf.set("fs.default.name", config.getArchivedHost());
			conf.set("fs.hdfs.impl",
					"org.apache.hadoop.hdfs.DistributedFileSystem");
			FileSystem fs = FileSystem.get(conf);
			Path tenantFileName = new Path(url);
			if (!fs.exists(tenantFileName)) {
				if (log.isDebugEnabled()) {
					log.debug("The HDF file " + url + " does not exsist");
				}
				return null;
			}
			FileStatus[] status = fs.listStatus(tenantFileName);
			ArrayList<LogInfo> logs = new ArrayList<LogInfo>();
			if (log.isDebugEnabled()) {
				log.debug("The retrieving log data from " + url );
			}
			for (int i = 0; i < status.length; i++) {
				Path logFilePath = new Path(status[i].getPath().toString()+"/"+status[i].getPath().getName()+".log");
				String fileName = logFilePath.getName();
				FileStatus[] newStatus = fs.listStatus(logFilePath);
				if (log.isDebugEnabled()) {
					log.debug("The retrieving log data from " + url );
				}
                if(newStatus != null) {
                    if (newStatus[0] != null) {

                        LogInfo logEvent = new LogInfo(fileName, new Date(
                                newStatus[0].getModificationTime()).toString(),
                                getFileSize(newStatus[0].getLen()));
                        logs.add(logEvent);
                    }
                }
				
				if (log.isDebugEnabled()) {
					log.debug("Retrieving "
							+ fileName
							+ " "
							+ getFileSize(status[i].getLen())
							+ " "
							+ new Date(status[i].getModificationTime())
									.toString());
				}
			}
            return getSortedLogInfo(logs.toArray(new LogInfo[logs.size()]));
		} catch (Exception e) {
			log.error("Cannot get log files from " + url, e);
			throw new LogViewerException("Cannot get log files from " + url, e);
		}
	}
	
	private boolean isLogFile(String fileName) {
		String archivePattern = "[a-zA-Z]*\\.log";
		CharSequence inputStr = fileName;
		Pattern pattern = Pattern.compile(archivePattern);
		Matcher matcher = pattern.matcher(inputStr);
		return matcher.find();
	}
	private  String getFileSize(long bytes) {
		int unit = 1024;
		if (bytes < unit)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		char pre = "KMGTPE".charAt(exp - 1);
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	private LogInfo[] getSortedLogInfo(LogInfo logs[]) {
		int maxLen = logs.length;
		if (maxLen > 0) {
			List<LogInfo> logInfoList = Arrays.asList(logs);
			Collections.sort(logInfoList, new Comparator<Object>() {
				public int compare(Object o1, Object o2) {
					LogInfo log1 = (LogInfo) o1;
					LogInfo log2 = (LogInfo) o2;
					return log1.getLogName().compareToIgnoreCase(log2.getLogName());
				}

			});
			return (LogInfo[]) logInfoList.toArray(new LogInfo[logInfoList.size()]);
		} else {
			return null;
		}
	}

	public int getLineNumbers(String logFile) throws Exception {
		InputStream logStream;

		try {
			logStream = getLocalInputStream(logFile);
		} catch (IOException e) {
			throw new LogViewerException("Cannot find the specified file location to the log file",
					e);
		} catch (Exception e) {
			throw new LogViewerException("Cannot find the specified file location to the log file",
					e);
		}
		try {
			byte[] c = new byte[1024];
			int count = 0;
			int readChars = 0;
			while ((readChars = logStream.read(c)) != -1) {
				for (int i = 0; i < readChars; ++i) {
					if (c[i] == '\n') {
						++count;
					}
				}
			}
			return count;
		} catch (IOException e) {
			throw new LogViewerException("Cannot read file size from the " + logFile, e);
		} finally {
			try {
				logStream.close();
			} catch (IOException e) {
				throw new LogViewerException("Cannot close the input stream " + logFile, e);
			}
		}
	}

	public DataHandler downloadArchivedLogFiles(String logFile, String domain, String serverKey) throws LogViewerException {
		InputStream logStream;
		try {
			logFile = logFile.replace(".log", "")+"/"+logFile;
			logStream = getHDFSLogDataStream(logFile, domain, serverKey);
			if (logStream == null) {
				return null;
			}
		} catch (Exception e) {
			throw new LogViewerException("Cannot find the specified file location to the log file",
					e);
		}
		try {
			ByteArrayDataSource bytArrayDS = new ByteArrayDataSource(logStream, "application/txt");
			DataHandler dataHandler = new DataHandler(bytArrayDS);
			return dataHandler;
		} catch (IOException e) {
			throw new LogViewerException("Cannot read file size from the " + logFile, e);
		} finally {
			try {
				logStream.close();
			} catch (IOException e) {
				log.error("Cannot close the input stream " + logFile, e);
				throw new LogViewerException("Cannot close the input stream " + logFile, e);
			}
		}
	}

	private InputStream getLocalInputStream(String logFile) throws FileNotFoundException {
		String fileName = CarbonUtils.getCarbonLogsPath() + LoggingConstants.URL_SEPARATOR
				+ logFile;
		InputStream is = new BufferedInputStream(new FileInputStream(fileName));
		return is;
	}

	public String[] getLogLinesFromFile(String logFile, int maxLogs, int start, int end)
			throws LogViewerException {
		ArrayList<String> logsList = new ArrayList<String>();
		InputStream logStream;
		if (end > maxLogs) {
			end = maxLogs;
		}
		try {
			logStream = getLocalInputStream(logFile);
		} catch (Exception e) {
			throw new LogViewerException("Cannot find the specified file location to the log file",
					e);
		}
		BufferedReader dataInput = new BufferedReader(new InputStreamReader(logStream));
		int index = 1;
		String line;
		try {
			while ((line = dataInput.readLine()) != null) {
				if (index <= end && index > start) {
					logsList.add(line);
				}
				index++;
			}
			dataInput.close();
		} catch (IOException e) {
			log.error("Cannot read the log file", e);
			throw new LogViewerException("Cannot read the log file", e);
		}
		return logsList.toArray(new String[logsList.size()]);
	}

    public int getTenantIdForDomain(String tenantDomain) throws LogViewerException {
        int tenantId;
        TenantManager tenantManager = LoggingServiceComponent.getTenantManager();
        if (tenantDomain == null || tenantDomain.equals("")) {
            tenantId = org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_ID;
        } else {

            try {
                tenantId = tenantManager.getTenantId(tenantDomain);
            } catch (UserStoreException e) {
            	log.error("Cannot find tenant id for the given tenant domain.");
                throw new LogViewerException("Cannot find tenant id for the given tenant domain.");
            }
        }
        return tenantId;
    }
}

