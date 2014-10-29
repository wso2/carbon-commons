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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.template.core.internal.ReportingTemplateComponent;
import org.wso2.carbon.reporting.template.core.util.common.ReportConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.Iterator;

/*
Handles the searching of metadata irrespective of type of report
*/
public class MetadataFinder {
    private static OMElement reportsElement;
    private static final String NAME = "name";
    private static final String TYPE = "type";

    private static Log log = LogFactory.getLog(MetadataFinder.class);

    private static boolean loadMetaData() throws ReportingException {
        try {
            RegistryService registryService = ReportingTemplateComponent.getRegistryService();
            Registry registry = registryService.getConfigSystemRegistry();
            registry.beginTransaction();
            String location = ReportConstants.REPORT_META_DATA_PATH + ReportConstants.METADATA_FILE_NAME;
            Resource resource = null;
            if (registry.resourceExists(location)) {
                resource = registry.get(location);
                loadXML(resource);
                registry.commitTransaction();
                return true;
            } else {
                registry.commitTransaction();
                return false;
            }
        } catch (RegistryException e) {
            log.error("Exception occurred in loading the mete-data of reports", e);
            throw new ReportingException("Exception occurred in loading the mete-data of reports", e);
        }

    }

    private static void loadXML(Resource resource) throws ReportingException {

        InputStream reportDefinitionOmStream;

        StAXOMBuilder stAXOMBuilder;
        try {
            reportDefinitionOmStream = resource.getContentStream();
        } catch (RegistryException e) {
            log.error("failed to get the metadata xml", e);
            throw new ReportingException("failed to get the metadata xml", e);
        }
        XMLInputFactory xmlInputFactory;
        XMLStreamReader xmlStreamReader = null;
        xmlInputFactory = XMLInputFactory.newInstance();
        try {
            xmlStreamReader = xmlInputFactory.createXMLStreamReader(reportDefinitionOmStream);
            stAXOMBuilder = new StAXOMBuilder(xmlStreamReader);
            reportsElement = stAXOMBuilder.getDocumentElement();
            reportsElement.build();
        } catch (XMLStreamException e) {
            throw new ReportingException("failed to get the metadata xml", e);
        }

    }

    public static boolean isMetaDataExists(String reportName) throws ReportingException {
        if (loadMetaData()) {

            Iterator iterator = reportsElement.getChildElements();

            while (iterator.hasNext()) {
                OMElement reportElement = (OMElement) iterator.next();

                Iterator reportIterator = reportElement.getChildElements();
                while (reportIterator.hasNext()) {
                    OMElement specificReportElement = (OMElement) reportIterator.next();
                    String aReportName = specificReportElement.getAttributeValue(new QName(NAME));
                    if (aReportName.equalsIgnoreCase(reportName)) {
                        return true;
                    }
                }
            }
            return false;
        } else return false;
    }

    public static String findReportType(String reportName) throws ReportingException {
        if (loadMetaData()) {
            Iterator iterator = reportsElement.getChildElements();

            while (iterator.hasNext()) {
                OMElement reportElement = (OMElement) iterator.next();
                String reportType = reportElement.getAttributeValue(new QName(TYPE));
                Iterator reportIterator = reportElement.getChildElements();
                while (reportIterator.hasNext()) {
                    OMElement specificReportElement = (OMElement) reportIterator.next();
                    String aReportName = specificReportElement.getAttributeValue(new QName(NAME));
                    if (aReportName.equalsIgnoreCase(reportName)) {
                        return reportType;
                    }
                }


            }
            return null;
        } else return null;
    }


}
