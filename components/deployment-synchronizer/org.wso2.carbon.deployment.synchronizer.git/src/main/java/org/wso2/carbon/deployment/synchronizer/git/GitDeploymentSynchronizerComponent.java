package org.wso2.carbon.deployment.synchronizer.git;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.deployment.synchronizer.ArtifactRepository;


/**
 * @scr.component name="org.wso2.carbon.deployment.synchronizer.git" immediate="true"
 */
public class GitDeploymentSynchronizerComponent {

    private static final Log log = LogFactory.getLog(GitDeploymentSynchronizerComponent.class);

    private ServiceRegistration gitDepSyncServiceRegistration;

    /**
     * Activate Git Deployment Synchronizer Component
     *
     * @param context ComponentContext instance to access osgi runtime
     */
    protected void activate(ComponentContext context) {

        ArtifactRepository gitBasedArtifactRepository = new GitBasedArtifactRepository();
        gitDepSyncServiceRegistration = context.getBundleContext().registerService(ArtifactRepository.class.getName(),
                gitBasedArtifactRepository, null);

        /*ServerConfiguration serverConf = ServerConfiguration.getInstance();
        String depSyncEnabledParam = serverConf.getFirstProperty(GitDeploymentSynchronizerConstants.ENABLED);

        //Check if deployment synchronization is enabled
        if (depSyncEnabledParam != null && depSyncEnabledParam.equals("true")) {

            //check if repository type is 'git', else no need to create GitBasedArtifactRepository instance
            String repoTypeParam = serverConf.getFirstProperty(GitDeploymentSynchronizerConstants.REPOSITORY_TYPE);
            if (repoTypeParam != null && repoTypeParam.equals(DeploymentSynchronizerConstants.REPOSITORY_TYPE_GIT)) {

                ArtifactRepository gitBasedArtifactRepository = new GitBasedArtifactRepository();
                gitDepSyncServiceRegistration = context.getBundleContext().registerService(ArtifactRepository.class.getName(),
                        gitBasedArtifactRepository, null);
            }
            else {
                if(log.isDebugEnabled()) {
                    log.debug("Git deployment synchronization disabled, GitBasedArtifactRepository instance not created");
                }
            }
        }
        else {
            if(log.isDebugEnabled()) {
                log.debug("Deployment synchronization disabled, GitBasedArtifactRepository instance not created");
            }
        }*/

        if(log.isDebugEnabled()) {
            log.debug("Git based deployment synchronizer component activated");
        }
    }

    /**
     * De-activate Git Deployment Synchronizer Component
     *
     * @param context ComponentContext instance to access osgi runtime
     */
    protected void deactivate(ComponentContext context) {

        if(gitDepSyncServiceRegistration != null){
            gitDepSyncServiceRegistration.unregister();
            gitDepSyncServiceRegistration = null;
        }

        if(log.isDebugEnabled()) {
            log.debug("Git based deployment synchronizer component deactivated");
        }
    }

}
