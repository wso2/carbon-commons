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
package org.wso2.carbon.um.ws.api;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.um.ws.api.stub.RemoteProfileConfigurationManagerServiceStub;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.profile.ProfileConfiguration;
import org.wso2.carbon.user.core.profile.ProfileConfigurationManager;

public class WSProfileConfigurationManager implements ProfileConfigurationManager {

    private RemoteProfileConfigurationManagerServiceStub stub = null;

    private static Log log = LogFactory.getLog(WSProfileConfigurationManager.class);

    public WSProfileConfigurationManager(String serverUrl, String cookie,
                                         ConfigurationContext configCtxt) throws UserStoreException {
        try {
            stub =
                   new RemoteProfileConfigurationManagerServiceStub(configCtxt, serverUrl +
                                                                                "RemoteProfileConfigurationManagerService");
            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        } catch (AxisFault e) {
            throw new UserStoreException();
        }
    }

    public void addProfileConfig(ProfileConfiguration profileConfig) throws UserStoreException {
        try {
            stub.addProfileConfig(WSRealmUtil.convertToADBProfileConfiguration(profileConfig));
        } catch (Exception e) {
            this.handleException(e.getMessage(), e);
        }
    }

    public void deleteProfileConfig(ProfileConfiguration profileConfig) throws UserStoreException {
        try {
            stub.deleteProfileConfig(WSRealmUtil.convertToADBProfileConfiguration(profileConfig));
        } catch (Exception e) {
            this.handleException(e.getMessage(), e);
        }
    }

    public void updateProfileConfig(ProfileConfiguration profileConfig) throws UserStoreException {
        try {
            stub.updateProfileConfig(WSRealmUtil.convertToADBProfileConfiguration(profileConfig));
        } catch (Exception e) {
            this.handleException(e.getMessage(), e);
        }
    }

    public ProfileConfiguration[] getAllProfiles() throws UserStoreException {
        try {
            return WSRealmUtil.convertToProfileConfigurations(stub.getAllProfiles());
        } catch (Exception e) {
            this.handleException(e.getMessage(), e);
        }
        return null;
    }

    public ProfileConfiguration getProfileConfig(String profileName) throws UserStoreException {
        try {
            return WSRealmUtil.convertToProfileConfiguration(stub.getProfileConfig(profileName));
        } catch (Exception e) {
            this.handleException(e.getMessage(), e);
        }
        return null;
    }

    public void addProfileConfig(org.wso2.carbon.user.api.ProfileConfiguration profileConfiguration)
            throws org.wso2.carbon.user.api.UserStoreException {
        addProfileConfig((ProfileConfiguration) profileConfiguration);
    }

    public void updateProfileConfig(
            org.wso2.carbon.user.api.ProfileConfiguration profileConfiguration)
            throws org.wso2.carbon.user.api.UserStoreException {
        updateProfileConfig((ProfileConfiguration) profileConfiguration);
    }

    public void deleteProfileConfig(
            org.wso2.carbon.user.api.ProfileConfiguration profileConfiguration)
            throws org.wso2.carbon.user.api.UserStoreException {
        deleteProfileConfig((ProfileConfiguration) profileConfiguration);
    }

    private String[] handleException(String msg, Exception e) throws UserStoreException {
        log.error(e.getMessage(), e);
        throw new UserStoreException(msg, e);
    }

}
