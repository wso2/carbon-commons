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

package org.wso2.carbon.reporting.core.services;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.CarbonDataSource;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.core.ReportBean;
import org.wso2.carbon.reporting.core.ReportingService;
import org.wso2.carbon.reporting.core.datasource.ReportDataSourceManager;
import org.wso2.carbon.reporting.core.internal.ReportingComponent;
import org.wso2.carbon.reporting.core.utils.ImageLoader;
import org.wso2.carbon.reporting.util.JasperPrintProvider;
import org.wso2.carbon.reporting.util.ReportDataSource;
import org.wso2.carbon.reporting.util.ReportParamMap;
import org.wso2.carbon.reporting.util.ReportStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This service used to get report pointing a data source.
 */
public class DBReportingService implements ReportingService {

    private static Log log = LogFactory.getLog(DBReportingService.class);
    /*the db connection */
    private Connection connection;

    /**
     * @param reportBean     (name , type , data source)
     * @param reportParamMap required parameter map to generate report
     * @return report byte array
     * @throws ReportingException if failed to generate report from data source
     */
    public byte[] getReport(ReportBean reportBean, ReportParamMap[] reportParamMap)
            throws ReportingException, JRException {


        byte[] reportBytes = new byte[0];
        ByteArrayOutputStream byteArrayOutputStream = null;
        String template;

        String templateName = reportBean.getTemplateName();
        String dataSourceName = reportBean.getDataSourceName();
        String reportType = reportBean.getReportType();

        if (templateName == null || dataSourceName == null || reportType == null) {
            throw new ReportingException("templateName ,dataSourceName or reportType null ");
        }
        try {

            try {
                template = new ReportingResourcesSupplier().getReportResources(null, templateName);
            } catch (ReportingException e) {
                log.error("Failed to report template", e);
                throw new ReportingException("Failed get template " + templateName, e);
            }

            if (template == null) {
                throw new ReportingException("empty report template found :" + templateName);
            }

            connection = new ReportDataSourceManager().getJDBCConnection(dataSourceName);
            JasperPrint jasperPrint;
            if (connection != null) {
                try {
                    jasperPrint = new JasperPrintProvider().createJasperPrint(connection, template,
                                                                              reportParamMap);
                } catch (JRException e) {
                    throw new JRException("Can't create JasperPrint Object from " + templateName, e);
                }
            } else {
                log.error("valid connection not found to generate report");
                throw new ReportingException("db connection null");
            }
            byteArrayOutputStream = new ReportStream().getReportStream(jasperPrint, reportType);

            if (byteArrayOutputStream != null) {
                reportBytes = byteArrayOutputStream.toByteArray();
            }
        } catch (Exception e) {
            throw new ReportingException("Failed to generate report", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    String msg = "Failed to close the db connection";
                    log.error(msg, e);
                }
            }
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                    log.error("Failed to close the report stream");
                }
            }
        }
        return reportBytes;

    }

    public byte[] getJRDataSourceReport(ReportDataSource dataSource, String templateName, ReportParamMap[] parameterMap ,String reportType) throws ReportingException, JRException {
        byte[] reportBytes = new byte[0];
        ByteArrayOutputStream byteArrayOutputStream = null;
        String template;
        ImageLoader imageLoader = null;
        if (templateName == null || reportType == null) {
            throw new ReportingException("templateName ,dataSourceName or reportType null ");
        }

         try{
          try {
                template = new ReportingResourcesSupplier().getReportResources(null, templateName);
            } catch (ReportingException e) {
                log.error("Failed to report template", e);
                throw new ReportingException("Failed get template " + templateName, e);
            }

            if (template == null) {
                throw new ReportingException("empty report template found :" + templateName);
            }

        String[] subReports = getSubreportNames(template);
         String[] subReportTemplates =  null;
        if(subReports != null){
           subReportTemplates = new String[subReports.length];
            int i=0;
            for(String aSubRep : subReports){
                subReportTemplates[i] = new ReportingResourcesSupplier().getReportResources(null, aSubRep);
                imageLoader = new ImageLoader();
                imageLoader.loadTempImages(aSubRep, subReportTemplates[i]);
                i++;
            }
        }
        imageLoader = new ImageLoader();
        imageLoader.loadTempImages(templateName, template);
            JasperPrint jasperPrint;
        try {
            jasperPrint = new JasperPrintProvider().createJasperPrint(dataSource, template,parameterMap,subReports, subReportTemplates);
        } catch (JRException e) {
            throw new JRException("Can't create JasperPrint Object from " + templateName, e);
        }

        byteArrayOutputStream = new ReportStream().getReportStream(jasperPrint, reportType);

            if (byteArrayOutputStream != null) {
                reportBytes = byteArrayOutputStream.toByteArray();
            }
        imageLoader.deleteTempImages();
         }
         catch (Exception ex){
            throw new ReportingException("Failed to generate report", ex);
         }finally{
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                    log.error("Failed to close the report stream");
                }
            }
             if(imageLoader != null){
                 imageLoader.deleteTempImages();
             }

        }
        return reportBytes;
    }

     private String[] getSubreportNames(String template) throws XMLStreamException, JaxenException {
        InputStream is = new ByteArrayInputStream(template.getBytes());
         XMLInputFactory xif = XMLInputFactory.newInstance();
        XMLStreamReader reader = xif.createXMLStreamReader(is);
        StAXOMBuilder builder = new StAXOMBuilder(reader);
        OMDocument document = builder.getDocument();
        OMElement documentElement = document.getOMDocumentElement();

        AXIOMXPath xpathExpression = new AXIOMXPath("//a:subreport");
        xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
        List nodeList = xpathExpression.selectNodes(documentElement);

        ArrayList<String> repNames = new ArrayList<String>();
        if(nodeList == null | nodeList.size()<1){
            return null;
        }
        else {
           for(Object obj : nodeList){
               if(obj instanceof OMElement){
                   OMElement subReport = (OMElement)obj;
                   Iterator iterator = subReport.getChildrenWithLocalName("subreportExpression");
                   OMElement element = (OMElement)iterator.next();

                   String reportName = element.getText();
                   reportName = reportName.replaceAll("\\{", "");
                   reportName = reportName.replaceAll("\\}", "");
                   reportName = reportName.replaceAll("\\$P", "");
                   reportName = reportName.replaceAll("\\$F", "");
                   repNames.add(reportName);
               }
           }
            String[] names = new String[repNames.size()];
            names = repNames.toArray(names);
            return names;
        }
    }


    public List<String> getCarbonDataSourceNames() {
        DataSourceService dataSourceService = ReportingComponent.
                getCarbonDataSourceService();
        if (dataSourceService == null) {
            log.error("Carbon data source service is not available, returning empty list");
            return new ArrayList<String>();
        }
        try {
            ArrayList<String> dsNames = new ArrayList<String>();
            List<CarbonDataSource> dataSourceList = dataSourceService.getAllDataSources();
            for (CarbonDataSource dataSource : dataSourceList){
                dsNames.add(dataSource.getDSMInfo().getName());
            }
            return dsNames;
        } catch (DataSourceException e) {
           log.error(e.getMessage());
            return new ArrayList<String>();
        }
    }
}
