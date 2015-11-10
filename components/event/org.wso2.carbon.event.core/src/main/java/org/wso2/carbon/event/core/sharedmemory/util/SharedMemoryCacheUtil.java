/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.event.core.sharedmemory.util;

import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.core.sharedmemory.SharedMemoryMatchingManager;
import org.wso2.carbon.utils.ConfigurationContextService;

import javax.cache.Cache;
import javax.cache.CacheConfiguration;
import javax.cache.CacheManager;
import javax.cache.Caching;
import java.util.concurrent.TimeUnit;

@Deprecated
public class SharedMemoryCacheUtil {
    private static boolean cacheInit = false;

    private SharedMemoryCacheUtil(){}
    
    public static Cache<Integer, SharedMemoryMatchingManager> getInMemoryMatchingCache() {
        if (cacheInit) {
            return Caching.getCacheManagerFactory()
            		.getCacheManager(SharedMemoryCacheConstants.INMEMORY_EVENT_CACHE_MANAGER)
            		.getCache(SharedMemoryCacheConstants.INMEMORY_EVENT_CACHE);
        } else {
            CacheManager cacheManager = Caching.getCacheManagerFactory()
            		.getCacheManager(SharedMemoryCacheConstants.INMEMORY_EVENT_CACHE_MANAGER);
            cacheInit = true;

            return cacheManager.<Integer, SharedMemoryMatchingManager>createCacheBuilder(SharedMemoryCacheConstants.INMEMORY_EVENT_CACHE).
                    setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new CacheConfiguration.Duration(TimeUnit.SECONDS, 
                    		SharedMemoryCacheConstants.CACHE_INVALIDATION_TIME)).
                    setExpiry(CacheConfiguration.ExpiryType.ACCESSED, new CacheConfiguration.Duration(TimeUnit.SECONDS, 
                    		SharedMemoryCacheConstants.CACHE_INVALIDATION_TIME)).
                    setStoreByValue(false).build();
        }
    }
    
    /**
     * This is to send clusterMessage to inform other nodes about subscription added to the system, so that everyone can add new one.
     * @param topicName
     * @param subsciptionID
     * @param tenantID
     * @param tenantName
     * @throws ClusteringFault
     */
	public static void sendAddSubscriptionClusterMessage(String topicName,String subsciptionID, 
			int tenantID, String tenantName) throws ClusteringFault{
		ConfigurationContextService configContextService = (ConfigurationContextService) PrivilegedCarbonContext
				.getThreadLocalCarbonContext().getOSGiService(ConfigurationContextService.class);
		ConfigurationContext configContext = configContextService.getServerConfigContext();
		ClusteringAgent agent = configContext.getAxisConfiguration().getClusteringAgent();

		agent.sendMessage(new SubscriptionClusterMessage(topicName,subsciptionID,tenantID, tenantName), false);
	}    
	
}
