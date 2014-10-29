/*
* Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.hostobjects.sso;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.LogoutResponse;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.SessionIndex;
import org.opensaml.saml2.core.Subject;
import org.opensaml.xml.XMLObject;
import org.wso2.carbon.hostobjects.sso.internal.SSOConstants;
import org.wso2.carbon.hostobjects.sso.internal.SessionInfo;
import org.wso2.carbon.hostobjects.sso.internal.builder.AuthReqBuilder;
import org.wso2.carbon.hostobjects.sso.internal.builder.LogoutRequestBuilder;
import org.wso2.carbon.hostobjects.sso.internal.util.Util;

import javax.script.ScriptException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class wrap up the operations needed to write a saml relying party for sso use case.
 */
public class SAMLSSORelyingPartyObject extends ScriptableObject {
    private static final Log log = LogFactory.getLog(SAMLSSORelyingPartyObject.class);
    //stores sso properties like, identity server url,keystore path, alias, keystore password, issuerId
    private Properties ssoConfigProperties = new Properties();

    // relay state,requested uri
    private static Map<String, String> relayStateMap = new HashMap<String, String>();

    // issuerId, relyingPartyObject .this is to provide sso functionality to multiple jaggery apps.
    private static Map<String, SAMLSSORelyingPartyObject> ssoRelyingPartyMap = new HashMap<String, SAMLSSORelyingPartyObject>();

    // sessionId, sessionIndex. this is to map current session with session index sent from Identity server.
    // When log out request come from identity server,we need to invalidate the current session.
    private static Map<String, SessionInfo> sessionIdMap = new ConcurrentHashMap<String, SessionInfo>();
    //used store logged in user name until put into jaggery session
    private String loggedInUserName;


    @Override
    public String getClassName() {
        return "SSORelyingParty";
    }

