/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.event.core.sharedmemory.util;

import java.io.Serializable;

import org.apache.axis2.clustering.ClusteringCommand;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.core.sharedmemory.SharedMemorySubscriptionStorage;
import org.wso2.carbon.event.core.sharedmemory.SubscriptionContainer;

public class SubscriptionClusterMessage extends ClusteringMessage implements
		Serializable {
	
	private static final Log log = LogFactory.getLog(SubscriptionClusterMessage.class);
	private String topicName;
	private String subscriptionID;
	private Integer tenantID;
	private String tenantDomain;

	SubscriptionClusterMessage(String topicName, String subscriptionID,
			Integer tenantID, String tenantDomain) {
		this.topicName = topicName;
		this.subscriptionID = subscriptionID;
		this.tenantID = tenantID;
		this.tenantDomain = tenantDomain;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -4585980192987552283L;

	@Override
	public ClusteringCommand getResponse() {
		return null;
	}

	@Override
	public void execute(ConfigurationContext arg0) throws ClusteringFault {

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantID);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
           
            //get the subscription by adding the 
            SubscriptionContainer subscriptionsContainer = SharedMemorySubscriptionStorage.getTopicSubscriptionCache().get(topicName);

            if (subscriptionsContainer != null){
            	subscriptionsContainer.getSubscriptionsCache().get(subscriptionID);
            	log.info("Subscription ID: "+subscriptionID+" for the topic: "+topicName+" is received.");
            }          

        }
        finally{
            PrivilegedCarbonContext.endTenantFlow();
        }
	}

}
