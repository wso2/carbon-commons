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
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.deployment.synchronizer.ArtifactRepository;
import org.wso2.carbon.deployment.synchronizer.DeploymentSynchronizerException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class is responsible for creating DeploymentSynchronizer instances as and when necessary
 * and keeping track of them for management purposes. It also provides a base framework for
 * scheduling automatic repository synchronization tasks.
 */
public final class DeploymentSynchronizationManager {

    private static final Log log = LogFactory.getLog(DeploymentSynchronizationManager.class);

    private static final DeploymentSynchronizationManager instance = new DeploymentSynchronizationManager();

    private Map<String,DeploymentSynchronizer> synchronizers =
            new ConcurrentHashMap<String,DeploymentSynchronizer>();
    private Map<String,ScheduledFuture> scheduledFutures =
            new ConcurrentHashMap<String,ScheduledFuture>();

    private ScheduledExecutorService repositoryTaskExecutor;

    private DeploymentSynchronizationManager() {

    }

    public static DeploymentSynchronizationManager getInstance() {
        return instance;
    }

    /**
     * Initialize the RepositoryManager instance. The RepositoryManager must be initialized by
     * calling this method, before synchronizers can use it to schedule tasks.
     *
     * @param serverConfig Active Carbon ServerConfiguration
     */
    void init(ServerConfiguration serverConfig) {
        if (log.isDebugEnabled()) {
            log.debug("Initializing deployment synchronization manager");
        }

        int poolSize = DeploymentSynchronizerConstants.DEFAULT_POOL_SIZE;
        String value = serverConfig.getFirstProperty(DeploymentSynchronizerConstants.POOL_SIZE);
        if (value != null) {
            poolSize = Integer.parseInt(value);
        }

        repositoryTaskExecutor = Executors.newScheduledThreadPool(poolSize, new SimpleThreadFactory());
    }

    /**
     * Cleanup and shutdown the RepositoryManager instance. This method will also gracefully
     * destroy all the created synchronizers, cancel any existing scheduled tasks and then
     * free up any additional resources.
     */
    void shutdown() {
        for (DeploymentSynchronizer repository : synchronizers.values()) {
            repository.stop();
        }
        synchronizers.clear();

        for (ScheduledFuture future : scheduledFutures.values()) {
            future.cancel(false);
        }
        ((ScheduledThreadPoolExecutor) repositoryTaskExecutor).purge();
        scheduledFutures.clear();

        repositoryTaskExecutor.shutdownNow();
    }

    /**
     * Creates a new DeploymentSynchronizer instance using the provided parameters. A given file path
     * may only have one DeploymentSynchronizer engaged on it. Any attempts to create multiple
     * DeploymentSynchronizer instances on the same file path will result in an IllegalStateException.
     * This method only creates DeploymentSynchronizer instances. The caller should take action
     * to customize and further initialize the repository as required.
     *
     * @param artifactRepository ArtifactRepository representing the remote repository
     * @param filePath File path in the local file system
     * @return a DeploymentSynchronizer instance
     */
    public DeploymentSynchronizer createSynchronizer(int tenantId, ArtifactRepository artifactRepository,
                                                    String filePath) {

        // We synchronize based on filePath.intern() to make sure that two threads don't
        // access the following logic with the same path. That could lead to multiple
        // synchronizers getting engaged on the same file path.
        synchronized (filePath.intern()) {
            if (synchronizers.containsKey(filePath)) {
                log.warn("A deployment synchronizer is already engaged for the " +
                        "file system at: " + filePath);
                DeploymentSynchronizer synchronizer = synchronizers.remove(filePath);
                if (synchronizer != null) {
                    synchronizer.stop();
                }
            }

            DeploymentSynchronizer synchronizer = new DeploymentSynchronizer(tenantId, artifactRepository,
                    filePath);
            synchronizers.put(filePath, synchronizer);
            return synchronizer;
        }
    }

    public DeploymentSynchronizer getSynchronizer(String filePath) {
        return synchronizers.get(filePath);
    }

    public DeploymentSynchronizer getSynchronizer(int tenantId) {
        return getSynchronizer(MultitenantUtils.getAxis2RepositoryPath(tenantId));
    }

    public Map<String, DeploymentSynchronizer> getSynchronizers() {
        return synchronizers;
    }

    /**
     * Deletes the DeploymentSynchronizer for the specified file path. This will simply remove the
     * repository from the control of the DeploymentSynchronizationManager. Any tasks spawned by the
     * synchronizer will continue to run until the synchronizer is properly stopped.
     *
     * @param filePath File path on which the repository is created
     * @return A RegistryBasedRepository or null
     */
    public DeploymentSynchronizer deleteSynchronizer(String filePath) {
        synchronized (filePath.intern()) {
            return synchronizers.remove(filePath);
        }
    }
    
    public DeploymentSynchronizer deleteSynchronizer(int tenantId) {
        String filePath = MultitenantUtils.getAxis2RepositoryPath(tenantId);
        synchronized (filePath.intern()) {
            return synchronizers.remove(filePath);
        }
    }

    void scheduleSynchronizationTask(String key, Runnable task, long delaySec, long periodSec) {
        ScheduledFuture future = repositoryTaskExecutor.scheduleWithFixedDelay(task, delaySec,
                periodSec, TimeUnit.SECONDS);
        scheduledFutures.put(key, future);
    }

    void cancelSynchronizationTask(String key) {
        ScheduledFuture future = scheduledFutures.get(key);
        if (future != null) {
            future.cancel(false);
            ((ScheduledThreadPoolExecutor) repositoryTaskExecutor).purge();
            scheduledFutures.remove(key);
        }
    }

    void initDelayedAutoCheckout() {
        for (DeploymentSynchronizer synchronizer : synchronizers.values()) {
            try {
                synchronizer.getArtifactRepository().initAutoCheckout(synchronizer.isUseEventing());
            } catch (DeploymentSynchronizerException e) {
                log.error("Error while calling delayed auto checkout", e);
            }
        }
    }

    private static class SimpleThreadFactory implements ThreadFactory {

        private AtomicInteger counter = new AtomicInteger(0);

        public Thread newThread(Runnable r) {
            return new Thread(r, "deployment-sync-" + counter.incrementAndGet());
        }
    }

}
