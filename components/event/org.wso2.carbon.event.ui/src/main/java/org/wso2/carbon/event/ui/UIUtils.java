/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.event.ui;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.event.client.broker.BrokerClient;
import org.wso2.carbon.event.stub.internal.xsd.TopicRolePermission;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;

/**
 * This class is used by the UI to connect to services and provides utilities. Used by JSP pages.
 */
@Deprecated
public class UIUtils {

    /**
     * Gets the broker client for EventBrokerService
     * Suppressing warning of unused declaration as it used by the UI (JSP pages)
     *
     * @param config the servlet configuration
     * @param session the http session
     * @param request the http servlet request
     * @return the broker client
     */
    @SuppressWarnings("UnusedDeclaration")
    public static BrokerClient getBrokerClient(ServletConfig config, HttpSession session,
                                               HttpServletRequest request) {
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

        backendServerURL = backendServerURL + "EventBrokerService";

        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        return new BrokerClient(configContext, backendServerURL, cookie);
    }

    /**
     * Gets subscription mode description.
     * Suppressing warning of unused declaration as it used by the UI (JSP pages)
     *
     * @param serverMode subscription mode
     * @return subscription mode description
     */
    @SuppressWarnings("UnusedDeclaration")
    public static String getSubscriptionMode(String serverMode) {
        if (serverMode.equals(UIConstants.SUBSCRIPTION_MODE_1)) {
            return UIConstants.SUBSCRIPTION_MODE_1_DESCRIPTION;
        } else if (serverMode.equals(UIConstants.SUBSCRIPTION_MODE_2)) {
            return UIConstants.SUBSCRIPTION_MODE_2_DESCRIPTION;
        } else {
            return UIConstants.SUBSCRIPTION_MODE_0_DESCRIPTION;
        }
    }

    /**
     * Filter the full user-roles list to suit the range.
     * Suppressing warning of unused declaration as it used by the UI (JSP pages)
     *
     * @param fullList      full list of roles
     * @param startingIndex starting index to filter
     * @param maxRolesCount maximum number of roles that the filtered list can contain
     * @return ArrayList<TopicRolePermission>
     */
    @SuppressWarnings("UnusedDeclaration")
    public static ArrayList<TopicRolePermission> getFilteredRoleList
    (ArrayList<TopicRolePermission> fullList, int startingIndex, int maxRolesCount) {
        int resultSetSize = maxRolesCount;

        if ((fullList.size() - startingIndex) < maxRolesCount) {
            resultSetSize = (fullList.size() - startingIndex);
        }

        ArrayList<TopicRolePermission> resultList = new ArrayList<TopicRolePermission>();
        for (int i = startingIndex; i < startingIndex + resultSetSize; i++) {
            resultList.add(fullList.get(i));
        }

        return resultList;
    }
}
