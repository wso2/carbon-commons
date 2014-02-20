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
package org.wso2.carbon.idp.mgt.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.idp.mgt.exception.IdentityProviderMgtException;
import org.wso2.carbon.idp.mgt.model.TrustedIdPDO;
import org.wso2.carbon.idp.mgt.util.IdentityProviderMgtConstants;
import org.wso2.carbon.idp.mgt.util.IdentityProviderMgtUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class IdPMgtDAO {

    private static final Log log = LogFactory.getLog(IdPMgtDAO.class);

    public List<TrustedIdPDO> getIdPs(Connection dbConnection, int tenantId, String tenantDomain) throws IdentityProviderMgtException {

        boolean dbConnInitialized = true;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        List<TrustedIdPDO> tenantIdPs = new ArrayList<TrustedIdPDO>();
        try {
            if(dbConnection == null){
                dbConnection = IdentityProviderMgtUtil.getDBConnection();
            } else {
                dbConnInitialized = false;
            }
            String sqlStmt = IdentityProviderMgtConstants.SQLQueries.GET_TENANT_IDPS_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);
            rs = prepStmt.executeQuery();
            while(rs.next()){
                TrustedIdPDO trustedIdPDO = new TrustedIdPDO();
                trustedIdPDO.setIdPName(rs.getString(1));
                trustedIdPDO.setHomeRealmId(rs.getString(2));
                tenantIdPs.add(trustedIdPDO);
            }
            return tenantIdPs;
        } catch (SQLException e){
            String msg = "Error occurred while retrieving registered IdP Issuers for tenant " + tenantDomain;
            log.error(msg, e);
            IdentityProviderMgtUtil.rollBack(dbConnection);
            throw new IdentityProviderMgtException(msg);
        } finally {
            if(dbConnInitialized){
                IdentityProviderMgtUtil.closeConnection(dbConnection);
            }
        }
    }

    public TrustedIdPDO getIdPByName(String idPName, int tenantId, String tenantDomain) throws IdentityProviderMgtException {
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        TrustedIdPDO trustedIdPDO = null;
        try {
            dbConnection = IdentityProviderMgtUtil.getDBConnection();
            String sqlStmt = IdentityProviderMgtConstants.SQLQueries.GET_IDP_BY_NAME_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, idPName);
            rs = prepStmt.executeQuery();
            if(rs.next()){
                trustedIdPDO = new TrustedIdPDO();
                int idPId = rs.getInt(1);
                trustedIdPDO.setIdPName(idPName);
                if(rs.getString(2).equals("1")){
                    trustedIdPDO.setPrimary(true);
                } else {
                    trustedIdPDO.setPrimary(false);
                }
                trustedIdPDO.setHomeRealmId(rs.getString(3));
                trustedIdPDO.setPublicCertThumbPrint(rs.getString(4));
                trustedIdPDO.setTokenEndpointAlias(rs.getString(5));
                if(rs.getString(6).equals("1")){
                    trustedIdPDO.setSAML2SSOEnabled(true);
                } else {
                    trustedIdPDO.setSAML2SSOEnabled(false);
                }
                trustedIdPDO.setIdpEntityId(rs.getString(7));
                trustedIdPDO.setSpEntityId(rs.getString(8));
                trustedIdPDO.setSSOUrl(rs.getString(9));
                if(rs.getString(10).equals("1")){
                    trustedIdPDO.setAuthnRequestSigned(true);
                } else {
                    trustedIdPDO.setAuthnRequestSigned(false);
                }
                if(rs.getString(11).equals("1")){
                    trustedIdPDO.setLogoutEnabled(true);
                } else {
                    trustedIdPDO.setLogoutEnabled(false);
                }
                trustedIdPDO.setLogoutRequestUrl(rs.getString(12));
                if(rs.getString(13).equals("1")){
                    trustedIdPDO.setLogoutRequestSigned(true);
                } else {
                    trustedIdPDO.setLogoutRequestSigned(false);
                }
                if(rs.getString(14).equals("1")){
                    trustedIdPDO.setAuthnResponseSigned(true);
                } else {
                    trustedIdPDO.setAuthnResponseSigned(false);
                }
                if(rs.getString(15).equals("1")){
                    trustedIdPDO.setOIDCEnabled(true);
                } else {
                    trustedIdPDO.setOIDCEnabled(false);
                }
                trustedIdPDO.setClientId(rs.getString(16));
                trustedIdPDO.setClientSecret(rs.getString(17));
                trustedIdPDO.setAuthzEndpointUrl(rs.getString(18));
                trustedIdPDO.setTokenEndpointUrl(rs.getString(19));

                Map<String, Integer> claimIdMap = new HashMap<String, Integer>();
                sqlStmt = IdentityProviderMgtConstants.SQLQueries.GET_TENANT_IDP_CLAIMS_SQL;
                prepStmt = dbConnection.prepareStatement(sqlStmt);
                prepStmt.setInt(1, idPId);
                rs = prepStmt.executeQuery();
                while(rs.next()){
                    int id = rs.getInt(1);
                    String claim = rs.getString(2);
                    claimIdMap.put(claim, id);
                }
                trustedIdPDO.setClaims(new ArrayList<String>(claimIdMap.keySet()));

                Map<String,String> claimMapping = new HashMap<String, String>();
                for(Map.Entry<String,Integer> claimId:claimIdMap.entrySet()){
                    String idpClaim = claimId.getKey();
                    int id = claimId.getValue();
                    sqlStmt = IdentityProviderMgtConstants.SQLQueries.GET_TENANT_IDP_CLAIM_MAPPINGS_SQL;
                    prepStmt = dbConnection.prepareStatement(sqlStmt);
                    prepStmt.setInt(1, id);
                    rs = prepStmt.executeQuery();
                    while(rs.next()){
                        String tenantClaim = rs.getString(1);
                        if(claimMapping.containsKey(idpClaim)){
                            claimMapping.put(idpClaim, claimMapping.get(idpClaim)+ "," +tenantClaim);
                        } else {
                            claimMapping.put(idpClaim, tenantClaim);
                        }
                    }
                }
                trustedIdPDO.setClaimMappings(claimMapping);

                Map<String, Integer> roleIdMap = new HashMap<String, Integer>();
                sqlStmt = IdentityProviderMgtConstants.SQLQueries.GET_TENANT_IDP_ROLES_SQL;
                prepStmt = dbConnection.prepareStatement(sqlStmt);
                prepStmt.setInt(1, idPId);
                rs = prepStmt.executeQuery();
                while(rs.next()){
                    int id = rs.getInt(1);
                    String role = rs.getString(2);
                    roleIdMap.put(role, id);
                }
                trustedIdPDO.setRoles(new ArrayList<String>(roleIdMap.keySet()));

                Map<String,String> roleMapping = new HashMap<String, String>();
                for(Map.Entry<String,Integer> roleId:roleIdMap.entrySet()){
                    String idPRole = roleId.getKey();
                    int id = roleId.getValue();
                    sqlStmt = IdentityProviderMgtConstants.SQLQueries.GET_TENANT_IDP_ROLE_MAPPINGS_SQL;
                    prepStmt = dbConnection.prepareStatement(sqlStmt);
                    prepStmt.setInt(1, id);
                    rs = prepStmt.executeQuery();
                    while(rs.next()){
                        String tenantRole = rs.getString(1);
                        if(roleMapping.containsKey(idPRole)){
                            roleMapping.put(idPRole, roleMapping.get(idPRole)+ "," +tenantRole);
                        } else {
                            roleMapping.put(idPRole, tenantRole);
                        }
                    }
                }
                trustedIdPDO.setRoleMappings(roleMapping);
            }
            return trustedIdPDO;
        } catch (SQLException e){
            String msg = "Error occurred while retrieving Identity Provider information for tenant : " + tenantDomain
                    + " and Identity Provider name : " + idPName;
            log.error(msg, e);
            IdentityProviderMgtUtil.rollBack(dbConnection);
            throw new IdentityProviderMgtException(msg);
        } finally {
            IdentityProviderMgtUtil.closeConnection(dbConnection);
        }
    }

    public TrustedIdPDO getIdPByRealmId(String realmId, int tenantId, String tenantDomain) throws IdentityProviderMgtException {
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        TrustedIdPDO trustedIdPDO = null;
        try {
            dbConnection = IdentityProviderMgtUtil.getDBConnection();
            String sqlStmt = IdentityProviderMgtConstants.SQLQueries.GET_IDP_BY_REALM_ID_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, realmId);
            rs = prepStmt.executeQuery();
            if(rs.next()){
                trustedIdPDO = new TrustedIdPDO();
                int idPId = rs.getInt(1);
                trustedIdPDO.setIdPName(rs.getString(2));
                if(rs.getString(3).equals("1")){
                    trustedIdPDO.setPrimary(true);
                } else {
                    trustedIdPDO.setPrimary(false);
                }
                trustedIdPDO.setHomeRealmId(realmId);
                trustedIdPDO.setPublicCertThumbPrint(rs.getString(4));
                trustedIdPDO.setTokenEndpointAlias(rs.getString(5));
                if(rs.getString(6).equals("1")){
                    trustedIdPDO.setSAML2SSOEnabled(true);
                } else {
                    trustedIdPDO.setSAML2SSOEnabled(false);
                }
                trustedIdPDO.setIdpEntityId(rs.getString(7));
                trustedIdPDO.setSpEntityId(rs.getString(8));
                trustedIdPDO.setSSOUrl(rs.getString(9));
                if(rs.getString(10).equals("1")){
                    trustedIdPDO.setAuthnRequestSigned(true);
                } else {
                    trustedIdPDO.setAuthnRequestSigned(false);
                }
                if(rs.getString(11).equals("1")){
                    trustedIdPDO.setLogoutEnabled(true);
                } else {
                    trustedIdPDO.setLogoutEnabled(false);
                }
                trustedIdPDO.setLogoutRequestUrl(rs.getString(12));
                if(rs.getString(13).equals("1")){
                    trustedIdPDO.setLogoutRequestSigned(true);
                } else {
                    trustedIdPDO.setLogoutRequestSigned(false);
                }
                if(rs.getString(14).equals("1")){
                    trustedIdPDO.setAuthnResponseSigned(true);
                } else {
                    trustedIdPDO.setAuthnResponseSigned(false);
                }
                if(rs.getString(15).equals("1")){
                    trustedIdPDO.setOIDCEnabled(true);
                } else {
                    trustedIdPDO.setOIDCEnabled(false);
                }
                trustedIdPDO.setClientId(rs.getString(16));
                trustedIdPDO.setClientSecret(rs.getString(17));
                trustedIdPDO.setAuthzEndpointUrl(rs.getString(18));
                trustedIdPDO.setTokenEndpointUrl(rs.getString(19));

                Map<String, Integer> claimIdMap = new HashMap<String, Integer>();
                sqlStmt = IdentityProviderMgtConstants.SQLQueries.GET_TENANT_IDP_CLAIMS_SQL;
                prepStmt = dbConnection.prepareStatement(sqlStmt);
                prepStmt.setInt(1, idPId);
                rs = prepStmt.executeQuery();
                while(rs.next()){
                    int id = rs.getInt(1);
                    String claim = rs.getString(2);
                    claimIdMap.put(claim, id);
                }
                trustedIdPDO.setClaims(new ArrayList<String>(claimIdMap.keySet()));

                Map<String,String> claimMapping = new HashMap<String, String>();
                for(Map.Entry<String,Integer> claimId:claimIdMap.entrySet()){
                    String idpClaim = claimId.getKey();
                    int id = claimId.getValue();
                    sqlStmt = IdentityProviderMgtConstants.SQLQueries.GET_TENANT_IDP_CLAIM_MAPPINGS_SQL;
                    prepStmt = dbConnection.prepareStatement(sqlStmt);
                    prepStmt.setInt(1, id);
                    rs = prepStmt.executeQuery();
                    while(rs.next()){
                        String tenantClaim = rs.getString(1);
                        if(claimMapping.containsKey(idpClaim)){
                            claimMapping.put(idpClaim, claimMapping.get(idpClaim)+ "," +tenantClaim);
                        } else {
                            claimMapping.put(idpClaim, tenantClaim);
                        }
                    }
                }
                trustedIdPDO.setClaimMappings(claimMapping);

                Map<String, Integer> roleIdMap = new HashMap<String, Integer>();
                sqlStmt = IdentityProviderMgtConstants.SQLQueries.GET_TENANT_IDP_ROLES_SQL;
                prepStmt = dbConnection.prepareStatement(sqlStmt);
                prepStmt.setInt(1, idPId);
                rs = prepStmt.executeQuery();
                while(rs.next()){
                    int id = rs.getInt(1);
                    String role = rs.getString(2);
                    roleIdMap.put(role, id);
                }
                trustedIdPDO.setRoles(new ArrayList<String>(roleIdMap.keySet()));

                Map<String,String> roleMapping = new HashMap<String, String>();
                for(Map.Entry<String,Integer> roleId:roleIdMap.entrySet()){
                    String idPRole = roleId.getKey();
                    int id = roleId.getValue();
                    sqlStmt = IdentityProviderMgtConstants.SQLQueries.GET_TENANT_IDP_ROLE_MAPPINGS_SQL;
                    prepStmt = dbConnection.prepareStatement(sqlStmt);
                    prepStmt.setInt(1, id);
                    rs = prepStmt.executeQuery();
                    while(rs.next()){
                        String tenantRole = rs.getString(1);
                        if(roleMapping.containsKey(idPRole)){
                            roleMapping.put(idPRole, roleMapping.get(idPRole)+ "," +tenantRole);
                        } else {
                            roleMapping.put(idPRole, tenantRole);
                        }
                    }
                }
                trustedIdPDO.setRoleMappings(roleMapping);
            }
            return trustedIdPDO;
        } catch (SQLException e){
            String msg = "Error occurred while retrieving Identity Provider information for tenant : " + tenantDomain +
                    " and Realm Id : " + realmId;
            log.error(msg, e);
            IdentityProviderMgtUtil.rollBack(dbConnection);
            throw new IdentityProviderMgtException(msg);
        } finally {
            IdentityProviderMgtUtil.closeConnection(dbConnection);
        }
    }

    public void addIdP(TrustedIdPDO trustedIdP, int tenantId, String tenantDomain) throws IdentityProviderMgtException {
        Connection dbConnection = null;
        try {
            dbConnection = IdentityProviderMgtUtil.getDBConnection();
            String idPName = trustedIdP.getIdPName();
            boolean isPrimary = trustedIdP.isPrimary();
            if(isPrimary){
                doSwitchPrimary(dbConnection, tenantId);
            }
            String homeRealmId = trustedIdP.getHomeRealmId();
            String thumbPrint = trustedIdP.getPublicCertThumbPrint();
            String tokenEndpointAlias = trustedIdP.getTokenEndpointAlias();
            List<String> claims = trustedIdP.getClaims();
            Map<String,String> claimMappings = trustedIdP.getClaimMappings();
            List<String> roles = trustedIdP.getRoles();
            Map<String,String> roleMappings = trustedIdP.getRoleMappings();
            boolean isSAML2SSOEnabled = trustedIdP.isSAML2SSOEnabled();
            String idpEntityId = trustedIdP.getIdpEntityId();
            String spEntityId = trustedIdP.getSpEntityId();
            String ssoUrl = trustedIdP.getSSOUrl();
            boolean isAuthnRequestSigned = trustedIdP.isAuthnRequestSigned();
            boolean isLogoutEnabled = trustedIdP.isLogoutEnabled();
            String logoutUrl = trustedIdP.getLogoutRequestUrl();
            boolean isLogoutRequestSigned = trustedIdP.isLogoutRequestSigned();
            boolean isAuthnResponseSigned = trustedIdP.isAuthnResponseSigned();
            boolean isOIDCEnabled = trustedIdP.isOIDCEnabled();
            String clientId = trustedIdP.getClientId();
            String clientSecret = trustedIdP.getClientSecret();
            String authzUrl = trustedIdP.getAuthzEndpointUrl();
            String tokenUrl = trustedIdP.getTokenEndpointUrl();
            doAddIdP(dbConnection, tenantId, idPName, isPrimary, homeRealmId, thumbPrint, tokenEndpointAlias,
                    isSAML2SSOEnabled, idpEntityId, spEntityId, ssoUrl, isAuthnRequestSigned, isLogoutEnabled, logoutUrl,
                    isLogoutRequestSigned, isAuthnResponseSigned, isOIDCEnabled, clientId, clientSecret, authzUrl,
                    tokenUrl);
            int idPId = isTenantIdPExisting(dbConnection, trustedIdP, tenantId, tenantDomain);
            if(idPId <= 0){
                String msg = "Error adding Identity Provider for tenant " + tenantDomain;
                log.error(msg);
                throw new IdentityProviderMgtException(msg);
            }
            if(claims != null && claims.size() > 0){
                doAddIdPClaims(dbConnection, idPId, claims);
            }
            if(claimMappings != null && claimMappings.size() > 0){
                doAddIdPClaimMappings(dbConnection, idPId, tenantId, claimMappings, tenantDomain);
            }
            if(roles != null && roles.size() > 0){
                doAddIdPRoles(dbConnection, idPId, roles);
            }
            if(roleMappings != null && roleMappings.size() > 0){
                doAddIdPRoleMappings(dbConnection, idPId, tenantId, roleMappings, tenantDomain);
            }
            dbConnection.commit();
        } catch (SQLException e){
            String msg = "Error occurred while adding Identity Provider for tenant";
            IdentityProviderMgtUtil.rollBack(dbConnection);
            log.error(msg + " " + tenantDomain, e);
            throw new IdentityProviderMgtException(msg);
        } finally {
            IdentityProviderMgtUtil.closeConnection(dbConnection);
        }
    }

    public void updateIdP(TrustedIdPDO trustedIdPDO1, TrustedIdPDO trustedIdPDO2, int tenantId, String tenantDomain)
            throws IdentityProviderMgtException {

        Connection dbConnection = null;

        String idPName1 = trustedIdPDO1.getIdPName();
        boolean isPrimary1 = false;
        if(trustedIdPDO1.isPrimary()){
            isPrimary1 = true;
        }
        String homeRealmId1 = trustedIdPDO1.getHomeRealmId();
        String thumbPrint1 = trustedIdPDO1.getPublicCertThumbPrint();
        String tokenEndpointAlias1 = trustedIdPDO1.getTokenEndpointAlias();
        List<String> claims1 = trustedIdPDO1.getClaims();
        Map<String,String> claimMappings1 = trustedIdPDO1.getClaimMappings();
        List<String> roles1 = trustedIdPDO1.getRoles();
        Map<String,String> roleMappings1 = trustedIdPDO1.getRoleMappings();
        boolean isSAML2SSOEnabled1 = false;
        if(trustedIdPDO1.isSAML2SSOEnabled()){
            isSAML2SSOEnabled1 = true;
        }
        String idpEntityId1 = trustedIdPDO1.getIdpEntityId();
        String spEntityId1 = trustedIdPDO1.getSpEntityId();
        String ssoUrl1 = trustedIdPDO1.getSSOUrl();
        boolean isAuthnRequestSigned1 = false;
        if(trustedIdPDO1.isAuthnRequestSigned()){
            isAuthnRequestSigned1 = true;
        }
        boolean isLogoutEnabled1 = false;
        if(trustedIdPDO1.isLogoutEnabled()){
            isLogoutEnabled1 = true;
        }
        String logoutRequestUrl1 = trustedIdPDO1.getLogoutRequestUrl();
        boolean isLogoutRequestSigned1 = false;
        if(trustedIdPDO1.isLogoutRequestSigned()){
            isLogoutRequestSigned1 = true;
        }
        boolean isAuthnResponseSigned1 = false;
        if(trustedIdPDO1.isAuthnResponseSigned()){
            isAuthnResponseSigned1 = true;
        }
        boolean isOIDCEnabled1 = false;
        if(trustedIdPDO1.isOIDCEnabled()){
            isOIDCEnabled1 = true;
        }
        String clientId1 = trustedIdPDO1.getClientId();
        String clientSecret1 = trustedIdPDO1.getClientSecret();
        String authzUrl1 = trustedIdPDO1.getAuthzEndpointUrl();
        String tokenUrl1 = trustedIdPDO1.getTokenEndpointUrl();

        String idPName2 = trustedIdPDO2.getIdPName();
        boolean isPrimary2 = false;
        if(trustedIdPDO2.isPrimary()){
            isPrimary2 = true;
        }
        String homeRealmId2 = trustedIdPDO2.getHomeRealmId();
        String thumbPrint2 = trustedIdPDO2.getPublicCertThumbPrint();
        String tokenEndpointAlias2 = trustedIdPDO2.getTokenEndpointAlias();
        List<String> claims2 = trustedIdPDO2.getClaims();
        Map<String,String> claimMappings2 = trustedIdPDO2.getClaimMappings();
        List<String> roles2 = trustedIdPDO2.getRoles();
        Map<String,String> roleMappings2 = trustedIdPDO2.getRoleMappings();
        boolean isSAML2SSOEnabled2 = false;
        if(trustedIdPDO2.isSAML2SSOEnabled()){
            isSAML2SSOEnabled2 = true;
        }
        String idpEntityId2 = trustedIdPDO2.getIdpEntityId();
        String spEntityId2 = trustedIdPDO2.getSpEntityId();
        String ssoUrl2 = trustedIdPDO2.getSSOUrl();
        boolean isAuthnRequestSigned2 = false;
        if(trustedIdPDO2.isAuthnRequestSigned()){
            isAuthnRequestSigned2 = true;
        }
        boolean isLogoutEnabled2 = false;
        if(trustedIdPDO2.isLogoutEnabled()){
            isLogoutEnabled2 = true;
        }
        String logoutRequestUrl2 = trustedIdPDO2.getLogoutRequestUrl();
        boolean isLogoutRequestSigned2 = false;
        if(trustedIdPDO2.isLogoutRequestSigned()){
            isLogoutRequestSigned2 = true;
        }
        boolean isAuthnResponseSigned2 = false;
        if(trustedIdPDO2.isAuthnResponseSigned()){
            isAuthnResponseSigned2 = true;
        }
        boolean isOIDCEnabled2 = false;
        if(trustedIdPDO2.isOIDCEnabled()){
            isOIDCEnabled2 = true;
        }
        String clientId2 = trustedIdPDO2.getClientId();
        String clientSecret2 = trustedIdPDO2.getClientSecret();
        String authzUrl2 = trustedIdPDO2.getAuthzEndpointUrl();
        String tokenUrl2 = trustedIdPDO2.getTokenEndpointUrl();

        if(claims2.size() < claims1.size()){
            String msg = "Input error: new set of claim URIs cannot be smaller than old set of claim URIs. " + claims2.size() +
                    " < " + claims1.size();
            log.error(msg);
            throw new IdentityProviderMgtException(msg);
        }

        if(roles2.size() < roles1.size()){
            String msg = "Input error: new set of roles cannot be smaller than old set of roles. " + roles2.size() +
                    " < " + roles1.size();
            log.error(msg);
            throw new IdentityProviderMgtException(msg);
        }

        Map<String,String> addedClaimMappings = new HashMap<String, String>();
        Map<String,String> deletedClaimMappings = new HashMap<String, String>();
        for(Map.Entry<String,String> entry:claimMappings1.entrySet()){
            if(!IdentityProviderMgtUtil.containsEntry(claimMappings2, entry)){
                deletedClaimMappings.put(entry.getKey(), entry.getValue());
            }
        }
        for(Map.Entry<String,String> entry:claimMappings2.entrySet()){
            if(!IdentityProviderMgtUtil.containsEntry(claimMappings1, entry)){
                addedClaimMappings.put(entry.getKey(), entry.getValue());
            }
        }
        List<String> renamedOldClaims = new ArrayList<String>();
        List<String> renamedNewClaims = new ArrayList<String>();
        List<String> addedClaims = new ArrayList<String>();
        List<String> deletedClaims = new ArrayList<String>();
        for(int i = 0; i < claims1.size(); i++){
            if(claims2.get(i) == null){
                deletedClaims.add(claims1.get(i));
            }
            if(claims2.get(i) != null && !claims2.get(i).equals(claims1.get(i))){
                renamedOldClaims.add(claims1.get(i));
                renamedNewClaims.add(claims2.get(i));
            }
        }
        for(int i = claims1.size(); i < claims2.size(); i++){
            addedClaims.add(claims2.get(i));
        }

        Map<String,String> addedRoleMappings = new HashMap<String, String>();
        Map<String,String> deletedRoleMappings = new HashMap<String, String>();
        for(Map.Entry<String,String> entry:roleMappings1.entrySet()){
            if(!IdentityProviderMgtUtil.containsEntry(roleMappings2, entry)){
                deletedRoleMappings.put(entry.getKey(), entry.getValue());
            }
        }
        for(Map.Entry<String,String> entry:roleMappings2.entrySet()){
            if(!IdentityProviderMgtUtil.containsEntry(roleMappings1, entry)){
                addedRoleMappings.put(entry.getKey(), entry.getValue());
            }
        }
        List<String> renamedOldRoles = new ArrayList<String>();
        List<String> renamedNewRoles = new ArrayList<String>();
        List<String> addedRoles = new ArrayList<String>();
        List<String> deletedRoles = new ArrayList<String>();
        for(int i = 0; i < roles1.size(); i++){
            if(roles2.get(i) == null){
                deletedRoles.add(roles1.get(i));
            }
            if(roles2.get(i) != null && !roles2.get(i).equals(roles1.get(i))){
                renamedOldRoles.add(roles1.get(i));
                renamedNewRoles.add(roles2.get(i));
            }
        }
        for(int i = roles1.size(); i < roles2.size(); i++){
            addedRoles.add(roles2.get(i));
        }

        try {
            dbConnection = IdentityProviderMgtUtil.getDBConnection();
            int idPId = isTenantIdPExisting(dbConnection, trustedIdPDO1, tenantId, tenantDomain);
            if(idPId <= 0){
                String msg = "Trying to update non-existent Identity Provider for tenant " + tenantDomain;
                log.error(msg);
                throw new IdentityProviderMgtException(msg);
            }
            if(idPName1 != idPName2 || isPrimary1 != isPrimary2 ||
                    (homeRealmId1 != null && homeRealmId2 != null && !homeRealmId1.equals(homeRealmId2) ||
                            homeRealmId1 != null && homeRealmId2 == null ||
                            homeRealmId1 == null && homeRealmId2 != null) ||
                    (thumbPrint1 != null && thumbPrint2 != null && !thumbPrint1.equals(thumbPrint2) ||
                            thumbPrint1 != null && thumbPrint2 == null ||
                            thumbPrint1 == null && thumbPrint2 != null) ||
                    (tokenEndpointAlias1 != null && tokenEndpointAlias2 != null &&
                            !tokenEndpointAlias1.equals(tokenEndpointAlias2) ||
                            tokenEndpointAlias1 != null && tokenEndpointAlias2 == null ||
                            tokenEndpointAlias1 == null && tokenEndpointAlias2 != null) ||
                    isSAML2SSOEnabled1 != isSAML2SSOEnabled2 ||
                    (idpEntityId1 != null && idpEntityId2 != null && !idpEntityId1.equals(idpEntityId2) ||
                            idpEntityId1!= null && idpEntityId2 == null ||
                            idpEntityId1 == null && idpEntityId2 != null) ||
                    (spEntityId1 != null && spEntityId2 != null && !spEntityId1.equals(spEntityId2) ||
                            spEntityId1!= null && spEntityId2 == null ||
                            spEntityId1 == null && spEntityId2 != null) ||
                    (ssoUrl1 != null && idpEntityId2 != null && !idpEntityId1.equals(idpEntityId2) ||
                            ssoUrl1!= null && ssoUrl2 == null ||
                            ssoUrl1 == null && ssoUrl2 != null) ||
                    isAuthnRequestSigned1 != isAuthnRequestSigned2 ||
                    isLogoutEnabled1 != isLogoutEnabled2||
                    (logoutRequestUrl1 != null && logoutRequestUrl2 != null && !logoutRequestUrl1.equals(logoutRequestUrl2) ||
                            logoutRequestUrl1 != null && logoutRequestUrl2 == null ||
                            logoutRequestUrl1 == null && logoutRequestUrl2 != null) ||
                    isLogoutRequestSigned1 != isLogoutRequestSigned2 ||
                    isAuthnResponseSigned1 != isAuthnResponseSigned2 ||
                    isOIDCEnabled1 != isOIDCEnabled2 ||
                    (clientId1 != null && clientId2 != null && !clientId1.equals(clientId2) ||
                            clientId1!= null && clientId2 == null ||
                            clientId1 == null && clientId2 != null) ||
                    (clientSecret1 != null && clientSecret2 != null && !clientSecret1.equals(clientSecret2) ||
                            clientSecret1!= null && clientSecret2 == null ||
                            clientSecret1 == null && clientSecret2 != null) ||
                    (authzUrl1 != null && authzUrl2 != null && !authzUrl1.equals(authzUrl2) ||
                            authzUrl1!= null && authzUrl2 == null ||
                            authzUrl1 == null && authzUrl2 != null) ||
                    (tokenUrl1 != null && tokenUrl2 != null && !tokenUrl1.equals(tokenUrl2) ||
                            tokenUrl1!= null && tokenUrl2 == null ||
                            tokenUrl1 == null && tokenUrl2 != null)) {
                if(isPrimary1 != isPrimary2){
                    doSwitchPrimary(dbConnection, tenantId);
                }
                doUpdateIdP(dbConnection, tenantId, idPName1, idPName2, isPrimary2, homeRealmId2, thumbPrint2,
                        tokenEndpointAlias2, isSAML2SSOEnabled2, idpEntityId2, spEntityId2, ssoUrl2,
                        isAuthnRequestSigned2, isLogoutEnabled2, logoutRequestUrl2, isLogoutRequestSigned2,
                        isAuthnResponseSigned2, isOIDCEnabled2, clientId2, clientSecret2, authzUrl2, tokenUrl2);
            }
            if(!addedClaims.isEmpty() || !deletedClaims.isEmpty() || !renamedOldClaims.isEmpty()){
                doUpdateIdPClaims(dbConnection, idPId, addedClaims, deletedClaims, renamedOldClaims, renamedNewClaims);
            }
            if(!addedClaimMappings.isEmpty() || !deletedClaimMappings.isEmpty()){
                doUpdateClaimMappings(dbConnection, idPId, tenantId, renamedOldClaims, renamedNewClaims, addedClaimMappings,
                        deletedClaimMappings, tenantDomain);
            }
            if(!addedRoles.isEmpty() || !deletedRoles.isEmpty() || !renamedOldRoles.isEmpty()){
                doUpdateIdPRoles(dbConnection, idPId, addedRoles, deletedRoles, renamedOldRoles, renamedNewRoles);
            }
            if(!addedRoleMappings.isEmpty() || !deletedRoleMappings.isEmpty()){
                doUpdateRoleMappings(dbConnection, idPId, tenantId, renamedOldRoles, renamedNewRoles, addedRoleMappings,
                        deletedRoleMappings, tenantDomain);
            }
            dbConnection.commit();
        } catch(SQLException e){
            String msg = "Error occurred while updating Identity Provider information  for tenant " + tenantDomain;
            log.error(msg, e);
            IdentityProviderMgtUtil.rollBack(dbConnection);
            throw new IdentityProviderMgtException(msg);
        } finally {
            IdentityProviderMgtUtil.closeConnection(dbConnection);
        }
    }

    public void deleteIdP(TrustedIdPDO trustedIdP, int tenantId, String tenantDomain) throws IdentityProviderMgtException {

        Connection dbConnection = null;
        try {
            dbConnection = IdentityProviderMgtUtil.getDBConnection();
            String idPName = trustedIdP.getIdPName();
            int idPId = isTenantIdPExisting(dbConnection, trustedIdP, tenantId, tenantDomain);
            if(idPId <= 0){
                String msg = "Trying to delete non-existent Identity Provider for tenant " + tenantDomain;
                log.error(msg);
                return;
            }

            trustedIdP.setPrimary(true);
            int primaryIdPId = isPrimaryIdPExisting(dbConnection, trustedIdP, tenantId, tenantDomain);
            if(primaryIdPId <= 0){
                String msg = "Cannot find primary IdP for tenant " + tenantDomain;
                log.warn(msg);
            }

            doDeleteIdP(dbConnection, tenantId, idPName);

            if(idPId == primaryIdPId){
                doAppointPrimary(dbConnection, tenantId, tenantDomain);
            }

            dbConnection.commit();
        } catch (SQLException e){
            String msg = "Error occurred while deleting Identity Provider of tenant " + tenantDomain;
            log.error(msg, e);
            IdentityProviderMgtUtil.rollBack(dbConnection);
            throw new IdentityProviderMgtException(msg);
        } finally {
            IdentityProviderMgtUtil.closeConnection(dbConnection);
        }
    }

    private void doSwitchPrimary(Connection conn, int tenantId) throws SQLException {
        PreparedStatement prepStmt = null;
        String sqlStmt = IdentityProviderMgtConstants.SQLQueries.SWITCH_TENANT_IDP_PRIMARY_SQL;
        prepStmt = conn.prepareStatement(sqlStmt);
        prepStmt.setString(1, "0");
        prepStmt.setInt(2, tenantId);
        prepStmt.setString(3, "1");
        prepStmt.executeUpdate();
    }

    private void doAppointPrimary(Connection conn, int tenantId, String tenantDomain)
            throws SQLException, IdentityProviderMgtException {
        List<TrustedIdPDO> tenantIdPs = getIdPs(conn, tenantId, tenantDomain);
        if(!tenantIdPs.isEmpty()){
            PreparedStatement prepStmt = null;
            String sqlStmt = IdentityProviderMgtConstants.SQLQueries.SWITCH_TENANT_IDP_PRIMARY_ON_DELETE_SQL;
            prepStmt = conn.prepareStatement(sqlStmt);
            prepStmt.setString(1, "1");
            prepStmt.setInt(2, tenantId);
            prepStmt.setString(3, tenantIdPs.get(0).getIdPName());
            prepStmt.setString(4, "0");
            prepStmt.executeUpdate();
        } else {
            String msg = "No Identity Providers registered for tenant " + tenantDomain;
            log.warn(msg);
        }
    }

    private void doAddIdP(Connection conn, int tenantId, String idPName, boolean isPrimary, String homeRealmId,
                          String thumbPrint, String tokenEndpointAlias, boolean isSAML2SSOEnabled,
                          String idpEntityId, String spEntityId, String ssoUrl, boolean isAuthnRequestSigned,
                          boolean isLogoutEnabled, String logoutRequestUrl, boolean isLogoutRequestSigned,
                          boolean isAuthnResponseSigned, boolean isOIDCEnabled, String clientId, String clientSecret,
                          String authzUrl, String tokenUrl) throws SQLException {
        PreparedStatement prepStmt = null;
        String sqlStmt = IdentityProviderMgtConstants.SQLQueries.ADD_TENANT_IDP_SQL;
        prepStmt = conn.prepareStatement(sqlStmt);
        prepStmt.setInt(1, tenantId);
        prepStmt.setString(2, idPName);
        if(isPrimary){
            prepStmt.setString(3, "1");
        } else {
            prepStmt.setString(3, "0");
        }
        prepStmt.setString(4, homeRealmId);
        prepStmt.setString(5, thumbPrint);
        prepStmt.setString(6, tokenEndpointAlias);
        if(isSAML2SSOEnabled){
            prepStmt.setString(7, "1");
        } else {
            prepStmt.setString(7, "0");
        }
        prepStmt.setString(8, idpEntityId);
        prepStmt.setString(9, spEntityId);
        prepStmt.setString(10, ssoUrl);
        if(isAuthnRequestSigned){
            prepStmt.setString(11, "1");
        } else {
            prepStmt.setString(11, "0");
        }
        if(isLogoutEnabled){
            prepStmt.setString(12, "1");
        } else {
            prepStmt.setString(12, "0");
        }
        prepStmt.setString(13, logoutRequestUrl);
        if(isLogoutRequestSigned){
            prepStmt.setString(14, "1");
        } else {
            prepStmt.setString(14, "0");
        }
        if(isAuthnResponseSigned){
            prepStmt.setString(15, "1");
        } else {
            prepStmt.setString(15, "0");
        }
        if(isOIDCEnabled){
            prepStmt.setString(16, "1");
        } else {
            prepStmt.setString(16, "0");
        }
        prepStmt.setString(17, clientId);
        prepStmt.setString(18, clientSecret);
        prepStmt.setString(19, authzUrl);
        prepStmt.setString(20, tokenUrl);
        prepStmt.executeUpdate();
    }

    private void doAddIdPClaims(Connection conn, int idPId, List<String> claims) throws SQLException {
        PreparedStatement prepStmt = null;
        String sqlStmt = IdentityProviderMgtConstants.SQLQueries.ADD_TENANT_IDP_CLAIMS_SQL;
        for(String claim:claims){
            prepStmt = conn.prepareStatement(sqlStmt);
            prepStmt.setInt(1, idPId);
            prepStmt.setString(2, claim);
            prepStmt.executeUpdate();
        }
    }

    private void doAddIdPClaimMappings(Connection conn, int idPId, int tenantId, Map<String,String> claimMappings,
                                       String tenantDomain) throws SQLException, IdentityProviderMgtException {
        Map<String, Integer> claimIdMap = new HashMap<String, Integer>();
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String sqlStmt = IdentityProviderMgtConstants.SQLQueries.GET_TENANT_IDP_CLAIMS_SQL;
        prepStmt = conn.prepareStatement(sqlStmt);
        prepStmt.setInt(1, idPId);
        rs = prepStmt.executeQuery();
        while(rs.next()){
            int id = rs.getInt(1);
            String claim = rs.getString(2);
            claimIdMap.put(claim, id);
        }
        if(claimIdMap.isEmpty()){
            String message = "No Identity Provider claim URIs defined for tenant " + tenantDomain;
            log.error(message);
            throw new IdentityProviderMgtException(message);
        }
        for(Map.Entry<String,String> entry : claimMappings.entrySet()){
            if(claimIdMap.containsKey(entry.getKey())){
                int idpClaimId = claimIdMap.get(entry.getKey());
                String localClaim = entry.getValue();
                sqlStmt = IdentityProviderMgtConstants.SQLQueries.ADD_TENANT_IDP_CLAIM_MAPPINGS_SQL;
                prepStmt = conn.prepareStatement(sqlStmt);
                prepStmt.setInt(1, idpClaimId);
                prepStmt.setInt(2, tenantId);
                prepStmt.setString(3, localClaim);
                prepStmt.executeUpdate();
            } else {
                String msg = "Cannot find Identity Provider claim URI " + entry.getKey() + " for tenant " + tenantDomain;
                log.error(msg);
                throw new IdentityProviderMgtException(msg);
            }
        }
    }

    private void doAddIdPRoles(Connection conn, int idPId, List<String> roles) throws SQLException {
        PreparedStatement prepStmt = null;
        String sqlStmt = IdentityProviderMgtConstants.SQLQueries.ADD_TENANT_IDP_ROLES_SQL;
        for(String role:roles){
            prepStmt = conn.prepareStatement(sqlStmt);
            prepStmt.setInt(1, idPId);
            prepStmt.setString(2, role);
            prepStmt.executeUpdate();
        }
    }

    private void doAddIdPRoleMappings(Connection conn, int idPId, int tenantId, Map<String,String> roleMappings, String tenantDomain)
            throws SQLException, IdentityProviderMgtException {
        Map<String, Integer> roleIdMap = new HashMap<String, Integer>();
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String sqlStmt = IdentityProviderMgtConstants.SQLQueries.GET_TENANT_IDP_ROLES_SQL;
        prepStmt = conn.prepareStatement(sqlStmt);
        prepStmt.setInt(1, idPId);
        rs = prepStmt.executeQuery();
        while(rs.next()){
            int id = rs.getInt(1);
            String role = rs.getString(2);
            roleIdMap.put(role, id);
        }
        if(roleIdMap.isEmpty()){
            String message = "No Identity Provider roles defined for tenant " + tenantDomain;
            log.error(message);
            throw new IdentityProviderMgtException(message);
        }
        for(Map.Entry<String,String> entry : roleMappings.entrySet()){
            if(roleIdMap.containsKey(entry.getKey())){
                int idpRoleId = roleIdMap.get(entry.getKey());
                String localRole = entry.getValue();
                sqlStmt = IdentityProviderMgtConstants.SQLQueries.ADD_TENANT_IDP_ROLE_MAPPINGS_SQL;
                prepStmt = conn.prepareStatement(sqlStmt);
                prepStmt.setInt(1, idpRoleId);
                prepStmt.setInt(2, tenantId);
                prepStmt.setString(3, localRole);
                prepStmt.executeUpdate();
            } else {
                String msg = "Cannot find Identity Provider role " + entry.getKey() + " for tenant " + tenantDomain;
                log.error(msg);
                throw new IdentityProviderMgtException(msg);
            }
        }
    }
    
    private void doUpdateIdP(Connection conn, int tenantId, String oldIdpName, String newIdPName, boolean isPrimary,
                             String homeRealmId, String thumbPrint, String tokenEndpointAlias,
                             boolean isSAML2SSOEnabled, String idpEntityId, String spEntityId, String ssoUrl,
                             boolean isAuthnRequestSigned, boolean isLogoutEnabled, String logoutRequestUrl,
                             boolean isLogoutRequestSigned, boolean isAuthnResponseSigned, boolean isOIDCEnabled,
                             String clientId, String clientSecret, String authzUrl, String tokenUrl) throws SQLException {

        PreparedStatement prepStmt = null;
        String sqlStmt = IdentityProviderMgtConstants.SQLQueries.UPDATE_TENANT_IDP_SQL;
        prepStmt = conn.prepareStatement(sqlStmt);
        prepStmt.setString(1, newIdPName);
        if(isPrimary){
            prepStmt.setString(2, "1");
        } else {
            prepStmt.setString(2, "0");
        }
        prepStmt.setString(3, homeRealmId);
        prepStmt.setString(4, thumbPrint);
        prepStmt.setString(5, tokenEndpointAlias);
        if(isSAML2SSOEnabled){
            prepStmt.setString(6, "1");
        } else {
            prepStmt.setString(6, "0");
        }
        prepStmt.setString(7, idpEntityId);
        prepStmt.setString(8, spEntityId);
        prepStmt.setString(9, ssoUrl);
        if(isAuthnRequestSigned){
            prepStmt.setString(10, "1");
        } else {
            prepStmt.setString(10, "0");
        }
        if(isLogoutEnabled){
            prepStmt.setString(11, "1");
        } else {
            prepStmt.setString(11, "0");
        }
        prepStmt.setString(12, logoutRequestUrl);
        if(isLogoutRequestSigned){
            prepStmt.setString(13, "1");
        } else {
            prepStmt.setString(13, "0");
        }
        if(isAuthnResponseSigned){
            prepStmt.setString(14, "1");
        } else {
            prepStmt.setString(14, "0");
        }
        if(isOIDCEnabled){
            prepStmt.setString(15, "1");
        } else {
            prepStmt.setString(15, "0");
        }
        prepStmt.setString(16, clientId);
        prepStmt.setString(17, clientSecret);
        prepStmt.setString(18, authzUrl);
        prepStmt.setString(19, tokenUrl);

        prepStmt.setInt(20, tenantId);
        prepStmt.setString(21, oldIdpName);
        prepStmt.executeUpdate();
    }

    private void doUpdateIdPClaims(Connection conn, int idPId, List<String> addedClaims, List<String> deletedClaims,
                                  List<String> renamedOldClaims, List<String> renamedNewClaims)
            throws SQLException {
        PreparedStatement prepStmt = null;
        String sqlStmt = null;
        for(String deletedClaim:deletedClaims){
            sqlStmt = IdentityProviderMgtConstants.SQLQueries.DELETE_TENANT_IDP_CLAIMS_SQL;
            prepStmt = conn.prepareStatement(sqlStmt);
            prepStmt.setInt(1, idPId);
            prepStmt.setString(2, deletedClaim);
            prepStmt.executeUpdate();
        }
        for(String addedClaim:addedClaims){
            sqlStmt = IdentityProviderMgtConstants.SQLQueries.ADD_TENANT_IDP_CLAIMS_SQL;
            prepStmt = conn.prepareStatement(sqlStmt);
            prepStmt.setInt(1, idPId);
            prepStmt.setString(2, addedClaim);
            prepStmt.executeUpdate();
        }
        for(int i = 0; i < renamedOldClaims.size(); i++){
            sqlStmt = IdentityProviderMgtConstants.SQLQueries.UPDATE_TENANT_IDP_CLAIMS_SQL;
            prepStmt = conn.prepareStatement(sqlStmt);
            prepStmt.setString(1, renamedNewClaims.get(i));
            prepStmt.setInt(2, idPId);
            prepStmt.setString(3, renamedOldClaims.get(i));
            prepStmt.executeUpdate();
        }
    }

    private void doUpdateClaimMappings(Connection conn, int idPId, int tenantId, List<String> renamedOldClaims,
                                      List<String> renamedNewClaims, Map<String,String> addedClaimMappings,
                                      Map<String,String> deletedClaimMappings, String tenantDomain)
            throws SQLException, IdentityProviderMgtException {
        Map<String, Integer> claimIdMap = new HashMap<String, Integer>();
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String sqlStmt = IdentityProviderMgtConstants.SQLQueries.GET_TENANT_IDP_CLAIMS_SQL;
        prepStmt = conn.prepareStatement(sqlStmt);
        prepStmt.setInt(1, idPId);
        rs = prepStmt.executeQuery();
        while(rs.next()){
            int id = rs.getInt(1);
            String claim = rs.getString(2);
            claimIdMap.put(claim, id);
        }
        if(claimIdMap.isEmpty()){
            String msg = "No Identity Provider claim URIs defined for tenant " + tenantDomain;
            log.error(msg);
            throw new IdentityProviderMgtException(msg);
        }
        if(!deletedClaimMappings.isEmpty()){
            Map<String,String> temp = new HashMap<String, String>();
            for(Map.Entry<String,String> entry : deletedClaimMappings.entrySet()){
                if(renamedOldClaims.contains(entry.getKey())){
                    int index = renamedOldClaims.indexOf(entry.getKey());
                    String value = renamedNewClaims.get(index);
                    temp.put(value, entry.getValue());
                } else {
                    temp.put(entry.getKey(), entry.getValue());
                }
            }
            deletedClaimMappings = temp;
            for(Map.Entry<String,String> entry : deletedClaimMappings.entrySet()){
                if(claimIdMap.containsKey(entry.getKey())){
                    int idpClaimId = claimIdMap.get(entry.getKey());
                    String localClaim = entry.getValue();
                    sqlStmt = IdentityProviderMgtConstants.SQLQueries.DELETE_TENANT_IDP_CLAIM_MAPPINGS_SQL;
                    prepStmt = conn.prepareStatement(sqlStmt);
                    prepStmt.setInt(1, idpClaimId);
                    prepStmt.setInt(2, tenantId);
                    prepStmt.setString(3, localClaim);
                    prepStmt.executeUpdate();
                } else {
                    String msg = "Cannot find Identity Provider claim URI " + entry.getKey() + " for tenant " + tenantDomain;
                    log.error(msg);
                    throw new IdentityProviderMgtException(msg);
                }
            }
        }
        if(!addedClaimMappings.isEmpty()){
            for(Map.Entry<String,String> entry : addedClaimMappings.entrySet()){
                if(claimIdMap.containsKey(entry.getKey())){
                    int idpClaimId = claimIdMap.get(entry.getKey());
                    String localClaim = entry.getValue();
                    sqlStmt = IdentityProviderMgtConstants.SQLQueries.ADD_TENANT_IDP_CLAIM_MAPPINGS_SQL;
                    prepStmt = conn.prepareStatement(sqlStmt);
                    prepStmt.setInt(1, idpClaimId);
                    prepStmt.setInt(2, tenantId);
                    prepStmt.setString(3, localClaim);
                    prepStmt.executeUpdate();
                } else {
                    String msg = "Cannot find Identity Provider claim URI " + entry.getKey() + " for tenant " + tenantDomain;
                    log.error(msg);
                    throw new IdentityProviderMgtException(msg);
                }
            }
        }
    }

    private void doUpdateIdPRoles(Connection conn, int idPId, List<String> addedRoles, List<String> deletedRoles,
                                  List<String> renamedOldRoles, List<String> renamedNewRoles)
            throws SQLException {
        PreparedStatement prepStmt = null;
        String sqlStmt = null;
        for(String deletedRole:deletedRoles){
            sqlStmt = IdentityProviderMgtConstants.SQLQueries.DELETE_TENANT_IDP_ROLES_SQL;
            prepStmt = conn.prepareStatement(sqlStmt);
            prepStmt.setInt(1, idPId);
            prepStmt.setString(2, deletedRole);
            prepStmt.executeUpdate();
        }
        for(String addedRole:addedRoles){
            sqlStmt = IdentityProviderMgtConstants.SQLQueries.ADD_TENANT_IDP_ROLES_SQL;
            prepStmt = conn.prepareStatement(sqlStmt);
            prepStmt.setInt(1, idPId);
            prepStmt.setString(2, addedRole);
            prepStmt.executeUpdate();
        }
        for(int i = 0; i < renamedOldRoles.size(); i++){
            sqlStmt = IdentityProviderMgtConstants.SQLQueries.UPDATE_TENANT_IDP_ROLES_SQL;
            prepStmt = conn.prepareStatement(sqlStmt);
            prepStmt.setString(1, renamedNewRoles.get(i));
            prepStmt.setInt(2, idPId);
            prepStmt.setString(3, renamedOldRoles.get(i));
            prepStmt.executeUpdate();
        }
    }

    private void doUpdateRoleMappings(Connection conn, int idPId, int tenantId, List<String> renamedOldRoles,
                                      List<String> renamedNewRoles, Map<String,String> addedRoleMappings,
                                      Map<String,String> deletedRoleMappings, String tenantDomain)
            throws SQLException, IdentityProviderMgtException {
        Map<String, Integer> roleIdMap = new HashMap<String, Integer>();
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String sqlStmt = IdentityProviderMgtConstants.SQLQueries.GET_TENANT_IDP_ROLES_SQL;
        prepStmt = conn.prepareStatement(sqlStmt);
        prepStmt.setInt(1, idPId);
        rs = prepStmt.executeQuery();
        while(rs.next()){
            int id = rs.getInt(1);
            String role = rs.getString(2);
            roleIdMap.put(role, id);
        }
        if(roleIdMap.isEmpty()){
            String msg = "No Identity Provider roles defined for tenant " + tenantDomain;
            log.error(msg);
            throw new IdentityProviderMgtException(msg);
        }
        if(!deletedRoleMappings.isEmpty()){
            Map<String,String> temp = new HashMap<String, String>();
            for(Map.Entry<String,String> entry : deletedRoleMappings.entrySet()){
                if(renamedOldRoles.contains(entry.getKey())){
                    int index = renamedOldRoles.indexOf(entry.getKey());
                    String value = renamedNewRoles.get(index);
                    temp.put(value, entry.getValue());
                } else {
                    temp.put(entry.getKey(), entry.getValue());
                }
            }
            deletedRoleMappings = temp;
            for(Map.Entry<String,String> entry : deletedRoleMappings.entrySet()){
                if(roleIdMap.containsKey(entry.getKey())){
                    int idpRoleId = roleIdMap.get(entry.getKey());
                    String localRole = entry.getValue();
                    sqlStmt = IdentityProviderMgtConstants.SQLQueries.DELETE_TENANT_IDP_ROLE_MAPPINGS_SQL;
                    prepStmt = conn.prepareStatement(sqlStmt);
                    prepStmt.setInt(1, idpRoleId);
                    prepStmt.setInt(2, tenantId);
                    prepStmt.setString(3, localRole);
                    prepStmt.executeUpdate();
                } else {
                    String msg = "Cannot find Identity Provider role " + entry.getKey() + " for tenant " + tenantDomain;
                    log.error(msg);
                    throw new IdentityProviderMgtException(msg);
                }
            }
        }
        if(!addedRoleMappings.isEmpty()){
            for(Map.Entry<String,String> entry : addedRoleMappings.entrySet()){
                if(roleIdMap.containsKey(entry.getKey())){
                    int idpRoleId = roleIdMap.get(entry.getKey());
                    String localRole = entry.getValue();
                    sqlStmt = IdentityProviderMgtConstants.SQLQueries.ADD_TENANT_IDP_ROLE_MAPPINGS_SQL;
                    prepStmt = conn.prepareStatement(sqlStmt);
                    prepStmt.setInt(1, idpRoleId);
                    prepStmt.setInt(2, tenantId);
                    prepStmt.setString(3, localRole);
                    prepStmt.executeUpdate();
                } else {
                    String msg = "Cannot find Identity Provider role " + entry.getKey() + " for tenant " + tenantDomain;
                    log.error(msg);
                    throw new IdentityProviderMgtException(msg);
                }
            }
        }
    }

    private void doDeleteIdP(Connection conn, int tenantId, String idPName) throws SQLException {
        PreparedStatement prepStmt = null;
        String sqlStmt = IdentityProviderMgtConstants.SQLQueries.DELETE_TENANT_IDP_SQL;
        prepStmt = conn.prepareStatement(sqlStmt);
        prepStmt.setInt(1, tenantId);
        prepStmt.setString(2, idPName);
        prepStmt.executeUpdate();
    }

    public int isTenantIdPExisting(Connection dbConnection, TrustedIdPDO trustedIdPDO, int tenantId, String tenantDomain)
            throws IdentityProviderMgtException {

        boolean dbConnInitialized = true;
        PreparedStatement prepStmt = null;
        try {
            if(dbConnection == null){
                dbConnection = IdentityProviderMgtUtil.getDBConnection();
            } else {
                dbConnInitialized = false;
            }
            String sqlStmt = IdentityProviderMgtConstants.SQLQueries.IS_EXISTING_TENANT_IDP_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, trustedIdPDO.getIdPName());
            ResultSet rs = prepStmt.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while checking if Identity Provider exists with name : " +
                    trustedIdPDO.getIdPName() + " and for tenant : " + tenantDomain;
            log.error(msg, e);
            throw new IdentityProviderMgtException(msg);
        } finally {
            if(dbConnInitialized){
                IdentityProviderMgtUtil.closeConnection(dbConnection);
            }
        }
        return 0;
    }

    public int isPrimaryIdPExisting(Connection dbConnection, TrustedIdPDO trustedIdPDO, int tenantId,
                                    String tenantDomain) throws IdentityProviderMgtException {
        PreparedStatement prepStmt = null;
        try {
            String sqlStmt = IdentityProviderMgtConstants.SQLQueries.IS_EXISTING_PRIMARY_TENANT_IDP_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, "1");
            ResultSet rs = prepStmt.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            String msg = "Error occurred checking if primary Identity Provider exists for tenant " + tenantDomain;
            log.error(msg, e);
            throw new IdentityProviderMgtException(msg);
        }
        return 0;
    }

    public void deleteTenantRole(int tenantId, String role, String tenantDomain) throws IdentityProviderMgtException {
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        try {
            dbConnection = IdentityProviderMgtUtil.getDBConnection();
            String sqlStmt = IdentityProviderMgtConstants.SQLQueries.DELETE_TENANT_ROLE_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, role);
            prepStmt.executeUpdate();
            dbConnection.commit();
        } catch (SQLException e) {
            String msg = "Error occurred while deleting tenant role " + role + " of tenant " + tenantDomain;
            log.error(msg, e);
            IdentityProviderMgtUtil.rollBack(dbConnection);
            throw new IdentityProviderMgtException(msg);
        } finally {
            IdentityProviderMgtUtil.closeConnection(dbConnection);
        }
    }

    public void renameTenantRole(String newRoleName, int tenantId, String oldRoleName, String tenantDomain)
            throws IdentityProviderMgtException {
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        try {
            dbConnection = IdentityProviderMgtUtil.getDBConnection();
            String sqlStmt = IdentityProviderMgtConstants.SQLQueries.RENAME_TENANT_ROLE_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, newRoleName);
            prepStmt.setInt(2, tenantId);
            prepStmt.setString(3, oldRoleName);
            prepStmt.executeUpdate();
            dbConnection.commit();
        } catch (SQLException e) {
            String msg = "Error occurred while renaming tenant role " + oldRoleName +
                    " to " + newRoleName + " of tenant " + tenantDomain;
            log.error(msg, e);
            IdentityProviderMgtUtil.rollBack(dbConnection);
            throw new IdentityProviderMgtException(msg);
        } finally {
            IdentityProviderMgtUtil.closeConnection(dbConnection);
        }
    }

}
