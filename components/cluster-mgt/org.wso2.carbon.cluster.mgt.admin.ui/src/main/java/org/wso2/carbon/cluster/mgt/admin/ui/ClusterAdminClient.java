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
package org.wso2.carbon.cluster.mgt.admin.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cluster.mgt.admin.stub.ClusterAdminStub;
import org.wso2.carbon.cluster.mgt.admin.stub.types.carbon.Group;
import org.wso2.carbon.cluster.mgt.admin.stub.types.carbon.GroupMember;

import java.lang.Exception;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Web service client for cluster management
 */
public class ClusterAdminClient {

    private static final Log log = LogFactory.getLog(ClusterAdminClient.class);
    private static final String BUNDLE = "org.wso2.carbon.cluster.mgt.admin.ui.i18n.Resources";
    private ResourceBundle bundle;
    public ClusterAdminStub stub;

    public ClusterAdminClient(String cookie,
                              String backendServerURL,
                              ConfigurationContext configCtx,
                              Locale locale) throws AxisFault {
        String serviceURL = backendServerURL + "ClusterAdmin";
        bundle = ResourceBundle.getBundle(BUNDLE, locale);

        stub = new ClusterAdminStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public Group[] getGroups() throws java.lang.Exception {
        try {
            return stub.getGroups();
        } catch (java.lang.Exception e) {
            String msg = bundle.getString("cannot.get.groups");
            handleException(msg, e);
        }
        return new Group[0];
    }

    public GroupMember[] getMembers(String groupName) throws java.lang.Exception {
        try {
            return stub.getMembers(groupName);
        } catch (java.lang.Exception e) {
            String msg = MessageFormat.format(bundle.getString("cannot.get.members.in.group"),
                                              groupName);
            handleException(msg, e);
        }
        return new GroupMember[0];
    }

    public void shutdownGroup(String groupName) throws java.lang.Exception {
        try {
            stub.shutdownGroup(groupName);
        } catch (java.lang.Exception e) {
            String msg = MessageFormat.format(bundle.getString("cannot.shutdown.group"),
                                              groupName);
            handleException(msg, e);
        }
    }

    public void shutdownGroupGracefully(String groupName) throws java.lang.Exception {
        try {
            stub.shutdownGroupGracefully(groupName);
        } catch (java.lang.Exception e) {
            String msg = MessageFormat.format(bundle.getString("cannot.shutdown.group.gracefully"),
                                              groupName);
            handleException(msg, e);
        }
    }

    public void restartGroup(String groupName) throws java.lang.Exception {
        try {
            stub.restartGroup(groupName);
        } catch (java.lang.Exception e) {
            String msg = MessageFormat.format(bundle.getString("cannot.restart.group"),
                                              groupName);
            handleException(msg, e);
        }
    }

    public void restartGroupGracefully(String groupName) throws java.lang.Exception {
        try {
            stub.restartGroupGracefully(groupName);
        } catch (java.lang.Exception e) {
            String msg = MessageFormat.format(bundle.getString("cannot.restart.group.gracefully"),
                                              groupName);
            handleException(msg, e);
        }
    }

    public void shutdownCluster() throws java.lang.Exception {
        try {
            stub.shutdownCluster();
        } catch (java.lang.Exception e) {
            String msg = bundle.getString("cannot.shutdown.cluster");
            handleException(msg, e);
        }
    }

    public void shutdownClusterGracefully() throws java.lang.Exception {
        try {
            stub.shutdownClusterGracefully();
        } catch (java.lang.Exception e) {
            String msg = bundle.getString("cannot.shutdown.cluster.gracefully");
            handleException(msg, e);
        }
    }

    public void restartCluster() throws java.lang.Exception {
        try {
            stub.restartCluster();
        } catch (java.lang.Exception e) {
            String msg = bundle.getString("cannot.restart.cluster");
            handleException(msg, e);
        }
    }

    public void restartClusterGracefully() throws java.lang.Exception {
        try {
            stub.restartClusterGracefully();
        } catch (java.lang.Exception e) {
            String msg = bundle.getString("cannot.restart.cluster.gracefully");
            handleException(msg, e);
        }
    }

    public void startGroupMaintenance(String groupName) throws java.lang.Exception {
        try {
            stub.startGroupMaintenance(groupName);
        } catch (java.lang.Exception e) {
            String msg = MessageFormat.format(bundle.getString("cannot.start.group.maintenance"),
                                              groupName);
            handleException(msg, e);
        }
    }

    public void endGroupMaintenance(String groupName) throws Exception {
        try {
            stub.endGroupMaintenance(groupName);
        } catch (java.lang.Exception e) {
            String msg = MessageFormat.format(bundle.getString("cannot.end.group.maintenance"),
                                              groupName);
            handleException(msg, e);
        }
    }

    public void startClusterMaintenance() throws Exception {
        try {
            stub.startClusterMaintenance();
        } catch (java.lang.Exception e) {
            String msg = bundle.getString("cannot.start.cluster.maintenance");
            handleException(msg, e);
        }
    }

    public void endClusterMaintenance() throws Exception {
        try {
            stub.endClusterMaintenance();
        } catch (java.lang.Exception e) {
            String msg = bundle.getString("cannot.end.cluster.maintenance");
            handleException(msg, e);
        }
    }

    private void handleException(String msg, java.lang.Exception e) throws java.lang.Exception {
        log.error(msg, e);
        throw new java.lang.Exception(msg, e);
    }
}
