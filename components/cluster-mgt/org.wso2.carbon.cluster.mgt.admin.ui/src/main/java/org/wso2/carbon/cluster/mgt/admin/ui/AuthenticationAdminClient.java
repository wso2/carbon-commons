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
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.Login;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Admin client for authenticating against the BE of a member
 */
public class AuthenticationAdminClient {

    private static final Log log = LogFactory.getLog(ClusterAdminClient.class);
    private static final String BUNDLE = "org.wso2.carbon.cluster.mgt.admin.ui.i18n.Resources";
    private ResourceBundle bundle;
    public AuthenticationAdminStub stub;
    private String backendServerURL;

    public AuthenticationAdminClient(String backendServerURL,
                                     ConfigurationContext configCtx,
                                     Locale locale) throws AxisFault {
        String serviceURL = backendServerURL + "AuthenticationAdmin";
        bundle = ResourceBundle.getBundle(BUNDLE, locale);
        this.backendServerURL = backendServerURL;
        stub = new AuthenticationAdminStub(configCtx, serviceURL);
    }

    public boolean login(HttpServletRequest request, String username, String password) throws java.lang.Exception {
        log.info("Logging into " + backendServerURL);
        Login loginRequest = new Login();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);
        loginRequest.setRemoteAddress(request.getRemoteAddr());
        Options option = stub._getServiceClient().getOptions();
        option.setManageSession(true);
        boolean isLoggedIn = false;
        try {
            isLoggedIn = stub.login(username, password, request.getRemoteAddr());
            if (isLoggedIn) {
                String cookie =
                        (String) stub._getServiceClient().getServiceContext().
                                getProperty(HTTPConstants.COOKIE_STRING);
                HttpSession session = request.getSession();
                session.setAttribute(ServerConstants.ADMIN_SERVICE_COOKIE, cookie);
            }
        } catch (java.lang.Exception e) {
            String msg = MessageFormat.format(bundle.getString("cannot.login.to.server"),
                                              backendServerURL);
            handleException(msg, e);
        }

        return isLoggedIn;
    }

    private void handleException(String msg, java.lang.Exception e) throws java.lang.Exception {
        log.error(msg, e);
        throw new java.lang.Exception(msg, e);
    }
}
