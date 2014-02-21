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

package org.wso2.carbon.security.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.dom.DOOMAbstractFactory;
import org.apache.axiom.om.util.Base64;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.keys.content.KeyName;
//import org.wso2.xkms2.Authentication;
//import org.wso2.xkms2.KeyBinding;
//import org.wso2.xkms2.LocateRequest;
//import org.wso2.xkms2.LocateResult;
//import org.wso2.xkms2.PrototypeKeyBinding;
//import org.wso2.xkms2.QueryKeyBinding;
//import org.wso2.xkms2.RecoverKeyBinding;
//import org.wso2.xkms2.RecoverRequest;
//import org.wso2.xkms2.RecoverResult;
//import org.wso2.xkms2.RegisterRequest;
//import org.wso2.xkms2.ReissueKeyBinding;
//import org.wso2.xkms2.ReissueRequest;
//import org.wso2.xkms2.RespondWith;
//import org.wso2.xkms2.ResultMinor;
//import org.wso2.xkms2.Status;
//import org.wso2.xkms2.StatusValue;
//import org.wso2.xkms2.UnverifiedKeyBinding;
//import org.wso2.xkms2.UseKeyWith;
//import org.wso2.xkms2.ValidateRequest;
//import org.wso2.xkms2.ValidateResult;
//import org.wso2.xkms2.XKMSException;
//import org.wso2.xkms2.builder.LocateResultBuilder;
//import org.wso2.xkms2.builder.RecoverResultBuilder;
//import org.wso2.xkms2.builder.ValidateResultBuilder;
//import org.wso2.xkms2.util.XKMSKeyUtil;
//import org.wso2.xkms2.util.XKMSUtil;

import javax.xml.stream.XMLInputFactory;
import java.io.ByteArrayInputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;

public class XKMSCryptoClient {
    private static final Log LOG = LogFactory.getLog(XKMSCryptoClient.class
            .getName());

    public static PrivateKey getPrivateKey(String alias, String serverURL,
                                           String passPhrase) {

//        try {
//
//            RecoverRequest request = createRecoverRequest();
//            request.setServiceURI(serverURL);
//
//            Authentication authentication = new Authentication();
//            Key authenKey = XKMSKeyUtil.getAuthenticationKey(passPhrase);
//            authentication.setKeyBindingAuthenticationKey(authenKey);
//            request.setAuthentication(authentication);
//
//            RecoverKeyBinding keyBinding = createRecoverKeyBinding();
//            keyBinding.setKeyName(alias);
//
//            Status status = new Status();
//            status.setStatusValue(StatusValue.INDETERMINATE);
//            keyBinding.setStatus(status);
//
//            request.setRecoverKeyBinding(keyBinding);
//
//            request.addRespondWith(RespondWith.PRIVATE_KEY);
//
//            OMElement element = getAsOMElement(request);
//            OMElement result = sendReceive(element, serverURL);
//            result = buildElement(result);
//
//            RecoverResult recoverResult = getRecoverResult(result);
//
//            ResultMinor resultMinor = recoverResult.getResultMinor();
//            if (resultMinor != null && ResultMinor.NO_MATCH.equals(resultMinor)) {
//                return null;
//            }
//
//            org.wso2.xkms2.PrivateKey xkmsPrivateKey = recoverResult
//                    .getPrivateKey();
//            xkmsPrivateKey.setKey(XKMSKeyUtil.getPrivateKey(passPhrase,
//                                                            "DESede"));
//            KeyPair keyPair = xkmsPrivateKey.getRSAKeyPair();
//            return keyPair.getPrivate();
//
//        } catch (Exception ex) {
//            if (LOG.isDebugEnabled()) {
//                LOG.debug("Exception is thrown when invoking XKMS Service", ex);
//            }
//            return null;
//        }
    	return null;
    }

