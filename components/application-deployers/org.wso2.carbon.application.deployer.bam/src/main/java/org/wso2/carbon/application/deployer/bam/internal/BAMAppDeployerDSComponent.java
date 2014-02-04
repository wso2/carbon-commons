package org.wso2.carbon.application.deployer.bam.internal;

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.application.deployer.AppDeployerConstants;
import org.wso2.carbon.application.deployer.AppDeployerUtils;
import org.wso2.carbon.application.deployer.Feature;
import org.wso2.carbon.application.deployer.handler.AppDeploymentHandler;
import org.wso2.carbon.application.deployer.bam.BAMAppDeployer;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * @scr.component name="application.deployer.bam" immediate="true"
 */

public class BAMAppDeployerDSComponent {

    private static Log log = LogFactory.getLog(BAMAppDeployerDSComponent.class);
    private static ServiceRegistration appHandlerRegistration;

    protected void activate(ComponentContext ctxt) {
        try {
            //register bam deployer as an OSGi Service
            BAMAppDeployer bamDeployer = new BAMAppDeployer();
            appHandlerRegistration = ctxt.getBundleContext().registerService(
                    AppDeploymentHandler.class.getName(), bamDeployer, null);
        } catch (Throwable e) {
            log.error("Failed to activate BAM Application Deployer", e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        // Unregister the OSGi service
        if (appHandlerRegistration != null) {
            appHandlerRegistration.unregister();
        }
    }
}
