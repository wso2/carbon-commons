/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.tryit;

import org.apache.http.protocol.HTTP;
import org.wso2.carbon.core.transports.CarbonHttpRequest;
import org.wso2.carbon.core.transports.CarbonHttpResponse;
import org.wso2.carbon.core.transports.HttpGetRequestProcessor;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.wsdl2form.WSDL2FormGenerator;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.OutputStream;

public class TryitRequestProcessor implements HttpGetRequestProcessor {

    private static Log log = LogFactory.getLog(TryitRequestProcessor.class);
    private static final String TRY_IT_FUNCTIONALITY_DISABLED = "tryItFunctionalityDisabled";

    public void process(CarbonHttpRequest request, CarbonHttpResponse response,
                        ConfigurationContext configurationContext) throws Exception {
        OutputStream outputStream = response.getOutputStream();
        String requestURL = request.getRequestURL() + "?" + request.getQueryString();
        String serviceParameter = request.getParameter(WSDL2FormGenerator.SERVICE_QUERY_PARAM);
        String endpointParameter = request.getParameter(WSDL2FormGenerator.ENDPOINT_QUERY_PARAM);
        String operationParameter = request.getParameter(WSDL2FormGenerator.OPERATION_PARAM);

        response.addHeader(HTTP.CONTENT_TYPE, "text/html; charset=utf-8");

        String tryItFunctionalityDisabled = System.getProperty(TRY_IT_FUNCTIONALITY_DISABLED);
        if (tryItFunctionalityDisabled != null && tryItFunctionalityDisabled.equalsIgnoreCase("true")) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            outputStream.write(("<h4>Try-it functionality is disabled. Please unset the property '"
                    + TRY_IT_FUNCTIONALITY_DISABLED + "' in conf/carbon.properties file to enable.</h4>").getBytes());
            outputStream.flush();
            return;
        }

        String isAuthenticated = request.getParameter("authenticated");
        if ("false".equalsIgnoreCase(isAuthenticated)) {
            response.setRedirect("/carbon/admin/login.jsp");
            return;
        }

        try {
            Result result = new StreamResult(outputStream);
            String str = WSDL2FormGenerator.getInstance().getInternalTryit(result,
                                                                           configurationContext,
                                                                           requestURL,
                                                                           serviceParameter,
                                                                           operationParameter,
                                                                           endpointParameter, true);
            if(!str.equals(WSDL2FormGenerator.SUCCESS)) {
                response.setRedirect(str);
            }
        } catch (CarbonException e) {
            log.error(e);
            if (e.getMessage().equals(WSDL2FormGenerator.SERVICE_INACTIVE)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                outputStream
                        .write(("<h4>Requested Service is inactive. Cannot generate stubs.</h4>").getBytes());
                outputStream.flush();
            } else if (e.getMessage().equals(WSDL2FormGenerator.SERVICE_NOT_FOUND)) {
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                outputStream
                        .write(("<h4>Service cannot be found. Cannot display <em>Stub</em>.</h4>").getBytes());
                outputStream.flush();
            } else {
                response.setError(HttpServletResponse.SC_NOT_FOUND);
                outputStream.write(
                        ("<h4>" + e.getMessage() + "</h4>").getBytes());
                outputStream.flush();
            }
        }
    }
}