    public static X509Certificate[] getCertificates(String alias,
                                                    String serviceURL) {
//        try {
//            LocateRequest request = createLocateRequest();
//            request.setServiceURI(serviceURL);
//
//            QueryKeyBinding queryKeybinding = createQueryKeyBinding();
//            queryKeybinding.setKeyName(alias);
//            request.setQueryKeyBinding(queryKeybinding);
//
//            request.addRespondWith(RespondWith.X_509_CERT);
//
//            OMElement element = getAsOMElement(request);
//            OMElement result = sendReceive(element, serviceURL);
//            result = buildElement(result);
//
//            LocateResult locateResult = getLocateResult(result);
//
//            if (ResultMinor.NO_MATCH.equals(locateResult.getResultMinor())) {
//                return null;
//
//            } else {
//
//                List keybindings = locateResult.getUnverifiedKeyBindingList();
//                X509Certificate[] certs = new X509Certificate[keybindings
//                        .size()];
//
//                for (int i = 0; i < keybindings.size(); i++) {
//                    UnverifiedKeyBinding unverifiedKeybinding = (UnverifiedKeyBinding) keybindings
//                            .get(i);
//                    KeyInfo keyInfo = unverifiedKeybinding.getKeyInfo();
//                    certs[i] = keyInfo.getX509Certificate();
//                }
//                return certs;
//            }
//
//        } catch (Exception ex) {
//            if (LOG.isDebugEnabled()) {
//                LOG.debug("", ex);
//            }
//            return null;
//        }
    	return null;
    }

    public static String getAliasForX509Certificate(X509Certificate cert,
                                                    String serviceURL) {
//        try {
//
//            LocateRequest request = createLocateRequest();
//            request.setServiceURI(serviceURL);
//
//            QueryKeyBinding queryKeybinding = createQueryKeyBinding();
//
//            queryKeybinding.setCertValue(cert);
//            queryKeybinding.addUseKeyWith(UseKeyWith.PKIX, cert.getSubjectDN()
//                    .getName());
//
//            request.setQueryKeyBinding(queryKeybinding);
//
//            request.addRespondWith(RespondWith.KEY_NAME);
//
//            OMElement element = getAsOMElement(request);
//            OMElement result = sendReceive(element, serviceURL);
//            result = buildElement(result);
//
//            LocateResult locateResult = getLocateResult(result);
//
//            if (ResultMinor.NO_MATCH.equals(locateResult.getResultMinor())) {
//                return null;
//
//            } else {
//
//                List keybindings = locateResult.getUnverifiedKeyBindingList();
//                UnverifiedKeyBinding keybinding = (UnverifiedKeyBinding) keybindings
//                        .get(0);
//                KeyInfo keyInfo = keybinding.getKeyInfo();
//                KeyName keyName = keyInfo.itemKeyName(0);
//                return keyName.getKeyName();
//            }
//
//        } catch (Exception ex) {
//            if (LOG.isDebugEnabled()) {
//                LOG.debug("", ex);
//            }
//            return null;
//        }
    	
    	return null;
    }

    public static String getAliasForX509Certificate(byte[] skiValue,
                                                    String serviceURL) {
//        try {
//
//            LocateRequest request = createLocateRequest();
//            request.setServiceURI(serviceURL);
//
//            QueryKeyBinding queryKeybinding = createQueryKeyBinding();
//            queryKeybinding.addUseKeyWith(UseKeyWith.SKI, Base64
//                    .encode(skiValue));
//            request.setQueryKeyBinding(queryKeybinding);
//
//            request.addRespondWith(RespondWith.KEY_NAME);
//
//            OMElement element = getAsOMElement(request);
//            OMElement result = sendReceive(element, serviceURL);
//            result = buildElement(result);
//
//            LocateResult locateResult = getLocateResult(result);
//
//            if (ResultMinor.NO_MATCH.equals(locateResult.getResultMinor())) {
//                return null;
//
//            } else {
//
//                List keybindings = locateResult.getUnverifiedKeyBindingList();
//                UnverifiedKeyBinding keybinding = (UnverifiedKeyBinding) keybindings
//                        .get(0);
//                KeyInfo keyInfo = keybinding.getKeyInfo();
//                KeyName keyName = keyInfo.itemKeyName(0);
//                return keyName.getKeyName();
//            }
//
//        } catch (Exception ex) {
//            if (LOG.isDebugEnabled()) {
//                LOG.debug("", ex);
//            }
//            return null;
//        }
    	return null;
    }

