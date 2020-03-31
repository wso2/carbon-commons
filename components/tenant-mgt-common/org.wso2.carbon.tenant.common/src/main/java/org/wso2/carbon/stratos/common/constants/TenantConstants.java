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

        ERROR_CODE_EMPTY_EMAIL("100001",
                "Provided email is empty."),
        ERROR_CODE_ILLEGAL_EMAIL("100002",
                "Wrong characters in the email."),
        ERROR_CODE_INVALID_EMAIL("100003",
                "Invalid email address is provided."),
        ERROR_CODE_UNAVAILABLE_DOMAIN("100004", "You can not use a registry reserved word: %s as a tenant domain. " +
                "Please choose a different one.");

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
