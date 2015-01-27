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

package org.wso2.carbon.reporting.core.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.core.internal.ReportingComponent;

import javax.activation.DataHandler;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ImageLoader {
    private ArrayList<String> imageNames;
    private String imageLocation = "repository/reports/org.wso2.carbon.reporting.template/";

    public void loadTempImages(String templateName, String jrxmlContent) throws ReportingException {
        imageNames = getImageNames(jrxmlContent);
        for (String imageName : imageNames) {
            copyImagesToHome(templateName, imageName);
        }

    }

    public boolean saveImage(String imageName, String reportName, DataHandler imageContent) throws ReportingException {
        boolean success = true;
        try {
            Registry registry = ReportingComponent.getRegistryService().
                    getConfigSystemRegistry(CarbonContext.getThreadLocalCarbonContext().getTenantId());
            Resource imgResource = registry.newResource();
            imgResource.setContentStream(imageContent.getInputStream());
            imgResource.setMediaType(imageContent.getContentType());
            registry.put(imageLocation + reportName + "/" + imageName, imgResource);
        } catch (RegistryException e) {
            throw new ReportingException(e.getMessage(), e);
        } catch (IOException e) {
            throw new ReportingException(e.getMessage(), e);
        }
        return success;

    }

    private ArrayList<String> getImageNames(String jrxmlContent) throws ReportingException {
        try {
            InputStream is = new ByteArrayInputStream(jrxmlContent.getBytes());
            XMLInputFactory xif = XMLInputFactory.newInstance();
            XMLStreamReader reader = xif.createXMLStreamReader(is);
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            OMElement docElement = builder.getDocumentElement();

            AXIOMXPath xpathExpression = new AXIOMXPath("//a:title//a:band//a:image//a:imageExpression");
            xpathExpression.addNamespace("a", "http://jasperreports.sourceforge.net/jasperreports");
            List nodeList = xpathExpression.selectNodes(docElement);

            // Iterator iterator = docElement.getChildrenWithLocalName("imageExpression");
            ArrayList<String> imageNames = new ArrayList<String>();
            for (Object aNodeList : nodeList) {
                OMElement element = (OMElement) aNodeList;
                String imageName = element.getText();
                if (imageName != null && !imageName.equalsIgnoreCase("")) {
                    String imageText = imageName.replaceAll("\"", "");
                    imageNames.add(imageText);
                }

            }
            return imageNames;
        } catch (XMLStreamException e) {
            throw new ReportingException(e.getMessage(), e);
        } catch (JaxenException e) {
            throw new ReportingException(e.getMessage(), e);
        }
    }

    private void copyImagesToHome(String templateName, String imageName) throws ReportingException {
        try {
            Registry registry = ReportingComponent.getRegistryService().
                    getConfigSystemRegistry(CarbonContext.getThreadLocalCarbonContext().getTenantId());
            Resource resource;
            if (registry.resourceExists(imageLocation + templateName + "/" + imageName)) {
                resource = registry.get(imageLocation + templateName + "/" + imageName);
                InputStream inputStream = resource.getContentStream();
                File f = new File(imageName);
                OutputStream out = new FileOutputStream(f);
                byte buf[] = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) > 0)
                    out.write(buf, 0, len);
                out.close();
                inputStream.close();
            }
        } catch (RegistryException e) {
            throw new ReportingException(e.getMessage(), e);
        } catch (FileNotFoundException e) {
            throw new ReportingException(e.getMessage(), e);
        } catch (IOException e) {
            throw new ReportingException(e.getMessage(), e);
        }
    }

    public void deleteTempImages() {
        for (String imageName : imageNames) {
            File file = new File(imageName);
            file.delete();
        }
    }


}
