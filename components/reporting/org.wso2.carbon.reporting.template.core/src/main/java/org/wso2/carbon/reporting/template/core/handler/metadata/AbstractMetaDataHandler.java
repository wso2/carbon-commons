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
import javax.xml.stream.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;

 /**
  * This class does the common aspects of all MetadataHandlers.
  * This loads the metadata information from the registry and and save it back.
  * If any MetaDataHandler needs to be newly developed it should extend this class.
  *
  * */

public abstract class AbstractMetaDataHandler {
    private static final String REPORTS = "reports";
    protected static final String REPORT = "report";
    protected static final String TYPE = "type";
    protected static final String NAME = "name";
    protected OMElement reportsElement;


    private static Log log = LogFactory.getLog(AbstractMetaDataHandler.class);


    public AbstractMetaDataHandler() throws ReportingException {
        loadMetaData();
    }

    private void loadMetaData() throws ReportingException {
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
            } else {
                createNewMetaData();
                registry.commitTransaction();
            }
        } catch (RegistryException e) {
            throw new ReportingException("Exception occured in loading the mete-data of reports", e);
        }

    }

    private void createNewMetaData() {

        OMFactory fac = OMAbstractFactory.getOMFactory();
        reportsElement = fac.createOMElement(new QName(REPORTS));
    }

    private void loadXML(Resource resource) throws ReportingException {

        InputStream reportDefinitionOmStream;

        StAXOMBuilder stAXOMBuilder;
        try {
            reportDefinitionOmStream = resource.getContentStream();
        } catch (RegistryException e) {
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


    public void saveMetadata
            () throws ReportingException {
        try {
            RegistryService registryService = ReportingTemplateComponent.getRegistryService();
            Registry registry = registryService.getConfigSystemRegistry();
            registry.beginTransaction();
            Resource reportFilesResource = registry.newResource();
            reportFilesResource.setContent(reportsElement.toString());
            String location = ReportConstants.REPORT_META_DATA_PATH + ReportConstants.METADATA_FILE_NAME;
            registry.put(location, reportFilesResource);
            registry.commitTransaction();
        } catch (RegistryException e) {
            throw new ReportingException("Exception occured in loading the meta-data of reports", e);
        }
    }

    private void saveTempfile() {
        try {
            File file = new File("metadata.xml");
            FileWriter fstream = new FileWriter(file);
            BufferedWriter out = new BufferedWriter(fstream);
            XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
            reportsElement.serialize(writer, true);
            writer.flush();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }


}


