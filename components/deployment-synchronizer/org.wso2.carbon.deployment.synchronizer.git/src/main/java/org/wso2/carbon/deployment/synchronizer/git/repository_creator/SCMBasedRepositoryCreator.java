/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.deployment.synchronizer.git.repository_creator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.deployment.synchronizer.DeploymentSynchronizerException;
import org.wso2.carbon.deployment.synchronizer.RepositoryCreator;
import org.wso2.carbon.deployment.synchronizer.RepositoryInformation;

/**
 * SCM Manager based repository creator implementation
 */

public class SCMBasedRepositoryCreator implements RepositoryCreator {

    private static final Log log = LogFactory.getLog(SCMBasedRepositoryCreator.class);

    public RepositoryInformation createRepository(int tenantId, String baseUrl, String userName, String password)
            throws DeploymentSynchronizerException {

        /*ScmClientSession session = ScmClient.createSession(baseUrl, userName, password);
        RepositoryInformation repoInfo = null;

        User user = getUser(session, userName);
        if(user != null) {
            //already exists
            if(log.isDebugEnabled()) {
                log.debug("User already exists for tenant " + tenantId + ", url: " + baseUrl);
            }
        }
        else {
            user = createUser(session, tenantId, userName, password);
        }

        String repositoryName = "tenant_" + Integer.toString(tenantId);
        Repository repository = getRepository(session, repositoryName);
        if(repository != null) {
            //already exists
            if(log.isDebugEnabled()) {
                log.debug("Repository already exists for tenant " + tenantId + ", url: " + baseUrl);
            }
        }
        else {
             repository = createRepository(session, tenantId, baseUrl, repositoryName);
        }
        repoInfo = new GitRepositoryInformation(repository.getUrl());

        setPermissions(repository, user);

        session.close();

        return repoInfo;*/
        return null;
    }

    /**
     * Get the user specified by userName
     *
     * @param session active ScmClientSession session
     * @param userName username for user
     * @return User instance if exists, else null
     */
    /*private User getUser (ScmClientSession session, String userName) {

        return session.getUserHandler().get(userName);
    }*/

    /**
     * Create the user specified by username
     *
     * @param session active ScmClientSession session
     * @param tenantId tenant Id
     * @param username username for user
     * @param password password for user
     * @return User instance created
     */
    /*private User createUser (ScmClientSession session, int tenantId, String username, String password) {

        User user = new User(username);
        user.setPassword(password);
        user.setAdmin(true);
        session.getUserHandler().create(user);

        log.info("User created successfully for tenant " + tenantId);

        return user;
    }*/

    /**
     * Get the repository specified by name
     *
     * @param session active ScmClientSession session
     * @param repoName name of the repository
     * @return Repository instance if exists, else null
     */
    /*private Repository getRepository (ScmClientSession session, String repoName) {

        return session.getRepositoryHandler().get("git", repoName);
    }*/

    /**
     * Create the Repository speciusernamefied by repositoryName
     *
     * @param session active ScmClientSession session
     * @param tenantId tenant Id
     * @param baseUrl base url of scm-manager server
     * @param repositoryName reposiotry name
     * @return Repository instance created
     */
    /*private Repository createRepository (ScmClientSession session, int tenantId, String baseUrl, String repositoryName) {

        Repository repository = new Repository();
        repository.createUrl(baseUrl);
        repository.setType("git");
        repository.setName(repositoryName);
        session.getRepositoryHandler().create(repository);

        log.info("Repository created successfully for tenant " + tenantId);

        return repository;
    }*/

    /**
     * Set the permission for user for the repository
     *
     * @param repository Repository instance
     * @param user User instance
     */
    /*private void setPermissions (Repository repository, User user) {

        List<Permission> permissions = repository.getPermissions();
        for (Permission permission : permissions) {
            if (permission.getName().equals(user.getName())) {
                if(log.isDebugEnabled()) {
                    log.debug("Permission already set for user " + user.getName() + " for repository " +
                            repository.getName());
                }
                return;
            }
        }

        //add permission for the user relevant to git repo
        permissions.add(new Permission(user.getName(), PermissionType.OWNER));
        repository.setPermissions(permissions);
    }*/

}