    /**
     * @param cx
     * @param args      - args[0]-issuerId, this issuer need to be registered in Identity server.
     * @param ctorObj
     * @param inNewExpr
     * @return
     * @throws Exception
     */
    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj,
                                           boolean inNewExpr)
            throws Exception {
        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid arguments!, IssuerId is missing in parameters.");
        }

        SAMLSSORelyingPartyObject relyingPartyObject = ssoRelyingPartyMap.get((String) args[0]);
        if (relyingPartyObject == null) {
            relyingPartyObject = new SAMLSSORelyingPartyObject();
            relyingPartyObject.setSSOProperty(SSOConstants.ISSUER_ID, (String) args[0]);
            ssoRelyingPartyMap.put((String) args[0], relyingPartyObject);
        }
        return relyingPartyObject;
    }

    /**
     * @param cx
     * @param thisObj
     * @param args    -args[0]- SAML response xml
     * @param funObj
     * @return
     * @throws Exception
     */
    public static boolean jsFunction_validateSignature(Context cx, Scriptable thisObj,
                                                       Object[] args,
                                                       Function funObj)
            throws Exception {

        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. SAML response is missing.");
        }

        String decodedString = Util.decode((String) args[0]);

        XMLObject samlObject = Util.unmarshall(decodedString);
        String tenantDomain = Util.getDomainName(samlObject);

        int tenantId = Util.getRealmService().getTenantManager().getTenantId(tenantDomain);

        if (samlObject instanceof Response) {
            Response samlResponse = (Response) samlObject;
            SAMLSSORelyingPartyObject relyingPartyObject = (SAMLSSORelyingPartyObject) thisObj;
            return Util.validateSignature(samlResponse,
                                          relyingPartyObject.getSSOProperty(SSOConstants.KEY_STORE_NAME),
                                          relyingPartyObject.getSSOProperty(SSOConstants.KEY_STORE_PASSWORD),
                                          relyingPartyObject.getSSOProperty(SSOConstants.IDP_ALIAS),
                                          tenantId, tenantDomain);
        }
        if (log.isWarnEnabled()) {
            log.warn("SAML response in signature validation is not a SAML Response.");
        }
        return false;
    }

    /**
     * @param cx
     * @param thisObj
     * @param args    -args[0]-Logout request xml as a string.
     * @param funObj
     * @return
     * @throws Exception
     */
    public static boolean jsFunction_isLogoutRequest(Context cx, Scriptable thisObj, Object[] args,
                                                     Function funObj)
            throws Exception {
        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. Logout request xml is missing.");
        }
        String decodedString = Util.decode((String) args[0]);

        XMLObject samlObject = Util.unmarshall(decodedString);
        return samlObject instanceof LogoutRequest;

    }

    /**
     * @param cx
     * @param thisObj
     * @param args-args[0]- Logout response xml as a string
     * @param funObj
     * @return
     * @throws Exception
     */
    public static boolean jsFunction_isLogoutResponse(Context cx, Scriptable thisObj, Object[] args,
                                                      Function funObj)
            throws Exception {
        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. Logout response xml is missing.");
        }
        String decodedString = Util.decode((String) args[0]);
        XMLObject samlObject = Util.unmarshall(decodedString);
        return samlObject instanceof LogoutResponse;

    }

    /**
     * Compressing and Encoding the response
     *
     * @param cx
     * @param thisObj
     * @param args    -args[0]- string to be encoded.
     * @param funObj
     * @return
     * @throws Exception
     */
    public static String jsFunction_encode(Context cx, Scriptable thisObj, Object[] args,
                                           Function funObj)
            throws Exception {
        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. String to be encoded is missing.");
        }
        return Util.encode((String) args[0]);

    }

    public static String jsFunction_getSAMLToken(Context cx, Scriptable thisObj, Object[] args,
                                                 Function funObj)
            throws Exception {
        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. Session Id is missing.");
        }
        SAMLSSORelyingPartyObject relyingPartyObject = (SAMLSSORelyingPartyObject) thisObj;
        SessionInfo sessionInfo = relyingPartyObject.getSessionInfo((String) args[0]);
        if (sessionInfo != null) {
//            Here the samlToken is encoded. So no need to encode that again
            return sessionInfo.getSamlToken();
        }
        return null;
    }

    /**
     * Decoding and deflating the encoded AuthReq
     *
     * @param cx
     * @param thisObj
     * @param args    -args[0]-String to be decoded
     * @param funObj
     * @return
     * @throws Exception
     */
    public static String jsFunction_decode(Context cx, Scriptable thisObj, Object[] args,
                                           Function funObj)
            throws Exception {
        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. String to be decoded is missing.");
        }
        return Util.decode((String) args[0]);

    }

    /**
     * generate a UUID
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     * @throws Exception
     */
    public static String jsFunction_getUUID(Context cx, Scriptable thisObj,
                                            Object[] args,
                                            Function funObj)
            throws Exception {
        return UUID.randomUUID().toString();

    }

    /**
     * Get SAML authentication request build with given issuer
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     * @throws Exception
     */
    public static String jsFunction_getSAMLAuthRequest(Context cx, Scriptable thisObj,
                                                       Object[] args,
                                                       Function funObj)
            throws Exception {
        SAMLSSORelyingPartyObject relyingPartyObject = (SAMLSSORelyingPartyObject) thisObj;
        return Util.marshall(new AuthReqBuilder().
                buildAuthenticationRequest(relyingPartyObject.getSSOProperty(SSOConstants.ISSUER_ID)));

    }

    /**
     * Get SAML logout request build.
     *
     * @param cx
     * @param thisObj
     * @param args-args[0]-the user to be logout
     * @param funObj
     * @return
     * @throws Exception
     */
    public static String jsFunction_getSAMLLogoutRequest(Context cx, Scriptable thisObj,
                                                         Object[] args,
                                                         Function funObj)
            throws Exception {
        int argLength = args.length;
        if (argLength != 2 || !(args[0] instanceof String) &&( args[1] instanceof String)) {
            throw new ScriptException("Invalid argument. The user to be logout is missing.");
        }
        SAMLSSORelyingPartyObject relyingPartyObject = (SAMLSSORelyingPartyObject) thisObj;
        String sessionIndexId=relyingPartyObject.getSessionInfo((String)args[1]).getSessionIndex();
        return Util.marshall(new LogoutRequestBuilder().
                buildLogoutRequest((String) args[0],sessionIndexId,
                                   SSOConstants.LOGOUT_USER,
                                   relyingPartyObject.getSSOProperty(SSOConstants.ISSUER_ID)));

    }

    /**
     * Extract the name of authenticated user from SAML response.
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     * @throws Exception
     */
    public static String jsFunction_getSAMLResponseNameId(Context cx, Scriptable thisObj,
                                                          Object[] args,
                                                          Function funObj)
            throws Exception {
        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. The SAML response is missing.");
        }
        String decodedString = Util.decode((String) args[0]);
        XMLObject samlObject = Util.unmarshall(decodedString);
        String username = null;

        if (samlObject instanceof Response) {
            Response samlResponse = (Response) samlObject;
            List<Assertion> assertions = samlResponse.getAssertions();

            // extract the username
            if (assertions != null && assertions.size() > 0) {
                Subject subject = assertions.get(0).getSubject();
                if (subject != null) {
                    if (subject.getNameID() != null) {
                        username = subject.getNameID().getValue();
                    }
                }
            }
        }
        if (username == null) {
            throw new Exception("Failed to get subject assertion from SAML response.");
        }
        return username;
    }

    /**
     * Set SSO Configuration key,values
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @throws ScriptException
     */
    public static void jsFunction_setProperty(Context cx, Scriptable thisObj, Object[] args,
                                              Function funObj)
            throws ScriptException {
        int argLength = args.length;
        if (argLength != 2 || !(args[0] instanceof String) || !(args[1] instanceof String)) {
            throw new ScriptException("Invalid arguments when setting sso configuration values.");
        }
        SAMLSSORelyingPartyObject relyingPartyObject = (SAMLSSORelyingPartyObject) thisObj;
        relyingPartyObject.setSSOProperty((String) args[0], (String) args[1]);

    }

    /**
     * Check if the browser session is valid. If user is log out from any sso service provider,
     * user session is invalidated.
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     * @throws ScriptException
     */
    public static boolean jsFunction_isSessionAuthenticated(Context cx, Scriptable thisObj,
                                                            Object[] args,
                                                            Function funObj)
            throws ScriptException {
        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. Session id is missing.");
        }
        SAMLSSORelyingPartyObject relyingPartyObject = (SAMLSSORelyingPartyObject) thisObj;
        return relyingPartyObject.isSessionIdExists((String) args[0]);

    }

    public static String jsFunction_getIdentitySessionId(Context cx, Scriptable thisObj,
            Object[] args,
            Function funObj) throws ScriptException {
    	 String identitySession = null;
    	 int argLength = args.length;
         if (argLength != 1 || !(args[0] instanceof String)) {
             throw new ScriptException("Invalid argument. Session id is missing.");
         }
         SAMLSSORelyingPartyObject relyingPartyObject = (SAMLSSORelyingPartyObject) thisObj;
         SessionInfo sessionInfo = relyingPartyObject.getSessionInfo((String) args[0]);
         if (sessionInfo != null) {
        	 identitySession = sessionInfo.getSessionId();
         }
         return identitySession;
    }

    public static String jsFunction_getLoggedInUser(Context cx, Scriptable thisObj,
                                                    Object[] args,
                                                    Function funObj)
            throws ScriptException {
        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. Session id is missing.");
        }
        SAMLSSORelyingPartyObject relyingPartyObject = (SAMLSSORelyingPartyObject) thisObj;
        SessionInfo sessionInfo = relyingPartyObject.getSessionInfo((String) args[0]);
        String loggedInUser = null;
        if (sessionInfo != null && sessionInfo.getLoggedInUser() != null) {
            loggedInUser = sessionInfo.getLoggedInUser();
        }
        return loggedInUser;

    }

    /**
     * Invalidate current browser authenticated session based on SAML log out request session index value.
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @throws Exception
     */
    public static void jsFunction_invalidateSessionBySAMLResponse(Context cx, Scriptable thisObj,
                                                                  Object[] args,
                                                                  Function funObj)
            throws Exception {
        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. SAML log out request is missing.");
        }
        String decodedString = Util.decode((String) args[0]);

        SAMLSSORelyingPartyObject relyingPartyObject = (SAMLSSORelyingPartyObject) thisObj;
        XMLObject samlObject = Util.unmarshall(decodedString);
        String sessionIndex = null;
        if (samlObject instanceof LogoutRequest) {
            // if log out request
            LogoutRequest samlLogoutRequest = (LogoutRequest) samlObject;
            List<SessionIndex> sessionIndexes = samlLogoutRequest.getSessionIndexes();
            if (sessionIndexes != null && sessionIndexes.size() > 0) {
                sessionIndex = sessionIndexes.get(0).getSessionIndex();
            }
        }

        if (sessionIndex == null) {
            throw new Exception("Failed to get session index from session indexes in SAML logout request.");
        }

        relyingPartyObject.invalidateSessionBySessionIndex(sessionIndex);
        // this is to invalidate relying party object after user log out. To release memory allocations.
        invalidateRelyingPartyObject(relyingPartyObject.getSSOProperty(SSOConstants.ISSUER_ID));
    }

    /**
     * Invalidate current browser authenticated session based on session id.
     * Session will be invalidated after user log out request get succeeded.
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @throws Exception
     */
    public static void jsFunction_invalidateSessionBySessionId(Context cx, Scriptable thisObj,
                                                               Object[] args,
                                                               Function funObj)
            throws Exception {
        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. Session id is missing.");
        }
        SAMLSSORelyingPartyObject relyingPartyObject = (SAMLSSORelyingPartyObject) thisObj;

        relyingPartyObject.invalidateSessionBySessionId((String) args[0]);
        // this is to invalidate relying party object after user log out. To release memory allocations.
        invalidateRelyingPartyObject(relyingPartyObject.getSSOProperty(SSOConstants.ISSUER_ID));

    }

    /**
     * Set the current session as authenticated by mapping with current session id to session index.
     *
     * @param cx
     * @param thisObj
     * @param args    -args[0]- current session id, args[1]-SAML response
     * @param funObj
     * @throws Exception
     */
    public static void jsFunction_setSessionAuthenticated(Context cx, Scriptable thisObj,
                                                          Object[] args,
                                                          Function funObj)
            throws Exception {
        int argLength = args.length;
        if (argLength != 2 || !(args[0] instanceof String) || !(args[1] instanceof String)) {
            throw new ScriptException("Invalid argument. Current session id and SAML response are missing.");
        }
        String decodedString = Util.decode((String) args[1]);
        SAMLSSORelyingPartyObject relyingPartyObject = (SAMLSSORelyingPartyObject) thisObj;
        XMLObject samlObject = Util.unmarshall(decodedString);
        String sessionIndex = null;
        String username = null;
        if (samlObject instanceof Response) {
            Response samlResponse = (Response) samlObject;
            List<Assertion> assertions = samlResponse.getAssertions();

            // extract the session index
            if (assertions != null && assertions.size() > 0) {
                List<AuthnStatement> authenticationStatements = assertions.get(0).getAuthnStatements();
                AuthnStatement authnStatement = authenticationStatements.get(0);
                if (authnStatement != null) {
                    if (authnStatement.getSessionIndex() != null) {
                        sessionIndex = authnStatement.getSessionIndex();
                    }
                }
            }

            // extract the username
            if (assertions != null && assertions.size() > 0) {
                Subject subject = assertions.get(0).getSubject();
                if (subject != null) {
                    if (subject.getNameID() != null) {
                        username = subject.getNameID().getValue();
                    }
                }
            }
        }
        if (sessionIndex == null) {
            throw new Exception("Failed to get session index from authentication statement in SAML response.");
        }
        if (username == null) {
            throw new Exception("Failed to get subject assertion from SAML response.");
        }

        SessionInfo sessionInfo = new SessionInfo((String) args[0]);
        sessionInfo.setSessionIndex(sessionIndex);
        sessionInfo.setLoggedInUser(username);
        sessionInfo.setSamlToken((String) args[1]);//We expect an encoded SamlToken here.
        relyingPartyObject.addSessionInfo(sessionInfo);

    }

    /**
     * Get SSO configuration properties.
     *
     * @param cx
     * @param thisObj
     * @param args    -args[0]-configuration key
     * @param funObj
     * @return
     * @throws ScriptException
     */
    public static String jsFunction_getProperty(Context cx, Scriptable thisObj, Object[] args,
                                                Function funObj)
            throws ScriptException {
        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. SSO configuratin key is missing.");
        }
        SAMLSSORelyingPartyObject relyingPartyObject = (SAMLSSORelyingPartyObject) thisObj;
        return relyingPartyObject.getSSOProperty((String) args[0]);

    }

    /**
     * Set relay state property with requested uri.
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @throws ScriptException
     */
    public static void jsFunction_setRelayStateProperty(Context cx, Scriptable thisObj,
                                                        Object[] args,
                                                        Function funObj)
            throws ScriptException {
        int argLength = args.length;
        if (argLength != 2 || !(args[0] instanceof String) || !(args[1] instanceof String)) {
            throw new ScriptException("Invalid argument. RelayState and requested URI are missing.");
        }
        relayStateMap.put((String) args[0], (String) args[1]);

    }

    /**
     * Get requested URI for relay state. And relay state value is removed, as relay state is unique and onetime value.
     *
     * @param cx
     * @param thisObj
     * @param args
     * @param funObj
     * @return
     * @throws ScriptException
     */
    public static String jsFunction_getRelayStateProperty(Context cx, Scriptable thisObj,
                                                          Object[] args,
                                                          Function funObj)
            throws ScriptException {
        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. Relay state value is missing.");
        }
        String requestedURI = relayStateMap.get((String) args[0]);
        relayStateMap.remove((String) args[0]);
        return requestedURI;

    }
    public static String jsFunction_xmlDecode(Context cx, Scriptable thisObj,
                                                          Object[] args,
                                                          Function funObj)
            throws ScriptException {
        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. Relay state value is missing.");
        }
        String xmlString = (String) args[0];
        xmlString = xmlString.replaceAll("&gt;", ">").replaceAll("&lt;", "<");
