/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.reporting.custom.ui.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.reporting.stub.DBReportingServiceStub;
import org.wso2.carbon.reporting.stub.core.services.ReportBean;
import org.wso2.carbon.reporting.stub.core.services.ReportParamMap;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.activation.DataHandler;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpSession;

public class DBReportingServiceClient {
    DBReportingServiceStub stub;
    private static Log log = LogFactory.getLog(DBReportingServiceClient.class);

    public DBReportingServiceClient(String cookie,
                                    String backEndServerURL,
                                    ConfigurationContext configCtx) throws AxisFault {
        String serviceURL = backEndServerURL + "DBReportingService";
        stub = new DBReportingServiceStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

    }
    public static DBReportingServiceClient getInstance(ServletConfig config, HttpSession session)
            throws AxisFault {
         String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(
                        CarbonConstants.CONFIGURATION_CONTEXT);

        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        return new DBReportingServiceClient(cookie,backendServerURL,configContext);
    }

    public DataHandler getReport(ReportBean reportBean, ReportParamMap[] reportParamMap) throws Exception {
        DataHandler dataHandler;
        try {
            dataHandler = stub.getReport(reportBean, reportParamMap);
        } catch (java.lang.Exception e) {
            log.error("Failed to generate report", e);
            throw e;
        }
        return dataHandler;
    }

    public String[] getCarbonDataSourceNames() throws Exception {
        String[] dataSourceNames;
        try {
            dataSourceNames = stub.getCarbonDataSourceNames();
        } catch (Exception e) {
            log.error("Failed to get data sources list ", e);
            throw e;
        }
        return dataSourceNames;
    }
}
