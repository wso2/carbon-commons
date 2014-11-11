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

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.tigris.subversion.svnclientadapter.commandline.CmdLineClientAdapter;
import org.tigris.subversion.svnclientadapter.utils.Depth;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.deployment.synchronizer.ArtifactRepository;
import org.wso2.carbon.deployment.synchronizer.internal.DeploymentSynchronizerConstants;
import org.wso2.carbon.deployment.synchronizer.DeploymentSynchronizerException;
import org.wso2.carbon.deployment.synchronizer.internal.repository.CarbonRepositoryUtils;
import org.wso2.carbon.deployment.synchronizer.internal.util.DeploymentSynchronizerConfiguration;
import org.wso2.carbon.deployment.synchronizer.internal.util.RepositoryConfigParameter;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Subversion based artifact repository can be used in conjunction with the
 * DeploymentSynchronizer to synchronize a local repository against a remote
 * SVN repository. By default this implementation does not entertain SVN
 * externals but it can be enabled if required. This is based on the Subclipse
 * SVN client adapter which in turns support SVN Kit, Java HL and command line
 * SVN client adapters.
 */
public class SVNBasedArtifactRepository implements ArtifactRepository {

    private static final Log log = LogFactory.getLog(SVNBasedArtifactRepository.class);

    private static final int UNVERSIONED = SVNStatusKind.UNVERSIONED.toInt();
    private static final int MISSING = SVNStatusKind.MISSING.toInt();

    private static final boolean RECURSIVE = true;
    private static final boolean NO_SET_DEPTH = false;

    private Map<Integer, TenantSVNRepositoryContext> tenantSVNRepositories;

    private  List<RepositoryConfigParameter> parameters;

    public SVNBasedArtifactRepository(){
        tenantSVNRepositories = new HashMap<Integer, TenantSVNRepositoryContext>();
        populateParameters();
    }

    public void init(int tenantId) throws DeploymentSynchronizerException {

        ServerConfiguration serverConfig = ServerConfiguration.getInstance();
        DeploymentSynchronizerConfiguration conf = CarbonRepositoryUtils.getActiveSynchronizerConfiguration(tenantId);

        String url = null;
        boolean appendTenantId = true;
        boolean ignoreExternals = false;
        boolean forceUpdate = true;
        String user = null;
        String password = null;

        RepositoryConfigParameter[] configParameters = conf.getRepositoryConfigParameters();

        if(configParameters == null || configParameters.length == 0){
            handleException("SVN configuration parameters must be specified for the SVN based deployment synchronizer");
        }

        for (RepositoryConfigParameter parameter : configParameters) {
            if (SVNConstants.SVN_URL.equals(parameter.getName())) {
                url = parameter.getValue();
            } else if (SVNConstants.SVN_USER.equals(parameter.getName())) {
                user = parameter.getValue();
            } else if (SVNConstants.SVN_PASSWORD.equals(parameter.getName())) {
                password = parameter.getValue();
            } else if (SVNConstants.SVN_URL_APPEND_TENANT_ID.equals(parameter.getName())) {
                appendTenantId = Boolean.valueOf(parameter.getValue());
            } else if (SVNConstants.SVN_IGNORE_EXTERNALS.equals(parameter.getName())) {
                ignoreExternals = Boolean.valueOf(parameter.getValue());
            } else if (SVNConstants.SVN_FORCE_UPDATE.equals(parameter.getName())) {
                forceUpdate = Boolean.valueOf(parameter.getValue());
            }
        }

        if (url == null) {
            handleException("SVN URL must be specified for the SVN based deployment synchronizer");
            return;
        }

        if (appendTenantId) {
            if (!url.endsWith("/")) {
                url += "/";
            }
            url += tenantId;
        }

        if (log.isDebugEnabled()) {
            log.debug("Registering SVN URL: " + url + " for tenant: " + tenantId);
        }

        SVNUrl svnUrl = null;
        try {
            svnUrl = new SVNUrl(url);
        } catch (MalformedURLException e) {
            handleException("Provided SVN URL is malformed: " + url, e);
        }

        String clientType = serverConfig.getFirstProperty(SVNConstants.SVN_CLIENT);
        if (clientType == null) {
            try {
                clientType = SVNClientAdapterFactory.getPreferredSVNClientType();
            } catch (SVNClientException e) {
                handleException("Error while retrieving the preferred SVN client type", e);
            }
        }

        ISVNClientAdapter svnClient = SVNClientAdapterFactory.createSVNClient(clientType);
        if (user != null) {
            svnClient.setUsername(user);
            svnClient.setPassword(password);
        }

        SVNNotifyListener notifyListener = new SVNNotifyListener();
        svnClient.addNotifyListener(notifyListener);
        svnClient.setProgressListener(notifyListener);
        svnClient.addConflictResolutionCallback(new DefaultSVNConflictResolver());

        TenantSVNRepositoryContext tenantRepositoryContext = new TenantSVNRepositoryContext();
        tenantRepositoryContext.setSvnUrl(svnUrl);
        tenantRepositoryContext.setSvnClient(svnClient);
        tenantRepositoryContext.setConf(conf);
        tenantRepositoryContext.setIgnoreExternals(ignoreExternals);
        tenantRepositoryContext.setForceUpdate(forceUpdate);

        tenantSVNRepositories.put(tenantId, tenantRepositoryContext);

        checkRemoteDirectory(tenantId);
    }

