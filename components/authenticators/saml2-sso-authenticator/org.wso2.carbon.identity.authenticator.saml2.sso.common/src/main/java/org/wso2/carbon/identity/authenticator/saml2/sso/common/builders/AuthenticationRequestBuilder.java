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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.signature.XMLSignature;
import org.joda.time.DateTime;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml1.core.NameIdentifier;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.NameIDPolicy;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml2.core.impl.NameIDBuilder;
import org.opensaml.saml2.core.impl.NameIDPolicyBuilder;
import org.opensaml.saml2.core.impl.SubjectBuilder;
import org.wso2.carbon.identity.authenticator.saml2.sso.common.SAML2SSOAuthenticatorConstants;
import org.wso2.carbon.identity.authenticator.saml2.sso.common.Util;

/**
 * This class is used to generate Authentication Requests. When there is an unauthenticated user
 * trying to access the carbon mgt-console, he will be redirected to identity provider after setting
 * an authentication request to the http request.
 */
public class AuthenticationRequestBuilder {

    private static Log log = LogFactory.getLog(AuthenticationRequestBuilder.class);

    /**
     * Generate an authentication request.
     * 
     * @return AuthnRequest Object
     * @throws org.wso2.carbon.identity.authenticator.saml2.sso.ui.SAML2SSOUIAuthenticatorException
     *             error when bootstrapping
     */
    public AuthnRequest buildAuthenticationRequest(String subjectName, String nameIdPolicyFormat)
            throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Building Authentication Request");
        }
        Util.doBootstrap();
        AuthnRequest authnRequest = (AuthnRequest) Util
                .buildXMLObject(AuthnRequest.DEFAULT_ELEMENT_NAME);
        authnRequest.setID(Util.createID());
        authnRequest.setVersion(SAMLVersion.VERSION_20);
        authnRequest.setIssueInstant(new DateTime());
        authnRequest.setIssuer(buildIssuer());
        authnRequest.setNameIDPolicy(buildNameIDPolicy(nameIdPolicyFormat));
        authnRequest.setDestination(Util.getIdentityProviderSSOServiceURL());

        if (subjectName != null) {
            Subject subject = new SubjectBuilder().buildObject();
            NameID nameId = new NameIDBuilder().buildObject();
            nameId.setValue(subjectName);
            nameId.setFormat(NameIdentifier.EMAIL);
            subject.setNameID(nameId);
            authnRequest.setSubject(subject);

        }

        Util.setSignature(authnRequest, XMLSignature.ALGO_ID_SIGNATURE_RSA, new SignKeyDataHolder());

        return authnRequest;
    }

    /**
     * Build the issuer object
     * 
     * @return Issuer object
     */
    private static Issuer buildIssuer() {
        IssuerBuilder issuerBuilder = new IssuerBuilder();
        Issuer issuer = issuerBuilder.buildObject();
        issuer.setValue(Util.getServiceProviderId());
        return issuer;
    }

    /**
     * Build the NameIDPolicy object
     * 
     * @return NameIDPolicy object
     */
    private static NameIDPolicy buildNameIDPolicy(String nameIdPolicyFormat) {
		NameIDPolicy nameIDPolicy = new NameIDPolicyBuilder().buildObject();
        if (nameIdPolicyFormat == null) {
            nameIdPolicyFormat = SAML2SSOAuthenticatorConstants.SAML2_NAME_ID_POLICY_UNSPECIFIED;
        }
		nameIDPolicy.setFormat(nameIdPolicyFormat);
		nameIDPolicy.setAllowCreate(true);
		return nameIDPolicy;
	}
}
