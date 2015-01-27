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

package org.wso2.carbon.reporting.core.utils;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.core.ReportConstants;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.FileUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * used to include common utility functions
 */
public class CommonUtil {

    private static Log log = LogFactory.getLog(CommonUtil.class);

    /**
     * @param templateName name of the report
     * @param registry     Registry
     * @throws ReportingException if failed to delete report template
     */
    public static void deleteReportTemplate(String templateName, Registry registry) throws ReportingException {
        try {
            String resourcePath = RegistryUtils.getRelativePathToOriginal(ReportConstants.JRXML_PATH,
                    RegistryConstants.CONFIG_REGISTRY_BASE_PATH);
            if (registry.resourceExists(resourcePath)) {
                registry.delete(resourcePath + RegistryConstants.PATH_SEPARATOR + templateName + ".jrxml");
            } else {
                if (log.isDebugEnabled()) {
                    log.info("no any report templates called " + templateName + " , to delete");
                }
            }
        } catch (RegistryException e) {
            throw new ReportingException("Error occurred deleting the report template : " + templateName, e);
        }


    }

    /**
     * @param registry Registry
     * @return report name list
     * @throws ReportingException if failed to get report name list
     */
    public static List<String> getAllReports(Registry registry) throws ReportingException {
        Resource resource;
        List<String> reportNames = null;
        try {

            String relativePath = RegistryUtils.getRelativePathToOriginal(ReportConstants.JRXML_PATH,
                    RegistryConstants.CONFIG_REGISTRY_BASE_PATH);
            if (registry.resourceExists(relativePath)) {
                resource = registry.get(relativePath);
                if (resource instanceof Collection) {
                    reportNames = new ArrayList<String>();
                    String[] paths = ((Collection) resource).getChildren();
                    for (String resourcePath : paths) {
                        Resource childResource = registry.get(resourcePath);

                        if (!(childResource instanceof Collection)) {
                            String name = ((ResourceImpl) childResource).getName();
                            reportNames.add(name.split(".jrxml")[0]);
                        }

                    }
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.info("no any report templates available to generate reports");
                }
            }
        } catch (RegistryException e) {
            throw new ReportingException("Error occurred getting all the reports names", e);
        }
        return reportNames;

    }

    /**
     * @param componentName  name of the report requesting component
     * @param reportTemplate name of the template
     * @param registry       Registry
     * @return report template as string
     * @throws ReportingException if failed to get report template
     */
    public static String getReportResources(String componentName, String reportTemplate, Registry registry)
            throws ReportingException, XMLStreamException {

        String jrXmlPath;
        if (reportTemplate != null && !"".equals(reportTemplate)) {
            jrXmlPath = ReportConstants.JRXML_PATH + RegistryConstants.PATH_SEPARATOR + reportTemplate + ".jrxml";
        } else {
            throw new ReportingException("Can't generate report without template ");
        }

        Resource resource;
        InputStream reportDefinitionOmStream;

        StAXOMBuilder stAXOMBuilder;
        OMElement reportJrXmlOmElement;
        try {

            resource = registry.get(RegistryUtils.getRelativePathToOriginal(jrXmlPath,
                    RegistryConstants.CONFIG_REGISTRY_BASE_PATH));
            reportDefinitionOmStream = resource.getContentStream();

        } catch (RegistryException e) {
            throw new ReportingException(reportTemplate + " getting  failed from " + componentName, e);
        }
        XMLInputFactory xmlInputFactory;
        XMLStreamReader xmlStreamReader = null;
        xmlInputFactory = XMLInputFactory.newInstance();
        try {
            xmlStreamReader = xmlInputFactory.createXMLStreamReader(reportDefinitionOmStream);
            stAXOMBuilder = new StAXOMBuilder(xmlStreamReader);
            reportJrXmlOmElement = stAXOMBuilder.getDocumentElement();
            return reportJrXmlOmElement.toString();
        } catch (XMLStreamException e) {
            throw new ReportingException(reportTemplate + " getting  failed from " + componentName, e);
        } finally {
            if (xmlStreamReader != null) {
                xmlStreamReader.close();
            }
        }

    }

