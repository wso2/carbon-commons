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
package org.wso2.carbon.url.mapper.clustermessage.commands.delete;

import org.apache.axis2.clustering.ClusteringCommand;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.tomcat.ext.utils.URLMappingHolder;

/**
 *  Cluster command to delete service mapping from url-mapper
 */
public class ServiceMappingDeleteRequest extends ClusteringMessage {
    private static final Log log = LogFactory.getLog(ServiceMappingDeleteRequest.class);

    private String mapping;
    
    public ServiceMappingDeleteRequest(String mapping) {
        this.mapping = mapping;
    }
    @Override
    public ClusteringCommand getResponse() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void execute(ConfigurationContext configurationContext) throws ClusteringFault {
        URLMappingHolder urlMappingHolder = URLMappingHolder.getInstance();
        if(urlMappingHolder.isUrlMappingExists(mapping)) {
            urlMappingHolder.removeUrlMappingMap(mapping);
        }
        log.info("mapping removed to service:***********: " + mapping);
    }
}
