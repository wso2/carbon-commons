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
package org.wso2.carbon.identity.authenticator.saml2.sso.common.builders;

import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.signature.XMLSignature;
import org.joda.time.DateTime;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.SessionIndex;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml2.core.impl.NameIDBuilder;
import org.opensaml.saml2.core.impl.SessionIndexBuilder;
import org.wso2.carbon.identity.authenticator.saml2.sso.common.SAML2SSOAuthenticatorConstants;
import org.wso2.carbon.identity.authenticator.saml2.sso.common.Util;

/**
 * This class is used to generate the Logout Requests.
 */
public class LogoutRequestBuilder {
	
	private static Log log = LogFactory.getLog(LogoutRequestBuilder.class);

    /**
     * Build the logout request
     * @param subject name of the user
     * @param reason reason for generating logout request.
     * @return LogoutRequest object
     * @throws Exception 
     */
    public LogoutRequest buildLogoutRequest(String subject, String reason, String sessionIndexStr) throws Exception {
    	log.info("Building logout request");
        Util.doBootstrap();
        LogoutRequest logoutReq = new org.opensaml.saml2.core.impl.LogoutRequestBuilder().buildObject();
        logoutReq.setID(Util.createID());
        logoutReq.setDestination(Util.getIdentityProviderSSOServiceURL());
        
        DateTime issueInstant = new DateTime();
        logoutReq.setIssueInstant(issueInstant);
        logoutReq.setNotOnOrAfter(new DateTime(issueInstant.getMillis() + 5 * 60 * 1000));

        IssuerBuilder issuerBuilder = new IssuerBuilder();
        Issuer issuer = issuerBuilder.buildObject();
        issuer.setValue(Util.getServiceProviderId());
        logoutReq.setIssuer(issuer);

        NameID nameId = new NameIDBuilder().buildObject();
        nameId.setFormat(SAML2SSOAuthenticatorConstants.SAML2_NAME_ID_POLICY_TRANSIENT);
        nameId.setValue(subject);
        logoutReq.setNameID(nameId);

        SessionIndex sessionIndex = new SessionIndexBuilder().buildObject();
        sessionIndex.setSessionIndex(sessionIndexStr);
        logoutReq.getSessionIndexes().add(sessionIndex);

        logoutReq.setReason(reason);
        
        Util.setSignature(logoutReq, XMLSignature.ALGO_ID_SIGNATURE_RSA, new SignKeyDataHolder());

        return logoutReq;
    }
}
