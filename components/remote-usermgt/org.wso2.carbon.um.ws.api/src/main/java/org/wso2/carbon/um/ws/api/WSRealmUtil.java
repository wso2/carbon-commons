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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.wso2.carbon.um.ws.api.stub.*;
import org.wso2.carbon.user.api.Claim;
import org.wso2.carbon.user.api.ClaimMapping;
import org.wso2.carbon.user.api.RealmConfiguration;

/**
 * Utility class used for conversions
 */
public class WSRealmUtil {

    public static Map<String, String> convertClaimValueToMap(ClaimValue[] values) {
        Map<String, String> map = new HashMap<String, String>();
        if (values == null) {
            return map;
        }
        for (ClaimValue claimValue : values) {
            map.put(claimValue.getClaimURI(), claimValue.getValue());
        }
        return map;
    }

    public static ClaimValue[] convertMapToClaimValue(Map<String, String> map) {
        if (map == null) {
            return null;
        }
        ClaimValue[] claims = new ClaimValue[map.size()];
        Iterator<Map.Entry<String, String>> ite = map.entrySet().iterator();
        int i = 0;
        while (ite.hasNext()) {
            Map.Entry<String, String> entry = ite.next();
            claims[i] = new ClaimValue();
            claims[i].setClaimURI(entry.getKey());
            claims[i].setValue(entry.getValue());
            i++;
        }
        return claims;
    }

    public static Map<String, String> convertClaimValuesToMap(ClaimValue[] claims) {
        Map<String, String> map = new HashMap<String, String>();
        if (claims == null) {
            return map;
        }
        for (ClaimValue claim : claims) {
            map.put(claim.getClaimURI(), claim.getValue());
        }
        return map;
    }

    public static org.wso2.carbon.um.ws.api.stub.ClaimMapping[] convertToADBClaimMappings(
            org.wso2.carbon.user.core.claim.ClaimMapping[] claimMappings) {
        if (claimMappings == null) {
            return null;
        }
        org.wso2.carbon.um.ws.api.stub.ClaimMapping[] cms = new org.wso2.carbon.um.ws.api.stub.ClaimMapping[claimMappings.length];
        int i = 0;
        for (org.wso2.carbon.user.core.claim.ClaimMapping claimMapping : claimMappings) {
            org.wso2.carbon.um.ws.api.stub.ClaimMapping cm = new org.wso2.carbon.um.ws.api.stub.ClaimMapping();
            cm.setClaim(convertToADBClaim(claimMapping.getClaim()));
            cm.setMappedAttribute(claimMapping.getMappedAttribute());
            cms[i] = cm;
            i++;
        }
        return cms;
    }

    public static org.wso2.carbon.um.ws.api.stub.Claim convertToADBClaim(org.wso2.carbon.user.api.Claim claim) {
        if (claim == null) {
            return null;
        }
        org.wso2.carbon.um.ws.api.stub.Claim claimz = new org.wso2.carbon.um.ws.api.stub.Claim();
        claimz.setClaimUri(claim.getClaimUri());
        claimz.setDescription(claim.getDescription());
        claimz.setDialectURI(claim.getDialectURI());
        claimz.setDisplayOrder(claim.getDisplayOrder());
        claimz.setDisplayTag(claim.getDisplayTag());
        claimz.setRegEx(claim.getRegEx());
        claimz.setRequired(claim.isRequired());
        claimz.setSupportedByDefault(claim.isSupportedByDefault());
        claimz.setValue(claim.getValue());
        return claimz;
    }

    public static org.wso2.carbon.um.ws.api.stub.ClaimMapping convertToADBClaimMapping(
            org.wso2.carbon.user.api.ClaimMapping claimMapping) {
        if (claimMapping == null) {
            return null;
        }
        org.wso2.carbon.um.ws.api.stub.ClaimMapping cm = new org.wso2.carbon.um.ws.api.stub.ClaimMapping();
        cm.setClaim(convertToADBClaim(claimMapping.getClaim()));
        cm.setMappedAttribute(claimMapping.getMappedAttribute());
        return cm;
    }

