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

package org.wso2.carbon.um.ws.api;


import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.core.config.RealmConfiguration;

public class WSRealmBuilder {

    private static Log logger = LogFactory.getLog(WSRealmBuilder.class);

    /**
     * 
     *
     * @param realmProperties
     * @return
     */
    public static UserRealm createWSRealm(RealmConfiguration realmConfig)
            throws UserStoreException {
        WSRealm realm = new WSRealm();
        try {
            realm.init(realmConfig, null, null, -1);
        } catch (UserStoreException e) {
            String errorMessage = "Cannot initialize the realm.";
            logger.error(errorMessage, e);
            throw new UserStoreException(errorMessage, e);

        }
        return realm;
    }

   
    /**
     * Method to create WSRealm for non-Carbon environment
     * Recommended method
     */
    public static UserRealm createWSRealm(RealmConfiguration realmConfig,
                                          ConfigurationContext configContext)
                                                                             throws UserStoreException {
        WSRealm realm = new WSRealm();
        realm.init(realmConfig, configContext);
        return realm;
    }
    
    /**
     * Method to create WSRealm for non-Carbon environment
     * Recommended method
     * 
     */
    public static UserRealm createWSRealm(String serverUrl, String cookie, ConfigurationContext configContext)
                                                                                               throws UserStoreException {
        
        WSRealm realm = new WSRealm();
        realm.init(serverUrl, cookie, configContext);
        return realm;
    }
    
}
