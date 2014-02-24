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
package org.wso2.carbon.identity.authenticator.saml2.sso.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to hold the list of authentication requests sent for authentication.
 */
public class SSOSessionManager {
    private static List<String> relayStateList = new ArrayList<String>();
    private static Map<String, FederatedSSOToken> federatedTokenHolder = new HashMap<String, FederatedSSOToken>();


    /**
     * Verify whether the response is valid with the given RELAY - STATE Id
     * @param relayStateId RELAY STATE sent with the request
     * @return true, if it is a valid response
     */
    public static boolean isValidResponse(String relayStateId){
        if(relayStateId == null){
            return false;
        }
        else if(relayStateList.contains((String)relayStateId)){
            // remove the relay state id from the list, and return TRUE
            relayStateList.remove(relayStateId) ;
            return true;
        }
        return false;
    }

    /**
     * Add a new authentication request which is sent for authentication.
     * @param relayStateId RELAY STATE sent with the request                  
     */
    public static void addAuthnRequest(String relayStateId){
        relayStateList.add(relayStateId);
    }
    
    public static void addFederatedToken(String tokenId, FederatedSSOToken token){
    	federatedTokenHolder.put(tokenId, token);
    }
    
    public static FederatedSSOToken getFederatedToken(String tokenId){
    	return federatedTokenHolder.remove(tokenId);
    }
}