    public static org.wso2.carbon.um.ws.api.stub.Claim[] convertToADBClaims(org.wso2.carbon.user.core.claim.Claim[] claims) {
        if (claims == null) {
            return null;
        }

        org.wso2.carbon.um.ws.api.stub.Claim[] claimz = new org.wso2.carbon.um.ws.api.stub.Claim[claims.length];
        for (int i = 0; i < claims.length; i++) {
            claimz[i] = convertToADBClaim(claims[i]);
        }
        return claimz;
    }

    public static ProfileConfiguration convertToADBProfileConfiguration(
            org.wso2.carbon.user.core.profile.ProfileConfiguration profileConfig) {
        if (profileConfig == null) {
            return null;
        }
        ProfileConfiguration profz = new ProfileConfiguration();
        profz.setDialectName(profileConfig.getDialectName());
        List<String> lst = profileConfig.getHiddenClaims();
        profz.setHiddenClaims(lst.toArray(new String[lst.size()]));
        lst = profileConfig.getInheritedClaims();
        profz.setInheritedClaims(lst.toArray(new String[lst.size()]));
        lst = profileConfig.getOverriddenClaims();
        profz.setOverriddenClaims(lst.toArray(new String[lst.size()]));
        profz.setProfileName(profileConfig.getProfileName());
        return profz;
    }
    
    public static RealmConfiguration convertToRealmConfiguration(RealmConfigurationDTO realmConfigDTO) {
        RealmConfiguration realmConfig = new RealmConfiguration();
        realmConfig.setRealmClassName(realmConfigDTO.getRealmClassName());
        realmConfig.setUserStoreClass(realmConfigDTO.getUserStoreClass());
        realmConfig.setAuthorizationManagerClass(realmConfigDTO.getAuthorizationManagerClass());
        realmConfig.setAdminRoleName(realmConfigDTO.getAdminRoleName());
        realmConfig.setAdminUserName(realmConfigDTO.getAdminUserName());
        realmConfig.setAdminPassword(realmConfigDTO.getAdminPassword());
        realmConfig.setEveryOneRoleName(realmConfigDTO.getEveryOneRoleName());
        realmConfig.setUserStoreProperties(getPropertyValueMap(realmConfigDTO
                .getUserStoreProperties()));
        realmConfig.setAuthzProperties(getPropertyValueMap(realmConfigDTO.getAuthzProperties()));
        realmConfig.setRealmProperties(getPropertyValueMap(realmConfigDTO.getRealmProperties()));
        return realmConfig;
        
    }
    
    private static Map<String, String> getPropertyValueMap(RealmPropertyDTO[] properties) {
        Map<String, String> map = new HashMap<String, String>();
        if(properties == null) {
            return map;
        }
        for (int i = 0; i < properties.length; i++) {
            map.put(properties[i].getName(), properties[i].getValue());
        }
        return map;
    }

    public static org.wso2.carbon.user.core.claim.Claim[] convertToClaims(ClaimDTO[] claims) {
        if (claims == null) {
            return null;
        }
        org.wso2.carbon.user.core.claim.Claim[] claimz = new org.wso2.carbon.user.core.claim.Claim[claims.length];
        int i = 0;
        for (ClaimDTO claim : claims) {
            claimz[i] = convertToClaim(claim);
            i++;
        }
        return claimz;
    }

    /*public static org.wso2.carbon.user.core.claim.Claim[] convertToClaims(Claim[] claims) {
        if (claims == null) {
            return null;
        }
        org.wso2.carbon.user.core.claim.Claim[] claimz = new org.wso2.carbon.user.core.claim.Claim[claims.length];
        int i = 0;
        for (Claim claim : claims) {
            claimz[i] = convertToClaim(claim);
            i++;
        }
        return claimz;
    }*/

