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
import org.wso2.carbon.reporting.template.stub.ReportTemplateAdminStub.ChartReportDTO;
import org.wso2.carbon.reporting.template.stub.ReportTemplateAdminStub.DataDTO;
import org.wso2.carbon.reporting.template.stub.ReportTemplateAdminStub.SeriesDTO;
import org.wso2.carbon.reporting.template.ui.client.ReportTemplateClient;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ChartDataProcessor extends HttpServlet {

    private static Log log = LogFactory.getLog(ChartDataProcessor.class);

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String webContext = (String) request.getAttribute(CarbonConstants.WEB_CONTEXT);
        HttpSession session = request.getSession();
        String serverURL = CarbonUIUtil.getServerURL(getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        ReportTemplateClient client;
        String errorString = "";
        client = new ReportTemplateClient(configContext, serverURL, cookie);

        String reportType = request.getParameter("reportType");
        String dsName = request.getParameter("datasource");
        String tableName = request.getParameter("tableName");
        String reportname = request.getParameter("reportName");

        SeriesDTO[] series = getSeries(request);
        String msg = "";
        if (reportType.contains("xy")) {
            msg = client.isValidNumberAxis(reportType, dsName, tableName, getXAxisFields(series));
           if(!msg.isEmpty()) msg = "X-Axis Fields are not compatible for the chart type : \n"+msg;
        }
        if (msg.isEmpty()) {
            msg = client.isValidNumberAxis(reportType, dsName, tableName, getYAxisFields(series));
            if(!msg.isEmpty())msg = "Y-Axis Fields are not compatible for the chart type : \n"+msg;
        }
        if (msg.equals("")) {
            ChartReportDTO chartReport = new ChartReportDTO();
            chartReport.setReportType(reportType);
            chartReport.setReportName(reportname);
            chartReport.setDsName(dsName);
            chartReport = addSeries(series, chartReport);
            session.setAttribute("chart-report", chartReport);
            response.sendRedirect("../reporting-template/chart-report-format.jsp?reportType=" + reportType);
        } else {
            request.setAttribute("errorString", msg);
            response.sendRedirect("../reporting-template/add-chart-report.jsp?reportType=" +
                    reportType + "&success=false&errorString="+msg+"&datasource="+dsName+"&tableName"+tableName+"&reportName");
        }

    }


    private SeriesDTO[] getSeries(HttpServletRequest request) {
        String strSeriesNumber = request.getParameter("noSeries");
        Integer noOfSeries = Integer.parseInt(strSeriesNumber);
        String tableName = request.getParameter("tableName");
        SeriesDTO[] allSeries = new SeriesDTO[noOfSeries];

        for (int i = 0; i < noOfSeries; i++) {
            SeriesDTO series = new SeriesDTO();
            String name = request.getParameter("series_" + (i + 1) + "_name");
            if (name == null) name = "";
            series.setName(name);

            DataDTO xData = new DataDTO();
            xData.setDsTableName(tableName);
            String colname = request.getParameter("series_" + (i + 1) + "_xData");
            xData.setDsColumnName(colname);
            series.setXdata(xData);

            DataDTO yData = new DataDTO();
            yData.setDsTableName(tableName);
            colname = request.getParameter("series_" + (i + 1) + "_yData");
            yData.setDsColumnName(colname);
            series.setYdata(yData);
            allSeries[i] = series;
        }
        return allSeries;
    }

    private ChartReportDTO addSeries(SeriesDTO[] series, ChartReportDTO chartReport) {
        for (SeriesDTO aSeries : series) {
            chartReport.addCategorySeries(aSeries);
        }
        return chartReport;
    }

    private String[] getYAxisFields(SeriesDTO[] serieses) {
        String[] yFields = new String[serieses.length];
        for (int i = 0; i < serieses.length; i++) {
            yFields[i] = serieses[i].getYdata().getDsColumnName();
        }
        return yFields;
    }

    private String[] getXAxisFields(SeriesDTO[] serieses) {
        String[] xFields = new String[serieses.length];
        for (int i = 0; i < serieses.length; i++) {
            xFields[i] = serieses[i].getXdata().getDsColumnName();
        }
        return xFields;
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