    private void populateParameters(){
        parameters = new ArrayList<RepositoryConfigParameter>();

        RepositoryConfigParameter parameter;

        parameter = new RepositoryConfigParameter();
        parameter.setName(SVNConstants.SVN_URL);
        parameter.setType("string");
        parameter.setRequired(true);
        parameter.setMaxlength(50);
        parameters.add(parameter);

        parameter = new RepositoryConfigParameter();
        parameter.setName(SVNConstants.SVN_USER);
        parameter.setType("string");
        parameter.setRequired(true);
        parameters.add(parameter);

        parameter = new RepositoryConfigParameter();
        parameter.setName(SVNConstants.SVN_PASSWORD);
        parameter.setType("string");
        parameter.setRequired(true);
        parameter.setMasked(true);
        parameters.add(parameter);

        parameter = new RepositoryConfigParameter();
        parameter.setName(SVNConstants.SVN_IGNORE_EXTERNALS);
        parameter.setType("boolean");
        parameters.add(parameter);

        parameter = new RepositoryConfigParameter();
        parameter.setName(SVNConstants.SVN_FORCE_UPDATE);
        parameter.setType("boolean");
        parameters.add(parameter);

        parameter = new RepositoryConfigParameter();
        parameter.setName(SVNConstants.SVN_URL_APPEND_TENANT_ID);
        parameter.setType("boolean");
        parameters.add(parameter);
    }

    /**
     * Check whether the specified directory exists in the remote SVN repository. If the
     * directory does not exist, attempt to create it.
     *
     * @throws DeploymentSynchronizerException If an error occurs while creating the directory
     */
    private void checkRemoteDirectory(int tenantId) throws DeploymentSynchronizerException {
        TenantSVNRepositoryContext repoContext= tenantSVNRepositories.get(tenantId);
        if (repoContext == null ) {
            log.warn("TenantSVNRepositoryContext not initialized for " + tenantId);
            return;
        }

        SVNUrl svnUrl = repoContext.getSvnUrl();
        ISVNClientAdapter svnClient = repoContext.getSvnClient();

        try {

            ISVNInfo info = svnClient.getInfo(svnUrl);
            if (info != null && log.isDebugEnabled()) {
                log.debug("Remote directory: " + svnUrl + " exists");
            }
        } catch (SVNClientException ex) {
            if (log.isDebugEnabled()) {
                log.debug("Error while retrieving information from the directory: " + svnUrl, ex);
                log.debug("Attempting to create the directory: " + svnUrl);
            }

            try {
                svnClient.mkdir(svnUrl, true, "Directory creation by deployment synchronizer");
            } catch (SVNClientException e) {
                handleException("Error while attempting to create the directory: " + svnUrl, e);
            }
        }
    }

