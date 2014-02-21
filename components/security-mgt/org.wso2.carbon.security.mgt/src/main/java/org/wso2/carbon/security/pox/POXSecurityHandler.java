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

package org.wso2.carbon.security.pox;

import java.io.IOException;
import org.apache.axiom.om.impl.dom.jaxp.DocumentBuilderFactoryImpl;
import org.apache.axiom.om.util.Base64;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.rampart.util.Axis2Util;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecTimestamp;
import org.apache.ws.security.message.WSSecUsernameToken;
import org.w3c.dom.Document;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.security.SecurityConfigException;
import org.wso2.carbon.security.SecurityConstants;
import org.wso2.carbon.security.config.SecurityConfigAdmin;
import org.wso2.carbon.security.config.service.SecurityScenarioData;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Handler to convert the HTTP basic auth information into
 * <code>wsse:UsernameToken</code>
 */
public class POXSecurityHandler implements Handler {

    private static Log log = LogFactory.getLog(POXSecurityHandler.class);
    private static String POX_SECURITY_MODULE = "POXSecurityModule";
    public static final String POX_CACHE_MANAGER = "POX_CACHE_MANAGER";
    public static final String POX_ENABLED = "pox-security";

    private HandlerDescription description;

    /**
     * @see org.apache.axis2.engine.Handler#cleanup()
     */
    public void cleanup() {
    }

    /**
     * @see org.apache.axis2.engine.Handler#init(org.apache.axis2.description.HandlerDescription)
     */
    public void init(HandlerDescription description) {
        this.description = description;
    }

