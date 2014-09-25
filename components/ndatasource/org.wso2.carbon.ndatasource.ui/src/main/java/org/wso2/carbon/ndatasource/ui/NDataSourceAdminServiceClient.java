/**
 *  Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.ndatasource.ui;

import java.rmi.RemoteException;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpSession;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.ndatasource.ui.stub.NDataSourceAdminStub;
import org.wso2.carbon.ndatasource.ui.stub.NDataSourceAdminDataSourceException;
import org.wso2.carbon.ndatasource.ui.stub.core.services.xsd.WSDataSourceMetaInfo;
import org.wso2.carbon.ndatasource.ui.stub.core.services.xsd.WSDataSourceInfo;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

public class NDataSourceAdminServiceClient {

	private NDataSourceAdminStub stub = null;

	private static Log log = LogFactory.getLog(NDataSourceAdminServiceClient.class);

	public NDataSourceAdminServiceClient(String cookie, String backendServerURL,
			ConfigurationContext configCtx) throws AxisFault {
		String serviceURL = backendServerURL + "NDataSourceAdmin";
		stub = new NDataSourceAdminStub(configCtx, serviceURL);
		ServiceClient client = stub._getServiceClient();
		Options option = client.getOptions();
		option.setManageSession(true);
		option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
	}

	public static NDataSourceAdminServiceClient getInstance(ServletConfig config,
			HttpSession session) throws AxisFault {
		String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
		ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
				.getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

		String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
		return new NDataSourceAdminServiceClient(cookie, backendServerURL, configContext);

	}

	public void addDataSource(WSDataSourceMetaInfo dataSourceMetaInfo) throws RemoteException,
			DataSourceException {
		validateDataSourceMetaInformation(dataSourceMetaInfo);
		if (log.isDebugEnabled()) {
			log.debug("Going to add Datasource :" + dataSourceMetaInfo.getName());
		}
		try {
			stub.addDataSource(dataSourceMetaInfo);
		} catch (NDataSourceAdminDataSourceException e) {
			if (e.getFaultMessage().getDataSourceException().isErrorMessageSpecified()) {
				handleException(e.getFaultMessage().getDataSourceException().getErrorMessage(), e);
			}
			handleException(e.getMessage(), e);
		}
	}
	
	public boolean testDataSourceConnection(WSDataSourceMetaInfo dataSourceMetaInfo) throws RemoteException, 
			DataSourceException {
		validateDataSourceMetaInformation(dataSourceMetaInfo);
		if (log.isDebugEnabled()) {
			log.debug("Going test connection of Datasource :" + dataSourceMetaInfo.getName());
		}
		try {
			return stub.testDataSourceConnection(dataSourceMetaInfo);
		} catch (NDataSourceAdminDataSourceException e) {
			if (e.getFaultMessage().getDataSourceException().isErrorMessageSpecified()) {
				handleException(e.getFaultMessage().getDataSourceException().getErrorMessage(), e);
			}
			handleException(e.getMessage(), e);
		}
		return false;
	}

	public void deleteDataSource(String dsName) throws RemoteException,
			DataSourceException {
		validateName(dsName);
		if (log.isDebugEnabled()) {
			log.debug("Going to delete a Datasource with name : " + dsName);
		}
		try {
			stub.deleteDataSource(dsName);
		} catch (NDataSourceAdminDataSourceException e) {
			if (e.getFaultMessage().getDataSourceException().isErrorMessageSpecified()) {
				handleException(e.getFaultMessage().getDataSourceException().getErrorMessage(), e);
			}
			handleException(e.getMessage(), e);
		}
	}

	public WSDataSourceInfo[] getAllDataSources() throws RemoteException,
			DataSourceException {
		WSDataSourceInfo[] allDataSources = null;
		try {
			allDataSources = stub.getAllDataSources();
		} catch (NDataSourceAdminDataSourceException e) {
			if (e.getFaultMessage().getDataSourceException().isErrorMessageSpecified()) {
				handleException(e.getFaultMessage().getDataSourceException().getErrorMessage(), e);
			}
			handleException(e.getMessage(), e);
		}
		return allDataSources;
	}

	public WSDataSourceInfo getDataSource(String dsName) throws RemoteException,
			DataSourceException {
		validateName(dsName);
		WSDataSourceInfo wsDataSourceInfo = null;
		try {
			wsDataSourceInfo = stub.getDataSource(dsName);
		} catch (NDataSourceAdminDataSourceException e) {
			if (e.getFaultMessage().getDataSourceException().isErrorMessageSpecified()) {
				handleException(e.getFaultMessage().getDataSourceException().getErrorMessage(), e);
			}
			handleException(e.getMessage(), e);
		}
		return wsDataSourceInfo;
	}

	public WSDataSourceInfo[] getAllDataSourcesForType(String dsType) throws RemoteException,
			DataSourceException {
		validateType(dsType);
		WSDataSourceInfo[] allDataSources = null;
		try {
			allDataSources = stub.getAllDataSourcesForType(dsType);
		} catch (NDataSourceAdminDataSourceException e) {
			if (e.getFaultMessage().getDataSourceException().isErrorMessageSpecified()) {
				handleException(e.getFaultMessage().getDataSourceException().getErrorMessage(), e);
			}
			handleException(e.getMessage(), e);
		}
		return allDataSources;
	}

	public String[] getDataSourceTypes() throws RemoteException,
			DataSourceException {
		String[] dataSourceTypes = null;
		try {
			dataSourceTypes = stub.getDataSourceTypes();
		} catch (NDataSourceAdminDataSourceException e) {
			if (e.getFaultMessage().getDataSourceException().isErrorMessageSpecified()) {
				handleException(e.getFaultMessage().getDataSourceException().getErrorMessage(), e);
			}
			handleException(e.getMessage(), e);
		}
		return dataSourceTypes;
	}

	public boolean reloadAllDataSources() throws RemoteException,
			DataSourceException {
		try {
			return stub.reloadAllDataSources();
		} catch (NDataSourceAdminDataSourceException e) {
			if (e.getFaultMessage().getDataSourceException().isErrorMessageSpecified()) {
				handleException(e.getFaultMessage().getDataSourceException().getErrorMessage(), e);
			}
			handleException(e.getMessage(), e);
		}
		return false;
	}

	public boolean reloadDataSource(String dsName) throws RemoteException,
			DataSourceException {
		validateName(dsName);
		try {
			return stub.reloadDataSource(dsName);
		} catch (NDataSourceAdminDataSourceException e) {
			if (e.getFaultMessage().getDataSourceException().isErrorMessageSpecified()) {
				handleException(e.getFaultMessage().getDataSourceException().getErrorMessage(), e);
			}
			handleException(e.getMessage(), e);
		}
		return false;
	}

	private static void validateDataSourceMetaInformation(WSDataSourceMetaInfo dataSourceMetaInfo) {
		if (dataSourceMetaInfo == null) {
			handleException("WSDataSourceMetaInfo can not be found.");
		}
	}

	private static void validateName(String name) {
		if (name == null || "".equals(name)) {
			handleException("Name is null or empty");
		}
	}

	private static void validateType(String type) {
		if (type == null || "".equals(type)) {
			handleException("Type is null or empty");
		}
	}

	private static void handleException(String msg) {
		log.error(msg);
		throw new IllegalArgumentException(msg);
	}
	
	private static void handleException(String msg, Exception e) throws DataSourceException{
		throw new DataSourceException(msg, e);
	}

}