    public static ClaimMapping[] convertToClaimMappings(org.wso2.carbon.um.ws.api.stub.ClaimMapping[] claimMappings) {
        if (claimMappings == null) {
            return null;
        }
        ClaimMapping[] claimz = new ClaimMapping[claimMappings.length];
        int i = 0;
        for (org.wso2.carbon.um.ws.api.stub.ClaimMapping claim : claimMappings) {
            claimz[i] = convertToClaimMapping(claim);
            i++;
        }
        return claimz;
    }

    public static org.wso2.carbon.user.core.claim.Claim convertToClaim(ClaimDTO claim) {
        if (claim == null) {
            return null;
        }
        org.wso2.carbon.user.core.claim.Claim claimz = new org.wso2.carbon.user.core.claim.Claim();
        claimz.setClaimUri(claim.getClaimUri());
        claimz.setDescription(claim.getDescription());
        claimz.setDialectURI(claim.getDialectURI());
        claimz.setDisplayOrder(claim.getDisplayOrder());
        claimz.setDisplayTag(claim.getDisplayTag());
        claimz.setRegEx(claim.getRegEx());
        claimz.setRequired(claim.getRequired());
        claimz.setSupportedByDefault(claim.getSupportedByDefault());
        claimz.setValue(claim.getValue());
        return claimz;
    }

    public static org.wso2.carbon.user.core.claim.Claim convertToClaim(org.wso2.carbon.um.ws.api.stub.Claim claim) {
        if (claim == null) {
            return null;
        }
        org.wso2.carbon.user.core.claim.Claim claimz = new org.wso2.carbon.user.core.claim.Claim();
        claimz.setClaimUri(claim.getClaimUri());
        claimz.setDescription(claim.getDescription());
        claimz.setDialectURI(claim.getDialectURI());
        claimz.setDisplayOrder(claim.getDisplayOrder());
        claimz.setDisplayTag(claim.getDisplayTag());
        claimz.setRegEx(claim.getRegEx());
        claimz.setRequired(claim.getRequired());
        claimz.setSupportedByDefault(claim.getSupportedByDefault());
        claimz.setValue(claim.getValue());
        return claimz;
    }


    public static org.wso2.carbon.user.core.claim.ClaimMapping convertToClaimMapping(
            org.wso2.carbon.um.ws.api.stub.ClaimMapping claimMapping) {
        if (claimMapping == null) {
            return null;
        }
        org.wso2.carbon.user.core.claim.ClaimMapping cm = new org.wso2.carbon.user.core.claim.ClaimMapping();
        cm.setClaim(convertToClaim(claimMapping.getClaim()));
        cm.setMappedAttribute(claimMapping.getMappedAttribute());
        return cm;
    }

    public static org.wso2.carbon.user.core.profile.ProfileConfiguration convertToProfileConfiguration(
            ProfileConfiguration profileConfig) {
        if (profileConfig == null) {
            return null;
        }

        org.wso2.carbon.user.core.profile.ProfileConfiguration profz = new org.wso2.carbon.user.core.profile.ProfileConfiguration();
        profz.setDialectName(profileConfig.getDialectName());
        profz.setHiddenClaims(Arrays.asList(profileConfig.getHiddenClaims()));
        profz.setInheritedClaims(Arrays.asList(profileConfig.getInheritedClaims()));
        profz.setOverriddenClaims(Arrays.asList(profileConfig.getOverriddenClaims()));
        profz.setProfileName(profileConfig.getProfileName());
        return profz;
    }

    public static org.wso2.carbon.user.core.profile.ProfileConfiguration[] convertToProfileConfigurations(
            ProfileConfiguration[] profileConfigs) {
        if (profileConfigs == null) {
            return null;
        }

        org.wso2.carbon.user.core.profile.ProfileConfiguration[] configz = new org.wso2.carbon.user.core.profile.ProfileConfiguration[profileConfigs.length];
        int i = 0;
        for (ProfileConfiguration config : profileConfigs) {
            configz[i] = convertToProfileConfiguration(config);
            i++;
        }
        return configz;
    }

}
