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
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.core.ReportConstants;
import org.wso2.carbon.reporting.core.utils.ImageLoader;

import javax.activation.DataHandler;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * used to upload jrxml file
 */
public class JrxmlFileUploader extends AbstractAdmin {
    /**
     * @param fileName    name of the template
     * @param fileContent content of uploading file
     * @return states of upload
     */
    String status = "failed";

    public String uploadJrxmlFile(String fileName, String fileContent)
            throws ReportingException, JRException {
        if (!"".equals(fileContent)) {
            try {
                try {
                    // Here validate template.
                    byte[] templateContent = fileContent.getBytes();
                    InputStream inputStream = new ByteArrayInputStream(templateContent);
                    JRXmlLoader.load(inputStream);
                } catch (JRException e) {
                    throw new JRException(fileName + " is not a valide template", e);
                }
                Registry registry = getConfigSystemRegistry();
                Resource reportFilesResource = registry.newResource();
                reportFilesResource.setContent(fileContent);
                String reportPath = ReportConstants.JRXML_PATH +RegistryConstants.PATH_SEPARATOR+ fileName + ".jrxml";

                String relativePathToConfig = RegistryUtils.getRelativePathToOriginal(reportPath,
                        RegistryConstants.CONFIG_REGISTRY_BASE_PATH);
                registry.put(relativePathToConfig,reportFilesResource);
                status = "success";
            } catch (RegistryException e) {
                throw new ReportingException("Failed to upload " + "\"" + fileName + "\"", e);
            }
        } else {
            throw new ReportingException("File content empty or in-complete template");
        }
        return status;
    }

    public String uploadLogo(String imageName, String reportName, DataHandler imageContent) throws ReportingException {
        if (null != imageContent && !"".equals(imageName)) {
            ImageLoader loader = new ImageLoader();
            loader.saveImage(imageName, reportName, imageContent);
        } else {
            throw new ReportingException("File content empty or in-complete template");
        }
        return status;

    }
}

