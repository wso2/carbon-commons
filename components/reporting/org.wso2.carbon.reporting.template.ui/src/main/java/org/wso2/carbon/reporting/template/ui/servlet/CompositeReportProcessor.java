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


package org.wso2.carbon.reporting.template.ui.servlet;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.reporting.template.ui.client.ReportTemplateClient;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


public class CompositeReportProcessor extends HttpServlet {
    private static Log log = LogFactory.getLog(CompositeReportProcessor.class);

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws
            Exception {
        String webContext = (String) request.getAttribute(CarbonConstants.WEB_CONTEXT);
        HttpSession session = request.getSession();
        String serverURL = CarbonUIUtil.getServerURL(getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        ReportTemplateClient client;
        String errorString = "";

        client = new ReportTemplateClient(configContext, serverURL, cookie);
        String reportname = request.getParameter("reportName");
        String[] reports = getSubReportsName(request);

        if (reports != null) {
            client.addNewCompositeReport(reports, reportname);
            response.sendRedirect("../reporting_custom/list-reports.jsp?region=region5&item=reporting_list");
        } else {
            errorString = "No reports was sleected to form the composite report";
            request.setAttribute("errorString", errorString);
            response.sendRedirect("../reporting-template/add-composite-report.jsp");
        }
    }


    private String[] getSubReportsName(HttpServletRequest request) {
        String reportNo = request.getParameter("noReports");
        if (reportNo != null) {
            String[] reportIndex = reportNo.split(",");
            ArrayList<String> reports = new ArrayList<String>();
            for (String index : reportIndex) {
                String repName = request.getParameter("report_" + index);
                reports.add(repName);
            }
            String[] temp = new String[reports.size()];
            return reports.toArray(temp);
        }
        return null;
    }


    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws javax.servlet.ServletException if a servlet-specific error occurs
     * @throws java.io.IOException            if an I/O error occurs
     */

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(ReportGenerator.class.getName()).log(Level.SEVERE, null, ex);
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

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(ReportGenerator.class.getName()).log(Level.SEVERE, null, ex);
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


