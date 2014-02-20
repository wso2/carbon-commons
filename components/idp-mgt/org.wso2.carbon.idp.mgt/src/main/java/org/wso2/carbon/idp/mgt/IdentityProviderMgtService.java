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
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.idp.mgt.dao.IdPMgtDAO;
import org.wso2.carbon.idp.mgt.dto.TrustedIdPDTO;
import org.wso2.carbon.idp.mgt.exception.IdentityProviderMgtException;
import org.wso2.carbon.idp.mgt.model.TrustedIdPDO;
import org.wso2.carbon.idp.mgt.util.IdentityProviderMgtUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;

import java.util.*;

public class IdentityProviderMgtService {

    private static final Log log = LogFactory.getLog(IdentityProviderMgtService.class);

    private IdPMgtDAO dao = new IdPMgtDAO();

    /**
     * Retrieves registered IdPs for a given tenant
     *
     * @throws IdentityProviderMgtException
     */
    public TrustedIdPDTO[] getIdPs() throws IdentityProviderMgtException {
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        TrustedIdPDTO[] trustedIdPDTOs = null;
        try {
            List<TrustedIdPDO> tenantIdPs = dao.getIdPs(null, tenantId, tenantDomain);
            trustedIdPDTOs = new TrustedIdPDTO[tenantIdPs.size()];
            for(int i = 0; i < tenantIdPs.size(); i++){
                TrustedIdPDTO trustedIdPDTO = new TrustedIdPDTO();
                trustedIdPDTO.setIdPName(tenantIdPs.get(i).getIdPName());
                trustedIdPDTO.setHomeRealmId(tenantIdPs.get(i).getHomeRealmId());
                trustedIdPDTOs[i] = trustedIdPDTO;
            }
            return trustedIdPDTOs;
        } catch (IdentityProviderMgtException e) {
            throw new IdentityProviderMgtException("Error getting service-provider DB connection", e);
        }
    }