    private void svnAddFiles(int tenantId, File root) throws SVNClientException {
        if (log.isDebugEnabled()) {
            log.debug("SVN adding files in " + root);
        }
        TenantSVNRepositoryContext repoContext= tenantSVNRepositories.get(tenantId);
        if (repoContext == null ) {
            log.warn("TenantSVNRepositoryContext not initialized for " + tenantId);
            return;
        }

        ISVNClientAdapter svnClient = repoContext.getSvnClient();

        ISVNStatus[] status = svnClient.getStatus(root, true, false);
        
        // This is required to filter exploded web apps and unpack directories of .WAR files.
        List<String>  dirListToAddSVN= processUnversionedWebappActions(status);
        
        for (ISVNStatus s : status) {
            if (s.getTextStatus().toInt() == UNVERSIONED) {
                File file = s.getFile();
                String fileName = file.getName();
                if (fileName.startsWith(".") || fileName.startsWith("~") ||
                        fileName.endsWith(".bk")) {
                    continue;
                }

                String filePath = file.getPath();
                if (file.isFile()) {
                	 if (log.isDebugEnabled()) {
                		 log.debug(" SVN ADD : " + filePath);
                		 }
                    svnClient.addFile(file);
                    
                    //If this is  a .war file then we need to add svn:ignore for unpack directory.
                    if (isWARWebApp(filePath)) {
                        String simpleName = getSimpleFileName(filePath);
                        String ignorePattern = simpleName.replace(".war", "");
                        String webAppsDir = getWebAppsDirPath(filePath);
                        svnClient.addToIgnoredPatterns
                                    (new File(webAppsDir), ignorePattern);
                        if (log.isDebugEnabled()) {
                             log.debug(" SVN Ignore : " + webAppsDir + " - " + ignorePattern);
                                  }
                         }
                    
                } else {
                	// For web apps we need to perform extra filtering here before adding to SVN.
                	if (isWebApp(filePath)) {
                	     String simpleName = getSimpleFileName(filePath);
                	     if (!dirListToAddSVN.contains(simpleName)) {
                	        if (log.isDebugEnabled()) {
                	           log.debug(" Ignoring directory : " + filePath);
                	            }
                	        continue;
                	          }
                	   }
                	
                    // Do not svn add directories with the recursive option.
                    // That will add child directories and files that we don't want to add.
                    // First add the top level directory only.
                    svnClient.addDirectory(file, false);

                    // Then iterate over the children and add each of them by recursively calling
                    // this method
                    File[] children = file.listFiles(new FileFilter() {
                        public boolean accept(File file) {
                            return !file.getName().equals(".svn");
                        }
                    });

                    for (File child : children) {
                        svnAddFiles(tenantId, child);
                    }
                }
            }
        }
    }

    public boolean commit(int tenantId, String filePath) throws DeploymentSynchronizerException {
        if (log.isDebugEnabled()) {
            log.debug("SVN committing " + filePath);
        }
        TenantSVNRepositoryContext repoContext= tenantSVNRepositories.get(tenantId);
        if (repoContext == null ) {
            log.warn("TenantSVNRepositoryContext not initialized for " + tenantId);
            return false;
        }

        ISVNClientAdapter svnClient = repoContext.getSvnClient();

        File root = new File(filePath);
        try {
            svnClient.cleanup(root);
            svnAddFiles(tenantId, root);
            cleanupDeletedFiles(tenantId, root);
            ISVNStatus[] status = svnClient.getStatus(root, true, false);
            if (status != null && status.length > 0 && !isAllUnversioned(status)) {
                File[] files = new File[] { root };
                svnClient.commit(files, "Commit initiated by deployment synchronizer", true);

                //Always do a svn update if you do a commit. This is just to update the working copy's
                //revision number to the latest. This fixes out-of-date working copy issues.
                if (log.isDebugEnabled()) {
                    log.debug("Updating the working copy after the commit.");
                }
                checkout(tenantId, filePath);

                return true;
            } else {
                log.debug("No changes in the local working copy");
            }
        } catch (SVNClientException e) {
            String message = e.getMessage();
            String pattern = System.getProperty("line.separator");
            message = message.replaceAll(pattern, " ");

            boolean isOutOfDate = message.matches(".*svn: Commit failed.* is out of date");

            if (isOutOfDate) {
                log.warn("Working copy is out of date. Forcing a svn update. Tenant: " + tenantId, e);
                boolean updated = checkout(tenantId, filePath);
                if (!updated) {
                    log.error("Failed to update the working copy even though the previous commit failed due to " +
                            "out of date content.");
                }
            } else {
                handleException("Error while committing artifacts to the SVN repository", e);
            }
        }
        return false;
    }

