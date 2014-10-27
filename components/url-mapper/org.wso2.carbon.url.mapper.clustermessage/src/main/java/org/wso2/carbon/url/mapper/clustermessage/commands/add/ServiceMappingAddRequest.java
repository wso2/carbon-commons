/*
*  Copyright (c), WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.url.mapper.clustermessage.commands.add;

import org.apache.axis2.clustering.ClusteringCommand;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.tomcat.ext.utils.URLMappingHolder;

/**
 *  Cluster command to add service mapping from url-mapper
 */
public class ServiceMappingAddRequest extends ClusteringMessage {
    private static final Log log = LogFactory.getLog(ServiceMappingAddRequest.class);

    private String mapping;
    private String epr;
    
    public ServiceMappingAddRequest(String mapping, String epr) {
        this.mapping = mapping;
        this.epr = epr;
        
    }
    @Override
    public ClusteringCommand getResponse() {
        return null;
    }

    @Override
    public void execute(ConfigurationContext configurationContext) throws ClusteringFault {
        URLMappingHolder.getInstance().putUrlMappingForApplication(mapping, epr);
        log.info("mapping added to service:***********: " + mapping + "******: " + epr);
    }
}
