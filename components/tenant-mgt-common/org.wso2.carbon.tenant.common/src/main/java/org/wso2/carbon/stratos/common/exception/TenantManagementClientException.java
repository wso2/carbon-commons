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
package org.wso2.carbon.stratos.common.exception;

import org.wso2.carbon.stratos.common.constants.TenantConstants;

/**
 * A custom Java {@code TenantManagementClientException} class used for the tenant management client error handling.
 */
public class TenantManagementClientException extends TenantMgtException {

    public TenantManagementClientException(String errorCode, String errorDescription) {

        super(errorCode, errorDescription);
    }

    public TenantManagementClientException(TenantConstants.ErrorMessage error) {

        super(error.getCode(), error.getMessage());
    }
}
