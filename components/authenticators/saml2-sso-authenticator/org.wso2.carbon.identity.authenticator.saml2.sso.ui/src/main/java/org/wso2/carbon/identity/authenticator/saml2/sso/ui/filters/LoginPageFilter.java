/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.authenticator.saml2.sso.ui.filters;

import org.wso2.carbon.identity.authenticator.saml2.sso.common.SAML2SSOAuthenticatorConstants;
import org.wso2.carbon.identity.authenticator.saml2.sso.common.Util;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This servlet filter is used to intercept the login requests coming to a Carbon server.
 * It checks whether they are coming from a user with an authenticated session, if not redirect him
 * to the corresponding identity provider.
 */
public class LoginPageFilter implements Filter {

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        if(!(servletRequest instanceof HttpServletRequest)){
            return;
        }

        if(servletResponse.isCommitted()){
            return;    
        }

        if("false".equals(servletRequest.getParameter("loginStatus")) ||
                          "failed".equals(servletRequest.getParameter("loginStatus"))){
            ((HttpServletRequest) servletRequest).getSession().setAttribute(
                    SAML2SSOAuthenticatorConstants.NOTIFICATIONS_ERROR_MSG,
                    "Service Temporarily Unavailable.");
            ((HttpServletResponse)servletResponse).sendRedirect("../sso-acs/authFailure.jsp");
            return;
        }

        if (servletRequest.getParameter(
                SAML2SSOAuthenticatorConstants.HTTP_ATTR_IS_LOGOUT_REQ) != null) {
            if (Boolean.parseBoolean(servletRequest.getParameter(
                    SAML2SSOAuthenticatorConstants.HTTP_ATTR_IS_LOGOUT_REQ))) {
                String logoutReq = "../sso-acs/redirect_ajaxprocessor.jsp?" +
                                   SAML2SSOAuthenticatorConstants.LOG_OUT_REQ + "=true";
                RequestDispatcher reqDispatcher = servletRequest.getRequestDispatcher(logoutReq);
                reqDispatcher.forward(servletRequest, servletResponse);
            }
        } else if(Util.getLandingPage() != null){
            ((HttpServletResponse)servletResponse).sendRedirect(Util.getLandingPage());
        }
        else {
            RequestDispatcher reqDispatcher = servletRequest.getRequestDispatcher("../sso-acs/redirect_ajaxprocessor.jsp");
            reqDispatcher.forward(servletRequest, servletResponse);
        }
    }

    public void destroy() {
        // This method is not required at the moment
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        // This method is not required at the moment
    }
}
