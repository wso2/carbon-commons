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
package org.wso2.carbon.um.ws.service;

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.context.MessageContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.um.ws.service.internal.UMRemoteServicesDSComponent;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.profile.ProfileConfiguration;
import org.wso2.carbon.user.core.profile.ProfileConfigurationManager;
import org.wso2.carbon.user.core.service.RealmService;

public class ProfileConfigurationManagerService extends AbstractAdmin {

    public void addProfileConfig(ProfileConfiguration profileConfig) throws UserStoreException {
        try {
            getProfileConfigurationManager().addProfileConfig(profileConfig);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException(e);
        }
    }

    public void deleteProfileConfig(ProfileConfiguration profileConfig) throws UserStoreException {
        try {
            getProfileConfigurationManager().deleteProfileConfig(profileConfig);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException(e);
        }

    }

    public ProfileConfiguration[] getAllProfiles() throws UserStoreException {
        try {
            return (ProfileConfiguration[]) getProfileConfigurationManager().getAllProfiles();
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException(e);
        }
    }

    public ProfileConfiguration getProfileConfig(String profileName) throws UserStoreException {
        try {
            return (ProfileConfiguration) getProfileConfigurationManager().getProfileConfig(
                    profileName);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException(e);
        }
    }

    public void updateProfileConfig(ProfileConfiguration profileConfig) throws UserStoreException {
        try {
            getProfileConfigurationManager().updateProfileConfig(profileConfig);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException(e);
        }
    }

    private ProfileConfigurationManager getProfileConfigurationManager() throws UserStoreException {
        try {
            UserRealm realm = super.getUserRealm();
            if (realm == null) {
                throw new UserStoreException("UserRealm is null");
            }
            return realm.getProfileConfigurationManager();
        } catch (Exception e) {
            throw new UserStoreException(e);
        }
    }

}