    private boolean isAllUnversioned(ISVNStatus[] status) {
        for (ISVNStatus s : status) {
            if (s.getTextStatus().toInt() != UNVERSIONED) {
                return false;
            }
        }
        return true;
    }

    public boolean checkout(int tenantId, String filePath) throws DeploymentSynchronizerException {
        if (log.isDebugEnabled()) {
            log.debug("SVN checking out " + filePath);
        }
        TenantSVNRepositoryContext repoContext= tenantSVNRepositories.get(tenantId);
        if (repoContext == null ) {
            log.warn("TenantSVNRepositoryContext not initialized for " + tenantId);
            return false;
        }

        SVNUrl svnUrl = repoContext.getSvnUrl();
        ISVNClientAdapter svnClient = repoContext.getSvnClient();
        DeploymentSynchronizerConfiguration conf = repoContext.getConf();
        boolean ignoreExternals = repoContext.isIgnoreExternals();
        boolean forceUpdate = repoContext.isForceUpdate();

        File root = new File(filePath);
        try {
            if (conf.isAutoCommit()) {
                cleanupDeletedFiles(tenantId, root);
            }
            ISVNStatus status = svnClient.getSingleStatus(root);
            if (CarbonUtils.isWorkerNode()) {
                if (log.isDebugEnabled()) {
                    log.debug("reverting " + root);
                }
                if (status != null && status.getTextStatus().toInt() != UNVERSIONED) {
                    svnClient.revert(root, true);
                }
            }
            if (status != null && status.getTextStatus().toInt() == UNVERSIONED) {
                cleanupUnversionedFiles(tenantId, svnUrl, root);
                if (svnClient instanceof CmdLineClientAdapter) {
                    // CmdLineClientAdapter does not support all the options
                    svnClient.checkout(svnUrl, root, SVNRevision.HEAD, RECURSIVE);
                    if (log.isDebugEnabled()) {
                        log.debug("Checked out using CmdLineClientAdapter");
                    }
                } else {
                    svnClient.checkout(svnUrl, root, SVNRevision.HEAD,
                            Depth.infinity, ignoreExternals, forceUpdate);
                    if (log.isDebugEnabled()) {
                        log.debug("Checked out using SVN Kit");
                    }
                }
                return true;
            } else {
                long lastRevisionNumber = -1;
                long newRevisionNumber = -1;
                svnClient.cleanup(root);
                int tries = 0;
                do {
                    try {
                        tries++;
                        if (svnClient instanceof CmdLineClientAdapter) {
                            // CmdLineClientAdapter does not support all the options
                            lastRevisionNumber = svnClient.getSingleStatus(root)
                                    .getLastChangedRevision().getNumber();
                            newRevisionNumber = svnClient.update(root, SVNRevision.HEAD, RECURSIVE);
                            if (log.isDebugEnabled()) {
                                log.debug(" files were updated to revision number: " +
                                        newRevisionNumber + " using CmdLineClientAdapter");
                            }
                        } else {
                            lastRevisionNumber = svnClient.getSingleStatus(root)
                                    .getLastChangedRevision().getNumber();
                            newRevisionNumber = svnClient.update(root, SVNRevision.HEAD,
                                    Depth.infinity, NO_SET_DEPTH,
                                    ignoreExternals, forceUpdate);
                            if (log.isDebugEnabled()) {
                                log.debug("files were updated to revision number: " +
                                        newRevisionNumber + " using SVN Kit");
                            }
                        }
                        break;
                    } catch (SVNClientException e) {
                        if (tries < 10 &&
                                (e.getMessage().contains("an unversioned file of the same name already exists") ||
                                 e.getMessage().contains("an unversioned directory of the same name already exists"))) {
                            log.info("Unversioned file problem. Retrying " + tries);
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException ignored) {
                            }
                            cleanupUnversionedFiles(tenantId, svnUrl,root);
                        } else {
                            throw e;
                        }
                    }
                } while (tries < 10); // try to recover & retry
                return newRevisionNumber > lastRevisionNumber;
            }
        } catch (SVNClientException e) {
            handleException("Error while checking out or updating artifacts from the " +
                    "SVN repository", e);
        }
        return false;
    }

    private void cleanupUnversionedFiles(int tenantId, SVNUrl svnURL,  File root) throws SVNClientException {
        TenantSVNRepositoryContext repoContext= tenantSVNRepositories.get(tenantId);
        if (repoContext == null ) {
            log.warn("TenantSVNRepositoryContext not initialized for " + tenantId);
            return;
        }

        ISVNClientAdapter svnClient = repoContext.getSvnClient();
        DeploymentSynchronizerConfiguration conf = repoContext.getConf();

        ISVNDirEntry[] entries = svnClient.getList(svnURL, SVNRevision.HEAD, false);
        for (ISVNDirEntry entry : entries) {
            String fileName = entry.getPath();
            SVNNodeKind nodeType = entry.getNodeKind();
            File localFile = new File(root, fileName);
            if (localFile.exists()) {
                ISVNStatus status = svnClient.getSingleStatus(localFile);
                if (status != null && status.getTextStatus().toInt() != UNVERSIONED) {
                    if (localFile.isDirectory()) { // see whether there are unversioned files under this dir. Recursive
                        String appendPath = "/" + localFile.getName();
                        cleanupUnversionedFiles(tenantId, svnURL.appendPath(appendPath), localFile);
                        continue; // this is not an unversioned directory, continue
                    } else if (localFile.isFile()) {
                        continue;
                    }
                }

                if (localFile.isFile() && SVNNodeKind.FILE.equals(nodeType)) {
                    log.info("Unversioned file: " + localFile.getPath() + " will be deleted");
                    if (!localFile.delete()) {
                        log.error("Unable to delete the file: " + localFile.getPath());
                    }
                } else if (localFile.isDirectory() && SVNNodeKind.DIR.equals(nodeType)) {
                    log.info("Unversioned directory: " + localFile.getPath() + " will be deleted");
                    try {
                        FileUtils.deleteDirectory(localFile);
                    } catch (IOException e) {
                        log.error("Error while deleting the directory: " + localFile.getPath(), e);
                    }
                }
            }
        }
    }


    /**
     * Find the files and directories which are in the MISSING state and schedule them
     * for svn delete
     *
     * @param root Root directory of the local working copy
     * @throws SVNClientException If an error occurs in the SVN client
     */
    private void cleanupDeletedFiles(int tenantId, File root) throws SVNClientException {
        TenantSVNRepositoryContext repoContext= tenantSVNRepositories.get(tenantId);
        if (repoContext == null ) {
            log.warn("TenantSVNRepositoryContext not initialized for " + tenantId);
            return;
        }

        ISVNClientAdapter svnClient = repoContext.getSvnClient();

        ISVNStatus[] status = svnClient.getStatus(root, true, false);
        if (status != null) {
            List<File> deletableFiles = new ArrayList<File>();
            for (ISVNStatus s : status) {
                int statusCode = s.getTextStatus().toInt();
                if (statusCode == MISSING) {
                    if (log.isDebugEnabled()) {
                        log.debug("Scheduling the file: " + s.getPath() + " for SVN delete");
                    }
                    deletableFiles.add(s.getFile());
                }
            }

            if (deletableFiles.size() > 0) {
                svnClient.remove(deletableFiles.toArray(new File[deletableFiles.size()]), true);
            }
        }
    }

    public void initAutoCheckout(boolean useEventing) throws DeploymentSynchronizerException {
        // Nothing to impl
    }

    public void cleanupAutoCheckout() {
        // Nothing to impl
    }

    public String getRepositoryType() {
        return DeploymentSynchronizerConstants.REPOSITORY_TYPE_SVN;
    }

    public List<RepositoryConfigParameter> getParameters() {
        return parameters;
    }

    public boolean checkout(int tenantId, String filePath, int depth)
            throws DeploymentSynchronizerException {
        log.info("SVN checking out " + filePath);
        TenantSVNRepositoryContext repoContext= tenantSVNRepositories.get(tenantId);
        if (repoContext == null ) {
            log.warn("TenantSVNRepositoryContext not initialized for " + tenantId);
            return false;
        }

        SVNUrl svnUrl = repoContext.getSvnUrl();
        ISVNClientAdapter svnClient = repoContext.getSvnClient();
        DeploymentSynchronizerConfiguration conf = repoContext.getConf();
        boolean ignoreExternals = repoContext.isIgnoreExternals();
        boolean forceUpdate = repoContext.isForceUpdate();

        File root = new File(filePath);
        try {
            if (conf.isAutoCommit()) {
                cleanupDeletedFiles(tenantId, root);
            }
            ISVNStatus status = svnClient.getSingleStatus(root);
            if (status != null && status.getTextStatus().toInt() == UNVERSIONED) {
                cleanupUnversionedFiles(tenantId, svnUrl, root);
                if (svnClient instanceof CmdLineClientAdapter) {
                    // CmdLineClientAdapter does not support all the options
                    svnClient.checkout(svnUrl, root, SVNRevision.HEAD, RECURSIVE);
                    log.info("Checked out using CmdLineClientAdapter");
                } else {
                    svnClient.checkout(svnUrl, root, SVNRevision.HEAD,
                                       depth, ignoreExternals, forceUpdate);
                    log.info("Checked out using SVN Kit");
                }
                return true;
            } else {
                long lastRevisionNumber = -1;
                long newRevisionNumber = -1;
                svnClient.cleanup(root);
                int tries = 0;
                do {
                    try {
                        tries++;
                        if (svnClient instanceof CmdLineClientAdapter) {
                            // CmdLineClientAdapter does not support all the options
                            lastRevisionNumber = svnClient.getSingleStatus(root)
                                    .getLastChangedRevision().getNumber();
                            newRevisionNumber = svnClient.update(root, SVNRevision.HEAD, RECURSIVE);
                            log.info("files were updated to revision number: " + newRevisionNumber +
                                    " using CmdLineClientAdapter");
                        } else {
                            lastRevisionNumber = svnClient.getSingleStatus(root)
                                    .getLastChangedRevision().getNumber();
                            newRevisionNumber = svnClient.update(root, SVNRevision.HEAD,
                                                            depth, NO_SET_DEPTH,
                                                            ignoreExternals, forceUpdate);
                            log.info("files were updated to revision number: " + newRevisionNumber +
                                    " using SVN Kit");
                        }
                        break;
                    } catch (SVNClientException e) {
                        if (tries < 10 &&
                            (e.getMessage().contains("an unversioned file of the same name already exists") ||
                             e.getMessage().contains("an unversioned directory of the same name already exists"))) {
                            log.info("Unversioned file problem. Retrying " + tries);
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException ignored) {
                            }
                            cleanupUnversionedFiles(tenantId, svnUrl, root);
                        } else {
                            throw e;
                        }
                    }
                } while (tries < 10); // try to recover & retry
                return newRevisionNumber > lastRevisionNumber;
            }
        } catch (SVNClientException e) {
            handleException("Error while checking out or updating artifacts from the " +
                            "SVN repository", e);
        }
        return false;
    }

    public boolean update(int tenantId, String rootPath, String filePath, int depth) throws DeploymentSynchronizerException {
        log.info("SVN updating " + filePath);

        TenantSVNRepositoryContext repoContext= tenantSVNRepositories.get(tenantId);
        if (repoContext == null ) {
            log.warn("TenantSVNRepositoryContext not initialized for " + tenantId);
            return false;
        }
        SVNUrl svnUrl = repoContext.getSvnUrl();
        ISVNClientAdapter svnClient = repoContext.getSvnClient();
        boolean ignoreExternals = repoContext.isIgnoreExternals();
        boolean forceUpdate = repoContext.isForceUpdate();

        File root = new File(rootPath);
        boolean setDepth = false;
        if (depth == Depth.infinity) {
            setDepth = true;
        }
        long lastRevisionNumber = -1;
        long newRevisionNumber = -1;
        try {
            svnClient.cleanup(root);

            if (CarbonUtils.isWorkerNode()) {
                if (log.isDebugEnabled()) {
                    log.debug("reverting " + root);
                }
                svnClient.revert(root, true);
            }

            int tries = 0;
            do {
                try {
                    tries++;
                    if (svnClient instanceof CmdLineClientAdapter) {
                        // CmdLineClientAdapter does not support all the options
                        lastRevisionNumber = svnClient.getSingleStatus(root)
                                .getLastChangedRevision().getNumber();
                        newRevisionNumber = svnClient.update(root, SVNRevision.HEAD, RECURSIVE);
                        log.info("files were updated to revision number: " + newRevisionNumber +
                                " using CmdLineClientAdapter");
                    } else {
                        lastRevisionNumber = svnClient.getSingleStatus(root)
                                .getLastChangedRevision().getNumber();
                        newRevisionNumber = svnClient.update(root, filePath, SVNRevision.HEAD,
                                                        depth, setDepth,
                                                        ignoreExternals, forceUpdate);
                        log.info("files were updated to revision number: " + newRevisionNumber +
                                " using SVN Kit");
                    }
                    break;
                } catch (SVNClientException e) {
                    if (tries < 10 &&
                        (e.getMessage().contains("an unversioned file of the same name already exists") ||
                         e.getMessage().contains("an unversioned directory of the same name already exists"))) {
                        log.info("Unversioned file problem. Retrying " + tries);
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ignored) {
                        }
                        cleanupUnversionedFiles(tenantId, svnUrl, root);
                    } else {
                        throw e;
                    }
                }
            } while (tries < 10); // try to recover & retry
            return newRevisionNumber > lastRevisionNumber;
        } catch (SVNClientException e) {
            handleException("Error while checking out or updating artifacts from the " +
                            "SVN repository", e);
        }
        return false;
    }

    public void cleanupTenantContext(int tenantId) {
        tenantSVNRepositories.remove(tenantId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SVNBasedArtifactRepository that = (SVNBasedArtifactRepository) o;

        if (!getRepositoryType().equals(that.getRepositoryType())){
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return getRepositoryType().hashCode();
    }

    private void handleException(String msg) throws DeploymentSynchronizerException {
        log.error(msg);
        throw new DeploymentSynchronizerException(msg);
    }

    private void handleException(String msg, Exception e) throws DeploymentSynchronizerException {
        log.error(msg, e);
        throw new DeploymentSynchronizerException(msg, e);
    }
    
    public List<String> processUnversionedWebappActions(ISVNStatus[] status) {
        List<String> addToSvn = new ArrayList<String>();
        for (ISVNStatus st : status) {
            String path = st.getPath();
            if (path != null && isWebApp(path)) {
                int lastIdx = path.lastIndexOf(File.separator);
                String simpleName = lastIdx > 0 ? path.substring(lastIdx + 1) : null;
                if (simpleName != null) {
                    if (!simpleName.endsWith(".war")) {
                        // This is a directory ensure there is no .war exists before adding to SVN
                        File appBase = new File(path.concat(".war"));
                        if (!appBase.exists()) {
                            addToSvn.add(simpleName);
                        }

                    }
                }
            }
        }
        return addToSvn;
    }

    private String getSimpleFileName(String filePath) {
        int lastIdx = filePath.lastIndexOf(File.separator);
        return lastIdx > 0 ? filePath.substring(lastIdx + 1) : null;
    }

    private boolean isWARWebApp(String filePath) {
        return (isWebApp(filePath) && filePath.endsWith(".war"));
    }

    private boolean isWebApp(String filePath) {
        /* Decide file is on "webapps" directory or not.

          supper tenant - "repository/deployment/server/webapps";
          tenant -  "repository/tenants/1/webapps"
         */
        return filePath.contains(File.separator + "repository" + File.separator)
                && filePath.contains(File.separator + "webapps" + File.separator);
    }

    private String getWebAppsDirPath(String path) {
        int start = path.indexOf("repository");
        int end = path.lastIndexOf(File.separator);
        return path.substring(start, end);
    }

}