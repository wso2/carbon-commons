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

package org.wso2.carbon.event.core.internal.delivery.jms;

import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.wso2.carbon.event.core.internal.util.EventBrokerHolder;
import org.wso2.carbon.event.core.qpid.QpidServerDetails;

import javax.jms.TopicConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Context;
import java.util.Properties;

public class QpidJMSDeliveryManager extends JMSDeliveryManager {

    public static final String QPID_ICF = "org.wso2.andes.jndi.PropertiesFileInitialContextFactory";
    public static final String CF_NAME_PREFIX = "connectionfactory.";
    public static final String CF_NAME = "qpidConnectionfactory";

    public static final String MB_TYPE_LOCAL = "local";
    public static final String MB_TYPE_REMOTE = "remote";

    private String type;
    private String hostName;
    private String qpidPort;
    private String clientID;
    private String virtualHostName;
    private String accessKey;

    public QpidJMSDeliveryManager(String type) {
        this.type = type;
    }

    protected Properties getInitialContextProperties(String userName, String password) {

        Properties initialContextProperties = new Properties();
        QpidServerDetails qpidServerDetails = EventBrokerHolder.getInstance().getQpidServerDetails();
        initialContextProperties.put(Context.INITIAL_CONTEXT_FACTORY, QPID_ICF);
        initialContextProperties.put(CarbonConstants.REQUEST_BASE_CONTEXT, "true");
        String connectionURL = null;
        if (MB_TYPE_LOCAL.equals(this.type)) {
            connectionURL = qpidServerDetails.getTCPConnectionURL(userName, qpidServerDetails.getAccessKey());
        } else {
            connectionURL = "amqp://" + userName + ":" + this.accessKey
                    + "@" + clientID + "/" + this.virtualHostName
                    + "?brokerlist='tcp://" + this.hostName + ":" + this.qpidPort + "'";
        }
        initialContextProperties.put(CF_NAME_PREFIX + CF_NAME, connectionURL);
        return initialContextProperties;
    }

    protected TopicConnectionFactory getTopicConnectionFactory(InitialContext initialContext)
                                              throws EventBrokerException {
        try {
            return  (TopicConnectionFactory) initialContext.lookup(CF_NAME);
        } catch (NamingException e) {
            throw new EventBrokerException("Can not look up the connection factory ", e);
        }
    }

    protected String getTopicName(String topicName) {
        if (topicName.startsWith("/")){
            topicName = topicName.substring(1);
        }
        topicName = topicName.replaceAll("/",".");
        // use _ to replace ~ this is a requirement for registry.
        // use _ to replace ~ this is a requirement for registry.
        topicName = topicName.replaceAll("~","_");
        topicName = topicName.replaceAll("&","_");
        topicName = topicName.replaceAll(" ","_");

        return topicName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getQpidPort() {
        return qpidPort;
    }

    public void setQpidPort(String qpidPort) {
        this.qpidPort = qpidPort;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getVirtualHostName() {
        return virtualHostName;
    }

    public void setVirtualHostName(String virtualHostName) {
        this.virtualHostName = virtualHostName;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }
}
