/**
 *  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 **/

package org.wso2.carbon.event.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.event.client.stub.generated.authentication.AuthenticationAdminServiceStub;
import org.wso2.carbon.event.client.stub.generated.authentication.AuthenticationExceptionException;
import org.wso2.carbon.utils.NetworkUtils;

import java.net.SocketException;
import java.rmi.RemoteException;

public class AuthenticationClient {    
    private String sessionCookie;
    private AuthenticationAdminServiceStub stub;

    public AuthenticationClient(ConfigurationContext configurationContext, String serviceUrl) throws AxisFault {
        //if (configurationContext != null) {
            stub = new AuthenticationAdminServiceStub(configurationContext, serviceUrl);
        //}
        stub._getServiceClient().getOptions().setManageSession(true);
    }

    public AuthenticationClient(String serviceUrl) throws AxisFault {
        this(null, serviceUrl);
    }

    public boolean authenticate(String username, String password) throws AuthenticationExceptionException,
            RemoteException {
        try {
            boolean isAuthenticated = stub.login(username,password,NetworkUtils.getLocalHostname());
            if(isAuthenticated){
                ServiceContext serviceContext;
                serviceContext = stub._getServiceClient().getLastOperationContext().getServiceContext();
                String sessionCookie;
                sessionCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);

                this.sessionCookie = sessionCookie;
            }else{
                throw new AuthenticationExceptionException("Authentication Failed");
            }
            return isAuthenticated;
        } catch (SocketException e) {
            throw new AuthenticationExceptionException(e);
        }
    }

    public String getSessionCookie() {
        return sessionCookie;
    }

    private String generateURL(String[] components) {
        StringBuilder builder = new StringBuilder();
        if (components.length > 0) {
            builder.append(components[0]);
        }
        for (int i = 1; i < components.length; i++) {
            builder.append("/");
            builder.append(components[i]);
        }
        return builder.toString();
    }
    
    public static void main(String[] args) throws Exception {
        System.setProperty("javax.net.ssl.trustStore", "/home/hemapani/playground/events/wso2carbon-4.0.0-SNAPSHOT/resources/security/wso2carbon.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        
        AuthenticationClient client = new AuthenticationClient("https://localhost:9443/services/AuthenticationAdmin");
        System.out.println(client.authenticate("admin", "admin"));
    }
}
