/*
*  Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.tigris.subversion.svnclientadapter.commandline.CmdLineClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.javahl.JhlClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.svnkit.SvnKitClientAdapterFactory;
import org.wso2.carbon.deployment.synchronizer.ArtifactRepository;
import org.wso2.carbon.deployment.synchronizer.subversion.util.SVNDataHolder;
import org.wso2.carbon.tomcat.api.CarbonTomcatService;

/**
 * @scr.component name="org.wso2.carbon.deployment.synchronizer.subversion" immediate="true"
 * @scr.reference name="carbon.tomcat.service"
 * interface="org.wso2.carbon.tomcat.api.CarbonTomcatService"
 * cardinality="0..1" policy="dynamic" bind="setCarbonTomcatService"
 * unbind="unsetCarbonTomcatService"
 */
public class SVNDeploymentSynchronizerComponent {

    private static final Log log = LogFactory.getLog(SVNDeploymentSynchronizerComponent.class);

    private ServiceRegistration svnDepSyncServiceRegistration;

    protected void activate(ComponentContext context) {

        boolean hasFoundValidClient = false;

        hasFoundValidClient = attemptSvnKit();

        if (!hasFoundValidClient) {
            hasFoundValidClient = attemptJavaHL();
        }

        if (!hasFoundValidClient) {
            hasFoundValidClient = attemptCommandLineClient();
        }

        if (!hasFoundValidClient) {
            String error =
                    "Could not initialize any of the SVN client adapters - " + "Required jars/libraries may be missing";
            log.debug(error);
            return;
        }

        ArtifactRepository svnBasedArtifactRepository = new SVNBasedArtifactRepository();
        svnDepSyncServiceRegistration = context.getBundleContext()
                .registerService(ArtifactRepository.class.getName(), svnBasedArtifactRepository, null);

        log.debug("SVN based deployment synchronizer component activated");
    }

    private boolean attemptCommandLineClient() {
        boolean hasFoundValidClient = false;
        try {
            log.debug("Attempting to load SVN Kit baed SVN libraries");
            CmdLineClientAdapterFactory.setup();
            hasFoundValidClient = true;
            log.debug("Command line client adapter initialized");
        } catch (Throwable t) {
            log.debug("Unable to initialize the SVN Kit client adapter - Required jars may be missing");
        }
        return hasFoundValidClient;
    }

    private boolean attemptJavaHL() {
        boolean hasFoundValidClient = false;
        try {
            log.debug("Attempting to load Java-HL based SVN libraries");
            JhlClientAdapterFactory.setup();
            hasFoundValidClient = true;
            log.debug("Java HL client adapter initialized");
        } catch (Throwable t) {
            log.debug(
                    "Unable to initialize the Java HL client adapter - Required jars or the native libraries may be missing");
        }
        return hasFoundValidClient;
    }

    private boolean attemptSvnKit() {
        boolean hasFoundValidClient = false;
        try {
            log.debug("Attempting to load command line based SVN libraries");
            SvnKitClientAdapterFactory.setup();
            hasFoundValidClient = true;
            log.debug("SVN Kit client adapter initialized");
        } catch (Throwable t) {
            log.debug("Unable to initialize the SVN Kit client adapter - Required jars may be missing", t);
        }
        return hasFoundValidClient;
    }

    protected void deactivate(ComponentContext context) {

        if (svnDepSyncServiceRegistration != null) {
            svnDepSyncServiceRegistration.unregister();
            svnDepSyncServiceRegistration = null;
        }

        log.debug("SVN based deployment synchronizer component deactivated");
    }

    protected void setCarbonTomcatService(CarbonTomcatService carbonTomcatService) {
        SVNDataHolder.getInstance().setCarbonTomcatService(carbonTomcatService);
    }

    protected void unsetCarbonTomcatService(CarbonTomcatService carbonTomcatService) {
        SVNDataHolder.getInstance().setCarbonTomcatService(null);
    }
}
