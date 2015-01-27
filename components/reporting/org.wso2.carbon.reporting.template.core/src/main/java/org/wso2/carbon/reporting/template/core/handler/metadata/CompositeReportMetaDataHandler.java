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

package org.wso2.carbon.reporting.template.core.handler.metadata;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.template.core.util.common.ReportConstants;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.LinkedHashMap;

/*
Handles metadata information about the CompositeReport
 */

public class CompositeReportMetaDataHandler extends AbstractMetaDataHandler {
    private String[] subReportNames;
    private String reportName;

    private static final String COMPOSITE_REPORT = "compositeReport";
    private static final String SUB_REPORT = "subReport";
    private static final String DS_PARAM_NAME = "dsParamName";

    private OMElement reportElement;
    private OMElement compositeElement;
    private String[] jrDatasourceNames;

    private static Log log = LogFactory.getLog(CompositeReportMetaDataHandler.class);


    public CompositeReportMetaDataHandler(String[] reportNames, String[] jrDataSource, String reportName) throws ReportingException {
        super();
        this.subReportNames = reportNames;
        this.reportName = reportName;
        this.jrDatasourceNames = jrDataSource;
    }

    public CompositeReportMetaDataHandler() throws ReportingException {
        super();
    }


    private void removeCompositeMetadata(String repName) {
        Iterator iterator = reportsElement.getChildElements();
        boolean isChartFound = false;

        while (iterator.hasNext()) {
            OMElement reportElement = (OMElement) iterator.next();
            String reportType = reportElement.getAttributeValue(new QName(TYPE));
            if (reportType.equalsIgnoreCase(ReportConstants.COMPOSITE_TYPE)) {
                Iterator compositeIterator = reportElement.getChildElements();
                while (compositeIterator.hasNext()) {
                    OMElement compositeElement = (OMElement) compositeIterator.next();
                    String reportName = compositeElement.getAttributeValue(new QName(NAME));
                    if (reportName.equalsIgnoreCase(repName)) {
                        reportElement.detach();
                        isChartFound = true;
                        return;
                    }
                }

            }

            if (isChartFound) break;
        }
    }


    public void addCompositeReportMetaData() throws ReportingException {
        removeMetaData(this.reportName);
        createReportElement();
        addSubReports();
        saveMetadata();
    }

    private void createReportElement() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        reportElement = fac.createOMElement(new QName(REPORT));
        reportElement.addAttribute(TYPE, ReportConstants.COMPOSITE_TYPE, null);
        reportsElement.addChild(reportElement);
    }

    private void addSubReports() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        compositeElement = fac.createOMElement(new QName(COMPOSITE_REPORT));
        compositeElement.addAttribute(NAME, this.reportName, null);
        reportElement.addChild(compositeElement);

        for (int i = 0; i < subReportNames.length; i++) {
            OMElement subReport = fac.createOMElement(new QName(SUB_REPORT));
            subReport.addAttribute(NAME, subReportNames[i], null);
            subReport.addAttribute(DS_PARAM_NAME, jrDatasourceNames[i], null);
            compositeElement.addChild(subReport);
        }
    }

    public LinkedHashMap<String, String> getCompositeReport(String reportName) throws ReportingException {
        compositeElement = getCompositeElement(reportName);
        if (compositeElement != null) {
            return getReports();
        } else {
            log.error("No meta information available about the composite report : " + reportName);
            throw new ReportingException("No meta information available about the report : " + reportName);
        }
    }

    private LinkedHashMap<String, String> getReports() {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        Iterator subReportIterator = compositeElement.getChildren();
        while (subReportIterator.hasNext()) {
            OMElement subReport = (OMElement) subReportIterator.next();
            String key = subReport.getAttributeValue(new QName(NAME));
            String value = subReport.getAttributeValue(new QName(DS_PARAM_NAME));
            map.put(key, value);
        }
        return map;
    }

    private OMElement getCompositeElement(String reportName) {
        Iterator iterator = reportsElement.getChildElements();

        while (iterator.hasNext()) {
            OMElement reportElement = (OMElement) iterator.next();
            String reportType = reportElement.getAttributeValue(new QName(TYPE));
            if (reportType.equalsIgnoreCase(ReportConstants.COMPOSITE_TYPE)) {
                Iterator compositeIterator = reportElement.getChildElements();
                while (compositeIterator.hasNext()) {
                    OMElement compositeElement = (OMElement) compositeIterator.next();
                    String aReportName = compositeElement.getAttributeValue(new QName(NAME));
                    if (aReportName.equalsIgnoreCase(reportName)) {
                        return compositeElement;
                    }
                }

            }

        }
        return null;
    }

    public void removeMetaData(String reportName) throws ReportingException {
        removeCompositeMetadata(reportName);
        saveMetadata();
    }


}