    public static String[] getAliasesForDN(String subjectDN, String serviceURL) {
//        try {
//            LocateRequest request = createLocateRequest();
//            request.setServiceURI(serviceURL);
//
//            QueryKeyBinding queryKeybinding = createQueryKeyBinding();
//            queryKeybinding.addUseKeyWith(UseKeyWith.PKIX, subjectDN);
//            request.setQueryKeyBinding(queryKeybinding);
//
//            request.addRespondWith(RespondWith.KEY_NAME);
//
//            OMElement element = getAsOMElement(request);
//            OMElement result = sendReceive(element, serviceURL);
//            result = buildElement(result);
//
//            LocateResult locateResult = getLocateResult(result);
//
//            if (ResultMinor.NO_MATCH.equals(locateResult.getResultMinor())) {
//                return null;
//
//            } else {
//
//                List keybindings = locateResult.getUnverifiedKeyBindingList();
//                String[] aliases = new String[keybindings.size()];
//
//                for (int i = 0; i < keybindings.size(); i++) {
//                    UnverifiedKeyBinding unverifiedKeybinding = (UnverifiedKeyBinding) keybindings
//                            .get(i);
//                    KeyInfo keyInfo = unverifiedKeybinding.getKeyInfo();
//                    aliases[i] = keyInfo.itemKeyName(0).getKeyName();
//                }
//                return aliases;
//            }
//
//        } catch (Exception ex) {
//            if (LOG.isDebugEnabled()) {
//                LOG.debug("", ex);
//            }
//            return null;
//        }
    	return null;

    }

    public static boolean validateCertPath(Certificate[] certs,
                                           String serviceURL) {
        return validateCertPath((X509Certificate) certs[0], serviceURL);
    }