//                .replaceAll("&apos;", "'").replaceAll("&quot;", "\"").replaceAll("&amp;", "&");

        return xmlString;

    }
    public static String jsFunction_xmlEncode(Context cx, Scriptable thisObj,
                                                          Object[] args,
                                                          Function funObj)
            throws ScriptException {
        int argLength = args.length;
        if (argLength != 1 || !(args[0] instanceof String)) {
            throw new ScriptException("Invalid argument. Relay state value is missing.");
        }
        String xmlString = (String) args[0];
        xmlString = xmlString.replaceAll(">","&gt;").replaceAll( "<","&lt;")                  ;
//                .replaceAll("'","&apos;").replaceAll("\"","&quot;").replaceAll("&","&amp;");

        return xmlString;

    }

    private String getSSOProperty(String key) {
        return ssoConfigProperties.getProperty(key);
    }

    private void setSSOProperty(String key, String value) {
        ssoConfigProperties.put(key, value);
    }

    /**
     * Decode xml
     *
     * @param xmlString
     * @return
     */
    public static String decode(String xmlString) {
        xmlString = xmlString.replaceAll("&gt;", ">").replaceAll("&lt;", "<").
                replaceAll("&apos;", "'").replaceAll("&quot;", "\"").replaceAll("&amp;", "&");

        return xmlString;
    }

    /**
     * Add current browser session with session index.
     */
    private void addSessionInfo(SessionInfo sessionInfo) {
        sessionIdMap.put(sessionInfo.getSessionId(), sessionInfo);
    }

    /**
     * Remove current browser session(s) mapped with session index given.
     *
     * @param sessionIndex
     */
    private void invalidateSessionBySessionIndex(String sessionIndex) {
        for (Map.Entry entry : sessionIdMap.entrySet()) {
            if (entry.getValue() instanceof SessionInfo) {
                SessionInfo sessionInfo = (SessionInfo) entry.getValue();
                if (sessionInfo != null && sessionIndex.equals(sessionInfo.getSessionIndex())) {
                    sessionIdMap.remove(entry.getKey());
                }
            }
        }
    }

    private void invalidateSessionBySessionId(String sessionId) {
        sessionIdMap.remove(sessionId);
    }

    private boolean isSessionIdExists(String sessionId) {
        return sessionIdMap.containsKey(sessionId);
    }

    private SessionInfo getSessionInfo(String sessionId) {
        return sessionIdMap.get(sessionId);
    }

    /**
     * Remove relying party object added with issuerId.
     *
     * @param issuerId
     */
    private static void invalidateRelyingPartyObject(String issuerId) {
        ssoRelyingPartyMap.remove(issuerId);
    }

}