    /**
     * Retrieves trusted IdP information about a given tenant
     *
     * @throws IdentityProviderMgtException
     */
    public TrustedIdPDTO getIdPByName(String idPName) throws IdentityProviderMgtException {

        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        TrustedIdPDO trustedIdPDO = dao.getIdPByName(idPName, tenantId, tenantDomain);
        TrustedIdPDTO trustedIdPDTO = null;
        if(trustedIdPDO != null){
            trustedIdPDTO = new TrustedIdPDTO();
            trustedIdPDTO.setIdPName(trustedIdPDO.getIdPName());
            trustedIdPDTO.setPrimary(trustedIdPDO.isPrimary());
            trustedIdPDTO.setHomeRealmId(trustedIdPDO.getHomeRealmId());
            if(trustedIdPDO.getPublicCertThumbPrint() != null){
                trustedIdPDTO.setPublicCert(IdentityProviderMgtUtil.getEncodedIdPCertFromAlias(trustedIdPDO.getIdPName(), tenantId, tenantDomain));
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
    }

    /**
     * Updates a given tenant with trusted IDP information
     *
     * @param oldTrustedIdP existing tenant IdP information
     * @param newTrustedIdP new tenant IdP information
     * @throws IdentityProviderMgtException
     */
    public void updateIdP(TrustedIdPDTO oldTrustedIdP, TrustedIdPDTO newTrustedIdP) throws IdentityProviderMgtException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        if(oldTrustedIdP == null && newTrustedIdP == null){
            log.error("Arguments are NULL");
            throw new IdentityProviderMgtException("Invalid arguments");
        } else if (oldTrustedIdP != null && newTrustedIdP == null){
            doDeleteIdP(oldTrustedIdP, tenantId, tenantDomain);
            return;
        } else if(oldTrustedIdP == null && newTrustedIdP !=null){
            doAddIdP(newTrustedIdP, tenantId, tenantDomain);
            return;
        }

        TrustedIdPDO newTrustedIdPDO = new TrustedIdPDO();
        TrustedIdPDO oldTrustedIdPDO = new TrustedIdPDO();

        if(oldTrustedIdP.getIdPName() == null || oldTrustedIdP.getIdPName().equals("")){
            String msg = "Invalid arguments: Identity Provider Name value is empty";
            log.error(msg);
            throw new IdentityProviderMgtException(msg);
        }
        if(oldTrustedIdP.isPrimary() == true && newTrustedIdP.isPrimary() == false){
            String msg = "Invalid arguments: Cannot unset Identity Provider from primary. " +
                    "Alternatively set new Identity Provider to primary";
            log.error(msg);
            throw new IdentityProviderMgtException(msg);
        }
        oldTrustedIdPDO.setIdPName(oldTrustedIdP.getIdPName());
        oldTrustedIdPDO.setPrimary(oldTrustedIdP.isPrimary());
        oldTrustedIdPDO.setHomeRealmId(oldTrustedIdP.getHomeRealmId());
        if(oldTrustedIdP.getPublicCert() != null){
            oldTrustedIdPDO.setPublicCertThumbPrint(IdentityProviderMgtUtil.generatedThumbPrint(oldTrustedIdP.getPublicCert()));
        }
        oldTrustedIdPDO.setTokenEndpointAlias(oldTrustedIdP.getTokenEndpointAlias());

        if(oldTrustedIdP.getClaims() != null){
            oldTrustedIdPDO.setClaims(new ArrayList<String>(Arrays.asList(oldTrustedIdP.getClaims())));
        } else {
            oldTrustedIdPDO.setClaims(new ArrayList<String>());
        }
        for(int i = 0; i < oldTrustedIdPDO.getClaims().size(); i++){
            if(oldTrustedIdPDO.getClaims().get(i) == null){
                String msg = "Invalid arguments: claim URI names cannot be \'NULL\'";
                log.error(msg);
                throw new IdentityProviderMgtException(msg);
            }else if(oldTrustedIdPDO.getClaims().get(i).equals("")){
                String msg = "Invalid arguments: claim URI names cannot be strings of zero length in \'oldTrustedIdP\' argument";
                log.error(msg);
                throw new IdentityProviderMgtException(msg);
            }
        }
        Map<String,String> claimMappings = new HashMap<String, String>();
        if(oldTrustedIdP.getClaimMappings() != null){
            for(String mapping:oldTrustedIdP.getClaimMappings()){
                String[] split = mapping.split(":");
                claimMappings.put(split[0],split[1]);
            }
        }
        oldTrustedIdPDO.setClaimMappings(claimMappings);

        if(oldTrustedIdP.getRoles() != null){
            oldTrustedIdPDO.setRoles(new ArrayList<String>(Arrays.asList(oldTrustedIdP.getRoles())));
        } else {
            oldTrustedIdPDO.setRoles(new ArrayList<String>());
        }
        for(int i = 0; i < oldTrustedIdPDO.getRoles().size(); i++){
            if(oldTrustedIdPDO.getRoles().get(i) == null){
                String msg = "Invalid arguments: role names cannot be \'NULL\'";
                log.error(msg);
                throw new IdentityProviderMgtException(msg);
            }else if(oldTrustedIdPDO.getRoles().get(i).equals("")){
                String msg = "Invalid arguments: role names cannot be strings of zero length in \'oldTrustedIdP\' argument";
                log.error(msg);
                throw new IdentityProviderMgtException(msg);
            }
        }
        Map<String,String> roleMappings = new HashMap<String, String>();
        if(oldTrustedIdP.getRoleMappings() != null){
            for(String mapping:oldTrustedIdP.getRoleMappings()){
                String[] split = mapping.split(":");
                roleMappings.put(split[0],split[1]);
            }
        }
        oldTrustedIdPDO.setRoleMappings(roleMappings);

        oldTrustedIdPDO.setSAML2SSOEnabled(oldTrustedIdP.isSAML2SSOEnabled());
        oldTrustedIdPDO.setIdpEntityId(oldTrustedIdP.getIdpEntityId());
        oldTrustedIdPDO.setSpEntityId(oldTrustedIdP.getSpEntityId());
        oldTrustedIdPDO.setSSOUrl(oldTrustedIdP.getSSOUrl());
        oldTrustedIdPDO.setAuthnRequestSigned(oldTrustedIdP.isAuthnRequestSigned());
        oldTrustedIdPDO.setLogoutEnabled(oldTrustedIdP.isLogoutEnabled());
        oldTrustedIdPDO.setLogoutRequestUrl(oldTrustedIdP.getLogoutRequestUrl());
        oldTrustedIdPDO.setLogoutRequestSigned(oldTrustedIdP.isLogoutRequestSigned());
        oldTrustedIdPDO.setAuthnResponseSigned(oldTrustedIdP.isAuthnResponseSigned());
        oldTrustedIdPDO.setOIDCEnabled(oldTrustedIdP.isOIDCEnabled());
        oldTrustedIdPDO.setClientId(oldTrustedIdP.getClientId());
        oldTrustedIdPDO.setClientSecret(oldTrustedIdP.getClientSecret());
        oldTrustedIdPDO.setAuthzEndpointUrl(oldTrustedIdP.getAuthzEndpointUrl());
        oldTrustedIdPDO.setTokenEndpointUrl(oldTrustedIdP.getTokenEndpointUrl());

        if(newTrustedIdP.getIdPName() == null || newTrustedIdP.getIdPName().equals("")){
            String msg = "Invalid arguments: Identity Provider Name value is empty";
            log.error(msg);
            throw new IdentityProviderMgtException(msg);
        }
        newTrustedIdPDO.setIdPName(newTrustedIdP.getIdPName());
        newTrustedIdPDO.setPrimary(newTrustedIdP.isPrimary());
        newTrustedIdPDO.setHomeRealmId(newTrustedIdP.getHomeRealmId());
        if(newTrustedIdP.getPublicCert() != null){
            newTrustedIdPDO.setPublicCertThumbPrint(IdentityProviderMgtUtil.generatedThumbPrint(newTrustedIdP.getPublicCert()));
        }
        newTrustedIdPDO.setTokenEndpointAlias(newTrustedIdP.getTokenEndpointAlias());

        if(newTrustedIdP.getClaims() != null){
            newTrustedIdPDO.setClaims(new ArrayList<String>(Arrays.asList(newTrustedIdP.getClaims())));
        } else {
            newTrustedIdPDO.setClaims(new ArrayList<String>());
        }
        for(int i = 0; i < newTrustedIdPDO.getClaims().size(); i++){
            if(newTrustedIdPDO.getClaims().get(i) == null){
                String msg = "Invalid arguments: claim URI names cannot be \'NULL\'";
                log.error(msg);
                throw new IdentityProviderMgtException(msg);
            }
            if(newTrustedIdPDO.getClaims().get(i).equals("")){
                newTrustedIdPDO.getClaims().remove(i);
                newTrustedIdPDO.getClaims().add(i, null);
            }
        }
        claimMappings = new HashMap<String, String>();
        if(newTrustedIdP.getClaimMappings() != null){
            for(String mapping:newTrustedIdP.getClaimMappings()){
                String[] split = mapping.split(":");
                // validate if claim URI exists
                claimMappings.put(split[0],split[1]);
            }
        }
        newTrustedIdPDO.setClaimMappings(claimMappings);

        if(newTrustedIdP.getRoles() != null){
            newTrustedIdPDO.setRoles(new ArrayList<String>(Arrays.asList(newTrustedIdP.getRoles())));
        } else {
            newTrustedIdPDO.setRoles(new ArrayList<String>());
        }
        for(int i = 0; i < newTrustedIdPDO.getRoles().size(); i++){
            if(newTrustedIdPDO.getRoles().get(i) == null){
                String msg = "Invalid arguments: role names cannot be \'NULL\'";
                log.error(msg);
                throw new IdentityProviderMgtException(msg);
            }
            if(newTrustedIdPDO.getRoles().get(i).equals("")){
                newTrustedIdPDO.getRoles().remove(i);
                newTrustedIdPDO.getRoles().add(i, null);
            }
        }
        roleMappings = new HashMap<String, String>();
        if(newTrustedIdP.getRoleMappings() != null){
            for(String mapping:newTrustedIdP.getRoleMappings()){
                String[] split = mapping.split(":");
                UserStoreManager usm = null;
                try {
                    usm = CarbonContext.getThreadLocalCarbonContext().getUserRealm().getUserStoreManager();
                    if(usm.isExistingRole(split[1]) || usm.isExistingRole(split[1], true)){
                        String msg = "Cannot find tenant role " + split[1] + " for tenant " + tenantDomain;
                        log.error(msg);
                        throw new IdentityProviderMgtException(msg);
                    }
                } catch (UserStoreException e) {
                    String msg = "Error occurred while retrieving UserStoreManager for tenant " + tenantDomain;
                    log.error(msg);
                    throw new IdentityProviderMgtException(msg);
                }
                roleMappings.put(split[0],split[1]);
            }
        }
        newTrustedIdPDO.setRoleMappings(roleMappings);

        newTrustedIdPDO.setSAML2SSOEnabled(newTrustedIdP.isSAML2SSOEnabled());
        newTrustedIdPDO.setIdpEntityId(newTrustedIdP.getIdpEntityId());
        newTrustedIdPDO.setSpEntityId(newTrustedIdP.getSpEntityId());
        newTrustedIdPDO.setSSOUrl(newTrustedIdP.getSSOUrl());
        newTrustedIdPDO.setAuthnRequestSigned(newTrustedIdP.isAuthnRequestSigned());
        newTrustedIdPDO.setLogoutEnabled(newTrustedIdP.isLogoutEnabled());
        newTrustedIdPDO.setLogoutRequestUrl(newTrustedIdP.getLogoutRequestUrl());
        newTrustedIdPDO.setLogoutRequestSigned(newTrustedIdP.isLogoutRequestSigned());
        newTrustedIdPDO.setAuthnResponseSigned(newTrustedIdP.isAuthnResponseSigned());
        newTrustedIdPDO.setOIDCEnabled(newTrustedIdP.isOIDCEnabled());
        newTrustedIdPDO.setClientId(newTrustedIdP.getClientId());
        newTrustedIdPDO.setClientSecret(newTrustedIdP.getClientSecret());
        newTrustedIdPDO.setAuthzEndpointUrl(newTrustedIdP.getAuthzEndpointUrl());
        newTrustedIdPDO.setTokenEndpointUrl(newTrustedIdP.getTokenEndpointUrl());

        dao.updateIdP(oldTrustedIdPDO, newTrustedIdPDO, tenantId, tenantDomain);

        if(oldTrustedIdPDO.getPublicCertThumbPrint() != null &&
                newTrustedIdPDO.getPublicCertThumbPrint() != null &&
                !oldTrustedIdPDO.getPublicCertThumbPrint().equals(newTrustedIdPDO.getPublicCertThumbPrint())){
            IdentityProviderMgtUtil.updateCertToStore(oldTrustedIdP.getIdPName(), newTrustedIdP.getIdPName(), newTrustedIdP.getPublicCert(), tenantId, tenantDomain);
        } else if(oldTrustedIdPDO.getPublicCertThumbPrint() == null && newTrustedIdPDO.getPublicCertThumbPrint() != null){
            IdentityProviderMgtUtil.importCertToStore(newTrustedIdP.getIdPName(), newTrustedIdP.getPublicCert(), tenantId, tenantDomain);
        } else if(oldTrustedIdPDO.getPublicCertThumbPrint() != null && newTrustedIdPDO.getPublicCertThumbPrint() == null){
            IdentityProviderMgtUtil.deleteCertFromStore(oldTrustedIdP.getIdPName(), tenantId, tenantDomain);
        }
    }

    private void doAddIdP(TrustedIdPDTO trustedIdP, int tenantId, String tenantDomain) throws IdentityProviderMgtException {

        TrustedIdPDO trustedIdPDO = new TrustedIdPDO();
        if(trustedIdP.getIdPName() == null || trustedIdP.getIdPName().equals("")){
            String msg = "Invalid arguments: Identity Provider Name value is empty";
            log.error(msg);
            throw new IdentityProviderMgtException(msg);
        }
        trustedIdPDO.setIdPName(trustedIdP.getIdPName());
        if(dao.isTenantIdPExisting(null, trustedIdPDO, tenantId, tenantDomain) > 0){
            String msg = "An Identity Provider has already been registered with the name " + trustedIdPDO.getIdPName() +
                    " for tenant " + tenantDomain;
            log.error(msg);
            throw new IdentityProviderMgtException(msg);
        }
        trustedIdPDO.setPrimary(trustedIdP.isPrimary());
        trustedIdPDO.setHomeRealmId(trustedIdP.getHomeRealmId());
        if(trustedIdP.getPublicCert() != null){
            trustedIdPDO.setPublicCertThumbPrint(IdentityProviderMgtUtil.generatedThumbPrint(trustedIdP.getPublicCert()));
        }
        trustedIdPDO.setTokenEndpointAlias(trustedIdP.getTokenEndpointAlias());

        if(trustedIdP.getClaims() != null && trustedIdP.getClaims().length > 0){
            trustedIdPDO.setClaims(new ArrayList<String>(Arrays.asList(trustedIdP.getClaims())));
        } else {
            trustedIdPDO.setClaims(new ArrayList<String>());
        }
        for(String claim:trustedIdPDO.getClaims()){
            if(claim.equals("")){
                String msg = "Invalid arguments: claim URI name strings cannot be of zero length";
                log.error(msg);
                throw new IdentityProviderMgtException(msg);
            }
        }
        Map<String,String> claimMappings = new HashMap<String, String>();
        if(trustedIdP.getClaimMappings() != null && trustedIdP.getClaimMappings().length > 0){
            for(String mapping:trustedIdP.getClaimMappings()){
                String[] split = mapping.split(":");
                // validate if claim URI exists
                claimMappings.put(split[0],split[1]);
            }
        }
        trustedIdPDO.setClaimMappings(claimMappings);

        if(trustedIdP.getRoles() != null && trustedIdP.getRoles().length > 0){
            trustedIdPDO.setRoles(new ArrayList<String>(Arrays.asList(trustedIdP.getRoles())));
        } else {
            trustedIdPDO.setRoles(new ArrayList<String>());
        }
        for(String role:trustedIdPDO.getRoles()){
            if(role.equals("")){
                String msg = "Invalid arguments: role name strings cannot be of zero length";
                log.error(msg);
                throw new IdentityProviderMgtException(msg);
            }
        }
        Map<String,String> roleMappings = new HashMap<String, String>();
        if(trustedIdP.getRoleMappings() != null && trustedIdP.getRoleMappings().length > 0){
            for(String mapping:trustedIdP.getRoleMappings()){
                String[] split = mapping.split(":");
                UserStoreManager usm = null;
                try {
                    usm = CarbonContext.getThreadLocalCarbonContext().getUserRealm().getUserStoreManager();
                    if(!usm.isExistingRole(split[1]) || ((AbstractUserStoreManager)usm).isSharedGroupEnabled() && !usm.isExistingRole(split[1], true)){
                        String msg = "Cannot find tenant role " + split[1] + " for tenant " + tenantDomain;
                        log.error(msg);
                        throw new IdentityProviderMgtException(msg);
                    }
                } catch (UserStoreException e) {
                    String msg = "Error occurred while retrieving UserStoreManager for tenant " + tenantDomain;
                    log.error(msg);
                    throw new IdentityProviderMgtException(msg);
                }
                roleMappings.put(split[0],split[1]);
            }
        }
        trustedIdPDO.setRoleMappings(roleMappings);

        trustedIdPDO.setSAML2SSOEnabled(trustedIdP.isSAML2SSOEnabled());
        trustedIdPDO.setIdpEntityId(trustedIdP.getIdpEntityId());
        trustedIdPDO.setSpEntityId(trustedIdP.getSpEntityId());
        trustedIdPDO.setSSOUrl(trustedIdP.getSSOUrl());
        trustedIdPDO.setAuthnRequestSigned(trustedIdP.isAuthnRequestSigned());
        trustedIdPDO.setLogoutEnabled(trustedIdP.isLogoutEnabled());
        trustedIdPDO.setLogoutRequestUrl(trustedIdP.getLogoutRequestUrl());
        trustedIdPDO.setLogoutRequestSigned(trustedIdP.isLogoutRequestSigned());
        trustedIdPDO.setAuthnResponseSigned(trustedIdP.isAuthnResponseSigned());
        trustedIdPDO.setOIDCEnabled(trustedIdP.isOIDCEnabled());
        trustedIdPDO.setClientId(trustedIdP.getClientId());
        trustedIdPDO.setClientSecret(trustedIdP.getClientSecret());
        trustedIdPDO.setAuthzEndpointUrl(trustedIdP.getAuthzEndpointUrl());
        trustedIdPDO.setTokenEndpointUrl(trustedIdP.getTokenEndpointUrl());

        dao.addIdP(trustedIdPDO, tenantId, tenantDomain);

        if(trustedIdP.getPublicCert() != null){
            IdentityProviderMgtUtil.importCertToStore(trustedIdP.getIdPName(), trustedIdP.getPublicCert(), tenantId, tenantDomain);
        }
    }

    private void doDeleteIdP(TrustedIdPDTO trustedIdP, int tenantId, String tenantDomain) throws IdentityProviderMgtException {

        TrustedIdPDO trustedIdPDO = new TrustedIdPDO();
        if(trustedIdP.getIdPName() == null || trustedIdP.getIdPName().equals("")){
            String msg = "Invalid arguments: Identity Provider Name value is empty";
            log.error(msg);
            throw new IdentityProviderMgtException(msg);
        }
        trustedIdPDO.setIdPName(trustedIdP.getIdPName());

        dao.deleteIdP(trustedIdPDO, tenantId, tenantDomain);

        IdentityProviderMgtUtil.deleteCertFromStore(trustedIdP.getIdPName(), tenantId, tenantDomain);
    }

}
