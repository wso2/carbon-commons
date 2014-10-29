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
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.context.MessageContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.um.ws.service.dao.RealmConfigurationDTO;
import org.wso2.carbon.um.ws.service.dao.RealmPropertyDTO;
import org.wso2.carbon.um.ws.service.internal.UMRemoteServicesDSComponent;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.service.RealmService;

//TODO super tenant service
public class UserRealmService extends AbstractAdmin {

    public RealmConfigurationDTO getRealmConfiguration() throws UserStoreException {
        UserRealm userRealm = getApplicableUserRealm();
        RealmConfiguration realmConfig = userRealm.getRealmConfiguration();
        RealmConfigurationDTO realmConfigDTO = new RealmConfigurationDTO();
        realmConfigDTO.setRealmClassName(realmConfig.getRealmClassName());
        realmConfigDTO.setUserStoreClass(realmConfig.getUserStoreClass());
        realmConfigDTO.setAuthorizationManagerClass(realmConfig.getAuthorizationManagerClass());
        realmConfigDTO.setAdminRoleName(realmConfig.getAdminRoleName());
        realmConfigDTO.setAdminUserName(realmConfig.getAdminUserName());
        realmConfigDTO.setAdminPassword(realmConfig.getAdminPassword());
        realmConfigDTO.setEveryOneRoleName(realmConfig.getEveryOneRoleName());
        realmConfigDTO.setUserStoreProperties(getPropertyValueArray(realmConfig
                .getUserStoreProperties()));
        realmConfigDTO.setAuthzProperties(getPropertyValueArray(realmConfig.getAuthzProperties()));
        realmConfigDTO.setRealmProperties(getPropertyValueArray(realmConfig.getRealmProperties()));
        return realmConfigDTO;
    }

    private RealmPropertyDTO[] getPropertyValueArray(Map<String, String> map) {
        RealmPropertyDTO[] realmProps = new RealmPropertyDTO[map.size()];
        int i = 0;
        for (Iterator<Map.Entry<String, String>> ite = map.entrySet().iterator(); ite.hasNext();) {
            Map.Entry<String, String> entry = ite.next();
            realmProps[i] = new RealmPropertyDTO(entry.getKey(), entry.getValue());
            i++;
        }
        return realmProps;
    }

    private UserRealm getApplicableUserRealm() throws UserStoreException {
        try {
            UserRealm realm = super.getUserRealm();
            if (realm == null) {
                throw new UserStoreException("UserRealm is null");
            }
            return realm;
        } catch (Exception e) {
            throw new UserStoreException(e);
        }
    }
}