    public static boolean validateCertPath(X509Certificate cert,
                                           String serviceURL) {
//        try {
//
//            ValidateRequest request = createValidateRequest();
//            request.setServiceURI(serviceURL);
//
//            QueryKeyBinding keyBinding = createQueryKeyBinding();
//            keyBinding.setCertValue(cert);
//
//            String name = cert.getSubjectDN().getName();
//            keyBinding.addUseKeyWith(UseKeyWith.PKIX, name);
//
//            request.setQueryKeyBinding(keyBinding);
//            request.addRespondWith(RespondWith.X_509_CERT);
//
//            OMElement element = getElement(request);
//            OMElement result = sendReceive(element, serviceURL);
//            result = buildElement(result);
//
//            ValidateResult validateResult = getValidateResult(result);
//            List keybinds = validateResult.getKeyBindingList();
//            KeyBinding keybinding = (KeyBinding) keybinds.get(0);
//
//            Status status = keybinding.getStatus();
//
//            return StatusValue.VALID.equals(status.getStatusValue());
//
//        } catch (Exception ex) {
//            if (LOG.isDebugEnabled()) {
//                LOG.debug("", ex);
//            }
//
//            return false;
//        }
    	return false;
    }

//    private static OMElement getAsOMElement(RecoverRequest request)
//            throws XKMSException {
//        OMFactory factory = DOOMAbstractFactory.getOMFactory();
//        return request.serialize(factory);
//    }
//
//    private static RecoverResult getRecoverResult(OMElement recoverResultElem)
//            throws Exception {
//        return (RecoverResult) RecoverResultBuilder.INSTANCE
//                .buildElement(recoverResultElem);
//    }

//    public static RegisterRequest createRegisterRequest() {
//        RegisterRequest request = new RegisterRequest();
//        request.setId(XKMSUtil.getRamdomId());
//        return request;
//    }
//
//    public static Authentication createAuthenticate() {
//        Authentication authentication = new Authentication();
//        return authentication;
//    }
//
//    public static PrototypeKeyBinding createPrototypeKeyBinding() {
//        PrototypeKeyBinding keyBinding = new PrototypeKeyBinding();
//        keyBinding.setId(XKMSUtil.getRamdomId());
//        return keyBinding;
//    }
//
//    public static QueryKeyBinding createQueryKeyBinding() {
//        QueryKeyBinding binding = new QueryKeyBinding();
//        binding.setId(XKMSUtil.getRamdomId());
//        return binding;
//    }
//
//    public static ReissueRequest createReissueRequest() {
//        ReissueRequest reissueRequest = new ReissueRequest();
//        reissueRequest.setId(XKMSUtil.getRamdomId());
//        return reissueRequest;
//    }
//
//    public static ReissueKeyBinding createReissueKeyBinding() {
//        ReissueKeyBinding reissueKeyBinding = new ReissueKeyBinding();
//        reissueKeyBinding.setId(XKMSUtil.getRamdomId());
//        return reissueKeyBinding;
//    }
//
//    public static RecoverKeyBinding createRecoverKeyBinding() {
//        RecoverKeyBinding recoverKeyBinding = new RecoverKeyBinding();
//        recoverKeyBinding.setId(XKMSUtil.getRamdomId());
//        return recoverKeyBinding;
//
//    }
//
//    public static RecoverRequest createRecoverRequest() {
//        RecoverRequest recoverRequest = new RecoverRequest();
//        recoverRequest.setId(XKMSUtil.getRamdomId());
//        return recoverRequest;
//    }
//
//    public static ValidateRequest createValidateRequest() {
//        ValidateRequest validate = new ValidateRequest();
//        validate.setId(XKMSUtil.getRamdomId());
//        return validate;
//    }
//
//    public static LocateRequest createLocateRequest() {
//        LocateRequest locate = new LocateRequest();
//        locate.setId(XKMSUtil.getRamdomId());
//        return locate;
//    }

    public static OMElement sendReceive(OMElement element, String serviceURL)
            throws AxisFault {

        try {
            ConfigurationContext configCtx = ConfigurationContextFactory
                    .createDefaultConfigurationContext();
            ServiceClient client = new ServiceClient(configCtx, null);

            Options options = client.getOptions();
            EndpointReference epr = new EndpointReference(serviceURL);
            options.setTo(epr);

            options.setProperty(HTTPConstants.CHUNKED, Boolean.FALSE);
            options.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);

            OMElement result = client.sendReceive(element);
            return result;
        } catch (Exception ex) {
            throw AxisFault.makeFault(ex);
        }
    }

    public static OMElement buildElement(OMElement element) throws Exception {
        String str = element.toString();
        ByteArrayInputStream bais = new ByteArrayInputStream(str.getBytes());

        StAXOMBuilder builder = new StAXOMBuilder(DOOMAbstractFactory
                .getOMFactory(), XMLInputFactory.newInstance()
                .createXMLStreamReader(bais));

        return builder.getDocumentElement();
    }

//    private static OMElement getAsOMElement(LocateRequest request)
//            throws XKMSException {
//        OMFactory factory = DOOMAbstractFactory.getOMFactory();
//        return request.serialize(factory);
//    }
//
//    private static LocateResult getLocateResult(OMElement result)
//            throws Exception {
//        LocateResult locateResult = (LocateResult) LocateResultBuilder.INSTANCE
//                .buildElement(result);
//        return locateResult;
//    }
//
//    private static OMElement getElement(ValidateRequest request)
//            throws XKMSException {
//        OMFactory factory = DOOMAbstractFactory.getOMFactory();
//        return request.serialize(factory);
//    }
//
//    private static ValidateResult getValidateResult(OMElement element)
//            throws XKMSException {
//        return (ValidateResult) ValidateResultBuilder.INSTANCE
//                .buildElement(element);
//
//    }
}

