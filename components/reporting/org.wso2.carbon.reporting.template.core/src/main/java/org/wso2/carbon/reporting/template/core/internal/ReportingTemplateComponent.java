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
package org.wso2.carbon.reporting.template.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.template.core.util.Template;
import org.wso2.carbon.reporting.template.core.util.common.ReportConstants;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Component(
        name = "reporting.template",
        immediate = true)
public class ReportingTemplateComponent {

    private static RegistryService registryService;

    private static ConfigurationContextService configurationContextService;

    private static DataSourceService dataSourceService1;

    private static boolean BAMServer;

    private static URL bundleMetadataURL = null;

    private static Log log = LogFactory.getLog(ReportingTemplateComponent.class);

    @Activate
    protected void activate(ComponentContext componentContext) {

        try {
            String servername = CarbonUtils.getServerConfiguration().getProperties("Name")[0];
            BAMServer = false;
            Bundle[] bundles = componentContext.getBundleContext().getBundles();
            for (Bundle bundle : bundles) {
                if (bundle.getSymbolicName().equalsIgnoreCase("org.wso2.carbon.reporting.template.core")) {
                    if (bundle.getState() == Bundle.ACTIVE)
                        loadTemplates(bundle.getBundleContext());
                    setBundleMetadataURL(bundle.getBundleContext());
                }
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

    private void loadTemplates(BundleContext bundleContext) throws ReportingException {

        String localPath = "templates/";
        for (Template template : Template.values()) {
            URL configURL = bundleContext.getBundle().getResource(localPath + template.getTemplateName() + ".jrxml");
            if (configURL != null) {
                InputStream input = null;
                try {
                    input = configURL.openStream();
                    if (input != null) {
                        RegistryService registryService = ReportingTemplateComponent.getRegistryService();
                        Registry registry = registryService.getConfigSystemRegistry();
                        registry.beginTransaction();
                        Resource reportFilesResource = registry.newResource();
                        reportFilesResource.setContentStream(input);
                        String location = ReportConstants.REPORT_BASE_PATH + localPath + template.getTemplateName() +
                                ".jrxml";
                        registry.put(location, reportFilesResource);
                        input.close();
                        registry.commitTransaction();
                    }
                } catch (RegistryException e) {
                    throw new ReportingException("Exception occured in loading the templates", e);
                } catch (IOException e) {
                    throw new ReportingException("No content found in " + template.getTemplateName(), e);
                } finally {
                }
            } else {
                log.error("Error in loading template " + template.getTemplateName());
                throw new ReportingException("Error in loading template " + template.getTemplateName());
            }
        }
    }

    private void setBundleMetadataURL(BundleContext bundleContext) {

        bundleMetadataURL = bundleContext.getBundle().getResource("metadata/report_meta_data.xml");
    }

    public static URL getBundleMetadataURL() {

        return bundleMetadataURL;
    }

    public static RegistryService getRegistryService() {

        return registryService;
    }

    @Reference(
            name = "registry.service",
            service = org.wso2.carbon.registry.core.service.RegistryService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) throws RegistryException {

        ReportingTemplateComponent.registryService = registryService;
    }

    protected void unsetRegistryService(RegistryService registryService) {

        ReportingTemplateComponent.registryService = null;
    }

    public static ConfigurationContextService getConfigurationContextService() {

        return configurationContextService;
    }

    @Reference(
            name = "config.context.service",
            service = org.wso2.carbon.utils.ConfigurationContextService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {

        ReportingTemplateComponent.configurationContextService = configurationContextService;
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {

        ReportingTemplateComponent.configurationContextService = null;
    }

    @Reference(
            name = "org.wso2.carbon.ndatasource.core.DataSourceService",
            service = org.wso2.carbon.ndatasource.core.DataSourceService.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetCarbonDataSourceService")
    protected void setCarbonDataSourceService(DataSourceService dataSourceService) {

        if (log.isDebugEnabled()) {
            log.debug("Setting the Carbon Data Sources Service");
        }
        ReportingTemplateComponent.dataSourceService1 = dataSourceService;
    }

    protected void unsetCarbonDataSourceService(DataSourceService dataSourceService) {

        dataSourceService1 = dataSourceService;
    }

    public static DataSourceService getCarbonDataSourceService() {

        return dataSourceService1;
    }

    public static boolean isBAMServer() {

        return BAMServer;
    }
}
