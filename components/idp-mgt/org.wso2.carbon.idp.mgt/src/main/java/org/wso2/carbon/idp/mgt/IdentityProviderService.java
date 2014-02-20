/*
*Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.idp.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.idp.mgt.dao.IdPMgtDAO;
import org.wso2.carbon.idp.mgt.dto.TrustedIdPDTO;
import org.wso2.carbon.idp.mgt.exception.IdentityProviderMgtException;
import org.wso2.carbon.idp.mgt.model.TrustedIdPDO;
import org.wso2.carbon.idp.mgt.util.IdentityProviderMgtUtil;
import org.wso2.carbon.idp.mgt.util.SAMLValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IdentityProviderService {

    private static Log log = LogFactory.getLog(IdentityProviderService.class);

    private static IdPMgtDAO dao = new IdPMgtDAO();

    private static IdentityProviderService instance = null;

    // TODO: Need to give OSGi service
    public static IdentityProviderService getInstance(){
        if(instance == null){
            synchronized (IdentityProviderService.class){
                if (instance == null){
                    instance = new IdentityProviderService();
                }
            }
        }
        return instance;
    }

    /**
     * Retrieves registered Identity providers for a given tenant
     *
     * @param tenantDomain Tenant domain whose IdP names are requested
     * @return Identity Provider names and home relam identifiers
     * @throws IdentityProviderMgtException
     */
    public TrustedIdPDTO[] getIdPs(String tenantDomain) {
        TrustedIdPDTO[] trustedIdPDTOs = null;
        try {
            int tenantId = IdentityProviderMgtUtil.getTenantIdOfDomain(tenantDomain);
            List<TrustedIdPDO> tenantIdPs = dao.getIdPs(null, tenantId, tenantDomain);
            trustedIdPDTOs = new TrustedIdPDTO[tenantIdPs.size()];
            for(int i = 0; i < tenantIdPs.size(); i++){
                TrustedIdPDTO trustedIdPDTO = new TrustedIdPDTO();
                trustedIdPDTO.setIdPName(tenantIdPs.get(i).getIdPName());
                trustedIdPDTO.setHomeRealmId(tenantIdPs.get(i).getHomeRealmId());
                trustedIdPDTOs[i] = trustedIdPDTO;
            }
        } catch (IdentityProviderMgtException e) {
            if(log.isDebugEnabled()){
                log.debug("Error occurred while retrieving registered Identity Providers for tenant " + tenantDomain);
            }
        }
        return trustedIdPDTOs;
    }

    /**
     * Retrieves trusted Identity provider information about a given tenant by Identity Provider name
     *
     * @param idPName Unique name of the Identity provider of whose information is requested
     * @param tenantDomain Tenant domain whose information is requested
     * @throws IdentityProviderMgtException
     */
    public TrustedIdPDTO getIdPByName(String idPName, String tenantDomain) {
        try {
            int tenantId = IdentityProviderMgtUtil.getTenantIdOfDomain(tenantDomain);
            TrustedIdPDO trustedIdPDO = dao.getIdPByName(idPName, tenantId, tenantDomain);
            TrustedIdPDTO trustedIdPDTO = null;
            if(trustedIdPDO != null){
                trustedIdPDTO = new TrustedIdPDTO();
                trustedIdPDTO.setIdPName(idPName);
                trustedIdPDTO.setPrimary(trustedIdPDO.isPrimary());
                trustedIdPDTO.setHomeRealmId(trustedIdPDO.getHomeRealmId());
                if(trustedIdPDO.getPublicCertThumbPrint() != null){
                    trustedIdPDTO.setPublicCert(IdentityProviderMgtUtil.getEncodedIdPCertFromAlias(idPName, tenantId, tenantDomain));
                }
                trustedIdPDTO.setTokenEndpointAlias(trustedIdPDO.getTokenEndpointAlias());

                trustedIdPDTO.setClaims(trustedIdPDO.getClaims().toArray(new String[trustedIdPDO.getClaims().size()]));
                List<String> appendedClaimMappings = new ArrayList<String>();
                for(Map.Entry<String,String> entry:trustedIdPDO.getClaimMappings().entrySet()){
                    String idpClaim = entry.getKey();
                    String tenantClaim = entry.getValue();
                    appendedClaimMappings.add(idpClaim + ":" + tenantClaim);
                }
                trustedIdPDTO.setClaimMappings(appendedClaimMappings.toArray(new String[appendedClaimMappings.size()]));

                trustedIdPDTO.setRoles(trustedIdPDO.getRoles().toArray(new String[trustedIdPDO.getRoles().size()]));
                List<String> appendedRoleMappings = new ArrayList<String>();
                for(Map.Entry<String,String> entry:trustedIdPDO.getRoleMappings().entrySet()){
                    String idpRole = entry.getKey();
                    String tenantRole = entry.getValue();
                    appendedRoleMappings.add(idpRole+":"+tenantRole);
                }
                trustedIdPDTO.setRoleMappings(appendedRoleMappings.toArray(new String[appendedRoleMappings.size()]));

                trustedIdPDTO.setSAML2SSOEnabled(trustedIdPDO.isSAML2SSOEnabled());
                trustedIdPDTO.setIdpEntityId(trustedIdPDO.getIdpEntityId());
                trustedIdPDTO.setSpEntityId(trustedIdPDO.getSpEntityId());
                trustedIdPDTO.setSSOUrl(trustedIdPDO.getSSOUrl());
                trustedIdPDTO.setAuthnRequestSigned(trustedIdPDO.isAuthnRequestSigned());
                trustedIdPDTO.setLogoutEnabled(trustedIdPDO.isLogoutEnabled());
                trustedIdPDTO.setLogoutRequestUrl(trustedIdPDO.getLogoutRequestUrl());
                trustedIdPDTO.setLogoutRequestSigned(trustedIdPDO.isLogoutRequestSigned());
                trustedIdPDTO.setAuthnResponseSigned(trustedIdPDO.isAuthnResponseSigned());
                trustedIdPDTO.setOIDCEnabled(trustedIdPDO.isOIDCEnabled());
                trustedIdPDTO.setClientId(trustedIdPDO.getClientId());
                trustedIdPDTO.setClientSecret(trustedIdPDO.getClientSecret());
                trustedIdPDTO.setAuthzEndpointUrl(trustedIdPDO.getAuthzEndpointUrl());
                trustedIdPDTO.setTokenEndpointUrl(trustedIdPDO.getTokenEndpointUrl());
            }
            return trustedIdPDTO;
        } catch (IdentityProviderMgtException e) {
            if(log.isDebugEnabled()){
                log.debug("Error occurred while retrieving information for Identity Providers " + idPName + " for tenant " + tenantDomain);
            }
        }
        return null;
    }

    /**
     * Retrieves trusted Identity provider information about a given tenant by realm identifier
     *
     * @param realmId Unique realm identifier of the Identity provider of whose information is requested
     * @param tenantDomain Tenant domain whose information is requested
     * @throws IdentityProviderMgtException
     */
    public TrustedIdPDTO getIdPByRealmId(String realmId, String tenantDomain) {
        try {
            int tenantId = IdentityProviderMgtUtil.getTenantIdOfDomain(tenantDomain);
            TrustedIdPDO trustedIdPDO = dao.getIdPByRealmId(realmId, tenantId, tenantDomain);
            TrustedIdPDTO trustedIdPDTO = null;
            if(trustedIdPDO != null){
                trustedIdPDTO = new TrustedIdPDTO();
                trustedIdPDTO.setIdPName(trustedIdPDO.getIdPName());
                trustedIdPDTO.setPrimary(trustedIdPDO.isPrimary());
                trustedIdPDTO.setHomeRealmId(realmId);
                if(trustedIdPDO.getPublicCertThumbPrint() != null){
                    trustedIdPDTO.setPublicCert(IdentityProviderMgtUtil.getEncodedIdPCertFromAlias(realmId, tenantId, tenantDomain));
                }
                trustedIdPDTO.setTokenEndpointAlias(trustedIdPDO.getTokenEndpointAlias());

                trustedIdPDTO.setClaims(trustedIdPDO.getClaims().toArray(new String[trustedIdPDO.getClaims().size()]));
                List<String> appendedClaimMappings = new ArrayList<String>();
                for(Map.Entry<String,String> entry:trustedIdPDO.getClaimMappings().entrySet()){
                    String idpClaim = entry.getKey();
                    String tenantClaim = entry.getValue();
                    appendedClaimMappings.add(idpClaim + ":" + tenantClaim);
                }
                trustedIdPDTO.setClaimMappings(appendedClaimMappings.toArray(new String[appendedClaimMappings.size()]));

                trustedIdPDTO.setRoles(trustedIdPDO.getRoles().toArray(new String[trustedIdPDO.getRoles().size()]));
                List<String> appendedRoleMappings = new ArrayList<String>();
                for(Map.Entry<String,String> entry:trustedIdPDO.getRoleMappings().entrySet()){
                    String idpRole = entry.getKey();
                    String tenantRole = entry.getValue();
                    appendedRoleMappings.add(idpRole+":"+tenantRole);
                }
                trustedIdPDTO.setRoleMappings(appendedRoleMappings.toArray(new String[appendedRoleMappings.size()]));

                trustedIdPDTO.setSAML2SSOEnabled(trustedIdPDO.isSAML2SSOEnabled());
                trustedIdPDTO.setIdpEntityId(trustedIdPDO.getIdpEntityId());
                trustedIdPDTO.setSpEntityId(trustedIdPDO.getSpEntityId());
                trustedIdPDTO.setSSOUrl(trustedIdPDO.getSSOUrl());
                trustedIdPDTO.setAuthnRequestSigned(trustedIdPDO.isAuthnRequestSigned());
                trustedIdPDTO.setLogoutEnabled(trustedIdPDO.isLogoutEnabled());
                trustedIdPDTO.setLogoutRequestUrl(trustedIdPDO.getLogoutRequestUrl());
                trustedIdPDTO.setLogoutRequestSigned(trustedIdPDO.isLogoutRequestSigned());
                trustedIdPDTO.setAuthnResponseSigned(trustedIdPDO.isAuthnResponseSigned());
                trustedIdPDTO.setOIDCEnabled(trustedIdPDO.isOIDCEnabled());
                trustedIdPDTO.setClientId(trustedIdPDO.getClientId());
                trustedIdPDTO.setClientSecret(trustedIdPDO.getClientSecret());
                trustedIdPDTO.setAuthzEndpointUrl(trustedIdPDO.getAuthzEndpointUrl());
                trustedIdPDTO.setTokenEndpointUrl(trustedIdPDO.getTokenEndpointUrl());
            }
            return trustedIdPDTO;
        } catch (IdentityProviderMgtException e) {
            if(log.isDebugEnabled()){
                log.debug("Error occurred while retrieving information for Identity Providers " + realmId + " for tenant " + tenantDomain);
            }
        }
        return null;
    }

    /**
     * Retrieves trusted Identity provider information about a given tenant
     *
     * @param idPName Unique Name of the IdP to which the given IdP claim URIs need to be mapped
     * @param tenantDomain The tenant domain of claim URIs to be mapped
     * @param idPClaims IdP claim URIs which need to be mapped to tenant's claim URIs
     * @throws IdentityProviderMgtException
     */
    public String[] getMappedTenantClaims(String idPName, String tenantDomain, String[] idPClaims) {
        List<String> mappedClaims = new ArrayList<String>();
        try {
            int tenantId = IdentityProviderMgtUtil.getTenantIdOfDomain(tenantDomain);
            TrustedIdPDO trustedIdPDO = dao.getIdPByName(idPName, tenantId, tenantDomain);
            Map<String, String> claimMappings = trustedIdPDO.getClaimMappings();
            if(claimMappings != null && !claimMappings.isEmpty()){
                if(idPClaims == null){
                    for(Map.Entry<String,String> claimMapping: claimMappings.entrySet()){
                        mappedClaims.add(claimMapping.getKey() + ":" + claimMapping.getValue());
                    }
                } else {
                    for(String idpClaim : idPClaims){
                        if(claimMappings.containsKey(idpClaim)){
                            mappedClaims.add(idpClaim + ":" + claimMappings.get(idpClaim));
                        } else {
                            mappedClaims.add(idpClaim+":");
                        }
                    }
                }
            }
            return mappedClaims.toArray(new String[mappedClaims.size()]);
        } catch (IdentityProviderMgtException e) {
            if(log.isDebugEnabled()){
                log.debug("Error occurred while retrieving Tenant Claim URI mappings for " +
                        "Identity Providers " + idPName + " for tenant " + tenantDomain);
            }
        }
        return new String[0];
    }

    /**
     * Retrieves trusted Identity provider information about a given tenant
     *
     * @param idPName Unique Name of the IdP to which the given tenant claim URIs need to be mapped
     * @param tenantDomain The tenant domain of claim URIs to be mapped
     * @param tenantClaims Tenant claim URIs which need to be mapped to trusted IdP's claim URIs
     * @throws IdentityProviderMgtException
     */
    public String[] getMappedIdPClaims(String idPName, String tenantDomain, String[] tenantClaims) {

        List<String> mappedClaims = new ArrayList<String>();
        try {
            int tenantId = IdentityProviderMgtUtil.getTenantIdOfDomain(tenantDomain);
            TrustedIdPDO trustedIdPDO = dao.getIdPByName(idPName, tenantId, tenantDomain);
            Map<String, String> claimMappings = trustedIdPDO.getClaimMappings();
            Map<String,String> mirrorMap = new HashMap<String,String>();
            if(claimMappings != null && !claimMappings.isEmpty()){
                for(Map.Entry<String,String> claimMapping : claimMappings.entrySet()){
                    String key = claimMapping.getKey();
                    String value = claimMapping.getValue();
                    if(mirrorMap.containsKey(value)){
                        mirrorMap.put(value, mirrorMap.get(value) + "," + key);
                    } else {
                        mirrorMap.put(value, key);
                    }
                }
                if(tenantClaims == null){
                    for(Map.Entry<String,String> mirrorClaim: mirrorMap.entrySet()){
                        mappedClaims.add(mirrorClaim.getKey() + ":" + mirrorClaim.getValue());
                    }
                } else {
                    for(String tenantClaim : tenantClaims){
                        if(mirrorMap.containsKey(tenantClaim)){
                            mappedClaims.add(tenantClaim + ":" + mirrorMap.get(tenantClaim));
                        } else {
                            mappedClaims.add(tenantClaim + ":");
                        }

                    }
                }
            }
            return mappedClaims.toArray(new String[mappedClaims.size()]);
        } catch (IdentityProviderMgtException e){
            if(log.isDebugEnabled()){
                log.debug("Error occurred while retrieving IdP claim URI mappings for " +
                        "Identity Providers " + idPName + " for tenant " + tenantDomain);
            }
        }
        return new String[0];
    }

    /**
     * Retrieves trusted Identity provider information about a given tenant
     *
     * @param idPName Unique Name of the IdP to which the given IdP roles need to be mapped
     * @param tenantDomain The tenant domain of of roles to be mapped
     * @param idPRoles IdP Roles which need to be mapped to tenant's roles
     * @throws IdentityProviderMgtException
     */
    public String[] getMappedTenantRoles(String idPName, String tenantDomain, String[] idPRoles) {
        List<String> mappedRoles = new ArrayList<String>();
        try {
            int tenantId = IdentityProviderMgtUtil.getTenantIdOfDomain(tenantDomain);
            TrustedIdPDO trustedIdPDO = dao.getIdPByName(idPName, tenantId, tenantDomain);
            Map<String, String> roleMappings = trustedIdPDO.getRoleMappings();
            if(roleMappings != null && !roleMappings.isEmpty()){
                if(idPRoles == null){
                    for(Map.Entry<String,String> roleMapping: roleMappings.entrySet()){
                        mappedRoles.add(roleMapping.getKey() + ":" + roleMapping.getValue());
                    }
                } else {
                    for(String idPRole : idPRoles){
                        if(roleMappings.containsKey(idPRole)){
                            mappedRoles.add(idPRole + ":" + roleMappings.get(idPRole));
                        } else {
                            mappedRoles.add(idPRole+":");
                        }
                    }
                }
            }
            return mappedRoles.toArray(new String[mappedRoles.size()]);
        } catch (IdentityProviderMgtException e) {
            if(log.isDebugEnabled()){
                log.debug("Error occurred while retrieving Tenant Role mappings for " +
                        "Identity Providers " + idPName + " for tenant " + tenantDomain);
            }
        }
        return new String[0];
    }

    /**
     * Retrieves trusted Identity provider information about a given tenant
     *
     * @param idPName Unique Name of the IdP to which the given tenant roles need to be mapped
     * @param tenantDomain The tenant domain of of roles to be mapped
     * @param tenantRoles Tenant Roles which need to be mapped to trusted IdP's roles
     * @throws IdentityProviderMgtException
     */
    public String[] getMappedIdPRoles(String idPName, String tenantDomain, String[] tenantRoles) {

        List<String> mappedRoles = new ArrayList<String>();
        try {
            int tenantId = IdentityProviderMgtUtil.getTenantIdOfDomain(tenantDomain);
            TrustedIdPDO trustedIdPDO = dao.getIdPByName(idPName, tenantId, tenantDomain);
            Map<String, String> roleMappings = trustedIdPDO.getRoleMappings();
            Map<String,String> mirrorMap = new HashMap<String,String>();
            if(roleMappings != null && !roleMappings.isEmpty()){
                for(Map.Entry<String,String> roleMapping : roleMappings.entrySet()){
                    String key = roleMapping.getKey();
                    String value = roleMapping.getValue();
                    if(mirrorMap.containsKey(value)){
                        mirrorMap.put(value, mirrorMap.get(value) + "," + key);
                    } else {
                        mirrorMap.put(value, key);
                    }
                }
                if(tenantRoles == null){
                    for(Map.Entry<String,String> mirrorRole: mirrorMap.entrySet()){
                        mappedRoles.add(mirrorRole.getKey() + ":" + mirrorRole.getValue());
                    }
                } else {
                    for(String tenantRole : tenantRoles){
                        if(mirrorMap.containsKey(tenantRole)){
                            mappedRoles.add(tenantRole + ":" + mirrorMap.get(tenantRole));
                        } else {
                            mappedRoles.add(tenantRole + ":");
                        }

                    }
                }
            }
            return mappedRoles.toArray(new String[mappedRoles.size()]);
        } catch (IdentityProviderMgtException e){
            if(log.isDebugEnabled()){
                log.debug("Error occurred while retrieving IdP Role mappings for " +
                        "Identity Providers " + idPName + " for tenant " + tenantDomain);
            }
        }
        return new String[0];
    }

    /**
     * Retrieves the primary Identity provider name for a given tenant
     *
     * @param tenantDomain The tenant domain of of roles to be mapped
     * @return primary Identity Provider name and home realm identifier
     * @throws IdentityProviderMgtException
     */
    public TrustedIdPDTO getPrimaryIdP(String tenantDomain) {

        TrustedIdPDTO[] trustedIdPDTOs = getIdPs(tenantDomain);
        for(TrustedIdPDTO trustedIdPDTO : trustedIdPDTOs){
            TrustedIdPDTO trustedIdP = getIdPByName(trustedIdPDTO.getIdPName(), tenantDomain);
            if(trustedIdP.isPrimary()){
                return trustedIdP;
            }
        }
        if(log.isDebugEnabled()){
            log.debug("No primary Identity Provider found for tenant " + tenantDomain);
        }
        return null;
    }

    public boolean validateSAMLResponse(String tenantDomain, String idPName, String samlResponseString, String[] audiences,
                                        boolean validateSAMLResponse, boolean validateSAMLAssertion) {

        try {
            return SAMLValidator.validateSAMLResponse(getIdPByName(idPName, tenantDomain),
                    samlResponseString, audiences, validateSAMLResponse, validateSAMLAssertion);
        } catch (IdentityProviderMgtException e){
            if(log.isDebugEnabled()){
                log.debug("Error occurred while validating SAML2 Response message");
            }
            return false;
        }
    }

    public boolean validateSAMLAssertion(String tenantDomain, String idPName, String samlResponseString, String[] audiences,
                                         boolean validateSAMLResponse, boolean validateSAMLAssertion) {

        try {
            return SAMLValidator.validateSAMLResponse(getIdPByName(idPName, tenantDomain),
                    samlResponseString, audiences, validateSAMLResponse, validateSAMLAssertion);
        } catch (IdentityProviderMgtException e){
            if(log.isDebugEnabled()){
                log.debug("Error occurred while validating SAML2 Response message");
            }
            return false;
        }
    }

}