    /**
     * @see org.apache.axis2.engine.Handler#invoke(org.apache.axis2.context.MessageContext)
     */
    public InvocationResponse invoke(MessageContext msgCtx) throws AxisFault {

        if (msgCtx.isFault()) {
            // we only need to execute this block in Unauthorized situations when basicAuth used
            // otherwise it should continue the message flow by throwing the incoming fault message since
            // this is already a fault response - ESBJAVA-2731
            try {
                SecurityConfigAdmin securityAdmin = new SecurityConfigAdmin(msgCtx.getConfigurationContext().getAxisConfiguration());
                SecurityScenarioData data = securityAdmin.getCurrentScenario(msgCtx.getAxisService().getName());
                if (data != null && data.getScenarioId().equals(SecurityConstants.USERNAME_TOKEN_SCENARIO_ID)) {
                    String servername = ServerConfiguration.getInstance().getFirstProperty("Name");
                    if (servername == null || servername.trim().length() == 0) {
                        servername = "WSO2 Carbon";
                    }
                    try {
                        HttpServletResponse response = (HttpServletResponse)
                                msgCtx.getProperty(HTTPConstants.MC_HTTP_SERVLETRESPONSE);
                        if (response != null) {
                            response.setContentLength(0);
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.addHeader("WWW-Authenticate",
                                    "BASIC realm=\"" + servername + "\"");
                            response.flushBuffer();
                        } else {
                            // if not servlet transport assume it to be nhttp transport
                            msgCtx.setProperty("NIO-ACK-Requested", "true");
                            msgCtx.setProperty("HTTP_SC", HttpServletResponse.SC_UNAUTHORIZED);
                            Map<String, String> responseHeaders = new HashMap<String, String>();
                            responseHeaders.put("WWW-Authenticate",
                                    "BASIC realm=\"" + servername + "\"");
                            msgCtx.setProperty(MessageContext.TRANSPORT_HEADERS, responseHeaders);
                        }
                        return InvocationResponse.CONTINUE;
                    } catch (IOException e1) {
                        throw new AxisFault("System error", e1);
                    }
                }
            } catch (Exception e) {
                // throwing the same fault which returned by the messageCtx
                throw new AxisFault("System error", msgCtx.getFailureReason());
            } finally {
                return InvocationResponse.CONTINUE;
            }
        }
        
        if (msgCtx != null && !msgCtx.isEngaged(POX_SECURITY_MODULE)){
            return InvocationResponse.CONTINUE;
        }

        if (msgCtx == null || msgCtx.getIncomingTransportName() == null) {
            return InvocationResponse.CONTINUE;
        }

        String basicAuthHeader = getBasicAuthHeaders(msgCtx);

         //this handler only intercepts
        if (!(msgCtx.isDoingREST() || isSOAPWithoutSecHeader(msgCtx)) ||
		!msgCtx.getIncomingTransportName().equals("https")) {
            return InvocationResponse.CONTINUE;
        }

        //Then check whether UT auth is enabled on the service
        AxisService service = msgCtx.getAxisService();

        if (service == null) {
            if(log.isDebugEnabled()) {
                log.debug("Service not dispatched");
            }
            return InvocationResponse.CONTINUE;
        }

        
        // We do not add details of admin services to the registry, hence if a rest call comes to a
        // admin service that does not require authentication we simply skip it
        String isAdminService = (String) service.getParameterValue("adminService");
        if (isAdminService != null) {
            if (JavaUtils.isTrueExplicitly(isAdminService)) {
                return InvocationResponse.CONTINUE;
            }
        }
        
        String isHiddenService = (String) service.getParameterValue("hiddenService");
        if (isHiddenService != null) {
            if (JavaUtils.isTrueExplicitly(isHiddenService)) {
                return InvocationResponse.CONTINUE;
            }
        }

        String isReverseProxy = System.getProperty("reverseProxyMode");
        if (isReverseProxy != null) {
            if (JavaUtils.isTrueExplicitly(isReverseProxy)) {
                return InvocationResponse.CONTINUE;
            }
        }

        String isPox = null;

        Cache<String, String> cache = this.getPOXCache();

        if(cache != null){
        	if(cache.get(service.getName()) != null) {
        		isPox = cache.get(service.getName());
        	}
        }

        if (isPox != null && JavaUtils.isFalseExplicitly(isPox)) {
            return InvocationResponse.CONTINUE;
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Admin service check failed OR cache miss");
        }

        try {
            String scenarioID = null;
            try{
                scenarioID = (String)service.getParameter(SecurityConstants.SCENARIO_ID_PARAM_NAME).getValue();
            }catch (Exception e){}//ignore

            if(scenarioID == null){
                synchronized (this){
                    SecurityConfigAdmin securityAdmin = new SecurityConfigAdmin(msgCtx.
                                                          getConfigurationContext().getAxisConfiguration());
                    SecurityScenarioData data = securityAdmin.getCurrentScenario(service.getName());
                    if(data != null){
                        scenarioID = data.getScenarioId();
                        try {
                            Parameter param = new Parameter();
                            param.setName(SecurityConstants.SCENARIO_ID_PARAM_NAME);
                            param.setValue(scenarioID);
                            service.addParameter(param);
                        } catch (AxisFault axisFault) {
                            log.error("Error while adding Scenario ID parameter",axisFault);
                        }
                    }
                }
            }

            if (scenarioID != null && scenarioID.equals(SecurityConstants.USERNAME_TOKEN_SCENARIO_ID)) {
                if (log.isDebugEnabled()) {
                    log.debug("Processing POX security");
                }
            } else {
                if(cache != null){
                    cache.put(service.getName(), "false");
                }
                return InvocationResponse.CONTINUE;
            }
            String username = null;
            String password = null;
            if (basicAuthHeader != null && basicAuthHeader.startsWith("Basic ")) {
                basicAuthHeader = new String(Base64.decode(basicAuthHeader.substring(6)));
                int i = basicAuthHeader.indexOf(':');
                if (i == -1) {
                    username = basicAuthHeader;
                } else {
                    username = basicAuthHeader.substring(0, i);
                }

                if (i != -1) {
                    password = basicAuthHeader.substring(i + 1);
                    if (password != null && password.equals("")) {
                        password = null;
                    }
                }
            }

            if (username == null || password == null || password.trim().length() == 0
                    || username.trim().length() == 0) {
                
                
                String servername = ServerConfiguration.getInstance().getFirstProperty("Name");
                
                if(servername == null || servername.trim().length() == 0){
                    servername = "WSO2 Carbon";
                }

                HttpServletResponse response = (HttpServletResponse)
                        msgCtx.getProperty(HTTPConstants.MC_HTTP_SERVLETRESPONSE);
                if (response != null) {
                    response.setContentLength(0);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.addHeader("WWW-Authenticate",
                            "BASIC realm=\""+servername+"\"");
                    response.flushBuffer();
                } else {
                    // if not servlet transport assume it to be nhttp transport
                    msgCtx.setProperty("NIO-ACK-Requested", "true");
                    msgCtx.setProperty("HTTP_SC", HttpServletResponse.SC_UNAUTHORIZED);
                    Map<String, String> responseHeaders = new HashMap<String, String>();
                    responseHeaders.put("WWW-Authenticate",
                            "BASIC realm=\""+servername+"\"");
                    msgCtx.setProperty(MessageContext.TRANSPORT_HEADERS, responseHeaders);
                }
                
                return InvocationResponse.ABORT;
            }

            
            Document doc = Axis2Util.getDocumentFromSOAPEnvelope(msgCtx.getEnvelope(), true);

            WSSecHeader secHeader = new WSSecHeader();
            secHeader.insertSecurityHeader(doc);

            WSSecUsernameToken utBuilder = new WSSecUsernameToken();
            utBuilder.setPasswordType(WSConstants.PASSWORD_TEXT);
            utBuilder.setUserInfo(username, password);
            utBuilder.build(doc, secHeader);

            WSSecTimestamp tsBuilder = new WSSecTimestamp();
            tsBuilder.build(doc, secHeader);

            /**
             * Set the new SOAPEnvelope
             */
            msgCtx.setEnvelope(Axis2Util.getSOAPEnvelopeFromDOMDocument(doc, false));
        } catch (AxisFault e) {
            throw e;
        } catch (WSSecurityException wssEx) {
            throw new AxisFault("WSDoAllReceiver: Error in converting to Document", wssEx);
        } catch (Exception e) {
            throw new AxisFault("System error", e);
        }
        return InvocationResponse.CONTINUE;
    }

