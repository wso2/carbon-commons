<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.awt.image.BufferedImage" %>
<%@ page import="java.io.File" %>
<%@ page import="javax.imageio.ImageIO" %>
<%@ page import="java.awt.image.RenderedImage" %>
<%@ page import="java.util.Properties" %>
<%@ page import="java.util.UUID" %>
<%@ page import="java.net.URL" %>
<%@ page import="org.wso2.carbon.registry.core.exceptions.RegistryException" %>
<%@ page import="java.net.MalformedURLException" %>
<%@ page import="java.net.URLConnection" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="org.wso2.carbon.registry.core.utils.RegistryUtils" %>
<%@ page import="org.wso2.carbon.email.verification.ui.clients.EmailVerificationServiceClient" %>
<%@ page import="org.wso2.carbon.email.verification.stub.beans.xsd.ConfirmationBean" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%
        if (!"post".equalsIgnoreCase(request.getMethod())) {
            response.sendError(405);
            return;
        }
        String data = null;
        String redirect = null;
        try {
            EmailVerificationServiceClient client = new EmailVerificationServiceClient(config,session);
            String confirm = request.getParameter("confirmation");
            
            ConfirmationBean confirmationBean = client.confirmUser(confirm);
            data = confirmationBean.getData();
            redirect = confirmationBean.getRedirectPath();
        } catch (Exception ignore) {
            String errorRedirect = (String)session.getAttribute("email-verification-error-redirect");
            if (errorRedirect != null) {
                session.removeAttribute("email-verification-error-redirect");
                response.sendRedirect(errorRedirect);
                return;
            }
            response.sendRedirect("../email-verification/invalid_email.jsp");
            return;
        }
        session.setAttribute("intermediate-data", data);
        response.sendRedirect(redirect);
%>



