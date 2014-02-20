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
package org.wso2.carbon.idp.mgt.util;

public class IdentityProviderMgtConstants {

    public static class SQLQueries {

        public static final String GET_TENANT_IDPS_SQL = "SELECT UM_TENANT_IDP_NAME, UM_TENANT_IDP_HOME_REALM_ID " +
                "FROM UM_TENANT_IDP WHERE UM_TENANT_ID=?";
        public static final String GET_IDP_BY_NAME_SQL = "SELECT UM_ID, UM_TENANT_IDP_PRIMARY, " +
                "UM_TENANT_IDP_HOME_REALM_ID, UM_TENANT_IDP_THUMBPRINT, UM_TENANT_IDP_TOKEN_EP_ALIAS, " +
                "UM_TENANT_IDP_SAML2_SSO_ENABLED, UM_TENANT_IDP_ISSUER, UM_TENANT_IDP_SP_ENTITY_ID, UM_TENANT_IDP_URL, " +
                "UM_TENANT_IDP_AUTHN_REQUEST_SIGNED, UM_TENANT_IDP_LOGOUT_ENABLED, UM_TENANT_IDP_LOGOUT_URL, " +
                "UM_TENANT_IDP_LOGOUT_REQUEST_SIGNED, UM_TENANT_IDP_AUTHN_RESPONSE_SIGNED, UM_TENANT_IDP_OIDC_ENABLED, " +
                "UM_TENANT_IDP_CONSUMER_KEY, UM_TENANT_IDP_CONSUMER_SECRET, UM_TENANT_IDP_AUTHZ_URL, " +
                "UM_TENANT_IDP_TOKEN_URL FROM UM_TENANT_IDP WHERE UM_TENANT_ID=? AND UM_TENANT_IDP_NAME=?";
        public static final String GET_IDP_BY_REALM_ID_SQL = "SELECT UM_ID, UM_TENANT_IDP_NAME, UM_TENANT_IDP_PRIMARY, " +
                "UM_TENANT_IDP_THUMBPRINT, UM_TENANT_IDP_TOKEN_EP_ALIAS, UM_TENANT_IDP_SAML2_SSO_ENABLED, " +
                "UM_TENANT_IDP_ISSUER, UM_TENANT_IDP_SP_ENTITY_ID, UM_TENANT_IDP_URL, UM_TENANT_IDP_AUTHN_REQUEST_SIGNED, " +
                "UM_TENANT_IDP_LOGOUT_ENABLED, UM_TENANT_IDP_LOGOUT_URL, UM_TENANT_IDP_LOGOUT_REQUEST_SIGNED, " +
                "UM_TENANT_IDP_AUTHN_RESPONSE_SIGNED, UM_TENANT_IDP_OIDC_ENABLED, UM_TENANT_IDP_CONSUMER_KEY, " +
                "UM_TENANT_IDP_CONSUMER_SECRET, UM_TENANT_IDP_AUTHZ_URL, UM_TENANT_IDP_TOKEN_URL FROM UM_TENANT_IDP " +
                "WHERE UM_TENANT_ID=? AND UM_TENANT_IDP_HOME_REALM_ID=?";
        public static final String GET_TENANT_IDP_CLAIM_MAPPINGS_SQL = "SELECT UM_TENANT_CLAIM " +
                "FROM UM_TENANT_IDP_CLAIM_MAPPINGS WHERE UM_TENANT_IDP_CLAIM_ID=?";
        public static final String GET_TENANT_IDP_ROLE_MAPPINGS_SQL = "SELECT UM_TENANT_ROLE " +
                "FROM UM_TENANT_IDP_ROLE_MAPPINGS WHERE UM_TENANT_IDP_ROLE_ID=?";
        public static final String UPDATE_TENANT_IDP_SQL = "UPDATE UM_TENANT_IDP SET UM_TENANT_IDP_NAME=?, " +
                "UM_TENANT_IDP_PRIMARY=?, UM_TENANT_IDP_HOME_REALM_ID=?, UM_TENANT_IDP_THUMBPRINT=?, " +
                "UM_TENANT_IDP_TOKEN_EP_ALIAS=?, UM_TENANT_IDP_SAML2_SSO_ENABLED=?, UM_TENANT_IDP_ISSUER=?, " +
                "UM_TENANT_IDP_SP_ENTITY_ID=?, UM_TENANT_IDP_URL=?, UM_TENANT_IDP_AUTHN_REQUEST_SIGNED=?, " +
                "UM_TENANT_IDP_LOGOUT_ENABLED=?, UM_TENANT_IDP_LOGOUT_URL=?, UM_TENANT_IDP_LOGOUT_REQUEST_SIGNED=?, " +
                "UM_TENANT_IDP_AUTHN_RESPONSE_SIGNED=?, UM_TENANT_IDP_OIDC_ENABLED=?, UM_TENANT_IDP_CONSUMER_KEY=?, " +
                "UM_TENANT_IDP_CONSUMER_SECRET=?, UM_TENANT_IDP_AUTHZ_URL=?, UM_TENANT_IDP_TOKEN_URL=? " +
                "WHERE UM_TENANT_ID=? AND UM_TENANT_IDP_NAME=?";
        public static final String ADD_TENANT_IDP_CLAIMS_SQL = "INSERT INTO UM_TENANT_IDP_CLAIMS (UM_TENANT_IDP_ID, " +
                "UM_TENANT_IDP_CLAIM) VALUES (?, ?)";
        public static final String DELETE_TENANT_IDP_CLAIMS_SQL = "DELETE FROM UM_TENANT_IDP_CLAIMS " +
                "WHERE (UM_TENANT_IDP_ID=? AND UM_TENANT_IDP_CLAIM=?)";
        public static final String UPDATE_TENANT_IDP_CLAIMS_SQL = "UPDATE UM_TENANT_IDP_CLAIMS SET " +
                "UM_TENANT_IDP_CLAIM=? WHERE (UM_TENANT_IDP_ID=? AND UM_TENANT_IDP_CLAIM=?)";
        public static final String GET_TENANT_IDP_CLAIMS_SQL = "SELECT UM_ID, UM_TENANT_IDP_CLAIM " +
                "FROM UM_TENANT_IDP_CLAIMS WHERE UM_TENANT_IDP_ID=?";
        public static final String DELETE_TENANT_IDP_CLAIM_MAPPINGS_SQL = "DELETE FROM UM_TENANT_IDP_CLAIM_MAPPINGS " +
                "WHERE (UM_TENANT_IDP_CLAIM_ID=? AND UM_TENANT_ID=? AND UM_TENANT_CLAIM=?)";
        public static final String ADD_TENANT_IDP_CLAIM_MAPPINGS_SQL = "INSERT INTO UM_TENANT_IDP_CLAIM_MAPPINGS " +
                "(UM_TENANT_IDP_CLAIM_ID, UM_TENANT_ID, UM_TENANT_CLAIM) VALUES (?, ?, ?)";
        public static final String ADD_TENANT_IDP_ROLES_SQL = "INSERT INTO UM_TENANT_IDP_ROLES (UM_TENANT_IDP_ID, " +
                "UM_TENANT_IDP_ROLE) VALUES (?, ?)";
        public static final String DELETE_TENANT_IDP_ROLES_SQL = "DELETE FROM UM_TENANT_IDP_ROLES " +
                "WHERE (UM_TENANT_IDP_ID=? AND UM_TENANT_IDP_ROLE=?)";
        public static final String UPDATE_TENANT_IDP_ROLES_SQL = "UPDATE UM_TENANT_IDP_ROLES SET UM_TENANT_IDP_ROLE=? " +
                "WHERE (UM_TENANT_IDP_ID=? AND UM_TENANT_IDP_ROLE=?)";
        public static final String GET_TENANT_IDP_ROLES_SQL = "SELECT UM_ID, UM_TENANT_IDP_ROLE " +
                "FROM UM_TENANT_IDP_ROLES WHERE UM_TENANT_IDP_ID=?";
        public static final String DELETE_TENANT_IDP_ROLE_MAPPINGS_SQL = "DELETE FROM UM_TENANT_IDP_ROLE_MAPPINGS " +
                "WHERE (UM_TENANT_IDP_ROLE_ID=? AND UM_TENANT_ID=? AND UM_TENANT_ROLE=?)";
        public static final String ADD_TENANT_IDP_ROLE_MAPPINGS_SQL = "INSERT INTO UM_TENANT_IDP_ROLE_MAPPINGS " +
                "(UM_TENANT_IDP_ROLE_ID, UM_TENANT_ID, UM_TENANT_ROLE) VALUES (?, ?, ?)";
        public static final String ADD_TENANT_IDP_SQL = "INSERT INTO UM_TENANT_IDP (UM_TENANT_ID, UM_TENANT_IDP_NAME, " +
                "UM_TENANT_IDP_PRIMARY, UM_TENANT_IDP_HOME_REALM_ID, UM_TENANT_IDP_THUMBPRINT, " +
                "UM_TENANT_IDP_TOKEN_EP_ALIAS, UM_TENANT_IDP_SAML2_SSO_ENABLED, UM_TENANT_IDP_ISSUER, " +
                "UM_TENANT_IDP_SP_ENTITY_ID, UM_TENANT_IDP_URL, UM_TENANT_IDP_AUTHN_REQUEST_SIGNED, " +
                "UM_TENANT_IDP_LOGOUT_ENABLED, UM_TENANT_IDP_LOGOUT_URL, UM_TENANT_IDP_LOGOUT_REQUEST_SIGNED, " +
                "UM_TENANT_IDP_AUTHN_RESPONSE_SIGNED, UM_TENANT_IDP_OIDC_ENABLED, UM_TENANT_IDP_CONSUMER_KEY, " +
                "UM_TENANT_IDP_CONSUMER_SECRET, UM_TENANT_IDP_AUTHZ_URL, UM_TENANT_IDP_TOKEN_URL) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        public static final String DELETE_TENANT_IDP_SQL = "DELETE FROM UM_TENANT_IDP " +
                "WHERE (UM_TENANT_ID=? AND UM_TENANT_IDP_NAME=?)";
        public static final String IS_EXISTING_TENANT_IDP_SQL = "SELECT UM_ID FROM UM_TENANT_IDP " +
                "WHERE (UM_TENANT_ID=? AND UM_TENANT_IDP_NAME=?)";
        public static final String IS_EXISTING_PRIMARY_TENANT_IDP_SQL = "SELECT UM_ID FROM UM_TENANT_IDP " +
                "WHERE (UM_TENANT_ID=? AND UM_TENANT_IDP_PRIMARY=?)";
        public static final String SWITCH_TENANT_IDP_PRIMARY_SQL = "UPDATE UM_TENANT_IDP SET UM_TENANT_IDP_PRIMARY=? " +
                "WHERE (UM_TENANT_ID=? AND UM_TENANT_IDP_PRIMARY=?)";
        public static final String SWITCH_TENANT_IDP_PRIMARY_ON_DELETE_SQL = "UPDATE UM_TENANT_IDP " +
                "SET UM_TENANT_IDP_PRIMARY=? WHERE (UM_TENANT_ID=? AND UM_TENANT_IDP_NAME=? AND UM_TENANT_IDP_PRIMARY=?)";
        public static final String DELETE_TENANT_ROLE_SQL = "DELETE FROM UM_TENANT_IDP_ROLE_MAPPINGS " +
                "WHERE (UM_TENANT_ID=? AND UM_TENANT_ROLE=?)";
        public static final String RENAME_TENANT_ROLE_SQL = "UPDATE UM_TENANT_IDP_ROLE_MAPPINGS SET UM_TENANT_ROLE=? " +
                "WHERE (UM_TENANT_ID=? AND UM_TENANT_ROLE=?)";
    }

    public class StatusCodes {

        public static final String IDENTITY_PROVIDER_ERROR = "urn:oasis:names:tc:SAML:2.0:status:Responder";
        public static final String NO_PASSIVE = "urn:oasis:names:tc:SAML:2.0:status:NoPassive";

    }
}
