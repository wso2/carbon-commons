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

package org.wso2.carbon.deployment.synchronizer.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.deployment.synchronizer.ArtifactRepository;
import org.wso2.carbon.deployment.synchronizer.DeploymentSynchronizerException;

import java.io.File;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class is responsible for maintaining a repository in the local file system in sync with a
 * remote repository. The local repository could be an artifact repository used by Carbon, Axis2 or
 * Synapse. In fact it could be any directory hierarchy in the local file system. The remote repository
 * could be a registry instance, a version control system or an online data storage service. This
 * implementation can commit the changes in the local file system to the remote repository, and
 * checkout any updated artifacts from the repository to the local file system. Commit operations
 * and checkout operations are synchronized so that both operations will never run at the same time.
 * This implementation also supports triggering checkouts on events.
 */
public class DeploymentSynchronizer {

    private static final Log log = LogFactory.getLog(DeploymentSynchronizer.class);

    /**
     * DeploymentSynchronizer objects are created per-tenant
     *
     */
    private int tenantId;

    /**
     * Location in the file system where the repository is located
     */
    private String filePath;

    private long lastCommitTime = -1L;
    private long lastCheckoutTime = -1L;

    /**
     * This flag indicates whether the synchronizer should perform commits automatically
     */
    private boolean autoCommit;

    /**
     * This flag indicates whether the synchronizer should perform checkouts automatically
     */
    private boolean autoCheckout;

    /**
     * This flag indicates whether the synchronizer should perform checkouts based on
     * an external event notification
     */
    private boolean useEventing;

    /**
     * If checkouts are performed based on events, this variable keeps track of when
     * the last event notification was received
     */
    private AtomicLong lastNotificationTime = new AtomicLong(-1L);

    /**
     * A flag which indicates whether an external event has requested an automatic
     * checkout.
     */
    private AtomicBoolean checkoutRequested = new AtomicBoolean(false);

    private ArtifactRepository artifactRepository;

    private long period = DeploymentSynchronizerConstants.DEFAULT_AUTO_SYNC_PERIOD;

    DeploymentSynchronizer(int tenantId, ArtifactRepository artifactRepository, String filePath) {
        this.tenantId = tenantId;
        this.artifactRepository = artifactRepository;
        this.filePath = filePath;
    }

    /**
     * Start this DeploymentSynchronizer instance. If the autoCommit and autoCheckout modes are not
     * enabled, this method will do nothing. If such additional features are enabled, this method
     * will schedule the necessary periodic tasks and get the auto-sync tasks up and running.
     */
    public void start() {
        if (!autoCommit && !autoCheckout) {
            return;
        }

        log.info("Starting a synchronizer on file system at: " + filePath);

        try {
            doInitialSyncUp();
            if (autoCheckout) {
                artifactRepository.initAutoCheckout(useEventing);
            }

            Runnable task = new AutoSyncTask();
            DeploymentSynchronizationManager.getInstance().
                    scheduleSynchronizationTask(filePath, task, period, period);
        } catch (DeploymentSynchronizerException e) {
            log.error("Error while performing the initial sync-up on the repository at: " +
                      filePath + ". Auto sync tasks will not be engaged.", e);
        }
    }

    public void stop() {
        if (autoCommit || autoCheckout) {
            log.info("Terminating the synchronizer on file system at: " + filePath);
            DeploymentSynchronizationManager.getInstance().cancelSynchronizationTask(filePath);

            if (autoCheckout) {
                artifactRepository.cleanupAutoCheckout();
            }
        }
    }

