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
package org.wso2.carbon.url.mapper.clustermessage.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardHost;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.tomcat.api.CarbonTomcatService;
import org.wso2.carbon.tomcat.ext.utils.URLMappingHolder;
import org.wso2.carbon.tomcat.ext.valves.CarbonContextCreatorValve;
import org.wso2.carbon.tomcat.ext.valves.CompositeValve;
import org.wso2.carbon.url.mapper.clustermessage.commands.add.ServiceMappingAddRequest;
import org.wso2.carbon.url.mapper.clustermessage.commands.add.VirtualHostAddRequest;
import org.wso2.carbon.url.mapper.clustermessage.commands.delete.ServiceMappingDeleteRequest;
import org.wso2.carbon.url.mapper.clustermessage.commands.delete.VirtualHostDeleteMapping;
import org.wso2.carbon.utils.CarbonUtils;

/**
 *  Util class for sending cluster message to other nodes.
 */
public class VirtualHostClusterUtil {
    private static final Log log = LogFactory.getLog(VirtualHostClusterUtil.class);

    public static Boolean addServiceMappingToCluster(String mapping, String epr) throws AxisFault {
        Boolean isMappingAdded = false;
        try {
            ClusteringAgent agent = getClusteringAgent();
            if(agent != null) {
                agent.sendMessage(new ServiceMappingAddRequest(mapping, epr), true);
                isMappingAdded = true;
            }
            if (log.isDebugEnabled()) {
                log.debug("sent cluster command to to get Active tenants on cluster");
            }
        } catch (AxisFault f) {
            String msg = "Error in getting active tenant by cluster commands";
            log.error(msg, f);
            throw new AxisFault(msg);
        }
        return isMappingAdded;
    }

    public static Boolean addVirtualHostsToCluster(String hostName, String uri, String webappPath) throws AxisFault {
        Boolean isMappingAdded = false;
        try {
            ClusteringAgent agent = getClusteringAgent();
            if(agent != null) {
                agent.sendMessage(new VirtualHostAddRequest(hostName, uri, webappPath), true);
                isMappingAdded = true;
            }
            if (log.isDebugEnabled()) {
                log.debug("sent cluster command to to get Active tenants on cluster");
            }
        } catch (AxisFault f) {
            String msg = "Error in getting active tenant by cluster commands";
            log.error(msg, f);
            throw new AxisFault(msg);
        }
        return isMappingAdded;
    }

    public static Boolean deleteServiceMappingToCluster(String mapping) throws AxisFault {
        Boolean isMappingAdded = false;
        try {
            ClusteringAgent agent = getClusteringAgent();
            if(agent != null) {
                agent.sendMessage(new ServiceMappingDeleteRequest(mapping), true);
                isMappingAdded = true;
            }
            if (log.isDebugEnabled()) {
                log.debug("sent cluster command to to get Active tenants on cluster");
            }
        } catch (AxisFault f) {
            String msg = "Error in getting active tenant by cluster commands";
            log.error(msg, f);
            throw new AxisFault(msg);
        }
        return isMappingAdded;
    }

    public static Boolean deleteVirtualHostsToCluster(String hostName) throws AxisFault {
        Boolean isMappingAdded = false;
        try {
            ClusteringAgent agent = getClusteringAgent();
            if(agent != null) {
                agent.sendMessage(new VirtualHostDeleteMapping(hostName), true);
                isMappingAdded = true;
            }
            if (log.isDebugEnabled()) {
                log.debug("sent cluster command to to get Active tenants on cluster");
            }
        } catch (AxisFault f) {
            String msg = "Error in getting active tenant by cluster commands";
            log.error(msg, f);
            throw new AxisFault(msg);
        }
        return isMappingAdded;
    }

    private static ClusteringAgent getClusteringAgent() throws AxisFault {

        AxisConfiguration axisConfig =
                DataHolder.getInstance().getConfigurationContextService().getServerConfigContext().getAxisConfiguration();
        return axisConfig.getClusteringAgent();
    }
    
    public static void removeVirtualHost(String hostName) {
        Engine engine = DataHolder.getInstance().getCarbonTomcatService().getTomcat().getEngine();
        Host host = (Host) engine.findChild(hostName);
        Context context = (Context) host.findChild("/");
        try {
            if (host.getState().isAvailable()) {
                if (context != null && context.getAvailable()) {
                    context.setRealm(null);
                    context.stop();
                    context.destroy();
                    log.info("Unloaded webapp from the host: " + host
                            + " as the context of: " + context);
                }
                host.removeChild(context);
                host.setRealm(null);
                host.stop();
                host.destroy();
                engine.removeChild(host);
            }
        }catch (LifecycleException e) {
            log.error("error while removing host from tomcat", e);
        }
        URLMappingHolder.getInstance().removeUrlMappingMap(
                host.getName());
        log.info("Unloaded host from the engine: " + host);

    }
    public static Host addHostToEngine(String hostName) {
        String hostBaseDir = CarbonUtils.getCarbonRepository() + "/webapps/";
        CarbonTomcatService carbonTomcatService = DataHolder.getInstance().getCarbonTomcatService();
        // adding virtual host to tomcat engine
        Engine engine = carbonTomcatService.getTomcat().getEngine();
        StandardHost host = new StandardHost();
        host.setAppBase(hostBaseDir);
        host.setName(hostName);
        host.setUnpackWARs(false);
        host.addValve(new CarbonContextCreatorValve());
        host.addValve(new CompositeValve());
        engine.addChild(host);
        log.info("host added to the tomcat: " + host);
        return host;
    }



}
