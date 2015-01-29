/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.email.verification.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * The utility methods of the Email Verifier component
 */
public class Util {
    private static final Log log = LogFactory.getLog(Util.class);

    private static RegistryService registryService;
    private static final String EMAIL_VERIFICATION_COLLECTION =
            "/repository/components/org.wso2.carbon.email-verification/email-verifications-map";
    private static final String VERIFIED_EMAIL_RESOURCE_PATH =
            "/repository/components/org.wso2.carbon.email-verification/emailIndex";
    private static final String PASSWORD_RESET = "passwordReset";

    public static synchronized void setRegistryService(RegistryService service) {
        if (registryService == null) {
            registryService = service;
        }
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static UserRegistry getConfigSystemRegistry(int tenantId) throws RegistryException {
        return registryService.getConfigSystemRegistry(tenantId);
    }


    /**
     * Loads the email configuration from the respective components.
     * @param configFilename, the file that contains the email configurations
     * @return EmailVerfierConfig
     */
    public static EmailVerifierConfig loadeMailVerificationConfig(String configFilename) {
        EmailVerifierConfig config = new EmailVerifierConfig();
        File configfile = new File(configFilename);
        if (!configfile.exists()) {
            log.error("Email Configuration File is not present at: " + configFilename);
            return null;
        }
        try {
            XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(
                    new FileInputStream(configfile));
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            OMElement documentElement = builder.getDocumentElement();
            Iterator it = documentElement.getChildrenWithLocalName("configuration");

            //Assume the configuration file is in older format with just one email template
            if (it.hasNext() == false) {
                config =  fillConfig(documentElement.getChildElements());

            //If the configuration file is in new format
            } else {
                while (it.hasNext()) {
                    OMElement element = (OMElement) it.next();
                    String configType = element.getAttributeValue(new QName("type"));
                    if (configType.trim().equalsIgnoreCase(PASSWORD_RESET)) {
                        Iterator configValues = element.getChildElements();
                        config =  fillConfig(configValues);
                    }
                }
            }
            return config;
        } catch (Exception e) {
            String msg = "Error in loading configuration for email verification: " +
                    configFilename + ".";
            log.error(msg, e);
            return null;
        }
    }

    /**
     * Create the EmailVerifierConfig object from the read in values from the file
     * @param configValues
     * @return
     */
    private static EmailVerifierConfig fillConfig(Iterator configValues) {
        EmailVerifierConfig config = new EmailVerifierConfig();
        while (configValues.hasNext()) {
            OMElement configValue = (OMElement) configValues.next();
            if ("subject".equals(configValue.getLocalName())) {
                config.setSubject(configValue.getText());
            } else if ("body".equals(configValue.getLocalName())) {
                config.setEmailBody(configValue.getText());
            } else if ("footer".equals(configValue.getLocalName())) {
                config.setEmailFooter(configValue.getText());
            } else if ("targetEpr".equals(configValue.getLocalName())) {
                config.setTargetEpr(configValue.getText());
            } else if ("redirectPath".equals(configValue.getLocalName())) {
                config.setRedirectPath(configValue.getText());
            }
        }

        return config;
    }

    /**
     * Confirms that the link the user clicked is valid.
     * @param secretKey, the key that is sent to the user with the email.
     * @return the ConfirmationBean with the redirect path and data.
     * @throws Exception, if confirming the user failed.
     */
    public static ConfirmationBean confirmUser(String secretKey) throws Exception {
        ConfirmationBean confirmationBean = new ConfirmationBean();
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMElement data = fac.createOMElement("configuration", null);

        Registry registry = Util.getConfigSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);
        boolean success = false;
        try {
            registry.beginTransaction();

            String secretKeyPath = EMAIL_VERIFICATION_COLLECTION +
                    RegistryConstants.PATH_SEPARATOR + secretKey;
            if (!registry.resourceExists(secretKeyPath)) {
                String msg = "Email verification failed.";
                log.error(msg);
                throw new Exception(msg);
            }
            Resource resource = registry.get(secretKeyPath);

            // just get the properties of that
            Properties props = resource.getProperties();
            for (Object o : props.keySet()) {
                String key = (String) o;
                OMElement internal = fac.createOMElement(key, null);
                internal.setText(resource.getProperty(key));
                data.addChild(internal);
                if (key.equals("redirectPath")) {
                    confirmationBean.setRedirectPath(resource.getProperty(key));
                }
            }
            // remove the temporarily stored resource from the registry
            registry.delete(resource.getPath());
            confirmationBean.setData(data.toString());
            success = true;

            // when verifying the user,email address is being persisted in order to be recognized
            // for one time verification
            if (Boolean.parseBoolean(System.getProperty("onetime.email.verification", Boolean.toString(false)))) {
                Resource tempResource;
                if (registry.resourceExists(VERIFIED_EMAIL_RESOURCE_PATH)) {
                    String verifyingEmail = data.getFirstChildWithName(new QName("email")).getText();
                    String key = UUIDGenerator.generateUUID();
                    tempResource = registry.get(VERIFIED_EMAIL_RESOURCE_PATH);
                    if (tempResource != null) {
                        tempResource.setProperty(key, verifyingEmail);
                    }
                    registry.put(VERIFIED_EMAIL_RESOURCE_PATH, tempResource);
                }
            }

        } finally {
            if (success) {
                registry.commitTransaction();
            } else {
                registry.rollbackTransaction();
            }
        }
        return confirmationBean;
    }

    /**
     * Sends an email to the user with the link to verify.
     * @param data of the user
     * @param serviceConfig, EmailVerifier Configuration.
     * @throws Exception, if sending the user verification mail failed.
     */
    public static void requestUserVerification(Map<String, String> data,
                                               EmailVerifierConfig serviceConfig) throws Exception {
        String emailAddress = data.get("email");

        emailAddress = emailAddress.trim();
        try {
            String secretKey = UUID.randomUUID().toString();

            // User is supposed to give where he wants to store the intermediate data.
            // But, here there is no tenant signing in happened yet.
            // So get the super tenant registry instance.
            Registry registry = Util.getConfigSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);
            Resource resource = registry.newResource();
            // store the redirector url
            resource.setProperty("redirectPath", serviceConfig.getRedirectPath());
            // store the user data, redirectPath can be overwritten here.
            for (String s : data.keySet()) {
                resource.setProperty(s, data.get(s));
            }

            resource.setVersionableChange(false);
            String secretKeyPath = EMAIL_VERIFICATION_COLLECTION +
                    RegistryConstants.PATH_SEPARATOR + secretKey;
            registry.put(secretKeyPath, resource);
            // sending the mail
            EmailSender sender = new EmailSender(serviceConfig, emailAddress, secretKey,
                 PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true), data);
            sender.sendEmail();
        } catch (Exception e) {
            String msg = "Error in sending the email to validation.";
            log.error(msg, e);
            throw new Exception(msg, e);
        }
    }
}
