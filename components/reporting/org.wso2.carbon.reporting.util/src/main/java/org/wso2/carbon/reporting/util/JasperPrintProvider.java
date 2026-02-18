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
package org.wso2.carbon.reporting.util;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapArrayDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.wso2.carbon.reporting.api.ReportingException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * used for report generation JasperPrint object it is special object to Jasper Report
 */
public class JasperPrintProvider {
    /**
     * generate report ByteArrayOutputStream
     *
     * @param jrDataSource data for report
     * @param template       report template
     * @return report ByteArrayOutputStream
     * @throws ReportingException will occurred generating report
     */
    Log log = LogFactory.getLog(JasperPrintProvider.class);

    public JasperPrint createJasperPrint(Object dataSource, String template,
                                         ReportParamMap[] reportParamMap) throws JRException, ReportingException {

        // create a byte array from given report template

        byte[] templateBytes = template.getBytes();
        InputStream templateInputStream = new ByteArrayInputStream(templateBytes);
        JasperPrint jasperPrint;
        HashMap map;
        try {
            // load JasperDesign
            JasperDesign jasperDesign = JRXmlLoader.load(templateInputStream);
            // compiling JasperDesign from JasperCompileManager
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
            // generate JasperPrint Object from given JasperReport file and JRDataSource
            // by JasperFillManager
            try {
                map = getParamMap(reportParamMap, template);
            } catch (ReportingException e) {
                throw new ReportingException("Failed to get report param map", e);
            }
            if (dataSource instanceof JRDataSource) {
                jasperPrint = JasperFillManager.fillReport(jasperReport, map,
                        (JRDataSource) dataSource);

            } else if (dataSource instanceof Connection) {
                jasperPrint = JasperFillManager.fillReport(jasperReport, map,
                        (Connection) dataSource);

            } else {
                log.error("data source is not a JDBC connection or JRDataSource");
                return null;
            }
        } catch (JRException e) {
            log.error(e);
            throw new JRException("JasperPrint creation failed from ", e);
        }
        return jasperPrint;
    }

    public JasperPrint createJasperPrint(ReportDataSource dataSource, String template,
                                         ReportParamMap[] parametersMap, String[] subRepNames, String[] subReportTemplates) throws JRException, ReportingException {

        // create a byte array from given report template

        byte[] templateBytes = template.getBytes();
        InputStream templateInputStream = new ByteArrayInputStream(templateBytes);
        JasperPrint jasperPrint;
        HashMap map;
        JRDataSource jrDataSource = null;

        if (dataSource == null) {
            jrDataSource = new JREmptyDataSource();

        } else {
            jrDataSource = getJRDataSource(dataSource);
        }
        try {

            try {
                map = getParamMap(parametersMap, template);
            } catch (ReportingException e) {
                throw new ReportingException("Failed to get report param map", e);
            }

            if (subReportTemplates != null) {
                int i = 0;
                for (String aSubTemplate : subReportTemplates) {
                    InputStream subInputStream = new ByteArrayInputStream(aSubTemplate.getBytes());
                    JasperDesign jasperDesign = JRXmlLoader.load(subInputStream);
                    JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
                    log.info("Sub Report compilation completed..");
                    map.put(subRepNames[i], jasperReport);
                    i++;
                }
            }

            // load JasperDesign
            JasperDesign jasperDesign = JRXmlLoader.load(templateInputStream);
            // compiling JasperDesign from JasperCompileManager
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
            log.info("Report compilation completed..");
            // generate JasperPrint Object from given JasperReport file and JRDataSource
            // by JasperFillManager
            jasperPrint = JasperFillManager.fillReport(jasperReport, map,
                    jrDataSource);
            log.info("Report filling is completed..");

        } catch (JRException e) {
            log.error(e);
            throw new JRException("JasperPrint creation failed from ", e);
        }
        return jasperPrint;
    }


