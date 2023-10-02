package org.wso2.carbon.logging.service.internal;

import org.apache.axis2.clustering.ClusteringAgent;
import org.wso2.carbon.logging.service.RemoteLoggingConfigService;
import org.wso2.carbon.logging.service.clustering.ClusterRemoteLoggerConfigInvalidationRequestSender;
import org.wso2.carbon.registry.core.service.RegistryService;

/**
 * Remote Logging Config Data Holder.
 */
public class RemoteLoggingConfigDataHolder {

    private static RemoteLoggingConfigDataHolder instance = new RemoteLoggingConfigDataHolder();
    private RegistryService registryService;
    private RemoteLoggingConfigService remoteLoggingConfigService;
    private ClusteringAgent clusteringAgent;
    private ClusterRemoteLoggerConfigInvalidationRequestSender clusterRemoteLoggerConfigInvalidationRequestSender;

    private RemoteLoggingConfigDataHolder() {

    }

    public static RemoteLoggingConfigDataHolder getInstance() {

        return instance;
    }

    public RegistryService getRegistryService() {

        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {

        this.registryService = registryService;
    }

    public static void setInstance(RemoteLoggingConfigDataHolder instance) {

        RemoteLoggingConfigDataHolder.instance = instance;
    }

    public RemoteLoggingConfigService getRemoteLoggingConfigService() {

        return remoteLoggingConfigService;
    }

    public void setRemoteLoggingConfigService(
            RemoteLoggingConfigService remoteLoggingConfigService) {

        this.remoteLoggingConfigService = remoteLoggingConfigService;
    }

    public ClusteringAgent getClusteringAgent() {

        return clusteringAgent;
    }

    public void setClusteringAgent(ClusteringAgent clusteringAgent) {

        this.clusteringAgent = clusteringAgent;
    }

    public ClusterRemoteLoggerConfigInvalidationRequestSender getClusterRemoteLoggerConfigInvalidationRequestSender() {

        return clusterRemoteLoggerConfigInvalidationRequestSender;
    }

    public void setClusterRemoteLoggerConfigInvalidationRequestSender(
            ClusterRemoteLoggerConfigInvalidationRequestSender clusterRemoteLoggerConfigInvalidationRequestSender) {

        this.clusterRemoteLoggerConfigInvalidationRequestSender = clusterRemoteLoggerConfigInvalidationRequestSender;
    }
}
