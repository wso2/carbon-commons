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

package org.wso2.carbon.deployment.synchronizer.services;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.deployment.synchronizer.internal.DeploymentSynchronizationManager;
import org.wso2.carbon.deployment.synchronizer.internal.DeploymentSynchronizer;
import org.wso2.carbon.registry.common.eventing.RegistryEvent;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.xml.namespace.QName;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This service is used to receive repository update event notifications for the auto checkout
 * activities. The message format of the events is similar to the update notifications generated
 * by the governance registry. Therefore it integrates out of the box with the registry based
 * repository.
 */
public class AutoCheckoutService {

    private static final Log log = LogFactory.getLog(AutoCheckoutService.class);

    private static final QName TIMESTAMP = new QName(RegistryEvent.REGISTRY_EVENT_NS, "Timestamp");
    private static final QName DETAILS = new QName(RegistryEvent.REGISTRY_EVENT_NS, "Details");
    private static final QName SESSION = new QName(RegistryEvent.REGISTRY_EVENT_NS, "Session");
    private static final QName TENANT = new QName(RegistryEvent.REGISTRY_EVENT_NS, "TenantId");

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public void notifyUpdate(OMElement element) {
        if (log.isDebugEnabled()) {
            log.debug("Received new event: " + element);
        }

        OMElement timestampElement = element.getFirstChildWithName(TIMESTAMP);
        if (timestampElement == null) {
            log.warn("Timestamp element not available in the event");
            return;
        }

        OMElement detailElement = element.getFirstChildWithName(DETAILS);
        OMElement sessionElement = detailElement.getFirstChildWithName(SESSION);
        OMElement tenantElement = sessionElement.getFirstChildWithName(TENANT);

        if (tenantElement == null) {
            log.warn("Tenant ID not available in the event");
            return;
        }

        String timestamp = timestampElement.getText();
        int tenantId = Integer.parseInt(tenantElement.getText());

        DeploymentSynchronizationManager syncManager = DeploymentSynchronizationManager.getInstance();
        // TODO: Implement an alternative way to get the file path
        String filePath = MultitenantUtils.getAxis2RepositoryPath(tenantId);
        DeploymentSynchronizer synchronizer = syncManager.getSynchronizer(filePath);
        if (synchronizer == null || !synchronizer.isAutoCheckout()) {
            log.warn("Unable to find the synchronizer for the file path: " + filePath);
            return;
        }

        try {
            Date date = DATE_FORMAT.parse(timestamp);
            synchronizer.requestCheckout(date.getTime());
        } catch (ParseException e) {
            log.error("Error while parsing the registry event time stamp: " + timestamp, e);
        }
    }
}
