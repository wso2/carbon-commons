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

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.reporting.custom.ui.beans.FormParameters;
import org.wso2.carbon.reporting.stub.ReportingResourcesSupplierReportingExceptionException;
import org.wso2.carbon.reporting.stub.ReportingResourcesSupplierStub;
import org.wso2.carbon.reporting.stub.core.services.ReportParameters;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the client class used for  ReportResourceSupplier service
 */
public class ReportResourceSupplierClient {
    ReportingResourcesSupplierStub stub;
    private static Log log = LogFactory.getLog(ReportResourceSupplierClient.class);
    public ReportResourceSupplierClient(String cookie,
                                       String backEndServerURL,
                                       ConfigurationContext configCtx
                                       ) throws AxisFault {
        String serviceURL = backEndServerURL + "ReportingResourcesSupplier";


        stub = new ReportingResourcesSupplierStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public static  ReportResourceSupplierClient getInstance(ServletConfig config, HttpSession session)
            throws AxisFault {
          String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(
                        CarbonConstants.CONFIGURATION_CONTEXT);

        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        return new ReportResourceSupplierClient(cookie, backendServerURL, configContext);

    }
    public String getReportResources(String componentName, String reportTemplate)
            throws RemoteException, ReportingResourcesSupplierReportingExceptionException {
        return stub.getReportResources(componentName, reportTemplate);
    }

    public String[] getAllReports() throws Exception {
        String[] reports;
        try {
            reports = stub.getAllReports();
        } catch (Exception e) {
            log.error("Failed to get report list", e);
            throw e;
        }
        return reports;
    }
    public void deleteReportTemplate(String templateName) throws Exception{
        try {
            stub.deleteReportTemplate(templateName);
        } catch (Exception e) {
            log.error("Failed to delete report template : " + templateName, e);
            throw e;
        }
    }

    public boolean updateReport(HttpServletRequest request) throws Exception {
        String payload = request.getParameter("payload");
        String fileName = request.getParameter("name");
        boolean status;

        try {
            status = stub.updateReport(fileName, payload);
        } catch (Exception e) {
            log.error("Failed to update report template : " + fileName, e);
            throw e;
        }
        return status;
      }
     public FormParameters[] getReportParam(String reportName) throws Exception {
         List<FormParameters> formParametersList = new ArrayList<FormParameters>();
         try {
             ReportParameters[] parameters = stub.getReportParam(reportName);
             if(parameters !=null){
             for(ReportParameters reportParameters : parameters){
                 FormParameters formParameters = new FormParameters();
                 formParameters.setFormName(reportParameters.getParamName());
                 formParameters.setFormValue(reportParameters.getParamValue());
                 formParametersList.add(formParameters);
             }
           }

         } catch (Exception e) {
             log.error("Failed to get report parameters for " + reportName, e);
             throw e;
         }
         return formParametersList.toArray(new FormParameters[formParametersList.size()]);
     }

}
