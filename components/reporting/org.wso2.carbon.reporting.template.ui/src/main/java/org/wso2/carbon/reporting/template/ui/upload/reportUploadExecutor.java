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


package org.wso2.carbon.reporting.template.ui.upload;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.reporting.template.stub.ReportTemplateAdminReportingExceptionException;
import org.wso2.carbon.reporting.template.stub.ReportTemplateAdminStub.ColumnDTO;
import org.wso2.carbon.reporting.template.ui.client.ReportTemplateClient;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.ui.transports.fileupload.AbstractFileUploadExecutor;
import org.wso2.carbon.reporting.template.stub.ReportTemplateAdminStub.*;
import org.wso2.carbon.utils.FileItemData;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class reportUploadExecutor extends AbstractFileUploadExecutor {

    private List<FileItemData> images;
    private TableReportDTO tableReport;
    private ChartReportDTO chartReport;
    private Map<String, ArrayList<String>> formFieldsMap;
    private ReportTemplateClient client;
    private String redirect;

    public boolean execute(HttpServletRequest request, HttpServletResponse response) throws CarbonException, IOException {
        String webContext = (String) request.getAttribute(CarbonConstants.WEB_CONTEXT);

        try {
            init(request);
        } catch (Exception e) {
            throw new CarbonException("Error while initializing the upload table tableReport configuration", e);
        }
        String reportType = null;
        if(formFieldsMap.get("reportType") != null){
           reportType = formFieldsMap.get("reportType").get(0);
        }
        try {
            if(reportType == null){
              handleTableReport();
            }else {
                 handleChartReport(reportType);
            }

              if (redirect == null) {
                    response.sendRedirect("../" + webContext + "/reporting_custom/list-reports.jsp?region=region5&item=reporting_list");
                } else {
                    response.sendRedirect("../" + webContext + "/" + redirect);
                }
            return true;
        } catch (ReportTemplateAdminReportingExceptionException e) {
           response.sendRedirect("../" + webContext + "/reporting-template/table-tableReport-format.jsp?success=false" );
            return false;
        }
    }

    private void handleTableReport() throws ReportTemplateAdminReportingExceptionException, RemoteException {
      handleTableReportHeaderInformation();
             handleColumnFormat();
            client.addNewReport(tableReport);
    }

    private void handleChartReport(String reportType) throws ReportTemplateAdminReportingExceptionException, RemoteException {
        handleChartReportHeaderInformation(reportType);
        handleChartFormat();
        client.addNewReport(chartReport);
    }

    private void init(HttpServletRequest request) throws Exception {
        HttpSession session = request.getSession();
        String serverURL = CarbonUIUtil.getServerURL(session.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) session.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        client = new ReportTemplateClient(configContext, serverURL, cookie);
        Map<String, ArrayList<FileItemData>> fileItemsMap = getFileItemsMap();
        formFieldsMap = getFormFieldsMap();

        images = fileItemsMap.get("logo");

        String type = null;
        if(formFieldsMap.get("reportType") != null){
           type = formFieldsMap.get("reportType").get(0);
        }

        if(type == null){
          tableReport= (TableReportDTO)session.getAttribute("table-report");
        }
        else {
          chartReport = (ChartReportDTO)session.getAttribute("chart-report");
        }
    }

    private void handleLogo() throws ReportTemplateAdminReportingExceptionException {
         ReportHeaderInformationDTO header = null;
        if(tableReport != null){
           header = tableReport.getReportHeaderInformation();
        }else if(chartReport != null){
            header = chartReport.getReportHeaderInformation();
        }

        if (images != null && images.size() > 0)
        {
            FileItemData image = images.get(0);
            if(image.getFileItem().getContentType().contains("image/")){
            Resource resource = new Resource();
            resource.setDataHandler(image.getDataHandler());
            resource.setFileName(formFieldsMap.get("imageName").get(0));
            header.setLogo(resource);
            }
            else {
               throw new ReportTemplateAdminReportingExceptionException("Unsupported file format. Only Image can be uploaded here.");
            }
        }
    }

    private void handleTableReportHeaderInformation() throws ReportTemplateAdminReportingExceptionException {
        ReportHeaderInformationDTO header = tableReport.getReportHeaderInformation();
        if (header == null) {
            header = new ReportHeaderInformationDTO();
            tableReport.setReportHeaderInformation(header);
        }
        handleLogo();
        tableReport.setReportType("table_type_report");
        header.setTitle(formFieldsMap.get("reportTitle").get(0));
        tableReport.setBackgroundColour("#"+formFieldsMap.get("reportColor").get(0));
        header.setTitleFont(getFontStyle("reportHeader"));
    }

    private void handleChartReportHeaderInformation(String reportType) throws ReportTemplateAdminReportingExceptionException {

           ReportHeaderInformationDTO header = chartReport.getReportHeaderInformation();
           if (header == null) {
               header = new ReportHeaderInformationDTO();
               chartReport.setReportHeaderInformation(header);
           }
           handleLogo();
           chartReport.setReportType(reportType);
           header.setTitle(formFieldsMap.get("reportTitle").get(0));
           chartReport.setBackgroundColour("#"+formFieldsMap.get("reportColor").get(0));
           header.setTitleFont(getFontStyle("reportHeader"));
       }

    private FontStyleDTO getFontStyle(String elementName) {
        FontStyleDTO style = new FontStyleDTO();

        style.setFontName(formFieldsMap.get("font" + elementName + "style").get(0));
        style.setFontSize(Integer.parseInt(formFieldsMap.get("font" + elementName + "size").get(0)));
        style.setFontColor("#"+formFieldsMap.get("font" + elementName + "color").get(0));
        style.setBackgroundColour("#"+formFieldsMap.get(elementName + "BgColor").get(0));
        style.setBold(isChecked(elementName + "Bold"));
        style.setItalic(isChecked(elementName + "Italic"));
        style.setStrikeThough(isChecked(elementName + "Strike"));
        style.setUnderLine(isChecked(elementName + "underline"));
        style.setAlignment(formFieldsMap.get(elementName + "Alignment").get(0));

        return style;
    }


    private boolean isChecked(String fieldString) {
        if (formFieldsMap.get("selectedCheckBox") != null) {
            String checkedBox = formFieldsMap.get("selectedCheckBox").get(0);
            if (checkedBox != null && !checkedBox.equalsIgnoreCase("")) {
                return checkedBox.contains(fieldString);
            }
            return false;
        }
        return false;
    }

    private void handleColumnFormat() {
        ColumnDTO[] columns = tableReport.getColumns();
        ColumnDTO[] orderedCol = new ColumnDTO[columns.length];
        for (ColumnDTO column : columns) {
            column.setColumnHeaderName(formFieldsMap.get("columnHeaderName" + column.getColumnName()).get(0));
            column.setColumnFooterName(formFieldsMap.get("columnFooterName" + column.getColumnName()).get(0));
            column.setColumHeader(getFontStyle(column.getColumnName() + "Header"));
            column.setColumnFooter(getFontStyle(column.getColumnName() + "Footer"));
            column.setTableCell(getFontStyle(column.getColumnName() + "DetailCell"));
            int index = Integer.parseInt(formFieldsMap.get("columnOrder" + column.getColumnName()).get(0));
            orderedCol[index-1] = column;
        }
        tableReport.setColumns(orderedCol);
    }

     private void handleChartFormat(){
       chartReport.setChartBackColor("#"+formFieldsMap.get("chartBgColor").get(0));
       chartReport.setTitle(formFieldsMap.get("chartTitle").get(0));
       chartReport.setSubTitle(formFieldsMap.get("chartSubTitle").get(0));
       if(formFieldsMap.get("xDataLabel") != null) chartReport.setXAxisLabel(formFieldsMap.get("xDataLabel").get(0));
       if(formFieldsMap.get("yDataLabel") != null) chartReport.setYAxisLabel(formFieldsMap.get("yDataLabel").get(0));
     }
}