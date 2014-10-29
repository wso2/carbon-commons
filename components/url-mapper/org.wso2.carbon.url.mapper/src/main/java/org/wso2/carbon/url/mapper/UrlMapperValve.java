/*
 * Copyright WSO2, Inc. (http://wso2.com)
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
package org.wso2.carbon.url.mapper;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.mapper.MappingData;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.tomcat.ext.utils.URLMappingHolder;
import org.wso2.carbon.tomcat.ext.valves.CarbonTomcatValve;
import org.wso2.carbon.tomcat.ext.valves.CompositeValve;
import org.wso2.carbon.url.mapper.internal.exception.UrlMapperException;
import org.wso2.carbon.url.mapper.internal.util.DataHolder;
import org.wso2.carbon.url.mapper.internal.util.HostUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.xml.namespace.QName;

/**
 * This is a CarbonTomcatValve which hadles the request for services when a tenant specifies service
 * specific id in the url.
 */
public class UrlMapperValve extends CarbonTomcatValve {
    private static final Log log = LogFactory.getLog(UrlMapperValve.class);

    /**
     * This method is called when valve execute
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     */
    public void invoke(Request request, Response response, CompositeValve compositeValve) {
        try {
            process(request, response);
            getNext().invoke(request, response, compositeValve);
        } catch (Exception e) {
            log.error("error in forwarding the url", e);
        }
    }

    /**
     * @param request
     * @param response
     * @throws Exception
     * @throws UrlMapperException
     */
    private void process(Request request, Response response) throws Exception {
        String serverName = request.getServerName();
        String requestedUri = request.getRequestURI();
        String uri = URLMappingHolder.getInstance().
                getApplicationFromUrlMapping(serverName);

        if ((uri != null) && (uri.contains("services"))) {
            String filterUri = uri.substring(0, uri.length() - 1);
            String serviceName = HostUtil.getServiceName(filterUri);
            //if itz a rest call, getting the operation name from the request and
            // checking whether exists or not from AxisConfiguration
            if(!requestedUri.equalsIgnoreCase("/")) {
                String temp = requestedUri.substring(1, requestedUri.length());
                String operation;
                AxisService axisService;
                if(temp.contains("/")) {
                    operation = temp.substring(0, temp.indexOf("/"));
                } else {
                    operation = temp;
                }
                if(uri.contains("/t/")) {
                    ConfigurationContext configurationContext = TenantAxisUtils.
                            getTenantConfigurationContext(MultitenantUtils.getTenantDomainFromUrl(uri),
                            DataHolder.getInstance().getServerConfigContext());
                    axisService = configurationContext.getAxisConfiguration().getService(serviceName);
                    QName qname = new QName(operation);
                    AxisOperation axisOperation = axisService.getOperation(qname);
                    if(axisOperation != null) {
                        filterUri = filterUri + "/" + operation;
                        requestRewriteForService(request, filterUri);
                    }
                } else {
                    axisService = DataHolder.getInstance().getServerConfigContext().
                            getAxisConfiguration().getService(serviceName);
                    QName qname = new QName(operation);
                    AxisOperation axisOperation = axisService.getOperation(qname);
                    if(axisOperation != null) {
                        filterUri = filterUri + "/" + operation;
                        requestRewriteForService(request, filterUri);
                    }
                }

            } else if(requestedUri.equalsIgnoreCase("/")) {
                requestRewriteForService(request, filterUri);
            }
        }
    }

    public void requestRewriteForService(Request request, String filterUri) throws Exception {
        //rewriting the request with actual service url in order to retrieve the resource
        MappingData mappingData = request.getMappingData();
        org.apache.coyote.Request coyoteRequest = request.getCoyoteRequest();

        MessageBytes requestPath = MessageBytes.newInstance();
        requestPath.setString(filterUri);
        mappingData.requestPath = requestPath;
        MessageBytes pathInfo = MessageBytes.newInstance();
        pathInfo.setString(filterUri);
        mappingData.pathInfo = pathInfo;

        coyoteRequest.requestURI().setString(filterUri);
        coyoteRequest.decodedURI().setString(filterUri);
        if (request.getQueryString() != null) {
            coyoteRequest.unparsedURI().setString(filterUri + "?" + request.getQueryString());
        } else {
            coyoteRequest.unparsedURI().setString(filterUri);
        }
        request.getConnector().
                getMapper().map(request.getCoyoteRequest().serverName(),
                request.getCoyoteRequest().decodedURI(), null,
                mappingData);
        //connectorReq.setHost((Host)DataHolder.getInstance().getCarbonTomcatService().getTomcat().getEngine().findChild("testapp.wso2.com"));
        request.setCoyoteRequest(coyoteRequest);
    }
    
    public boolean equals(Object valve){
        return this.toString().equalsIgnoreCase(valve.toString());
    }
    
    public String toString() {
        return "valve for url-mapping";
    }
}