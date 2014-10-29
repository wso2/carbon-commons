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

package org.wso2.carbon.reporting.custom.ui.servlet;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.stub.DBReportingService;
import org.wso2.carbon.reporting.stub.core.services.ReportBean;
import org.wso2.carbon.reporting.stub.core.services.ReportParamMap;
import org.wso2.carbon.reporting.custom.ui.client.DBReportingServiceClient;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.activation.DataHandler;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BDReportServlet extends HttpServlet {
    private static Log log = LogFactory.getLog(BDReportServlet.class);

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws
                                                                                            Exception {
        String dataSource = request.getParameter("dataSource");
        String reportName = request.getParameter("reportName");
        String reportType = request.getParameter("reportType");
        String params = request.getParameter("hidden_param");

        String downloadFileName = null;

        if (reportType.equals("pdf")) {
            response.setContentType("application/pdf");
            downloadFileName = reportName + ".pdf";
        } else if (reportType.equals("excel")) {
            response.setContentType("application/vnd.ms-excel");
            downloadFileName = reportName + ".xls";
        } else if (reportType.equals("html")) {
            response.setContentType("text/html");
        } else {
            throw new ReportingException("requested report type can not be support");
        }

        if (downloadFileName != null) {
            response.setHeader("Content-Disposition", "attachment; filename=\"" + downloadFileName + "\"");
        }
        ReportBean reportBean = new ReportBean();
        reportBean.setTemplateName(reportName);
        reportBean.setReportType(reportType);
        reportBean.setDataSourceName(dataSource);


        String serverURL = CarbonUIUtil.getServerURL(request.getSession().getServletContext(), request.getSession());
        ConfigurationContext configurationContext = (ConfigurationContext) request.getSession().getServletContext().
                getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) request.getSession().getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        DBReportingServiceClient dbReportingServiceClient = null;
        try {
            dbReportingServiceClient = new DBReportingServiceClient(cookie, serverURL, configurationContext);
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
        List<ReportParamMap> reportParamMapsList = new ArrayList<ReportParamMap>();
        String[] parmCollection = params.split("\\|");

        for (String inputParam : parmCollection) {
            if (inputParam != null && !"".equals(inputParam)) {
                ReportParamMap reportParamMap = new ReportParamMap();
                String[] input = inputParam.split("\\=");
                reportParamMap.setParamKey(input[0]);
                reportParamMap.setParamValue(input[1]);
                reportParamMapsList.add(reportParamMap);
            }
        }

        try {
            DataHandler dataHandler = null;
            if (dbReportingServiceClient != null) {
                dataHandler = dbReportingServiceClient.getReport(reportBean,reportParamMapsList.toArray(new ReportParamMap[reportParamMapsList.size()]));
            }
            ServletOutputStream outputStream = response.getOutputStream();
            if (dataHandler != null) {
                dataHandler.writeTo(outputStream);
            }
        } catch (Exception e) {
            log.error("Failed to handle report request ",e);
            throw e;
        }

    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws javax.servlet.ServletException if a servlet-specific error occurs
     * @throws java.io.IOException            if an I/O error occurs
     */

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(DBReportingService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(DBReportingService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */

    public String getServletInfo() {
        return "used to generate report from data source";
    }// </editor-fold>
}