    /**
     * Commit the artifacts in the file system repository to the remote repository
     *
     * @throws DeploymentSynchronizerException If an error occurs while committing the artifacts
     */
    public synchronized boolean commit() throws DeploymentSynchronizerException {
        if (log.isDebugEnabled()) {
            log.debug("Started commit from " + filePath);
        }
        boolean result = artifactRepository.commit(tenantId, filePath);
        lastCommitTime = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("Commit completed at " + new Date(lastCommitTime) + ". Status: " + result);
        }
        return result;
    }

    /**
     * Commit the artifacts from the given path in the file system repository to the remote repository
     *
     * @throws DeploymentSynchronizerException If an error occurs while committing the artifacts
     */
    public synchronized boolean commit(String filePath) throws DeploymentSynchronizerException {
        if (log.isDebugEnabled()) {
            log.debug("Started commit from " + filePath);
        }
        boolean result = artifactRepository.commit(tenantId, filePath);
        lastCommitTime = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("Commit completed at " + new Date(lastCommitTime) + ". Status: " + result);
        }
        return result;
    }


    /**
     * Checkout the artifacts stored in the repository to the file system. If the artifacts
     * have already been checked out, an update will be executed instead.
     *
     * @throws DeploymentSynchronizerException If an error occurs while checking out or updating the resources
     */
    public synchronized boolean checkout() throws DeploymentSynchronizerException {
        if (log.isDebugEnabled()) {
            log.debug("Started checkout to " + filePath);
        }
        boolean result = artifactRepository.checkout(tenantId, filePath);
        lastCheckoutTime = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("Checkout completed at " + new Date(lastCheckoutTime) + ". Status: " + result);
        }
        return result;
    }

        /**
     * Checkout the artifacts stored in the repository to the file system. If the artifacts
     * have already been checked out, an update will be executed instead.
     *
     * @throws DeploymentSynchronizerException If an error occurs while checking out or updating the resources
     */
    public synchronized boolean checkout(String filePath, int depth) throws DeploymentSynchronizerException {
        if (log.isDebugEnabled()) {
            log.debug("Started checkout to " + filePath + " with depth: " + depth);
        }
        boolean result = artifactRepository.checkout(tenantId, filePath, depth);
        lastCheckoutTime = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("Checkout completed at " + new Date(lastCheckoutTime) + ". Status: " + result);
        }
        return result;
    }

        /**
     * Checkout the artifacts stored in the repository to the file system. If the artifacts
     * have already been checked out, an update will be executed instead.
     *
     * @throws DeploymentSynchronizerException If an error occurs while checking out or updating the resources
     */
    public synchronized boolean update(String rootPath, String filePath, int depth) throws DeploymentSynchronizerException {
        if (log.isDebugEnabled()) {
            log.debug("Started to update " + filePath);
        }
        boolean result = artifactRepository.update(tenantId, rootPath, filePath, depth);
        lastCheckoutTime = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("Update completed at " + new Date(lastCheckoutTime) + ". Status: " + result);
        }
        return result;
    }

    /**
     * Notify the synchronizer that a checkout needs to be performed. This method is mainly used
     * in conjunction with the eventing mode of operation. Once this method is invoked, synchronizer
     * will set a flag to indicate  that a checkout needs to be performed. During the next
     * execution of the synchronizer task, the checkout will be performed and the flag will be
     * unset.
     *
     * @param timestamp A timestamp corresponding to the time of the notification
     */
    public void requestCheckout(long timestamp) {
        long lastTimestamp = lastNotificationTime.get();
        if (checkoutRequested.get() || timestamp <= lastTimestamp) {
            // If a checkout is already requested or the current timestamp is older than a
            // previously encountered timestamp we will ignore this request
            return;
        }

        // Check whether another thread has beaten the current thread to it.
        // If the lastNotificationTime has changed since the last check, then some other thread
        // has already set the checkoutRequested flag
        if (lastNotificationTime.compareAndSet(lastTimestamp, timestamp)) {
            if (log.isDebugEnabled()) {
                log.debug("Checkout operation requested");
            }
            checkoutRequested.compareAndSet(false, true);
        }
    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public void setAutoCheckout(boolean autoCheckout) {
        this.autoCheckout = autoCheckout;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    public void setUseEventing(boolean useEventing) {
        this.useEventing = useEventing;
    }

    public boolean isUseEventing() {
        return useEventing;
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public boolean isAutoCheckout() {
        return autoCheckout;
    }

    public long getLastCheckoutTime() {
        return lastCheckoutTime;
    }

    public long getLastCommitTime() {
        return lastCommitTime;
    }

    public ArtifactRepository getArtifactRepository() {
        return artifactRepository;
    }

    public void doInitialSyncUp() throws DeploymentSynchronizerException {
        log.info("Doing initial sync up...");
        if (autoCommit) {
            if (lastCheckoutTime == -1L) {
                log.info("Checking out...");
                checkout();
            }
            log.info("Committing...");
            commit();
        }

        if (autoCheckout && lastCheckoutTime == -1L) {
            log.info("Checking out...");
            checkout();
        }
    }

    public boolean syncGhostMetaArtifacts() throws DeploymentSynchronizerException{
        log.info("Doing ghost meta artifacts sync up...");
        boolean hasFailed = false;
        if (autoCheckout && lastCheckoutTime == -1L) {
            log.info("Checking out...");
            // checkout with empty depth
            checkout(filePath, 2);
            // update modules and its metafiles
            update(filePath, filePath + File.separator +
                             CarbonConstants.MODULES_DEPLOYMENT_DIR, 3);
            update(filePath, filePath + File.separator +
                             CarbonConstants.MODULE_METAFILE_HOTDEPLOYMENT_DIR, 3);
            // then update only the ghost meta files
            hasFailed = update(filePath, filePath + File.separator +
                                         CarbonConstants.GHOST_METAFILE_DIR, 3);
        }
        return hasFailed;
    }


    private boolean checkoutRequested() {
        return !useEventing || checkoutRequested.getAndSet(false);
    }

    public void sync() {
        try {
            if (autoCheckout && checkoutRequested()) {
                checkout();
            }

            if (autoCommit) {
                commit();
            }
        } catch (DeploymentSynchronizerException e) {
            log.error("Synchronization error encountered in the repository " +
                      "at: " + filePath, e);
        } catch (Exception t) {
            log.error("Unexpected runtime error encountered while synchronizing the " +
                      "repository: " + filePath, t);
        }
    }

    private final class AutoSyncTask implements Runnable {

        private AutoSyncTask() {
            if (log.isDebugEnabled()) {
                log.debug("Initializing auto sync task for the repository at: " + filePath);
            }
        }

        public void run() {
            sync();
        }
    }
}
