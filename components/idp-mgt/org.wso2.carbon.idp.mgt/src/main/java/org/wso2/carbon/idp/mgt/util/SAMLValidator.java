/*
*Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.idp.mgt.util;

import edu.emory.mathcs.backport.java.util.Arrays;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.*;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.validation.ValidationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.idp.mgt.dto.TrustedIdPDTO;
import org.wso2.carbon.idp.mgt.exception.IdentityProviderMgtException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class SAMLValidator {

    private static final Log log = LogFactory.getLog(SAMLValidator.class);

    private static boolean bootstrapped = false;

    public static boolean validateSAMLResponse(TrustedIdPDTO trustedIdPDTO, String samlResponseString, String[] audiences,
                                               boolean validateResponseSignature, boolean validateAssertionSignature)
            throws IdentityProviderMgtException {

        Thread thread = null;
        ClassLoader loader = null;
        try {
            if(!bootstrapped){
                thread = Thread.currentThread();
                loader = thread.getContextClassLoader();
                thread.setContextClassLoader(SAMLValidator.class.getClassLoader());
                DefaultBootstrap.bootstrap();
            }
        } catch (ConfigurationException e) {
            String msg = "Error bootstrapping OpenSAML library";
            log.error(msg, e);
            throw new IdentityProviderMgtException(msg);
        } finally {
            thread.setContextClassLoader(loader);
        }

        if(log.isDebugEnabled()){
            String message = "Encoded SAML Response string: " + samlResponseString;
            log.debug(message);
        }

        Response samlResponse = null;
        samlResponse = (Response) unmarshall(new String(Base64.decode(samlResponseString)));

        if (samlResponse.getStatus() != null &&
                samlResponse.getStatus().getStatusCode() != null &&
                samlResponse.getStatus().getStatusCode().getValue().equals(IdentityProviderMgtConstants.StatusCodes.IDENTITY_PROVIDER_ERROR) &&
                samlResponse.getStatus().getStatusCode().getStatusCode() != null &&
                samlResponse.getStatus().getStatusCode().getStatusCode().getValue().equals(IdentityProviderMgtConstants.StatusCodes.NO_PASSIVE)) {
            return false;
        }

        List<Assertion> assertions = samlResponse.getAssertions();
        Assertion assertion = null;
        if (assertions != null && assertions.size() > 0) {
            assertion = assertions.get(0);
        }
        if (assertion == null) {
            if(log.isDebugEnabled()){
                String message = "SAML Assertion not found in the Response";
                log.debug(message);
            }
            return false;
        }

        // Validate Issuer Id
        if(!assertion.getIssuer().getValue().equals(trustedIdPDTO.getIdpEntityId())){
            if(log.isDebugEnabled()){
                String message = "Invalid IssuerId";
                log.debug(message);
            }
            return false;
        }

        String subject = null;
        if(assertion.getSubject() != null && assertion.getSubject().getNameID() != null){
            subject = assertion.getSubject().getNameID().getValue();
        }

        if(subject == null){
            if(log.isDebugEnabled()){
                String message = "SAML Response does not contain the name of the subject";
                log.debug(message);
            }
            return false;
        }

        // validate audience restriction
        validateAudienceRestriction(assertion, trustedIdPDTO, audiences);

        X509Certificate cert = null;
        try {
            cert = (X509Certificate) IdentityProviderMgtUtil.getCertificate(trustedIdPDTO.getPublicCert());
        } catch (IdentityProviderMgtException e) {
            if(log.isDebugEnabled()){
                String message = "Error while retrieving trusted certificate of IdP " + trustedIdPDTO.getIdpEntityId();
                log.debug(message);
            }
            return  false;
        }
        Credential credential = new X509CredentialImpl(cert);

        // Validate Response Signature
        if(validateResponseSignature){
            validateResponseSignature(samlResponse, credential);
        }

        // Validate Assertion Signature
        if(validateAssertionSignature){
            validateAssertionSignature(assertion, credential);
        }

        return true;
    }

    public static boolean validateSAMLAssertion(TrustedIdPDTO trustedIdPDTO, String samlAssertionString, String[] audiences,
                                                boolean validateAssertionSignature) throws IdentityProviderMgtException {

        try {
            if(!bootstrapped){
                DefaultBootstrap.bootstrap();
            }
        } catch (ConfigurationException e) {
            String msg = "Error bootstrapping OpenSAML library";
            log.error(msg, e);
            throw new IdentityProviderMgtException(msg);
        }

        if(log.isDebugEnabled()){
            String message = "Encoded SAML Assertion string: " + samlAssertionString;
            log.debug(message);
        }

        Assertion assertion = (Assertion) unmarshall(new String(Base64.decode(samlAssertionString)));

        // Validate Issuer Id
        if(!assertion.getIssuer().getValue().equals(trustedIdPDTO.getIdpEntityId())){
            if(log.isDebugEnabled()){
                String message = "Invalid IssuerId";
                log.debug(message);
            }
            return false;
        }

        String subject = null;
        if(assertion.getSubject() != null && assertion.getSubject().getNameID() != null){
            subject = assertion.getSubject().getNameID().getValue();
        }

        if(subject == null){
            if(log.isDebugEnabled()){
                String message = "SAML Response does not contain the name of the subject";
                log.debug(message);
            }
            return false;
        }

        // validate audience restriction
        validateAudienceRestriction(assertion, trustedIdPDTO, audiences);

        X509Certificate cert = null;
        try {
            cert = (X509Certificate) IdentityProviderMgtUtil.getCertificate(trustedIdPDTO.getPublicCert());
        } catch (IdentityProviderMgtException e) {
            if(log.isDebugEnabled()){
                String message = "Error while retrieving trusted certificate of IdP " + trustedIdPDTO.getIdpEntityId();
                log.debug(message);
            }
            return  false;
        }
        Credential credential = new X509CredentialImpl(cert);

        // validate signature this SP only looking for assertion signature
        if(validateAssertionSignature){
            validateAssertionSignature(assertion, credential);
        }

        return true;
    }
    private static XMLObject unmarshall(String samlString) throws IdentityProviderMgtException {

        String decodedString = decodeHTMLCharacters(samlString);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        try {
            DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
            ByteArrayInputStream is = new ByteArrayInputStream(decodedString.getBytes());
            Document document = docBuilder.parse(is);
            Element element = document.getDocumentElement();
            UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
            Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
            return unmarshaller.unmarshall(element);
        } catch (Exception e) {
            if(log.isDebugEnabled()){
                log.debug(e.getMessage(), e);
            }
            throw new IdentityProviderMgtException(e.getMessage());
        }
    }

    /**
     * Validate the AudienceRestriction of SAML2 Response
     *
     * @param assertion SAML2 Assertion
     * @return true if audience valid, false otherwise
     */
    private static boolean validateAudienceRestriction(Assertion assertion, TrustedIdPDTO trustedIdPDTO, String[] audiences) {

        List<String> requestedAudiences = null;
        if(audiences != null && audiences.length > 0){
            requestedAudiences = new ArrayList<String>(Arrays.asList(audiences));
        }

        if(requestedAudiences != null && requestedAudiences.size() > 0){
            for(String requestedAudience : requestedAudiences){
                Conditions conditions = assertion.getConditions();
                if (conditions != null) {
                    List<AudienceRestriction> audienceRestrictions = conditions.getAudienceRestrictions();
                    if (audienceRestrictions != null && !audienceRestrictions.isEmpty()) {
                        boolean audienceFound = false;
                        for (AudienceRestriction audienceRestriction : audienceRestrictions) {
                            if (audienceRestriction.getAudiences() != null && audienceRestriction.getAudiences().size() > 0) {
                                for(Audience audience: audienceRestriction.getAudiences()){
                                    if(audience.getAudienceURI().equals(requestedAudience)){
                                        audienceFound = true;
                                        break;
                                    }
                                }
                            } else {
                                if(log.isDebugEnabled()){
                                    String message = "SAML Response's AudienceRestriction doesn't contain Audiences";
                                    log.debug(message);
                                }
                                return false;
                            }
                            if(audienceFound){
                                break;
                            }
                        }
                        if(!audienceFound){
                            if(log.isDebugEnabled()){
                                String message = "SAML Assertion Audience Restriction validation failed";
                                log.debug(message);
                            }
                            return false;
                        }
                    } else {
                        if(log.isDebugEnabled()){
                            String message = "SAML Response doesn't contain AudienceRestrictions";
                            log.debug(message);
                        }
                        return false;
                    }
                } else {
                    if(log.isDebugEnabled()){
                        String message = "SAML Response doesn't contain Conditions";
                        log.debug(message);
                    }
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Validate the signature of a SAML2 Assertion
     *
     * @param response   SAML2 Response
     * @return true, if signature is valid, false otherwise
     */
    private static boolean validateResponseSignature(Response response, Credential credential) {

        if(response.getSignature() == null){
            if(log.isDebugEnabled()){
                String message = "Signature element is not found in SAML Response element";
                log.debug(message);
            }
            return false;
        } else {
            try {
                SignatureValidator validator = new SignatureValidator(credential);
                validator.validate(response.getSignature());
            }  catch (ValidationException e) {
                if(log.isDebugEnabled()){
                    String message = "Signature validation failed for SAML Response";
                    log.debug(message);
                }
                return false;
            }
        }
        return true;
    }

    /**
     * Validate the signature of a SAML2 Assertion
     *
     * @param assertion   SAML2 Assertion
     * @return true, if signature is valid, false otherwise
     */
    private static boolean validateAssertionSignature(Assertion assertion, Credential credential) {

        if(assertion.getSignature() == null){
            if(log.isDebugEnabled()){
                String message = "Signature element is not found in SAML Assertion element";
                log.debug(message);
            }
            return false;
        } else {
            try {
                SignatureValidator validator = new SignatureValidator(credential);
                validator.validate(assertion.getSignature());
            }  catch (ValidationException e) {
                if(log.isDebugEnabled()){
                    String message = "Signature validation failed for SAML Assertion";
                    log.debug(message);
                }
                return false;
            }
        }
        return true;
    }

    private static String decodeHTMLCharacters(String encodedStr) {
        return encodedStr.replaceAll("&amp;", "&").replaceAll("&lt;", "<").replaceAll("&gt;", ">")
                .replaceAll("&quot;", "\"").replaceAll("&apos;", "'");

    }

}