    /**
     *
     * @param msgCtx   message going through the handler chain
     * @return true if its a soap message without a security header
     */
    private boolean isSOAPWithoutSecHeader(MessageContext msgCtx) {
        //see whether security header present: if so return false
        SOAPHeader soapHeader = msgCtx.getEnvelope().getHeader();
        if (soapHeader == null) {
           return true; // no security header
        }
        //getting the set of secuirty headers
        ArrayList headerBlocks = soapHeader.getHeaderBlocksWithNSURI(WSConstants.WSSE_NS);
        // Issue is axiom - a returned collection must not be null
        if (headerBlocks != null) {
            Iterator headerBlocksIterator = headerBlocks.iterator();
            while (headerBlocksIterator.hasNext()) {
                SOAPHeaderBlock elem = (SOAPHeaderBlock) headerBlocksIterator.next();
                if (WSConstants.WSSE_LN.equals(elem.getLocalName())) {
                    return false; // security header already present. invalid request.
                }
            }
        }
        return true;
    }

    /**
     * Utility method to return basic auth transport headers if present
     * @return
     */
    private String getBasicAuthHeaders(MessageContext msgCtx) {

        Map map = (Map) msgCtx.getProperty(MessageContext.TRANSPORT_HEADERS);
        if(map == null) {
            return null;
        }
        String tmp =   (String) map.get("Authorization");
        if (tmp == null) {
            tmp = (String) map.get("authorization");
        }
        if (tmp != null && tmp.trim().startsWith("Basic ")) {
            return tmp;
        } else {
            return null;
        }
    }

    public void flowComplete(MessageContext msgContext) {
    }

    /**
     * @see org.apache.axis2.engine.Handler#getHandlerDesc()
     */
    public HandlerDescription getHandlerDesc() {
        return this.description;
    }

    /**
     * @see org.apache.axis2.engine.Handler#getName()
     */
    public String getName() {
        return "REST/POX Security handler";
    }

    /**
     * @see org.apache.axis2.engine.Handler#getParameter(java.lang.String)
     */
    public Parameter getParameter(String name) {
        return this.description.getParameter(name);
    }
    /**
     * Returns the default "POX_ENABLED" cache
     *
     */
    private Cache<String, String> getPOXCache() {
        CacheManager manager = Caching.getCacheManagerFactory().getCacheManager(POXSecurityHandler.POX_CACHE_MANAGER);
        Cache<String, String> cache = manager.getCache(POXSecurityHandler.POX_ENABLED);
    	return cache;
    }
}
