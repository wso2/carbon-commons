/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.reporting.ui.servlet;


import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.j2ee.servlets.ImageServlet;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.reporting.api.ReportData;
import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.ui.BeanCollectionReportData;
import org.wso2.carbon.reporting.ui.client.ReportResourceSupplierClient;
import org.wso2.carbon.reporting.util.JasperPrintProvider;
import org.wso2.carbon.reporting.util.ReportParamMap;
import org.wso2.carbon.reporting.util.ReportStream;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GraphGenerateServlet extends HttpServlet {
    private static Log log = LogFactory.getLog(GraphGenerateServlet.class);
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String type = request.getParameter("reportType");
        String typeSecond = request.getParameter("reportTypeSecond");
        String dataSessionVar = request.getParameter("reportDataSession");
        String component = request.getParameter("component");
        String template = request.getParameter("template");
        String generateType;
        String downloadFileName = null;



        if (type == null) {
            type = " ";
        } else {
            typeSecond = " ";
        }

        if (type.equals("pdf") || typeSecond.equals("pdf")) {
            generateType = "pdf";
            response.setContentType("application/pdf");
            downloadFileName = template + ".pdf";
        } else if (type.equals("excel") || typeSecond.equals("excel")) {
            generateType = "excel";
            response.setContentType("application/vnd.ms-excel");
            downloadFileName = template + ".xsl";
        } else if (type.equals("html") || typeSecond.equals("html")) {
            generateType = "html";
            response.setContentType("text/html");


        } else {
            throw new ReportingException("requested report type can not be support");
        }
        if (downloadFileName != null) {
            response.setHeader("Content-Disposition", "attachment; filename=\"" + downloadFileName + "\"");
        }
        Object reportDataObject = request.getSession().getAttribute(dataSessionVar);
        if (reportDataObject == null) {
            throw new ReportingException("can't generate report , data unavailable in session ");
        }
        try {
            String serverURL = CarbonUIUtil.getServerURL(request.getSession().getServletContext(), request.getSession());
            ConfigurationContext configurationContext = (ConfigurationContext) request.getSession().getServletContext().
                    getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            String cookie = (String) request.getSession().getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

            ReportResourceSupplierClient resourceSupplierClient = new ReportResourceSupplierClient(cookie,
                    serverURL, configurationContext);

            String reportResource = resourceSupplierClient.getReportResources(component, template);
            JRDataSource jrDataSource = new BeanCollectionReportData().getReportDataSource(reportDataObject);
            JasperPrintProvider jasperPrintProvider = new JasperPrintProvider();
            JasperPrint jasperPrint = jasperPrintProvider.createJasperPrint(jrDataSource ,reportResource, new ReportParamMap[0]);
            request.getSession().setAttribute(ImageServlet.DEFAULT_JASPER_PRINT_SESSION_ATTRIBUTE,jasperPrint);
            ReportStream reportStream = new ReportStream();
            ByteArrayOutputStream outputStream =  reportStream.getReportStream(jasperPrint,generateType);
            ServletOutputStream servletOutputStream = response.getOutputStream();
            try{
            outputStream.writeTo(servletOutputStream);
            outputStream.flush();
            }finally {
                outputStream.close();
                servletOutputStream.close();
            }

        } catch (Exception e) {
            String msg = "Error occurred handling " + template + "report request from " + component;
            log(msg);
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
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(GraphGenerateServlet.class.getName()).log(Level.SEVERE, null, ex);
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
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(GraphGenerateServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