    private HashMap getParamMap(ReportParamMap[] reportParamMap, String template) throws ReportingException {

        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        Map<String, String> reportParams = new HashMap<String, String>();

        if (reportParamMap.length > 0) {
            try {
                OMElement omElement = OMElementHandler.createOMElement(template);
                Iterator<OMElement> iterator = omElement.getChildrenWithName(new QName("parameter"));

                while (iterator.hasNext()) {

                    OMElement paramOmElement = iterator.next();
                    String classValue = paramOmElement.getAttribute(new QName("class")).getAttributeValue();
                    String name = paramOmElement.getAttribute(new QName("name")).getAttributeValue();
                    reportParams.put(name, classValue);

                }
            } catch (ReportingException e) {
                throw new ReportingException("Failed to generate OmElement from template ", e);
            }

            for (ReportParamMap paramMap : reportParamMap) {
                String key = paramMap.getParamKey();
                String value = paramMap.getParamValue();
                ReportDataSource reportDataSource = paramMap.getDataSource();

                String type = reportParams.get(key);
                if (key != null && (value != null | reportDataSource != null) && type != null) {
                    if (type.equals("java.lang.String")) {
                        hashMap.put(key, value);
                    } else if (type.equals("java.lang.Integer")) {
                        hashMap.put(key, Integer.parseInt(value));
                    } else if (type.equals("java.lang.Float")) {
                        hashMap.put(key, Float.parseFloat(value));
                    } else if (type.equals("java.lang.Double")) {
                        hashMap.put(key, Double.parseDouble(value));
                    } else if (type.equals("java.lang.Boolean")) {
                        hashMap.put(key, Boolean.parseBoolean(value));
                    } else if (type.equals("java.lang.Short")) {
                        hashMap.put(key, Short.parseShort(value));
                    } else if (type.equals("java.lang.Long")) {
                        hashMap.put(type, Long.parseLong(value));
                    } else if (type.equals("java.util.Date")) {
                        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyy");
                        try {
                            Date convertedDate = dateFormat.parse(value);
                            java.sql.Timestamp timeStampDate = new Timestamp(convertedDate.getTime());
                            hashMap.put(key, timeStampDate);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                    } else if (type.equals("java.sql.Timestamp")) {
                        hashMap.put(key, java.sql.Timestamp.valueOf(value));
                    } else if (type.equals("java.sql.Time")) {
                        hashMap.put(type, java.sql.Time.valueOf(value));
                    } else if (type.equals("java.math.BigDecimal")) {
                        hashMap.put(key, new BigDecimal(value));
                    } else if (type.equals("net.sf.jasperreports.engine.JRDataSource")) {
                        if (paramMap.getDataSource() != null) {
                            JRDataSource dataSource = getJRDataSource(paramMap.getDataSource());
                            hashMap.put(key, dataSource);
                        } else {
                            throw new ReportingException("No Data source found in parameters map");
                        }
                    } else {
                        log.warn("This data type not support : " + type);
                    }
                }
            }
        }
        return hashMap;
    }


    private JRDataSource getJRDataSource(ReportDataSource dataSource) {
        HashMap[] reportRows = new HashMap[dataSource.getRows().length];
        Row[] rows = dataSource.getRows();
        JRMapArrayDataSource jrDataSource;


        for (int i = 0; i < rows.length; i++) {
            Row row = rows[i];
            HashMap hashMap = new HashMap();
            Column[] columns = row.getColumns();

            for (Column aColumn : columns) {
                String key = aColumn.getKey();
                String value = aColumn.getValue();

                String type = aColumn.getType();
                if (key != null && value != null) {
                    if (type.equals("java.lang.String")) {
                        hashMap.put(key, value);
                    } else if (type.equals("java.lang.Integer")) {
                        hashMap.put(key, Integer.parseInt(value));
                    } else if (type.equals("java.lang.Float")) {
                        hashMap.put(key, Float.parseFloat(value));
                    } else if (type.equals("java.lang.Double")) {
                        hashMap.put(key, Double.parseDouble(value));
                    } else if (type.equals("java.lang.Boolean")) {
                        hashMap.put(key, Boolean.parseBoolean(value));
                    } else if (type.equals("java.lang.Short")) {
                        hashMap.put(key, Short.parseShort(value));
                    } else if (type.equals("java.lang.Long")) {
                        hashMap.put(type, Long.parseLong(value));
                    } else if (type.equals("java.util.Date")) {
                        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyy");
                        try {
                            Date convertedDate = dateFormat.parse(value);
                            java.sql.Timestamp timeStampDate = new Timestamp(convertedDate.getTime());
                            hashMap.put(key, timeStampDate);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                    } else if (type.equals("java.sql.Timestamp")) {
                        hashMap.put(key, java.sql.Timestamp.valueOf(value));
                    } else if (type.equals("java.sql.Time")) {
                        hashMap.put(type, java.sql.Time.valueOf(value));
                    } else if (type.equals("java.math.BigDecimal")) {
                        hashMap.put(key, new BigDecimal(value));
                    } else {
                        log.warn("This data type not support : " + type);
                    }
                }
            }
            reportRows[i] = hashMap;
        }
        jrDataSource = new JRMapArrayDataSource(reportRows);
        return jrDataSource;

    }


}
