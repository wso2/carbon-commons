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

package org.wso2.carbon.reporting.core.services;

import net.sf.jasperreports.engine.JRException;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.core.ReportParameters;
import org.wso2.carbon.reporting.core.internal.ReportingComponent;
import org.wso2.carbon.reporting.core.utils.CommonUtil;
import org.wso2.carbon.reporting.util.OMElementHandler;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * service class used to get report xml from registry
 */
public class ReportingResourcesSupplier extends AbstractAdmin {
    private static final String REPORTS_PATH = "/repository/reports/";
    private static Log log = LogFactory.getLog(ReportingResourcesSupplier.class);

    /**
     * @param componentName  name of the report request component
     * @param reportTemplate requested report template
     * @return report template
     * @throws ReportingException , if eaied to get report template
     */

    public String getReportResources(String componentName, String reportTemplate)
            throws ReportingException {
        String template;
        Registry registry;
        try {

            registry = getConfigSystemRegistry();
            template = CommonUtil.getReportResources(componentName, reportTemplate,
                                                      registry);
            return template;
        } catch (XMLStreamException e) {
            throw new ReportingException(" Failed to get report template" ,e);
        }
    }

    public String getJRXMLFileContent(String componentName, String reportTemplate) throws ReportingException{
        String template;
        Registry registry;

            try {
                registry = ReportingComponent.getRegistryService().getConfigSystemRegistry();
            } catch (RegistryException e) {
                throw new ReportingException("Failed to get registry",e);
            }
            template = CommonUtil.getJRXMLFileContent(componentName,reportTemplate, registry);
            return template;
    }

    public List<String> getAllReports() throws ReportingException {
        return CommonUtil.getAllReports(getConfigUserRegistry());

    }

    public void deleteReportTemplate(String templateName) throws ReportingException {
        CommonUtil.deleteReportTemplate(templateName, getConfigSystemRegistry());
    }

    public  boolean updateReport(String fileName, String fileContent)
            throws ReportingException, JRException {
       return CommonUtil.updateReport(fileName,fileContent,getConfigSystemRegistry());
    }

    public ReportParameters[] getReportParam(String reportName)
            throws ReportingException, XMLStreamException {

        String reportContent = getReportResources(null, reportName);
        List<ReportParameters> parameterList = new ArrayList<ReportParameters>();
        ReportParameters[] parametersArray;
        if (reportContent != null) {
            OMElement reportTemplateOmElement = OMElementHandler.createOMElement(reportContent);
            if (reportTemplateOmElement != null) {
                Iterator iterator = reportTemplateOmElement.getChildrenWithName(new QName("parameter"));
                if (iterator != null) {

                    while (iterator.hasNext()) {
                        ReportParameters parameter = new ReportParameters();
                        OMElement omElement = (OMElement) iterator.next();
                        String paramName = omElement.getAttribute(new QName("name")).getAttributeValue();
                        parameter.setParamName(paramName);
                        String paramValue = omElement.getAttribute(new QName("class")).getAttributeValue();
                        parameter.setParamValue(paramValue);
                        parameterList.add(parameter);
                    }
                }
            }
          parametersArray =  parameterList.toArray(new ReportParameters[parameterList.size()]);

        } else {
            log.error("Did not find a report called " + reportName);
            return null;
        }
        return parametersArray;
    }
}
