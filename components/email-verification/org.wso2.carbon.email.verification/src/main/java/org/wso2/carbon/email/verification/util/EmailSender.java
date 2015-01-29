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
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.axis2.transport.mail.MailConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.email.verification.internal.EmailVerificationServiceComponent;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * The thread responsible for sending emails.
 */
public class EmailSender extends Thread {

    public static final String CONF_STRING = "confirmation";
    private static Log log = LogFactory.getLog(EmailSender.class);

    private EmailVerifierConfig config = null;
    private String emailAddr = null;
    private String secretKey = null;
    private String tenantDomain = null;
    private Map<String, String> userParameters = null;

    public EmailSender(EmailVerifierConfig config, String emailAddr, String secretKey) {
        this(config, emailAddr, secretKey, null);
    }

    public EmailSender(EmailVerifierConfig config, String emailAddr, String secretKey,
                       String tenantDomain) {
        this(config, emailAddr, secretKey, tenantDomain, null);
    }

    public EmailSender(EmailVerifierConfig config, String emailAddr, String secretKey,
                       String tenantDomain, Map<String, String> userParameters) {
        this.config = config;
        this.emailAddr = emailAddr;
        this.secretKey = secretKey;
        this.tenantDomain = tenantDomain;
        this.userParameters = userParameters;
    }

    public void sendEmail() {
        start();
    }

    public void run() {

        Map<String, String> headerMap = new HashMap<String, String>();
        Map<String, String> userParams = new HashMap<String, String>();
        userParams.put("admin-name", userParameters.get("admin"));
        userParams.put("user-name", userParameters.get("userName"));
        userParams.put("domain-name", userParameters.get("tenantDomain"));
        userParams.put("first-name", userParameters.get("first-name"));
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);

            if (config.getSubject().length() == 0) {
                headerMap.put(MailConstants.MAIL_HEADER_SUBJECT,
                              EmailVerifierConfig.DEFAULT_VALUE_SUBJECT);

            } else {
                headerMap.put(MailConstants.MAIL_HEADER_SUBJECT, replacePlaceHolders(
                        config.getSubject(), userParams));
            }
            String requestMessage = replacePlaceHolders(getRequestMessage(), userParams);

            OMElement payload = OMAbstractFactory.getOMFactory().createOMElement(
                    BaseConstants.DEFAULT_TEXT_WRAPPER, null);
            payload.setText(requestMessage);
            ServiceClient serviceClient;
            ConfigurationContext configContext =
                    EmailVerificationServiceComponent.getConfigurationContext();
            if (configContext != null) {
                serviceClient = new ServiceClient(configContext, null);
            } else {
                serviceClient = new ServiceClient();
            }
            Options options = new Options();
            options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
            options.setProperty(MessageContext.TRANSPORT_HEADERS, headerMap);
            options.setProperty(
                    MailConstants.TRANSPORT_MAIL_FORMAT, MailConstants.TRANSPORT_FORMAT_TEXT);
            options.setTo(new EndpointReference("mailto:" + emailAddr));
            serviceClient.setOptions(options);
            serviceClient.fireAndForget(payload);
            log.debug("Sending confirmation mail to " + emailAddr);
            log.debug("Verification url : " + requestMessage);
            // Send the message

            log.debug("Sending confirmation mail to " + emailAddr + "DONE");
        } catch (AxisFault e) {
            log.error("Failed Sending Email", e);
        }  finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private String getRequestMessage() {
        String msg;
        String targetEpr = config.getTargetEpr();
        String tenantDomain = this.tenantDomain;
        if (tenantDomain == null) {
            PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
        }
        if (tenantDomain != null && targetEpr.indexOf("/carbon") > 0 &&
            MultitenantUtils.getTenantDomainFromRequestURL(targetEpr) == null &&
            PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true)!= MultitenantConstants.SUPER_TENANT_ID) {
            targetEpr = targetEpr.replace("/carbon", "/" +
                                                     MultitenantConstants.TENANT_AWARE_URL_PREFIX + "/" + tenantDomain + "/carbon");
        }
        if (config.getEmailBody().length() == 0) {
            msg = EmailVerifierConfig.DEFAULT_VALUE_MESSAGE + "\n" + targetEpr + "?"
                  + CONF_STRING + "=" + secretKey + "\n";
        } else {
            msg = config.getEmailBody() + "\n" + targetEpr + "?" + CONF_STRING + "="
                  + secretKey + "\n";
        }
        if (config.getEmailFooter() != null) {
            msg = msg + "\n" + config.getEmailFooter();
        }
        return msg;
    }

    /**
     * Replace the {user-parameters} in the config file with the respective values
     *
     * @param text           the initial text
     * @param userParameters mapping of the key and its value
     * @return the final text to be sent in the email
     */
    public static String replacePlaceHolders(String text, Map<String, String> userParameters) {
        if (userParameters != null) {
            for (Map.Entry<String, String> entry : userParameters.entrySet()) {
                String key = entry.getKey();
                text = text.replaceAll("\\{" + key + "\\}", entry.getValue());
            }
        }
        return text;
    }
}
