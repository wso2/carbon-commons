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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.event.client.stub.generated.authentication.AuthenticationAdminServiceStub;
import org.wso2.carbon.event.client.stub.generated.authentication.AuthenticationExceptionException;
import org.wso2.carbon.event.core.delivery.DeliveryManager;
import org.wso2.carbon.event.core.delivery.DeliveryManagerFactory;
import org.wso2.carbon.event.core.exception.EventBrokerConfigurationException;
import org.wso2.carbon.event.core.internal.util.EventBrokerHolder;
import org.wso2.carbon.event.core.internal.util.JavaUtil;
import org.wso2.carbon.event.core.util.EventBrokerConstants;
import org.wso2.carbon.qpid.stub.service.QpidAdminServiceStub;
import org.wso2.carbon.utils.NetworkUtils;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import javax.xml.namespace.QName;
import java.net.SocketException;
import java.rmi.RemoteException;

public class QpidJMSDeliveryManagerFactory implements DeliveryManagerFactory {

    public static final String EB_REMOTE_MESSGE_BROKER = "remoteMessageBroker";
    public static final String EB_HOST_NAME = "hostName";
    public static final String EB_SERVICE_PORT = "servicePort";
    public static final String EB_WEB_CONTEXT = "webContext";
    public static final String EB_USER_NAME = "userName";
    public static final String EB_PASSWORD = "password";
    public static final String EB_QPID_PORT = "qpidPort";
    public static final String EB_CLIENT_ID = "clientID";
    public static final String EB_VIRTUAL_HOST_NAME = "virtualHostName";
    public static final String EB_TYPE = "type";
    public static final String EB_REMOTE_MESSAGE_BROKER_PASSWORD_ALIAS = "eventBrokerConfig.eventBroker.deliveryManager.remoteMessageBroker.password";

    public DeliveryManager getDeliveryManger(OMElement config)
            throws EventBrokerConfigurationException {

        String type = config.getAttributeValue(new QName(null, EB_TYPE));
        QpidJMSDeliveryManager qpidJMSDelivaryManager = new QpidJMSDeliveryManager(type);

        if (QpidJMSDeliveryManager.MB_TYPE_REMOTE.equals(type)) {

            OMElement remoteQpidAdminService =
                    config.getFirstChildWithName(new QName(EventBrokerConstants.EB_CONF_NAMESPACE,
                                                           EB_REMOTE_MESSGE_BROKER));

            String hostName = JavaUtil.getValue(remoteQpidAdminService, EB_HOST_NAME);
            String servicePort = JavaUtil.getValue(remoteQpidAdminService, EB_SERVICE_PORT);
            String webContext = JavaUtil.getValue(remoteQpidAdminService, EB_WEB_CONTEXT);

            if (!webContext.endsWith("/")) {
                webContext += "/";
            }

            String userName = JavaUtil.getValue(remoteQpidAdminService, EB_USER_NAME);
            String password = JavaUtil.getValue(remoteQpidAdminService, EB_PASSWORD);
            // resolve password if it is secured with secure vault
            SecretResolver secretResolver = SecretResolverFactory.create(remoteQpidAdminService, false);
            if (secretResolver != null && secretResolver.isInitialized()) {
                if (secretResolver.isTokenProtected(EB_REMOTE_MESSAGE_BROKER_PASSWORD_ALIAS)) {
                    password = secretResolver.resolve(EB_REMOTE_MESSAGE_BROKER_PASSWORD_ALIAS);
                } else {
                     throw new EventBrokerConfigurationException("Can not resolve password for " +
                                                                 "remote message broker from secret resolver.");
                }
            }

            String qpidPort = JavaUtil.getValue(remoteQpidAdminService, EB_QPID_PORT);
            String clientID = JavaUtil.getValue(remoteQpidAdminService, EB_CLIENT_ID);
            String virtualHostName = JavaUtil.getValue(remoteQpidAdminService, EB_VIRTUAL_HOST_NAME);

            // getting the access key from the back end.
            ConfigurationContext clientConfigurationContext =
                    EventBrokerHolder.getInstance().getConfigurationContextService().getClientConfigContext();
            try {
                String servicesString = "https://" + hostName + ":" + servicePort + webContext + "services/";
                AuthenticationAdminServiceStub stub =
                        new AuthenticationAdminServiceStub(clientConfigurationContext, servicesString + "AuthenticationAdmin");
                stub._getServiceClient().getOptions().setManageSession(true);
                boolean isAuthenticated = stub.login(userName, password, NetworkUtils.getLocalHostname());

                if (isAuthenticated) {
                    ServiceContext serviceContext = stub._getServiceClient().getLastOperationContext().getServiceContext();
                    String sessionCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
                    QpidAdminServiceStub qpidAdminServiceStub = new QpidAdminServiceStub(clientConfigurationContext, servicesString + "QpidAdminService");
                    qpidAdminServiceStub._getServiceClient().getOptions().setManageSession(true);
                    qpidAdminServiceStub._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING, sessionCookie);
                    String accessKey = qpidAdminServiceStub.getAccessKey();

                    qpidJMSDelivaryManager.setHostName(hostName);
                    qpidJMSDelivaryManager.setAccessKey(accessKey);
                    qpidJMSDelivaryManager.setQpidPort(qpidPort);
                    qpidJMSDelivaryManager.setClientID(clientID);
                    qpidJMSDelivaryManager.setVirtualHostName(virtualHostName);

                } else {
                    throw new EventBrokerConfigurationException("Can not authenticate to the remote messge broker ");
                }
            } catch (AxisFault axisFault) {
                throw new EventBrokerConfigurationException("Can not connect to the remote Qpid Service ", axisFault);
            } catch (SocketException e) {
                throw new EventBrokerConfigurationException("Can not connect to the remote Qpid Service ", e);
            } catch (AuthenticationExceptionException e) {
                throw new EventBrokerConfigurationException("Can not connect to the remote Qpid Service ", e);
            } catch (RemoteException e) {
                throw new EventBrokerConfigurationException("Can not connect to the remote Qpid Service ", e);
            }
        }

        return qpidJMSDelivaryManager;

    }
}
