/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.wso2.carbon.cluster.mgt.admin;

import org.apache.axis2.AxisFault;
import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.Member;
import org.apache.axis2.clustering.management.GroupManagementAgent;
import org.apache.axis2.clustering.management.NodeManager;
import org.apache.axis2.clustering.management.GroupManagementCommand;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cluster.mgt.core.RestartMemberCommand;
import org.wso2.carbon.cluster.mgt.core.ShutdownMemberCommand;
import org.wso2.carbon.cluster.mgt.core.ShutdownMemberGracefullyCommand;
import org.wso2.carbon.cluster.mgt.core.RestartMemberGracefullyCommand;
import org.wso2.carbon.cluster.mgt.core.StartMaintenanceCommand;
import org.wso2.carbon.cluster.mgt.core.EndMaintenanceCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Cluster Administration Service
 */
public class ClusterAdmin {

    private static final Log log = LogFactory.getLog(ClusterAdmin.class);

    public GroupMember[] getMembers(String groupName) throws Exception {
        ClusteringAgent clusteringAgent = getClusteringAgent();
        GroupManagementAgent groupManagementAgent =
                clusteringAgent.getGroupManagementAgent(groupName);
        if (groupManagementAgent == null) {
            handleException("No GroupManagementAgent defined for domain " + groupName);
            return null;
        }
        List<Member> members = groupManagementAgent.getMembers();
        GroupMember[] groupMembers = new GroupMember[members.size()];
        int i = 0;
        for (Member member : members) {
            GroupMember groupMember = new GroupMember();
            groupMember.setHostName(member.getHostName());
            groupMember.setHttpPort(member.getHttpPort());
            groupMember.setHttpsPort(member.getHttpsPort());
            Properties properties = member.getProperties();
            groupMember.setBackendServerURL(properties.getProperty("backendServerURL"));
            groupMember.setMgConsoleURL(properties.getProperty("mgtConsoleURL"));
            groupMembers[i] = groupMember;
            i++;
        }

        return groupMembers;
    }

    public Group[] getGroups() throws Exception {
        ClusteringAgent clusteringAgent = getClusteringAgent();
        List<Group> groups = new ArrayList<Group>();
        Set<String> groupNames = clusteringAgent.getDomains();
        for (String groupName : groupNames) {
            Group group = new Group();
            group.setName(groupName);
            GroupManagementAgent gmAgent = clusteringAgent.getGroupManagementAgent(groupName);
            List<Member> memberList = gmAgent.getMembers();
            group.setDescription(gmAgent.getDescription());
            group.setNumberOfMembers(memberList.size());
            groups.add(group);
        }
        return groups.toArray(new Group[groups.size()]);
    }

    public void shutdownGroup(String groupName) throws Exception {
        GroupManagementAgent groupManagementAgent = getGroupManagementAgent(groupName);
        groupManagementAgent.send(new ShutdownMemberCommand());
    }

    public void restartGroup(String groupName) throws Exception {
        GroupManagementAgent groupManagementAgent = getGroupManagementAgent(groupName);
        groupManagementAgent.send(new RestartMemberCommand());
    }

    public void shutdownGroupGracefully(String groupName) throws Exception {
        GroupManagementAgent groupManagementAgent = getGroupManagementAgent(groupName);
        groupManagementAgent.send(new ShutdownMemberGracefullyCommand());
    }

    public void restartGroupGracefully(String groupName) throws Exception {
        GroupManagementAgent groupManagementAgent = getGroupManagementAgent(groupName);
        groupManagementAgent.send(new RestartMemberGracefullyCommand());
    }

    public void shutdownCluster() throws Exception {
        sendToCluster(new ShutdownMemberCommand());
    }

    public void shutdownClusterGracefully() throws Exception {
        sendToCluster(new ShutdownMemberGracefullyCommand());
    }

    public void restartCluster() throws Exception {
        sendToCluster(new RestartMemberCommand());
    }

    public void restartClusterGracefully() throws Exception {
        sendToCluster(new RestartMemberGracefullyCommand());
    }

    public void startGroupMaintenance(String groupName) throws Exception {
        GroupManagementAgent groupManagementAgent = getGroupManagementAgent(groupName);
        groupManagementAgent.send(new StartMaintenanceCommand());
    }

    public void endGroupMaintenance(String groupName) throws Exception {
        GroupManagementAgent groupManagementAgent = getGroupManagementAgent(groupName);
        groupManagementAgent.send(new EndMaintenanceCommand());
    }

    public void startClusterMaintenance() throws Exception {
        sendToCluster(new StartMaintenanceCommand());
    }

    public void endClusterMaintenance() throws Exception {
        sendToCluster(new EndMaintenanceCommand());
    }

    private void sendToCluster(GroupManagementCommand cmd) throws AxisFault {
        ClusteringAgent clusteringAgent = getClusteringAgent();
        Set<String> groupNames = clusteringAgent.getDomains();
        for (String groupName : groupNames) {
            GroupManagementAgent managementAgent =
                    clusteringAgent.getGroupManagementAgent(groupName);
            managementAgent.send(cmd);
        }
    }

    private GroupManagementAgent getGroupManagementAgent(String groupName) throws AxisFault {
        ClusteringAgent clusteringAgent = getClusteringAgent();
        GroupManagementAgent groupManagementAgent =
                clusteringAgent.getGroupManagementAgent(groupName);
        if (groupManagementAgent == null) {
            handleException("No GroupManagementAgent defined for domain " + groupName);
        }
        return groupManagementAgent;
    }

    // TODO: Cluster statistics

    private ClusteringAgent getClusteringAgent() throws AxisFault {
        AxisConfiguration axisConfig =
                MessageContext.getCurrentMessageContext().
                        getConfigurationContext().getAxisConfiguration();
        ClusteringAgent clusterManager = axisConfig.getClusteringAgent();
        if (clusterManager == null) {
            handleException("ClusteringAgent not enabled in axis2.xml file");
        }
        return clusterManager;
    }

    private NodeManager getNodeManager() throws AxisFault {
        NodeManager nodeManager;
        ClusteringAgent clusteringAgent = getClusteringAgent();
        nodeManager = clusteringAgent.getNodeManager();
        if (nodeManager == null) {
            handleException("Cluster NodeManager not enabled in axis2.xml file");
        }
        return nodeManager;
    }

    private void handleException(String message) throws AxisFault {
        log.error(message);
        throw new AxisFault(message);
    }
}
