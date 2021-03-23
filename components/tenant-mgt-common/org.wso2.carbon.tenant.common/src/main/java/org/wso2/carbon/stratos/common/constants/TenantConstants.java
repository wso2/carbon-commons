/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.stratos.common.constants;

public class TenantConstants {

    /**
     * Enum for error messages.
     */
    public enum ErrorMessage {

        ERROR_CODE_EMPTY_EMAIL("TM-60000",
                "Provided email is empty."),
        ERROR_CODE_ILLEGAL_EMAIL("TM-60001",
                "Wrong characters in the email."),
        ERROR_CODE_INVALID_EMAIL("TM-60002",
                "Invalid email address is provided."),
        ERROR_CODE_UNAVAILABLE_DOMAIN("TM-60003", "You can not use a registry reserved word as a tenant domain. " +
                "Please choose a different one."),
        ERROR_CODE_EMPTY_DOMAIN_NAME("TM-60004",
                "Provided domain name is empty."),
        ERROR_CODE_EMPTY_EXTENSION("TM-60005",
                "You should have an extension to your domain."),
        ERROR_CODE_INVALID_DOMAIN("TM-60006",
                "Invalid domain. Domain should not start with '.'"),
        ERROR_CODE_ILLEGAL_CHARACTERS_IN_DOMAIN("TM-60007",
                "The tenant domain %s contains one or more illegal characters. The valid characters are lowercase " +
                        "letters, numbers, '.', '-' and '_'."),
        ERROR_CODE_EXISTING_USER_NAME("TM-60008",
                "User name : %s exists in the system. Please pick another user name for tenant Administrator."),
        ERROR_CODE_EXISTING_DOMAIN("TM-60009",
                "A tenant with same domain %s already exist. Please use a different domain name."),
        ERROR_CODE_INVALID_LIMIT("TM-60010", "Limit should not be negative."),
        ERROR_CODE_INVALID_OFFSET("TM-60011", "Offset should not be negative."),
        ERROR_CODE_OWNER_REQUIRED("TM-60012", "Required parameter owner is not specified."),
        ERROR_CODE_MISSING_REQUIRED_PARAMETER("TM-60013", "Required parameter %s is not specified."),
        ERROR_CODE_RESOURCE_NOT_FOUND("TM-60014", "Tenant cannot be found for the provided id: %s."),
        ERROR_CODE_DOMAIN_NOT_FOUND("TM-60015", "Tenant cannot be found for the provided domain: %s."),
        ERROR_CODE_TENANT_DELETION_NOT_ENABLED("TM-60016", "Tenant deletion property Tenant.TenantDelete is not " +
                "enabled in carbon.xml file."),
        ERROR_CODE_TENANT_DOES_NOT_MATCH_REGEX_PATTERN("TM-60017", "Invalid tenant domain: %s. " +
                "Domain should match the regex pattern %s.");

        private final String code;
        private final String message;

        ErrorMessage(String code, String message) {

            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}
