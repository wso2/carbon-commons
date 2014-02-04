package org.wso2.carbon.deployment.synchronizer.git.internal;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.adc.mgt.dao.xsd.RepositoryCredentials;
import org.wso2.carbon.adc.repository.information.RepositoryInformationServiceException;
import org.wso2.carbon.adc.repository.information.RepositoryInformationServiceStub;
import org.wso2.carbon.deployment.synchronizer.DeploymentSynchronizerException;

import java.rmi.RemoteException;

/**
 * Client for ReposioryInformationService. Used to get the git repo URL
 * for a given tenant Id and the cartridge type (short name)
 */
public class RepositoryInformationServiceClient {

    private static final Log log = LogFactory.getLog(RepositoryInformationServiceClient.class);
    private RepositoryInformationServiceStub repositoryInformationServiceStub;

    /**
     * Constructor
     *
     * @param epr end point reference for the RepositoryInformationServiceClient
     *
     * @throws AxisFault
     */
    public RepositoryInformationServiceClient (String epr) throws AxisFault {

        try {
            repositoryInformationServiceStub = new RepositoryInformationServiceStub(epr);

        } catch (AxisFault axisFault) {
            String errorMsg = "Repository Information Service client initialization failed " + axisFault.getMessage();
            log.error(errorMsg, axisFault);
            throw new AxisFault(errorMsg, axisFault);
        }
    }

    /**
     * Retrieves the Git Repository URL for the tenant and cartridge type
     *
     * @param tenantId id of the tenant
     * @param cartridgeType cartridge type tenant is subscribed to
     *
     * @return valid repository url if exists
     *
     * @throws Exception
     */
    public String getGitRepositoryUrl (int tenantId, String cartridgeType) throws RemoteException,
            RepositoryInformationServiceException {

        String repoUrl = null;
        try {
            repoUrl = repositoryInformationServiceStub.getRepositoryUrl(tenantId, cartridgeType);
            if(repoUrl != null) {
                repoUrl = repoUrl.trim();
            }

        } catch (RemoteException e) {
            //handleError("Repository Information Service invocation failed in querying repo url", e);
            log.error("Accessing Repository Information Service failed while querying git repo url for tenant " + tenantId, e);
            throw e;

        } catch (RepositoryInformationServiceException e) {
            //handleError("Git repository url querying failed for tenant " + tenantId, e);
            log.error("Querying git repository url failed for tenant " + tenantId, e);
            throw e;
        }
        return repoUrl;
    }

    /**
     * Retrieves the repository url, username and password
     *
     * @param tenantId id of the tenant
     * @param cartridgeType cartridge type tenant is subscribed to
     *
     * @return RepositoryCredentials
     *
     * @throws Exception
     */
    public RepositoryCredentials getJsonRepositoryInformation (int tenantId, String cartridgeType) throws RemoteException,
            RepositoryInformationServiceException {

        RepositoryCredentials repoCredentials = null;
        try {
        	// FIXME: For now, carbon cartridges cartridge alias passed as a null value
        	repoCredentials = repositoryInformationServiceStub.getRepositoryCredentials(tenantId, cartridgeType, null);

        } catch (RemoteException e) {
            //handleError("Repository Information Service invocation failed in querying repo information", e);
            log.error("Accessing Repository Information Service failed while querying git repo credentials for tenant " + tenantId, e);
            throw e;

        } catch (RepositoryInformationServiceException e) {
            //handleError("Git repository information querying failed for tenant " + tenantId, e);
            log.error("Querying git repository credentials failed for tenant " + tenantId, e);
            throw e;
        }
        return repoCredentials;
    }

    /**
     * Handles errors
     *
     * @param errorMsg error message
     * @param e Exception instace
     * @throws Exception re-throws the same exception
     */
    private void handleError (String errorMsg, Exception e) throws DeploymentSynchronizerException {
        log.error(errorMsg, e);
        throw new DeploymentSynchronizerException(errorMsg, e);
    }
}
