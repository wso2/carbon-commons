/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.reporting.custom.ui.client;

import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.reporting.template.stub.ReportTemplateAdminReportingExceptionException;
import org.wso2.carbon.reporting.template.stub.ReportTemplateAdminStub;

import java.rmi.RemoteException;
/*
This class is client which communicates with ReportTemplateAdmin services

 */
public class ReportTemplateClient {
    private ReportTemplateAdminStub stub;

   public ReportTemplateClient(ConfigurationContext configCtx, String backendServerURL, String cookie) throws Exception{
		String serviceURL = backendServerURL + "ReportTemplateAdmin";
        stub = new ReportTemplateAdminStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

	}


    public boolean isReportTemplate(String reportName) throws ReportTemplateAdminReportingExceptionException, RemoteException {
       return stub.isReportExists(reportName);
    }

    public void deleteReportInfo(String reportName) throws ReportTemplateAdminReportingExceptionException, RemoteException {
        stub.deleteReport(reportName);
    }
}
