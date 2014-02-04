package org.wso2.carbon.application.deployer.cep.internal;


import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;

import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.application.deployer.AppDeployerConstants;
import org.wso2.carbon.application.deployer.AppDeployerUtils;
import org.wso2.carbon.application.deployer.Feature;

import org.wso2.carbon.application.deployer.cep.CEPAppDeployer;
import org.wso2.carbon.application.deployer.handler.AppDeploymentHandler;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * @scr.component name="application.deployer.cep" immediate="true"
 */
public class CEPAppDeployerDSComponent {

    private static Log log = LogFactory.getLog(CEPAppDeployerDSComponent.class);

    private static Map<String, List<Feature>> requiredFeatures;

    private static ServiceRegistration appHandlerRegistration;

    protected void activate(ComponentContext ctxt) {
        try {
            // Register CEP deployer as an OSGi service
            CEPAppDeployer cepDeployer = new CEPAppDeployer();
            appHandlerRegistration = ctxt.getBundleContext().registerService(
                    AppDeploymentHandler.class.getName(), cepDeployer, null);

            // read required-features.xml
            URL reqFeaturesResource = ctxt.getBundleContext().getBundle()
                    .getResource(AppDeployerConstants.REQ_FEATURES_XML);
            if (reqFeaturesResource != null) {
                InputStream xmlStream = reqFeaturesResource.openStream();
                requiredFeatures = AppDeployerUtils
                        .readRequiredFeaturs(new StAXOMBuilder(xmlStream).getDocumentElement());
            }
        } catch (Throwable e) {
            log.error("Failed to activate CEP Application Deployer", e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        // Unregister the OSGi service
        if (appHandlerRegistration != null) {
            appHandlerRegistration.unregister();
        }
    }

    public static Map<String, List<Feature>> getRequiredFeatures() {
        return requiredFeatures;
    }

}