    public static String getJRXMLFileContent(String componentName, String reportTemplate, Registry registry) throws ReportingException {
        String jrXmlPath;
        if (reportTemplate != null && !"".equals(reportTemplate)) {
            jrXmlPath = ReportConstants.JRXML_PATH + RegistryConstants.PATH_SEPARATOR + reportTemplate + ".jrxml";
        } else {
            throw new ReportingException("Can't generate report without template ");
        }
        Resource resource;
        InputStream reportDefinitionOmStream;
        try {
            resource = registry.get(RegistryUtils.getRelativePathToOriginal(jrXmlPath,
                    RegistryConstants.CONFIG_REGISTRY_BASE_PATH));
            reportDefinitionOmStream = resource.getContentStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(reportDefinitionOmStream));

            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            br.close();
            return sb.toString();
        } catch (RegistryException e) {
            throw new ReportingException(reportTemplate + " getting  failed from " + componentName, e);
        } catch (IOException e) {
            throw new ReportingException(reportTemplate + " failed to read");
        }
    }

    /**
     * @param fileName    name of the modifying report name
     * @param fileContent modified content
     * @param registry    Registry
     * @return status of the update process
     * @throws org.wso2.carbon.reporting.api.ReportingException
     *
     */
    public static boolean updateReport(String fileName, String fileContent, Registry registry)
            throws ReportingException, JRException {
        boolean status;
        try {
            try {
                // validate report template before updating
                byte[] fileContentBytes = fileContent.getBytes();
                InputStream inputStream = new ByteArrayInputStream(fileContentBytes);
                JRXmlLoader.load(inputStream);
            } catch (JRException e) {
                throw new JRException("This is not valid report template", e);
            }
            Resource reportFilesResource = registry.newResource();
            reportFilesResource.setContent(fileContent);
            registry.put(ReportConstants.JRXML_PATH + fileName + ".jrxml", reportFilesResource);
            status = true;
        } catch (RegistryException e) {
            throw new ReportingException("Failed to update report template " + fileName, e);
        }
        return status;
    }

    public static void addJrxmlConfigs(Registry systemRegistry) throws RegistryException {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String tenantDomain = carbonContext.getTenantDomain();
        try {
            PrivilegedCarbonContext.startTenantFlow();
            if (tenantDomain == null) {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants
                        .SUPER_TENANT_DOMAIN_NAME);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants
                        .SUPER_TENANT_ID);
            } else {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
            }
            String rxtDir = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator +
                    "resources" + File.separator + "reports";
            File file = new File(rxtDir);
            if (!file.exists()) {
                return;
            }
            //create a FilenameFilter
            FilenameFilter filenameFilter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    //if the file extension is .rxt return true, else false
                    return name.endsWith(".jrxml");
                }
            };
            String[] rxtFilePaths = file.list(filenameFilter);
            if (rxtFilePaths.length == 0) {
                return;
            }
            for (String rxtPath : rxtFilePaths) {
                String resourcePath = ReportConstants.JRXML_PATH + "/" + rxtPath;
                try {
                    if (systemRegistry.resourceExists(resourcePath)) {
                        continue;
                    }
                    String rxt = FileUtil.readFileToString(rxtDir + File.separator + rxtPath);
                    Resource resource = systemRegistry.newResource();
                    resource.setContent(rxt.getBytes());
                    resource.setMediaType("application/xml");
                    systemRegistry.put(resourcePath, resource);
                } catch (IOException e) {
                    String msg = "Failed to read rxt files";
                    throw new RegistryException(msg, e);
                } catch (RegistryException e) {
                    String msg = "Failed to add rxt to registry ";
                    throw new RegistryException(msg, e);
                }
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

}
