/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.deployment.synchronizer.subversion;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.tigris.subversion.svnclientadapter.commandline.CmdLineClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.javahl.JhlClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.svnkit.SvnKitClientAdapterFactory;
import org.wso2.carbon.deployment.synchronizer.ArtifactRepository;
import org.wso2.carbon.deployment.synchronizer.subversion.util.SVNDataHolder;
import org.wso2.carbon.tomcat.api.CarbonTomcatService;

@Component(
        name = "org.wso2.carbon.deployment.synchronizer.subversion",
        immediate = true)
public class SVNDeploymentSynchronizerComponent {

    private static final Log log = LogFactory.getLog(SVNDeploymentSynchronizerComponent.class);

    private ServiceRegistration svnDepSyncServiceRegistration;

    @Activate
    protected void activate(ComponentContext context) {

        boolean allClientsFailed = true;
        try {
            SvnKitClientAdapterFactory.setup();
            allClientsFailed = false;
            log.debug("SVN Kit client adapter initialized");
        } catch (Throwable t) {
            log.debug("Unable to initialize the SVN Kit client adapter - Required jars " + "may be missing");
        }
        try {
            JhlClientAdapterFactory.setup();
            allClientsFailed = false;
            log.debug("Java HL client adapter initialized");
        } catch (Throwable t) {
            log.debug("Unable to initialize the Java HL client adapter - Required jars " + " or the native libraries " +
                    "may be missing");
        }
        try {
            CmdLineClientAdapterFactory.setup();
            allClientsFailed = false;
            log.debug("Command line client adapter initialized");
        } catch (Throwable t) {
            log.debug("Unable to initialize the command line client adapter - SVN command " + "line tools may be " +
                    "missing");
        }
        if (allClientsFailed) {
            String error = "Could not initialize any of the SVN client adapters - " + "Required jars/libraries may be" +
                    " missing";
            log.debug(error);
            return;
        }
        ArtifactRepository svnBasedArtifactRepository = new SVNBasedArtifactRepository();
        svnDepSyncServiceRegistration = context.getBundleContext().registerService(ArtifactRepository.class.getName()
                , svnBasedArtifactRepository, null);
        log.debug("SVN based deployment synchronizer component activated");
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        if (svnDepSyncServiceRegistration != null) {
            svnDepSyncServiceRegistration.unregister();
            svnDepSyncServiceRegistration = null;
        }
        log.debug("SVN based deployment synchronizer component deactivated");
    }

    @Reference(
            name = "carbon.tomcat.service",
            service = org.wso2.carbon.tomcat.api.CarbonTomcatService.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetCarbonTomcatService")
    protected void setCarbonTomcatService(CarbonTomcatService carbonTomcatService) {

        SVNDataHolder.getInstance().setCarbonTomcatService(carbonTomcatService);
    }

    protected void unsetCarbonTomcatService(CarbonTomcatService carbonTomcatService) {

        SVNDataHolder.getInstance().setCarbonTomcatService(null);
    }
}
